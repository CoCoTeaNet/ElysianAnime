<template>
  <section v-if="items.length" class="ea-cw" aria-label="继续观看">
    <h2 class="ea-cw__title">继续观看</h2>
    <div class="ea-cw__row">
      <div v-if="items[0]" class="ea-cw__main" @click="$emit('play', items[0])">
        <div class="ea-cw__main-bg" :style="bgStyle(items[0])"></div>
        <div class="ea-cw__main-mask"></div>
        <div class="ea-cw__main-info">
          <p class="ea-cw__main-name">{{ items[0].nameCn }}</p>
          <p class="ea-cw__main-ep">{{ episodeLabel(items[0]) }}<template v-if="remainText(items[0])"> · 剩 {{ remainText(items[0]) }}</template></p>
          <el-button class="ea-cw__main-btn" type="primary" :icon="VideoPlay" @click.stop="$emit('play', items[0])">
            继续观看
          </el-button>
        </div>
      </div>
      <div v-for="item in items.slice(1)" :key="String(item.opusId)" class="ea-cw__card" @click="$emit('play', item)">
        <img class="ea-cw__card-cover" :src="coverSrc(item)" :alt="item.nameCn" loading="lazy" decoding="async"/>
        <p class="ea-cw__card-name">{{ item.nameCn }}</p>
        <p class="ea-cw__card-ep">{{ episodeLabel(item) }}</p>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import {computed, onMounted, ref} from "vue";
import {VideoPlay} from "@element-plus/icons-vue";
import {continueList} from "@/api/anime/ani-user-opus-api";
import {API_BASE} from "@/utils/axios-util";

defineEmits<{ (e: 'play', item: any): void }>();

const items = ref<any[]>([]);

onMounted(async () => {
  const res: any = await continueList();
  if (res && res.code === 200) {
    items.value = res.data || [];
  }
});

const coverSrc = (item: any) => `${API_BASE}/anime/opus/cover?resName=${item.coverUrl || ''}&w=480&h=640`;

const bgStyle = (item: any) => ({backgroundImage: `url(${coverSrc(item)})`});

const episodeLabel = (item: any) => item.displayName || item.episodeKey || '';

const remainText = (item: any) => {
  const duration = Number(item.durationMs || 0);
  const position = Number(item.positionMs || 0);
  if (duration <= 0 || position <= 0 || position >= duration) return '';
  return format(duration - position);
};

const format = (ms: number) => {
  const total = Math.floor(ms / 1000);
  const m = Math.floor(total / 60);
  const s = total % 60;
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
};
</script>

<style scoped>
.ea-cw {
  margin-bottom: var(--ea-space-6);
}

.ea-cw__title {
  margin: 0 0 var(--ea-space-3);
  font-size: 18px;
  color: var(--ea-text-1);
}

.ea-cw__row {
  display: flex;
  align-items: center;
  gap: var(--ea-space-4);
  overflow-x: auto;
}

.ea-cw__main {
  position: relative;
  flex: 1 1 560px;
  min-width: 320px;
  height: 320px;
  border-radius: var(--ea-radius-md);
  overflow: hidden;
  cursor: pointer;
  background: var(--ea-text-1);
  transition: transform var(--ea-duration), box-shadow var(--ea-duration);
}

.ea-cw__main:hover {
  transform: translateY(-2px);
  box-shadow: var(--ea-shadow-2);
}

.ea-cw__main-bg {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  filter: blur(16px) saturate(.9);
  transform: scale(1.15);
}

.ea-cw__main-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(0deg, rgba(0, 0, 0, .72), transparent 55%);
}

.ea-cw__main-info {
  position: absolute;
  left: var(--ea-space-5);
  right: var(--ea-space-5);
  bottom: var(--ea-space-4);
  color: #fff;
}

.ea-cw__main-name {
  margin: 0;
  font-size: 20px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ea-cw__main-ep {
  margin: var(--ea-space-1) 0 var(--ea-space-3);
  font-size: 14px;
  opacity: .85;
}

.ea-cw__main-btn {
  --el-button-bg-color: var(--ea-color-primary);
  --el-button-border-color: var(--ea-color-primary);
  --el-button-hover-bg-color: var(--ea-color-primary);
  --el-button-hover-border-color: var(--ea-color-primary);
}

.ea-cw__card {
  flex: 0 0 160px;
  cursor: pointer;
  transition: transform var(--ea-duration);
}

.ea-cw__card:hover {
  transform: translateY(-2px);
}

.ea-cw__card-cover {
  width: 160px;
  aspect-ratio: 3 / 4;
  border-radius: var(--ea-radius-sm);
  object-fit: cover;
  background: var(--ea-border);
  box-shadow: var(--ea-shadow-1);
}

.ea-cw__card-name {
  margin: var(--ea-space-2) 0 0;
  font-size: 13px;
  color: var(--ea-text-1);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ea-cw__card-ep {
  margin: 2px 0 0;
  font-size: 12px;
  color: var(--ea-text-3);
}

@media (max-width: 768px) {
  .ea-cw__main {
    flex: 0 0 100%;
    min-width: 0;
    height: 220px;
  }
}
</style>
