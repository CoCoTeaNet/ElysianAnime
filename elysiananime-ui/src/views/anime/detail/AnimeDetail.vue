<template>
  <div class="ea-detail">
    <opus-hero
        v-if="opus"
        :opus="opus"
        :continue-label="continueLabel"
        :has-media="mediaList.length > 0"
        :can-rescan="canRescan"
        :can-operate-rss="canOperateRss"
        @play="onPlayContinue"
        @follow="onFollow"
        @unfollow="onUnfollow"
        @mark-finished="onMarkFinished"
        @share="onShare"
        @close-rss="onCloseRss"
        @rescan="onRescan"
    />
    <div v-else class="ea-detail__skeleton">
      <el-skeleton :rows="4" animated/>
    </div>

    <opus-meta-panel v-if="opus" :opus="opus"/>

    <episode-grid
        :media-list="mediaList"
        :progress-map="progressMap"
        :latest-episode-key="opus?.mediaLatestKey || ''"
        :current-media-id="currentMediaId"
        :loading="loading"
        @select="onSelectEpisode"
    />

    <playback-dialog
        v-model:visible="dialogVisible"
        :opus-id="opusId"
        :opus-name="opus?.nameCn || ''"
        :media-list="mediaList"
        :current-media-id="currentMediaId"
        :progress-map="progressMap"
        :latest-episode-key="opus?.mediaLatestKey || ''"
        :resume-sec="dialogResumeSec"
        :can-rescan="canRescan"
        @switch="onDialogSwitch"
        @rescan="onRescan"
        @progress="onProgressResult"
    />
  </div>
</template>

<script setup lang="ts">
import {computed, nextTick, onMounted, ref, watch} from "vue";
import {useRoute} from "vue-router";
import {ElMessage} from "element-plus";
import {router} from "@/router";
import {getOpusMedia} from "@/api/anime/ani-opus-api";
import userOpusApi, {unfollow} from "@/api/anime/ani-user-opus-api";
import rssApi from "@/api/anime/ani-rss-api";
import {scanOpus} from "@/api/anime/ani-media-api";
import sysUserApi from "@/api/system/sys-user-api.ts";
import {reqCommonFeedback, reqSuccessFeedback} from "@/api/ApiFeedback";
import {addTabItem, updateTabItem, useStore} from "@/store";
import OpusHero from "@/views/anime/detail/components/OpusHero.vue";
import OpusMetaPanel from "@/views/anime/detail/components/OpusMetaPanel.vue";
import EpisodeGrid from "@/views/anime/detail/components/EpisodeGrid.vue";
import PlaybackDialog from "@/components/player/PlaybackDialog.vue";

defineOptions({name: 'AnimeDetail'});

const route = useRoute();
const store = useStore();

const loading = ref<boolean>(true);
const opus = ref<any>(null);
const mediaList = ref<any[]>([]);
const progressMap = ref<Record<string, any>>({});
const roleKeys = ref<string[]>([]);

const dialogVisible = ref<boolean>(false);
const dialogResumeSec = ref<number>(0);
const currentMediaId = ref<string>('');

// 本会话是否由站内 push 带 ep 进入：决定关闭弹窗用 back 还是 replace
let openedByPush = false;
// 切换番剧时复用组件实例，弹窗关闭回调不能走 back/replace 和重复加载
let switchingOpus = false;

const opusId = computed(() => route.params.id as string);

const continueProgress = computed(() => {
  if (!opus.value?.lastEpisodeKey) {
    return null;
  }
  return progressMap.value[opus.value.lastEpisodeKey];
});

const continueEpisode = computed(() =>
    mediaList.value.find(item => item.episodeKey === opus.value?.lastEpisodeKey));

const continueLabel = computed(() => {
  const media = continueEpisode.value;
  if (!media || !continueProgress.value) {
    return '';
  }
  if (continueProgress.value.isCompleted === 1) {
    return '';
  }
  return `继续观看 ${media.displayName}`;
});

const canRescan = computed(() =>
    roleKeys.value.includes('role:super:admin') || roleKeys.value.includes('role:simple:admin'));

const canOperateRss = computed(() =>
    roleKeys.value.includes('bangumi:rss:subscriber') || roleKeys.value.includes('role:super:admin'));

onMounted(() => {
  loadRoles();
  load();
});

const loadRoles = () => {
  reqCommonFeedback(sysUserApi.getDetail(), (data: any) => {
    data.roleList.forEach((item: { roleKey: string }) => roleKeys.value.push(item.roleKey));
  });
};

const load = () => {
  const targetId = opusId.value;
  loading.value = true;
  reqCommonFeedback(getOpusMedia(targetId), (data: any) => {
    // 快速切换标签时丢弃上一部作品的迟到响应
    if (targetId !== opusId.value) {
      return;
    }
    opus.value = data;
    mediaList.value = data.mediaList || [];
    progressMap.value = data.progressMap || {};
    loading.value = false;

    // 标签页按路径（不含 ep 查询参数）去重，避免同一部番剧每看一集就多一个标签
    addTabItem({name: data.nameCn, url: route.path, isActive: true});
    updateTabItem(route.path, data.nameCn);

    handleLegacyQuery();
    openFromQuery();
  });
};

