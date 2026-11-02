<template>
  <el-drawer :model-value="modelValue" title="筛选" size="320px" @update:model-value="$emit('update:modelValue', $event)">
    <el-form label-position="top">
      <el-form-item label="追番进度">
        <el-checkbox-group v-model="form.status">
          <el-checkbox label="未看">未看</el-checkbox>
          <el-checkbox label="已看">已看完</el-checkbox>
          <el-checkbox label="正在观看">在看</el-checkbox>
        </el-checkbox-group>
      </el-form-item>
      <el-form-item label="季度">
        <el-checkbox-group v-model="form.months">
          <el-checkbox v-for="m in months" :key="m" :label="m">{{ m }}</el-checkbox>
        </el-checkbox-group>
      </el-form-item>
      <el-form-item label="年份">
        <el-select v-model="form.years" multiple collapse-tags placeholder="全部" clearable style="width: 100%">
          <el-option v-for="y in years" :key="y" :label="y" :value="y"/>
        </el-select>
      </el-form-item>
      <el-form-item label="资源库">
        <el-switch v-model="form.hasResource" :active-value="1" :inactive-value="-1" active-text="只看有资源"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="onReset">重置</el-button>
      <el-button type="primary" @click="onApply">应用</el-button>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import {reactive, watch} from "vue";

export interface HomeFilters {
  status: string[];
  months: string[];
  years: string[];
  hasResource: number;
}

const props = defineProps<{ modelValue: boolean; filters: HomeFilters }>();
const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void;
  (e: 'apply', filters: HomeFilters): void;
}>();

const months = ['1月', '4月', '7月', '10月'];
const years: string[] = [];
for (let i = 0, c = new Date().getFullYear(); i < c - 2010; i++) {
  years.push('' + (c - i));
}
years.push('2010往前');

const form = reactive<HomeFilters>({status: [], months: [], years: [], hasResource: -1});

watch(() => props.modelValue, (open) => {
  if (open) {
    Object.assign(form, JSON.parse(JSON.stringify(props.filters)));
  }
});

const onReset = () => {
  form.status = [];
  form.months = [];
  form.years = [];
  form.hasResource = -1;
  emit('apply', {...form});
};

const onApply = () => {
  emit('apply', {...form});
  emit('update:modelValue', false);
};
</script>
