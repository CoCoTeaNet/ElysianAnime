<template>
  <card-box>
    <el-row :gutter="24" v-loading="loading" style="height: 100%;margin-left: 0;margin-right: 0;">
      <!--左侧-->
      <el-col :span="19" style="height: 100%">
        <!-- 卡片列表 -->
        <div class="a-home-container">
          <div v-for="anime in pageVo.records" :key="anime.id" class="a-h-card" :class="{ 'downloaded': anime.hasResource }">
            <div class="a-h-card-top" @click="toPlayerView(anime)">
              
              <!-- 原本右上角的吧唧标签，效果不好已取消，下载资源数量 -->
              <!-- <div class="a-h-card-downloads">{{formatUtil.fillZero(anime.downloadNum)}}</div> -->
              <!-- 已更新至第几集 / 全多少集   -->
              <div class="a-h-card-top-upper-info">
                <div class='playinfo' style="font-size: 0.7rem; flex-grow:4;">
                  {{ `${anime.downloadNum} / ${anime.episodes}` }}{{ anime.downloadNum >= anime.episodes ? ' (已完结)' : ''   }}
                </div>
              </div>
              <!-- 最新观看集数的观看进度条 -->
              <div class="a-h-card-top-progress" :style="{width: `${getProgressPercent(anime) *100}%`}"></div>
              <!--封面-->
              <div class="a-h-card-lazy-load"
                   :style="`background-image: url('api/anime/opus/cover?resName=${anime.coverUrl}');`"
              ></div>
              <div class="a-h-card-top-upper">
                <div class="a-h-card-top-upper-hover">
                  <div style="width: 100%; height: 100%; position: relative;">
                    <el-icon class="playbtn" v-show="anime.hasResource && !isMobile" :size="48" color="#eee">
                      <VideoPlay/>
                    </el-icon>
                    <el-button class="likebtn" style="width: 100%;" :alt="'开始追番'"
                               :type="anime.userId ? 'danger' : 'primary'" size='small' :disabled="!!anime.userId"
                               @click="onFollowOpus(anime.id)">
                      {{ anime.userId ? '已追番' : '追番' }}
                    </el-button>
                  </div>
                </div>
                <div class="tags" style="flex-grow: 1; display: flex;  justify-content: end;"></div>
              </div>
            </div>
            <el-row :gutter="8" style="margin: 4px 0;" class="a-h-card-bottom">
              <el-col :span="24" class="a-tcb-infogroup" @click="onOpenDetail(anime.detailInfoUrl)">
                <p class="a-tcb-info-name"
                   :style="anime.hasResource ? 'color: #F56C6C' : ''"
                   :title="anime.nameCn">
                  {{ anime.nameCn }}
                </p>
                <!-- 效果例如：已观看至第03集·每周三更新 -->
                <p style="font-size: 0.6rem; padding-top: 2px; color: #999;" @click="onOpenDetail(anime.detailInfoUrl)">
                  {{ anime.readingNum > 0 ? `已观看至第${formatUtil.fillZero(anime.readingNum)}集` : '从未观看' }} · {{isOverSeason(anime.launchStart) ? `每${anime.deliveryWeek}更新`: `${formatDateMonth(anime.launchStart)}`}}
                </p>
              </el-col>
            </el-row>
          </div>
        </div>
      </el-col>

      <el-col :span="5">
        <h2 class="a-home-title">番剧检索</h2>
        <el-form>
          <!--关键词搜索-->
          <el-form-item>
            <el-input placeholder="番剧名搜索~"
                      size="large"
                      v-model="pageParam.searchKey"
                      clearable
                      @keyup.enter="onSearch"
            >
              <template #append>
                <el-button type="primary" :icon="Search" @click="onSearch"/>
              </template>
            </el-input>
          </el-form-item>
          <!--筛选-->
          <el-form-item>
            <MultSelection @on-multiple-conditions-change="onMultipleConditionsChange"/>
          </el-form-item>
        </el-form>
        <!--通过URL抓取作品信息-->
        <el-row type="flex" justify="start" style="margin: 1em 0 0 0">
          <el-button link type="primary" @click="addAcgOpusDialog = true">没找到番剧？点我提交</el-button>
        </el-row>
        <el-dialog v-model="addAcgOpusDialog">
          <el-form>
            <el-form-item label="Bangumi详细链接：">
              <el-input v-model="bgmUrl" placeholder="https://bgm.tv/subject/389772"/>
              <el-link type="primary" href="https://bgm.tv">去Bangumi查找番剧信息~~~</el-link>
            </el-form-item>
          </el-form>
          <template #footer>
            <span class="dialog-footer">
              <el-button @click="closeAddAcgOpusDialog">取 消</el-button>
              <el-button :loading="addAcgOpusLoading" type="warning" @click="onAddAcgOpus(1)">重 刷</el-button>
              <el-button :loading="addAcgOpusLoading" type="primary" @click="onAddAcgOpus(0)">新 增</el-button>
            </span>
          </template>
        </el-dialog>
      </el-col>
    </el-row>

    <template #page>
      <!-- 分页 -->
      <el-pagination background layout="total, sizes, prev, pager, next, jumper"
                     :total="pageVo.total"
                     :page-size="pageVo.pageSize"
                     :page-sizes="[20, 30, 40]"
                     @current-change="onPageChange"
                     @size-change="onSizeChange"/>
    </template>
  </card-box>
</template>

