<template>
  <table-manage>
    <template #search>
      <el-form-item label="角色名称">
        <el-input placeholder="角色名称" v-model="pageParam.searchObject.roleName"/>
      </el-form-item>
      <el-form-item label="角色标识">
        <el-input placeholder="角色标识" v-model="pageParam.searchObject.roleKey"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadTableData" :icon="Search">搜索</el-button>
        <el-button @click="onResetSearchForm" :icon="Refresh">重置</el-button>
      </el-form-item>
    </template>

    <template #operate>
      <el-button type="primary" @click="onCreate" :icon="Plus">添加角色</el-button>
      <el-button plain type="danger" @click="onDeleteBatch" :icon="DeleteFilled">批量删除</el-button>
    </template>

    <template #default>
      <el-table v-loading="loading" :data="pageVo.records" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"/>
        <el-table-column prop="roleName" label="角色名称"/>
        <el-table-column prop="roleKey" label="角色标识"/>
        <el-table-column prop="sort" label="排序" sortable/>
        <el-table-column label="权限操作">
          <template #default="scope">
            <el-button size="small" @click="showDialogMenu(scope.row, 1)">赋予菜单</el-button>
            <el-button size="small" @click="showDialogMenu(scope.row, 0)">授予权限</el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <template #page>
      <el-pagination background layout="total, sizes, prev, pager, next, jumper"
                     :total="pageVo.total"
                     v-model:page-count="pageVo.pageNo"
                     v-model:page-size="pageParam.pageSize"
                     :page-sizes=[5,10,15]
      />
    </template>

    <template #form>
      <!-- 权限操作 -->
      <el-dialog v-model="dialogMenuVisible" :title="dialogMenuStatus == 1 ? '赋予菜单' : '授予权限'" width="50%">
        <el-tree v-model:default-checked-keys="defaultSelectMenuIdList"
                 @check-change="checkChange"
                 :data="menuOptions"
                 show-checkbox
                 default-expand-all
                 node-key="id"
                 highlight-current
                 :props="defaultProps"
        />
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogMenuVisible = false">取消</el-button>
            <el-button type="primary" @click="dialogConfirm">确认</el-button>
          </span>
        </template>
      </el-dialog>

      <el-dialog v-model="dialogEditVisible">
        <el-form ref="sstFormRef" :model="editForm" label-width="100px">
          <el-form-item prop="roleName" label="角色名称">
            <el-input v-model="editForm.roleName"></el-input>
          </el-form-item>
          <el-form-item prop="roleKey" label="角色标识">
            <el-input v-model="editForm.roleKey"></el-input>
          </el-form-item>
          <el-form-item prop="sort" label="排序">
            <el-input type="number" v-model="editForm.sort"></el-input>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogEditVisible = false">取消</el-button>
            <el-button type="primary" @click="onUpdateFormConfirm(sstFormRef)">确认</el-button>
          </span>
        </template>
      </el-dialog>
    </template>
  </table-manage>
</template>

<script setup lang="ts">
import {onMounted, nextTick, reactive, ref} from "vue";
import {reqCommonFeedback, reqSuccessFeedback} from "@/api/ApiFeedback";
import roleApi, {grantPermissionsByRoleId} from "@/api/system/sys-role-api";
import {listByTreeAsRoleSelection, listByRoleId} from "@/api/system/sys-menu-api";
import TableManage from "@/components/container/TableManage.vue";
import {ElMessage, ElMessageBox, FormInstance} from "element-plus";
import {DeleteFilled, Plus, Refresh, Search} from "@element-plus/icons-vue";

// 树形选择框配置
const defaultProps = {
  value: 'id',
  label: 'menuName',
  children: 'children',
  checkStrictly: true
}

const multipleSelection = ref<any[]>([]);
// 授予 权限/菜单 对话框
const dialogMenuVisible = ref<boolean>(false);
// 授予 权限/菜单 对话框标题
const dialogMenuStatus = ref<number>(0);
// 默认已选id
const defaultSelectMenuIdList = ref<string[]>([]);
// 菜单选项
const menuOptions = ref<RoleModel[]>([]);
// 授予权限的行id
const selectId = ref<string>('');
// 表单参数
const editForm = ref<RoleModel>({roleName: '', roleKey: '', sort: 1});
// 分页参数
const pageParam = ref<PageParam>({pageNo: 1, pageSize: 15, searchObject: {}});
// api返回的分页数据
const pageVo = ref<PageVO>({pageNo: 1, pageSize: 15, total: 0, records: []});
// 加载进度
const loading = ref<boolean>(true);
// 表单校验规则
const rules = reactive({
  roleName: [{required: true, min: 2, max: 30, message: '长度限制2~30', trigger: 'blur'}],
  roleKey: [{required: true, min: 2, max: 255, message: '长度限制2~155', trigger: 'blur'}]
});
const dialogEditVisible = ref<boolean>(false);
const sstFormRef = ref<FormInstance>();

