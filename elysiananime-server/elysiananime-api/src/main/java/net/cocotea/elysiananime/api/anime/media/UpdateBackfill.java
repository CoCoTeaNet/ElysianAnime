package net.cocotea.elysiananime.api.anime.media;

import lombok.Data;
import net.cocotea.elysiananime.util.SnowflakeIds;
import org.noear.solon.annotation.Component;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.solon.annotation.Db;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 更新动态回填：每部番只回填最新一集，create_time 取文件 mtime，避免历史刷屏。
 * <p>
 * 依赖 {@code ani_media}，不能放进迁移脚本；与进度回填分属两次发布，标记独立
 * （{@code 3.1.0-backfill-update}）。规则见 doc/plan/playback-media-rework.md §4.7。
 *
 * @author CoCoTea
 * @since 3.1.0
 */
@Component
public class UpdateBackfill {

    private static final Logger log = LoggerFactory.getLogger(UpdateBackfill.class);

    /**
     * 回填标记版本号，与进度回填的标记分开
     */
    public static final String VERSION = "3.1.0-backfill-update";

    private static final String SCRIPT_NAME = "update-backfill";

    private static final String CHECKSUM = "-";

    /**
     * 每部作品取「正片最后一集，无正片则任意内容最后一集」，与扫描器的 latest 语义一致
     */
    private static final String LATEST_MEDIA_SQL = """
            select id, opus_id, episode_key, episode_no, file_mtime
            from (
                select m.id, m.opus_id, m.episode_key, m.episode_no, m.file_mtime,
                       row_number() over (
                           partition by m.opus_id
                           order by (m.episode_no is not null) desc, m.sort_no desc, m.id desc
                       ) as rn
                from ani_media m
                where m.is_deleted = 0
            ) t
            where t.rn = 1
            """;

    private static final int BATCH_SIZE = 500;

    @Db
    private LightDao lightDao;

    /**
     * 幂等入口：已回填过就直接跳过
     */
    public void runIfNeeded() {
        try {
            Long count = lightDao.getCount("sys_schema_version_count", Map.of("version", VERSION));
            if (count != null && count > 0L) {
                return;
            }
            int backfilled = backfill();
            markDone();
            log.info("更新动态回填完成，共回填 {} 部作品的最新一集", backfilled);
        } catch (Exception e) {
            log.error("更新动态回填失败，下次启动会重试", e);
        }
    }

    private int backfill() {
        Map<String, Object> params = new HashMap<>(2);
        List<UpdateRow> rows = lightDao.find(LATEST_MEDIA_SQL, params, UpdateRow.class);
        if (rows == null || rows.isEmpty()) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        int count = 0;
        for (int from = 0; from < rows.size(); from += BATCH_SIZE) {
            List<UpdateRow> chunk = rows.subList(from, Math.min(from + BATCH_SIZE, rows.size()));
            StringBuilder sql = new StringBuilder("""
                    insert into ani_opus_update
                        (id, opus_id, media_id, episode_key, episode_no, source, create_time, update_time, is_deleted)
                    values """);
            List<Object> args = new ArrayList<>(chunk.size() * 8);
            for (int i = 0; i < chunk.size(); i++) {
                UpdateRow row = chunk.get(i);
                if (i > 0) {
                    sql.append(", ");
                }
                sql.append("(?, ?, ?, ?, ?, 1, ?, ?, 0)");
                args.add(SnowflakeIds.nextId());
                args.add(row.opusId);
                args.add(row.id);
                args.add(row.episodeKey);
                args.add(row.episodeNo);
                // create_time 取文件 mtime；upsert 不覆盖它，重跑不重复播报
                args.add(row.fileMtime == null ? now : row.fileMtime);
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

    private void markDone() {
        lightDao.executeSql("""
                        insert ignore into sys_schema_version (version, script_name, checksum, apply_time)
                        values (?, ?, ?, ?)
                        """,
                VERSION, SCRIPT_NAME, CHECKSUM, LocalDateTime.now());
    }

    /**
     * 每部番最新一集的媒体行（查询结果投影）
     */
    @Data
    public static class UpdateRow {
        private java.math.BigInteger id;
        private java.math.BigInteger opusId;
        private String episodeKey;
        private java.math.BigDecimal episodeNo;
        private LocalDateTime fileMtime;
    }
}
