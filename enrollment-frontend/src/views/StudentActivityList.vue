<template>
  <div class="activity-list" v-loading="loading">
    <!-- Banner Carousel -->
    <el-carousel
      height="220px"
      class="banner-carousel"
      :interval="4000"
      arrow="always"
    >
      <el-carousel-item v-for="(b, i) in banners" :key="i">
        <div class="banner" :style="{ background: b.color }" @click="b.id ? $router.push('/student/activities/' + b.id) : null" style="cursor:pointer">
          <div class="banner-content">
            <h2>{{ b.title }}</h2>
            <p>{{ b.subtitle }}</p>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>

    <!-- Tabs -->
    <div class="tabs-bar">
      <el-radio-group v-model="activeTab" @change="handleTabChange" size="default">
        <el-radio-button value="all">全部</el-radio-button>
        <el-radio-button value="offline">线下</el-radio-button>
        <el-radio-button value="online">线上</el-radio-button>
      </el-radio-group>
    </div>

    <!-- Filter Bar -->
    <div class="filter-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索活动名称..."
        style="width: 280px"
        clearable
        @clear="handleSearch"
        @keyup.enter="handleSearch"
      >
        <template #suffix>
          <el-icon @click="handleSearch" style="cursor: pointer">
            <Search />
          </el-icon>
        </template>
      </el-input>
      <el-select
        v-model="filterType"
        placeholder="筛选活动类型"
        clearable
        style="width: 180px"
        @change="handleTypeFilter"
      >
        <el-option label="全部类型" value="" />
        <el-option label="线上活动" value="ONLINE" />
        <el-option label="线下活动" value="OFFLINE" />
      </el-select>
    </div>

    <!-- Activity Cards -->
    <div v-if="!loading && activities.length > 0" class="card-grid">
      <div
        v-for="a in activities"
        :key="a.id"
        class="activity-card"
        shadow="hover"
        @click="$router.push(`/student/activities/${a.id}`)"
      >
        <div class="card-image" :class="'gradient-' + (a.id % 5)">
          <div class="card-emoji">{{ getActivityEmoji(a) }}</div>
          <div class="card-type-badge">
            <el-tag :type="activityTypeTagType(a.type)" size="small" effect="dark">
              {{ ACTIVITY_TYPE_MAP[a.type] || a.type }}
            </el-tag>
          </div>
        </div>
        <div class="card-body">
          <h3 class="card-title">{{ a.title }}</h3>
          <p class="card-desc">{{ a.description?.substring(0, 100) }}{{ a.description?.length > 100 ? '...' : '' }}</p>
          <div class="card-meta">
            <span class="meta-item">
              <el-icon><Calendar /></el-icon>
              {{ a.startTime?.substring(0, 10) }}
            </span>
            <span class="meta-item">
              <el-icon><Location /></el-icon>
              {{ a.location?.length > 8 ? a.location.substring(0, 8) + '...' : a.location }}
            </span>
          </div>
          <div class="card-footer">
            <el-tag
              :type="activityStatusTagType(a.status)"
              size="small"
            >
              {{ ACTIVITY_STATUS_MAP[a.status] || a.status }}
            </el-tag>
            <span class="quota">
              {{ a.currentStudents || 0 }}/{{ a.maxStudents }} 人
              <el-progress
                :percentage="a.maxStudents ? Math.round((a.currentStudents || 0) / a.maxStudents * 100) : 0"
                :stroke-width="6"
                :show-text="false"
                style="width: 80px; display: inline-block; vertical-align: middle; margin-left: 6px"
              />
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <el-empty
      v-if="!loading && activities.length === 0"
      description="暂无活动信息"
      :image-size="160"
    />

    <!-- Error State -->
    <el-result
      v-if="errorMessage"
      icon="error"
      title="加载失败"
      :sub-title="errorMessage"
    >
      <template #extra>
        <el-button type="primary" @click="fetchActivities">重新加载</el-button>
      </template>
    </el-result>

    <!-- Pagination -->
    <div v-if="total > pageSize" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next, total"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Calendar, Location } from '@element-plus/icons-vue'
import { activityApi } from '@/api'
import type { Activity } from '@/types'
import { ACTIVITY_TYPE_MAP, ACTIVITY_STATUS_MAP, activityTypeTagType, activityStatusTagType } from '@/utils/constants'

const bannerGradients = ['linear-gradient(135deg, #1a56db, #6366f1)', 'linear-gradient(135deg, #10b981, #059669)', 'linear-gradient(135deg, #f59e0b, #d97706)', 'linear-gradient(135deg, #6366f1, #8b5cf6)', 'linear-gradient(135deg, #ef4444, #dc2626)']
const banners = ref<any[]>([])

async function fetchBanners() {
  try {
    const res: any = await activityApi.getBanners()
    banners.value = (res?.data || res || []).map((a: any, i: number) => ({
      id: a.id, title: a.title, subtitle: a.description?.substring(0, 50) || '', color: bannerGradients[i % 5]
    }))
  } catch { banners.value = [] }
}

