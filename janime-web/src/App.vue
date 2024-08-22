<script setup lang="ts">
import BaseLayout from "@/layout/BaseLayout.vue";
import {setUserInfo} from "./store";

// 读取缓存，以免用户F5刷新登录失效
let userInfo = JSON.parse(`${localStorage.getItem("userInfo")}`);
if (userInfo) {
  setUserInfo(userInfo);
}
</script>

<template>
  <div class="base-layout">
    <el-scrollbar>
      <base-layout/>
    </el-scrollbar>
  </div>
</template>

<style scoped>
.base-layout {
  background-color: var(--el-bg-color-opacity);
  position: absolute;
  width: 100%;
  height: 100%;
}
</style>

<style>
div, h2, html, body {
  margin: 0;
  padding: 0;
  font-family: Noto Sans, Noto Sans HK, Noto Sans JP, Noto Sans KR, Noto Sans SC, Noto Sans TC, sans-serif;
}

.app-background {
  width: 100%;
  height: 100%;
  position: fixed;
}

p {
  font-weight: 500;
}
</style>
