<template>
  <template v-for="item in menuList" :key="item.id">

    <template v-if="item.menuType === 1 || item.menuType === 0">
      <el-sub-menu v-if="hasChildren(item)" :index="item.id">
        <template #title>
          <el-icon>
            <component :is="item.iconPath"></component>
          </el-icon>
          <span>{{ item.menuName }}</span>
        </template>
        <!-- 递归渲染子菜单 -->
        <child-menu :menu-list="item.children ? item.children : []"/>
      </el-sub-menu>

      <el-menu-item v-if="!hasChildren(item)" @click="onClickMenu(item)" :index="item.id">
        <el-icon>
          <component :is="item.iconPath"></component>
        </el-icon>
        <template #title>
          <span>{{ item.menuName }}</span>
        </template>
      </el-menu-item>
    </template>

  </template>
</template>

<script lang="ts" setup>
import {router} from "@/router";
import {addTabItem, useStore} from "@/store";

defineProps<{
  menuList: MenuModel[]
}>();

const store = useStore();

const hasChildren = (m: MenuModel) => {
  return m.children && m.children.length > 0;
}

const onClickMenu = (item: any) => {
  if (item.isExternalLink == 1) {
    window.open(item.routerPath, '_blank');
  } else {
    router.push({path: item.routerPath});
    addTabItem({name: item.menuName, url: item.routerPath, isActive: true});
  }
}
</script>

<style lang="css">
.el-menu-item.is-active {
  color: #000000;
  background-color: rgb(204, 204, 204);
  padding: 3px;
}

.el-menu-item:hover {
  background-color: rgb(204, 204, 204, 0.5);
}
</style>
