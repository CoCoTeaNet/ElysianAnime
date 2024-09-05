<template>
  <div class="tm-outbox">
    <el-form class="tm-header">
      <div class="tm-search">
        <slot name="search"></slot>
      </div>
      <div class="tm-operate">
        <slot name="operate"></slot>
      </div>
    </el-form>

    <div class="tm-body">
      <slot name="default"></slot>
    </div>

    <div class="tm-footer">
      <slot name="page"></slot>
      <slot name="form"></slot>
    </div>
  </div>
</template>

<script lang="ts" setup>
</script>

<style scoped>
.tm-outbox {
  width: 100%;
  height: 100%;
  overflow: auto;
  display: flex;
  flex-direction: column;
}

.tm-header {
  display: flex;
  justify-content: space-between;
  border-bottom: solid 1px var(--el-menu-border-color);
}

.tm-search {
  display: flex;
  flex-wrap: wrap;
}

.tm-operate {
  display: flex;
  justify-content: end;
}

.tm-body {
  width: 100%;
  height: calc(100% - 80px);
  overflow: auto;
}

.tm-footer {
  padding-top: 1em;
  background-color: var(--el-fill-color-blank);
}

:deep(.el-form-item) {
  margin-right: 1em;
}

:deep(.el-table) {
  height: 100%;
}
</style>