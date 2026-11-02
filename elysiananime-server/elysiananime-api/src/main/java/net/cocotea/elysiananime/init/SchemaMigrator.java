package net.cocotea.elysiananime.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 启动期数据库迁移执行器。
 * <p>
 * 必须在 HTTP 开始服务前完成，因此由 {@code Solon.start} 的回调驱动，直接用 {@link DriverManager}
 * 开一条物理连接（回调阶段容器 bean 尚未创建，拿不到 DataSource）。
 * <p>
 * 脚本约定：{@code classpath:db/migration/V<版本>__<描述>.sql}，按文件名顺序执行；脚本本身必须可重跑，
 * 执行器按 {@code information_schema} 跳过已生效的列、索引。
 *
 * @author CoCoTea
 * @since 3.1.0
 */
public final class SchemaMigrator {

    private static final Logger log = LoggerFactory.getLogger(SchemaMigrator.class);

    private static final String SCRIPT_DIR = "db/migration";
    private static final String LOCK_NAME = "ea_schema_migrate";
    private static final int LOCK_TIMEOUT_SECONDS = 60;
    private static final String BASELINE_SCRIPT = "baseline";
    private static final String BASELINE_CHECKSUM = "-";

    /**
     * 结构探测：三张业务表 + 存量表新增列全部存在，才算"已经是 3.1.0 的全量库"
     */
    private static final List<String> BASELINE_TABLES = List.of("ani_media", "ani_user_opus_progress", "ani_opus_update");
    private static final Map<String, List<String>> BASELINE_COLUMNS = Map.of(
            "ani_user_opus", List.of("last_episode_key", "last_media_id", "last_read_time"),
            "ani_opus", List.of("media_dir", "media_count", "media_latest_key", "media_scan_time")
    );

    private static final Pattern ADD_COLUMN = Pattern.compile(
            "^ALTER\\s+TABLE\\s+`?(\\w+)`?\\s+ADD\\s+COLUMN\\s+`?(\\w+)`?", Pattern.CASE_INSENSITIVE);
    private static final Pattern ADD_INDEX = Pattern.compile(
            "^ALTER\\s+TABLE\\s+`?(\\w+)`?\\s+ADD\\s+(?:UNIQUE\\s+)?(?:KEY|INDEX)\\s+`?(\\w+)`?", Pattern.CASE_INSENSITIVE);
    private static final Pattern DROP_INDEX = Pattern.compile(
            "^ALTER\\s+TABLE\\s+`?(\\w+)`?\\s+DROP\\s+(?:INDEX|KEY)\\s+`?(\\w+)`?\\s*$", Pattern.CASE_INSENSITIVE);

    private final String jdbcUrl;
    private final String username;
    private final String password;

    public SchemaMigrator(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password == null ? "" : password;
    }

    public void migrate() {
        List<Script> scripts = loadScripts();
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            if (!acquireLock(conn)) {
                throw new IllegalStateException("另一个实例正在执行数据库迁移（锁 " + LOCK_NAME + " 获取超时）");
            }
            try {
                doMigrate(conn, scripts);
            } finally {
                releaseLock(conn);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("数据库迁移失败：" + e.getMessage(), e);
        }
    }

    private void doMigrate(Connection conn, List<Script> scripts) throws SQLException {
        String database = currentDatabase(conn);
        ensureVersionTable(conn);

        Map<String, String> applied = loadApplied(conn);
        if (applied.isEmpty()) {
            String baselineVersion = detectBaseline(conn, database, scripts);
            if (baselineVersion != null) {
                insertVersion(conn, baselineVersion, BASELINE_SCRIPT, BASELINE_CHECKSUM);
                applied = loadApplied(conn);
                log.info("数据库迁移 >>> 检测到已是最新全量结构，写入基线 {}", baselineVersion);
            }
        }

        String maxVersion = applied.keySet().stream()
                .filter(version -> !isMarkerVersion(version))
                .max(SchemaMigrator::compareVersion)
                .orElse(null);
        for (Script script : scripts) {
            String checksum = applied.get(script.version);
            if (checksum != null) {
                // 基线与一次性回填标记不是脚本，没有 checksum
                if (!BASELINE_CHECKSUM.equals(checksum) && !checksum.equals(script.checksum)) {
                    throw new IllegalStateException("迁移脚本 " + script.name
                            + " 的内容与已执行记录不一致（checksum " + checksum + " != " + script.checksum + "）");
                }
                continue;
            }
            if (maxVersion != null && compareVersion(maxVersion, script.version) >= 0) {
                continue;
            }
            apply(conn, database, script);
            insertVersion(conn, script.version, script.name, script.checksum);
            maxVersion = script.version;
        }
    }

