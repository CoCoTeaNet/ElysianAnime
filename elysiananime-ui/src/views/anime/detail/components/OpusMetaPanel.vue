<template>
  <el-card shadow="never" class="ea-detail__meta">
    <div class="ea-meta__header">
      <h3 class="ea-meta__title">简介</h3>
      <el-link v-if="opus.aniSummary" type="primary" @click="expanded = !expanded">
        {{ expanded ? '收起' : '展开' }}
      </el-link>
    </div>
    <p class="ea-meta__summary" :class="{'is-collapsed': !expanded}" v-html="opus.aniSummary"></p>
  </el-card>
</template>

<script setup lang="ts">
import {ref} from "vue";

defineProps<{
  opus: any;
}>();

const expanded = ref<boolean>(false);
</script>

<style scoped>
.ea-detail__meta {
  border: 1px solid var(--ea-border);
  border-radius: var(--ea-radius-md);
}

.ea-meta__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.ea-meta__title {
  margin: 0;
  font-size: 16px;
  color: var(--ea-text-1);
}

.ea-meta__summary {
  margin: var(--ea-space-2) 0 0;
  font-size: 13px;
  line-height: 1.7;
  color: var(--ea-text-2);
  white-space: pre-line;
}

.ea-meta__summary.is-collapsed {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
