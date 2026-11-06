<template>
  <el-dialog
      :model-value="modelValue"
      class="ea-quick"
      title="快速订阅"
      width="min(920px, 94vw)"
      :close-on-click-modal="false"
      @update:model-value="$emit('update:modelValue', $event)"
      @closed="onClosed"
  >
    <!-- 第一步：输入链接 -->
    <div v-if="!parsed" class="ea-quick__input">
      <el-input
          v-model="rssUrl"
          clearable
          :prefix-icon="Link"
          placeholder="https://mikanime.tv/RSS/Bangumi?bangumiId=681&subgroupid=124"
          @keyup.enter="onParse"
      />
      <p class="ea-quick__tip">
        粘贴蜜柑番剧的 RSS 链接，解析后会自动按 bangumiId 创建番剧并生成订阅配置。
      </p>
      <div class="ea-quick__input-foot">
        <el-link :href="`https://mikanime.tv/Home/Search?searchstr=${searchKey}`" type="warning" target="_blank">
          去蜜柑查找番剧
        </el-link>
        <el-button :loading="parsing" type="primary" @click="onParse">解析并预览</el-button>
      </div>
    </div>

    <!-- 第二步：解析结果与订阅配置 -->
    <div v-else class="ea-quick__body">
      <div class="ea-quick__opus">
        <el-image v-if="coverSrc" class="ea-quick__cover" :src="coverSrc" fit="cover">
          <template #error>
            <div class="ea-quick__cover-empty">无封面</div>
          </template>
        </el-image>
        <div class="ea-quick__meta">
          <div class="ea-quick__name">{{ preview.nameCn }}</div>
          <div v-if="preview.nameOriginal" class="ea-quick__sub">{{ preview.nameOriginal }}</div>
          <div class="ea-quick__tags">
            <el-tag v-if="preview.existed" type="info" size="small">番剧已存在</el-tag>
            <el-tag v-else type="success" size="small">已自动创建番剧</el-tag>
            <el-tag v-if="mkXmlDetail.itemList?.length" size="small">解析到 {{ mkXmlDetail.itemList.length }} 条资源</el-tag>
          </div>
        </div>
      </div>

      <el-row :gutter="16">
        <el-col :xs="24" :sm="10">
          <el-scrollbar max-height="360px">
            <el-radio-group v-model="selectedIndex" class="ea-quick__list">
              <el-space direction="vertical" alignment="normal">
                <el-radio v-for="(item, index) in mkXmlDetail.itemList" :value="index" :key="index" border>
                  <span class="ea-quick__item-title">
                    <template v-for="(part, i) in highlightParts(item.title)" :key="i">
                      <span v-if="part.num" class="is-num">{{ part.text }}</span>
                      <template v-else>{{ part.text }}</template>
                    </template>
                  </span>
                </el-radio>
              </el-space>
            </el-radio-group>
          </el-scrollbar>
        </el-col>

        <el-col :xs="24" :sm="14">
          <el-form label-width="120px" label-position="left" class="ea-quick__form">
            <el-form-item label="订阅链接">
              <el-input v-model="form.rssUrl" @change="onConfigChange"/>
            </el-form-item>
            <el-form-item label="集数位置">
              <el-space>
                <el-input-number :min="0" v-model="form.rssLevelIndex" @change="onConfigChange"/>
                <el-select v-model="form.rssLevelIndex" placeholder="选择位置" style="width: 190px"
                           @change="onConfigChange">
                  <el-option v-for="(item, index) in mkXmlDetail.episodeIndexList?.[selectedIndex]"
                             :key="index" :label="`位置 ${index}：${item}`" :value="index"/>
                </el-select>
              </el-space>
            </el-form-item>
            <el-form-item label="资源格式">
              <el-input v-model="form.rssFileType" @change="onConfigChange"/>
            </el-form-item>
            <el-form-item label="唯一标识">
              <el-space>
                <el-input v-model="form.rssOnlyMark" @change="onConfigChange"/>
                <el-select v-model="form.rssOnlyMark" placeholder="选择标识" style="width: 130px"
                           @change="onConfigChange">
                  <el-option v-for="(item, index) in mkXmlDetail.titleFragmentList?.[selectedIndex]"
                             :key="index" :label="item" :value="item"/>
                </el-select>
              </el-space>
            </el-form-item>
            <el-form-item label="排除标识">
              <el-select v-model="form.rssExcludeRes" allow-create filterable clearable multiple
                         placeholder="选择排除的标识" style="width: 100%" @change="onConfigChange">
                <el-option v-for="(item, index) in exclusions" :key="index" :label="item" :value="item"/>
              </el-select>
            </el-form-item>
            <el-form-item label="覆盖已有资源">
              <el-radio-group v-model="form.rssOverride" @change="onConfigChange">
                <el-radio :value="1" border>是</el-radio>
                <el-radio :value="0" border>否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>

      <div v-loading="renamesLoading" class="ea-quick__preview">
        <div class="ea-quick__preview-head">
          <span>重命名预览</span>
          <span class="ea-quick__preview-count">{{ previewList.length }} 条</span>
        </div>
        <el-scrollbar max-height="150px">
          <p v-for="(item, index) in previewList" :key="index" class="ea-quick__preview-line">
            {{ item.rename }} <span class="ea-quick__arrow">←</span> {{ item.title }}
          </p>
          <p v-if="!renamesLoading && !previewList.length" class="ea-quick__preview-empty">
            当前配置没有匹配到资源，请调整唯一标识或排除项
          </p>
        </el-scrollbar>
      </div>
    </div>

    <template #footer>
      <el-button v-if="parsed" @click="backToInput">重新输入</el-button>
      <el-button @click="$emit('update:modelValue', false)">取消</el-button>
      <el-button v-if="parsed" :loading="submitting" type="primary" :disabled="!canSubmit" @click="onSubmit">
        提交订阅
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
defineOptions({name: "QuickSubscribeDialog"});
import {computed, reactive, ref} from "vue";
import {ElMessage} from "element-plus";
import {Link} from "@element-plus/icons-vue";
import {defaultExclusions, getRenames, quickPrepare, rssSubscribe} from "@/api/anime/ani-rss-api";
import {ApiResultEnum} from "@/api/ApiResultEnum";
import {API_BASE} from "@/utils/axios-util";
import {RULE_URL_HTTP} from "@/utils/rules-util";

