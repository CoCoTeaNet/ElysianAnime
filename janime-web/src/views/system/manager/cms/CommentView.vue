<template>
  <div>
    <table-manage>
      <template v-slot:search>
        <el-form-item label="时间">
          <el-date-picker v-model="page.searchObject.createTimeRange" type="daterange" range-separator="~"
                          start-placeholder="开始日期" end-placeholder="结束日期"/>
        </el-form-item>
        <el-form-item label="评论用户">
          <el-input placeholder="评论用户" v-model="page.searchObject.createBy"/>
        </el-form-item>
        <el-form-item label="评论内容">
          <el-input placeholder="评论内容" v-model="page.searchObject.content"/>
        </el-form-item>
        <el-form-item label="评论类型">
          <el-select placeholder="评论类型" v-model="page.searchObject.replyType">
            <el-option v-for="i in replyTypeList" :label="i.label" :value="i.value"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadTableData">搜索</el-button>
          <el-button @click="onResetSearchForm">重置</el-button>
        </el-form-item>
      </template>
      <template v-slot:operate>
        <el-button plain type="danger" @click="onDeleteBatch">批量删除</el-button>
      </template>
      <template v-slot:default>
        <el-table v-loading="loading" :data="tableData" style="width: 100%" @selection-change="handleSelectionChange"
                  max-height="700px">
          <el-table-column type="selection" width="55"/>
          <el-table-column prop="createBy" label="评论用户" width="200"/>
          <el-table-column prop="content" label="评论内容" width="400"/>
          <el-table-column prop="replyType" label="评论类型" width="100">
            <template #default="scope">
              <el-tag :type="getReplyType(scope.row.replyType, 0)">
                {{ getReplyType(scope.row.replyType, 1) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="评论时间" min-width="220"/>
          <el-table-column fixed="right" label="操作" width="240">
            <template #default="scope">
              <el-button plain size="small" type="danger" @click="onDelete(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>
      <template v-slot:page>
        <el-pagination background layout="total, sizes, prev, pager, next, jumper"
                       :total="total" :page-size="page.pageSize" :page-sizes=[15,25,35]
                       @current-change="onPageChange" @size-change="onSizeChange"/>
      </template>
    </table-manage>
  </div>
</template>

<script setup lang="ts">
import TableManage from '@/components/container/TableManage.vue';
import {nextTick, onMounted, ref} from "vue";
import {listByPage, deleteBatch} from '@/api/blog/blo-comment-api';
import {reqCommonFeedback} from "@/api/ApiFeedback";
import {ElMessage, ElMessageBox} from 'element-plus';

const loading = ref<boolean>(true);
const page = ref<PageParam>({pageNo: 1, pageSize: 15, searchObject: {createBy: ''}});
const tableData = ref();
const total = ref<number>(0);
const multipleSelection = ref<any[]>([]);
const editType = ref<string>('create');
const replyTypeList = ref<any>([
  {label: '文章', value: 0},
  {label: '用户', value: 1}
]);

onMounted(() => {
  loadTableData();
});

/**
 * 发布状态;1正常 2审核中 3审核不通过 4冻结 5保存
 * @param status
 * @param type
 */
const getReplyType: any = (status: number, type: number) => {
  let obj = {color: '', text: ''};
  switch (status) {
    case 0:
      obj = {color: 'success', text: '评论文章'};
      break;
    case 1:
      obj = {color: 'info', text: '评论用户'};
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
  let searchForm = page.value.searchObject;
  if (searchForm.createTimeRange) {
    searchForm.beginTime = searchForm.createTimeRange[0];
    searchForm.endTime = searchForm.createTimeRange[1];
  }
  let param = {pageNo: page.value.pageNo, pageSize: page.value.pageSize, commentVo: searchForm};
  reqCommonFeedback(listByPage(param), (data: any) => {
    tableData.value = data.rows;
    total.value = data.recordCount;
    loading.value = false;
  });
}

const onResetSearchForm = () => {
  page.value.searchObject.createTimeRange = '';
  page.value.searchObject.content = '';
  page.value.searchObject.createBy = '';
  page.value.searchObject.replyType = '';
  page.value.searchObject.beginTime = null;
  page.value.searchObject.endTime = null;
}

const onDelete = (id: string) => {
  ElMessageBox.confirm('确认删除此文章?', 'Warning', {
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
  ElMessageBox.confirm('确认删除所选择的评论?', 'Warning', {
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