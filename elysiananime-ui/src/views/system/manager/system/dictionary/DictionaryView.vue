<template>
  <table-manage>
    <!-- 表格操作 -->
    <template #search>
      <el-form-item label="字典名称">
        <el-input placeholder="字典名称" v-model:model-value="searchObj.dictionaryName"/>
      </el-form-item>
      <el-form-item label="启用状态">
        <el-select placeholder="选择启用状态" style="width: 200px" v-model="searchObj.enableStatus">
          <el-option v-for="i in enableStatusList" :label="i.label" :value="i.value"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadTableData" :icon="Search">搜索</el-button>
        <el-button @click="resetSearchForm" :icon="Refresh">重置</el-button>
        <el-button @click="onExpandAll">
          <el-icon>
            <arrow-right-bold v-if="!isExpandAll"/>
            <arrow-down-bold v-else/>
          </el-icon>
          {{ isExpandAll ? '收起' : '展开' }}
        </el-button>
      </el-form-item>
    </template>

    <template #operate>
      <el-button type="primary" @click="onAdd" :icon="Plus">添加字典</el-button>
      <el-button plain type="danger" @click="onDeleteBatch" :icon="DeleteFilled">批量删除</el-button>
    </template>

    <!-- 表格视图 -->
    <template #default>
      <el-table v-if="isShowTable" stripe row-key="id" :data="records" v-model:default-expand-all="isExpandAll"
                @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="80"/>
        <el-table-column prop="dictionaryName" label="名称" sortable show-overflow-tooltip/>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip/>
        <el-table-column prop="enableStatus" label="是否启用">
          <template #default="scope">
            <el-tag :type="getConfirm(scope.row.enableStatus, 0)">{{ getConfirm(scope.row.enableStatus, 1) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序号" sortable/>
        <el-table-column prop="createBy" label="创建人" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="200" />
        <!-- 单行操作 -->
        <el-table-column fixed="right" width="200" label="操作">
          <template #default="scope">
            <el-button size="small" @click="onEdit(scope.row)" :icon="Edit">编辑</el-button>
            <el-button size="small" plain type="danger" @click="onRemove(scope.row)" :icon="DeleteFilled">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <!-- 编辑对话框 -->
    <template #form>
      <el-dialog v-model="dialogFormVisible" :title="`${editForm.id? '编辑' : '添加'}字典`">
        <el-form ref="sttFormRef" label-width="120px" :model="editForm" :rules="rules">
          <el-form-item prop="dictionaryName" label="字典名称">
            <el-input v-model="editForm.dictionaryName"></el-input>
          </el-form-item>
          <el-form-item prop="remark" label="备注">
            <el-input v-model="editForm.remark"></el-input>
          </el-form-item>
          <el-form-item prop="enableStatus" label="是否启用">
            <el-radio-group v-model="editForm.enableStatus">
              <el-radio :label="0">是</el-radio>
              <el-radio :label="1">否</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item prop="sort" label="显示顺序">
            <el-input v-model="editForm.sort" type="number"></el-input>
          </el-form-item>
          <el-form-item label="上级">
            <el-cascader clearable v-model="editForm.parentId" placeholder="选择节点"
                         :props="defaultProps" :options="records" :show-all-levels="false"
                         @change="handleChange">
            </el-cascader>
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
import {nextTick, onMounted, ref, reactive} from "vue";
import {listByTree, add, deleteBatch, update} from "@/api/system/sys-dictionary-api";
import {reqCommonFeedback, reqSuccessFeedback} from "@/api/ApiFeedback";
import TableManage from "@/components/container/TableManage.vue";
import {ElForm} from "element-plus/es";
import {ElMessage, ElMessageBox} from "element-plus";
import {DeleteFilled, Edit, Plus, Refresh, Search} from "@element-plus/icons-vue";

type FormInstance = InstanceType<typeof ElForm>
const sttFormRef = ref<FormInstance>();

// 级联选择框配置
const defaultProps = {
  value: 'id',
  label: 'dictionaryName',
  children: 'children',
  checkStrictly: true
}

const records = ref<any>();
const searchObj = ref<DictionaryModel>({});
const isExpandAll = ref<boolean>(true);
// 表单参数
const editForm = ref<DictionaryModel>({});
// 加载进度
const loading = ref<boolean>(true);
// 表单校验规则
const rules = reactive({
  dictionaryName: [{required: true, min: 2, max: 30, message: '长度限制2~30', trigger: 'blur'}],
  enableStatus: [{required: true, message: '请选择启用状态', trigger: 'blur'}],
  remark: [{min: 2, max: 255, message: '长度限制2~255', trigger: 'blur'}]
});
// 是否显示外链选择按钮
const dialogFormVisible = ref<boolean>(false);
const isShowTable = ref<boolean>(true);
const multipleSelection = ref<any[]>([]);
const enableStatusList = ref<any>([
  {label: '启用', value: 0},
  {label: '关闭', value: 1}
]);

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
  editForm.value = {enableStatus: 1};
}

const onRemove = (row: DictionaryModel): void => {
  ElMessageBox.confirm('确认删除该字典?', '提示', {
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
  const param = searchObj.value;
  reqCommonFeedback(listByTree(param), (data: any) => {
    records.value = data;
    loading.value = false;
  });
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

const handleChange = (data: any) => {
  if (!data) {
    editForm.value.parentId = '0';
    return;
  }
  editForm.value.parentId = data[data.length - 1] ? data[data.length - 1] : '0';
}

const onExpandAll = () => {
  isShowTable.value = false;
  isExpandAll.value = !isExpandAll.value;
  nextTick(() => {
    isShowTable.value = true;
  });
}

const resetSearchForm = () => {
  searchObj.value = {};
}

const handleSelectionChange = (arr: any) => {
  multipleSelection.value = arr;
}

const onDeleteBatch = () => {
  let ids: string[] = [];
  multipleSelection.value.map((item, index) => ids.push(item.id));
  ElMessageBox.confirm('确认删除所选字典?', '提示', {
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

const getConfirm: any = (status: number, type: number) => {
  let obj = {color: '', text: ''};
  switch (status) {
    case 0:
      obj = {color: 'success', text: '是'};
      break;
    case 1:
      obj = {color: 'info', text: '否'};
      break;
  }
  if (type === 0) {
    return obj.color;
  } else {
    return obj.text;
  }
}
</script>

<style scoped></style>
