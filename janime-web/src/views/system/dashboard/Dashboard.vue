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

    <!-- cpu使用情况 -->
    <el-row style="margin-top: 1em">
      <el-col>
        <el-card shadow="never">
          <div id="dashboardPieId" style="height: 300px"></div>
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
import * as echarts from "echarts";
import {nextTick, onMounted, reactive, ref} from "vue";
import {getSystemInfo, getCount} from "@/api/system/sys-dashboard-api";
import {reqCommonFeedback} from "@/api/ApiFeedback";
import unitUtil from "@/utils/unit-util";

// 系统信息
const systemInfo = reactive<any>({data: {}});

// 表单统计
const countList = ref<any[]>([]);

onMounted(() => {
  initCount();
  initSystemInfo();
})

/**
 * 初始化系统信息
 */
const initSystemInfo = () => {
  reqCommonFeedback(getSystemInfo(), (systemModel: SystemModel) => {
    systemInfo.data = systemModel;
    nextTick(() => initPie(systemModel));
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

/**
 * 初始化饼状图
 * @param systemModel
 */
const initPie = (systemModel: SystemModel): void => {
  let htmlElement = document.getElementById("dashboardPieId") as HTMLElement;
  if (htmlElement) {
    let myChart = echarts.getInstanceByDom(htmlElement);
    if (myChart === undefined) {
      myChart = echarts.init(htmlElement);
    }
    let option = {
      title: {text: 'CPU使用情况', subtext: `CPU核心数: ${systemModel.cpuCount}`, left: 'center'},
      tooltip: {trigger: 'item', formatter: '{b} : {c} %'},
      legend: {orient: 'vertical', left: 'left'},
      series: [
        {
          type: 'pie',
          radius: '50%',
          data: [
            {value: systemModel.cpuSystemUsed, name: 'CPU系统使用率'},
            {value: systemModel.cpuUserUsed, name: 'CPU用户使用率'},
            {value: systemModel.cpuFree, name: 'CPU空闲率'}
          ],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: "rgba(0, 0, 0, 0.5)"
            }
          }
        }
      ]
    };
    option && myChart.setOption(option);
  }
}
</script>

<style scoped>

</style>
