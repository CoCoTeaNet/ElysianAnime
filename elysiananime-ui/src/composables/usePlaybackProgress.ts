import {reportProgress} from "@/api/anime/ani-user-opus-api";
import {API_BASE} from "@/utils/axios-util";

/**
 * 播放进度上报：10 秒 tick + pause/seek/ended/close/切集 补报；
 * pagehide 用 sendBeacon 兜底（plan §5.2）。
 */

export interface PlaybackState {
    opusId: string;
    mediaId: string;
    episodeKey: string;
    /** 当前播放位置（秒） */
    currentTime: number;
    /** 总时长（秒），未知为 0 / NaN */
    duration: number;
    /** 是否暂停中 */
    paused: boolean;
}

export interface ReportResult {
    readStatus: number;
    isCompleted: number;
    latestEpisodeKey: string;
    nextEpisodeKey: string;
    nextMediaId: string;
    positionMs: number;
}

const TICK_INTERVAL_MS = 10_000;
/** 单次上报的观看增量上限，与服务端夹取一致 */
const WATCHED_DELTA_MAX_MS = 15_000;

export function createProgressReporter() {
    // 换端/换页面会变：每次打开弹窗新建一个 reporter 即得新会话
    const sessionId = crypto.randomUUID();
    let seq = 0;
    let timer: number | null = null;
    let lastPositionSec = 0;
    let onResult: ((result: ReportResult) => void) | null = null;

    const buildBody = (event: string, state: PlaybackState) => {
        const positionMs = Math.max(0, Math.floor(state.currentTime * 1000));
        const durationMs = isFinite(state.duration) && state.duration > 0
            ? Math.floor(state.duration * 1000) : 0;
        // 观看增量：暂停/回退按 0 计，上限与服务端夹取一致
        let watchedDeltaMs = 0;
        if (!state.paused && event !== 'seek') {
            watchedDeltaMs = Math.min(
                Math.max(0, Math.floor((state.currentTime - lastPositionSec) * 1000)),
                WATCHED_DELTA_MAX_MS);
        }
        lastPositionSec = state.currentTime;
        seq += 1;
        return {
            opusId: state.opusId,
            mediaId: state.mediaId,
            episodeKey: state.episodeKey,
            positionMs,
            durationMs,
            watchedDeltaMs,
            event,
            sessionId,
            seq
        };
    };

    const report = async (event: string, state: PlaybackState): Promise<ReportResult | null> => {
        try {
            const res = await reportProgress(buildBody(event, state));
            // request() 返回 {code, data, message} 包装层，这里只透传业务数据
            const result = res && res.code === 200 ? res.data : null;
            if (result && onResult) {
                onResult(result);
            }
            return result;
        } catch (e) {
            // 进度上报失败不阻塞播放
            return null;
        }
    };

    const startTimer = (getState: () => PlaybackState) => {
        stopTimer();
        timer = window.setInterval(() => {
            const state = getState();
            if (!state.paused && state.currentTime > 0) {
                report('tick', state);
            }
        }, TICK_INTERVAL_MS);
    };

    const stopTimer = () => {
        if (timer !== null) {
            window.clearInterval(timer);
            timer = null;
        }
    };

    const beacon = (state: PlaybackState) => {
        try {
            const body = JSON.stringify(buildBody('close', state));
            navigator.sendBeacon(`${API_BASE}/anime/userOpus/progress`,
                new Blob([body], {type: 'application/json'}));
        } catch (e) {
            // 页面卸载阶段尽力而为
        }
    };

    const setResultHandler = (handler: (result: ReportResult) => void) => {
        onResult = handler;
    };

    const reset = () => {
        lastPositionSec = 0;
    };

    return {report, startTimer, stopTimer, beacon, setResultHandler, reset};
}

/**
 * 续播判定：位置/时长 ≥ 0.95 视为看完，从头播且不提示（plan §7.3）
 */
export function shouldRestartFromStart(positionSec: number, durationSec: number): boolean {
    return durationSec > 0 && positionSec / durationSec >= 0.95;
}

export function formatDuration(totalSeconds: number): string {
    const s = Math.max(0, Math.floor(totalSeconds));
    const m = Math.floor(s / 60);
    const sec = s % 60;
    return `${m.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}`;
}
