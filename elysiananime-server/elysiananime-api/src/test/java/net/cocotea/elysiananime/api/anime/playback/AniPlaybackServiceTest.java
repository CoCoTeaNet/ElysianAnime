package net.cocotea.elysiananime.api.anime.playback;

import net.cocotea.elysiananime.api.anime.model.dto.AniPlaybackReportDTO;
import net.cocotea.elysiananime.api.anime.model.po.AniUserOpusProgress;
import net.cocotea.elysiananime.api.anime.model.vo.AniContinueVO;
import net.cocotea.elysiananime.api.anime.model.vo.AniPlaybackResultVO;
import net.cocotea.elysiananime.common.model.BusinessException;
import net.cocotea.elysiananime.test.SolonTestSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.noear.solon.Solon;
import org.sagacity.sqltoy.dao.LightDao;

import java.math.BigInteger;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * 播放进度集成测试（连本机 MySQL）
 * <p>
 * 覆盖：自动建档、幂等、乱序丢弃、越权拒绝、末集置「已看」、新集入库后回到继续观看。
 *
 * @author CoCoTea
 * @since 3.1.0
 */
public class AniPlaybackServiceTest extends SolonTestSupport {

    private static final BigInteger USER_ID = new BigInteger("990000000000000001");
    private static final BigInteger OTHER_USER_ID = new BigInteger("990000000000000002");
    private static final BigInteger OPUS_ID = new BigInteger("990000000000000003");
    private static final BigInteger MEDIA_EP1 = new BigInteger("990000000000000011");
    private static final BigInteger MEDIA_EP2 = new BigInteger("990000000000000012");
    private static final BigInteger MEDIA_SP1 = new BigInteger("990000000000000013");
    private static final BigInteger MEDIA_EP3 = new BigInteger("990000000000000014");

    /**
     * 单集时长（毫秒）
     */
    private static final long EPISODE_DURATION_MS = 600_000L;

    private AniPlaybackService playbackService;

    private LightDao lightDao;

    @BeforeEach
    public void setUp() {
        playbackService = Solon.context().getBean(AniPlaybackService.class);
        lightDao = daoOf(playbackService);
        try (Connection conn = connection()) {
            assumeTrue(conn != null, "数据库不可用，跳过集成测试");
            cleanUp(conn);
            prepare(conn);
        } catch (SQLException e) {
            assumeTrue(false, "数据库不可用，跳过集成测试：" + e.getMessage());
        }
    }

    /**
     * sqltoy 的 Dao 是按注入点代理的，取不到容器 Bean，从服务里反射拿
     */
    private LightDao daoOf(AniPlaybackService service) {
        try {
            Field field = AniPlaybackService.class.getDeclaredField("lightDao");
            field.setAccessible(true);
            return (LightDao) field.get(service);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("取不到 LightDao：" + e.getMessage(), e);
        }
    }

    /**
     * 未追番直接上报：自动建档，进度落库，最近观看的集数写回
     */
    @Test
    public void reportCreatesUserOpusAndProgress() throws BusinessException {
        AniPlaybackResultVO result = playbackService.report(USER_ID, report(MEDIA_EP1, 30_000L, 10_000L, "s1", 1));

        Assertions.assertEquals(PlaybackStateMachine.READ_STATUS_NOT_READ, result.getReadStatus());
        Assertions.assertEquals(0, result.getIsCompleted());

        AniUserOpusProgress row = findProgress(USER_ID, "1");
        Assertions.assertNotNull(row, "未追番也应落进度");
        Assertions.assertEquals(BigInteger.valueOf(30_000L), row.getPositionMs());

        Map<String, Object> userOpus = queryOne(
                "select read_status, last_episode_key, last_media_id from ani_user_opus where user_id = ? and opus_id = ?",
                USER_ID, OPUS_ID);
        Assertions.assertNotNull(userOpus, "首次上报应自动建档");
        Assertions.assertEquals("1", userOpus.get("last_episode_key"));
        Assertions.assertEquals(MEDIA_EP1, toBigInteger(userOpus.get("last_media_id")));
    }

