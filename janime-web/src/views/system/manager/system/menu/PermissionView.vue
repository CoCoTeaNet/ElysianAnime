<template>
  <table-manage>
    <!-- 表格操作 -->
    <template #search>
      <el-form>
        <el-form-item label="权限名称">
          <el-input placeholder="权限名称" v-model:model-value="searchObj.menuName"/>
        </el-form-item>
      </el-form>
      <el-button type="primary" @click="loadTableData" :icon="Search">搜索</el-button>
      <el-button @click="resetSearchForm" :icon="Refresh">重置</el-button>
      <el-button @click="onExpandAll">
        <el-icon>
          <arrow-right-bold v-if="!isExpandAll"/>
          <arrow-down-bold v-else/>
        </el-icon>
        {{ isExpandAll ? '收起' : '展开' }}
      </el-button>
    </template>

    <template #operate>
      <el-button type="primary" @click="onAdd" :icon="Plus">添加权限</el-button>
      <el-button plain type="danger" @click="onDeleteBatch" :icon="DeleteFilled">批量删除</el-button>
    </template>

    <!-- 表格视图 -->
    <template #default>
      <el-table v-if="isShowTable" stripe row-key="id" :data="records" v-model:default-expand-all="isExpandAll"
                @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="80"/>
        <el-table-column prop="menuName" width="300" label="权限名称"/>
        <el-table-column prop="permissionCode" width="400" label="权限编号"/>
        <el-table-column prop="sort" width="200" label="显示顺序" sortable/>
        <el-table-column prop="createTime" width="200" label="创建时间"/>
        <el-table-column prop="updateTime" width="200" label="更新时间"/>
        <!-- 单行操作 -->
        <el-table-column fixed="right" width="200" label="操作">
          <template #default="scope">
            <el-button size="small" @click="onEdit(scope.row)" :icon="Edit">编辑</el-button>
            <el-button size="small" plain type="danger" @click="onRemove(scope.row)" :icon="DeleteFilled">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <!-- 编辑对话框 -->
    <template #form>
      <el-dialog v-model="dialogFormVisible" :title="`${editForm.id? '编辑' : '添加'}权限`">
        <el-form ref="sttFormRef" label-width="120px" :model="editForm" :rules="rules">
          <el-form-item prop="menuName" label="权限名称">
            <el-input v-model="editForm.menuName"></el-input>
          </el-form-item>
          <el-form-item prop="permissionCode" label="权限编号">
            <el-input v-model="editForm.permissionCode"></el-input>
          </el-form-item>
          <el-form-item prop="menuType" label="权限类型">
            <el-radio-group v-model="editForm.menuType">
              <el-radio :label="0">目录</el-radio>
              <el-radio :label="1">权限</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item prop="sort" label="显示顺序">
            <el-input v-model="editForm.sort" type="number"></el-input>
          </el-form-item>
          <el-form-item label="上级权限">
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
import {nextTick, onMounted, reactive, ref} from "vue";
import {listByTree, add, deleteBatch, update} from "@/api/system/sys-menu-api";
import {reqCommonFeedback, reqSuccessFeedback} from "@/api/ApiFeedback";
import TableManage from "@/components/container/TableManage.vue";
import {ElForm} from "element-plus/es";
import {ElMessage, ElMessageBox} from "element-plus";
import listUtil from "@/utils/list-util";
import {DeleteFilled, Edit, Plus, Refresh, Search} from "@element-plus/icons-vue";

type FormInstance = InstanceType<typeof ElForm>
const sttFormRef = ref<FormInstance>();

// 级联选择框配置
const defaultProps = {
  value: 'id',
  label: 'menuName',
  children: 'children',
  checkStrictly: true
}

const records = ref<any>();
const searchObj = ref<MenuModel>({});
const isExpandAll = ref<boolean>(false);
// 表单参数
const editForm = ref<MenuModel>({});
// 加载进度
const loading = ref<boolean>(true);
// 表单校验规则
const rules = reactive({
  menuName: [{required: true, min: 2, max: 30, message: '长度限制2~30', trigger: 'blur'}],
  permissionCode: [{min: 0, max: 60, message: '长度限制0~60', trigger: 'blur'}]
});
// 是否显示外链选择按钮
const dialogFormVisible = ref<boolean>(false);
const isShowTable = ref<boolean>(true);
const multipleSelection = ref<any[]>([]);

// 初始化数据
onMounted(() => {
  loadTableData();
});

const onEdit = (row: MenuModel): void => {
  editForm.value = row;
  dialogFormVisible.value = true;
}

const onAdd = () => {
  dialogFormVisible.value = true;
  editForm.value = {menuType: 1};
}

const onRemove = (row: MenuModel): void => {
  ElMessageBox.confirm('确认删除该权限?', '提示', {
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
    sysMenu: {
      isMenu: 0,
      menuName: searchObj.value.menuName
    }
  };
  reqCommonFeedback(listByTree(param), (data: any) => {
    listUtil.treeMap(data, (item: { disabled: boolean; menuType: number; }) => item.disabled = (item.menuType != 0));
    records.value = data;
    loading.value = false;
  });
}

const doUpdate = (formEl: any): void => {
  editForm.value.isMenu = 0;
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
  multipleSelection.value.map((item, index) => {
    ids.push(item.id);
  });
  ElMessageBox.confirm('确认删除所选权限?', '提示', {
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
</script>

<style scoped></style>