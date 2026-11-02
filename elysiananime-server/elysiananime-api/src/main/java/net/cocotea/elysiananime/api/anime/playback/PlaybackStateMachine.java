package net.cocotea.elysiananime.api.anime.playback;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 播放状态机（纯计算，不碰数据库）。
 * <p>
 * 规则见 doc/plan/playback-media-rework.md §5.2。
 *
 * @author CoCoTea
 * @since 3.1.0
 */
public final class PlaybackStateMachine {

    /**
     * 上报事件：播放中定时上报
     */
    public static final String EVENT_TICK = "tick";
    /**
     * 上报事件：暂停
     */
    public static final String EVENT_PAUSE = "pause";
    /**
     * 上报事件：拖动
     */
    public static final String EVENT_SEEK = "seek";
    /**
     * 上报事件：播放结束
     */
    public static final String EVENT_ENDED = "ended";
    /**
     * 上报事件：离开页面
     */
    public static final String EVENT_CLOSE = "close";

    /**
     * 未看
     */
    public static final int READ_STATUS_NOT_READ = 0;
    /**
     * 已看
     */
    public static final int READ_STATUS_IS_READ = 1;
    /**
     * 在看
     */
    public static final int READ_STATUS_READING = 2;

    /**
     * 单次上报允许累计的观看时长上限：15 秒
     */
    public static final long MAX_WATCHED_DELTA_MS = 15_000L;

    /**
     * 判定「看完」的位置比例
     */
    private static final double COMPLETED_RATIO = 0.95D;

    private static final Pattern NUMERIC = Pattern.compile("^[0-9]{1,9}(?:\\.[0-9]{1,2})?$");
    private static final Pattern SP = Pattern.compile("(?i)^sp\\s*0*([0-9]{1,4})$");
    private static final Pattern DIGIT = Pattern.compile("([0-9]{1,9}(?:\\.[0-9]{1,2})?)");

    private PlaybackStateMachine() {
    }

    /**
     * 单次上报的观看增量夹到 0~15 秒
     */
    public static long clampWatchedDelta(long deltaMs) {
        if (deltaMs <= 0L) {
            return 0L;
        }
        return Math.min(deltaMs, MAX_WATCHED_DELTA_MS);
    }

    /**
     * 是否是乱序上报（应当丢弃）。
     * <p>
     * 会话不同一律接受（并把 seq 重置为新值）；只有同一会话内 seq 变小才丢弃。
     */
    public static boolean isOutOfOrder(String sessionId, int seq, String storedSessionId, Integer storedSeq) {
        if (sessionId == null || storedSessionId == null) {
            return false;
        }
        if (!sessionId.equals(storedSessionId)) {
            return false;
        }
        int stored = storedSeq == null ? 0 : storedSeq;
        return seq < stored;
    }

    /**
     * 本集是否看完：ended 事件，或播放位置达到总时长的 95%
     */
    public static boolean isFinished(String event, long positionMs, long durationMs) {
        if (EVENT_ENDED.equals(event)) {
            return true;
        }
        if (durationMs <= 0L) {
            return false;
        }
        return positionMs >= (long) (durationMs * COMPLETED_RATIO);
    }

    /**
     * 推导作品观看状态。
     *
     * @param current        当前观看状态
     * @param watchedEnough  累计观看是否达到自动「在看」阈值
     * @param finished       本集是否看完
     * @param hasNextInGroup 同组内是否还有下一集
     * @param groupIsMain    本集是否属于正片组
     * @param workHasMain    整部作品是否存在正片
     */
    public static int resolveReadStatus(int current, boolean watchedEnough, boolean finished,
                                        boolean hasNextInGroup, boolean groupIsMain, boolean workHasMain) {
        int status = current;
        // 累计观看够了但还没开始标记的，置「在看」
        if (watchedEnough && status == READ_STATUS_NOT_READ) {
            status = READ_STATUS_READING;
        }
        // 本集看完且同组内没有后继：正片组直接置「已看」；
        // 额外内容组只有整部作品没有正片时才置「已看」，
        // 否则 last_episode_key 停在一个永远追不上的 SP 上，作品会反复回到「继续观看」
        if (finished && !hasNextInGroup && (groupIsMain || !workHasMain)) {
            status = READ_STATUS_IS_READ;
        }
        return status;
    }

    /**
     * 把旧字段 {@code reading_num} 归一成 episode_key：{@code 01 → 1}、{@code SP01 → SP1}。
     *
     * @return 归一后的集数键，抽不出数字时返回 null
     */
    public static String normalizeEpisodeKey(String readingNum) {
        if (readingNum == null) {
            return null;
        }
        String text = readingNum.trim();
        if (text.isEmpty()) {
            return null;
        }
        if (NUMERIC.matcher(text).matches()) {
            return stripZeros(new BigDecimal(text));
        }
        Matcher sp = SP.matcher(text);
        if (sp.matches()) {
            return "SP" + sp.group(1);
        }
        Matcher digit = DIGIT.matcher(text);
        if (digit.find()) {
            return stripZeros(new BigDecimal(digit.group(1)));
        }
        return null;
    }

    private static String stripZeros(BigDecimal number) {
        BigDecimal stripped = number.stripTrailingZeros();
        return stripped.scale() <= 0 ? stripped.toBigInteger().toString() : stripped.toPlainString();
    }
}
