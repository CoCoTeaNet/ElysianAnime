<template>
  <div>
    <el-button type="primary" :icon="Refresh" @click="loadShares">换一换</el-button>
    <el-row v-loading="sharesLoading" :gutter="12">
      <el-col :span="12" v-for="videoInfo in shares" class="a-video-info-card" :key="videoInfo.id">
        <el-card shadow="hover" @click="toPlayerView(videoInfo)">
          <el-space>
            <el-space style="width: 200px" direction="vertical" alignment="center">
              <el-image :src="`api/anime/opus/cover?resName=${videoInfo.coverUrl}`" :alt="videoInfo.opusName"
                style="width: 200px" />
            </el-space>
            <div style="margin-left: 1em">
              <p style="font-weight: 600; font-size: 22px">
                番名:
                <span style="color: var(--el-color-primary)">
                  {{ videoInfo.opusName }}
                </span>
              </p>
              <div style="display: flex">
                <el-divider direction="vertical" style="opacity: 0"></el-divider>
                <div>
                  <p style="font-size: 14px;max-height: 100px;overflow-y: scroll;">
                    标签：
                    <el-tag effect="dark" v-for="tag in videoInfo.aniTagList" :key="tag.id"
                      style="margin: 0 3px 3px 0;">
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
                    <el-text class="mx-1" type="info">
                      {{ videoInfo.launchStart ? videoInfo.launchStart : '-' }}
                    </el-text>
                  </p>
                  <p style="font-size: 14px">
                    放送星期：
                    <el-text class="mx-1" type="info">{{ videoInfo.deliveryWeek ? videoInfo.deliveryWeek : '-'
                      }}</el-text>
                  </p>
                  <p style="font-size: 14px">
                    推荐用户：
                    <el-tag v-for="(username, index) in videoInfo.shareUserList" :key="index"
                      style="margin: 0 3px 3px 0;" type="info">
                      {{ username }}
                    </el-tag>
                  </p>
                </div>
              </div>
            </div>
          </el-space>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { reqCommonFeedback } from "@/api/ApiFeedback";
import { sharesList } from "@/api/anime/ani-user-opus-api";
import { useRouter } from "vue-router";
import { Refresh } from "@element-plus/icons-vue";

const shares = ref<any[]>([]);
const sharesLoading = ref<boolean>(false);
const router = useRouter();

onMounted(() => {
  loadShares();
});

const loadShares = () => {
  sharesLoading.value = true;
  reqCommonFeedback(sharesList(30), (data: any) => {
    shares.value = data;
    sharesLoading.value = false;
  });
}

const onOpenDetail = (url: string) => {
  window.open(`https://bgm.tv${url}`, "_blank");
};

const toPlayerView = (anime: any) => {
  router.push({
    name: "AnimeVideo",
    params: {
      id: anime.opusId,
      num: 0,
      time: 0
    }
  });
};
</script>

<style src="./AnimeShare.css" scoped></style>