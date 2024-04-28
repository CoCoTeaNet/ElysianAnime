<template>
  <div style="text-align: center;padding-top: 6em">
    <el-image
        style="width: 600px"
        :src="require('@/assets/svg-source/404.svg')">
    </el-image>
    <div>
      <p>很抱歉！你访问的资源不存在......</p>
      <el-button type="primary" :icon="Back" @click="goHistory">返回上一步</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import {router} from "@/router";
import {Back} from "@element-plus/icons-vue";

/**
 * 返回上一页
 */
const goHistory = () => {
  router.go(-1);
}
</script>

<style scoped></style>