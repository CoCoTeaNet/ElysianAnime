<template>
  <el-dialog v-model="show" title="封面上传" width="35%" @close="onCancel">
    <div style="text-align: center">
      <el-upload v-model:file-list="fileList" action="api/system/file/upload" list-type="picture-card"
                 :before-upload="onBeforeUpload"
                 :on-preview="handlePictureCardPreview"
                 :on-remove="handleRemove"
                 :on-success="handleSuccess">
        <el-icon>
          <Plus/>
        </el-icon>
      </el-upload>

      <el-dialog v-model="dialogVisible">
        <img :src="dialogImageUrl" alt="Preview Image"/>
      </el-dialog>
    </div>
  </el-dialog>
</template>

<script lang="ts" setup>
import {ref} from 'vue';
import {update} from '@/api/blog/blo-article-api';
import {Plus} from '@element-plus/icons-vue'
import type {UploadProps, UploadUserFile, UploadFile, UploadFiles, UploadRawFile} from 'element-plus'
import {reqCommonFeedback} from "@/api/ApiFeedback";
import {ElMessage} from "element-plus/es";

const dialogImageUrl = ref('')
const dialogVisible = ref(false)
const disabled = ref(false)
const fileList = ref<UploadUserFile[]>([]);

const props = withDefaults(defineProps<{
  show?: boolean,
  aid: any
}>(), {
  show: false
});

const handleRemove: UploadProps['onRemove'] = (uploadFile, uploadFiles) => {
  console.log(uploadFile, uploadFiles)
}

const handlePictureCardPreview: UploadProps['onPreview'] = (uploadFile) => {
  dialogImageUrl.value = uploadFile.url!
  dialogVisible.value = true
}

const onBeforeUpload = (rawFile: UploadRawFile) => {
  if (rawFile.size > 2048000) {
    ElMessage({type: 'error', message: '图片不能超过2M'});
    return false;
  }
  console.log(rawFile.type)
  if (rawFile.type === 'image/jpeg' || rawFile.type === 'image/png' || rawFile.type === 'image/jpg') {
    return true;
  } else {
    ElMessage.error('不支持的图片类型');
    return false;
  }
}

const handleSuccess = (response: any, uploadFile: UploadFile, uploadFiles: UploadFiles) => {
  if (response.code === 200) {
    let param = {id: props.aid, cover: response.data};
    reqCommonFeedback(update(param), () => {
      ElMessage({type: 'success', message: '上传封面成功'});
      emit('onConfirm');
      emit('update:show', false);
    });
  } else {
    console.log(response)
  }
}

const emit = defineEmits(['update:show', 'onConfirm']);

const onCancel = () => emit('update:show', false);

</script>

<style scoped>
</style>