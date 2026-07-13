<template>
  <div class="teacher-activity-list">
    <div class="page-header">
      <h2>招生宣传活动</h2>
      <p class="subtitle">浏览并报名参与招生宣传活动，为学校招生工作贡献力量</p>
    </div>

    <el-carousel height="220px" class="banner-carousel">
      <el-carousel-item v-for="(b, i) in banners" :key="i">
        <div class="banner" :style="{ background: b.color }">
          <span class="banner-title">{{ b.title }}</span>
          <span class="banner-desc">{{ b.desc }}</span>
        </div>
      </el-carousel-item>
    </el-carousel>

    <div class="filter-bar">
      <el-input
        v-model="search"
        placeholder="搜索活动名称或描述..."
        style="width: 280px"
        clearable
        @clear="handleSearch"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select
        v-model="filterType"
        placeholder="全部类型"
        clearable
        style="width: 160px"
        @change="handleSearch"
      >
        <el-option label="全部类型" value="" />
        <el-option label="线上活动" value="ONLINE" />
        <el-option label="线下活动" value="OFFLINE" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <div v-loading="loading" class="content-area">
      <el-empty v-if="!loading && activities.length === 0" description="暂无活动数据" />

      <el-row v-else-if="!loading && activities.length > 0" :gutter="20">
        <el-col
          v-for="a in activities"
          :key="a.id"
          :xs="24"
          :sm="12"
          :md="8"
          style="margin-bottom: 20px"
        >
          <el-card
            class="activity-card"
            shadow="hover"
            @click="goDetail(a.id)"
          >
            <div class="card-banner" :class="'gradient-' + (a.id % 5)">
              <span class="card-emoji">{{ getEmoji(a) }}</span>
            </div>
            <div class="card-body">
              <h3 class="card-title">{{ a.title }}</h3>
              <p class="card-desc">{{ a.description?.substring(0, 80) }}{{ a.description?.length > 80 ? '...' : '' }}</p>
              <div class="card-meta">
                <el-tag size="small" :type="activityTypeTagType(a.type)">{{ ACTIVITY_TYPE_MAP[a.type] || a.type }}</el-tag>
                <span class="card-location">
                  <el-icon><Location /></el-icon>
                  {{ a.location }}
                </span>
              </div>
              <div class="card-footer">
                <span class="teacher-count">
                  教师 {{ a.currentTeachers || 0 }}/{{ a.maxTeachers || 0 }}
                </span>
                <span class="card-date">{{ a.startTime }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div v-if="total > pageSize" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        background
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Location } from '@element-plus/icons-vue'
import { activityApi } from '@/api'
import type { Activity } from '@/types'
import { ACTIVITY_TYPE_MAP, activityTypeTagType } from '@/utils/constants'

const router = useRouter()

const banners = [
  { title: '欢迎报考新疆大学', desc: '百年名校，培育英才', color: 'linear-gradient(135deg, #1a56db, #1e40af)' },
  { title: '2026年招生宣传活动', desc: '汇聚优秀教师，共展学校风采', color: 'linear-gradient(135deg, #2563eb, #7c3aed)' },
  { title: '携手同行，筑梦未来', desc: '让更多学子了解新疆大学的魅力', color: 'linear-gradient(135deg, #3b82f6, #06b6d4)' },
]

const search = ref('')
const filterType = ref('')
const page = ref(1)
const pageSize = 12
const total = ref(0)
const loading = ref(false)
const activities = ref<Activity[]>([])

function getEmoji(a: any): string {
  const t = a.title || ''
  if (t.includes('直播')||t.includes('线上')) return '📡'
  if (t.includes('校园')||t.includes('开放日')) return '🏫'
  if (t.includes('夏令营')||t.includes('暑期')) return '⛺'
  if (t.includes('高考')||t.includes('志愿')) return '📝'
  if (t.includes('南疆')||t.includes('北疆')) return '🗺️'
  if (t.includes('短视频')||t.includes('征集')) return '🎬'
  if (t.includes('总结')||t.includes('表彰')) return '🏆'
  if (t.includes('回访')||t.includes('母校')) return '🎓'
  return a.type === 'ONLINE' ? '💻' : '📍'
}

async function fetchActivities() {
  loading.value = true
  try {
    const params: any = { page: page.value, size: pageSize }
    if (search.value) params.keyword = search.value
    if (filterType.value) params.type = filterType.value

    const res: any = await activityApi.listTeacher(params)
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
  } catch {
    activities.value = []
    total.value = 0
    ElMessage.error('加载活动列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchActivities()
}

function handlePageChange() {
  fetchActivities()
}

function goDetail(id: number) {
  router.push(`/teacher/activities/${id}`)
}

onMounted(() => {
  fetchActivities()
})
</script>

<style scoped>
.teacher-activity-list {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 16px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 6px 0;
}

.page-header .subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.banner-carousel {
  margin-bottom: 24px;
  border-radius: 12px;
  overflow: hidden;
}

.banner {
  height: 220px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.banner-title {
  font-size: 30px;
  font-weight: 700;
  margin-bottom: 8px;
}

.banner-desc {
  font-size: 16px;
  opacity: 0.85;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.content-area {
  min-height: 300px;
}

.activity-card {
  cursor: pointer;
  border-radius: 12px;
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;
  border: 1px solid #e5e7eb;
}

.activity-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(26, 86, 219, 0.12);
}

.card-banner {
  width: 100%; height: 160px; display: flex; align-items: center; justify-content: center;
}
.card-banner.gradient-0 { background: linear-gradient(135deg,#1a56db,#6366f1); }
.card-banner.gradient-1 { background: linear-gradient(135deg,#10b981,#059669); }
.card-banner.gradient-2 { background: linear-gradient(135deg,#f59e0b,#d97706); }
.card-banner.gradient-3 { background: linear-gradient(135deg,#6366f1,#8b5cf6); }
.card-banner.gradient-4 { background: linear-gradient(135deg,#ef4444,#dc2626); }
.card-emoji { font-size: 52px; }

.card-body {
  padding: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.card-desc {
  font-size: 13px;
  color: #6b7280;
  margin: 0 0 12px 0;
  line-height: 1.5;
  min-height: 36px;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.card-location {
  font-size: 12px;
  color: #9ca3af;
  display: flex;
  align-items: center;
  gap: 2px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid #f3f4f6;
}

.teacher-count {
  font-size: 12px;
  color: #1a56db;
  font-weight: 500;
}

.card-date {
  font-size: 12px;
  color: #9ca3af;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
