package net.cocotea.elysiananime.api.anime.playback;

import cn.hutool.core.util.StrUtil;
import net.cocotea.elysiananime.api.anime.model.dto.AniPlaybackReportDTO;
import net.cocotea.elysiananime.api.anime.model.po.AniMedia;
import net.cocotea.elysiananime.api.anime.model.po.AniOpus;
import net.cocotea.elysiananime.api.anime.model.po.AniUserOpus;
import net.cocotea.elysiananime.api.anime.model.po.AniUserOpusProgress;
import net.cocotea.elysiananime.api.anime.model.vo.AniContinueVO;
import net.cocotea.elysiananime.api.anime.model.vo.AniEpisodeProgressVO;
import net.cocotea.elysiananime.api.anime.model.vo.AniPlaybackResultVO;
import net.cocotea.elysiananime.common.enums.IsEnum;
import net.cocotea.elysiananime.common.model.BusinessException;
import net.cocotea.elysiananime.properties.DefaultProp;
import net.cocotea.elysiananime.util.LoginUtils;
import net.cocotea.elysiananime.util.SnowflakeIds;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.solon.annotation.Db;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 播放进度服务：上报、按集进度、状态机、手动改状态、继续观看。
 * <p>
 * 规则见 doc/plan/playback-media-rework.md §5.2 与 §6。
 *
 * @author CoCoTea
 * @since 3.1.0
 */
@Component
public class AniPlaybackService {

    private static final Logger log = LoggerFactory.getLogger(AniPlaybackService.class);

    /**
     * 进度表没有自增 id，手写 SQL 必须自己生成
     */
    private static final String UPSERT_PROGRESS = """
            insert into ani_user_opus_progress
                (id, user_id, opus_id, episode_key, position_ms, duration_ms, watched_ms, is_completed,
                 session_id, seq, create_by, create_time, update_by, update_time, is_deleted)
            values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
            on duplicate key update
                position_ms = values(position_ms),
                duration_ms = case when values(duration_ms) is null then duration_ms
                                   else greatest(coalesce(duration_ms, 0), values(duration_ms)) end,
                watched_ms = watched_ms + values(watched_ms),
                is_completed = greatest(is_completed, values(is_completed)),
                session_id = coalesce(values(session_id), session_id),
                seq = case when values(session_id) is null then seq else values(seq) end,
                update_by = values(update_by),
                update_time = values(update_time),
                is_deleted = 0
            """;

    @Db
    private LightDao lightDao;

    @Inject
    private DefaultProp defaultProp;

    /**
     * 上报播放进度
     *
     * @param dto 上报参数
     * @return 状态机结果
     */
    public AniPlaybackResultVO report(AniPlaybackReportDTO dto) throws BusinessException {
        return report(LoginUtils.loginIdEx(), dto);
    }

