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
      <el-button type="primary" :icon="Plus" @click="onAdd">上传文件</el-button>
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
        <el-table-column width="100" prop="isShare" label="是否共享">
          <template #default="scope">
            <el-switch :model-value="scope.row.isShare"
                       :active-value="1"
                       :inactive-value="0"
                       @change="doShare(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column width="200" prop="createTime" label="上传时间"/>
        <!-- 单行操作 -->
        <el-table-column fixed="right" width="220" label="操作">
          <template #default="scope">
            <el-button size="small" link>
              <el-link size="small" :icon="Download" :href="`api/system/file/download/${scope.row.id}`" target="_blank">
                下载
              </el-link>
            </el-button>
            <el-button size="small" :icon="DeleteFilled" plain type="danger" @click="onRemove(scope.row)">
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
      <el-dialog v-model="dialogFormVisible" title="上传文件" @close="onCloseDialog">
        <el-upload
            v-model:file-list="fileList"
            action="api/system/file/upload?isSave=1"
            multiple
            :on-preview="handlePreview"
            :on-remove="handleRemove"
            :before-remove="beforeRemove"
        >
          <el-button type="primary">点击上传</el-button>
          <template #tip>
            <div class="el-upload__tip">
              文件大小不能超过500MB
            </div>
          </template>
        </el-upload>
      </el-dialog>
    </template>
  </table-manage>
</template>

<script setup lang="ts">
import {onMounted, ref, nextTick} from "vue";
import {deleteBatch, listByPage, update} from "@/api/system/sys-file-api";
import {reqCommonFeedback, reqSuccessFeedback} from "@/api/ApiFeedback";
import TableManage from "@/components/container/TableManage.vue";
import {ElMessageBox, UploadProps, UploadUserFile} from "element-plus";
import {DeleteFilled, Download, Plus, Search, RefreshRight} from "@element-plus/icons-vue";
import unitUtil from "@/utils/unit-util";
import {saveAs} from "file-saver";

const pageParam = ref<PageParam>({pageNo: 1, pageSize: 10, searchObject: {}});
// 表单参数
const editForm = ref<SysFileModel>({});
// 加载进度
const loading = ref<boolean>(true);
const multipleSelection = ref<SysFileModel[]>();
const dialogFormVisible = ref<boolean>(false);
const pageVo = ref<PageVO>({pageNo: 1, pageSize: 10, total: 0, records: []});

// 初始化数据
onMounted(() => {
  loadTableData();
});

const onAdd = () => {
  dialogFormVisible.value = true;
  editForm.value = {};
}

const onRemove = (row: SysFileModel): void => {
  ElMessageBox.confirm('确认删除文件?', '提示', {
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
  if (pageParam.value.searchObject.createTimeRange) {
    pageParam.value.searchObject.beginTime = pageParam.value.searchObject.createTimeRange[0];
    pageParam.value.searchObject.endTime = pageParam.value.searchObject.createTimeRange[1];
  }
  let param = {
    pageNo: pageParam.value.pageNo,
    pageSize: pageParam.value.pageSize,
    sysFile: pageParam.value.searchObject
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
  ElMessageBox.confirm('确认删除这些文件?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    reqSuccessFeedback(deleteBatch(ids), '删除成功', () => {
      loadTableData();
    });
  });
}

const fileList = ref<UploadUserFile[]>([]);

const handleRemove: UploadProps['onRemove'] = (file, uploadFiles) => {
  console.log(file, uploadFiles)
}

const handlePreview: UploadProps['onPreview'] = (uploadFile) => {
  console.log(uploadFile)
}

const beforeRemove: UploadProps['beforeRemove'] = (uploadFile, uploadFiles) => {
  return ElMessageBox.confirm(
      `取消上传文件[${uploadFile.name}] ?`
  ).then(
      () => true,
      () => false
  )
}

const onCloseDialog = () => {
  loadTableData();
}

const handleSelectionChange = (row: SysFileModel[]) => {
  multipleSelection.value = row;
}

const doShare = (row: SysFileModel) => {
  reqSuccessFeedback(update({
    id: row.id,
    isShare: row.isShare === 0 ? 1 : 0
  }), '更新成功', () => {
    loadTableData();
  });
}

</script>

<style scoped></style>