<template>
  <div class="ea-fbar">
    <el-input
        v-model="searchKey"
        class="ea-fbar__search"
        placeholder="搜索番剧"
        clearable
        :prefix-icon="Search"
        @keyup.enter="onSearch"
        @clear="onSearch"
    />
    <el-button :icon="Filter" @click="$emit('open-filter')">筛选</el-button>
    <el-button type="primary" :icon="Plus" @click="$emit('add')">添加番剧</el-button>
  </div>
</template>

<script setup lang="ts">
import {ref} from "vue";
import {Filter, Plus, Search} from "@element-plus/icons-vue";

const props = defineProps<{ keyword: string }>();
const emit = defineEmits<{
  (e: 'search', keyword: string): void;
  (e: 'open-filter'): void;
  (e: 'add'): void;
}>();

const searchKey = ref(props.keyword);

const onSearch = () => emit('search', searchKey.value.trim());
</script>

<style scoped>
.ea-fbar {
  display: flex;
  align-items: center;
  gap: var(--ea-space-2);
}

.ea-fbar__search {
  width: 220px;
}

@media (max-width: 768px) {
  .ea-fbar__search {
    width: 140px;
  }
}
</style>
