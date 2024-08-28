<template>
  <el-container>
    <el-header><anime-header/></el-header>
    <el-main :class="`a-layout ${store.state.isFullScreen ? 'a-layout-full' : 'a-layout-full-close'}`">
      <router-view v-slot="{Component}">
        <keep-alive>
          <transition :name="`slide-fade`" :mode="`out-in`">
            <component :is="Component"/>
          </transition>
        </keep-alive>
      </router-view>
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import AnimeHeader from "@/layout/modules/AnimeHeader.vue";
import {setUserInfo, store} from "@/store";
import {onMounted} from "vue";
import {reqCommonFeedback} from "@/api/ApiFeedback";
import {loginInfo} from "@/api/system/sys-login-api";

onMounted(() => {
  reqCommonFeedback(loginInfo(), (data:any) => setUserInfo(data));
});
</script>

<style scoped>
.el-main {
  box-sizing: border-box;
  position: relative;
  margin: 0 auto;
}

.a-layout-full-close {
  width: 86%;
}

.a-layout-full {
  width: 100%;
}

/* 可以设置不同的进入和离开动画 */
/* 设置持续时间和动画函数 */
.slide-fade-enter-active {
    transition: all .6s ease;
}

.slide-fade-leave-active {
    transition: all .3s cubic-bezier(0.42, 0, 0.58, 1);
}

.slide-fade-enter, .slide-fade-leave-to {
    transform: translateX(10px);
    opacity: 0;
}

@media screen and (max-width: 1439px) {
    .el-main {
        width: 100%;
    }
}
</style>