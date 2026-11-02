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

    <!-- 扫描状态 -->
    <el-row style="margin-top: 1em">
      <el-col>
        <el-card shadow="never">
          <el-descriptions title="扫描状态" direction="vertical" :column="5" border>
            <el-descriptions-item label="状态">
              {{ scan.running ? '扫描中' : '空闲' }}
            </el-descriptions-item>
            <el-descriptions-item label="开始时间">
              {{ scan.startedAt || '—' }}
            </el-descriptions-item>
            <el-descriptions-item label="进度">
              {{ scan.done }} / {{ scan.total }}
            </el-descriptions-item>
            <el-descriptions-item label="失败">
              {{ scan.failed }}
            </el-descriptions-item>
            <el-descriptions-item label="孤儿目录">
              {{ scan.orphans }}
            </el-descriptions-item>
          </el-descriptions>
          <p v-if="scan.lastResult" class="dash-scan__last">
            上次扫描：{{ scan.lastResult.finishedAt }} · 共 {{ scan.lastResult.opusTotal }} 部，新增 {{ scan.lastResult.added }} / 更新 {{ scan.lastResult.updated }} / 删除 {{ scan.lastResult.removed }}，失败 {{ scan.lastResult.failed }}
          </p>
        </el-card>
      </el-col>
    </el-row>

    <!-- 更新动态 -->
    <el-row style="margin-top: 1em">
      <el-col>
        <el-card shadow="never">
          <template #header>更新动态</template>
          <el-table :data="updates" size="small" empty-text="暂无更新记录">
            <el-table-column prop="opusName" label="作品" min-width="200"/>
            <el-table-column label="集数" width="160">
              <template #default="scope">
                {{ scope.row.displayName || scope.row.episodeKey || '—' }}
              </template>
            </el-table-column>
            <el-table-column label="来源" width="100">
              <template #default="scope">
                {{ sourceCn(scope.row.source) }}
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="时间" width="180"/>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

  </div>
</template>

<script setup lang="ts">
defineOptions({name: "Dashboard"});
import {onMounted, reactive, ref} from "vue";
import {getCount} from "@/api/system/sys-dashboard-api";
import {reqCommonFeedback} from "@/api/ApiFeedback";
import {getRssWorkStatus, getCounts} from "@/api/anime/ani-rss-api.ts";
import {updatePage} from "@/api/anime/ani-update-api";
import {scanStatus} from "@/api/anime/ani-media-api";

// 表单统计
const countList = ref<any[]>([]);
const rssWork = ref<string>('');
const rssCounts = reactive<any>({data: {subscribing: 0, subscriptionCompleted: 0}});
const scan = reactive<any>({running: false, startedAt: '', total: 0, done: 0, failed: 0, orphans: 0, lastResult: null});
const updates = ref<any[]>([]);

onMounted(() => {
  initCount();
  loadRssWorkStatus();
  loadCounts();
  loadScanStatus();
  loadUpdates();
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

const loadScanStatus = () => {
  reqCommonFeedback(scanStatus(), (data: any) => {
    Object.assign(scan, data);
  });
}

const loadUpdates = () => {
  reqCommonFeedback(updatePage({pageNo: 1, pageSize: 10}), (data: any) => {
    updates.value = data.records || [];
  });
}

const sourceCn = (source: number) => ({1: '扫描', 2: 'RSS', 3: '手动上传'}[source] || '—');
</script>

<style scoped>
.dash-scan__last {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--ea-text-3);
}
</style>
