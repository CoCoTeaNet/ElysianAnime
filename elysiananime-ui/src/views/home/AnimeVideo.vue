<template>
  <div class="a-video-wrap">
    <!--布局左侧-->
    <div class="a-video-wrap-left">
      <div class="a-video-card">
        <h2 class="a-video-title">
          {{ videoInfo.nameCn }} 第{{ currentNum }}集
        </h2>
        <div id="dplayer" class="player"/>
      </div>
      <!--番剧信息-->
      <el-card shadow="hover" class="a-video-info-card-wrap no-border-card">
        <div class="a-video-info-card">
          <div style="width: 200px">
            <el-image
                v-if="videoInfo.coverUrl"
                :src="`api/anime/opus/cover?resName=${videoInfo.coverUrl}`"
                :alt="videoInfo.nameCn"
                style="width: 200px"
            />
          </div>
          <div style="margin-left: 1em">
            <p style="font-weight: 600; font-size: 22px">
              番名:
              <span style="color: var(--el-color-primary)">
                  {{ videoInfo.nameCn }}
                </span>
            </p>
            <div style="display: flex">
              <div>
                <p style="font-size: 14px">
                  原名:
                  <span style="color: #909399">
                  {{ videoInfo.nameOriginal }}
                </span>
                </p>
                <p style="font-size: 14px; max-width: 220px">
                  简介:
                  <el-scrollbar max-height="146">
                    <span style="color: #747474;" v-html="videoInfo.aniSummary"></span>
                  </el-scrollbar>
                </p>
              </div>
              <el-divider direction="vertical" style="opacity: 0"></el-divider>
              <div>
                <p style="font-size: 14px">
                  标签：
                  <el-tag effect="dark" v-for="tag in videoInfo.aniTags" style="margin: 0 3px 3px 0;">
                    {{ tag.tagName ? tag.tagName : '...' }}
                  </el-tag>
                </p>
                <p style="font-size: 14px">
                  RSS：
                  <el-link :href="videoInfo.rssUrl">
                    {{ videoInfo.rssUrl ? videoInfo.rssUrl : '无' }}
                  </el-link>
                </p>
                <p style="font-size: 14px">
                  番组计划：
                  <el-link @click="onOpenDetail(videoInfo.detailInfoUrl)">
                    {{ `https://bgm.tv/${videoInfo.detailInfoUrl}` }}
                  </el-link>
                </p>
                <p style="font-size: 14px">
                  放送日期：
                  <el-text class="mx-1" type="info">{{ videoInfo.launchStart }}</el-text>
                </p>
                <p style="font-size: 14px">
                  放送星期：
                  <el-text class="mx-1" type="info">{{ videoInfo.deliveryWeek }}</el-text>
                </p>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
    <!--布局右侧-->
    <div class="a-video-wrap-right">
      <h2 class="a-layout a-video-title">剧集列表
        <el-icon :size="'1.5rem'" class="switch-eplist-view" @click="epListNewStyle = !epListNewStyle">
          <Grid/>
        </el-icon>
      </h2>
      <el-card class="a-video-ep-card no-border-card" shadow="hover">
        <el-alert title="光速建设中......"
                  type="warning"
                  center
                  show-icon
                  :closable="false"
                  v-if="!loading && mediaList.length === 0"
        />
        <div class="a-layout eplist "
             :class="epListNewStyle ? 'eplist-piano-style': 'eplist-candy-style'"
             v-if="!loading">
          <el-button
              class="a-layout ep-button"
              v-for="item in mediaList"
              :type="item.episodes === currentNum ? 'primary' : 'default'"
              @click="doSwitchPlay(item)"
          >
            {{ item.episodes }}
          </el-button>
        </div>
      </el-card>
      <!--其它信息-->
      <h2 class="a-layout a-video-title" style="margin-top: 1em">追番操作</h2>
      <el-card shadow="hover" class="no-border-card">
        <el-form ref="sttFormRef" :model="editForm">
          <el-form-item label="追番状态：">
            <el-button
                type="primary"
                :icon="Star"
                @click="onFollowOpus(videoInfo.id)"
                :disabled="videoInfo.isFollow === 1"
            >
              {{ videoInfo.isFollow === 1 ? "已追番" : "追番" }}
            </el-button>
          </el-form-item>
          <el-form-item label="正在观看：">
            <div>第 {{ currentNum }} 集</div>
          </el-form-item>
          <el-form-item label="番剧话数：">
            <el-tooltip content="该数据来自bangumi">
              <el-tag effect="dark" type="success">共{{ videoInfo.episodes }}话</el-tag>
            </el-tooltip>
          </el-form-item>
          <el-form-item label="观看状态：" v-if="videoInfo.readStatus >= 0">
            <el-select
                placeholder="选择观看状态"
                clearable
                v-model="editForm.readStatus"
                @change="doUpdateReadStatus"
            >
              <el-option
                  v-for="i in acgUserOpusTypes.getReadStatusList()"
                  :label="i.label"
                  :value="i.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="推荐番剧：">
            <el-switch
                v-model="videoInfo.isShare"
                :active-value="1"
                :inactive-value="0"
                active-text="o(￣▽￣)ｄ"
                inactive-text="(ˇˍˇ) 想～"
                inline-prompt
                @click="onShareOpus(videoInfo.id)"
            />
          </el-form-item>
          <el-form-item label="番剧链接：">
            <el-input type="textarea" :model-value="shareUrl" autosize></el-input>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {nextTick, onMounted, onUnmounted, ref, watch} from "vue";
