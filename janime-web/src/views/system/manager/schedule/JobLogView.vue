<template>
  <table-manage>
    <template #search>
      <el-form-item label="触发时间">
        <el-date-picker v-model="page.searchObject.triggerTimeRange" type="daterange" range-separator="~"
                        start-placeholder="开始时间" end-placeholder="结束时间"/>
      </el-form-item>
      <el-form-item label="执行结果">
        <el-select placeholder="执行结果" v-model="page.searchObject.exeResult">
          <el-option v-for="i in exeResultList" :label="i.label" :value="i.value"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadTableData">搜索</el-button>
        <el-button @click="onResetSearchForm">重置</el-button>
      </el-form-item>
    </template>

    <template #operate>
      <el-button plain type="danger" @click="onDeleteBatch">批量删除</el-button>
    </template>

    <template #default>
      <el-table v-loading="loading" :data="tableData" style="width: 100%" @selection-change="handleSelectionChange"
                max-height="700px">
        <el-table-column type="selection" width="55"/>
        <el-table-column prop="jobName" label="任务名称" width="220"/>
        <el-table-column prop="triggerBy" label="任务触发人"/>
        <el-table-column prop="triggerTime" label="触发时间" width="200"/>
        <el-table-column prop="spendTimeMillis" label="执行耗时(ms)" width="200"/>
        <el-table-column prop="exeResult" label="执行结果" width="150">
          <template #default="scope">
            <el-tag :type="getExeResultStatus(scope.row.exeResult, 0)">
              {{ getExeResultStatus(scope.row.exeResult, 1) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createBy" label="创建人" width="200"/>
        <el-table-column prop="createTime" label="创建时间" width="200"/>
        <el-table-column fixed="right" label="操作" width="120">
          <template #default="scope">
            <el-button plain size="small" type="danger" @click="onDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <template #page>
      <el-pagination background layout="total, sizes, prev, pager, next, jumper"
                     :total="total" :page-size="page.pageSize" :page-sizes=[15,25,35]
                     @current-change="onPageChange" @size-change="onSizeChange"/>
    </template>
  </table-manage>
</template>

<script setup lang="ts">
import TableManage from "@/components/container/TableManage.vue";
import {nextTick, onMounted, ref} from "vue";
import {reqCommonFeedback} from "@/api/ApiFeedback";
import {deleteBatch, listByPage} from "@/api/schedule/jobLog-api";
import {ElMessage, ElMessageBox} from "element-plus/es";

const loading = ref<boolean>(true);
const page = ref<PageParam>({pageNo: 1, pageSize: 15, searchObject: {triggerBy: '',exeResult:'',triggerTimeRange:null}});
const tableData = ref();
const total = ref<number>(0);
const multipleSelection = ref<any[]>([]);
const exeResultList = ref<any>([
  {label: '失败', value: 0},
  {label: '成功', value: 1}
]);

onMounted(() => {
  loadTableData();
});

const getExeResultStatus: any = (status: number, type: number) => {
  let obj = {color: '', text: ''};
  switch (status) {
    case 0:
      obj = {color: 'danger', text: '失败'};
      break;
    case 1:
      obj = {color: 'success', text: '成功'};
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
  let param = {
    pageNo: page.value.pageNo,
    pageSize: page.value.pageSize,
    scheduleJobLogVO: page.value.searchObject,
    triggerTimeRange: page.value.searchObject.triggerTimeRange
  };
  reqCommonFeedback(listByPage(param), (data: any) => {
    tableData.value = data.rows;
    total.value = data.recordCount;
    loading.value = false;
  });
}

const onResetSearchForm = () => {
  page.value.searchObject.triggerBy = '';
  page.value.searchObject.exeResult = null;
  page.value.searchObject.triggerTimeRange = null;
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