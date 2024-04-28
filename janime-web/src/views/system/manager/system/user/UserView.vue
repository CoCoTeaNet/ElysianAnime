<template>
  <table-manage>
    <template #search>
      <el-form-item label="用户账号">
        <el-input placeholder="账号" v-model="pageParam.searchObject.username"/>
      </el-form-item>
      <el-form-item label="用户昵称">
        <el-input placeholder="昵称" v-model="pageParam.searchObject.nickname"/>
      </el-form-item>
      <el-form-item label="账号邮箱">
        <el-input placeholder="邮箱" v-model="pageParam.searchObject.email"/>
      </el-form-item>
      <el-form-item label="用户性别">
        <el-select placeholder="选择性别" style="width: 200px" v-model="pageParam.searchObject.sex">
          <el-option v-for="i in sexList" :label="i.label" :value="i.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="账号状态">
        <el-select placeholder="选择状态" style="width: 200px" v-model="pageParam.searchObject.accountStatus">
          <el-option v-for="i in accountStatusList" :label="i.label" :value="i.value"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button :icon="Search" type="primary" @click="loadTableData">搜索</el-button>
        <el-button :icon="RefreshRight" @click="onResetSearchForm">重置</el-button>
      </el-form-item>
    </template>

    <template #operate>
      <el-button :icon="Plus" type="primary" @click="onCreate">添加用户</el-button>
      <el-button :icon="DeleteFilled" plain type="danger" @click="onDeleteBatch">批量删除</el-button>
    </template>

    <template #default>
      <el-table v-loading="loading" :data="pageVo.records" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"/>
        <el-table-column prop="username" width="200" label="账号"/>
        <el-table-column prop="nickname" width="200" label="昵称"/>
        <el-table-column prop="roleList" width="220" label="角色">
          <template #default="scope">
            <span style="display: flex;flex-wrap: wrap;">
              <el-tag v-for="(role, index) in scope.row.roleList" :key="index" style="margin-right: 0.5rem">
                {{role.roleName}}
              </el-tag>
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="sex" label="性别">
          <template #default="scope">
            <el-tag :type="getSex(scope.row.sex, 0)">
              {{ getSex(scope.row.sex, 1) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="accountStatus" label="状态">
          <template #default="scope">
            <el-tag :type="getAccountStatus(scope.row.accountStatus, 0)">
              {{ getAccountStatus(scope.row.accountStatus, 1) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="email" width="200" label="邮箱"/>
        <el-table-column prop="lastLoginIp" width="200" label="最后登录IP"/>
        <el-table-column prop="lastLoginTime" width="200" label="最后登录时间"/>
        <el-table-column fixed="right" label="操作" width="180">
          <template #default="scope">
            <el-button :icon="Edit" size="small" @click="onEdit(scope.row)">编辑</el-button>
            <el-button :icon="DeleteFilled" size="small" type="danger" plain @click="onDelete(scope.row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <template #page>
      <el-pagination background layout="total, sizes, prev, pager, next, jumper"
                     :total="pageVo.total" :page-size="pageParam.pageSize" :page-sizes=[5,10,15]
                     @current-change="onPageChange" @size-change="onSizeChange"/>
    </template>

    <template #form>
      <add-user v-model:show="formShow" :user="editUser" :edit-type="editType" @onConfirm="loadTableData"/>
    </template>
  </table-manage>
</template>

<script setup lang="ts">
import {nextTick, onMounted, ref} from "vue";
import {reqCommonFeedback} from "@/api/ApiFeedback";
import {listByPage, deleteBatch} from "@/api/system/sys-user-api";
import TableManage from "@/components/container/TableManage.vue";
import AddUser from "@/views/system/manager/system/user/module/AddUser.vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {DeleteFilled, Plus, Search, RefreshRight, Edit} from "@element-plus/icons-vue";

const formShow = ref<boolean>(false);
const editType = ref<string>("create");
const editUser = ref<UserModel>();
const multipleSelection = ref<any[]>([]);
// 分页参数
const pageParam = ref<PageParam>({pageNo: 1, pageSize: 15, searchObject: {}});
// api返回的分页数据
const pageVo = ref<PageVO>({pageNo: 1, pageSize: 15, total: 0, records: []});
// 加载进度
const loading = ref<boolean>(true);
const accountStatusList = ref<any>([
  {label: '停用', value: 0},
  {label: '正常', value: 1},
  {label: '冻结', value: 2},
  {label: '封禁', value: 3}
]);
const sexList = ref<any>([
  {label: '不公开', value: 0},
  {label: '男', value: 1},
  {label: '女', value: 2}
]);

// 初始化数据
onMounted(() => {
  loadTableData();
});

const getAccountStatus: any = (status: number, type: number) => {
  let obj = {color: '', text: ''};
  switch (status) {
    case 0:
      obj = {color: 'warning', text: '停用'};
      break;
    case 1:
      obj = {color: 'success', text: '正常'};
      break;
    case 2:
      obj = {color: 'info', text: '冻结'};
      break;
    case 3:
      obj = {color: 'danger', text: '封禁'};
      break;
  }
  return type === 0 ? obj.color : obj.text;
}

const getSex: any = (status: number, type: number) => {
  let obj = {color: '', text: ''};
  switch (status) {
    case 0:
      obj = {color: 'info', text: '不公开'};
      break;
    case 1:
      obj = {color: 'primary', text: '男'};
      break;
    case 2:
      obj = {color: 'success', text: '女'};
      break;
  }
  return type === 0 ? obj.color : obj.text;
}

const onEdit = (row: UserModel): void => {
  formShow.value = true;
  let roleIds:any = [];
  row.roleList?.map(item => roleIds.push(item.id));
  row.roleIds = roleIds;
  editUser.value = row;
  editType.value = 'update';
}

const loadTableData = () => {
  if (!loading.value) loading.value = true;
  let param = {
    pageNo: pageParam.value.pageNo,
    pageSize: pageParam.value.pageSize,
    sysUser: pageParam.value.searchObject
  };
  reqCommonFeedback(listByPage(param), (data: any) => {
    pageVo.value = data;
    loading.value = false;
  });
}

const onPageChange = (currentPage: number) => {
  pageParam.value.pageNo = currentPage;
  nextTick(() => loadTableData());
}

const onSizeChange = (size: number) => {
  pageParam.value.pageSize = size;
  nextTick(() => loadTableData());
}

const onResetSearchForm = () => {
  pageParam.value.searchObject = {};
}

const onCreate = () => {
  formShow.value = true;
  editType.value = 'create';
}

const onDelete = (id: string) => {
  ElMessageBox.confirm('确认该用户?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    reqCommonFeedback(deleteBatch([id]), () => {
      ElMessage({type: 'success', message: '删除成功'});
      loadTableData();
    });
  });
}

const onDeleteBatch = () => {
  let ids: string[] = [];
  multipleSelection.value.map((item) => ids.push(item.id));
  ElMessageBox.confirm('确认删除所选用户?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    reqCommonFeedback(deleteBatch(ids), () => {
      ElMessage({type: 'success', message: '删除成功'});
      loadTableData();
    });
  });
}

const handleSelectionChange = (arr: any) => {
  multipleSelection.value = arr;
}
</script>

<style scoped></style>