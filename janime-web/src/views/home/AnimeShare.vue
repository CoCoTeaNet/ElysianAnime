<template>
  <el-space direction="vertical" alignment="normal" v-loading="sharesLoading">
    <div v-for="videoInfo in shares" class="a-video-info-card">
      <div style="width: 200px">
        <el-image
            :src="`api/anime/opus/cover?resName=${videoInfo.coverUrl}`"
            :alt="videoInfo.opusName"
            style="width: 200px"
        />
      </div>
      <div style="margin-left: 1em">
        <p style="font-weight: 600; font-size: 22px">
          番名:
          <span style="color: var(--el-color-primary)">
                  {{ videoInfo.opusName }}
                </span>
        </p>
        <div style="display: flex">
          <div>
            <p style="font-size: 14px">
              原名:
              <span style="color: #909399">
                  {{ videoInfo.opusNameOriginal }}
                </span>
            </p>
            <p style="font-size: 14px; max-width: 220px">
              简介:
              <el-scrollbar max-height="146">
                <span style="color: #747474;" v-html="videoInfo.opusSummary"></span>
              </el-scrollbar>
            </p>
          </div>
          <el-divider direction="vertical" style="opacity: 0"></el-divider>
          <div>
            <p style="font-size: 14px">
              标签：
              <el-tag effect="dark" v-for="tag in videoInfo.aniTagList" style="margin: 0 3px 3px 0;">
                {{ tag.tagName ? tag.tagName : '...' }}
              </el-tag>
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
            <p style="font-size: 14px">
              推荐用户：
              <el-tag v-for="username in videoInfo.shareUserList" style="margin: 0 3px 3px 0;" type="info">
                {{username }}
              </el-tag>
            </p>
          </div>
        </div>
      </div>
    </div>
  </el-space>
</template>

<script setup lang="ts">
import {onMounted, ref} from "vue";
import {reqCommonFeedback} from "@/api/ApiFeedback";
import {sharesList} from "@/api/anime/ani-user-opus-api";

const shares = ref<any[]>([]);
const sharesLoading = ref<boolean>(false);

onMounted(() => {
  loadShares();
});

const loadShares = () => {
  sharesLoading.value = true;
  reqCommonFeedback(sharesList(150), (data: any) => {
    shares.value = data;
    sharesLoading.value = false;
  });
}

const onOpenDetail = (url: string) => {
  window.open(`https://bgm.tv${url}`, "_blank");
};
</script>

<style src="./AnimeShare.css" scoped></style>