    private void apply(Connection conn, String database, Script script) throws SQLException {
        List<String> statements = splitStatements(script.sql);
        log.info("数据库迁移 >>> 开始执行 {}（{} 条语句）", script.name, statements.size());
        for (String statement : statements) {
            executeStatement(conn, database, statement);
        }
        log.info("数据库迁移 >>> 执行完成 {}", script.name);
    }

    /**
     * MySQL 8 的 ADD COLUMN / ADD INDEX / DROP INDEX 都不支持 IF [NOT] EXISTS，
     * 只能先查 information_schema 再决定执行或跳过。
     */
    private void executeStatement(Connection conn, String database, String statement) throws SQLException {
        Matcher column = ADD_COLUMN.matcher(statement);
        if (column.find() && columnExists(conn, database, column.group(1), column.group(2))) {
            log.debug("数据库迁移 >>> 列已存在，跳过 {}.{}", column.group(1), column.group(2));
            return;
        }
        Matcher index = ADD_INDEX.matcher(statement);
        if (index.find() && indexExists(conn, database, index.group(1), index.group(2))) {
            log.debug("数据库迁移 >>> 索引已存在，跳过 {}.{}", index.group(1), index.group(2));
            return;
        }
        Matcher drop = DROP_INDEX.matcher(statement);
        if (drop.find() && !indexExists(conn, database, drop.group(1), drop.group(2))) {
            log.debug("数据库迁移 >>> 索引不存在，跳过删除 {}.{}", drop.group(1), drop.group(2));
            return;
        }
        try (Statement st = conn.createStatement()) {
            st.execute(statement);
        } catch (SQLException e) {
            throw new SQLException("执行失败：" + abbreviate(statement), e);
        }
    }

    private void ensureVersionTable(Connection conn) throws SQLException {
        String ddl = """
                CREATE TABLE IF NOT EXISTS `sys_schema_version` (
                  `version`     varchar(64)  NOT NULL COMMENT '脚本版本号，如 3.1.0',
                  `script_name` varchar(255) NOT NULL COMMENT '脚本文件名',
                  `checksum`    varchar(64)  NOT NULL COMMENT '脚本内容 MD5',
                  `apply_time`  datetime     NOT NULL COMMENT '执行时间',
                  PRIMARY KEY (`version`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='数据库版本记录表'
                """;
        try (Statement st = conn.createStatement()) {
            st.execute(ddl);
        }
    }

