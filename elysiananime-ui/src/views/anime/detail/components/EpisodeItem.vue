<template>
  <button
      class="ea-episode-item"
      :class="{'is-active': active, 'is-done': done}"
      :title="tooltip"
      @click="$emit('select', media)"
  >
    <span class="ea-episode-item__label">{{ label }}</span>
    <span
        v-if="progressRatio > 0 && progressRatio < 1"
        class="ea-episode-item__bar"
        :style="{width: (progressRatio * 100) + '%'}"
    ></span>
    <span v-if="done" class="ea-episode-item__check">✓</span>
    <span v-if="isLatest" class="ea-episode-item__dot"></span>
  </button>
</template>

<script setup lang="ts">
import {computed} from "vue";
import {formatDuration} from "@/composables/usePlaybackProgress";

const props = defineProps<{
  media: any;
  active: boolean;
  isLatest: boolean;
  progress?: { positionMs: number; durationMs: number; isCompleted: number };
}>();

defineEmits<{
  (e: 'select', media: any): void;
}>();

// 正片显示集数，SP / 无集数显示展示名；不从 displayName 反解数字
const label = computed(() =>
    props.media.isExtra === 1 ? (props.media.displayName || props.media.episodeKey) : props.media.episodeNo);

const done = computed(() => props.progress?.isCompleted === 1);

const progressRatio = computed(() => {
  const progress = props.progress;
  if (!progress || !progress.durationMs) {
    return 0;
  }
  return Math.min(progress.positionMs / progress.durationMs, 1);
});

const tooltip = computed(() => {
  if (!props.progress || !props.progress.durationMs || done.value) {
    return props.media.displayName;
  }
  const remainSec = (props.progress.durationMs - props.progress.positionMs) / 1000;
  return `${props.media.displayName} · 剩 ${formatDuration(remainSec)}`;
});
</script>

<style scoped>
.ea-episode-item {
  position: relative;
  min-width: 44px;
  height: 44px;
  padding: 0 var(--ea-space-2);
  border: 1px solid var(--ea-border);
  border-radius: var(--ea-radius-sm);
  background: var(--ea-surface);
  color: var(--ea-text-1);
  font-size: 14px;
  cursor: pointer;
  transition: all var(--ea-duration);
  overflow: hidden;
}

.ea-episode-item:hover {
  border-color: var(--ea-color-primary);
  color: var(--ea-color-primary);
  transform: translateY(-2px);
}

.ea-episode-item.is-active {
  border-color: var(--ea-color-primary);
  color: var(--ea-color-primary);
  font-weight: 600;
}

.ea-episode-item.is-done {
  background: #F5F7FA;
  color: var(--ea-text-3);
}

.ea-episode-item__bar {
  position: absolute;
  left: 0;
  bottom: 0;
  height: 2px;
  background: var(--ea-color-primary);
}

.ea-episode-item__check {
  position: absolute;
  top: 2px;
  left: 4px;
  font-size: 10px;
  color: var(--ea-success);
}

.ea-episode-item__dot {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--ea-accent-update);
}
</style>