    /**
     * 同一集反复上报：只留一行进度，累计观看时长按增量累加
     */
    @Test
    public void reportIsIdempotent() throws BusinessException {
        playbackService.report(USER_ID, report(MEDIA_EP1, 30_000L, 10_000L, "s1", 1));
        playbackService.report(USER_ID, report(MEDIA_EP1, 60_000L, 10_000L, "s1", 2));

        List<AniUserOpusProgress> rows = listProgress(USER_ID);
        Assertions.assertEquals(1, rows.size(), "同一集只应有一行进度");
        Assertions.assertEquals(BigInteger.valueOf(60_000L), rows.get(0).getPositionMs());
        Assertions.assertEquals(BigInteger.valueOf(20_000L), rows.get(0).getWatchedMs());
    }

    /**
     * 乱序上报（旧 seq 晚到）要丢弃，位置与状态都不能回退
     */
    @Test
    public void reportDiscardsOutOfOrder() throws BusinessException {
        playbackService.report(USER_ID, report(MEDIA_EP1, 60_000L, 10_000L, "s1", 5));
        AniPlaybackResultVO result = playbackService.report(USER_ID, report(MEDIA_EP1, 1_000L, 10_000L, "s1", 2));

        AniUserOpusProgress row = findProgress(USER_ID, "1");
        Assertions.assertEquals(BigInteger.valueOf(60_000L), row.getPositionMs(), "乱序上报不能把位置拉回去");
        Assertions.assertEquals(BigInteger.valueOf(10_000L), row.getWatchedMs(), "乱序上报不能重复累计");
        Assertions.assertEquals(60_000L, result.getPositionMs());
    }

    /**
     * 媒体归属校验：不属于该作品的媒体必须拒绝
     */
    @Test
    public void reportRejectsForeignMedia() {
        AniPlaybackReportDTO dto = report(MEDIA_EP1, 1_000L, 1_000L, "s1", 1)
                .setOpusId(new BigInteger("990000000000000999"));
        Assertions.assertThrows(BusinessException.class, () -> playbackService.report(USER_ID, dto));
    }

    /**
     * 播完正片最后一集：状态置「已看」，并退出「继续观看」
     */
    @Test
    public void finishedLastEpisodeMarksRead() throws BusinessException {
        playbackService.report(USER_ID, report(MEDIA_EP1, 30_000L, 10_000L, "s1", 1));
        playbackService.report(USER_ID, report(MEDIA_EP2, EPISODE_DURATION_MS, 10_000L, "s1", 2));

        Map<String, Object> userOpus = queryOne(
                "select read_status from ani_user_opus where user_id = ? and opus_id = ?", USER_ID, OPUS_ID);
        Assertions.assertEquals(PlaybackStateMachine.READ_STATUS_IS_READ, userOpus.get("read_status"));

        List<AniContinueVO> list = playbackService.continueList(USER_ID, 3);
        Assertions.assertTrue(list.stream().noneMatch(item -> OPUS_ID.equals(item.getOpusId())),
                "看完最新一集的作品不该继续出现在继续观看");
    }

    /**
     * 看完之后新集入库：自动回到「继续观看」
     */
    @Test
    public void newEpisodeBringsBackToContinue() throws BusinessException {
        playbackService.report(USER_ID, report(MEDIA_EP1, 30_000L, 10_000L, "s1", 1));
        playbackService.report(USER_ID, report(MEDIA_EP2, EPISODE_DURATION_MS, 10_000L, "s1", 2));

        // 新集入库，最新一集变成 3
        insertMedia(MEDIA_EP3, "3", 3.0D, "第 3 集", 3.0D);
        execute("update ani_opus set media_latest_key = '3', media_count = 3 where id = ?", OPUS_ID);

        List<AniContinueVO> list = playbackService.continueList(USER_ID, 3);
        Assertions.assertTrue(list.stream().anyMatch(item -> OPUS_ID.equals(item.getOpusId())),
                "新集入库后应回到继续观看");
    }

