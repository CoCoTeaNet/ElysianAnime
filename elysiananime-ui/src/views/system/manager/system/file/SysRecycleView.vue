<template>
  <table-manage>
    <!-- 表格操作 -->
    <template #search>
      <el-form-item label="创建时间">
        <el-date-picker v-model="pageParam.searchObject.createTimeRange"
                        type="daterange" range-separator="~"
                        start-placeholder="开始日期"
                        end-placeholder="结束日期"/>
      </el-form-item>
      <el-form-item label="文件名称">
        <el-input placeholder="文件名称" v-model:model-value="pageParam.searchObject.fileName"/>
      </el-form-item>
      <el-form-item label="文件后缀">
        <el-input placeholder="文件后缀" v-model:model-value="pageParam.searchObject.fileSuffix"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="loadTableData">搜索</el-button>
        <el-button :icon="RefreshRight" @click="resetSearchForm">重置</el-button>
      </el-form-item>
    </template>

    <template #operate>
      <el-button type="success" :icon="RefreshLeft" @click="onRecoveryBatch">批量恢复</el-button>
      <el-button plain type="danger" :icon="DeleteFilled" @click="onRemoveBatch">批量删除</el-button>
    </template>

    <!-- 表格视图 -->
    <template #default>
      <el-table stripe
                row-key="id"
                :data="pageVo.records"
                v-loading="loading"
                @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="fileName" label="文件名称" show-overflow-tooltip/>
        <el-table-column width="150" prop="fileSuffix" label="文件后缀"/>
        <el-table-column width="150" prop="fileSize" label="文件大小">
          <template #default="scope">
            <span>{{unitUtil.memoryCalculate(scope.row.fileSize)}}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createBy" label="创建人"/>
        <el-table-column width="200" prop="createTime" label="创建时间"/>
        <el-table-column prop="updateBy" label="更新人"/>
        <el-table-column width="200" prop="updateTime" label="更新时间"/>
        <!-- 单行操作 -->
        <el-table-column fixed="right" width="200" label="操作">
          <template #default="scope">
            <el-button size="small" type="success" plain :icon="RefreshLeft" @click="onRecovery(scope.row)">
              恢复
            </el-button>
            <el-button size="small" type="danger" plain :icon="DeleteFilled" @click="onRemove(scope.row)">
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
  </table-manage>
</template>

<script setup lang="ts">
import {onMounted, ref, nextTick} from "vue";
import {recycleBinDeleteBatch, recycleBinPage, recoveryBatch} from "@/api/system/sys-file-api";
import {reqCommonFeedback, reqSuccessFeedback} from "@/api/ApiFeedback";
import TableManage from "@/components/container/TableManage.vue";
import {ElMessageBox, ElMessage} from "element-plus";
import {DeleteFilled, Download, RefreshLeft, Search, RefreshRight} from "@element-plus/icons-vue";
import unitUtil from "@/utils/unit-util";

const pageParam = ref<PageParam>({pageNo: 1, pageSize: 10, searchObject: {}});
// 加载进度
const loading = ref<boolean>(true);
const multipleSelection = ref<SysFileModel[]>();
const pageVo = ref<PageVO>({pageNo: 1, pageSize: 10, total: 0, records: []});

// 初始化数据
onMounted(() => {
  loadTableData();
});

const onRemove = (row: SysFileModel): void => {
  ElMessageBox.confirm('确认彻底删除文件?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    reqSuccessFeedback(recycleBinDeleteBatch([row.id]), '删除成功', () => {
      loadTableData();
    });
  });
}

const loadTableData = (): void => {
  if (!loading.value) loading.value = true;
  if (pageParam.value.searchObject.createTimeRange) {
    pageParam.value.searchObject.beginTime = pageParam.value.searchObject.createTimeRange[0];
    pageParam.value.searchObject.endTime = pageParam.value.searchObject.createTimeRange[1];
  }
  let param = {
    pageNo: pageParam.value.pageNo,
    pageSize: pageParam.value.pageSize,
    sysFile: pageParam.value.searchObject
  };
  reqCommonFeedback(recycleBinPage(param), (data: any) => {
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

const resetSearchForm = () => {
  pageParam.value.searchObject = {};
}

const onRemoveBatch = () => {
  const ids:string[] = [];
  multipleSelection.value?.forEach(item => {
    if (item.id) {
      ids.push(item.id);
    }
  });
  ElMessageBox.confirm('确认彻底删除这些文件?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    reqSuccessFeedback(recycleBinDeleteBatch(ids), '删除成功', () => {
      loadTableData();
    });
  });
}

const handleSelectionChange = (row: SysFileModel[]) => {
  multipleSelection.value = row;
}

const onRecovery = (row: SysFileModel) => {
  ElMessageBox.confirm('确认恢复此文件?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    reqSuccessFeedback(recoveryBatch([row.id]), '已恢复', () => {
      loadTableData();
    });
  });
}

const onRecoveryBatch = () => {
  const ids:string[] = [];
  multipleSelection.value?.forEach(item => {
    if (item.id) {
      ids.push(item.id);
    }
  });
  ElMessageBox.confirm('确认恢复这些文件?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    reqSuccessFeedback(recoveryBatch(ids), '已恢复', () => {
      loadTableData();
    });
  });
}
</script>

<style scoped></style>