<script lang="ts" setup>
import {nextTick, onMounted, ref, watch} from "vue";
import {addAcgOpusByBgmUrl, listByUser} from "@/api/anime/ani-opus-api";
import userOpusApi from "@/api/anime/ani-user-opus-api";
import {reqCommonFeedback} from "@/api/ApiFeedback";
import {Search, VideoPlay} from "@element-plus/icons-vue";
import {useRoute, useRouter} from "vue-router";
import {ElMessage} from 'element-plus'
import MultSelection from "@/views/home/modules/MultipleConditionsSearch.vue";
import CardBox from "@/components/container/CardBox.vue";
import formatUtil from "@/utils/format-util.ts";

const route = useRoute();
const router = useRouter();

const isMobile = !!navigator.userAgent.match(/AppleWebKit.*Mobile.*/);

const pageParam = ref<any>({
  pageNo: 1,
  pageSize: 40,
  states: [],
  years: [],
  months: [],
  status: [],
  searchKey: "",
  hasResource: 1,
});
const pageVo = ref<PageVO>({pageNo: 1, pageSize: 0, total: 0, records: []});
const loading = ref<boolean>(true);
const addAcgOpusDialog = ref<boolean>(false);
const addAcgOpusLoading = ref<boolean>(false);
const bgmUrl = ref<string>('');

onMounted(() => {
  if (route.query.searchKey) {
    pageParam.value.searchKey = route.query.searchKey + '';
  }
  if (route.query.hasResource) {
    pageParam.value.hasResource = route.query.hasResource;
  }
  loadTableData();
});

watch(
    () => router.currentRoute.value,
    (newVal) => {
      if (newVal.query.searchKey) {
        pageParam.value.searchKey = newVal.query.searchKey + '';
      }
      if (newVal.query.hasResource) {
        pageParam.value.hasResource = newVal.query.hasResource;
      }
      loadTableData();
    }
);

const isOverSeason = (animeLaunchTime: string) => {
  // 是否过季，过季番显示到月份
  return formatUtil.strToDate(animeLaunchTime) < new Date(Date.now()-7776000000)  // 7776000000 = 90天
}

const formatDateMonth = (animeLaunchTime: string) => {
  return formatUtil.formatDate(formatUtil.strToDate(animeLaunchTime), 'YYYY/MM') 
}

const onSearch = () => {
  router.push({
    name: 'AnimeHome',
    query: {
      'searchKey': pageParam.value.searchKey,
      'hasResource': pageParam.value.hasResource
    }
  });
}

const loadTableData = (): void => {
  if (!loading.value) loading.value = true;
  if (pageParam.value.hasResource == -1) {
    pageParam.value.hasResource = null;
  }
  reqCommonFeedback(listByUser(pageParam.value), (data: any) => {
    pageVo.value.records = data.records;
    pageVo.value.total = data.total;
    pageVo.value.pageSize = data.pageSize;
    loading.value = false;
  });
  // 1分钟内如果还是无法有数据就将停止loading
  setTimeout(() => {
    if (loading.value) {
      loading.value = false;
    }
  }, 60000);
};

const onPageChange = (currentPage: number) => {
  pageParam.value.pageNo = currentPage;
  nextTick(() => loadTableData());
};

const onSizeChange = (size: number) => {
  pageParam.value.pageSize = size;
  nextTick(() => loadTableData());
};

const onOpenDetail = (url: string) => {
  window.open(`https://bgm.tv${url}`, "_blank");
};

const onFollowOpus = (id: string) => {
  loading.value = true;
  reqCommonFeedback(userOpusApi.follow(id), () => {
    loadTableData();
  });
};

const toPlayerView = (anime: any) => {
  if (anime.hasResource) {
    router.push({
          name: "AnimeVideo",
          params: {
            id: anime.id,
            num: anime.readingNum === 0 ? 1 : anime.readingNum,
            time: anime.readingTime
          }
        }
    );
  }
};

const onAddAcgOpus = (isCover: number) => {
  addAcgOpusLoading.value = true;
  addAcgOpusByBgmUrl({bgmUrl: bgmUrl.value, isCover: isCover})
      .then((data) => {
        if (data.code === 200) {
          ElMessage.success({message: '提交成功'});
        } else {
          ElMessage.error({message: data.message});
        }
      })
      .catch((err) => {
        console.log(err);
      })
      .finally(() => {
        closeAddAcgOpusDialog();
      });
}

const closeAddAcgOpusDialog = () => {
  addAcgOpusDialog.value = false;
  addAcgOpusLoading.value = false;
}

const onMultipleConditionsChange = (searchObj: any) => {
  if (searchObj.readStatus) {
    pageParam.value.status = searchObj.readStatus;
  }
  if (searchObj.month) {
    pageParam.value.months = searchObj.month;
  }
  if (searchObj.state) {
    pageParam.value.states = searchObj.state;
  }
  if (searchObj.year) {
    pageParam.value.years = searchObj.year;
  }
  if (searchObj.hasResource) {
    pageParam.value.hasResource = searchObj.hasResource;
  }
  loadTableData();
}

const getProgressPercent = (anime: any) => {
  let percent = 0;
  let readingTime = parseInt(anime.readingTime);
  let totalTime = parseInt(anime.totalTime);

  if (readingTime > totalTime) {
    percent = 0;
  } else if (readingTime > 0) {
    percent = readingTime / totalTime;
    if (percent > 1) {
      percent = 0;
    } else if (percent < 0) {
      percent = 0;
    }
  }

  console.debug(`${anime.nameCn} >>> readingTime:${anime.readingTime},totalTime:${anime.totalTime}, percent:${percent}`);
  return percent;
}
</script>

<style src="./AdminHome.css"></style>
