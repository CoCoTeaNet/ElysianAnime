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

    <!-- cpu使用情况 -->
    <el-row style="margin-top: 1em">
      <el-col>
        <el-card shadow="never">
          <el-descriptions title="CPU使用情况" direction="vertical" :column="4" border>
            <el-descriptions-item label="CPU核心数">
              {{ systemInfo.data.cpuCount }}
            </el-descriptions-item>
            <el-descriptions-item label="CPU系统使用率">
              {{ systemInfo.data.cpuSystemUsed }}%
            </el-descriptions-item>
            <el-descriptions-item label="CPU用户使用率">
              {{ systemInfo.data.cpuUserUsed }}%
            </el-descriptions-item>
            <el-descriptions-item label="CPU空闲率">
              {{ systemInfo.data.cpuFree }}%
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <!-- 系统信息 -->
    <el-row style="margin-top: 1em">
      <el-col>
        <el-card shadow="never">
          <el-descriptions title="系统信息" direction="vertical" :column="4" border>
            <el-descriptions-item label="操作系统">{{ systemInfo.data.os }}</el-descriptions-item>
            <el-descriptions-item label="服务器名">{{ systemInfo.data.serverName }}</el-descriptions-item>
            <el-descriptions-item label="服务器IP">{{ systemInfo.data.serverIp }}</el-descriptions-item>
            <el-descriptions-item label="系统架构">{{ systemInfo.data.serverArchitecture }}</el-descriptions-item>

            <el-descriptions-item label="Java名称">{{ systemInfo.data.javaName }}</el-descriptions-item>
            <el-descriptions-item label="Java版本">{{ systemInfo.data.javaVersion }}</el-descriptions-item>
            <el-descriptions-item label="安装路径">{{ systemInfo.data.javaPath }}</el-descriptions-item>
            <el-descriptions-item label="项目路径">{{ systemInfo.data.projectPath }}</el-descriptions-item>
            <el-descriptions-item label="服务器运行时长">
              {{ unitUtil.timeCalculate(systemInfo.data.runningTime) }}
            </el-descriptions-item>

            <el-descriptions-item label="总内存">
              {{ unitUtil.memoryCalculate(systemInfo.data.memoryTotalSize) }}
            </el-descriptions-item>
            <el-descriptions-item label="可用内存">
              {{ unitUtil.memoryCalculate(systemInfo.data.memoryAvailableSize) }}
            </el-descriptions-item>
            <el-descriptions-item label="已用内存">
              {{ unitUtil.memoryCalculate(systemInfo.data.memoryTotalSize - systemInfo.data.memoryAvailableSize) }}
            </el-descriptions-item>

            <el-descriptions-item label="磁盘总大小">
              {{ unitUtil.memoryCalculate(systemInfo.data.diskTotalSize) }}
            </el-descriptions-item>
            <el-descriptions-item label="可用空间">
              {{ unitUtil.memoryCalculate(systemInfo.data.diskFreeSize) }}
            </el-descriptions-item>
            <el-descriptions-item label="已用空间">
              {{ unitUtil.memoryCalculate(systemInfo.data.diskTotalSize - systemInfo.data.diskFreeSize) }}
            </el-descriptions-item>
            <el-descriptions-item label="盘符路径">{{ systemInfo.data.diskPath }}</el-descriptions-item>
            <el-descriptions-item label="磁盘分隔符">{{ systemInfo.data.diskSeparator }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import {onMounted, reactive, ref} from "vue";
import {getSystemInfo, getCount} from "@/api/system/sys-dashboard-api";
import {reqCommonFeedback} from "@/api/ApiFeedback";
import unitUtil from "@/utils/unit-util";
import {getRssWorkStatus, getCounts} from "@/api/anime/ani-rss-api.ts";

// 系统信息
const systemInfo = reactive<any>({data: {cpuCount:0, cpuSystemUsed:0, cpuUserUsed:0, cpuFree:0}});
// 表单统计
const countList = ref<any[]>([]);
const rssWork = ref<string>('');
const rssCounts = reactive<any>({data: {subscribing: 0, subscriptionCompleted: 0}});

onMounted(() => {
  initCount();
  initSystemInfo();
  loadRssWorkStatus();
  loadCounts();
})

/**
 * 初始化系统信息
 */
const initSystemInfo = () => {
  reqCommonFeedback(getSystemInfo(), (systemModel: SystemModel) => {
    systemInfo.data = systemModel;
  });
}

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
