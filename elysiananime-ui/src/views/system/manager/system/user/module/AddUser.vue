<template>
  <el-dialog :model-value="show"
             :title="editType === 'update' ? '更新用户' : '新增用户'"
             width="50%"
             @close="onCancel">
    <el-form :model="dataForm" ref="sstFormRef" label-width="100px" :rules="rules" style="max-height: 600px">
      <el-form-item prop="username" label="账号名">
        <el-input placeholder="请输入账号名" v-model="dataForm.username"></el-input>
      </el-form-item>
      <el-form-item prop="nickname" label="用户昵称">
        <el-input placeholder="请输入账号昵称" v-model="dataForm.nickname"></el-input>
      </el-form-item>
      <el-form-item prop="password" label="用户密码">
        <el-input placeholder="密码长度6~30" :prefix-icon="Lock" v-model="dataForm.password" type="password"></el-input>
      </el-form-item>
      <el-form-item prop="email" label="邮箱">
        <el-input placeholder="example@xx.com" v-model="dataForm.email"></el-input>
      </el-form-item>
      <el-form-item prop="roleIds" label="角色">
        <el-select v-model="dataForm.roleIds" placeholder="选择角色" :multiple="true">
          <el-option v-for="item in roleOptions" :key="item.id" :label="item.roleName" :value="item.id">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item prop="sort" label="性别">
        <el-radio-group v-model="dataForm.sex">
          <el-radio :label="0">不公开</el-radio>
          <el-radio :label="1">男</el-radio>
          <el-radio :label="2">女</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item prop="sort" label="状态">
        <el-radio-group v-model="dataForm.accountStatus">
          <el-radio :label="0">停用</el-radio>
          <el-radio :label="1">正常</el-radio>
          <el-radio :label="2">冻结</el-radio>
          <el-radio :label="3">封禁</el-radio>
        </el-radio-group>
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
import {onMounted, reactive, ref, watch} from 'vue';
import {ElInput, FormInstance} from 'element-plus';
import {Lock} from "@element-plus/icons-vue";
import {add, update} from '@/api/system/sys-user-api';
import {reqCommonFeedback, reqSuccessFeedback} from "@/api/ApiFeedback";
import roleApi from "@/api/system/sys-role-api";

const props = withDefaults(defineProps<{
  show?: boolean,
  editType: string,
  user: UserModel
}>(), {
  show: false
});

const dataForm = ref<UserModel>();
const sstFormRef = ref<FormInstance>();
const roleOptions = ref<RoleModel[]>([]);
const rules = reactive({
  username: [{required: true, min: 2, max: 30, message: '长度限制2~30', trigger: 'blur'}],
  nickname: [{required: true, min: 2, max: 30, message: '长度限制2~30', trigger: 'blur'}],
  roleIds: [{required: true, message: '请选择角色', trigger: 'blur'}]
});

watch(() => props.show, (b: boolean) => {
  if (b) {
    dataForm.value = props.editType === 'update' ? props.user : {sex: 0, accountStatus: 1};
    loadRoles();
  }
});

const emit = defineEmits(['update:show', 'onConfirm']);

const loadRoles = () => {
  let param: any = {pageNo: 1, pageSize: 1000, sysRole: {id: ''}}
  reqCommonFeedback(roleApi.listByPage(param), (data: any) => {
    roleOptions.value = data.records;
  });
}

const onCancel = () => emit('update:show', false);

const onConfirm = (formEl: FormInstance) => {
  formEl.validate((valid: boolean) => {
    if (valid) {
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
    }
  });
}
</script>