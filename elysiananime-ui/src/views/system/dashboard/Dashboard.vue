<template>
  <div class="ea-dash">
    <header class="ea-dash__head">
      <h1 class="ea-dash__title">仪表盘</h1>
      <p class="ea-dash__subtitle">系统与媒体库运行概览</p>
    </header>

    <section class="ea-dash__stats">
      <div v-for="item in countList" :key="item.title" class="ea-stat">
        <span class="ea-stat__label">{{ item.title }}</span>
        <strong class="ea-stat__value">{{ item.count }}</strong>
      </div>
    </section>

    <div class="ea-dash__row">
      <section class="ea-panel">
        <header class="ea-panel__head">
          <h2>RSS 情况</h2>
        </header>
        <div class="ea-panel__body">
          <div class="ea-kv">
            <div class="ea-kv__item">
              <span class="ea-kv__label">最后执行完成时间</span>
              <span class="ea-kv__value">{{ rssWork.lastExecTime || '—' }}</span>
            </div>
            <div class="ea-kv__item">
              <span class="ea-kv__label">执行消息</span>
              <span class="ea-kv__value">{{ rssWork.execMessage || '—' }}</span>
            </div>
            <div class="ea-kv__item">
              <span class="ea-kv__label">订阅中数量</span>
              <span class="ea-kv__value">{{ rssCounts.data.subscribing ?? 0 }}</span>
            </div>
            <div class="ea-kv__item">
              <span class="ea-kv__label">已完成订阅数量</span>
              <span class="ea-kv__value">{{ rssCounts.data.subscriptionCompleted ?? 0 }}</span>
            </div>
          </div>
        </div>
      </section>

      <section class="ea-panel">
        <header class="ea-panel__head">
          <h2>扫描状态</h2>
          <span class="ea-pill" :class="scan.running ? 'is-running' : 'is-idle'">
            {{ scan.running ? '扫描中' : '空闲' }}
          </span>
        </header>
        <div class="ea-panel__body">
          <el-progress
              v-if="scan.total > 0"
              :percentage="scanPercent"
              :show-text="false"
              :stroke-width="6"
              color="var(--ea-color-primary)"
          />
          <div class="ea-kv">
            <div class="ea-kv__item">
              <span class="ea-kv__label">开始时间</span>
              <span class="ea-kv__value">{{ scan.startedAt || '—' }}</span>
            </div>
            <div class="ea-kv__item">
              <span class="ea-kv__label">进度</span>
              <span class="ea-kv__value">{{ scan.done }} / {{ scan.total }}</span>
            </div>
            <div class="ea-kv__item">
              <span class="ea-kv__label">失败</span>
              <span class="ea-kv__value" :class="{'is-warn': scan.failed > 0}">{{ scan.failed }}</span>
            </div>
            <div class="ea-kv__item">
              <span class="ea-kv__label">孤儿目录</span>
              <span class="ea-kv__value" :class="{'is-warn': scan.orphans > 0}">{{ scan.orphans }}</span>
            </div>
          </div>
          <p v-if="scan.lastResult" class="ea-panel__note">
            上次扫描：{{ scan.lastResult.finishedAt }} · 共 {{ scan.lastResult.opusTotal }} 部，新增 {{ scan.lastResult.added }} / 更新 {{ scan.lastResult.updated }} / 删除 {{ scan.lastResult.removed }}，失败 {{ scan.lastResult.failed }}
          </p>
        </div>
      </section>
    </div>

    <section class="ea-panel">
      <header class="ea-panel__head">
        <h2>更新动态</h2>
      </header>
      <el-table :data="updates" size="small" empty-text="暂无更新记录" class="ea-table">
        <el-table-column prop="opusName" label="作品" min-width="220"/>
        <el-table-column label="集数" min-width="160">
          <template #default="scope">
            {{ scope.row.displayName || scope.row.episodeKey || '—' }}
          </template>
        </el-table-column>
        <el-table-column label="来源" width="120">
          <template #default="scope">
            {{ sourceCn(scope.row.source) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="200"/>
      </el-table>
    </section>
  </div>
</template>

<script setup lang="ts">
defineOptions({name: "Dashboard"});
import {computed, onMounted, reactive, ref} from "vue";
import {getCount} from "@/api/system/sys-dashboard-api";
import {reqCommonFeedback} from "@/api/ApiFeedback";
import {getRssWorkStatus, getCounts} from "@/api/anime/ani-rss-api.ts";
import {updatePage} from "@/api/anime/ani-update-api";
import {scanStatus} from "@/api/anime/ani-media-api";

// 表单统计
const countList = ref<any[]>([]);
const rssWork = ref<any>({});
const rssCounts = reactive<any>({data: {subscribing: 0, subscriptionCompleted: 0}});
const scan = reactive<any>({running: false, startedAt: '', total: 0, done: 0, failed: 0, orphans: 0, lastResult: null});
const updates = ref<any[]>([]);

const scanPercent = computed(() => {
  const total = Number(scan.total) || 0;
  if (total <= 0) return 0;
  return Math.min(Math.round((Number(scan.done) || 0) / total * 100), 100);
});

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
.ea-dash {
  display: flex;
  flex-direction: column;
  gap: var(--ea-space-4);
  max-width: 1560px;
  margin: 0 auto;
}

.ea-dash__head {
  margin-bottom: var(--ea-space-1);
}

.ea-dash__title {
  margin: 0;
  font-size: 22px;
  color: var(--ea-text-1);
}

.ea-dash__subtitle {
  margin: var(--ea-space-1) 0 0;
  font-size: 13px;
  color: var(--ea-text-3);
}

.ea-dash__stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--ea-space-3);
}

