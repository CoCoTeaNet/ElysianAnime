<template>
  <div class="ea-home">
    <header class="ea-home__header">
      <h1 class="ea-home__greeting">{{ greeting }}，{{ nickname }}</h1>
      <AnimeFilterBar :keyword="keyword" @search="onSearch" @open-filter="filterVisible = true"
                      @add="addDialogVisible = true"/>
    </header>

    <ContinueWatching @play="onContinuePlay"/>
    <RecentUpdateStrip @open="onOpenOpus" @more="router.push({name: 'Dashboard'})"/>

    <section class="ea-home__list">
      <div class="ea-home__list-head">
        <div class="ea-home__tabs">
          <button
              v-for="tab in tabs"
              :key="tab.key"
              class="ea-home__tab"
              :class="{'is-active': activeTab === tab.key}"
              @click="onTabChange(tab.key)"
          >
            {{ tab.label }}
          </button>
        </div>
      </div>

      <div v-loading="loading" class="ea-home__grid-wrap">
        <div v-if="records.length" class="ea-home__grid">
          <AnimeCard
              v-for="item in records"
              :key="String(item.id)"
              :opus="item"
              @open="onOpenOpus"
              @play="onPlayOpus"
              @follow="onFollow"
              @unfollow="onUnfollow"
              @feed="onFeed"
          />
        </div>
        <div v-else-if="!loading" class="ea-home__empty">
          <p>还没有追番，去搜索一部吧</p>
        </div>
      </div>

      <div class="ea-home__pager">
        <el-pagination
            background
            layout="total, prev, pager, next"
            :total="total"
            :page-size="pageSize"
            :current-page="pageNo"
            @current-change="onPageChange"
        />
      </div>
    </section>

    <FilterDrawer v-model="filterVisible" :filters="filters" @apply="onFilterApply"/>
    <AddOpusDialog v-model="addDialogVisible" @success="loadList"/>
    <FeedDialog v-model="feedVisible" :opus="feedTarget"/>
  </div>
</template>

<script lang="ts" setup>
defineOptions({name: "AnimeHome"});
import {computed, onMounted, reactive, ref, watch} from "vue";
import {useRoute, useRouter} from "vue-router";
import {ElMessage} from "element-plus";
import {listByUser} from "@/api/anime/ani-opus-api";
import userOpusApi from "@/api/anime/ani-user-opus-api";
import {reqCommonFeedback} from "@/api/ApiFeedback";
import {useStore} from "@/store";
import AnimeFilterBar from "./components/AnimeFilterBar.vue";
import ContinueWatching from "./components/ContinueWatching.vue";
import RecentUpdateStrip from "./components/RecentUpdateStrip.vue";
import AnimeCard from "./components/AnimeCard.vue";
import FilterDrawer, {HomeFilters} from "./components/FilterDrawer.vue";
import AddOpusDialog from "./components/AddOpusDialog.vue";
import FeedDialog from "./components/FeedDialog.vue";

const route = useRoute();
const router = useRouter();
const store = useStore();

const tabs = [
  {key: 'all', label: '全部', status: [] as string[]},
  {key: 'reading', label: '在看', status: ['正在观看']},
  {key: 'unread', label: '未看', status: ['未看']},
  {key: 'finished', label: '已看完', status: ['已看']},
];

const activeTab = ref('all');
const keyword = ref('');
const filterVisible = ref(false);
const addDialogVisible = ref(false);
const feedVisible = ref(false);
const feedTarget = ref<any>(null);

const filters = reactive<HomeFilters>({status: [], months: [], years: [], hasResource: -1});

const records = ref<any[]>([]);
const total = ref(0);
const pageNo = ref(1);
const pageSize = 40;
const loading = ref(false);

const nickname = computed(() => store.state.userInfo.nickname || '番剧迷');
const greeting = computed(() => {
  const h = new Date().getHours();
  if (h < 6) return '夜深了';
  if (h < 12) return '早上好';
  if (h < 18) return '下午好';
  return '晚上好';
});

onMounted(() => {
  syncFromQuery();
  loadList();
});

watch(() => route.query, () => {
  syncFromQuery();
  loadList();
});

const syncFromQuery = () => {
  const q = route.query;
  keyword.value = (q.searchKey as string) || '';
  activeTab.value = (q.tab as string) || 'all';
  filters.status = q.status ? String(q.status).split(',') : [];
  filters.months = q.months ? String(q.months).split(',') : [];
  filters.years = q.years ? String(q.years).split(',') : [];
  filters.hasResource = q.hasResource ? Number(q.hasResource) : -1;
  pageNo.value = Number(q.pageNo) || 1;
};

