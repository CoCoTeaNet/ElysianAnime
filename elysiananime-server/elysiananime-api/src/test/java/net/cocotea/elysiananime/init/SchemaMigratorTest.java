package net.cocotea.elysiananime.init;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 迁移脚本解析测试（不连库）
 *
 * @author CoCoTea
 */
public class SchemaMigratorTest {

    @Test
    public void versionOf() {
        Assertions.assertEquals("3.1.0", SchemaMigrator.versionOf("V3_1_0__media_and_progress.sql"));
        Assertions.assertEquals("3.1.1", SchemaMigrator.versionOf("V3_1_1__drop_notify.sql"));
    }

    @Test
    public void versionOfWithoutSeparator() {
        Assertions.assertThrows(IllegalStateException.class, () -> SchemaMigrator.versionOf("media.sql"));
    }

    @Test
    public void compareVersion() {
        Assertions.assertTrue(SchemaMigrator.compareVersion("3.1.0", "3.1.1") < 0);
        Assertions.assertTrue(SchemaMigrator.compareVersion("3.1.1", "3.1.0") > 0);
        Assertions.assertEquals(0, SchemaMigrator.compareVersion("3.1.0", "3.1"));
        Assertions.assertTrue(SchemaMigrator.compareVersion("3.1", "3.10") < 0);
    }

    @Test
    public void splitStatementsIgnoresCommentsAndQuotes() {
        String sql = """
                -- 行注释里有分号; 不能算语句结束
                CREATE TABLE `a` (`id` bigint NOT NULL COMMENT '0否;1是');
                /* 块注释 ; 也不是语句 */
                UPDATE `a` SET `id` = 1 WHERE `id` = 2;
                INSERT INTO `a` VALUES ('it''s; ok');
                """;
        List<String> statements = SchemaMigrator.splitStatements(sql);
        Assertions.assertEquals(3, statements.size());
        Assertions.assertTrue(statements.get(0).startsWith("CREATE TABLE"));
        Assertions.assertTrue(statements.get(1).startsWith("UPDATE"));
        Assertions.assertTrue(statements.get(2).contains("'it''s; ok'"));
    }

    @Test
    public void splitRealMigrationScript() throws IOException {
        String path = "db/migration/V3_1_0__media_and_progress.sql";
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            Assertions.assertNotNull(is, "迁移脚本必须打进 classpath：" + path);
            String script = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            List<String> statements = SchemaMigrator.splitStatements(script);
            Assertions.assertEquals(19, statements.size());
            for (String statement : statements) {
                Assertions.assertFalse(statement.startsWith("--"), "注释不能被当成语句：" + statement);
                Assertions.assertTrue(statement.length() > 10, "语句不能是空的");
            }
        }
    }
}