defineProps<{ modelValue: boolean }>();
const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void;
  (e: 'success'): void;
}>();

const rssUrl = ref('');
const parsing = ref(false);
const submitting = ref(false);
const renamesLoading = ref(false);
const parsed = ref(false);
const preview = ref<any>({});
const mkXmlDetail = ref<any>({});
const selectedIndex = ref(0);
const previewList = ref<any[]>([]);
const exclusions = ref<string[]>([]);

const form = reactive({
  id: '',
  rssUrl: '',
  rssLevelIndex: 0,
  rssFileType: 'mp4',
  rssOnlyMark: '',
  rssExcludeRes: [] as string[],
  rssOverride: 0,
});

const searchKey = computed(() => preview.value.nameCn || '');
const coverSrc = computed(() => preview.value.coverUrl
    ? `${API_BASE}/anime/opus/cover?resName=${preview.value.coverUrl}&w=120&h=168`
    : '');
const canSubmit = computed(() => !!form.rssOnlyMark && !renamesLoading.value);

const onClosed = () => {
  rssUrl.value = '';
  parsed.value = false;
  preview.value = {};
  mkXmlDetail.value = {};
  selectedIndex.value = 0;
  previewList.value = [];
  exclusions.value = [];
  Object.assign(form, {
    id: '',
    rssUrl: '',
    rssLevelIndex: 0,
    rssFileType: 'mp4',
    rssOnlyMark: '',
    rssExcludeRes: [],
    rssOverride: 0,
  });
};

const backToInput = () => {
  parsed.value = false;
};

const onParse = async () => {
  const url = rssUrl.value.trim();
  if (!url || !RULE_URL_HTTP.test(url)) {
    ElMessage.warning('RSS 链接格式有误');
    return;
  }
  parsing.value = true;
  try {
    const res = await quickPrepare({rssUrl: url});
    if (res.code !== ApiResultEnum.SUCCESS) {
      ElMessage.error(res.message || '解析失败');
      return;
    }
    const data = res.data || {};
    preview.value = data;
    mkXmlDetail.value = data.detail || {};
    form.id = data.opusId ? String(data.opusId) : '';
    form.rssUrl = url;
    form.rssFileType = data.rssFileType || 'mp4';
    form.rssLevelIndex = data.rssLevelIndex ?? 0;
    form.rssOnlyMark = data.rssOnlyMark || guessOnlyMark(data.detail);
    form.rssExcludeRes = data.rssExcludeRes ? data.rssExcludeRes.split(',').filter(Boolean) : [];
    selectedIndex.value = 0;
    parsed.value = true;
    await loadDefaultExclusions();
    await loadRenames();
  } catch (e) {
    ElMessage.error('解析失败，请稍后重试');
  } finally {
    parsing.value = false;
  }
};

const loadDefaultExclusions = async () => {
  try {
    const res = await defaultExclusions();
    if (res.code === ApiResultEnum.SUCCESS) {
      exclusions.value = res.data || [];
      if (!form.rssExcludeRes.length) {
        form.rssExcludeRes = [...exclusions.value];
      }
    }
  } catch (e) {
    // 默认排除项加载失败不影响订阅
  }
};

const onConfigChange = () => {
  loadRenames();
};

const loadRenames = async () => {
  if (!form.id || !form.rssOnlyMark) {
    previewList.value = [];
    return;
  }
  renamesLoading.value = true;
  try {
    const res = await getRenames({
      id: form.id,
      rssUrl: form.rssUrl,
      rssLevelIndex: form.rssLevelIndex,
      rssFileType: form.rssFileType,
      rssOnlyMark: form.rssOnlyMark,
      rssExcludeRes: form.rssExcludeRes.join(','),
    });
    if (res.code === ApiResultEnum.SUCCESS) {
      previewList.value = res.data || [];
    } else {
      previewList.value = [];
    }
  } catch (e) {
    previewList.value = [];
  } finally {
    renamesLoading.value = false;
  }
};