    private Map<String, String> loadApplied(Connection conn) throws SQLException {
        Map<String, String> applied = new LinkedHashMap<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT `version`, `checksum` FROM `sys_schema_version`")) {
            while (rs.next()) {
                applied.put(rs.getString(1), rs.getString(2));
            }
        }
        return applied;
    }

    private void insertVersion(Connection conn, String version, String scriptName, String checksum) throws SQLException {
        String sql = "INSERT INTO `sys_schema_version` (`version`, `script_name`, `checksum`, `apply_time`) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, version);
            ps.setString(2, scriptName);
            ps.setString(3, checksum);
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        }
    }

    /**
     * 全量初始化脚本已经把结构建到最新，版本表仍是空的时候按结构探测认定基线，避免重复跑脚本
     */
    private String detectBaseline(Connection conn, String database, List<Script> scripts) throws SQLException {
        if (scripts.isEmpty()) {
            return null;
        }
        for (String table : BASELINE_TABLES) {
            if (!tableExists(conn, database, table)) {
                return null;
            }
        }
        for (Map.Entry<String, List<String>> entry : BASELINE_COLUMNS.entrySet()) {
            for (String column : entry.getValue()) {
                if (!columnExists(conn, database, entry.getKey(), column)) {
                    return null;
                }
            }
        }
        return scripts.get(0).version;
    }

    private boolean tableExists(Connection conn, String database, String table) throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?";
        return count(conn, sql, database, table) > 0;
    }

    private boolean columnExists(Connection conn, String database, String table, String column) throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND COLUMN_NAME = ?";
        return count(conn, sql, database, table, column) > 0;
    }

    private boolean indexExists(Connection conn, String database, String table, String index) throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND INDEX_NAME = ?";
        return count(conn, sql, database, table, index) > 0;
    }

    private long count(Connection conn, String sql, String... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        }
    }

    private String currentDatabase(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT DATABASE()")) {
            return rs.next() ? rs.getString(1) : null;
        }
    }

    private boolean acquireLock(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT GET_LOCK(?, ?)")) {
            ps.setString(1, LOCK_NAME);
            ps.setInt(2, LOCK_TIMEOUT_SECONDS);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) == 1;
            }
        }
    }

    private void releaseLock(Connection conn) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT RELEASE_LOCK(?)")) {
            ps.setString(1, LOCK_NAME);
            ps.executeQuery().close();
        } catch (SQLException e) {
            log.warn("数据库迁移 >>> 释放锁失败（连接关闭时会自动释放）：{}", e.getMessage());
        }
    }

    private List<Script> loadScripts() {
        Map<String, String> resources = listScriptResources();
        if (resources.isEmpty()) {
            throw new IllegalStateException("未找到任何迁移脚本：classpath:" + SCRIPT_DIR);
        }
        List<Script> scripts = new ArrayList<>();
        for (Map.Entry<String, String> entry : resources.entrySet()) {
            String name = entry.getKey();
            scripts.add(new Script(versionOf(name), name, readResource(entry.getValue())));
        }
        return scripts;
    }

    /**
     * @return 文件名 → classpath 资源路径，按文件名排序
     */
    private Map<String, String> listScriptResources() {
        Map<String, String> resources = new TreeMap<>();
        ClassLoader loader = SchemaMigrator.class.getClassLoader();
        try {
            Enumeration<URL> urls = loader.getResources(SCRIPT_DIR);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                if ("file".equals(url.getProtocol())) {
                    try (Stream<Path> files = Files.list(Paths.get(url.toURI()))) {
                        files.filter(Files::isRegularFile)
                                .map(p -> p.getFileName().toString())
                                .filter(n -> n.endsWith(".sql"))
                                .forEach(n -> resources.putIfAbsent(n, SCRIPT_DIR + "/" + n));
                    }
                } else if ("jar".equals(url.getProtocol())) {
                    JarURLConnection connection = (JarURLConnection) url.openConnection();
                    try (JarFile jar = connection.getJarFile()) {
                        String prefix = SCRIPT_DIR + "/";
                        List<String> names = jar.stream()
                                .map(JarEntry::getName)
                                .filter(n -> n.startsWith(prefix) && n.endsWith(".sql"))
                                .filter(n -> n.indexOf('/', prefix.length()) < 0)
                                .collect(Collectors.toList());
                        for (String n : names) {
                            resources.putIfAbsent(n.substring(prefix.length()), n);
                        }
                    }
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException("读取迁移脚本目录失败：" + e.getMessage(), e);
        }
        return resources;
    }

    private String readResource(String path) {
        try (InputStream is = SchemaMigrator.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new IllegalStateException("迁移脚本不存在：" + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("读取迁移脚本失败：" + path, e);
        }
    }

    /**
     * V3_1_0__media_and_progress.sql → 3.1.0
     */
    static String versionOf(String fileName) {
        String body = fileName;
        if (body.startsWith("V") || body.startsWith("v")) {
            body = body.substring(1);
        }
        int end = body.indexOf("__");
        if (end < 0) {
            throw new IllegalStateException("迁移脚本命名不合法（缺少 __）：" + fileName);
        }
        return body.substring(0, end).replace('_', '.');
    }

    static int compareVersion(String left, String right) {
        String[] a = left.split("\\.");
        String[] b = right.split("\\.");
        int length = Math.max(a.length, b.length);
        for (int i = 0; i < length; i++) {
            int ai = i < a.length ? parsePart(a[i]) : 0;
            int bi = i < b.length ? parsePart(b[i]) : 0;
            if (ai != bi) {
                return Integer.compare(ai, bi);
            }
        }
        return 0;
    }

    /**
     * 一次回填标记（如 3.1.0-backfill-progress）不是迁移脚本，不参与版本比较
     */
    static boolean isMarkerVersion(String version) {
        for (String part : version.split("\\.")) {
            if (part.isEmpty()) {
                return true;
            }
            for (int i = 0; i < part.length(); i++) {
                if (!Character.isDigit(part.charAt(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 版本号分段：允许带后缀（0-backfill-progress 取 0），没有数字则视为非法
     */
    private static int parsePart(String part) {
        int end = 0;
        while (end < part.length() && Character.isDigit(part.charAt(end))) {
            end++;
        }
        if (end == 0) {
            throw new IllegalStateException("版本号不合法：" + part);
        }
        return Integer.parseInt(part.substring(0, end));
    }

    /**
     * 按分号切分语句，忽略注释与引号内的分号；不使用 DELIMITER，脚本里也不要写存储过程
     */
    static List<String> splitStatements(String sql) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        char[] chars = sql.toCharArray();
        int i = 0;
        while (i < chars.length) {
            char c = chars[i];
            if (c == '-' && i + 1 < chars.length && chars[i + 1] == '-') {
                while (i < chars.length && chars[i] != '\n') {
                    i++;
                }
                continue;
            }
            if (c == '/' && i + 1 < chars.length && chars[i + 1] == '*') {
                i += 2;
                while (i + 1 < chars.length && !(chars[i] == '*' && chars[i + 1] == '/')) {
                    i++;
                }
                i = Math.min(chars.length, i + 2);
                continue;
            }
            if (c == '\'' || c == '"' || c == '`') {
                char quote = c;
                current.append(c);
                i++;
                while (i < chars.length) {
                    char q = chars[i];
                    current.append(q);
                    if (q == '\\' && quote != '`' && i + 1 < chars.length) {
                        current.append(chars[i + 1]);
                        i += 2;
                        continue;
                    }
                    if (q == quote) {
                        if (i + 1 < chars.length && chars[i + 1] == quote) {
                            current.append(chars[i + 1]);
                            i += 2;
                            continue;
                        }
                        i++;
                        break;
                    }
                    i++;
                }
                continue;
            }
            if (c == ';') {
                addStatement(statements, current);
                i++;
                continue;
            }
            current.append(c);
            i++;
        }
        addStatement(statements, current);
        return statements;
    }

    private static void addStatement(List<String> statements, StringBuilder current) {
        String statement = current.toString().trim();
        current.setLength(0);
        if (!statement.isEmpty()) {
            statements.add(statement);
        }
    }

    private static String md5(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            return HexFormat.of().formatHex(digest.digest(content.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String abbreviate(String statement) {
        String singleLine = statement.replaceAll("\\s+", " ");
        return singleLine.length() <= 120 ? singleLine : singleLine.substring(0, 120) + "...";
    }

    private static final class Script {
        private final String version;
        private final String name;
        private final String sql;
        private final String checksum;

        private Script(String version, String name, String sql) {
            this.version = version;
            this.name = name;
            this.sql = sql;
            this.checksum = md5(sql);
        }
    }
}
