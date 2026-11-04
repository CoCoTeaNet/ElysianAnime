<template>
  <section v-if="items.length" class="ea-ru" aria-label="最近更新">
    <div class="ea-ru__head">
      <h2 class="ea-ru__title">最近更新</h2>
      <el-button link type="primary" @click="$emit('more')">查看全部</el-button>
    </div>
    <div class="ea-ru__row">
      <div v-for="item in visibleItems" :key="String(item.id)" class="ea-ru__item" @click="$emit('open', item)">
        <img class="ea-ru__cover" :src="coverSrc(item)" :alt="item.opusName" loading="lazy" decoding="async"/>
        <div class="ea-ru__text">
          <p class="ea-ru__name">{{ item.opusName }} {{ displayName(item) }}</p>
          <p class="ea-ru__time">{{ relativeTime(item.createTime) }}</p>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import {computed, onMounted, ref} from "vue";
import {latest} from "@/api/anime/ani-update-api";
import {API_BASE} from "@/utils/axios-util";

defineEmits<{ (e: 'open', item: any): void; (e: 'more'): void }>();

const items = ref<any[]>([]);

// 侧栏空间有限，只展示最近 4 条，完整动态在「查看全部」里
const visibleItems = computed(() => items.value.slice(0, 4));

onMounted(async () => {
  const res: any = await latest(12);
  if (res && res.code === 200) {
    items.value = res.data || [];
  }
});

const coverSrc = (item: any) => `${API_BASE}/anime/opus/cover?resName=${item.coverUrl || ''}&w=240&h=320`;

const displayName = (item: any) => {
  const name = item.displayName || item.episodeKey || '';
  return name ? `· ${name}` : '';
};

const relativeTime = (time: string) => {
  if (!time) return '';
  const diff = Date.now() - new Date(time).getTime();
  const minute = 60_000;
  if (diff < minute) return '刚刚';
  if (diff < 60 * minute) return `${Math.floor(diff / minute)} 分钟前`;
  if (diff < 24 * 60 * minute) return `${Math.floor(diff / (60 * minute))} 小时前`;
  if (diff < 30 * 24 * 60 * minute) return `${Math.floor(diff / (24 * 60 * minute))} 天前`;
  return time.slice(0, 10);
};
</script>

<style scoped>
.ea-ru {
  margin-bottom: var(--ea-space-6);
}

.ea-ru__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--ea-space-3);
}

.ea-ru__title {
  margin: 0;
  font-size: 18px;
  color: var(--ea-text-1);
}

.ea-ru__row {
  display: flex;
  flex-direction: column;
  gap: var(--ea-space-2);
}

.ea-ru__item {
  display: flex;
  align-items: center;
  gap: var(--ea-space-3);
  padding: var(--ea-space-2) var(--ea-space-3);
  border: 1px solid var(--ea-border);
  border-radius: var(--ea-radius-md);
  background: var(--ea-surface);
  cursor: pointer;
  transition: transform var(--ea-duration), box-shadow var(--ea-duration);
}

.ea-ru__item:hover {
  transform: translateY(-2px);
  box-shadow: var(--ea-shadow-2);
}

.ea-ru__cover {
  width: 40px;
  height: 53px;
  border-radius: var(--ea-radius-sm);
  object-fit: cover;
  background: var(--ea-border);
  flex: none;
}

.ea-ru__text {
  flex: 1;
  min-width: 0;
}

.ea-ru__name {
  margin: 0;
  font-size: 13px;
  color: var(--ea-text-1);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ea-ru__time {
  margin: 2px 0 0;
  font-size: 12px;
  color: var(--ea-text-3);
  white-space: nowrap;
}
</style>
