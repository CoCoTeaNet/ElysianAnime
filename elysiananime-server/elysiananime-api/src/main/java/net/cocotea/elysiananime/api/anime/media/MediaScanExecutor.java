package net.cocotea.elysiananime.api.anime.media;

import cn.hutool.core.util.StrUtil;
import net.cocotea.elysiananime.api.anime.model.po.AniMedia;
import net.cocotea.elysiananime.api.anime.model.po.AniOpus;
import net.cocotea.elysiananime.properties.FileProp;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Tran;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.EntityQuery;
import org.sagacity.sqltoy.plugins.id.IdGenerator;
import org.sagacity.sqltoy.plugins.id.impl.SnowflakeIdGenerator;
import org.sagacity.sqltoy.solon.annotation.Db;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 单部作品的扫描执行器：遍历目录、对比索引、识别重命名、软删/复活、维护 is_primary 与作品聚合字段。
 * <p>
 * 每部作品一个事务，失败整部回滚，下一轮扫描可重来。扫描流程见
 * {@code doc/plan/playback-media-rework.md} §5.1。
 *
 * @author CoCoTea
 * @since 3.1.0
 */
@Component
public class MediaScanExecutor {

    private static final Logger log = LoggerFactory.getLogger(MediaScanExecutor.class);

    /**
     * 相对作品目录的最大层数：{@code {opus}/Season 1/第01话/a.mkv} 是第 3 层
     */
    private static final int MAX_DEPTH = 3;

    /**
     * 修改时间在这么多秒内的文件视为仍在写入，跳过且不参与软删
     */
    private static final long CHANGING_FILE_SECONDS = 60;

    private static final int ID_BATCH_SIZE = 500;

    @Db
    private LightDao lightDao;

    @Inject
    private FileProp fileProp;

    /**
     * 扫描一部作品
     *
     * @param opus   作品（需要 id / nameCn / mediaDir）
     * @param opusDir 作品目录（已确认存在）
     * @param source 触发来源，写入更新动态
     */
    @Tran
    public Outcome scanOpus(AniOpus opus, Path opusDir, int source) {
        Outcome outcome = new Outcome();
        LocalDateTime scanStartAt = LocalDateTime.now();
        Instant changingAfter = Instant.now().minusSeconds(CHANGING_FILE_SECONDS);
        String realRoot = MediaPathUtils.realPath(opusDir.toString());

        List<AniMedia> existing = lightDao.findEntity(AniMedia.class,
                EntityQuery.create().where("opus_id = :opusId").names("opusId").values(opus.getId()));
        Map<String, AniMedia> byHash = new HashMap<>(existing.size() * 2);
        for (AniMedia row : existing) {
            byHash.put(row.getFilePathHash(), row);
        }

        Set<String> seenHashes = new HashSet<>();
        List<AniMedia> seenRows = new ArrayList<>();
        Map<BigInteger, AniMedia> changed = new LinkedHashMap<>();
        List<ScannedFile> pendingAdds = new ArrayList<>();

        List<ScannedFile> files = walk(opusDir, realRoot, changingAfter, byHash, seenHashes, seenRows, outcome);

        for (ScannedFile file : files) {
            AniMedia row = byHash.get(file.filePathHash);
            if (row == null) {
                pendingAdds.add(file);
                continue;
            }
            seenHashes.add(file.filePathHash);
            seenRows.add(row);
            if (row.getIsDeleted() != null && row.getIsDeleted() == 1) {
                applyFile(row, file);
                row.setIsDeleted(0);
                changed.put(row.getId(), row);
                outcome.revived++;
                outcome.fresh.add(row);
            } else if (needsUpdate(row, file)) {
                applyFile(row, file);
                changed.put(row.getId(), row);
                outcome.updated++;
            }
        }

        List<AniMedia> removals = new ArrayList<>();
        for (AniMedia row : existing) {
            if (row.getIsDeleted() == 1 || seenHashes.contains(row.getFilePathHash())) {
                continue;
            }
            removals.add(row);
        }
        List<ScannedFile> adds = new ArrayList<>(pendingAdds);

        pairRenames(removals, adds, changed, seenHashes, seenRows, outcome);

        List<AniMedia> inserts = new ArrayList<>();
        for (ScannedFile file : adds) {
            AniMedia row = new AniMedia()
                    .setId(nextId())
                    .setOpusId(opus.getId())
                    .setFilePathHash(file.filePathHash)
                    .setExtension(file.extension)
                    .setEpisodeNo(file.episodeNo)
                    .setEpisodeKey(file.episodeKey)
                    .setDisplayName(file.displayName)
                    .setFileSize(file.fileSize)
                    .setFileMtime(file.fileMtime)
                    .setSortNo(file.sortNo)
                    .setIsPrimary(0)
                    .setLastSeenTime(scanStartAt)
                    .setIsDeleted(0);
            applyFile(row, file);
            inserts.add(row);
            outcome.added++;
            outcome.fresh.add(row);
        }

        // 本次扫描完整成功才软删，避免 NAS 掉线时把索引清空
        if (outcome.complete) {
            for (AniMedia row : removals) {
                boolean untouched = row.getLastSeenTime() == null
                        || row.getLastSeenTime().isBefore(scanStartAt);
                boolean stillMissing = StrUtil.isNotBlank(row.getFilePath())
                        && Files.notExists(Paths.get(row.getFilePath()));
                if (untouched && stillMissing) {
                    row.setIsDeleted(1);
                    changed.put(row.getId(), row);
                    outcome.removed++;
                }
            }
        }

        List<AniMedia> active = new ArrayList<>();
        for (AniMedia row : byHash.values()) {
            if (row.getIsDeleted() == 0) {
                active.add(row);
            }
        }
        active.addAll(inserts);
        markPrimary(active, changed);

        if (!inserts.isEmpty()) {
            lightDao.saveAll(inserts);
        }
        if (!changed.isEmpty()) {
            lightDao.updateAll(new ArrayList<>(changed.values()));
        }
        if (!seenRows.isEmpty()) {
            refreshLastSeen(seenRows, scanStartAt);
        }

        int mediaCount = 0;
        for (AniMedia row : active) {
            if (row.getIsPrimary() != null && row.getIsPrimary() == 1) {
                mediaCount++;
            }
        }
        String latestKey = latestKey(active);
        updateOpusAggregate(opus, opusDir, mediaCount, latestKey, scanStartAt);

        List<AniMedia> updateRows = new ArrayList<>();
        for (AniMedia row : outcome.fresh) {
            if (row.getIsPrimary() != null && row.getIsPrimary() == 1) {
                updateRows.add(row);
            }
        }
        outcome.updateRecords = upsertUpdates(updateRows, source, scanStartAt);
        outcome.mediaCount = mediaCount;
        outcome.latestKey = latestKey;
        return outcome;
    }

