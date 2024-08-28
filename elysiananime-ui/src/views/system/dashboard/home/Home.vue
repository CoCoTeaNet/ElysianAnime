<template>
  <el-row :gutter="3">
    <el-col :span="12">
      <el-card shadow="never">
        <template #header>
          <div><span>变更内容</span></div>
        </template>
        <el-collapse accordion :model-value="0">
          <el-collapse-item v-for="(item,index) in pageVo.records" :title="item.updateNo" :name="index">
            <el-input class="p-view" v-model="item.updateDesc" type="textarea" rows="8"></el-input>
          </el-collapse-item>
        </el-collapse>
      </el-card>
    </el-col>

    <el-col :span="12">
      <el-card shadow="hover">
        <template #header>
          <div><span>简介</span></div>
        </template>
        <h3>SRA后台管理系统</h3>
        <p>
          SRA-ADMIN 是一个前后端分离的后台管理系统，引入了市面上常用的工具包以及核心框架，实现了用户、字典、角色、权限等常见功能， 能够快速搭建一个web项目。
        </p>
        <p>当前版本：{{pageVo.records.length>0 ? pageVo.records[0].updateNo : ''}}</p>
      </el-card>
      <el-card shadow="hover" style="margin-top: 3px">
        <template #header>
          <div><span>系统工作状态</span></div>
        </template>
        <p>RSS：{{rssWork.execMessage}}，最后运行时间：{{rssWork.lastExecTime}}</p>
      </el-card>
      <el-card shadow="hover" style="margin-top: 3px">
        <template #header>
          <div><span>联系信息</span></div>
        </template>
        <p>QQ技术交流群：543112505</p>
        <p>Gitee：<a href="https://gitee.com/momoljw">https://gitee.com/momoljw</a></p>
        <p>GitHub：<a href="https://github.com/momofoolish">https://github.com/momofoolish</a></p>
        <p>博客：<a href="https://live1024.cn">https://live1024.cn</a></p>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import {reqCommonFeedback} from "@/api/ApiFeedback";
import {listByPage} from "@/api/system/sys-version-api";
import {getRssWorkStatus} from "@/api/system/sys-dashboard-api";
import {onMounted, ref} from "vue";

const pageVo = ref<PageVO>({pageNo: 1, pageSize: 50, total: 0, records: []});
const rssWork = ref<any>({execMessage: ''});

onMounted(() => {
  loadTableData();
  loadRssWorkStatus();
})

const loadTableData = (): void => {
  let param = {
    pageNo: 1,
    pageSize: 9999,
    sysVersion: {}
  };
  reqCommonFeedback(listByPage(param), (data: any) => {
    pageVo.value = data;
    pageVo.value.pageSize = data.pageSize;
  });
}

const loadRssWorkStatus = () => {
  reqCommonFeedback(getRssWorkStatus(), (data: any) => {
    rssWork.value = data;
  });
}

</script>

<style scoped src="./css/Home.css"></style>