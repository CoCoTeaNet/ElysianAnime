<template>
  <el-dialog
      :model-value="visible"
      :show-close="false"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      width="min(1120px, 92vw, calc((100dvh - 170px) * 16 / 9))"
      align-center
      class="playback-dialog"
      append-to-body
      @update:model-value="onVisibleChange"
  >
    <div class="playback-dialog__wrap" @mousemove="showChrome" @mouseleave="scheduleHideChrome">
      <!-- 顶部栏：随控制条显隐 -->
      <div class="playback-dialog__header" :class="{'is-hidden': chromeHidden}">
        <span class="playback-dialog__title">{{ opusName }} {{ currentTitle }}</span>
        <el-icon class="playback-dialog__close" :size="20" @click="requestClose">
          <Close/>
        </el-icon>
      </div>

      <!-- 播放器 / 错误态 -->
      <div class="playback-dialog__stage">
        <video-stage
            v-if="!error"
            ref="stageRef"
            @metadata="onMetadata"
            @ended="onEnded"
            @error="onStreamError"
            @pause="onPause"
            @play="chromeVisible = true"
            @seeked="onSeeked"
        />
        <player-error-state
            v-else
            :error="error"
            :can-rescan="canRescan"
            @retry="loadStream"
            @rescan="$emit('rescan')"
        />
        <!-- 自动下一集倒计时 -->
        <div v-if="countdown > 0" class="playback-dialog__next">
          <span>{{ countdown }} 秒后播放下一集</span>
          <el-button size="small" type="primary" @click="goNext">立即播放</el-button>
          <el-button size="small" @click="cancelCountdown">取消</el-button>
        </div>
      </div>

      <!-- 选集条 -->
      <div class="playback-dialog__strip" :class="{'is-hidden': chromeHidden}">
        <episode-strip
            :media-list="mediaList"
            :current-media-id="currentMediaId"
            :progress-map="progressMap"
            :latest-episode-key="latestEpisodeKey"
            @select="onSelectEpisode"
        />
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import {computed, onBeforeUnmount, ref, watch} from "vue";
import {Close} from "@element-plus/icons-vue";
import {ElMessage} from "element-plus";
import {router} from "@/router";
import {API_BASE} from "@/utils/axios-util";
import VideoStage from "@/components/player/VideoStage.vue";
import EpisodeStrip from "@/components/player/EpisodeStrip.vue";
import PlayerErrorState from "@/components/player/PlayerErrorState.vue";
import {
  createProgressReporter,
  formatDuration,
  PlaybackState,
  shouldRestartFromStart
} from "@/composables/usePlaybackProgress";

const props = defineProps<{
  visible: boolean;
  opusId: string;
  opusName: string;
  mediaList: any[];
  currentMediaId: string;
  progressMap: Record<string, { positionMs: number; durationMs: number; isCompleted: number }>;
  latestEpisodeKey: string;
  /** 初始续播位置（秒），来自 ?t= 或该集已有进度 */
  resumeSec: number;
  canRescan: boolean;
}>();

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'switch', media: any): void;
  (e: 'rescan'): void;
  (e: 'progress', result: any): void;
}>();

const stageRef = ref<InstanceType<typeof VideoStage>>();
const error = ref<string>('');
const chromeVisible = ref<boolean>(true);
const countdown = ref<number>(0);

const reporter = createProgressReporter();
let hideChromeTimer: number | null = null;
let countdownTimer: number | null = null;
let pendingResumeSec = 0;
let pagehideInstalled = false;
let switching = false;
let loadToken = 0;
/** el-dialog 关闭时还会额外触发一次 update:model-value，防止 close 被上报两次 */
let closing = false;

const currentMedia = computed(() =>
    props.mediaList.find(item => item.mediaId === props.currentMediaId));

const currentTitle = computed(() => {
  const media = currentMedia.value;
  if (!media) {
    return '';
  }
  return media.displayName || media.episodeKey;
});

const streamUrl = (mediaId: string) => `${API_BASE}/anime/media/${mediaId}/stream`;

const getState = (): PlaybackState => {
  const state = stageRef.value?.getState();
  return {
    opusId: props.opusId,
    mediaId: props.currentMediaId,
    episodeKey: currentMedia.value?.episodeKey || '',
    currentTime: state?.currentTime || 0,
    duration: state?.duration || 0,
    paused: state ? state.paused : true
  };
};

const onVisibleChange = (value: boolean) => {
  if (!value) {
    requestClose();
  }
  emit('update:visible', value);
};

const showChrome = () => {
  chromeVisible.value = true;
  scheduleHideChrome();
};

