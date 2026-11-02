package net.cocotea.elysiananime.api.anime.playback;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 播放状态机单元测试（不连库）
 * <p>
 * 规则见 doc/plan/playback-media-rework.md §5.2。
 *
 * @author CoCoTea
 */
public class PlaybackStateMachineTest {

    @Test
    public void clampWatchedDelta() {
        Assertions.assertEquals(0L, PlaybackStateMachine.clampWatchedDelta(-1L));
        Assertions.assertEquals(0L, PlaybackStateMachine.clampWatchedDelta(0L));
        Assertions.assertEquals(10_000L, PlaybackStateMachine.clampWatchedDelta(10_000L));
        Assertions.assertEquals(15_000L, PlaybackStateMachine.clampWatchedDelta(15_000L));
        // 超过上限要被夹住，防止前端一次上报几分钟
        Assertions.assertEquals(15_000L, PlaybackStateMachine.clampWatchedDelta(60_000L));
    }

    @Test
    public void isOutOfOrder() {
        // 会话不同：一律接受
        Assertions.assertFalse(PlaybackStateMachine.isOutOfOrder("s2", 1, "s1", 10));
        // 同会话且序号更小：丢弃
        Assertions.assertTrue(PlaybackStateMachine.isOutOfOrder("s1", 9, "s1", 10));
        // 同会话且序号相同/更大：接受
        Assertions.assertFalse(PlaybackStateMachine.isOutOfOrder("s1", 10, "s1", 10));
        Assertions.assertFalse(PlaybackStateMachine.isOutOfOrder("s1", 11, "s1", 10));
        // 没有会话信息时不判定
        Assertions.assertFalse(PlaybackStateMachine.isOutOfOrder(null, 1, "s1", 10));
        Assertions.assertFalse(PlaybackStateMachine.isOutOfOrder("s1", 1, null, 10));
    }

    @Test
    public void isFinished() {
        Assertions.assertTrue(PlaybackStateMachine.isFinished(PlaybackStateMachine.EVENT_ENDED, 10L, 1_000L));
        // 拖到 95% 视为看完
        Assertions.assertTrue(PlaybackStateMachine.isFinished(PlaybackStateMachine.EVENT_TICK, 950L, 1_000L));
        Assertions.assertFalse(PlaybackStateMachine.isFinished(PlaybackStateMachine.EVENT_TICK, 949L, 1_000L));
        // 不知道总时长时不能靠比例判定
        Assertions.assertFalse(PlaybackStateMachine.isFinished(PlaybackStateMachine.EVENT_TICK, 9_999L, 0L));
    }

    @Test
    public void resolveReadStatusToReading() {
        int status = PlaybackStateMachine.resolveReadStatus(
                PlaybackStateMachine.READ_STATUS_NOT_READ, true, false, true, true, true);
        Assertions.assertEquals(PlaybackStateMachine.READ_STATUS_READING, status);
    }

    @Test
    public void resolveReadStatusKeepsUnwatched() {
        // 累计观看不足仍是未看
        int status = PlaybackStateMachine.resolveReadStatus(
                PlaybackStateMachine.READ_STATUS_NOT_READ, false, false, true, true, true);
        Assertions.assertEquals(PlaybackStateMachine.READ_STATUS_NOT_READ, status);
    }

    @Test
    public void resolveReadStatusLastMainEpisode() {
        // 正片组最后一集看完 → 已看
        int status = PlaybackStateMachine.resolveReadStatus(
                PlaybackStateMachine.READ_STATUS_READING, true, true, false, true, true);
        Assertions.assertEquals(PlaybackStateMachine.READ_STATUS_IS_READ, status);
    }

    @Test
    public void resolveReadStatusWithNextEpisode() {
        // 同组内还有下一集 → 不置已看
        int status = PlaybackStateMachine.resolveReadStatus(
                PlaybackStateMachine.READ_STATUS_READING, true, true, true, true, true);
        Assertions.assertEquals(PlaybackStateMachine.READ_STATUS_READING, status);
    }

    @Test
    public void resolveReadStatusExtraGroupKeepsReading() {
        // 额外内容组（SP）看完，但整部作品有正片 → 不能置已看，
        // 否则 last_episode_key 停在 SP 上永远追不上最新正片集
        int status = PlaybackStateMachine.resolveReadStatus(
                PlaybackStateMachine.READ_STATUS_READING, true, true, false, false, true);
        Assertions.assertEquals(PlaybackStateMachine.READ_STATUS_READING, status);
    }

    @Test
    public void resolveReadStatusExtraGroupWithoutMain() {
        // 整部作品没有正片（只有 SP / 剧场版）→ 看完置已看
        int status = PlaybackStateMachine.resolveReadStatus(
                PlaybackStateMachine.READ_STATUS_READING, true, true, false, false, false);
        Assertions.assertEquals(PlaybackStateMachine.READ_STATUS_IS_READ, status);
    }

    @Test
    public void resolveReadStatusNeverRegress() {
        // 已看完不会被普通上报改回在看
        int status = PlaybackStateMachine.resolveReadStatus(
                PlaybackStateMachine.READ_STATUS_IS_READ, true, false, true, true, true);
        Assertions.assertEquals(PlaybackStateMachine.READ_STATUS_IS_READ, status);
    }

    @Test
    public void normalizeEpisodeKey() {
        Assertions.assertEquals("1", PlaybackStateMachine.normalizeEpisodeKey("01"));
        Assertions.assertEquals("1", PlaybackStateMachine.normalizeEpisodeKey("1"));
        Assertions.assertEquals("12.5", PlaybackStateMachine.normalizeEpisodeKey("12.5"));
        Assertions.assertEquals("SP1", PlaybackStateMachine.normalizeEpisodeKey("SP01"));
        Assertions.assertEquals("SP1", PlaybackStateMachine.normalizeEpisodeKey("SP1"));
        Assertions.assertEquals("SP1", PlaybackStateMachine.normalizeEpisodeKey("SP 1"));
        Assertions.assertEquals("5", PlaybackStateMachine.normalizeEpisodeKey("第5集"));
        Assertions.assertNull(PlaybackStateMachine.normalizeEpisodeKey(""));
        Assertions.assertNull(PlaybackStateMachine.normalizeEpisodeKey(null));
        Assertions.assertNull(PlaybackStateMachine.normalizeEpisodeKey("movie"));
    }
}