.ea-stat {
  padding: var(--ea-space-4) var(--ea-space-5);
  background: var(--ea-surface);
  border: 1px solid var(--ea-border);
  border-radius: var(--ea-radius-md);
}

.ea-stat__label {
  display: block;
  font-size: 13px;
  color: var(--ea-text-2);
}

.ea-stat__value {
  display: block;
  margin-top: var(--ea-space-2);
  font-size: 30px;
  line-height: 1.1;
  font-weight: 600;
  color: var(--ea-text-1);
}

.ea-dash__row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--ea-space-3);
  align-items: start;
}

.ea-panel {
  background: var(--ea-surface);
  border: 1px solid var(--ea-border);
  border-radius: var(--ea-radius-md);
  overflow: hidden;
}

.ea-panel__head {
  display: flex;
  align-items: center;
  gap: var(--ea-space-2);
  padding: var(--ea-space-3) var(--ea-space-5);
  border-bottom: 1px solid var(--ea-border);
}

.ea-panel__head h2 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--ea-text-1);
}

.ea-panel__body {
  padding: var(--ea-space-4) var(--ea-space-5) var(--ea-space-5);
}

.ea-panel__note {
  margin: var(--ea-space-3) 0 0;
  font-size: 12px;
  color: var(--ea-text-3);
}

.ea-kv {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--ea-space-3) var(--ea-space-4);
}

.ea-kv__item {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.ea-kv__label {
  font-size: 12px;
  color: var(--ea-text-3);
}

.ea-kv__value {
  font-size: 14px;
  color: var(--ea-text-1);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ea-kv__value.is-warn {
  color: var(--ea-accent-update);
}

.ea-pill {
  padding: 1px 8px;
  border-radius: 999px;
  font-size: 12px;
  line-height: 1.6;
}

.ea-pill.is-running {
  background: var(--ea-color-primary);
  color: #fff;
}

.ea-pill.is-idle {
  background: var(--ea-border);
  color: var(--ea-text-2);
}

.ea-panel__body :deep(.el-progress) {
  margin-bottom: var(--ea-space-4);
}

.ea-table {
  width: 100%;
}

@media (max-width: 1200px) {
  .ea-dash__stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .ea-dash__row {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 768px) {
  .ea-dash__stats {
    grid-template-columns: minmax(0, 1fr);
  }

  .ea-kv {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
