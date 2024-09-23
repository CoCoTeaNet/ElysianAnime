<template>
  <div class="tm-outbox">
    <div class="tm-body">
      <slot name="default"></slot>
    </div>

    <div class="tm-footer">
      <slot name="page"></slot>
    </div>
  </div>
</template>

<script lang="ts" setup>
</script>

<style scoped>
.tm-outbox {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.tm-body {
  overflow-y: auto;
}

.tm-footer {
  padding: 1em 0 0 0;
}

:deep(.el-form-item) {
  margin-right: 1em;
}

:deep(.el-table) {
  height: 100%;
}
</style>