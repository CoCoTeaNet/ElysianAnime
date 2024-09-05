<template>
  <table-manage>
    <!-- 表格操作 -->
    <template #search>
      <el-form-item label="原名:">
        <el-input v-model="pageParam.nameOriginal" placeholder="けいおん！"></el-input>
      </el-form-item>
      <el-form-item label="中文名:">
        <el-input v-model="pageParam.nameCn" placeholder="轻音少女"></el-input>
      </el-form-item>
      <el-form-item label="观看状态:">
        <el-select placeholder="选择观看状态" clearable style="width: 200px" v-model="pageParam.readStatus">
          <el-option v-for="i in acgUserOpusTypes.getReadStatusList()" :label="i.label" :value="i.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="是否分享:">
        <el-select placeholder="选择分享" clearable style="width: 200px" v-model="pageParam.isShare">
          <el-option v-for="i in commonTypes.getIsList()" :label="i.label" :value="i.value"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button :icon="Search" type="primary" @click="loadTableData">搜索</el-button>
        <el-button :icon="RefreshRight" @click="resetSearchForm">重置</el-button>
      </el-form-item>
    </template>

    <!-- 表格视图 -->
    <template #default>
      <el-table stripe row-key="id" :data="pageVo.records" v-loading="loading">
        <el-table-column prop="coverUrl" width="155" label="封面地址">
          <template #default="scope">
            <el-image style="width: 100px; height: 144px"
                      :src="`api/anime/opus/cover?resName=${scope.row.coverUrl}`"
                      fit="fill"/>
          </template>
        </el-table-column>
        <el-table-column width="200" prop="nameOriginal" label="原名"/>
        <el-table-column width="200" prop="nameCn" label="中文名">
          <template #default="scope">
            <div>
              <el-link :icon="VideoCameraFilled"
                       v-if="scope.row.hasResource == 1"
                       @click="toPlayerView(scope.row.id)"
                       style="color: var(--el-color-primary)">
                <span style="font-weight: 600;font-size: 18px">{{ scope.row.nameCn }}</span>
              </el-link>
              <div v-else>
                <span>{{ scope.row.nameCn }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column width="100" prop="readStatus" label="观看状态">
          <template #default="scope">
            {{ acgUserOpusTypes.getReadStatusCn(scope.row.readStatus) }}
          </template>
        </el-table-column>
        <el-table-column width="100" prop="readingNum" label="观看集数"/>
        <el-table-column width="200" prop="readingTime" label="播放进度">
          <template #default="scope">
            {{
              dayjs.duration(scope.row.readingTime * 1000).hours() + '时' +
              dayjs.duration(scope.row.readingTime * 1000).minutes() + '分' +
              dayjs.duration(scope.row.readingTime * 1000).seconds() + '秒'
            }}
          </template>
        </el-table-column>
        <el-table-column width="300" prop="resourceUrl" label="资源地址">
          <template #default="scope">
            <el-link :href="scope.row.resourceUrl">{{ scope.row.resourceUrl }}</el-link>
          </template>
        </el-table-column>
        <el-table-column width="200" prop="createTime" label="关注时间"/>
        <el-table-column width="100" prop="isShare" label="是否分享">
          <template #default="scope">
            {{ commonTypes.getIsCn(scope.row.isShare) }}
          </template>
        </el-table-column>
        <!-- 单行操作 -->
        <el-table-column fixed="right" width="200" label="操作">
          <template #default="scope">
            <el-button size="small" :icon="Edit" @click="onEdit(scope.row)">编辑</el-button>
            <el-button size="small" plain type="warning" @click="onCancelFollow(scope.row.opusId)">取消追番</el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <template #page>
      <el-pagination background layout="total, sizes, prev, pager, next, jumper"
                     v-model:current-page="pageParam.pageNo"
                     v-model:page-size="pageParam.pageSize"
                     :total="pageVo.total"
                     :page-sizes=[5,10,15]
                     @size-change="loadTableData"
      />
    </template>

    <!-- 编辑对话框 -->
    <template #form>
      <el-dialog v-model="dialogFormVisible" title="编辑追番信息">
        <el-form ref="sttFormRef" label-width="120px" :model="editForm" :rules="rules">
          <el-form-item prop="resourceUrl" label="资源地址">
            <el-input v-model="editForm.resourceUrl"></el-input>
          </el-form-item>
          <el-form-item prop="readingNum" label="正在的播放集数">
            <el-input v-model="editForm.readingNum"></el-input>
          </el-form-item>
          <el-form-item prop="readingTime" label="正在播放的时长">
            <el-input v-model="editForm.readingTime"></el-input>
          </el-form-item>
          <el-form-item prop="readStatus" label="观看状态">
            <el-radio-group v-model="editForm.readStatus">
              <el-radio :label="0">未看</el-radio>
              <el-radio :label="1">已看</el-radio>
              <el-radio :label="2">在看</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item prop="isShare" label="是否分享">
            <el-radio-group v-model="editForm.isShare">
              <el-radio :label="0">否</el-radio>
              <el-radio :label="1">是</el-radio>
            </el-radio-group>
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
import {onMounted, ref, reactive, watch} from "vue";
import {follow, listByUser, update} from "@/api/anime/ani-user-opus-api";
import {reqCommonFeedback, reqSuccessFeedback} from "@/api/ApiFeedback";
import TableManage from "@/components/container/TableManage.vue";
import {ElForm} from "element-plus/es";
import {ElMessage, ElMessageBox, dayjs} from "element-plus";
import {Edit, Search, RefreshRight, VideoCameraFilled} from "@element-plus/icons-vue";
import acgUserOpusTypes from "@/types/acg-user-opus-types";
import commonTypes from "@/types/common-types";
import {router} from "@/router";
import {useRoute} from "vue-router";
import duration from 'dayjs/plugin/duration';

dayjs.extend(duration);

const route = useRoute();
type FormInstance = InstanceType<typeof ElForm>
const sttFormRef = ref<FormInstance>();

const pageParam = ref<any>({
  pageNo: 1,
  pageSize: 5,
  nameOriginal: '',
  nameCn: '',
  readStatus: null,
  isShare: null
});
// 表单参数
const editForm = ref<AcgUserOpusModel>({});
// 加载进度
const loading = ref<boolean>(true);
// 表单校验规则
const rules = reactive({
  updateNo: [{required: true, min: 2, max: 30, message: '长度限制2~20', trigger: 'blur'}],
});
const dialogFormVisible = ref<boolean>(false);
const pageVo = ref<PageVO>({pageNo: 1, pageSize: 5, total: 0, records: []});

watch(
    () => pageParam.value.pageNo,
    (val : number) => loadTableData()
)

// 初始化数据
onMounted(() => {
  if (route.query.readStatus) {
    let readStatus: any = route.query.readStatus;
    pageParam.value.readStatus = parseInt(readStatus);
  }
  loadTableData();
});

const onEdit = (row: AcgUserOpusModel): void => {
  editForm.value = row;
  dialogFormVisible.value = true;
}

const loadTableData = (): void => {
  if (!loading.value) loading.value = true;
  reqCommonFeedback(listByUser(pageParam.value), (data: any) => {
    pageVo.value = data;
    loading.value = false;
  });
}

const doUpdate = (formEl: any): void => {
  formEl.validate((valid: any) => {
    if (valid) {
      reqSuccessFeedback(update(editForm.value), '修改成功', () => {
        loadTableData();
        dialogFormVisible.value = false;
      });
    }
  });
}

const resetSearchForm = () => {
  pageParam.value = {
    pageNo: 1,
    pageSize: 5,
    nameOriginal: '',
    nameCn: '',
    readStatus: null,
    isShare: null
  };
}

const onCancelFollow = (opusId: string) => {
  ElMessageBox.confirm('取消追番吗?', '提示', {
        confirmButtonText: '是的',
        cancelButtonText: '点错了~',
        type: 'warning',
      }
  ).then(() => {
    reqCommonFeedback(follow(opusId), () => {
      ElMessage({type: 'success', message: '操作成功'});
      loadTableData();
    });
  });
}

const toPlayerView = (id: string) => {
  router.push({name: 'AcgVideo', query: {'opusId': id, 'num': 1}})
}
</script>

<style scoped></style>