    /**
     * 上报播放进度（指定用户）
     *
     * @param userId 用户ID，只取登录态，不接受报文里的值
     * @param dto    上报参数
     * @return 状态机结果
     */
    public AniPlaybackResultVO report(BigInteger userId, AniPlaybackReportDTO dto) throws BusinessException {
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        if (dto == null || dto.getOpusId() == null || dto.getMediaId() == null) {
            throw new BusinessException("参数不完整");
        }
        AniMedia media = lightDao.loadById(AniMedia.class, dto.getMediaId());
        if (media == null || (media.getIsDeleted() != null && media.getIsDeleted() == 1)) {
            throw new BusinessException("资源不存在");
        }
        if (!dto.getOpusId().equals(media.getOpusId())) {
            throw new BusinessException("资源与作品不匹配");
        }
        String episodeKey = StrUtil.isNotBlank(dto.getEpisodeKey()) ? dto.getEpisodeKey().trim() : media.getEpisodeKey();
        if (!StrUtil.equals(episodeKey, media.getEpisodeKey())) {
            throw new BusinessException("集数与资源不匹配");
        }

        String event = normalizeEvent(dto.getEvent());
        long positionMs = dto.getPositionMs() == null ? 0L : Math.max(0L, dto.getPositionMs());
        long durationMs = dto.getDurationMs() == null ? 0L : Math.max(0L, dto.getDurationMs());
        long watchedDelta = PlaybackStateMachine.clampWatchedDelta(
                dto.getWatchedDeltaMs() == null ? 0L : dto.getWatchedDeltaMs());
        int seq = dto.getSeq() == null ? 0 : dto.getSeq();
        String sessionId = StrUtil.trimToNull(dto.getSessionId());
        LocalDateTime now = LocalDateTime.now();

        // 首次上报自动建档（修未追番无法保存进度）
        AniUserOpus userOpus = ensureUserOpus(userId, dto.getOpusId(), now);
        AniOpus opus = lightDao.loadById(AniOpus.class, dto.getOpusId());

        AniUserOpusProgress exist = findProgress(userId, dto.getOpusId(), episodeKey);
        if (exist != null && PlaybackStateMachine.isOutOfOrder(sessionId, seq, exist.getSessionId(), exist.getSeq())) {
            log.debug("丢弃乱序上报：opusId={}, episodeKey={}, seq={}, storedSeq={}",
                    dto.getOpusId(), episodeKey, seq, exist.getSeq());
            return buildResult(opus, exist, null, currentReadStatus(userOpus));
        }

        long storedDuration = exist == null || exist.getDurationMs() == null ? 0L : exist.getDurationMs().longValue();
        // 总时长只认 >= 1000 毫秒的值，且只增不减
        long effectiveDuration = durationMs >= 1000L ? Math.max(storedDuration, durationMs) : storedDuration;
        boolean finished = PlaybackStateMachine.isFinished(event, positionMs, effectiveDuration);
        boolean alreadyCompleted = exist != null && exist.getIsCompleted() != null && exist.getIsCompleted() == 1;
        int isCompleted = alreadyCompleted || finished ? 1 : 0;
        long watchedMs = (exist == null ? 0L : nullSafe(exist.getWatchedMs())) + watchedDelta;

        upsertProgress(userId, dto.getOpusId(), episodeKey, positionMs,
                durationMs >= 1000L ? durationMs : null, watchedDelta, isCompleted, sessionId, seq, now);

        // 同组内取下一集：正片组与额外内容组各走各的顺序
        AniMedia next = null;
        if (finished) {
            next = findNextInGroup(dto.getOpusId(), media);
        }
        boolean groupIsMain = media.getEpisodeNo() != null;
        boolean workHasMain = groupIsMain || existsMainEpisode(dto.getOpusId());
        boolean watchedEnough = watchedMs >= autoReadingTimeMs();
        int currentStatus = currentReadStatus(userOpus);
        int newStatus = PlaybackStateMachine.resolveReadStatus(currentStatus, watchedEnough, finished,
                next != null, groupIsMain, workHasMain);
        applyReadStatus(userOpus.getId(), currentStatus, newStatus);

        // 最近观看的集数与旧列一起更新：位置大于零或事件为 ended 才算「在看这一集」
        if (positionMs > 0L || PlaybackStateMachine.EVENT_ENDED.equals(event)) {
            updateLastWatched(userOpus.getId(), episodeKey, media.getId(), positionMs, effectiveDuration, now);
        }

        return new AniPlaybackResultVO()
                .setReadStatus(newStatus)
                .setIsCompleted(isCompleted)
                .setLatestEpisodeKey(opus == null ? null : opus.getMediaLatestKey())
                .setNextEpisodeKey(next == null ? null : next.getEpisodeKey())
                .setNextMediaId(next == null ? null : next.getId())
                .setPositionMs(positionMs);
    }

    /**
     * 继续观看列表
     *
     * @param userId 用户ID
     * @param limit  条数
     */
    public List<AniContinueVO> continueList(BigInteger userId, int limit) {
        if (userId == null) {
            return List.of();
        }
        Map<String, Object> params = new HashMap<>(2);
        params.put("userId", userId);
        params.put("limit", limit);
        return lightDao.find("ani_progress_continueList", params, AniContinueVO.class);
    }