const scheduleHideChrome = () => {
  if (hideChromeTimer !== null) {
    window.clearTimeout(hideChromeTimer);
  }
  hideChromeTimer = window.setTimeout(() => {
    chromeVisible.value = false;
  }, 3000);
};

const resumeSecFor = (mediaId: string): number => {
  const media = props.mediaList.find(item => item.mediaId === mediaId);
  const progress = media ? props.progressMap[media.episodeKey] : null;
  if (!progress || progress.isCompleted === 1) {
    return 0;
  }
  return Math.floor((progress.positionMs || 0) / 1000);
};

/** 弹窗首次渲染 + HEAD 探测期间 stage 可能还没挂载，等它出现而不是丢弃 open */
const waitForStage = async (): Promise<InstanceType<typeof VideoStage> | undefined> => {
  if (stageRef.value) {
    return stageRef.value;
  }
  await new Promise<void>(resolve => {
    const stop = watch(stageRef, value => {
      if (value) {
        stop();
        resolve();
      }
    }, {flush: 'post'});
    window.setTimeout(() => {
      stop();
      resolve();
    }, 3000);
  });
  return stageRef.value;
};

const loadStream = async () => {
  const mediaId = props.currentMediaId;
  if (!mediaId) {
    return;
  }
  const token = ++loadToken;
  error.value = '';
  // 流接口自己写状态码，先探一次才能区分登录失效与编码问题
  try {
    const resp = await fetch(streamUrl(mediaId), {method: 'HEAD'});
    if (token !== loadToken) {
      return;
    }
    if (resp.status === 401) {
      error.value = '登录已失效，请重新登录';
      return;
    }
    if (resp.status === 404) {
      error.value = '文件不存在，可能已被移动或删除';
      return;
    }
  } catch (e) {
    if (token !== loadToken) {
      return;
    }
    error.value = '无法连接服务器';
    return;
  }
  const stage = await waitForStage();
  if (token !== loadToken) {
    return;
  }
  stage?.open(streamUrl(mediaId));
};

const onMetadata = () => {
  const state = getState();
  // 恢复进度必须等 metadata 就绪（不能固定延时），≥0.95 视为看完从头播
  if (pendingResumeSec > 0 && state.duration > 0) {
    if (shouldRestartFromStart(pendingResumeSec, state.duration)) {
      stageRef.value?.seek(0);
    } else {
      stageRef.value?.seek(pendingResumeSec);
      ElMessage({
        message: `已从 ${formatDuration(pendingResumeSec)} 继续播放`,
        type: 'primary',
        duration: 5000
      });
    }
    pendingResumeSec = 0;
  }
  reporter.startTimer(getState);
};

const onPause = () => {
  reporter.report('pause', getState());
};

const onSeeked = () => {
  reporter.report('seek', getState());
};

const onStreamError = () => {
  if (error.value) {
    return;
  }
  error.value = '视频加载失败，可能是编码不支持（如 HEVC / AV1）';
};

const onEnded = async () => {
  reporter.stopTimer();
  const result = await reporter.report('ended', getState());
  const nextMediaId = result?.nextMediaId && result.nextMediaId !== '0' ? result.nextMediaId : '';
  if (!nextMediaId) {
    ElMessage({message: '已看完本番', type: 'success'});
    return;
  }
  countdown.value = 3;
  countdownTimer = window.setInterval(() => {
    countdown.value -= 1;
    if (countdown.value <= 0) {
      goNext();
    }
  }, 1000);
};

const goNext = () => {
  cancelCountdown();
  // ended 结果里的下一集
  const result = lastResult.value;
  const nextMediaId = result?.nextMediaId && result.nextMediaId !== '0' ? result.nextMediaId : '';
  const next = props.mediaList.find(item => item.mediaId === nextMediaId);
  if (next) {
    switchTo(next);
  }
};

const lastResult = ref<any>(null);
reporter.setResultHandler(result => {
  lastResult.value = result;
  emit('progress', result);
});

const cancelCountdown = () => {
  if (countdownTimer !== null) {
    window.clearInterval(countdownTimer);
    countdownTimer = null;
  }
  countdown.value = 0;
};

const onSelectEpisode = (media: any) => {
  if (media.mediaId === props.currentMediaId) {
    return;
  }
  switchTo(media);
};

const switchTo = async (media: any) => {
  cancelCountdown();
  if (!switching) {
    switching = true;
    try {
      // 切集前先 flush 当前集进度
      await reporter.report('close', getState());
    } finally {
      switching = false;
    }
  }
  reporter.stopTimer();
  reporter.reset();
  // 换集由 currentMediaId watcher 统一加载，并按目标集历史进度续播
  emit('switch', media);
  router.replace({query: {...router.currentRoute.value.query, ep: media.mediaId}});
};

