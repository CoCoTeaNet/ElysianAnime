package net.cocotea.elysiananime.api.anime.service.impl;

import net.cocotea.elysiananime.api.anime.model.dto.AniUserOpusUpdateDTO;
import net.cocotea.elysiananime.api.anime.model.po.AniUserOpus;
import net.cocotea.elysiananime.api.anime.playback.AniPlaybackService;
import net.cocotea.elysiananime.common.model.BusinessException;
import net.cocotea.elysiananime.test.SolonTestSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.noear.solon.Solon;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * 追番记录的越权校验。
 * <p>
 * 只能操作自己的追番记录：别人的记录一律拒绝，未登录同样拒绝。
 *
 * @author CoCoTea
 * @since 3.1.0
 */
public class AniUserOpusServiceImplTest extends SolonTestSupport {

    private static final BigInteger USER_ID = new BigInteger("990000000000000101");
    private static final BigInteger OTHER_USER_ID = new BigInteger("990000000000000102");
    private static final BigInteger OPUS_ID = new BigInteger("990000000000000103");
    private static final BigInteger USER_OPUS_ID = new BigInteger("990000000000000104");

    private AniUserOpusServiceImpl userOpusService;

    @BeforeEach
    public void setUp() {
        // 带事务的服务会被 Solon 代理，代理对象上取不到注入的字段，这里手工装配原始实例
        AniPlaybackService playbackService = Solon.context().getBean(AniPlaybackService.class);
        userOpusService = new AniUserOpusServiceImpl();
        setField(userOpusService, "lightDao", lightDaoOf(playbackService));
        setField(userOpusService, "aniPlaybackService", playbackService);
        try (Connection conn = connection()) {
            assumeTrue(conn != null, "数据库不可用，跳过集成测试");
            cleanUp(conn);
            prepare(conn);
        } catch (SQLException e) {
            assumeTrue(false, "数据库不可用，跳过集成测试：" + e.getMessage());
        }
    }

    /**
     * 自己的记录能取到
     */
    @Test
    public void loadsOwnUserOpus() throws BusinessException {
        AniUserOpus userOpus = userOpusService.loadOwnedUserOpus(USER_OPUS_ID, USER_ID);
        Assertions.assertNotNull(userOpus);
        Assertions.assertEquals(USER_OPUS_ID, userOpus.getId());
        Assertions.assertEquals(USER_ID, userOpus.getUserId());
    }

    /**
     * 别人的记录一律拒绝
     */
    @Test
    public void rejectsForeignUserOpus() {
        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> userOpusService.loadOwnedUserOpus(USER_OPUS_ID, OTHER_USER_ID));
        Assertions.assertEquals("无权操作该记录", exception.getErrorMsg());
    }

    /**
     * 记录不存在同样拒绝，不能当成「操作成功」
     */
    @Test
    public void rejectsMissingUserOpus() {
        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> userOpusService.loadOwnedUserOpus(new BigInteger("990000000000000199"), USER_ID));
        Assertions.assertEquals("无权操作该记录", exception.getErrorMsg());
    }

    /**
     * 关联id为空直接拒绝
     */
    @Test
    public void rejectsNullUserOpusId() {
        BusinessException exception = Assertions.assertThrows(BusinessException.class,
                () -> userOpusService.loadOwnedUserOpus(null, USER_ID));
        Assertions.assertEquals("关联id为空", exception.getErrorMsg());
    }

    /**
     * 未登录时改状态要拒绝，登录态只认会话，报文里的 id / opusId 顶多影响被拒绝
     */
    @Test
    public void rejectsAnonymousUpdate() throws SQLException {
        AniUserOpusUpdateDTO dto = new AniUserOpusUpdateDTO()
                .setId(USER_OPUS_ID)
                .setOpusId(OPUS_ID)
                .setReadStatus(1);
        Assertions.assertThrows(Exception.class, () -> userOpusService.update(dto));

        try (Connection conn = connection();
             PreparedStatement ps = conn.prepareStatement(
                     "select read_status from ani_user_opus where id = ?")) {
            ps.setLong(1, USER_OPUS_ID.longValue());
            try (var rs = ps.executeQuery()) {
                Assertions.assertTrue(rs.next());
                Assertions.assertEquals(0, rs.getInt("read_status"), "未登录不能改掉观看状态");
            }
        }
    }

    private void prepare(Connection conn) throws SQLException {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        try (PreparedStatement ps = conn.prepareStatement("""
                insert into ani_opus (id, name_cn, name_original, episodes, create_time, update_time, is_deleted,
                                       media_count, media_latest_key)
                values (?, ?, ?, ?, ?, ?, 0, 0, null)
                """)) {
            ps.setLong(1, OPUS_ID.longValue());
            ps.setString(2, "越权测试作品");
            ps.setString(3, "Ownership Test Work");
            ps.setString(4, "0");
            ps.setTimestamp(5, now);
            ps.setTimestamp(6, now);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement("""
                insert into ani_user_opus (id, user_id, opus_id, read_status, is_share, reading_num, reading_time,
                                            total_time, create_time)
                values (?, ?, ?, 0, 0, '0', 0, null, ?)
                """)) {
            ps.setLong(1, USER_OPUS_ID.longValue());
            ps.setLong(2, USER_ID.longValue());
            ps.setLong(3, OPUS_ID.longValue());
            ps.setTimestamp(4, now);
            ps.executeUpdate();
        }
    }

    private void cleanUp(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "delete from ani_user_opus where id = ?")) {
            ps.setLong(1, USER_OPUS_ID.longValue());
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "delete from ani_opus where id = ?")) {
            ps.setLong(1, OPUS_ID.longValue());
            ps.executeUpdate();
        }
    }
}
