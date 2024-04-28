<template>
  <table-manage>
    <template #search>
      <el-form-item label="任务标题">
        <el-input placeholder="标题" v-model="page.searchObject.name"/>
      </el-form-item>
      <el-form-item label="Cron表达式">
        <el-input placeholder="例：1-2 * * * * ?" v-model="page.searchObject.cornExpression"/>
      </el-form-item>
      <el-form-item label="任务对应类名">
        <el-input placeholder="任务对应类名" v-model="page.searchObject.className"/>
      </el-form-item>
      <el-form-item label="方法名">
        <el-input placeholder="方法名" v-model="page.searchObject.methodName"/>
      </el-form-item>
      <el-form-item label="配置类型">
        <el-select placeholder="选择类型" v-model="page.searchObject.type">
          <el-option v-for="i in typeList" :label="i.label" :value="i.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="是否启用">
        <el-select placeholder="选择启用状态" v-model="page.searchObject.active">
          <el-option v-for="i in activeList" :label="i.label" :value="i.value"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadTableData">搜索</el-button>
        <el-button @click="onResetSearchForm">重置</el-button>
      </el-form-item>
    </template>

    <template #operate>
      <el-button plain @click="onCreate">添加任务</el-button>
      <el-button plain type="danger" @click="onDeleteBatch">批量删除</el-button>
    </template>

    <template #default>
      <el-table v-loading="loading" :data="tableData" style="width: 100%" @selection-change="handleSelectionChange"
                max-height="700px">
        <el-table-column type="selection" width="55"/>
        <el-table-column prop="name" label="任务名称" width="200"/>
        <el-table-column prop="className" label="任务对应类名" width="200"/>
        <el-table-column prop="methodName" label="方法名" width="200"/>
        <el-table-column prop="cornExpression" label="Cron表达式" width="200"/>
        <el-table-column prop="parameters" label="参数JSON对象" width="300"/>
        <el-table-column prop="description" label="任务描述" width="300"/>
        <el-table-column prop="type" label="配置类型" width="100">
          <template #default="scope">
            <el-tag :type="getTypeListStatus(scope.row.type, 0)">
              {{ getTypeListStatus(scope.row.type, 1) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="active" label="是否启用" width="100">
          <template #default="scope">
            <el-tag :type="getActiveListStatus(scope.row.active, 0)">
              {{ getActiveListStatus(scope.row.active, 1) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="nextExeTime" label="下一次执行时间" min-width="180"/>
        <el-table-column fixed="right" label="操作" width="240">
          <template #default="scope">
            <el-button link @click="onEdit(scope.row)">编辑</el-button>
            <el-button link type="primary" @click="onExecute(scope.row)">执行</el-button>
            <el-button link type="danger" @click="onDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="序号" width="80"/>
      </el-table>
    </template>

    <template #page>
      <el-pagination background layout="total, sizes, prev, pager, next, jumper"
                     :total="total" :page-size="page.pageSize" :page-sizes=[15,25,35]
                     @current-change="onPageChange" @size-change="onSizeChange"/>
    </template>

    <template #form>
      <add-job :edit-type="editType" :job="editRow" v-model:show="showAddDialog" @onConfirm="loadTableData"/>
    </template>
  </table-manage>
</template>

<script setup lang="ts">
import TableManage from "@/components/container/TableManage.vue";
import {nextTick, onMounted, ref} from "vue";
import {reqCommonFeedback} from "@/api/ApiFeedback";
import {deleteBatch, listByPage, execute, queryProgress} from "@/api/schedule/job-api";
import {ElMessage, ElMessageBox, ElNotification} from "element-plus/es";
import AddJob from "@/views/system/manager/schedule/modules/AddJob.vue";

const loading = ref<boolean>(true);
const page = ref<PageParam>({pageNo: 1, pageSize: 15, searchObject: {name: ''}});
const tableData = ref();
const total = ref<number>(0);
const multipleSelection = ref<any[]>([]);
const editType = ref<string>('create');
const editRow = ref<JobModel>();
const showAddDialog = ref<boolean>(false);
const typeList = ref<any>([
  {label: '类模式', value: 0},
  {label: '函数模式', value: 1}
]);
const activeList = ref<any>([
  {label: '未启用', value: 0},
  {label: '启用', value: 1}
]);

onMounted(() => {
  loadTableData();
});

const getActiveListStatus: any = (status: number, type: number) => {
  let obj = {color: '', text: ''};
  switch (status) {
    case 0:
      obj = {color: 'info', text: '未启用'};
      break;
    case 1:
      obj = {color: 'success', text: '启用'};
      break;
  }
  if (type === 0) {
    return obj.color;
  } else {
    return obj.text;
  }
}

const getTypeListStatus: any = (status: number, type: number) => {
  let obj = {color: '', text: ''};
  switch (status) {
    case 0:
      obj = {color: 'success', text: '类模式'};
      break;
    case 1:
      obj = {color: 'info', text: '函数模式'};
      break;
  }
  if (type === 0) {
    return obj.color;
  } else {
    return obj.text;
  }
}

const onPageChange = (current: number) => {
  page.value.pageNo = current;
  nextTick(() => loadTableData());
}

const onSizeChange = (size: number) => {
  page.value.pageSize = size;
  nextTick(() => loadTableData());
}

const loadTableData = () => {
  if (!loading.value) loading.value = true;
  let param = {pageNo: page.value.pageNo, pageSize: page.value.pageSize, scheduleJobVO: page.value.searchObject};
  reqCommonFeedback(listByPage(param), (data: any) => {
    tableData.value = data.rows;
    total.value = data.recordCount;
    loading.value = false;
  });
}

const onResetSearchForm = () => {
  page.value.searchObject.name = '';
  page.value.searchObject.cornExpression = '';
  page.value.searchObject.className = '';
  page.value.searchObject.methodName = '';
  page.value.searchObject.type = null;
  page.value.searchObject.active = null;
}

const onCreate = () => {
  showAddDialog.value = true;
  editType.value = 'create';
}

const onEdit = (row: JobModel) => {
  showAddDialog.value = true;
  editType.value = 'update';
  editRow.value = row;
}

const onExecute = (row: JobModel) => {
  reqCommonFeedback(execute({id: row.id}), (data: any) => {
    ElNotification({type: 'info', message: `${row.name} | 开始执行...`});
    const timer = setInterval(() => {
      reqCommonFeedback(queryProgress({id: data}), () => {
        ElNotification({type: 'success', message: `${row.name} | 执行成功!`});
        clearInterval(timer);
      })
    }, 1000)
  });
}

const onDelete = (id: string) => {
  ElMessageBox.confirm('确认删除此任务?', 'Warning', {
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

const handleSelectionChange = (arr: any[]) => {
  multipleSelection.value = arr;
}

const onDeleteBatch = () => {
  let ids: string[] = [];
  multipleSelection.value.map((item, index) => {
    ids.push(item.id);
  });
  ElMessageBox.confirm('确认删除所选择的任务?', 'Warning', {
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