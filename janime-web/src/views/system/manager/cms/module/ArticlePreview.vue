<template>
  <el-dialog v-model="show" :title="'标题：' + dataForm.title" width="75%" @close="onCancel">
    <template #default>
      <div style="max-height: 600px;overflow: auto;background-color: gainsboro;padding: 3px">
        <pre v-html="dataForm.content" style="overflow: auto;white-space: pre-wrap;word-wrap: break-word"></pre>
      </div>
    </template>

    <template #footer>
      <el-tag v-for="i in dynamicTags" style="margin-right: 3px">{{ i }}</el-tag>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import {ref, reactive, watch} from 'vue';
import {detail} from '@/api/blog/blo-article-api';
import {reqCommonFeedback} from "@/api/ApiFeedback";

const props = withDefaults(defineProps<{
  show?: boolean,
  aid: any
}>(), {
  show: false
});

const mode = ref<string>('default');
const dataForm = reactive<any>({title: '', content: ''});
const inputValue = ref<string>('');
const dynamicTags = ref<any>(['默认']);

watch(() => props.show, (b: boolean) => {
  if (b) loadArticleDetail();
});

const loadArticleDetail = () => {
  if (props.aid) {
    reqCommonFeedback(detail(props.aid), (data: any) => {
      dataForm.title = data.title;
      dataForm.content = data.content;
      dynamicTags.value = data.tagList;
    });
  }
}

const emit = defineEmits(['update:show', 'onConfirm']);

const onCancel = () => emit('update:show', false);

</script>

<style scoped>
</style>