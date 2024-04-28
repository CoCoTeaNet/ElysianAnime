<template>
  <div style="display: flex">
    <el-scrollbar>
      <div class="scrollbar-flex-content">
        <p v-for="item in store.state.tabItems" :key="item" :class="`scrollbar-tab-item ${item.isActive?'active':''}`">
          <span @click="onClick(item)">{{ item.name }}</span>
          <el-icon class="tab-icon" @click="removeTabItem(item.id)">
            <close/>
          </el-icon>
        </p>
      </div>
    </el-scrollbar>
    <el-dropdown>
        <span class="el-dropdown-link">
          <el-icon class="el-icon--right">
            <arrow-down/>
          </el-icon>
        </span>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item @click="refresh">刷新页面</el-dropdown-item>
          <el-dropdown-item @click="onCloseAll">关闭所有</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script lang="ts" setup>
import {Close} from "@element-plus/icons-vue";
import {router} from "@/router";
import {useRoute} from "vue-router";
import {useStore, addTabItem} from "@/store";
import {removeTabItem,initTabItems} from "@/store";
import {onMounted} from "vue";

const store = useStore();
const route = useRoute();

onMounted(() => {
  addTabItem({name: JSON.parse(JSON.stringify(route.meta)).title, url: route.path, isActive: true});
});

const onClick = (obj: any) => {
  router.push({path: obj.url});
  store.state.tabItems.forEach((item: TabItem) => {
    item.isActive = item.id == obj.id;
  });
}

const onCloseAll = () => {
  initTabItems();
}

const refresh = () => {
  window.location.reload();
}
</script>

<style scoped>
.scrollbar-flex-content {
  display: flex;
}

.tab-icon {
  margin-left: 1px;
  padding: 1px;
}

.tab-icon:hover {
  border-radius: 16px;
  color: var(--el-color-primary);
  background-color: var(--el-color-white);
}

.scrollbar-tab-item {
  font-size: 12px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 23px;
  margin: 3px;
  padding: 0 3px;
  text-align: center;
  background: var(--el-color-white);
  box-shadow: var(--el-box-shadow-lighter);
  border-radius: 2px;
  color: var(--el-color-primary);
}

.scrollbar-tab-item:hover {
  cursor: pointer;
  background-color: var(--el-color-primary);
  color: var(--el-color-white);
}

.active {
  background-color: var(--el-color-primary);
  color: var(--el-color-white);
}

.el-dropdown-link {
  cursor: pointer;
  color: var(--el-color-primary);
  display: flex;
  align-items: center;
}
</style>