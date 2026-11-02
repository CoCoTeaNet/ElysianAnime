<template>
  <el-dialog :model-value="modelValue" title="投喂番剧" width="500px"
             @update:model-value="$emit('update:modelValue', $event)">
    <p style="margin-bottom: 12px; font-weight: 600">选择要投喂的用户：</p>
    <el-input
        v-model="keyword"
        placeholder="搜索用户昵称或用户名"
        clearable
        @input="onSearchInput"
        style="margin-bottom: 12px"
    />
    <div v-loading="userLoading" class="ea-feed__list">
      <div
          v-for="user in users"
          :key="user.id"
          class="ea-feed__item"
          :class="{ 'is-selected': selectedUserId === user.id }"
          @click="selectedUserId = user.id"
      >
        <el-avatar :size="36">{{ user.nickname?.charAt(0) || '?' }}</el-avatar>
        <div class="ea-feed__info">
          <span class="ea-feed__nickname">{{ user.nickname }}</span>
          <span class="ea-feed__username">@{{ user.username }}</span>
        </div>
        <el-icon v-if="selectedUserId === user.id" color="#E6A23C">
          <Check/>
        </el-icon>
      </div>
      <el-empty v-if="!userLoading && users.length === 0" description="暂无用户"/>
    </div>
    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">取消</el-button>
      <el-button type="warning" :loading="loading" :disabled="!selectedUserId" @click="onConfirm">确定投喂</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import {ref, watch} from "vue";
import {Check} from "@element-plus/icons-vue";
import {ElMessage} from "element-plus";
import {feed} from "@/api/anime/ani-feed-api";
import {listActiveUsers, searchUsers} from "@/api/system/sys-user-api";
import {ApiResultEnum} from "@/api/ApiResultEnum";

const props = defineProps<{ modelValue: boolean; opus: any }>();
const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void;
  (e: 'success'): void;
}>();

const keyword = ref('');
const selectedUserId = ref('');
const users = ref<any[]>([]);
const userLoading = ref(false);
const loading = ref(false);

watch(() => props.modelValue, (open) => {
  if (open) {
    selectedUserId.value = '';
    keyword.value = '';
    loadUsers();
  }
});

const loadUsers = () => {
  userLoading.value = true;
  listActiveUsers().then((res: any) => {
    users.value = res.data || [];
  }).finally(() => {
    userLoading.value = false;
  });
};

const onSearchInput = (kw: string) => {
  if (!kw) {
    loadUsers();
    return;
  }
  userLoading.value = true;
  searchUsers(kw).then((res: any) => {
    users.value = res.data || [];
  }).finally(() => {
    userLoading.value = false;
  });
};

const onConfirm = async () => {
  if (!selectedUserId.value) {
    ElMessage.warning('请选择要投喂的用户');
    return;
  }
  loading.value = true;
  try {
    const data: any = await feed({opusId: props.opus.id, toUserId: selectedUserId.value});
    if (data.code === ApiResultEnum.SUCCESS) {
      ElMessage.success(data.message);
      emit('success');
      emit('update:modelValue', false);
    }
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.ea-feed__list {
  max-height: 320px;
  overflow-y: auto;
  border: 1px solid var(--ea-border);
  border-radius: var(--ea-radius-sm);
  padding: 4px;
}

.ea-feed__item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: var(--ea-radius-sm);
  cursor: pointer;
  transition: background-color var(--ea-duration);
}

.ea-feed__item:hover {
  background-color: var(--el-fill-color-light);
}

.ea-feed__item.is-selected {
  background-color: var(--el-color-warning-light-9);
  border: 1px solid var(--el-color-warning-light-5);
}

.ea-feed__info {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.ea-feed__nickname {
  font-weight: 600;
  font-size: 14px;
}

.ea-feed__username {
  font-size: 12px;
  color: var(--ea-text-3);
}
</style>
