<template>
  <el-affix>
    <el-menu :ellipsis="isMobile" :default-active="navMenuActive" mode="horizontal">
      <!--logo-->
      <el-menu-item index="0" @click="goHome">
        ElysianAnime
      </el-menu-item>
      <div class="flex-grow" />
      <!--功能菜单-->
      <el-menu-item index="1" @click="onBackup">
        <el-icon><Back /></el-icon>
        回退
      </el-menu-item>
      <el-menu-item index="2" @click="onFullScreen">
        <el-icon>
          <Close v-if="store.state.isFullScreen"/>
          <FullScreen v-else/>
        </el-icon>
        宽屏
      </el-menu-item>
      <!--通知消息-->
      <el-menu-item index="3">
        <el-popover trigger="hover" placement="bottom" width="460">
          <template #reference>
            <div>
              <el-icon>
                <el-badge :hidden="notifyList.length <= 0" :value="notifyList.length">
                  <ChatDotRound />
                </el-badge>
              </el-icon>
            </div>
          </template>
          <template #default>
            <el-scrollbar max-height="460px">
              <el-timeline v-if="notifyList.length > 0">
                <el-timeline-item
                    v-for="(notify,index) in notifyList"
                    :key="index"
                    :type="index === 0 ? 'primary' : 'info'"
                    :timestamp="notify.notifyTime"
                >
                  <el-link type="primary" @click="onReadMore(notify)">
                    {{notify.title}}
                  </el-link>
                  <el-text type="info" size="small">{{notify.memo}}</el-text>
                </el-timeline-item>
              </el-timeline>
              <p v-else>
                <el-text class="mx-1" type="info">暂时没有新消息了哦！</el-text>
              </p>
            </el-scrollbar>
          </template>
        </el-popover>
      </el-menu-item>
      <!--导航菜单-->
      <!-- 推荐栏目 -->
      <el-menu-item index="4" @click="toSharesView">
        <el-icon><Share /></el-icon>
        推荐栏目
      </el-menu-item>
      <!-- 资源库 -->
      <el-menu-item index="5" @click="toResourceLibrary">
        <el-icon><VideoCamera /></el-icon>
        资源库
      </el-menu-item>
      <!--番剧管理-->
      <el-sub-menu index="6">
        <template #title>番剧管理</template>
        <el-menu-item index="5-1" @click="router.push({name:'AnimeUserOpusView'})">
          <el-icon><Star/></el-icon>
          我的追番
        </el-menu-item>
        <el-menu-item index="5-2" @click="router.push({name:'AnimeUserOpusView', query: {readStatus: 2}})">
          <el-icon><Reading /></el-icon>
          在看番剧
        </el-menu-item>
        <el-menu-item index="5-3" @click="router.push({name:'AnimeUserOpusView', query: {readStatus: 1}})">
          <el-icon><Collection /></el-icon>
          已看番剧
        </el-menu-item>
      </el-sub-menu>
      <!--个人追番导航-->
      <el-menu-item index="7">
        <el-popover :width="300">
          <template #reference>
              <span style="margin: 0; font-weight: 500">
                {{ store.state.userInfo.nickname }}
              </span>
          </template>
          <template #default>
            <div class="acg-header-userinfo">
              <el-avatar
                  v-if="store.state.userInfo.avatar"
                  :size="60"
                  :src="`api/system/file/getAvatar?avatar=${store.state.userInfo.avatar}`"
                  style="margin-bottom: 8px"
              />
              <el-avatar
                  v-else
                  :size="60"
                  src="@/assets/svg-source/default-avatar.svg"
                  style="margin-bottom: 8px"
              />
              <span style="margin: 0; font-size: 14px; color: var(--el-color-info)">
                {{ store.state.userInfo.nickname }} @ {{ store.state.userInfo.username }}
              </span>
              <p style="margin: 0">
                Element Plus, a Vue 3 based component library for developers,
                designers and product managers
              </p>
              <el-button class="a-h-u-btn" text @click="clickToGo('UserCenterView')">个人中心</el-button>
              <el-button class="a-h-u-btn" text @click="clickToGo('SysFileView')">NAS个人云盘</el-button>
              <el-button class="a-h-u-btn" text @click="clickToIssues">我要提意见</el-button>
              <el-divider style="margin: 0"/>
              <el-button class="a-h-u-btn" text @click="doLogout">退出登录</el-button>
            </div>
          </template>
        </el-popover>
      </el-menu-item>
    </el-menu>
  </el-affix>
</template>

<script lang="ts" setup>
import {router} from "@/router";
import {reqCommonFeedback} from "@/api/ApiFeedback";
import {loginInfo, logout} from "@/api/system/sys-login-api";
import {listByType, read} from "@/api/anime/ani-notify-api";
import {setUserInfo, useStore, setFullScreen} from "@/store";
import {useRoute} from "vue-router";
import {
  Close,
  FullScreen,
  Back,
  ChatDotRound, VideoCamera, Star, Reading, Collection, MoreFilled, Share
} from "@element-plus/icons-vue";
import {onMounted, ref} from "vue";

const store = useStore();
const route = useRoute();
const isMobile = !!navigator.userAgent.match(/AppleWebKit.*Mobile.*/);

const notifyList = ref<any>([]);
const navMenuActive = ref('1');

onMounted(() => {
  reqCommonFeedback(loginInfo(), (data:any) => setUserInfo(data));
  getNotifyList();
});

const clickToGo = (routeName: string) => {
  router.push({name: routeName});
}

const doLogout = () => {
  reqCommonFeedback(logout(), () => {
    setUserInfo({id: '', username: '', nickname: ''});
    router.push({name: 'Login', query: {redirect: route.name ? route.name.toString() : ''}});
  });
}

// 全屏
const onFullScreen = () => {
  setFullScreen();
}

// 返回上一页
const onBackup = () => {
  router.back();
}

// 提意见
const clickToIssues = () => {
  window.open("https://github.com/CoCoTeaNet/janime/issues", "_blank");
}

// 获取通知列表
const getNotifyList = async () => {
  reqCommonFeedback(listByType('OPUS_UPDATE'), (data: any) => {
    if (data && data.length > 0) {
      notifyList.value = data;
    }
  });
}

// 通知跳转链接
const onReadMore = (notify: any) => {
  reqCommonFeedback(read(notify.id), () => {
    getNotifyList();
    window.open(`/#/anime/video/${notify.jumpUrl}/1/1`, '_blank');
  });
}

const toResourceLibrary = () => {
  router.push({
    name:'AnimeHome',
    query: {'hasResource': 1, 'searchKey': route.query.searchKey}
  });
}

const toSharesView = () => {
  router.push({
    name:'AnimeShare'
  });
}

const goHome = () => {
  router.push({
    name:'AnimeHome',
    query: {
      'searchKey': '',
      'hasResource': -1
    }
  });
}
</script>

<style scoped>
.flex-grow {
  flex-grow: 1;
}

.acg-header-userinfo {
  display: flex;
  gap: 16px;
  flex-direction: column;
  align-items: center;
}

.a-h-u-btn {
  text-align: center;
  width: 100%;
}
</style>
