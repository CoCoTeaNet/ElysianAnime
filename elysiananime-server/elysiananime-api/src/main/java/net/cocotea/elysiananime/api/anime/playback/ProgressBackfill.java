package net.cocotea.elysiananime.api.anime.playback;

import cn.hutool.core.util.StrUtil;
import net.cocotea.elysiananime.api.anime.model.po.AniMedia;
import net.cocotea.elysiananime.api.anime.model.po.AniUserOpus;
import net.cocotea.elysiananime.util.SnowflakeIds;
import org.noear.solon.annotation.Component;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.solon.annotation.Db;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 历史播放进度回填：把旧列（reading_num / reading_time / total_time）搬进按集进度表。
 * <p>
 * 依赖 {@code ani_media}，所以不能放进迁移脚本，只能在首次扫描建好索引后由启动任务执行。
 * 规则见 doc/plan/playback-media-rework.md §4.7。执行一次即止，标记写在 {@code sys_schema_version}。
 *
 * @author CoCoTea
 * @since 3.1.0
 */
@Component
public class ProgressBackfill {

    private static final Logger log = LoggerFactory.getLogger(ProgressBackfill.class);

    /**
     * 回填标记版本号，与更新动态回填的标记分开
     */
    public static final String VERSION = "3.1.0-backfill-progress";

    private static final String SCRIPT_NAME = "progress-backfill";

    private static final String CHECKSUM = "-";

    /**
     * 位置超过总时长时的回退量：截断到「总时长 - 5 秒」
     */
    private static final long OVERFLOW_TRIM_MS = 5_000L;

    @Db
    private LightDao lightDao;

    /**
     * 幂等入口：已回填过就直接跳过
     */
    public void runIfNeeded() {
        try {
            if (isDone()) {
                return;
            }
            BackfillResult result = backfill();
            if (result.retryable() > 0) {
                // 集数键在媒体索引里还不存在，等下次扫描建好索引再回填，本次不写标记
                log.info("历史播放进度回填暂缓：{} 条的集数键还没进媒体索引，等下次扫描后重试", result.retryable());
                return;
            }
            markDone();
            log.info("历史播放进度回填完成，共回填 {} 条，跳过 {} 条", result.backfilled(), result.skipped());
        } catch (Exception e) {
            log.error("历史播放进度回填失败，下次启动会重试", e);
        }
    }

    private boolean isDone() {
        Long count = lightDao.getCount("sys_schema_version_count", Map.of("version", VERSION));
        return count != null && count > 0L;
    }

    private void markDone() {
        lightDao.executeSql("""
                        insert ignore into sys_schema_version (version, script_name, checksum, apply_time)
                        values (?, ?, ?, ?)
                        """,
                VERSION, SCRIPT_NAME, CHECKSUM, LocalDateTime.now());
    }

    private BackfillResult backfill() {
        Map<String, Object> params = new HashMap<>(2);
        List<AniUserOpus> rows = lightDao.find("ani_user_opus_listForBackfill", params, AniUserOpus.class);
        int backfilled = 0;
        int skipped = 0;
        int retryable = 0;
        for (AniUserOpus row : rows) {
            try {
                Outcome outcome = backfillOne(row);
                if (outcome == Outcome.BACKFILLED) {
                    backfilled++;
                } else if (outcome == Outcome.RETRY) {
                    retryable++;
                } else {
                    skipped++;
                }
            } catch (Exception e) {
                log.warn("回填单条进度失败：userOpusId={}, message={}", row.getId(), e.getMessage());
                retryable++;
            }
        }
        return new BackfillResult(backfilled, skipped, retryable);
    }

    private Outcome backfillOne(AniUserOpus row) {
        BigInteger userId = row.getUserId();
        BigInteger opusId = row.getOpusId();
        BigInteger userOpusId = row.getId();
        String readingNum = StrUtil.trimToNull(row.getReadingNum());
        long readingTime = row.getReadingTime() == null ? 0L : row.getReadingTime().longValue();
        long totalTime = row.getTotalTime() == null ? 0L : row.getTotalTime().longValue();
        if (userId == null || opusId == null || userOpusId == null || readingNum == null || readingTime <= 0L) {
            return Outcome.SKIPPED;
        }

        String episodeKey = PlaybackStateMachine.normalizeEpisodeKey(readingNum);
        if (episodeKey == null) {
            log.info("回填跳过，reading_num 无法归一成集数键：userOpusId={}, readingNum={}", userOpusId, readingNum);
            return Outcome.SKIPPED;
        }
        Map<String, Object> mediaParams = new HashMap<>(2);
        mediaParams.put("opusId", opusId);
        mediaParams.put("episodeKey", episodeKey);
        AniMedia media = lightDao.findOne("ani_media_findPrimaryByEpisodeKey", mediaParams, AniMedia.class);
        if (media == null) {
            log.info("回填暂缓，集数键在媒体索引里不存在：userOpusId={}, episodeKey={}", userOpusId, episodeKey);
            return Outcome.RETRY;
        }

        long positionMs = readingTime * 1000L;
        Long durationMs = totalTime > 0L ? totalTime * 1000L : null;
        if (durationMs != null && positionMs > durationMs) {
            positionMs = Math.max(0L, durationMs - OVERFLOW_TRIM_MS);
        }
        int isCompleted = durationMs != null && durationMs > 0L && positionMs >= (long) (durationMs * 0.95D) ? 1 : 0;
        LocalDateTime now = LocalDateTime.now();

        // 只补缺失的行，绝不覆盖改造后已经产生的新进度
        lightDao.executeSql("""
                        insert into ani_user_opus_progress
                            (id, user_id, opus_id, episode_key, position_ms, duration_ms, watched_ms, is_completed,
                             session_id, seq, create_by, create_time, update_by, update_time, is_deleted)
                        values (?, ?, ?, ?, ?, ?, ?, ?, null, 0, ?, ?, ?, ?, 0)
                        on duplicate key update id = id
                        """,
                SnowflakeIds.nextId(),
                userId,
                opusId,
                episodeKey,
                BigInteger.valueOf(positionMs),
                durationMs == null ? null : BigInteger.valueOf(durationMs),
                BigInteger.valueOf(positionMs),
                isCompleted,
                userId,
                now,
                userId,
                now);

        // 补上最近观看的集数，续播入口才有数据；已经有值的不覆盖
        LocalDateTime lastReadTime = row.getCreateTime() == null ? now : row.getCreateTime();
        lightDao.executeSql("""
                        update ani_user_opus
                        set last_episode_key = ?, last_media_id = ?, last_read_time = coalesce(last_read_time, ?)
                        where id = ? and last_episode_key is null
                        """,
                episodeKey, media.getId(), lastReadTime, userOpusId);
        return Outcome.BACKFILLED;
    }

    /**
     * 单条回填结果
     */
    private enum Outcome {
        /**
         * 已回填
         */
        BACKFILLED,
        /**
         * 数据本身不可用，永久跳过
         */
        SKIPPED,
        /**
         * 依赖媒体索引，等索引建好再回填
         */
        RETRY
    }

    /**
     * 回填统计
     */
    private record BackfillResult(int backfilled, int skipped, int retryable) {
    }
}