    /**
     * 看完正片再看 SP：SP 不应把作品改成「已看」，也不该重新挂回继续观看
     */
    @Test
    public void extraEpisodeDoesNotMarkRead() throws BusinessException {
        playbackService.report(USER_ID, report(MEDIA_EP1, 30_000L, 10_000L, "s1", 1));
        playbackService.report(USER_ID, report(MEDIA_EP2, EPISODE_DURATION_MS, 10_000L, "s1", 2));
        playbackService.report(USER_ID, report(MEDIA_SP1, 60_000L, 10_000L, "s1", 3));

        Map<String, Object> userOpus = queryOne(
                "select read_status, last_episode_key from ani_user_opus where user_id = ? and opus_id = ?",
                USER_ID, OPUS_ID);
        Assertions.assertEquals(PlaybackStateMachine.READ_STATUS_IS_READ, userOpus.get("read_status"));
        Assertions.assertEquals("SP1", userOpus.get("last_episode_key"));

        List<AniContinueVO> list = playbackService.continueList(USER_ID, 3);
        Assertions.assertTrue(list.stream().noneMatch(item -> OPUS_ID.equals(item.getOpusId())),
                "看完正片再看 SP 不该回到继续观看");
    }

    /**
     * 手动标记「已看完」：同步最新集、累计观看清零
     */
    @Test
    public void manualStatusSyncsLatestKey() throws BusinessException {
        playbackService.report(USER_ID, report(MEDIA_EP1, 30_000L, 10_000L, "s1", 1));

        Map<String, Object> userOpus = queryOne(
                "select id, user_id, opus_id from ani_user_opus where user_id = ? and opus_id = ?", USER_ID, OPUS_ID);
        Assertions.assertNotNull(userOpus);
        net.cocotea.elysiananime.api.anime.model.po.AniUserOpus entity =
                new net.cocotea.elysiananime.api.anime.model.po.AniUserOpus()
                        .setId(toBigInteger(userOpus.get("id")))
                        .setUserId(toBigInteger(userOpus.get("user_id")))
                        .setOpusId(toBigInteger(userOpus.get("opus_id")));
        Assertions.assertTrue(playbackService.applyManualStatus(entity, PlaybackStateMachine.READ_STATUS_IS_READ));

        Map<String, Object> after = queryOne(
                "select read_status, last_episode_key, last_media_id from ani_user_opus where id = ?",
                toBigInteger(userOpus.get("id")));
        Assertions.assertEquals(PlaybackStateMachine.READ_STATUS_IS_READ, after.get("read_status"));
        Assertions.assertEquals("2", after.get("last_episode_key"), "手动标完要同步最新一集");
        Assertions.assertEquals(MEDIA_EP2, toBigInteger(after.get("last_media_id")));

        Long watched = ((Number) queryOne(
                "select coalesce(sum(watched_ms), 0) as total from ani_user_opus_progress where user_id = ? and opus_id = ?",
                USER_ID, OPUS_ID).get("total")).longValue();
        Assertions.assertEquals(0L, watched, "手动改状态要清零累计观看时长");
    }