const requestClose = () => {
  if (closing) {
    return;
  }
  closing = true;
  cancelCountdown();
  reporter.stopTimer();
  reporter.report('close', getState());
  stageRef.value?.close();
  emit('update:visible', false);
};

const onPageHide = () => {
  reporter.beacon(getState());
};

/**
 * 打开与换集合并成一个 watcher：同一 tick 内 visible + currentMediaId 一起变化时只触发一次，
 * 避免第二个 watcher 把刚设置的 pendingResumeSec 清零、并重复加载。
 */
watch([() => props.visible, () => props.currentMediaId],
    ([visible, mediaId], [oldVisible, oldMediaId]) => {
      if (!visible) {
        if (oldVisible) {
          // 父组件直接关闭（如浏览器后退）时在这里补报，requestClose 已报过则跳过
          if (!closing) {
            reporter.report('close', getState());
          }
          closing = true;
          window.removeEventListener('pagehide', onPageHide);
          pagehideInstalled = false;
          stageRef.value?.close();
        }
        return;
      }
      if (!oldVisible) {
        closing = false;
        // 打开弹窗：续播位置以父组件传入为准（?t= 优先，其次历史进度）
        pendingResumeSec = props.resumeSec;
        if (!pagehideInstalled) {
          window.addEventListener('pagehide', onPageHide);
          pagehideInstalled = true;
        }
        loadStream();
        return;
      }
      if (mediaId && mediaId !== oldMediaId) {
        // 弹窗内换集：续播目标集自己的历史进度
        reporter.reset();
        pendingResumeSec = resumeSecFor(mediaId);
        loadStream();
      }
    });

// 快捷键：只在弹窗打开且焦点不在输入框时生效
const onKeydown = (e: KeyboardEvent) => {
  if (!props.visible) {
    return;
  }
  const target = e.target as HTMLElement;
  if (target && (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA')) {
    return;
  }
  const stage = stageRef.value;
  switch (e.key) {
    case ' ':
    case 'k':
    case 'K':
      e.preventDefault();
      getState().paused ? stage?.play() : stage?.pause();
      break;
    case 'ArrowLeft':
      stage?.seek(Math.max(0, getState().currentTime - 5));
      break;
    case 'ArrowRight':
      stage?.seek(getState().currentTime + 5);
      break;
    case 'ArrowUp':
    case 'ArrowDown':
      // 音量交给 xgplayer 自身控件
      break;
    case 'm':
    case 'M':
      stage?.mute();
      break;
    case 'f':
    case 'F':
      stage?.getFullscreen();
      break;
    case 'Escape':
      if (document.fullscreenElement) {
        document.exitFullscreen();
      } else {
        requestClose();
      }
      break;
    case '[': {
      const idx = props.mediaList.findIndex(item => item.mediaId === props.currentMediaId);
      if (idx > 0) {
        switchTo(props.mediaList[idx - 1]);
      }
      break;
    }
    case ']': {
      const idx = props.mediaList.findIndex(item => item.mediaId === props.currentMediaId);
      if (idx >= 0 && idx < props.mediaList.length - 1) {
        switchTo(props.mediaList[idx + 1]);
      }
      break;
    }
  }
};

window.addEventListener('keydown', onKeydown);

onBeforeUnmount(() => {
  window.removeEventListener('keydown', onKeydown);
  window.removeEventListener('pagehide', onPageHide);
  reporter.stopTimer();
  cancelCountdown();
});
</script>

<style scoped>
.playback-dialog__wrap {
  background: #0B0B0F;
  border-radius: var(--ea-radius-lg);
  overflow: hidden;
}

.playback-dialog__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--ea-space-3) var(--ea-space-4);
  color: #E5E7EB;
  transition: opacity 200ms;
}

.playback-dialog__header.is-hidden,
.playback-dialog__strip.is-hidden {
  opacity: 0;
  pointer-events: none;
}

.playback-dialog__title {
  font-size: 15px;
}

.playback-dialog__close {
  cursor: pointer;
  color: #E5E7EB;
}

.playback-dialog__stage {
  position: relative;
  aspect-ratio: 16 / 9;
}

.playback-dialog__next {
  position: absolute;
  left: 50%;
  bottom: 64px;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: var(--ea-space-3);
  padding: var(--ea-space-2) var(--ea-space-4);
  background: rgba(12, 12, 16, .8);
  border-radius: var(--ea-radius-sm);
  color: #E5E7EB;
  font-size: 13px;
}

.playback-dialog__strip {
  padding: var(--ea-space-3) var(--ea-space-4);
  transition: opacity 200ms;
}
</style>

<style>
.playback-dialog .el-dialog__header {
  display: none;
}

.playback-dialog .el-dialog__body {
  padding: 0;
  background: #0B0B0F;
}
</style>