const onSubmit = async () => {
  if (!canSubmit.value) {
    return;
  }
  submitting.value = true;
  try {
    const res = await rssSubscribe({
      id: form.id,
      rssUrl: form.rssUrl,
      rssLevelIndex: form.rssLevelIndex,
      rssFileType: form.rssFileType,
      rssOnlyMark: form.rssOnlyMark,
      rssExcludeRes: form.rssExcludeRes.join(','),
      rssOverride: form.rssOverride,
    });
    if (res.code === ApiResultEnum.SUCCESS) {
      ElMessage.success(preview.value.existed ? '订阅已更新' : '番剧已添加并开始订阅');
      emit('success');
      emit('update:modelValue', false);
    } else {
      ElMessage.error(res.message || '提交订阅失败');
    }
  } catch (e) {
    ElMessage.error('提交订阅失败，请稍后重试');
  } finally {
    submitting.value = false;
  }
};

/**
 * 从标题碎片中猜测唯一标识：取出现次数最多的非纯数字碎片
 */
const guessOnlyMark = (detail: any): string => {
  const groups: string[][] = detail?.titleFragmentList || [];
  const counter = new Map<string, number>();
  groups.forEach(frags => {
    new Set(frags || []).forEach(f => {
      if (!/^\d+(\.\d+)?$/.test(f) && f.length >= 2) {
        counter.set(f, (counter.get(f) || 0) + 1);
      }
    });
  });
  let best = '';
  let bestCount = 0;
  counter.forEach((count, fragment) => {
    if (count > bestCount) {
      best = fragment;
      bestCount = count;
    }
  });
  return best;
};

const highlightParts = (title: string) => {
  const text = title || '';
  const parts: { text: string; num: boolean }[] = [];
  const re = /\d+(\.\d+)?/g;
  let last = 0;
  let match: RegExpExecArray | null;
  while ((match = re.exec(text)) !== null) {
    if (match.index > last) {
      parts.push({text: text.slice(last, match.index), num: false});
    }
    parts.push({text: match[0], num: true});
    last = match.index + match[0].length;
  }
  if (last < text.length) {
    parts.push({text: text.slice(last), num: false});
  }
  return parts;
};
</script>

<style scoped>
.ea-quick__tip {
  margin: var(--ea-space-2, 8px) 0 0;
  font-size: 13px;
  color: var(--ea-text-3, #909399);
}

.ea-quick__input-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: var(--ea-space-4, 16px);
}

.ea-quick__opus {
  display: flex;
  align-items: center;
  gap: var(--ea-space-4, 16px);
  margin-bottom: var(--ea-space-4, 16px);
}

.ea-quick__cover,
.ea-quick__cover-empty {
  width: 72px;
  height: 100px;
  border-radius: var(--ea-radius-sm, 6px);
  flex: none;
}

.ea-quick__cover-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  background: var(--el-fill-color-light);
  color: var(--el-text-color-secondary);
}

.ea-quick__name {
  font-size: 18px;
  font-weight: 600;
  color: var(--ea-text-1, #303133);
}

.ea-quick__sub {
  margin-top: 2px;
  font-size: 13px;
  color: var(--ea-text-3, #909399);
}

.ea-quick__tags {
  display: flex;
  gap: var(--ea-space-2, 8px);
  margin-top: var(--ea-space-2, 8px);
  flex-wrap: wrap;
}

.ea-quick__list {
  width: 100%;
}

.ea-quick__list :deep(.el-space__item) {
  width: 100%;
}

.ea-quick__list :deep(.el-radio) {
  width: 100%;
  height: auto;
  margin-right: 0;
  padding: 8px 12px;
}

.ea-quick__list :deep(.el-radio__label) {
  width: 100%;
  min-width: 0;
  font-size: 13px;
  line-height: 1.4;
  white-space: normal;
  word-break: break-all;
}

.ea-quick__item-title .is-num {
  color: #f38181;
}

.ea-quick__preview {
  margin-top: var(--ea-space-3, 12px);
  padding: var(--ea-space-3, 12px);
  background: var(--el-fill-color-lighter);
  border-radius: var(--ea-radius-sm, 6px);
}

.ea-quick__preview-head {
  display: flex;
  justify-content: space-between;
  margin-bottom: var(--ea-space-2, 8px);
  font-size: 13px;
  color: var(--ea-text-2, #606266);
}

.ea-quick__preview-count {
  color: var(--ea-text-3, #909399);
}

.ea-quick__preview-line {
  margin: 0 0 4px;
  font-size: 12px;
  color: var(--ea-text-2, #606266);
  word-break: break-all;
}

.ea-quick__arrow {
  color: var(--ea-text-3, #909399);
  margin: 0 4px;
}

.ea-quick__preview-empty {
  margin: 0;
  font-size: 12px;
  color: var(--ea-text-3, #909399);
}
</style>