    private BigInteger toBigInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigInteger bigInteger) {
            return bigInteger;
        }
        return BigInteger.valueOf(((Number) value).longValue());
    }

    private AniPlaybackReportDTO report(BigInteger mediaId, long positionMs, long watchedDeltaMs,
                                         String sessionId, int seq) {
        return new AniPlaybackReportDTO()
                .setOpusId(OPUS_ID)
                .setMediaId(mediaId)
                .setEpisodeKey(episodeKeyOf(mediaId))
                .setPositionMs(positionMs)
                .setDurationMs(EPISODE_DURATION_MS)
                .setWatchedDeltaMs(watchedDeltaMs)
                .setEvent(PlaybackStateMachine.EVENT_TICK)
                .setSessionId(sessionId)
                .setSeq(seq);
    }

    private String episodeKeyOf(BigInteger mediaId) {
        if (MEDIA_EP1.equals(mediaId)) {
            return "1";
        }
        if (MEDIA_EP2.equals(mediaId)) {
            return "2";
        }
        if (MEDIA_EP3.equals(mediaId)) {
            return "3";
        }
        return "SP1";
    }

    private AniUserOpusProgress findProgress(BigInteger userId, String episodeKey) {
        Map<String, Object> params = new HashMap<>(3);
        params.put("userId", userId);
        params.put("opusId", OPUS_ID);
        params.put("episodeKey", episodeKey);
        return lightDao.findOne("ani_progress_findByEpisode", params, AniUserOpusProgress.class);
    }

    private List<AniUserOpusProgress> listProgress(BigInteger userId) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("userId", userId);
        params.put("opusId", OPUS_ID);
        return lightDao.find("ani_progress_listByOpus", params, AniUserOpusProgress.class);
    }

    private Map<String, Object> queryOne(String sql, Object... args) {
        try (Connection conn = connection()) {
            if (conn == null) {
                return null;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < args.length; i++) {
                    ps.setObject(i + 1, args[i]);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    Map<String, Object> row = new HashMap<>();
                    int count = rs.getMetaData().getColumnCount();
                    for (int i = 1; i <= count; i++) {
                        row.put(rs.getMetaData().getColumnLabel(i), rs.getObject(i));
                    }
                    return row;
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("查询失败：" + e.getMessage(), e);
        }
    }

    private void execute(String sql, Object... args) {
        try (Connection conn = connection()) {
            if (conn == null) {
                return;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < args.length; i++) {
                    ps.setObject(i + 1, args[i]);
                }
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("执行失败：" + e.getMessage(), e);
        }
    }

    private void prepare(Connection conn) throws SQLException {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        try (PreparedStatement ps = conn.prepareStatement("""
                insert into ani_opus (id, name_cn, name_original, episodes, create_time, update_time, is_deleted,
                                       media_count, media_latest_key)
                values (?, ?, ?, ?, ?, ?, 0, 3, '2')
                """)) {
            ps.setLong(1, OPUS_ID.longValue());
            ps.setString(2, "集成测试作品");
            ps.setString(3, "Integration Test Work");
            ps.setString(4, "2");
            ps.setTimestamp(5, now);
            ps.setTimestamp(6, now);
            ps.executeUpdate();
        }
        insertMedia(MEDIA_EP1, "1", 1.0D, "第 1 集", 1.0D);
        insertMedia(MEDIA_EP2, "2", 2.0D, "第 2 集", 2.0D);
        insertMedia(MEDIA_SP1, "SP1", null, "SP1", 10001.0D);
    }

    private void insertMedia(BigInteger mediaId, String episodeKey, Double episodeNo, String displayName, Double sortNo) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        try (Connection conn = connection()) {
            if (conn == null) {
                return;
            }
            try (PreparedStatement ps = conn.prepareStatement("""
                    insert into ani_media (id, opus_id, file_path, file_path_hash, relative_path, file_name, extension,
                                           episode_no, episode_key, display_name, file_size, file_mtime, sort_no,
                                           is_primary, last_seen_time, create_time, update_time, is_deleted)
                    values (?, ?, ?, ?, ?, ?, 'mp4', ?, ?, ?, 1024, ?, ?, 1, ?, ?, ?, 0)
                    """)) {
                ps.setLong(1, mediaId.longValue());
                ps.setLong(2, OPUS_ID.longValue());
                ps.setString(3, "/tmp/test/" + episodeKey + ".mp4");
                ps.setString(4, String.format("%32s", episodeKey).replace(' ', '0'));
                ps.setString(5, episodeKey + ".mp4");
                ps.setString(6, episodeKey + ".mp4");
                if (episodeNo == null) {
                    ps.setNull(7, java.sql.Types.DECIMAL);
                } else {
                    ps.setDouble(7, episodeNo);
                }
                ps.setString(8, episodeKey);
                ps.setString(9, displayName);
                ps.setTimestamp(10, now);
                if (sortNo == null) {
                    ps.setNull(11, java.sql.Types.DECIMAL);
                } else {
                    ps.setDouble(11, sortNo);
                }
                ps.setTimestamp(12, now);
                ps.setTimestamp(13, now);
                ps.setTimestamp(14, now);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("插入媒体失败：" + e.getMessage(), e);
        }
    }

    private void cleanUp(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "delete from ani_user_opus_progress where user_id in (?, ?)")) {
            ps.setLong(1, USER_ID.longValue());
            ps.setLong(2, OTHER_USER_ID.longValue());
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "delete from ani_user_opus where user_id in (?, ?)")) {
            ps.setLong(1, USER_ID.longValue());
            ps.setLong(2, OTHER_USER_ID.longValue());
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "delete from ani_media where opus_id = ?")) {
            ps.setLong(1, OPUS_ID.longValue());
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "delete from ani_opus where id = ?")) {
            ps.setLong(1, OPUS_ID.longValue());
            ps.executeUpdate();
        }
    }
}
