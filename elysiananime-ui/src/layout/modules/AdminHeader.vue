<template>
  <el-row align="middle" class="header-row">
    <el-col :span="20" style="display: flex">
      <el-button link @click="setCollapseMenu">
        <template #icon>
          <el-icon class="mouse-over right-item" :size="iconSize">
            <expand v-if="store.state.isCollapseMenu"/>
            <fold v-else/>
          </el-icon>
        </template>
      </el-button>
      <admin-tab/>
    </el-col>
    <!-- 用户信息 -->
    <el-col :span="4">
      <el-row :gutter="10" justify="end" align="middle">
        <el-icon class="mouse-over" :size="iconSize-2" @click="doFullScreen">
          <full-screen/>
        </el-icon>
        <el-icon class="mouse-over" :size="iconSize-2" @click="clickToGo('Home')">
          <house/>
        </el-icon>
        <el-dropdown>
          <span class="mouse-over">
            <el-avatar v-if="store.state.userInfo.avatar" shape="square" :src="`api/system/file/getAvatar?avatar=${url}`"/>
            <el-avatar v-else shape="square" src="@/assets/svg-source/default-avatar.svg"/>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="clickToGo('UserCenterView')">个人中心</el-dropdown-item>
              <el-dropdown-item divided @click="doLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-row>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import {router} from "@/router";
import {useRoute} from "vue-router";
import {reqCommonFeedback} from "@/api/ApiFeedback";
import {loginInfo, logout} from "@/api/system/sys-login-api";
import {setUserInfo, useStore, setCollapseMenu} from "@/store";
import AdminTab from "@/layout/modules/AdminTab.vue";
import {Expand, Fold, FullScreen, House} from "@element-plus/icons-vue";
import {onMounted, ref} from 'vue';

const store = useStore();
const route = useRoute();

const iconSize = ref<number>(24);

onMounted(() => {
  reqCommonFeedback(loginInfo(), (data:any) => setUserInfo(data));
});

/**
 * 点击跳转
 */
const clickToGo = (name: string) => {
  router.push({name: name});
}

/**
 * 退出登录
 */
const doLogout = () => {
  reqCommonFeedback(logout(), () => {
    setUserInfo({id: '', username: '', nickname: ''});
    router.push({name: 'Login', query: {redirect: route.name ? route.name.toString() : ''}});
  });
}

/**
 * 浏览器全屏
 */
const doFullScreen = (event: { exitFullscreen: () => void; }) => {
  // 点击切换全屏模式
  if (document.fullscreenElement) {
    document.exitFullscreen()
  } else {
    document.documentElement.requestFullscreen()
  }
}
</script>

<style>
.header-row {
  height: 63px;
  border-radius: 4px;
  padding: 3px 0;
}

.mouse-over {
  cursor: pointer;
  padding: 0 3px;
}
</style>