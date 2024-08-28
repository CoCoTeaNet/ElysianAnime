<template>
  <table-manage>
    <!-- 表格操作 -->
    <template #search>
      <el-form-item label="版本名称">
        <el-input placeholder="版本名称" v-model:model-value="pageParam.searchObject.updateNo"/>
      </el-form-item>
      <el-form-item label="平台名称">
        <el-input placeholder="平台名称" v-model:model-value="pageParam.searchObject.platformName"/>
      </el-form-item>
      <el-form-item>
        <el-button :icon="Search" type="primary" @click="loadTableData">搜索</el-button>
        <el-button :icon="RefreshRight" @click="resetSearchForm">重置</el-button>
      </el-form-item>
    </template>

    <template #operate>
      <el-button :icon="Plus" type="primary" @click="onAdd">添加版本</el-button>
    </template>

    <!-- 表格视图 -->
    <template #default>
      <el-table stripe row-key="id" :data="pageVo.records">
        <el-table-column prop="updateNo" label="版本号" width="150" />
        <el-table-column prop="platformName" label="平台名称" show-overflow-tooltip />
        <el-table-column prop="updateDesc" label="更新描述" width="300"  />
        <el-table-column prop="downloadUrl" label="下载地址" width="250" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="200" />
        <el-table-column prop="updateTime" label="更新时间" width="200" />
        <!-- 单行操作 -->
        <el-table-column fixed="right" width="180" label="操作">
          <template #default="scope">
            <el-button :icon="Edit" size="small" @click="onEdit(scope.row)">编辑</el-button>
            <el-button :icon="DeleteFilled" size="small" plain type="danger" @click="onRemove(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <template #page>
      <el-pagination background layout="total, sizes, prev, pager, next, jumper"
                     :total="pageVo.total" :page-size="pageVo.pageSize" :page-sizes=[5,10,15]
                     @current-change="onPageChange" @size-change="onSizeChange"/>
    </template>

    <!-- 编辑对话框 -->
    <template #form>
      <el-dialog v-model="dialogFormVisible" :title="`${editForm.id? '编辑' : '添加'}版本`">
        <el-form ref="sttFormRef" label-width="120px" :model="editForm" :rules="rules">
          <el-form-item prop="updateNo" label="版本号">
            <el-input v-model="editForm.updateNo"></el-input>
          </el-form-item>
          <el-form-item prop="updateDesc" label="更新描述">
            <el-input v-model="editForm.updateDesc" type="textarea" rows="8"></el-input>
          </el-form-item>
          <el-form-item prop="platformName" label="平台名称">
            <el-input v-model="editForm.platformName"></el-input>
          </el-form-item>
          <el-form-item prop="downloadUrl" label="下载地址">
            <el-input v-model="editForm.downloadUrl"></el-input>
          </el-form-item>
        </el-form>
        <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogFormVisible = false">取消</el-button>
          <el-button type="primary" @click="doUpdate(sttFormRef)">确认</el-button>
        </span>
        </template>
      </el-dialog>
    </template>
  </table-manage>
</template>

<script setup lang="ts">
import {onMounted, ref, reactive, nextTick} from "vue";
import {add, deleteBatch, update, listByPage} from "@/api/system/sys-version-api";
import {reqCommonFeedback, reqSuccessFeedback} from "@/api/ApiFeedback";
import TableManage from "@/components/container/TableManage.vue";
import {ElForm} from "element-plus/es";
import {ElMessageBox} from "element-plus";
import {DeleteFilled, Plus, Search, RefreshRight, Edit} from "@element-plus/icons-vue";

type FormInstance = InstanceType<typeof ElForm>
const sttFormRef = ref<FormInstance>();

const pageParam = ref<PageParam>({pageNo: 1, pageSize: 10, searchObject: {}});
// 表单参数
const editForm = ref<VersionModel>({});
// 加载进度
const loading = ref<boolean>(true);
// 表单校验规则
const rules = reactive({
  updateNo: [{required: true, min: 2, max: 30, message: '长度限制2~20', trigger: 'blur'}],
});
const dialogFormVisible = ref<boolean>(false);
const pageVo = ref<PageVO>({pageNo: 1, pageSize: 10, total: 0, records: []});

// 初始化数据
onMounted(() => {
  loadTableData();
});

const onEdit = (row: DictionaryModel): void => {
  editForm.value = row;
  dialogFormVisible.value = true;
}

const onAdd = () => {
  dialogFormVisible.value = true;
  editForm.value = {};
}

const onRemove = (row: DictionaryModel): void => {
  ElMessageBox.confirm('确认删除此版本?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    reqSuccessFeedback(deleteBatch([row.id]), '删除成功', () => {
      loadTableData();
    });
  });
}

const loadTableData = (): void => {
  if (!loading.value) loading.value = true;
  let param = {
    pageNo: pageParam.value.pageNo,
    pageSize: pageParam.value.pageSize,
    sysVersion: pageParam.value.searchObject
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

const doUpdate = (formEl: any): void => {
  formEl.validate((valid: any) => {
    if (valid) {
      if (!editForm.value.id) {
        reqSuccessFeedback(add(editForm.value), '新增成功', () => {
          loadTableData();
          dialogFormVisible.value = false;
        });
      } else {
        reqSuccessFeedback(update(editForm.value), '修改成功', () => {
          loadTableData();
          dialogFormVisible.value = false;
        });
      }
    }
  });
}

const resetSearchForm = () => {
  pageParam.value.searchObject = {};
}
</script>

<style scoped></style>