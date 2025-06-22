<template>
  <el-menu class="new-el-menu--sidebar" :default-active="menuState.defaultActive"
    :default-openeds="menuState.defaultOpened" :collapse="store.state.isCollapseMenu" :unique-opened="true">
    <!-- 菜单渲染 -->
    <child-menu :menu-list="store.state.userInfo.menuList" />
  </el-menu>
</template>

<script setup lang="ts">
import { useStore, updateCollapseMenu } from "@/store";
import { useRoute } from "vue-router";
import { computed } from "vue";
import ChildMenu from "@/layout/modules/ChildMenu.vue";
import { useWindowSize } from "@vueuse/core";

const store = useStore();
const route = useRoute();

/**
 * 根据路由路径动态设置当前菜单的状态
 */
let menuState = computed(() => {
  initMenu();
  const path = route.path;
  const routes = store.state.userInfo.menuList;
  let state = { defaultOpened: [""], defaultActive: "" };
  if (routes) {
    let stack: Array<string> = [];
    for (let i in routes) {
      let route = routes[i];
      let len = stack.length;
      dfs(route, path, stack);
      if (!route.id) {
        console.error('路由配置错误，请检查菜单路径');
      }
      if (len < stack.length) {
        // 默认打开的页面
        state.defaultOpened = [routes[i].id + ''];
        break;
      }
      if (path === route.routerPath) {
        stack.push(route.id + '');
      }
    }
    if (stack.length > 0) {
      state.defaultActive = stack[0];
    }
  }
  return state;
});

const dfs = (root: MenuModel, path: string, stack: Array<string>) => {
  if (root.children) {
    let len = stack.length;
    for (let i in root.children) {
      let r = root.children[i];
      if (path === r.routerPath) {
        stack.push(r.id + '');
        return;
      }
      dfs(r, path, stack);
      if (len < stack.length) {
        return;
      }
    }
  }
}

const initMenu = () => {
  let winSize = useWindowSize();
  if (winSize.width.value <= 1200) {
    updateCollapseMenu(true);
  } else {
    updateCollapseMenu(false);
  }
}
</script>

<style scoped lang="css">
.logo {
  display: flex;
  align-items: center;
  color: rgb(55, 76, 82);
  padding: 0.5em 0 0.5em 1em;
}

/* 解决侧边栏折叠卡顿的问题 */
.new-el-menu--sidebar:not(.el-menu--collapse) {
  width: 220px;
}
</style>