const activities = ref<Activity[]>([])
const loading = ref(false)
const errorMessage = ref('')

const activeTab = ref('all')
const keyword = ref('')
const filterType = ref('')
const page = ref(1)
const pageSize = ref(12)
const total = ref(0)

function getActivityEmoji(a: any): string {
  const title = a.title || ''
  if (title.includes('直播') || title.includes('线上')) return '📡'
  if (title.includes('校园') || title.includes('开放日')) return '🏫'
  if (title.includes('夏令营') || title.includes('暑期')) return '⛺'
  if (title.includes('高考') || title.includes('志愿')) return '📝'
  if (title.includes('南疆') || title.includes('北疆')) return '🗺️'
  if (title.includes('短视频') || title.includes('征集')) return '🎬'
  if (title.includes('总结') || title.includes('表彰')) return '🏆'
  if (title.includes('回访') || title.includes('母校')) return '🎓'
  return a.type === 'ONLINE' ? '💻' : '📍'
}

function buildTypeParam(): string | undefined {
  // If a specific type is selected in the dropdown, use it
  if (filterType.value) {
    return filterType.value
  }
  // Otherwise use the tab-based filter
  if (activeTab.value === 'offline') {
    return 'OFFLINE'
  }
  if (activeTab.value === 'online') {
    return 'ONLINE'
  }
  return undefined
}

async function fetchActivities() {
  loading.value = true
  errorMessage.value = ''
  try {
    const res: any = await activityApi.listStudent({
      page: page.value,
      size: pageSize.value,
      keyword: keyword.value || undefined,
      type: buildTypeParam(),
    })

    // Handle different response shapes
    if (res?.data?.list) {
      activities.value = res.data.list
      total.value = res.data.total || 0
    } else if (res?.data?.records) {
      activities.value = res.data.records
      total.value = res.data.total || 0
    } else if (res?.records) {
      activities.value = res.records
      total.value = res.total || 0
    } else if (Array.isArray(res)) {
      activities.value = res
      total.value = res.length
    } else {
      activities.value = []
      total.value = 0
    }
  } catch (err: any) {
    activities.value = []
    total.value = 0
    // The request interceptor already shows ElMessage.error for non-401 errors.
    // Show a more specific message for certain cases.
    const msg = err?.response?.data?.message
    errorMessage.value = msg || '加载活动列表失败，请稍后重试'
    if (err?.response?.status !== 401) {
      ElMessage.error(errorMessage.value)
    }
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchActivities()
}

function handleTabChange() {
  filterType.value = ''
  page.value = 1
  fetchActivities()
}

function handleTypeFilter() {
  page.value = 1
  fetchActivities()
}

function handlePageChange(newPage: number) {
  page.value = newPage
  fetchActivities()
}

onMounted(() => {
  fetchActivities()
  fetchBanners()
})
</script>

<style scoped>
.activity-list {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 4px;
}

/* Banner */
.banner-carousel {
  margin-bottom: 24px;
  border-radius: 12px;
  overflow: hidden;
}

.banner {
  height: 220px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.banner-content {
  text-align: center;
  color: #fff;
}

.banner-content h2 {
  margin: 0 0 8px;
  font-size: 32px;
  font-weight: 700;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.banner-content p {
  margin: 0;
  font-size: 16px;
  opacity: 0.9;
  text-shadow: 0 1px 4px rgba(0, 0, 0, 0.15);
}

/* Tabs */
.tabs-bar {
  margin-bottom: 20px;
}

/* Filter Bar */
.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

/* Card Grid */
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 20px;
}

.activity-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  border: 1px solid #eee;
}

.activity-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(26, 86, 219, 0.15);
}

.card-image {
  position: relative;
  height: 180px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}
.card-image.gradient-0 { background: linear-gradient(135deg, #1a56db, #6366f1); }
.card-image.gradient-1 { background: linear-gradient(135deg, #10b981, #059669); }
.card-image.gradient-2 { background: linear-gradient(135deg, #f59e0b, #d97706); }
.card-image.gradient-3 { background: linear-gradient(135deg, #6366f1, #8b5cf6); }
.card-image.gradient-4 { background: linear-gradient(135deg, #ef4444, #dc2626); }
.card-emoji { font-size: 56px; }

.card-type-badge {
  position: absolute;
  top: 10px;
  right: 10px;
}

.card-body {
  padding: 16px 18px 18px;
}

.card-title {
  margin: 0 0 8px;
  font-size: 17px;
  font-weight: 600;
  color: #1a1a2e;
  line-height: 1.4;
}

.card-desc {
  margin: 0 0 12px;
  font-size: 13px;
  color: #666;
  line-height: 1.6;
  min-height: 40px;
}

.card-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
  font-size: 13px;
  color: #999;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.quota {
  font-size: 12px;
  color: #999;
  display: flex;
  align-items: center;
}

/* Pagination */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding: 20px 0;
}
</style>
