<template>
  <el-dialog :model-value="modelValue" title="从 Bangumi 添加番剧" width="480px"
             @update:model-value="$emit('update:modelValue', $event)">
    <el-form>
      <el-form-item label="Bangumi 详细链接">
        <el-input v-model="bgmUrl" placeholder="https://bgm.tv/subject/389772"/>
        <el-link type="primary" href="https://bgm.tv" target="_blank">去 Bangumi 查找番剧信息</el-link>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">取消</el-button>
      <el-button :loading="loading" type="warning" @click="onSubmit(1)">重刷</el-button>
      <el-button :loading="loading" type="primary" @click="onSubmit(0)">新增</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import {ref} from "vue";
import {ElMessage} from "element-plus";
import {addOpusFromBangumi} from "@/api/anime/ani-opus-api";

defineProps<{ modelValue: boolean }>();
const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void;
  (e: 'success'): void;
}>();

const bgmUrl = ref('');
const loading = ref(false);

const onSubmit = async (isCover: number) => {
  loading.value = true;
  try {
    const data: any = await addOpusFromBangumi({bgmUrl: bgmUrl.value, isCover});
    if (data.code === 200) {
      ElMessage.success('提交成功');
      bgmUrl.value = '';
      emit('success');
      emit('update:modelValue', false);
    } else {
      ElMessage.error(data.message);
    }
  } finally {
    loading.value = false;
  }
};
</script>
