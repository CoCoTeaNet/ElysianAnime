<template>
  <div class="bg">
    <!-- 综合统计 -->
    <el-row :gutter="12">
      <el-col :span="6" v-for="item in countList" :key="item.title">
        <el-card shadow="never" body-style>
          <div style="display: flex;flex-direction: column;align-items: center">
            <h3>{{ item.title }}</h3>
            <span>{{ item.count }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- RSS情况 -->
    <el-row style="margin-top: 1em">
      <el-col>
        <el-card shadow="never">
          <el-descriptions title="RSS情况" direction="vertical" :column="4" border>
            <el-descriptions-item label="最后执行完成时间">
              {{ rssWork.lastExecTime }}
            </el-descriptions-item>
            <el-descriptions-item label="执行消息">
              {{ rssWork.execMessage }}
            </el-descriptions-item>
            <el-descriptions-item label="订阅中数量">
              {{ rssCounts.data.subscribing }}
            </el-descriptions-item>
            <el-descriptions-item label="已完成订阅数量">
              {{ rssCounts.data.subscriptionCompleted }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

  </div>
</template>

<script setup lang="ts">
import {onMounted, reactive, ref} from "vue";
import {getCount} from "@/api/system/sys-dashboard-api";
import {reqCommonFeedback} from "@/api/ApiFeedback";
import {getRssWorkStatus, getCounts} from "@/api/anime/ani-rss-api.ts";

// 表单统计
const countList = ref<any[]>([]);
const rssWork = ref<string>('');
const rssCounts = reactive<any>({data: {subscribing: 0, subscriptionCompleted: 0}});

onMounted(() => {
  initCount();
  loadRssWorkStatus();
  loadCounts();
})

/**
 * 数据统计
 */
const initCount = () => {
  reqCommonFeedback(getCount(), (data: any) => {
    countList.value = data;
  });
}

const loadRssWorkStatus = () => {
  reqCommonFeedback(getRssWorkStatus(), (data: any) => {
    rssWork.value = data;
  });
}

const loadCounts = () => {
  reqCommonFeedback(getCounts(), (data: any) => {
    rssCounts.data = data;
  });
}
</script>

<style scoped>

</style>