    /**
     * 作品的按集进度：key = episodeKey
     */
    public Map<String, AniEpisodeProgressVO> progressMap(BigInteger userId, BigInteger opusId) {
        Map<String, AniEpisodeProgressVO> result = new HashMap<>();
        if (userId == null || opusId == null) {
            return result;
        }
        Map<String, Object> params = new HashMap<>(2);
        params.put("userId", userId);
        params.put("opusId", opusId);
        List<AniUserOpusProgress> rows = lightDao.find("ani_progress_listByOpus", params, AniUserOpusProgress.class);
        for (AniUserOpusProgress row : rows) {
            result.put(row.getEpisodeKey(), new AniEpisodeProgressVO()
                    .setPositionMs(row.getPositionMs() == null ? 0L : row.getPositionMs().longValue())
                    .setDurationMs(row.getDurationMs() == null ? 0L : row.getDurationMs().longValue())
                    .setIsCompleted(row.getIsCompleted() == null ? 0 : row.getIsCompleted()));
        }
        return result;
    }

    /**
     * 手动改观看状态。
     * <p>
     * 改成「已看完」时必须同步 last_episode_key / last_media_id，
     * 否则 caughtUp 不成立，作品会立刻弹回「继续观看」。
     */
    public boolean applyManualStatus(AniUserOpus userOpus, int readStatus) {
        if (userOpus == null || userOpus.getId() == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        // 只清累计观看时长，各集的 ✓ 保留
        lightDao.executeSql("""
                        update ani_user_opus_progress
                        set watched_ms = 0, update_time = ?
                        where user_id = ? and opus_id = ? and is_deleted = 0
                        """,
                now, userOpus.getUserId(), userOpus.getOpusId());

        if (readStatus == PlaybackStateMachine.READ_STATUS_IS_READ) {
            AniOpus opus = lightDao.loadById(AniOpus.class, userOpus.getOpusId());
            String latestKey = opus == null ? null : opus.getMediaLatestKey();
            BigInteger mediaId = null;
            if (StrUtil.isNotBlank(latestKey)) {
                Map<String, Object> params = new HashMap<>(2);
                params.put("opusId", userOpus.getOpusId());
                params.put("episodeKey", latestKey);
                AniMedia media = lightDao.findOne("ani_media_findPrimaryByEpisodeKey", params, AniMedia.class);
                mediaId = media == null ? null : media.getId();
            }
            lightDao.executeSql("""
                            update ani_user_opus
                            set read_status = ?, last_episode_key = ?, last_media_id = ?, last_read_time = ?
                            where id = ?
                            """,
                    readStatus, latestKey, mediaId, now, userOpus.getId());
        } else {
            lightDao.executeSql("update ani_user_opus set read_status = ? where id = ?", readStatus, userOpus.getId());
        }
        return true;
    }

    /**
     * 追番记录不存在时自动建档
     */
    public AniUserOpus ensureUserOpus(BigInteger userId, BigInteger opusId, LocalDateTime now) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("opusId", opusId);
        params.put("userId", userId);
        AniUserOpus exist = lightDao.findOne("ani_user_opus_findList", params, AniUserOpus.class);
        if (exist != null) {
            return exist;
        }
        AniUserOpus userOpus = new AniUserOpus()
                .setUserId(userId)
                .setOpusId(opusId)
                .setReadStatus(PlaybackStateMachine.READ_STATUS_NOT_READ)
                .setIsShare(IsEnum.N.getCode())
                .setCreateTime(now);
        try {
            lightDao.save(userOpus);
        } catch (Exception e) {
            // 并发建档：唯一键挡住第二条，重新读一次即可
            AniUserOpus again = lightDao.findOne("ani_user_opus_findList", params, AniUserOpus.class);
            if (again != null) {
                return again;
            }
            throw e;
        }
        return userOpus;
    }

    private void upsertProgress(BigInteger userId, BigInteger opusId, String episodeKey, long positionMs,
                                Long durationMs, long watchedDelta, int isCompleted, String sessionId, int seq,
                                LocalDateTime now) {
        lightDao.executeSql(UPSERT_PROGRESS,
                SnowflakeIds.nextId(),
                userId,
                opusId,
                episodeKey,
                BigInteger.valueOf(positionMs),
                durationMs == null ? null : BigInteger.valueOf(durationMs),
                BigInteger.valueOf(watchedDelta),
                isCompleted,
                sessionId,
                seq,
                userId,
                now,
                userId,
                now);
    }

