<template>
  <div class="ea-episode-grid">
    <el-alert
        v-if="!loading && mediaList.length === 0"
        title="尚未扫描到资源"
        type="info"
        :closable="false"
        show-icon
    />
    <template v-else>
      <div v-for="group in groups" :key="group.title" class="ea-episode-grid__group">
        <h4 class="ea-episode-grid__title">{{ group.title }}</h4>
        <div class="ea-episode-grid__list">
          <episode-item
              v-for="media in group.items"
              :key="media.mediaId"
              :media="media"
              :active="media.mediaId === currentMediaId"
              :is-latest="media.episodeKey === latestEpisodeKey"
              :progress="progressMap[media.episodeKey]"
              @select="$emit('select', $event)"
          />
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import {computed} from "vue";
import EpisodeItem from "@/views/anime/detail/components/EpisodeItem.vue";

const props = defineProps<{
  mediaList: any[];
  progressMap: Record<string, { positionMs: number; durationMs: number; isCompleted: number }>;
  latestEpisodeKey: string;
  currentMediaId: string;
  loading: boolean;
}>();

defineEmits<{
  (e: 'select', media: any): void;
}>();

// 按 isExtra 分组（episode_no 出参为 null 会序列化成 0，不能用是否为 0 判断）
const groups = computed(() => {
  const main = props.mediaList.filter(item => item.isExtra === 0);
  const extra = props.mediaList.filter(item => item.isExtra !== 0);
  const result: { title: string; items: any[] }[] = [];
  if (main.length > 0) {
    result.push({title: `正片 ${main.length}`, items: main});
  }
  if (extra.length > 0) {
    result.push({title: 'SP / 其他', items: extra});
  }
  return result;
});
</script>

<style scoped>
.ea-episode-grid__group {
  margin-bottom: var(--ea-space-4);
}

.ea-episode-grid__title {
  margin: 0 0 var(--ea-space-2);
  font-size: 15px;
  color: var(--ea-text-1);
}

.ea-episode-grid__list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(44px, 1fr));
  gap: var(--ea-space-2);
}

@media (max-width: 768px) {
  .ea-episode-grid__list {
    grid-template-columns: repeat(4, 1fr);
  }
}
</style>