import {getOpusMedia} from "@/api/anime/ani-opus-api";
import {reqCommonFeedback, reqSuccessFeedback} from "@/api/ApiFeedback";
import {useRoute} from "vue-router";
import Dplayer from "dplayer";
import {router} from "@/router";
import {ElForm} from "element-plus";
import acgUserOpusTypes from "@/types/acg-user-opus-types";
import userOpusApi, {updateProgress} from "@/api/anime/ani-user-opus-api";
import {Star} from "@element-plus/icons-vue";
import formatUtil from "@/utils/format-util";

const route = useRoute();

const loading = ref<boolean>(true);
const videoInfo = ref<any>({readingTime: 0, isFollow: 0});
const mediaList = ref<any[]>([]);
const player = ref<Dplayer>(null);
const currentNum = ref<any>('0');
const editForm = ref<AcgUserOpusModel>({});
const epListNewStyle = ref<boolean>(true);
const shareUrl = ref<string>('');

const init = () => {
  if (route.params) {
    videoInfo.value.id = route.params.id;
    currentNum.value = route.params.num;
    videoInfo.value.readingTime = route.params.time;
  }

  // 创建h5播放器
  if (!player.value) {
    let dplayer = new Dplayer({
      autoplay: false,
      container: document.getElementById('dplayer'),
      video: {
        type: 'auto'
      }
    });
    dplayer.on('loadeddata', function () {
      dplayer.play();
    });
    player.value = dplayer;
  }
}

watch(
    () => route.params,
    (toParams, previousParams) => {
      init();
    }
)

onMounted(() => {
  init();
  nextTick(() => loadData());

  // 定时更新当前播放进度
  setInterval(() => {
    if (route.name !== 'AnimeVideo') {
      player.value.pause();
    }
    if (!player.value.video.paused && videoInfo.value.userOpusId > 0) {
      let currentTime = parseInt(player.value.video.currentTime);
      updateProgress({readingTime: currentTime, id: videoInfo.value.userOpusId});
    }
  }, 2500);

  window.addEventListener(
      "onorientationchange" in window ? "orientationchange" : "resize", onOrientationchange, false)
});

onUnmounted(() => {
  window.removeEventListener("onorientationchange" in window ? "orientationchange" : "resize", onOrientationchange, false)
});

const onOrientationchange = (): void => {
  if (window.orientation === 90 || window.orientation === -90) {
    player.value.fullScreen.request('browser')
  }
}

const loadData = (): void => {
  if (!loading.value) loading.value = true;
  reqCommonFeedback(getOpusMedia(route.params.id), (data: any) => {
    shareUrl.value = window.location.href + '?nameCn=' + data.nameCn;
    loading.value = false;
    videoInfo.value = data;
    mediaList.value = data.mediaList;
    editForm.value.readStatus = data.readStatus;

    if (!route.params.num && data.readingNum <= 0) {
      // 默认播放集数
      currentNum.value = data.mediaList[0].episodes;
      player.value.switchVideo({url: getMediaUrl(data.id, data.mediaList[0].episodes, data.mediaList[0].mediaType)});
    } else {
      // 历史播放
      let currentNumFormat = formatUtil.fillZero(data.readingNum);
      currentNum.value = currentNumFormat;
      player.value.seek(data.readingTime);
      player.value.switchVideo({
        url: getMediaUrl(data.id, currentNumFormat, data.mediaList[data.readingNum - 1].mediaType)
      });
      // 根据集数来自动选择选集风格
      if (data.mediaList.length > 13) {
        epListNewStyle.value = false;
      }
    }

    // 历史播放进度
    player.value.seek(data.readingTime);
  });
};

const doSwitchPlay = (item: any) => {
  // 路由传参
  router.push({
    name: "AnimeVideo",
    params: {id: route.params.id + '', num: item.episodes, time: 1},
  });
  player.value.switchVideo({url: getMediaUrl(videoInfo.value.id, item.episodes, item.mediaType)});
  // 切换进度
  player.value.seek(0);
  // 显示当前播放集数
  currentNum.value = item.episodes;
  // 更新当前播放集数
  updateProgress({ readingNum: item.episodes, readingTime: 0, id: videoInfo.value.userOpusId });
};

const getMediaUrl = (id: string, episodes: string, mediaType: string) => {
  let url = `/api/anime/opus/media/${id}?resName=${episodes}.${mediaType}`;
  console.log(url);
  return url;
}

const onFollowOpus = (id: string) => {
  reqCommonFeedback(userOpusApi.follow(id), () => {
    videoInfo.value.isFollow = 1;
  });
};

const onShareOpus = (id: string) => {
  reqSuccessFeedback(userOpusApi.share(id), "操作成功", () => {});
}

const doUpdateReadStatus = (): void => {
  reqSuccessFeedback(updateProgress({
        readStatus: editForm.value.readStatus,
        id: videoInfo.value.userOpusId,
      }), "操作成功", (flag: boolean) => {
        if (flag) {
          videoInfo.value.readStatus = editForm.value.readStatus;
        } else {
          console.log('更新失败...')
        }
      }
  );
};

const onOpenDetail = (url: string) => {
  window.open(`https://bgm.tv${url}`, "_blank");
};

</script>

<style src="./AdminVideo.css"></style>