    private AniUserOpusProgress findProgress(BigInteger userId, BigInteger opusId, String episodeKey) {
        Map<String, Object> params = new HashMap<>(3);
        params.put("userId", userId);
        params.put("opusId", opusId);
        params.put("episodeKey", episodeKey);
        return lightDao.findOne("ani_progress_findByEpisode", params, AniUserOpusProgress.class);
    }

    /**
     * 同组内的下一集：正片组按集数往后找，额外内容组按 SP 序号往后找
     */
    private AniMedia findNextInGroup(BigInteger opusId, AniMedia media) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("opusId", opusId);
        params.put("sortNo", media.getSortNo());
        if (media.getEpisodeNo() != null) {
            return lightDao.findOne("ani_media_findNextMain", params, AniMedia.class);
        }
        return lightDao.findOne("ani_media_findNextExtra", params, AniMedia.class);
    }

    private boolean existsMainEpisode(BigInteger opusId) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("opusId", opusId);
        return lightDao.findOne("ani_media_existsMain", params, AniMedia.class) != null;
    }

    /**
     * 状态更新走条件 SQL，避免并发下状态回退
     */
    private void applyReadStatus(BigInteger userOpusId, int currentStatus, int newStatus) {
        if (newStatus == currentStatus) {
            return;
        }
        if (newStatus == PlaybackStateMachine.READ_STATUS_READING) {
            lightDao.executeSql("update ani_user_opus set read_status = ? where id = ? and read_status = 0",
                    newStatus, userOpusId);
        } else if (newStatus == PlaybackStateMachine.READ_STATUS_IS_READ) {
            lightDao.executeSql("update ani_user_opus set read_status = ? where id = ? and read_status <> 1",
                    newStatus, userOpusId);
        }
    }

    /**
     * 更新最近观看的集数；旧列 reading_num / reading_time / total_time 自 3.1 起停写
     */
    private void updateLastWatched(BigInteger userOpusId, String episodeKey, BigInteger mediaId, long positionMs,
                                   long durationMs, LocalDateTime now) {
        lightDao.executeSql("""
                        update ani_user_opus
                        set last_episode_key = ?, last_media_id = ?, last_read_time = ?
                        where id = ?
                        """,
                episodeKey,
                mediaId,
                now,
                userOpusId);
    }

    private AniPlaybackResultVO buildResult(AniOpus opus, AniUserOpusProgress row, AniMedia next, int readStatus) {
        return new AniPlaybackResultVO()
                .setReadStatus(readStatus)
                .setIsCompleted(row == null || row.getIsCompleted() == null ? 0 : row.getIsCompleted())
                .setLatestEpisodeKey(opus == null ? null : opus.getMediaLatestKey())
                .setNextEpisodeKey(next == null ? null : next.getEpisodeKey())
                .setNextMediaId(next == null ? null : next.getId())
                .setPositionMs(row == null || row.getPositionMs() == null ? 0L : row.getPositionMs().longValue());
    }

    private int currentReadStatus(AniUserOpus userOpus) {
        if (userOpus == null || userOpus.getReadStatus() == null) {
            return PlaybackStateMachine.READ_STATUS_NOT_READ;
        }
        return userOpus.getReadStatus();
    }

    private long autoReadingTimeMs() {
        Long seconds = defaultProp.getAutoReadingTime();
        if (seconds == null || seconds <= 0L) {
            return Long.MAX_VALUE;
        }
        return seconds * 1000L;
    }

    private String normalizeEvent(String event) {
        if (StrUtil.isBlank(event)) {
            return PlaybackStateMachine.EVENT_TICK;
        }
        String text = event.trim().toLowerCase(Locale.ROOT);
        return switch (text) {
            case PlaybackStateMachine.EVENT_PAUSE, PlaybackStateMachine.EVENT_SEEK,
                 PlaybackStateMachine.EVENT_ENDED, PlaybackStateMachine.EVENT_CLOSE -> text;
            default -> PlaybackStateMachine.EVENT_TICK;
        };
    }

    private static long nullSafe(BigInteger value) {
        return value == null ? 0L : value.longValue();
    }
}
