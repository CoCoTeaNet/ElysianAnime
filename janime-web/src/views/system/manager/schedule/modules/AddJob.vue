<template>
  <el-dialog v-model="show" title="新增用户" width="50%" @close="onCancel">
    <el-form :model="dataForm" ref="sstFormRef" label-width="120px" :rules="rules" style="max-height: 600px">
      <el-form-item prop="name" label="任务名称">
        <el-input v-model="dataForm.name"></el-input>
      </el-form-item>
      <el-form-item prop="className" label="任务对应类名">
        <el-input v-model="dataForm.className"></el-input>
      </el-form-item>
      <el-form-item prop="methodName" label="方法名">
        <el-input v-model="dataForm.methodName"></el-input>
      </el-form-item>
      <el-form-item prop="parameters" label="参数JSON对象">
        <el-input v-model="dataForm.parameters"></el-input>
      </el-form-item>
      <el-form-item prop="cornExpression" label="Cron表达式">
        <el-input v-model="dataForm.cornExpression"></el-input>
      </el-form-item>
      <el-form-item prop="description" label="任务描述">
        <el-input v-model="dataForm.description"></el-input>
      </el-form-item>
      <el-form-item prop="sort" label="配置类型">
        <el-radio-group v-model="dataForm.type">
          <el-radio :label="0">类模式</el-radio>
          <el-radio :label="1">函数模式</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item prop="sort" label="是否启用">
        <el-radio-group v-model="dataForm.active">
          <el-radio :label="0">未启用</el-radio>
          <el-radio :label="1">启用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="排序">
        <el-input placeholder="排序" type="number" v-model="dataForm.sort"/>
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="onCancel">取 消</el-button>
        <el-button type="primary" @click="onConfirm(sstFormRef)">确 认</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import {reactive, ref, watch} from 'vue';
import {ElInput, FormInstance} from 'element-plus';
import {add, update} from '@/api/schedule/job-api';
import {reqSuccessFeedback} from "@/api/ApiFeedback";

const props = withDefaults(defineProps<{
  show?: boolean,
  editType: string,
  job: JobModel
}>(), {
  show: false
});

const dataForm = ref<JobModel>();
const sstFormRef = ref<FormInstance>();
const rules = reactive({
  name: [{required: true, min: 2, max: 90, message: '长度限制2~90', trigger: 'blur'}],
  className: [{required: true, min: 2, max: 100, message: '长度限制2~100', trigger: 'blur'}],
  methodName: [{required: true, min: 2, max: 100, message: '长度限制2~100', trigger: 'blur'}],
  parameters: [{required: true, min: 0, max: 900, message: '长度限制0~900', trigger: 'blur'}],
  cornExpression: [{required: true, min: 2, max: 50, message: '长度限制2~50', trigger: 'blur'}],
  description: [{required: true, min: 2, max: 900, message: '长度限制6~900', trigger: 'blur'}],
  type: [{required: true, message: '配置类型为空', trigger: 'blur'}],
  active: [{required: true, message: '启用状态为空', trigger: 'blur'}]
});

watch(() => props.show, (b: boolean) => {
  if (b) dataForm.value = props.editType === 'update' ? props.job : {active: 0, type: 0, sort: 1};
});

const emit = defineEmits(['update:show', 'onConfirm']);

const onCancel = () => emit('update:show', false);

const onConfirm = (formEl: FormInstance) => {
  formEl.validate((valid: boolean) => {
    if (valid) {
      try {
        let parameters = eval(`(${dataForm.value.parameters})`);
        dataForm.value.parameters = JSON.stringify(parameters);
        if (props.editType === 'create') {
          reqSuccessFeedback(add(dataForm.value), "添加成功",() => {
            emit('update:show', false);
            emit('onConfirm');
          });
        } else if (props.editType === 'update') {
          reqSuccessFeedback(update(dataForm.value), "修改成功",() => {
            emit('update:show', false);
            emit('onConfirm');
          });
        }
      } catch(e) {
        ElMessage({type: 'error', message: '参数为非JSON格式，请检查'});
      }
    }
  });
}
</script>