// 初始化数据
onMounted(() => {
  loadTableData();
});

const loadTableData = () => {
  // 渲染数据
  if (!loading.value) loading.value = true;
  let param = {
    pageNo: pageParam.value.pageNo,
    pageSize: pageParam.value.pageSize,
    sysRole: {roleName: pageParam.value.searchObject.roleName, roleKey: pageParam.value.searchObject.roleKey}
  };
  reqCommonFeedback(roleApi.listByPage(param), (data: any) => {
    pageVo.value = data;
    loading.value = false;
  });
}

const onUpdateFormConfirm = (formEl: any): void => {
  formEl.validate((valid: boolean) => {
    if (valid) {
      if (!editForm.value.id) {
        // 新增
        reqSuccessFeedback(roleApi.add(editForm.value), '新增成功', () => {
          loadTableData();
          dialogEditVisible.value = false;
        });
      } else {
        // 修改
        reqSuccessFeedback(roleApi.update(editForm.value), '修改成功', () => {
          loadTableData();
          dialogEditVisible.value = false;
        });
      }
    }
  });
}

/**
 * 确认修改权限
 */
const dialogConfirm = () => {
  let param: any[] = [];
  defaultSelectMenuIdList.value.forEach((item: string) => {
    param.push({roleId: selectId.value, menuId: item});
  });
  reqSuccessFeedback(grantPermissionsByRoleId(param), '修改成功', () => {
    dialogMenuVisible.value = false;
  });
}

/**
 * 显示授予菜单对话框
 * @param row 行数据
 * @param t 是否菜单：0是 1否
 */
const showDialogMenu = (row: any, t: number) => {
  defaultSelectMenuIdList.value = [];
  dialogMenuStatus.value = t;
  dialogMenuVisible.value = true;
  selectId.value = row.id;
  getMenus(t, () => {
    getMenusByRoleId(row.id);
  });
}

/**
 * 获取权限列表
 */
const getMenus = (t: number, callback: Function) => {
  let param: any = {
    isMenu: t
  }
  reqCommonFeedback(listByTreeAsRoleSelection(param), (data: any) => {
    menuOptions.value = data;
    nextTick(callback());
  });
}

/**
 * 根据角色id获取菜单id列表
 * @param roleId 角色id
 */
const getMenusByRoleId = (roleId: string) => {
  let selectIdList: string[] = [];
  reqCommonFeedback(listByRoleId(roleId), (data: any) => {
    data.forEach((item: any) => {
      selectIdList.push(item.id);
    });
    defaultSelectMenuIdList.value = selectIdList;
  });
}

/**
 * 共三个参数，依次为：传递给 data 属性的数组中该节点所对应的对象、节点本身是否被选中、节点的子树中是否有被选中的节点
 * @param v1
 * @param v2
 * @param v3
 */
const checkChange = (v1: any, v2: any, v3: any) => {
  if (v2) {
    // 选中，没有重复项
    if (defaultSelectMenuIdList.value.indexOf(v1.id) == -1) {
      defaultSelectMenuIdList.value.push(v1.id);
    }
  } else {
    // 未选中
    let index = defaultSelectMenuIdList.value.indexOf(v1.id);
    if (index != -1 && !v3) {
      defaultSelectMenuIdList.value.splice(index, 1);
    }
  }
}

const onResetSearchForm = () => {
  pageParam.value.searchObject = {};
}

const onCreate = () => {
  dialogEditVisible.value = true;
}

const onDeleteBatch = () => {
  let ids: string[] = [];
  multipleSelection.value.map((item) => {
    ids.push(item.id);
  });
  ElMessageBox.confirm('确认删除所选角色?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    reqCommonFeedback(roleApi.deleteBatch(ids), () => {
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