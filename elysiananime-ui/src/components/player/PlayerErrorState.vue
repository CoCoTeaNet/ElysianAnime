<template>
  <div class="ea-player-error">
    <el-icon :size="36" class="ea-player-error__icon">
      <WarningFilled/>
    </el-icon>
    <p class="ea-player-error__reason">{{ error }}</p>
    <div class="ea-player-error__actions">
      <el-button type="primary" @click="$emit('retry')">重试</el-button>
      <el-button v-if="canRescan" @click="$emit('rescan')">重新扫描</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import {WarningFilled} from '@element-plus/icons-vue';

defineProps<{
  error: string;
  canRescan: boolean;
}>();

defineEmits<{
  (e: 'retry'): void;
  (e: 'rescan'): void;
}>();
</script>

<style scoped>
.ea-player-error {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--ea-space-3);
  color: var(--ea-text-2);
}

.ea-player-error__icon {
  color: var(--ea-accent-update);
}

.ea-player-error__reason {
  margin: 0;
  font-size: 14px;
}
</style>
