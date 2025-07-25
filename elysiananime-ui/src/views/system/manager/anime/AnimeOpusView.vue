<template xmlns="http://www.w3.org/1999/html">
  <table-manage>
    <!-- 表格操作 -->
    <template #search>
      <el-form-item label="原名:">
        <el-input v-model="pageParam.searchObject.nameOriginal" placeholder="けいおん！"></el-input>
      </el-form-item>
      <el-form-item label="中文名:">
        <el-input v-model="pageParam.searchObject.nameCn" placeholder="轻音少女"></el-input>
      </el-form-item>
      <el-form-item label="订阅状态:">
        <el-select placeholder="选择状态" style="width: 200px" v-model="pageParam.searchObject.rssStatus">
          <el-option v-for="(item, index) in rssStatusList" :label="item.label" :value="item.value" :key="index" />
        </el-select>
      </el-form-item>
      <el-form-item label="是否有资源:">
        <el-select placeholder="选择状态" style="width: 200px" v-model="pageParam.searchObject.hasResource">
          <el-option v-for="(item, index) in hasResourceList" :label="item.label" :value="item.value" :key="index" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button icon="Search" type="primary" @click="loadTableData">搜索</el-button>
        <el-button icon="RefreshRight" @click="resetSearchForm">重置</el-button>
      </el-form-item>
    </template>

    <template #operate>
      <el-button type="primary" @click="addAcgOpusDialog = true">通过URL自动添加</el-button>
      <el-button type="primary" @click="onClickShareBatchImport">批量导出 / 导入</el-button>
      <el-button type="primary" icon="Plus" @click="onAdd">添加作品</el-button>
    </template>

    <!-- 表格视图 -->
    <template #default>
      
      <el-table @selection-change="onSelectionChange" stripe row-key="id" :data="pageVo.records" v-loading="loading">
        <el-table-column
          type="selection"
          width="55">
        </el-table-column>
        <el-table-column prop="coverUrl" width="155" label="封面地址">
          <template #default="scope">
            <el-image style="width: 100px; height: 144px" :src="`api/anime/opus/cover?resName=${scope.row.coverUrl}`"
              fit="fill" />
          </template>
        </el-table-column>
        <el-table-column width="300" prop="nameOriginal" label="原名" />
        <el-table-column width="300" prop="nameCn" label="中文名" />
        <el-table-column prop="detailInfoUrl" label="详细链接" />
        <el-table-column width="100" prop="hasResource" label="是否有资源">
          <template #default="scope">
            <el-switch :model-value="scope.row.hasResource" :active-value="1" :inactive-value="0"
              @change="updateHasResource(scope.row.id, scope.row.hasResource)" />
          </template>
        </el-table-column>
        <el-table-column width="100" prop="rssStatus" label="RSS状态">
          <template #default="scope">
            <div>
              <el-button v-if="scope.row.rssStatus === 1" @click="onCloseSubscribe(scope.row.id)" size="small">
                关闭订阅
              </el-button>
              <el-tag v-else>
                {{ scope.row.rssStatus === 0 ? '未订阅' : '完成订阅' }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column width="200" prop="createTime" label="创建时间" />
        <el-table-column prop="createByName" label="创建人" />
        <el-table-column width="200" prop="updateTime" label="更新时间" />
        <el-table-column prop="updateByName" label="更新人" />
        <!-- 单行操作 -->
        <el-table-column fixed="right" width="400" label="操作">
          <template #default="scope">
            <!-- <el-button size="small" icon="Upload" @click="onUploadRes(scope.row)">上传资源</el-button> -->
            <el-button size="small" icon="Edit" @click="onEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="primary" icon="VideoCamera" @click="onRssEdit(scope.row)">
              RSS订阅
            </el-button>
            <el-button size="small" plain type="danger" icon="DeleteFilled" @click="onRemove(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <template #page>
      <el-pagination background layout="total, sizes, prev, pager, next, jumper" :total="pageVo.total"
        :page-size="pageVo.pageSize" :page-sizes=[5,10,15] @current-change="onPageChange" @size-change="onSizeChange" />
    </template>

    <!-- 编辑对话框 -->
    <template #form>
      <el-dialog v-model="dialogFormVisible" :title="editForm.id ? '编辑' : '添加'">
        <el-form ref="sttFormRef" label-width="150px" :model="editForm" :rules="rules">
          <!-- 普通属性 -->
          <el-form-item prop="nameCn" label="中文名">
            <el-input v-model="editForm.nameCn"></el-input>
          </el-form-item>
          <el-form-item prop="nameOriginal" label="原名">
            <el-input v-model="editForm.nameOriginal"></el-input>
          </el-form-item>
          <el-form-item prop="detailInfoUrl" label="详细链接">
            <el-input v-model="editForm.detailInfoUrl"></el-input>
          </el-form-item>
          <el-form-item prop="coverUrl" label="封面地址">
            <el-input v-model="editForm.coverUrl"></el-input>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogFormVisible = false">取消</el-button>
            <el-button type="primary" @click="doUpdate(sttFormRef)">确认</el-button>
          </span>
        </template>
      </el-dialog>

      <!-- 订阅配置 -->
      <el-dialog v-model="enableRss" :title="`RSS订阅配置 —— ${rssForm.nameCn}`" :fullscreen="mkXmlParsed">
        <el-row :gutter="10">
          <!--参考-->
          <el-col :span="mkXmlParsed ? 12 : 0">
            <el-scrollbar max-height="640px">
              <el-radio-group v-model="mkXmlItemSelectedIndex" @change="onMkXmlItemSelectedIndexChange">
                <el-space direction="vertical" alignment="normal">
                  <el-radio v-for="(item, index) in mkXmlDetail.itemList" :value="index" :key="index" border>
                    <el-text>
                      <div v-html="item.titleHtml"></div>
                    </el-text>
                  </el-radio>
                </el-space>
              </el-radio-group>
            </el-scrollbar>
          </el-col>

          <!--配置-->
          <el-col :span="mkXmlParsed ? 12 : 24">
            <el-card :shadow="mkXmlParsed ? 'always' : 'never'">
              <el-form ref="sttRssFormRef" label-width="150px" label-position="left" :model="rssForm" :rules="rssRules">
                <el-form-item prop="rssUrl" label="订阅链接">
                  <el-input v-model="rssForm.rssUrl" @change="loadRenames"></el-input>
                </el-form-item>
                <el-form-item prop="rssLevelIndex" label="集数出现的位置">
                  <el-space>
                    <el-input-number :min="0" v-model="rssForm.rssLevelIndex"></el-input-number>
                    <el-select v-model="rssForm.rssLevelIndex" placeholder="选择位置" style="width: 240px"
                      @change="loadRenames">
                      <el-option v-for="(item, index) in mkXmlDetail.episodeIndexList?.[mkXmlItemSelectedIndex]"
                        :key="index" :label="`位置：${index}，  索引：${item}`" :value="index" />
                    </el-select>
                  </el-space>
                </el-form-item>
                <el-form-item prop="rssFileType" label="资源格式">
                  <el-input v-model="rssForm.rssFileType"></el-input>
                </el-form-item>
                <el-form-item prop="rssOnlyMark" label="匹配的唯一标识">
                  <el-space>
                    <el-input v-model="rssForm.rssOnlyMark" @change="loadRenames"></el-input>
                    <el-select v-model="rssForm.rssOnlyMark" placeholder="选择唯一标识" style="width: 160px"
                      @change="loadRenames">
                      <el-option v-for="(item, index) in mkXmlDetail.titleFragmentList?.[mkXmlItemSelectedIndex]"
                        :key="index" :label="item" :value="item" />
                    </el-select>
                  </el-space>
                </el-form-item>
                <el-form-item label="排除的资源标识">
                  <el-space>
                    <el-select v-model="rssExcludeResArr" @change="loadRenames" placeholder="选择排除的标识" allow-create
                      filterable clearable multiple style="width: 160px">
                      <el-option v-for="(item, index) in myExclusions" :key="index" :label="item" :value="item" />
                    </el-select>
                  </el-space>
                </el-form-item>
                <el-form-item label="是否覆盖原有资源">
                  <el-radio-group v-model="rssOverride">
                    <el-radio :value="1" size="large" border>是</el-radio>
                    <el-radio :value="0" size="large" border>否</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-form>
            </el-card>

            <el-card v-if="mkXmlParsed" shadow="never" style="margin-top: 1em;">
              <el-scrollbar max-height="240px">
                <el-text line-clamp="1" :truncated="false" v-for="(item, index) in filenamesPreview" :key="index">
                  {{ item.rename }} --> {{ item.title }}<br>
                </el-text>
              </el-scrollbar>
            </el-card>
          </el-col>
        </el-row>

        <!--快速链接-->
        <el-row justify="center">
          <el-link :href="`https://mikanime.tv/Home/Search?searchstr=${rssForm.nameCn}`" icon="Right" type="warning"
            target="_blank">
            前往蜜柑获取RSS链接
          </el-link>
        </el-row>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="onShareRSS(rssForm)">导入 / 导出</el-button>
            <el-button @click="doParseMkXml(sttRssFormRef)" :loading="mkXmlParseLoading">资源解析</el-button>
            <el-button @click="enableRss = false">取 消</el-button>
            <el-button type="primary" @click="doRssUpdate(sttRssFormRef)">确 认</el-button>
          </span>
        </template>
      </el-dialog>

      <!--bangumi抓取番剧信息-->
      <el-dialog v-model="addAcgOpusDialog">
        <el-form>
          <el-form-item label="Bangumi番剧详细链接：">
            <el-input v-model="bgmUrl" placeholder="https://bgm.tv/subject/389772" />
            <el-link type="primary" href="https://bgm.tv" target="_blank">在Bangumi检索</el-link>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="closeAddAcgOpusDialog">取 消</el-button>
            <el-button :loading="addAcgOpusLoading" type="warning" @click="onAddAcgOpus(1)">重 刷</el-button>
            <el-button :loading="addAcgOpusLoading" type="primary" @click="onAddAcgOpus(0)">新 增</el-button>
          </span>
        </template>
      </el-dialog>
      <!-- 导入链接 -->
      <el-dialog v-model="showShareBatchImport" :title="`批量导出 / 导入`">
        <el-dialog
          width="30%"
          title="导入失败列表"
          :visible.sync="showInnerShareBatchImport"
          append-to-body>
          <el-input type="textarea"  v-model="innerShareBatchImportErrText" :autosize="{ minRows: 16, maxRows: 64}" />
        </el-dialog>
        <el-form>
          <el-form-item label="分享链接">
            <el-input type="textarea" v-model="shareBatchImport" :autosize="{ minRows: 16, maxRows: 64}" />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="showShareBatchImport = false">取 消</el-button>
            <el-button type="primary" @click="onImportShareBatch">导 入</el-button>
          </span>
        </template>
      </el-dialog>

      <el-dialog v-model="showShareRSSConfig" :title="`RSS配置分享 —— ${rssForm.nameCn}`">
        <el-form>
          <el-form-item label="配置JSON">
            <el-input type="textarea" v-model="shareRSSConfig" autosize />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="showShareRSSConfig = false">取 消</el-button>
            <el-button type="primary" @click="onImportShareRSS">导 入</el-button>
          </span>
        </template>
      </el-dialog>

      <!--上传资源-->
      <el-dialog v-model="uploadResDialog" :title="`${currentRow.nameCn} - 资源上传`">
        <el-upload v-model:file-list="fileList" :action="uploadUrl" multiple :limit="24" :on-exceed="handleExceed">
          <el-button type="primary">点击上传</el-button>
          <template #tip>
            <div class="el-upload__tip">
              建议mp4格式和内嵌字幕
            </div>
          </template>
        </el-upload>
      </el-dialog>
    </template>
  </table-manage>
</template>

<script setup lang="ts">
import { onMounted, ref, reactive, nextTick } from "vue";
import { add, deleteBatch, update, listByPage, addAcgOpusByBgmUrl } from "@/api/anime/ani-opus-api";
import { reqCommonFeedback, reqSuccessFeedback } from "@/api/ApiFeedback";
import TableManage from "@/components/container/TableManage.vue";
import { ElForm } from "element-plus/es";
import { ElMessage, ElMessageBox, UploadProps, UploadUserFile } from "element-plus";
import { rssSubscribe, closeSubscribe, getMkXmlDetail, getRenames, defaultExclusions } from "@/api/anime/ani-rss-api";
import { ApiResultEnum } from "@/api/ApiResultEnum";

type FormInstance = InstanceType<typeof ElForm>
const sttFormRef = ref<FormInstance>();
const sttRssFormRef = ref<FormInstance>();
const rssStatusList = ref<any>([
  { label: '未订阅', value: 0 },
  { label: '订阅中', value: 1 },
  { label: '订阅完成', value: 2 }
]);
const hasResourceList = ref<any>([
  { label: '否', value: 0 },
  { label: '是', value: 1 }
]);

// 是否开启订阅
const enableRss = ref<boolean>(false);
const pageParam = ref<PageParam>({ pageNo: 1, pageSize: 10, searchObject: {} });
// 表单参数
const editForm = ref<AniOpusModel>({});
// 表单参数
const rssForm = ref<AniOpusModel>({});
// 加载进度
const loading = ref<boolean>(true);
// 表单校验规则
const rules = reactive({
  nameCn: [{ required: true, min: 1, max: 100, message: '字数1~100', trigger: 'blur' }],
  nameOriginal: [{ required: true, min: 1, max: 100, message: '字数1~100', trigger: 'blur' }],
  detailInfoUrl: [{ required: true, min: 2, max: 200, message: '字数2~200', trigger: 'blur' }],
  coverUrl: [{ required: true, min: 5, max: 200, message: '字数5~200', trigger: 'blur' }],
  rssUrl: [{ required: true, trigger: 'blur' }],
  rssLevelIndex: [{ required: true, trigger: 'blur' }],
  rssFileType: [{ required: true, trigger: 'blur' }],
  rssOnlyMark: [{ required: true, trigger: 'blur' }],
});
// rss订阅校验规则
const rssRules = reactive({
  rssUrl: [{ required: true, trigger: 'blur' }],
  rssLevelIndex: [{ required: true, trigger: 'blur' }],
  rssFileType: [{ required: true, trigger: 'blur' }],
  rssOnlyMark: [{ required: true, trigger: 'blur' }],
});
const dialogFormVisible = ref<boolean>(false);
const pageVo = ref<PageVO>({ pageNo: 1, pageSize: 10, total: 0, records: [] });
// url抓取番剧信息
const addAcgOpusDialog = ref<boolean>(false);
const addAcgOpusLoading = ref<boolean>(false);
const bgmUrl = ref<string>('');
// 上传资源
const uploadResDialog = ref<boolean>(false);
const uploadUrl = ref<string>('');
// 选中弹窗项
const currentRow = ref<AniOpusModel>({});
const multipleSelection = ref<AniOpusModel[]>([]);
// RSS资源详细
const mkXmlDetail = ref<MkXmlDetailModel>({});
const mkXmlParseLoading = ref<boolean>(false);
const mkXmlParsed = ref<boolean>(false);
const mkXmlItemSelectedIndex = ref<number>(0);
// 选择的排除项
const rssExcludeResArr = ref<string[]>([]);
const myExclusions = ref<string[]>([]);
// 是否覆盖已下载的rss资源
const rssOverride = ref<number>(0);
// 重命名结果集预览
const filenamesPreview = ref<any>([]);
// 是否展示导入导出对话框
const showShareRSSConfig = ref<boolean>(false);
const shareRSSConfig = ref<string>('');
const showShareBatchImport = ref<boolean>(false);
const showInnerShareBatchImport = ref<boolean>(false);
const shareBatchImport = ref<string>('');
const innerShareBatchImportErrText = ref<string>('');

// 初始化数据
onMounted(() => {
  loadTableData();
});

const onEdit = (row: AniOpusModel): void => {
  editForm.value = row;
  dialogFormVisible.value = true;
}

const onRssEdit = (row: AniOpusModel): void => {
  rssForm.value = row;
  rssForm.value.rssFileType = row.rssFileType ? row.rssFileType : 'mp4';
  enableRss.value = true;
  // 初始化RSS详细
  mkXmlParsed.value = false;
  mkXmlDetail.value = {};
  mkXmlItemSelectedIndex.value = 0;
  // 字符串转数组
  let split = rssForm.value.rssExcludeRes?.split(',');
  if (split && split[0] != '' && split.length > 0) {
    rssExcludeResArr.value = split;
  } else {
    rssExcludeResArr.value = [];
  }
  // 获取默认排除选项
  if (row.rssStatus === 0) {
    loadDefaultExclusions();
  }
}

const onAdd = () => {
  dialogFormVisible.value = true;
  editForm.value = {};
}

const onRemove = (row: AniOpusModel): void => {
  ElMessageBox.confirm('确认删除改行数据?', '提示', {
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

const onSelectionChange = (val: AniOpusModel[]) => {
  multipleSelection.value = val;
}

const loadTableData = (): void => {
  if (!loading.value) loading.value = true;
  let param = {
    pageNo: pageParam.value.pageNo,
    pageSize: pageParam.value.pageSize,
    aniOpus: {
      likeNameOriginal: pageParam.value.searchObject.nameOriginal,
      likeNameCn: pageParam.value.searchObject.nameCn,
      rssStatus: pageParam.value.searchObject.rssStatus,
      hasResource: pageParam.value.searchObject.hasResource,
    }
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
        reqSuccessFeedback(add({
          nameOriginal: editForm.value.nameOriginal,
          nameCn: editForm.value.nameCn,
          detailInfoUrl: editForm.value.detailInfoUrl,
          coverUrl: editForm.value.coverUrl,
        }), '添加成功', () => {
          loadTableData();
          dialogFormVisible.value = false;
        });
      } else {
        reqSuccessFeedback(update({
          id: editForm.value.id,
          nameOriginal: editForm.value.nameOriginal,
          nameCn: editForm.value.nameCn,
          detailInfoUrl: editForm.value.detailInfoUrl,
          coverUrl: editForm.value.coverUrl,
        }), '修改成功', () => {
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

// 更新资源状态
const updateHasResource = (id: string, hasResource: number) => {
  reqSuccessFeedback(update({
    id: id,
    hasResource: hasResource == 0 ? 1 : 0
  }), '更新成功', () => {
    loadTableData();
  });
}

const onCloseSubscribe = (id: string) => {
  reqSuccessFeedback(closeSubscribe(id), '关闭成功', () => {
    loadTableData();
  });
}

// 提交rss订阅
const doRssUpdate = (formEl: any): void => {
  formEl.validate((valid: any) => {
    if (valid) {
      let rssSubscribeDTO = {
        id: rssForm.value.id,
        rssUrl: rssForm.value.rssUrl,
        rssLevelIndex: rssForm.value.rssLevelIndex,
        rssFileType: rssForm.value.rssFileType,
        rssOnlyMark: rssForm.value.rssOnlyMark,
        rssExcludeRes: rssExcludeResArr.value.join(','),
        rssOverride: rssOverride.value,
      }
      console.log(rssSubscribeDTO);
      reqSuccessFeedback(rssSubscribe(rssSubscribeDTO), '修改成功', () => {
        loadTableData();
        enableRss.value = false;
      });
    }
  });
}

const closeAddAcgOpusDialog = () => {
  addAcgOpusDialog.value = false;
  addAcgOpusLoading.value = false;
}

const onAddAcgOpus = (isCover: number) => {
  addAcgOpusLoading.value = true;
  addAcgOpusByBgmUrl({ bgmUrl: bgmUrl.value, isCover: isCover }).then((data) => {
    if (data.code === 200) {
      ElMessage.success({ message: '提交成功' });
      loadTableData();
    } else {
      ElMessage.error({ message: data.message });
    }
  }).catch((err) => {
    console.log(err);
  }).finally(() => {
    closeAddAcgOpusDialog();
  });
}

const fileList = ref<UploadUserFile[]>([]);

const handleExceed: UploadProps['onExceed'] = (files, uploadFiles) => {
  ElMessage.warning(
    `The limit is 24, you selected ${files.length} files this time, add up to ${files.length + uploadFiles.length
    } totally`
  )
}

const onUploadRes = (row: AniOpusModel) => {
  if (row.id) {
    currentRow.value = row;
    uploadUrl.value = `/api/anime/opus/${row.id}/uploadRes`;
    uploadResDialog.value = true;
  } else {
    ElMessage.warning('上传地址异常');
  }
}

const doParseMkXml = (formEl: any) => {
  let rssUrl = rssForm.value.rssUrl;
  let prefix = 'https://mikanime.tv/RSS/';
  if (rssUrl && rssUrl.startsWith(prefix)) {
    mkXmlParseLoading.value = true;
    let encodeRssUrl = encodeURIComponent(rssUrl);
    console.log(encodeRssUrl);
    getMkXmlDetail(encodeRssUrl).then(result => {
      if (result.code === ApiResultEnum.SUCCESS) {
        if (result.data) {
          mkXmlDetail.value = result.data;
          mkXmlParsed.value = true;
        } else {
          ElMessage.warning('无数据');
        }
      } else {
        ElMessage.error(result.message);
      }
      mkXmlParseLoading.value = false;
    }).catch(err => {
      console.log(err);
      mkXmlParseLoading.value = false;
    });
    // 获取解析结果
    formEl.validate((valid: any) => {
      if (valid) {
        loadRenames();
      }
    });
  } else {
    ElMessage.warning('RSS链接格式有误');
  }
}

const loadRenames = () => {
  let rssSubscribeDTO = {
    id: rssForm.value.id,
    rssUrl: rssForm.value.rssUrl,
    rssLevelIndex: rssForm.value.rssLevelIndex,
    rssFileType: rssForm.value.rssFileType,
    rssOnlyMark: rssForm.value.rssOnlyMark,
    rssExcludeRes: rssExcludeResArr.value.join(','),
  }
  reqCommonFeedback(getRenames(rssSubscribeDTO), res => {
    filenamesPreview.value = res;
  });
}

const onMkXmlItemSelectedIndexChange = () => {
  loadRenames();
}

const loadDefaultExclusions = () => {
  reqCommonFeedback(defaultExclusions(), (res: any) => {
    myExclusions.value = res;
    rssExcludeResArr.value = res;
  });
}

const onShareRSS = (rssForm: any) => {
  showShareRSSConfig.value = true;
  shareRSSConfig.value = JSON.stringify({
    "rssUrl": rssForm.rssUrl,
    "rssExcludeRes": rssForm.rssExcludeRes,
    "rssFileType": rssForm.rssFileType,
    "rssLevelIndex": rssForm.rssLevelIndex,
    "rssOnlyMark": rssForm.rssOnlyMark,
    "rssStatus": rssForm.rssStatus,
  }, null, '\t');
}

const onImportShareRSS = () => {
  if (shareRSSConfig.value == null) {
    return;
  }
  let cloneObj = Object.assign(rssForm.value, JSON.parse(shareRSSConfig.value));
  rssForm.value = cloneObj;
  
  let split = cloneObj.rssExcludeRes?.split(',');
  if (split && split[0] != '' && split.length > 0) {
    rssExcludeResArr.value = split;
  } else {
    rssExcludeResArr.value = [];
  }

  showShareRSSConfig.value = false;
}

const onClickShareBatchImport = () => {
  showShareBatchImport.value = true;
  if (multipleSelection.value.length !== 0) {
    shareBatchImport.value = multipleSelection.value.map(item => { 
      let json = JSON.stringify({
        id: item.id,
        bgmUrl: item.detailInfoUrl, 
        rssUrl: item.rssUrl,
        rssExcludeRes: item.rssExcludeRes,
        rssFileType: item.rssFileType,
        rssLevelIndex: item.rssLevelIndex,
        rssOnlyMark: item.rssOnlyMark,
        rssStatus: item.rssStatus, 
      }, (_key, value) => { return typeof value === 'string' ?  encodeURI(value) : value })
      console.log(json);
      return item.nameCn + "-" + btoa(json)
    }).join('\n');
  } else {
    // 显示警告信息，提示用户选择要导出的行
    ElMessage.warning('如需导出，请选择要导出的行');
    return;
  }
}

const onImportShareBatch = async () => {
  if (shareBatchImport.value == null) {
    return;
  }
  // 解密然后解析
  let arr = shareBatchImport.value.split('\n');
  let errorList = []
  let decodeArr = arr.map(item => { 
    if (typeof item === 'string' && item.lastIndexOf('-') > 0) {
       let sliced = item.slice(item.lastIndexOf('-') + 1)
       return atob(sliced)
    }
    return atob(item)
  });
  for (const item of decodeArr) { 
    if (item == '') { 
      continue;
    }
    let shareItem = JSON.parse(item, (_key, value) => { return typeof value === 'string'?  decodeURI(value) : value });
        console.log(shareItem);
    try {
        // debugger
        // 等待 doRssUpdate 请求完成
        // 等待 addAcgOpusByBgmUrl 请求完成
        const data = await addAcgOpusByBgmUrl({ bgmUrl: 'https://bgm.tv' + shareItem.bgmUrl, isCover: false }); 
        if  (data.code !== 200 && data.message !== '作品已经存在') { 
          ElMessage.error({ message: data.message }); 
        }
        await rssSubscribe(shareItem); 
    } catch (err) { 
      ElMessage.error({ message: shareItem.nameCn + '导入失败' }); 
      errorList.push({nameCn: shareItem.nameCn, content: item, err: err});
    }
  }
  if (errorList.length > 0) {
    ElMessage.error({ message: '部分导入失败' });
    showInnerShareBatchImport.value = true;
    innerShareBatchImportErrText.value = errorList.map(item => { return item.nameCn + "-" + btoa(item.content) }).join('\n');
  } else {
    ElMessage.success({ message: '导入成功' });
  }
  loadTableData();
  showShareBatchImport.value = false;
}
</script>

<style scoped></style>