const applyQuery = () => {
  const tab = tabs.find(t => t.key === activeTab.value);
  const query: Record<string, any> = {};
  if (keyword.value) query.searchKey = keyword.value;
  if (activeTab.value !== 'all') query.tab = activeTab.value;
  if (filters.status.length && !tab?.status.length) query.status = filters.status.join(',');
  if (filters.months.length) query.months = filters.months.join(',');
  if (filters.years.length) query.years = filters.years.join(',');
  if (filters.hasResource === 1) query.hasResource = '1';
  if (pageNo.value > 1) query.pageNo = String(pageNo.value);
  router.push({query});
};

const loadList = () => {
  loading.value = true;
  const tab = tabs.find(t => t.key === activeTab.value);
  const status = tab?.status.length ? tab.status : filters.status;
  const param: any = {
    pageNo: pageNo.value,
    pageSize,
    isFollow: 1,
    status,
    months: filters.months,
    years: filters.years,
    searchKey: keyword.value,
  };
  if (filters.hasResource === 1) {
    param.hasResource = 1;
  }
  reqCommonFeedback(listByUser(param), (data: any) => {
    records.value = data.records || [];
    total.value = data.total || 0;
    loading.value = false;
  });
};

const onSearch = (kw: string) => {
  keyword.value = kw;
  pageNo.value = 1;
  applyQuery();
};

const onTabChange = (key: string) => {
  activeTab.value = key;
  pageNo.value = 1;
  applyQuery();
};

const onFilterApply = (applied: HomeFilters) => {
  Object.assign(filters, applied);
  pageNo.value = 1;
  applyQuery();
};

const onPageChange = (page: number) => {
  pageNo.value = page;
  applyQuery();
};

const onOpenOpus = (item: any) => {
  router.push({name: 'AnimeDetail', params: {id: item.opusId || item.id}});
};

const onContinuePlay = (item: any) => {
  const query: Record<string, any> = {};
  if (item.mediaId && Number(item.mediaId) > 0) {
    query.ep = String(item.mediaId);
  } else if (item.episodeKey) {
    query.ek = item.episodeKey;
  }
  router.push({name: 'AnimeDetail', params: {id: item.opusId}, query});
};

const onPlayOpus = (opus: any) => {
  const query: Record<string, any> = {};
  if (opus.lastMediaId && Number(opus.lastMediaId) > 0) {
    query.ep = String(opus.lastMediaId);
  } else if (opus.lastEpisodeKey) {
    query.ek = opus.lastEpisodeKey;
  }
  router.push({name: 'AnimeDetail', params: {id: opus.id}, query});
};

const onFollow = (opus: any) => {
  reqCommonFeedback(userOpusApi.follow(opus.id), () => {
    ElMessage.success('已追番');
    loadList();
  });
};

const onUnfollow = (opus: any) => {
  reqCommonFeedback(userOpusApi.unfollow(opus.id), () => {
    ElMessage.success('已取消追番');
    loadList();
  });
};

const onFeed = (opus: any) => {
  feedTarget.value = opus;
  feedVisible.value = true;
};
</script>

<style scoped>
.ea-home {
  max-width: 1560px;
  margin: 0 auto;
}

.ea-home__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--ea-space-4);
  margin-bottom: var(--ea-space-5);
  flex-wrap: wrap;
}

.ea-home__greeting {
  margin: 0;
  font-size: 22px;
  color: var(--ea-text-1);
}

.ea-home__list-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--ea-space-3);
}

.ea-home__tabs {
  display: flex;
  gap: var(--ea-space-1);
}

.ea-home__tab {
  border: none;
  background: transparent;
  padding: 6px 14px;
  border-radius: var(--ea-radius-sm);
  font-size: 14px;
  color: var(--ea-text-2);
  cursor: pointer;
  transition: background-color var(--ea-duration), color var(--ea-duration);
}

.ea-home__tab:hover {
  color: var(--ea-text-1);
}

.ea-home__tab.is-active {
  background: var(--ea-color-primary);
  color: #fff;
}

.ea-home__grid-wrap {
  min-height: 200px;
}

.ea-home__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: var(--ea-space-4);
}

.ea-home__empty {
  padding: var(--ea-space-6) 0;
  text-align: center;
  color: var(--ea-text-3);
}

.ea-home__pager {
  display: flex;
  justify-content: center;
  padding: var(--ea-space-5) 0 var(--ea-space-2);
}

@media (max-width: 768px) {
  .ea-home__grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: var(--ea-space-3);
  }
}
</style>
