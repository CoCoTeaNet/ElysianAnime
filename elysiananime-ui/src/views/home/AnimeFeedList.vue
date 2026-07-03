<template>
  <div>
    <el-tabs v-model="activeTab" @tab-change="loadData">
      <el-tab-pane label="收到的投喂" name="received">
        <template #default>
          <el-row v-loading="loading" :gutter="12">
            <el-col :span="12" v-for="item in pageVo.records" :key="item.id" style="margin-bottom: 12px">
              <el-card shadow="hover" @click="toPlayerView(item)">
                <el-space>
                  <el-space style="width: 200px" direction="vertical" alignment="center">
                    <el-image :src="`api/anime/opus/cover?resName=${item.coverUrl}`" :alt="item.nameCn"
                      style="width: 200px" />
                  </el-space>
                  <div style="margin-left: 1em">
                    <p style="font-weight: 600; font-size: 22px">
                      番名:
                      <span style="color: var(--el-color-primary)">{{ item.nameCn }}</span>
                    </p>
                    <p style="font-size: 14px; color: #666; margin-top: 8px">
                      投喂者: {{ item.fromUserNickname }}
                    </p>
                    <p style="font-size: 14px; color: #999">
                      {{ formatTime(item.createTime) }}
                    </p>
                    <div style="margin-top: 12px">
                      <el-button type="danger" size="small" @click.stop="onDelete(item.id)">删除</el-button>
                    </div>
                  </div>
                </el-space>
              </el-card>
            </el-col>
            <el-empty v-if="!loading && pageVo.records.length === 0" description="暂无数据" />
          </el-row>
        </template>
      </el-tab-pane>
      <el-tab-pane label="发出的投喂" name="sent">
        <template #default>
          <el-row v-loading="loading" :gutter="12">
            <el-col :span="12" v-for="item in pageVo.records" :key="item.id" style="margin-bottom: 12px">
              <el-card shadow="hover" @click="toPlayerView(item)">
                <el-space>
                  <el-space style="width: 200px" direction="vertical" alignment="center">
                    <el-image :src="`api/anime/opus/cover?resName=${item.coverUrl}`" :alt="item.nameCn"
                      style="width: 200px" />
                  </el-space>
                  <div style="margin-left: 1em">
                    <p style="font-weight: 600; font-size: 22px">
                      番名:
                      <span style="color: var(--el-color-primary)">{{ item.nameCn }}</span>
                    </p>
                    <p style="font-size: 14px; color: #666; margin-top: 8px">
                      投喂给: {{ item.fromUserNickname }}
                    </p>
                    <p style="font-size: 14px; color: #999">
                      {{ formatTime(item.createTime) }}
                    </p>
                    <div style="margin-top: 12px">
                      <el-button type="danger" size="small" @click.stop="onDelete(item.id)">删除</el-button>
                    </div>
                  </div>
                </el-space>
              </el-card>
            </el-col>
            <el-empty v-if="!loading && pageVo.records.length === 0" description="暂无数据" />
          </el-row>
        </template>
      </el-tab-pane>
    </el-tabs>

    <div class="pagination-wrap" v-if="pageVo.total > 0">
      <el-pagination background
                      layout="total, sizes, prev, pager, next, jumper"
                      :total="pageVo.total"
                      :page-size="pageVo.pageSize"
                      :page-sizes="[10, 20, 30]"
                      @current-change="onPageChange"
                      @size-change="onSizeChange"/>
    </div>
  </div>
</template>

<script setup lang="ts">
import {onMounted, ref} from "vue";
import {reqCommonFeedback, reqSuccessFeedback} from "@/api/ApiFeedback";
import {feedListByUser, feedListSent, deleteFeed} from "@/api/anime/ani-feed-api";
import {useRouter} from "vue-router";

const router = useRouter();
const activeTab = ref<string>('received');
const loading = ref<boolean>(false);
const pageParam = ref<any>({
  pageNo: 1,
  pageSize: 10,
});
const pageVo = ref<any>({pageNo: 1, pageSize: 10, total: 0, records: []});

onMounted(() => {
  loadData();
});

const loadData = () => {
  loading.value = true;
  const apiFn = activeTab.value === 'received' ? feedListByUser : feedListSent;
  reqCommonFeedback(apiFn(pageParam.value), (data: any) => {
    pageVo.value = data;
    loading.value = false;
  });
};

const onPageChange = (currentPage: number) => {
  pageParam.value.pageNo = currentPage;
  loadData();
};

const onSizeChange = (size: number) => {
  pageParam.value.pageSize = size;
  loadData();
};

const onDelete = (id: string) => {
  reqSuccessFeedback(deleteFeed(id), '删除成功', () => {
    loadData();
  });
};

const toPlayerView = (item: any) => {
  router.push({
    name: "AnimeVideo",
    params: {
      id: item.opusId,
      num: 1,
      time: 0
    }
  });
};

const formatTime = (time: string) => {
  if (!time) return '';
  return time.replace('T', ' ').substring(0, 19);
};
</script>

<style scoped>
.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}
</style>
