<template>
  <div class="video-stage">
    <div v-show="ready" ref="stageRef" class="video-stage__player"></div>
    <div v-if="!ready" class="video-stage__loading">
      <span>正在加载…</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import {onBeforeUnmount, ref} from "vue";
import {useXgPlayer} from "@/composables/useXgPlayer";

const emit = defineEmits<{
  (e: 'metadata'): void;
  (e: 'ended'): void;
  (e: 'error'): void;
  (e: 'pause'): void;
  (e: 'play'): void;
  (e: 'seeked'): void;
}>();

const stageRef = ref<HTMLElement>();
const ready = ref<boolean>(false);

const {create, destroy, switchSrc, get} = useXgPlayer();

const createPlayer = (url: string) => {
  if (!stageRef.value) {
    return;
  }
  create(stageRef.value, {
    onMetadata: () => {
      ready.value = true;
      emit('metadata');
    },
    onEnded: () => emit('ended'),
    onError: () => {
      ready.value = false;
      emit('error');
    },
    onPause: () => emit('pause'),
    onPlay: () => emit('play'),
    onSeeked: () => emit('seeked')
  }, url);
};

onBeforeUnmount(() => {
  destroy();
});

defineExpose({
  /** 实例存在时只切源；close 后重开则重建，避免落空 */
  open: (url: string) => {
    ready.value = false;
    if (get()) {
      switchSrc(url);
    } else {
      createPlayer(url);
    }
  },
  close: () => {
    destroy();
    ready.value = false;
  },
  seek: (sec: number) => get()?.seek(sec),
  play: () => get()?.play(),
  pause: () => get()?.pause(),
  mute: () => {
    const player = get();
    if (player) {
      player.mute = !player.mute;
    }
  },
  getFullscreen: () => get()?.getFullscreen(),
  getState: () => {
    const player = get();
    return {
      currentTime: player ? Number(player.currentTime) || 0 : 0,
      duration: player ? Number(player.duration) || 0 : 0,
      paused: player ? player.paused : true
    };
  }
});
</script>

<style scoped>
.video-stage {
  position: relative;
  width: 100%;
  height: 100%;
  background: #0B0B0F;
}

.video-stage__player {
  width: 100%;
  height: 100%;
}

.video-stage__loading {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--ea-text-3);
  font-size: 14px;
}
</style>
