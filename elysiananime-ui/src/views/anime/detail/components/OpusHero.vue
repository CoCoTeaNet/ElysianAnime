<template>
  <section class="ea-detail__hero">
    <div class="ea-hero__bg" :style="bgStyle"></div>
    <div class="ea-hero__content">
      <div class="ea-hero__cover">
        <img v-if="opus.coverUrl" :src="coverSrc" :alt="opus.nameCn" loading="lazy" decoding="async"/>
      </div>
      <div class="ea-hero__info">
        <h1 class="ea-hero__title">{{ opus.nameCn }}</h1>
        <p class="ea-hero__original">{{ opus.nameOriginal }}</p>
        <p class="ea-hero__meta">
          <span v-if="opus.score">★ {{ opus.score }}</span>
          <span v-if="opus.episodes">共 {{ opus.episodes }} 话</span>
          <span v-if="opus.launchStart">{{ opus.launchStart }}</span>
          <span v-if="opus.deliveryWeek">每周{{ deliveryWeekCn }}</span>
        </p>
        <div class="ea-hero__tags">
          <el-tag v-for="tag in opus.aniTags" :key="tag.id" size="small" effect="plain">
            {{ tag.tagName || '...' }}
          </el-tag>
        </div>
        <div class="ea-hero__actions">
          <el-button type="primary" :disabled="!hasMedia" @click="$emit('play')">
            {{ playLabel }}
          </el-button>
          <el-button v-if="opus.isFollow === 1" @click="$emit('unfollow')">取消追番</el-button>
          <el-button v-else @click="$emit('follow')">追番</el-button>
          <el-button v-if="opus.readStatus !== 1" @click="$emit('mark-finished')">标记已看完</el-button>
          <el-dropdown trigger="click">
            <el-button class="ea-hero__more">
              <el-icon>
                <MoreFilled/>
              </el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$emit('share')">
                  {{ opus.isShare === 1 ? '取消推荐' : '推荐番剧' }}
                </el-dropdown-item>
                <el-dropdown-item v-if="canOperateRss && opus.rssStatus === 1" @click="$emit('close-rss')">
                  关闭订阅
                </el-dropdown-item>
                <el-dropdown-item v-if="opus.detailInfoUrl" @click="openBgm">番组计划</el-dropdown-item>
                <el-dropdown-item @click="copyLink">复制链接</el-dropdown-item>
                <el-dropdown-item v-if="canRescan" divided @click="$emit('rescan')">重新扫描</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import {computed} from "vue";
import {MoreFilled} from "@element-plus/icons-vue";
import {ElMessage} from "element-plus";
import {API_BASE} from "@/utils/axios-util";

const props = defineProps<{
  opus: any;
  /** 续播集展示名，如「第 5 集 · 剩 12:34」 */
  continueLabel: string;
  hasMedia: boolean;
  canRescan: boolean;
  canOperateRss: boolean;
}>();

defineEmits<{
  (e: 'play'): void;
  (e: 'follow'): void;
  (e: 'unfollow'): void;
  (e: 'mark-finished'): void;
  (e: 'share'): void;
  (e: 'close-rss'): void;
  (e: 'rescan'): void;
}>();

const playLabel = computed(() => props.continueLabel || '开始观看');

const coverSrc = computed(() => `${API_BASE}/anime/opus/cover?resName=${props.opus.coverUrl}&w=480&h=640`);

const bgStyle = computed(() => props.opus.coverUrl
    ? {backgroundImage: `url(${coverSrc.value})`}
    : {});

const deliveryWeekCn = computed(() => {
  const map: Record<string, string> = {
    '1': '一', '2': '二', '3': '三', '4': '四', '5': '五', '6': '六', '7': '日'
  };
  return map[String(props.opus.deliveryWeek)] || props.opus.deliveryWeek || '';
});

const openBgm = () => {
  window.open(`https://bgm.tv${props.opus.detailInfoUrl}`, '_blank');
};

const copyLink = () => {
  navigator.clipboard.writeText(window.location.href)
      .then(() => ElMessage.success('链接已复制'))
      .catch(() => ElMessage.error('复制失败'));
};
</script>

<style scoped>
.ea-detail__hero {
  position: relative;
  min-height: min(420px, 45vh);
  border-radius: var(--ea-radius-md);
  overflow: hidden;
  background: var(--ea-surface);
}

.ea-hero__bg {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  filter: blur(24px) saturate(.8);
  transform: scale(1.1);
}

.ea-hero__bg::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, rgba(255, 255, 255, .92), rgba(255, 255, 255, .75));
}

.ea-hero__content {
  position: relative;
  display: flex;
  gap: var(--ea-space-5);
  padding: var(--ea-space-5);
}

.ea-hero__cover {
  flex: none;
  width: 200px;
  aspect-ratio: 3 / 4;
  border-radius: var(--ea-radius-sm);
  overflow: hidden;
  box-shadow: var(--ea-shadow-2);
  background: var(--ea-bg);
}

.ea-hero__cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.ea-hero__info {
  display: flex;
  flex-direction: column;
  gap: var(--ea-space-2);
  min-width: 0;
}

.ea-hero__title {
  margin: 0;
  font-size: 28px;
  color: var(--ea-text-1);
}

.ea-hero__original {
  margin: 0;
  font-size: 14px;
  color: var(--ea-text-2);
}

.ea-hero__meta {
  margin: 0;
  display: flex;
  flex-wrap: wrap;
  gap: var(--ea-space-3);
  font-size: 13px;
  color: var(--ea-text-2);
}

.ea-hero__tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--ea-space-2);
}

.ea-hero__actions {
  margin-top: auto;
  display: flex;
  align-items: center;
  gap: var(--ea-space-2);
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .ea-hero__content {
    flex-direction: column;
  }

  .ea-hero__cover {
    width: 96px;
  }
}
</style>
