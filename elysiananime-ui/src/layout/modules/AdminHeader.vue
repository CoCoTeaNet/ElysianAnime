<template>
  <el-row align="middle" class="header-row">
    <el-col :span="20">
      <el-space>
        <!-- LOGO -->
        <el-space class="logo">
          <img src="@/assets/svg-source/logo.png" style="width: 36px;margin-right: 3px" alt="login-logo">
          <h3>ElysianAnime</h3>
        </el-space>

        <el-button link @click="setCollapseMenu">
          <template #icon>
            <el-icon class="mouse-over right-item" :size="iconSize">
              <expand v-if="store.state.isCollapseMenu"/>
              <fold v-else/>
            </el-icon>
          </template>
        </el-button>
        <admin-tab/>
      </el-space>
    </el-col>

    <!-- 用户信息 -->
    <el-col :span="4">
      <el-row :gutter="10" justify="end" align="middle">
        <!--通知消息-->
        <div class="mouse-over">
          <el-popover trigger="hover" placement="bottom" width="460">
            <template #reference>
              <div>
                <el-icon :size="iconSize-2">
                  <el-badge :hidden="notifyList.length <= 0" :value="notifyList.length">
                    <chat-dot-round />
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
        </div>
        <el-icon class="mouse-over" :size="iconSize-2" @click="doFullScreen">
          <full-screen/>
        </el-icon>
        <el-icon class="mouse-over" :size="iconSize-2" @click="clickToGo('Home')">
          <house/>
        </el-icon>
        <el-dropdown>
          <span class="mouse-over">
            <el-space>
              <el-avatar shape="square" :src="avatar"/>
              <el-text>{{store.state.userInfo.nickname}}</el-text>
            </el-space>
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
import {ChatDotRound, Expand, Fold, FullScreen, House} from "@element-plus/icons-vue";
import {onMounted, ref} from 'vue';
import {listByType, read} from "@/api/anime/ani-notify-api";
import default_avatar from '@/assets/svg-source/default-avatar.svg';

const store = useStore();
const route = useRoute();

const notifyList = ref<any>([]);
const iconSize = ref<number>(24);
const avatar = ref<any>(default_avatar);

onMounted(() => {
  reqCommonFeedback(loginInfo(), (data:any) => setUserInfo(data));

  if (store.state.userInfo.avatar) {
    avatar.value = `api/system/file/getAvatar?avatar=${store.state.userInfo.avatar}`;
  }

  getNotifyList();
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
    router.push({
          name: "AnimeVideo",
          params: {
            id: notify.jumpUrl,
            num: 0,
            time: 1
          }
        }
    );
  });
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