    /**
     * @return 扫描到的媒体文件（已按扩展名过滤、排除临时/隐藏文件）
     */
    private List<ScannedFile> walk(Path opusDir, String realRoot, Instant changingAfter,
                                   Map<String, AniMedia> byHash, Set<String> seenHashes,
                                   List<AniMedia> seenRows, Outcome outcome) {
        List<ScannedFile> result = new ArrayList<>();
        Set<String> mediaTypes = mediaTypes();
        try {
            Files.walkFileTree(opusDir, Collections.emptySet(), MAX_DEPTH, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (!dir.equals(opusDir) && dir.getFileName().toString().startsWith(".")) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    String name = file.getFileName().toString();
                    if (attrs.isRegularFile() && !name.startsWith(".") && !isTempFile(name)) {
                        String extension = extensionOf(name);
                        if (mediaTypes.contains(extension)) {
                            Path parent = file.getParent();
                            String dirName = parent.equals(opusDir) ? null : parent.getFileName().toString();
                            String filePath = MediaPathUtils.realPath(file.toString());
                            String normalized = MediaPathUtils.normalizeReal(filePath);
                            String hash = MediaPathUtils.md5(normalized);
                            if (attrs.lastModifiedTime().toInstant().isAfter(changingAfter)) {
                                // 仍在写入：不索引，但已存在的行要保留
                                AniMedia row = byHash.get(hash);
                                if (row != null && row.getIsDeleted() == 0) {
                                    seenHashes.add(hash);
                                    seenRows.add(row);
                                }
                                outcome.skipped++;
                                return FileVisitResult.CONTINUE;
                            }
                            if (attrs.size() <= 0) {
                                outcome.skipped++;
                                return FileVisitResult.CONTINUE;
                            }
                            EpisodeInfo info = EpisodeParser.parse(name, dirName);
                            ScannedFile scanned = new ScannedFile();
                            scanned.filePath = filePath;
                            scanned.filePathHash = hash;
                            scanned.relativePath = MediaPathUtils.relativeTo(realRoot, filePath);
                            scanned.fileName = name;
                            scanned.extension = extension;
                            scanned.episodeNo = info.episodeNo();
                            scanned.episodeKey = info.episodeKey();
                            scanned.displayName = info.displayName();
                            scanned.sortNo = info.sortNo();
                            scanned.fileSize = BigInteger.valueOf(attrs.size());
                            // 截断到秒：file_mtime 是无小数位的 datetime，
                            // 不截断的话每轮扫描都会判定为「已变更」，行被反复重写
                            scanned.fileMtime = LocalDateTime
                                    .ofInstant(attrs.lastModifiedTime().toInstant(), ZoneId.systemDefault())
                                    .withNano(0);
                            result.add(scanned);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    outcome.complete = false;
                    log.warn("扫盘失败，跳过：{}，原因：{}", file, exc.getMessage());
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            outcome.complete = false;
            log.error("遍历作品目录失败：{}", opusDir, e);
        }
        return result;
    }

    /**
     * 先配对重命名、再插入：否则新路径的 file_path_hash 已被占用，旧行改过去会撞唯一键
     */
    private void pairRenames(List<AniMedia> removals, List<ScannedFile> adds,
                             Map<BigInteger, AniMedia> changed, Set<String> seenHashes,
                             List<AniMedia> seenRows, Outcome outcome) {
        if (removals.isEmpty() || adds.isEmpty()) {
            return;
        }
        Map<String, List<AniMedia>> removalGroups = new HashMap<>();
        for (AniMedia row : removals) {
            removalGroups.computeIfAbsent(pairKey(row.getEpisodeKey(), row.getFileSize()), k -> new ArrayList<>()).add(row);
        }
        Map<String, List<ScannedFile>> addGroups = new HashMap<>();
        for (ScannedFile file : adds) {
            addGroups.computeIfAbsent(pairKey(file.episodeKey, file.fileSize), k -> new ArrayList<>()).add(file);
        }
        Set<AniMedia> pairedRemovals = new HashSet<>();
        Set<ScannedFile> pairedAdds = new HashSet<>();
        for (Map.Entry<String, List<ScannedFile>> entry : addGroups.entrySet()) {
            List<AniMedia> rows = removalGroups.get(entry.getKey());
            if (rows == null || rows.size() != 1 || entry.getValue().size() != 1) {
                continue;
            }
            applyRename(rows.get(0), entry.getValue().get(0), changed, seenHashes, seenRows, outcome);
            pairedRemovals.add(rows.get(0));
            pairedAdds.add(entry.getValue().get(0));
        }
        removals.removeAll(pairedRemovals);
        adds.removeAll(pairedAdds);

        // 无集数文件（X 键）改名后键会变，按同体积兜底配对；同体积候选多于一个时放弃
        Map<BigInteger, List<AniMedia>> xRemovals = new HashMap<>();
        for (AniMedia row : removals) {
            if (isXKey(row.getEpisodeKey())) {
                xRemovals.computeIfAbsent(row.getFileSize(), k -> new ArrayList<>()).add(row);
            }
        }
        Map<BigInteger, List<ScannedFile>> xAdds = new HashMap<>();
        for (ScannedFile file : adds) {
            if (isXKey(file.episodeKey)) {
                xAdds.computeIfAbsent(file.fileSize, k -> new ArrayList<>()).add(file);
            }
        }
        for (Map.Entry<BigInteger, List<ScannedFile>> entry : xAdds.entrySet()) {
            List<AniMedia> rows = xRemovals.get(entry.getKey());
            if (rows == null || rows.size() != 1 || entry.getValue().size() != 1) {
                continue;
            }
            AniMedia row = rows.get(0);
            ScannedFile file = entry.getValue().get(0);
            applyRename(row, file, changed, seenHashes, seenRows, outcome);
            removals.remove(row);
            adds.remove(file);
        }
    }

    private void applyRename(AniMedia row, ScannedFile file, Map<BigInteger, AniMedia> changed,
                             Set<String> seenHashes, List<AniMedia> seenRows, Outcome outcome) {
        applyFile(row, file);
        changed.put(row.getId(), row);
        seenHashes.add(file.filePathHash);
        seenRows.add(row);
        outcome.renamed++;
    }

    /**
     * 同集多文件只保留一个 {@code is_primary = 1}：体积最大优先，相同取 mtime 新的
     */
    private void markPrimary(List<AniMedia> active, Map<BigInteger, AniMedia> changed) {
        Map<String, List<AniMedia>> groups = new LinkedHashMap<>();
        for (AniMedia row : active) {
            String key = row.getEpisodeKey() == null ? "" : row.getEpisodeKey();
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
        }
        for (List<AniMedia> group : groups.values()) {
            AniMedia best = group.get(0);
            for (AniMedia row : group) {
                if (isBetterPrimary(row, best)) {
                    best = row;
                }
            }
            for (AniMedia row : group) {
                int expected = row == best ? 1 : 0;
                if (row.getIsPrimary() == null || row.getIsPrimary() != expected) {
                    row.setIsPrimary(expected);
                    if (row.getId() != null) {
                        changed.put(row.getId(), row);
                    }
                }
            }
        }
    }

    private boolean isBetterPrimary(AniMedia row, AniMedia current) {
        int size = nullSafe(row.getFileSize()).compareTo(nullSafe(current.getFileSize()));
        if (size != 0) {
            return size > 0;
        }
        LocalDateTime left = row.getFileMtime();
        LocalDateTime right = current.getFileMtime();
        if (left != null && right != null && !left.equals(right)) {
            return left.isAfter(right);
        }
        return nullSafe(row.getId()).compareTo(nullSafe(current.getId())) > 0;
    }

    private void refreshLastSeen(List<AniMedia> rows, LocalDateTime seenAt) {
        List<BigInteger> ids = new ArrayList<>(rows.size());
        for (AniMedia row : rows) {
            if (row.getId() != null) {
                ids.add(row.getId());
            }
        }
        for (int from = 0; from < ids.size(); from += ID_BATCH_SIZE) {
            List<BigInteger> chunk = ids.subList(from, Math.min(from + ID_BATCH_SIZE, ids.size()));
            String placeholders = String.join(",", Collections.nCopies(chunk.size(), "?"));
            Object[] args = new Object[chunk.size() + 1];
            args[0] = seenAt;
            for (int i = 0; i < chunk.size(); i++) {
                args[i + 1] = chunk.get(i);
            }
            lightDao.executeSql("update ani_media set last_seen_time = ? where id in (" + placeholders + ")", args);
        }
    }

    private void updateOpusAggregate(AniOpus opus, Path opusDir, int mediaCount, String latestKey,
                                     LocalDateTime scanTime) {
        lightDao.executeSql("""
                        update ani_opus
                        set media_dir = ?, media_count = ?, media_latest_key = ?, has_resource = ?,
                            media_scan_time = ?, update_time = ?
                        where id = ?
                        """,
                MediaPathUtils.realPath(opusDir.toString()),
                mediaCount,
                latestKey,
                mediaCount > 0 ? 1 : 0,
                scanTime,
                scanTime,
                opus.getId());
    }

    /**
     * 更新动态按 {@code (opus_id, episode_key)} 去重；{@code create_time} 取文件修改时间。
     * <p>
     * 一批多行写入：逐条 upsert 时 1 万个文件就是 1 万次数据库往返，扫描耗时直接翻十几倍。
     */
    private int upsertUpdates(List<AniMedia> rows, int source, LocalDateTime now) {
        int count = 0;
        for (int from = 0; from < rows.size(); from += ID_BATCH_SIZE) {
            List<AniMedia> chunk = rows.subList(from, Math.min(from + ID_BATCH_SIZE, rows.size()));
            StringBuilder sql = new StringBuilder("""
                    insert into ani_opus_update
                        (id, opus_id, media_id, episode_key, episode_no, source, create_time, update_time, is_deleted)
                    values """);
            List<Object> args = new ArrayList<>(chunk.size() * 8);
            for (int i = 0; i < chunk.size(); i++) {
                AniMedia row = chunk.get(i);
                if (i > 0) {
                    sql.append(", ");
                }
                sql.append("(?, ?, ?, ?, ?, ?, ?, ?, 0)");
                args.add(nextId());
                args.add(row.getOpusId());
                args.add(row.getId());
                args.add(row.getEpisodeKey());
                args.add(row.getEpisodeNo());
                args.add(source);
                args.add(row.getFileMtime() == null ? now : row.getFileMtime());
                args.add(now);
            }
            sql.append("""
                     on duplicate key update
                         media_id = values(media_id),
                         episode_no = values(episode_no),
                         source = values(source),
                         update_time = values(update_time),
                         is_deleted = 0
                    """);
            lightDao.executeSql(sql.toString(), args.toArray());
            count += chunk.size();
        }
        return count;
    }

    /**
     * 取正片里排序最后的一个；没有正片时退化为全部媒体里排序最后一个
     */
    private String latestKey(List<AniMedia> active) {
        AniMedia bestMain = null;
        AniMedia bestAny = null;
        for (AniMedia row : active) {
            if (row.getSortNo() == null) {
                continue;
            }
            if (bestAny == null || isLater(row, bestAny)) {
                bestAny = row;
            }
            if (row.getEpisodeNo() != null && (bestMain == null || isLater(row, bestMain))) {
                bestMain = row;
            }
        }
        if (bestMain != null) {
            return bestMain.getEpisodeKey();
        }
        if (bestAny != null) {
            return bestAny.getEpisodeKey();
        }
        AniMedia last = null;
        for (AniMedia row : active) {
            if (last == null || nullSafe(row.getId()).compareTo(nullSafe(last.getId())) > 0) {
                last = row;
            }
        }
        return last == null ? null : last.getEpisodeKey();
    }

    private boolean isLater(AniMedia row, AniMedia current) {
        int sort = row.getSortNo().compareTo(current.getSortNo());
        if (sort != 0) {
            return sort > 0;
        }
        return nullSafe(row.getId()).compareTo(nullSafe(current.getId())) > 0;
    }

    private void applyFile(AniMedia row, ScannedFile file) {
        row.setFilePath(file.filePath);
        row.setFilePathHash(file.filePathHash);
        row.setRelativePath(file.relativePath);
        row.setFileName(file.fileName);
        row.setExtension(file.extension);
        row.setEpisodeNo(file.episodeNo);
        row.setEpisodeKey(file.episodeKey);
        row.setDisplayName(file.displayName);
        row.setFileSize(file.fileSize);
        row.setFileMtime(file.fileMtime);
        row.setSortNo(file.sortNo);
    }

    private boolean needsUpdate(AniMedia row, ScannedFile file) {
        return !Objects.equals(row.getFileName(), file.fileName)
                || !Objects.equals(row.getExtension(), file.extension)
                || !Objects.equals(row.getRelativePath(), file.relativePath)
                || !Objects.equals(row.getEpisodeKey(), file.episodeKey)
                || !Objects.equals(row.getDisplayName(), file.displayName)
                || !equalsDecimal(row.getEpisodeNo(), file.episodeNo)
                || !equalsDecimal(row.getSortNo(), file.sortNo)
                || !Objects.equals(nullSafe(row.getFileSize()), file.fileSize)
                || !Objects.equals(row.getFileMtime(), file.fileMtime);
    }

    private boolean equalsDecimal(BigDecimal left, BigDecimal right) {
        if (left == null || right == null) {
            return left == right;
        }
        return left.compareTo(right) == 0;
    }

    private Set<String> mediaTypes() {
        Set<String> types = new HashSet<>();
        String raw = fileProp.getMediaFileType();
        if (StrUtil.isNotBlank(raw)) {
            Arrays.stream(raw.split(","))
                    .map(String::trim)
                    .filter(StrUtil::isNotBlank)
                    .map(s -> s.toLowerCase(Locale.ROOT))
                    .forEach(types::add);
        }
        return types;
    }

    private static String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot <= 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static boolean isTempFile(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        return lower.endsWith(".part") || lower.endsWith(".tmp")
                || lower.endsWith(".!qb") || lower.endsWith(".qb");
    }

    private static boolean isXKey(String episodeKey) {
        return episodeKey != null && episodeKey.startsWith("X");
    }

    private static String pairKey(String episodeKey, BigInteger size) {
        return (episodeKey == null ? "" : episodeKey) + "|" + nullSafe(size);
    }

    private static BigInteger nullSafe(BigInteger value) {
        return value == null ? BigInteger.ZERO : value;
    }

    private static BigInteger nextId() {
        IdGenerator generator = SnowflakeIdGenerator.getInstance();
        Object id = generator.getId(null, null, null, null, new Date(), null, 0, 0);
        return BigInteger.valueOf(((Number) id).longValue());
    }

    /**
     * 单部作品的扫描结果
     */
    public static class Outcome {
        public int added;
        public int updated;
        public int removed;
        public int revived;
        public int renamed;
        public int skipped;
        public int updateRecords;
        public int mediaCount;
        public String latestKey;
        /**
         * 目录遍历是否完整；失败时不软删
         */
        public boolean complete = true;
        /**
         * 新增/复活的媒体行，用于写更新动态
         */
        public final List<AniMedia> fresh = new ArrayList<>();
    }

    private static class ScannedFile {
        String filePath;
        String filePathHash;
        String relativePath;
        String fileName;
        String extension;
        BigDecimal episodeNo;
        String episodeKey;
        String displayName;
        BigInteger fileSize;
        LocalDateTime fileMtime;
        BigDecimal sortNo;
    }
}
