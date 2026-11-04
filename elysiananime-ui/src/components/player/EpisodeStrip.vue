<template>
  <div class="ea-episode-strip">
    <div class="ea-episode-strip__list">
      <button
          v-for="item in mediaList"
          :key="item.mediaId"
          class="ea-episode-strip__item"
          :class="{
            'is-active': item.mediaId === currentMediaId,
            'is-done': isDone(item),
            'is-latest': item.episodeKey === latestEpisodeKey
          }"
          :title="tooltip(item)"
          @click="$emit('select', item)"
      >
        <span class="ea-episode-strip__label">{{ label(item) }}</span>
        <span
            v-if="progressRatio(item) > 0"
            class="ea-episode-strip__bar"
            :style="{width: (progressRatio(item) * 100) + '%'}"
        ></span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
// 组内顺序直接用接口返回的顺序（服务端已按 sort_no 排好），前端不按 sortNo 重排
const props = defineProps<{
  mediaList: any[];
  currentMediaId: string;
  progressMap: Record<string, { positionMs: number; durationMs: number; isCompleted: number }>;
  latestEpisodeKey?: string;
}>();

defineEmits<{
  (e: 'select', media: any): void;
}>();

const isDone = (item: any) => props.progressMap[item.episodeKey]?.isCompleted === 1;

const progressRatio = (item: any) => {
  const progress = props.progressMap[item.episodeKey];
  if (!progress || !progress.durationMs) {
    return 0;
  }
  return Math.min(progress.positionMs / progress.durationMs, 1);
};

const label = (item: any) => item.episodeNo != null && item.episodeNo !== 0
    ? item.episodeNo
    : (item.displayName || item.episodeKey);

const tooltip = (item: any) => item.displayName || item.episodeKey;
</script>

<style scoped>
.ea-episode-strip__list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--ea-space-2);
  max-height: 180px;
  overflow: auto;
}

.ea-episode-strip__item {
  position: relative;
  min-width: 40px;
  height: 40px;
  padding: 0 var(--ea-space-2);
  border: 1px solid var(--ea-border);
  border-radius: var(--ea-radius-sm);
  background: var(--ea-surface);
  color: var(--ea-text-1);
  cursor: pointer;
  font-size: 13px;
  transition: all var(--ea-duration);
  overflow: hidden;
}

.ea-episode-strip__item:hover {
  border-color: var(--ea-color-primary);
  color: var(--ea-color-primary);
}

.ea-episode-strip__item.is-active {
  border-color: var(--ea-color-primary);
  color: var(--ea-color-primary);
  font-weight: 600;
}

.ea-episode-strip__item.is-done {
  background: #F5F7FA;
  color: var(--ea-text-3);
}

.ea-episode-strip__item.is-latest::after {
  content: '';
  position: absolute;
  top: 4px;
  right: 4px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--ea-accent-update);
}

.ea-episode-strip__bar {
  position: absolute;
  left: 0;
  bottom: 0;
  height: 2px;
  background: var(--ea-color-primary);
}
</style>
