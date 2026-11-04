<template>
  <div class="ea-card" :class="{'is-empty-resource': !opus.hasResource}" @click="$emit('open', opus)">
    <div class="ea-card__cover">
      <img :src="coverSrc" :alt="opus.nameCn" loading="lazy" decoding="async"/>
      <div v-if="hasProgress" class="ea-card__progress" :style="{width: `${progressPercent * 100}%`}"></div>
      <span v-if="opus.hasUpdate === 1" class="ea-card__badge is-new">NEW</span>
      <span v-else-if="opus.isCompleted === 1" class="ea-card__badge is-done">✓</span>
      <span v-else-if="!opus.hasResource" class="ea-card__badge is-waiting">等待资源</span>
      <span v-if="!isMobile" class="ea-card__hover-play" @click.stop="$emit('play', opus)">
        <el-icon :size="36" color="#fff">
          <VideoPlay/>
        </el-icon>
      </span>
      <el-dropdown class="ea-card__more" trigger="click" @command="onCommand">
        <span class="ea-card__more-btn" @click.stop>
          <el-icon>
            <MoreFilled/>
          </el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="play">播放</el-dropdown-item>
            <el-dropdown-item v-if="opus.isFollow === 1" command="unfollow">取消追番</el-dropdown-item>
            <el-dropdown-item v-else command="follow">追番</el-dropdown-item>
            <el-dropdown-item command="feed" divided>投喂</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    <p class="ea-card__name" :title="opus.nameCn">{{ opus.nameCn }}</p>
    <p class="ea-card__meta">
      <span class="ea-card__status" :class="`is-${statusKey}`">{{ statusText }}</span>
      <span v-if="metaLine" class="ea-card__meta-text">· {{ metaLine }}</span>
    </p>
  </div>
</template>

<script setup lang="ts">
import {computed} from "vue";
import {MoreFilled, VideoPlay} from "@element-plus/icons-vue";
import {API_BASE} from "@/utils/axios-util";

const props = defineProps<{ opus: any }>();
const emit = defineEmits<{
  (e: 'open', opus: any): void;
  (e: 'play', opus: any): void;
  (e: 'follow', opus: any): void;
  (e: 'unfollow', opus: any): void;
  (e: 'feed', opus: any): void;
}>();

const isMobile = !!navigator.userAgent.match(/AppleWebKit.*Mobile.*/);

const coverSrc = computed(() => `${API_BASE}/anime/opus/cover?resName=${props.opus.coverUrl || ''}&w=360&h=480`);

const hasProgress = computed(() => {
  if (props.opus.isCompleted === 1) return Number(props.opus.mediaCount) > 0;
  return Number(props.opus.positionMs) > 0 && Number(props.opus.durationMs) > 0;
});

const progressPercent = computed(() => {
  if (props.opus.isCompleted === 1) {
    const count = Number(props.opus.mediaCount);
    return count > 0 ? Number(props.opus.completedCount) / count : 0;
  }
  const duration = Number(props.opus.durationMs);
  return duration > 0 ? Math.min(Number(props.opus.positionMs) / duration, 1) : 0;
});

const metaLine = computed(() => {
  const parts: string[] = [];
  if (Number(props.opus.isFollow) === 0) {
    if (Number(props.opus.mediaCount) > 0) {
      parts.push(`共 ${props.opus.mediaCount} 集`);
    } else if (props.opus.launchStart) {
      parts.push(String(props.opus.launchStart).slice(0, 7).replace('-', '/'));
    }
  } else if (Number(props.opus.mediaCount) > 0) {
    parts.push(`已看 ${props.opus.completedCount || 0}/${props.opus.mediaCount}`);
  } else if (props.opus.launchStart) {
    parts.push(String(props.opus.launchStart).slice(0, 7).replace('-', '/'));
  }
  if (props.opus.deliveryWeek) {
    parts.push(`每周${props.opus.deliveryWeek}`);
  }
  return parts.join(' · ');
});

const statusKey = computed(() => {
  if (Number(props.opus.isFollow) === 0) return 'none';
  const readStatus = Number(props.opus.readStatus);
  if (readStatus === 1 || props.opus.isCompleted === 1) return 'done';
  if (readStatus === 2 || Number(props.opus.positionMs) > 0) return 'reading';
  return 'unread';
});

const statusText = computed(() => ({done: '已看完', reading: '在看', unread: '未看', none: '未追番'}[statusKey.value]));

const onCommand = (cmd: string) => {
  if (cmd === 'play') emit('play', props.opus);
  else if (cmd === 'follow') emit('follow', props.opus);
  else if (cmd === 'unfollow') emit('unfollow', props.opus);
  else if (cmd === 'feed') emit('feed', props.opus);
};
</script>

<style scoped>
.ea-card {
  cursor: pointer;
}

.ea-card__cover {
  position: relative;
  aspect-ratio: 3 / 4;
  border-radius: var(--ea-radius-sm);
  overflow: hidden;
  background: var(--ea-border);
  transition: transform var(--ea-duration), box-shadow var(--ea-duration);
}

.ea-card:hover .ea-card__cover {
  transform: translateY(-2px);
  box-shadow: var(--ea-shadow-2);
}

.ea-card.is-empty-resource .ea-card__cover img {
  filter: saturate(.2) opacity(.6);
}

.ea-card__cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.ea-card__progress {
  position: absolute;
  left: 0;
  bottom: 0;
  height: 3px;
  background: var(--ea-color-primary);
  z-index: 2;
}

.ea-card__badge {
  position: absolute;
  top: var(--ea-space-2);
  right: var(--ea-space-2);
  padding: 2px 6px;
  border-radius: var(--ea-radius-sm);
  font-size: 11px;
  line-height: 1.4;
  color: #fff;
  z-index: 2;
}

.ea-card__badge.is-new {
  background: var(--ea-accent-update);
}

.ea-card__badge.is-done {
  background: var(--ea-success);
}

.ea-card__badge.is-waiting {
  background: rgba(31, 35, 41, .6);
}

.ea-card__hover-play {
  position: absolute;
  inset: 0;
  display: none;
  align-items: center;
  justify-content: center;
  background: linear-gradient(0deg, rgba(0, 0, 0, .45), transparent 55%);
  z-index: 1;
}

.ea-card:hover .ea-card__hover-play {
  display: flex;
}

.ea-card__more {
  position: absolute;
  top: var(--ea-space-2);
  left: var(--ea-space-2);
  z-index: 3;
  opacity: 0;
  transition: opacity var(--ea-duration);
}

.ea-card:hover .ea-card__more {
  opacity: 1;
}

.ea-card__more-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(255, 255, 255, .85);
  color: var(--ea-text-1);
}

.ea-card__name {
  margin: var(--ea-space-2) 0 0;
  font-size: 13px;
  color: var(--ea-text-1);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ea-card__meta {
  margin: 2px 0 0;
  font-size: 12px;
  color: var(--ea-text-3);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ea-card__status.is-done {
  color: var(--ea-success);
}

.ea-card__status.is-reading {
  color: var(--ea-color-primary);
}

.ea-card__status.is-unread,
.ea-card__status.is-none {
  color: var(--ea-text-3);
}

@media (max-width: 768px) {
  .ea-card__more {
    opacity: 1;
  }

  .ea-card__hover-play {
    display: none;
  }
}
</style>
