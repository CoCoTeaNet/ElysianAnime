<template>
  <el-row align="middle">
    <!-- 折叠菜单按钮 -->
    <el-col :span="2">
      <el-button link @click="setCollapseMenu" style="color: #303133">
        <template #icon>
          <el-icon class="mouse-over right-item" :size="20">
            <expand v-if="store.state.isCollapseMenu"/>
            <fold v-else/>
          </el-icon>
        </template>
      </el-button>
    </el-col>
    <!-- 面包屑 -->
    <el-col :span="22">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item v-for="item in itemList" :to="{ path: item.href }">
          {{ item.text }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import {onMounted, ref, watch} from "vue";
import {RouteLocationMatched, useRoute} from "vue-router";
import {Expand, Fold} from '@element-plus/icons-vue';
import {useStore, setCollapseMenu} from "@/store";

const route = useRoute();
const store = useStore();

const itemList = ref<RouteLocationMatched[]>([]);

watch(() => route.path, (v: any) => {
  getRouterMatch(route.matched);
});

onMounted(() => {
  getRouterMatch(route.matched);
});

/**
 * 获取路由导航地址
 */
const getRouterMatch = (list: any[]) => {
  let items: any[] = []
  list.forEach((item, index) => {
    items.push({text: item.meta.title, disabled: index === list.length - 1, href: item.path});
  });
  itemList.value = items;
}
</script>

<style scoped></style>