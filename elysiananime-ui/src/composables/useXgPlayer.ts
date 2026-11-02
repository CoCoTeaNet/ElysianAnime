import Player, {Events} from "xgplayer";
import 'xgplayer/dist/index.min.css';

/**
 * xgplayer 实例封装：创建 / 销毁 / 切源。
 * 切集不销毁实例，只切 src（plan §5.3）。
 */
export function useXgPlayer() {
    let player: Player | null = null;

    const create = (el: HTMLElement, handlers: {
        onMetadata?: () => void;
        onEnded?: () => void;
        onError?: () => void;
        onPause?: () => void;
        onPlay?: () => void;
        onSeeked?: () => void;
    }) => {
        destroy();
        const p = new Player({
            el: el,
            width: '100%',
            height: '100%',
            autoplay: true,
            video: {type: 'auto'},
            plugins: [],
            playbackRate: [0.5, 0.75, 1, 1.5, 2],
            pip: true,
            lastPlayTimeHideDelay: 5
        });
        if (handlers.onMetadata) {
            p.on(Events.LOADED_METADATA, () => handlers.onMetadata!());
        }
        if (handlers.onEnded) {
            p.on(Events.ENDED, () => handlers.onEnded!());
        }
        if (handlers.onError) {
            p.on(Events.ERROR, () => handlers.onError!());
        }
        if (handlers.onPause) {
            p.on(Events.PAUSE, () => handlers.onPause!());
        }
        if (handlers.onPlay) {
            p.on(Events.PLAY, () => handlers.onPlay!());
        }
        if (handlers.onSeeked) {
            p.on(Events.SEEKED, () => handlers.onSeeked!());
        }
        player = p;
        return p;
    };

    const destroy = () => {
        if (player) {
            try {
                player.pause();
                player.destroy();
            } catch (e) {
                // 实例可能已销毁
            }
            player = null;
        }
    };

    const switchSrc = (url: string) => {
        if (player) {
            player.src = url;
        }
    };

    const get = () => player;

    return {create, destroy, switchSrc, get};
}