// 路由参数变化时组件实例会被复用（顶部标签切换两个详情页），必须重新加载
watch(opusId, () => {
  switchingOpus = true;
  dialogVisible.value = false;
  openedByPush = false;
  currentMediaId.value = '';
  dialogResumeSec.value = 0;
  opus.value = null;
  mediaList.value = [];
  progressMap.value = {};
  load();
  nextTick(() => {
    switchingOpus = false;
  });
});

/**
 * 旧链接 /admin/ani-video/:id/:num/:time 被 redirect 成 ?ek=&t=，
 * 这里用 mediaList 反查出 mediaId 后 replace 成 ?ep=&t=，匹配不到就提示
 */
const handleLegacyQuery = () => {
  const ek = route.query.ek as string;
  if (!ek) {
    return;
  }
  const media = mediaList.value.find(item => item.episodeKey === ek);
  const query = {...route.query};
  delete query.ek;
  if (media) {
    query.ep = media.mediaId;
    query.t = route.query.t;
  } else {
    ElMessage.warning('未找到该集');
  }
  router.replace({query});
};

const resumeSecOf = (mediaId: string): number => {
  const media = mediaList.value.find(item => item.mediaId === mediaId);
  const progress = media ? progressMap.value[media.episodeKey] : null;
  if (!progress || progress.isCompleted === 1) {
    return 0;
  }
  return Math.floor((progress.positionMs || 0) / 1000);
};

const openFromQuery = () => {
  const ep = route.query.ep as string;
  if (ep && mediaList.value.some(item => item.mediaId === ep)) {
    currentMediaId.value = ep;
    dialogResumeSec.value = Number(route.query.t) || resumeSecOf(ep);
    dialogVisible.value = true;
  }
};

watch(() => route.query.ep, (ep, old) => {
  if (ep === old) {
    return;
  }
  if (ep && typeof ep === 'string' && mediaList.value.some(item => item.mediaId === ep)) {
    openedByPush = true;
    currentMediaId.value = ep;
    dialogResumeSec.value = Number(route.query.t) || resumeSecOf(ep);
    dialogVisible.value = true;
  } else if (!ep) {
    dialogVisible.value = false;
  }
});

watch(dialogVisible, value => {
  if (switchingOpus) {
    return;
  }
  if (!value) {
    // 关闭弹窗：历史里有 ep 项就 back，刷新直达的 replace 去掉 ep
    if (route.query.ep) {
      if (openedByPush) {
        router.back();
      } else {
        const query = {...route.query};
        delete query.ep;
        delete query.t;
        router.replace({query});
      }
      openedByPush = false;
    }
    load();
  }
});

const onPlayContinue = () => {
  // 主按钮：有进度指向 lastMediaId（为 0 时按 lastEpisodeKey 反查），否则第一集
  const target = continueEpisode.value
      || mediaList.value.find(item => item.mediaId === opus.value?.lastMediaId)
      || mediaList.value[0];
  if (!target) {
    return;
  }
  onSelectEpisode(target);
};

const onSelectEpisode = (media: any) => {
  currentMediaId.value = media.mediaId;
  dialogResumeSec.value = resumeSecOf(media.mediaId);
  openedByPush = true;
  router.push({query: {...route.query, ep: media.mediaId}});
  dialogVisible.value = true;
};

const onDialogSwitch = (media: any) => {
  currentMediaId.value = media.mediaId;
};

const onProgressResult = (result: any) => {
  const media = mediaList.value.find(item => item.mediaId === currentMediaId.value);
  if (media && result) {
    progressMap.value = {
      ...progressMap.value,
      [media.episodeKey]: {
        positionMs: result.positionMs,
        durationMs: progressMap.value[media.episodeKey]?.durationMs || 0,
        isCompleted: result.isCompleted
      }
    };
  }
};

const onFollow = () => {
  reqCommonFeedback(userOpusApi.follow(opusId.value), () => {
    opus.value.isFollow = 1;
  });
};

const onUnfollow = () => {
  reqCommonFeedback(unfollow({opusId: opusId.value}), () => {
    opus.value.isFollow = 0;
  });
};

const onMarkFinished = () => {
  reqSuccessFeedback(userOpusApi.update({opusId: opusId.value, readStatus: 1}), '操作成功', () => {
    opus.value.readStatus = 1;
    load();
  });
};

const onShare = () => {
  reqSuccessFeedback(userOpusApi.share(opusId.value), '操作成功', () => {
    opus.value.isShare = opus.value.isShare === 1 ? 0 : 1;
  });
};

const onCloseRss = () => {
  reqSuccessFeedback(rssApi.closeSubscribe(opusId.value), '操作成功', () => {
    opus.value.rssStatus = 2;
  });
};

const onRescan = () => {
  reqCommonFeedback(scanOpus(opusId.value), () => {
    ElMessage.success('已触发重新扫描');
  });
};
</script>

<style scoped>
.ea-detail {
  display: flex;
  flex-direction: column;
  gap: var(--ea-space-4);
  padding: var(--ea-space-4);
}

.ea-detail__skeleton {
  background: var(--ea-surface);
  border-radius: var(--ea-radius-md);
  padding: var(--ea-space-5);
}
</style>
