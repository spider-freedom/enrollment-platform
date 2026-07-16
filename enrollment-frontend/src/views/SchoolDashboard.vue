<template>
  <div class="school-dashboard" v-loading="loading">
    <!-- 错误状态 -->
    <el-result
      v-if="error"
      status="error"
      title="数据加载失败"
      sub-title="请检查网络连接后重试"
    >
      <template #extra>
        <el-button type="primary" @click="fetchAll">重新加载</el-button>
      </template>
    </el-result>

    <!-- 正常内容 -->
    <template v-else>
      <h3>数据大屏</h3>

      <!-- 统计卡片 -->
      <el-row :gutter="16" class="stat-row">
        <el-col :span="6">
          <el-card class="stat-card stat-card-blue" shadow="hover">
            <div class="stat-card-inner">
              <div class="stat-icon stat-icon-blue">
                <el-icon :size="28"><Document /></el-icon>
              </div>
              <div class="stat-content">
                <h1 class="stat-number">{{ stats.totalActivities }}</h1>
                <p>活动总数 <span class="stat-badge up">↑ 12%</span></p>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card stat-card-green" shadow="hover">
            <div class="stat-card-inner">
              <div class="stat-icon stat-icon-green">
                <el-icon :size="28"><UserFilled /></el-icon>
              </div>
              <div class="stat-content">
                <h1 class="stat-number">{{ stats.totalEnrollments }}</h1>
                <p>报名总数 <span class="stat-badge up">↑ 23%</span></p>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card stat-card-orange" shadow="hover">
            <div class="stat-card-inner">
              <div class="stat-icon stat-icon-orange">
                <el-icon :size="28"><CircleCheckFilled /></el-icon>
              </div>
              <div class="stat-content">
                <h1 class="stat-number">{{ stats.approvalRate }}%</h1>
                <p>通过率 <span class="stat-badge up">↑ 5%</span></p>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card stat-card-purple" shadow="hover">
            <div class="stat-card-inner">
              <div class="stat-icon stat-icon-purple">
                <el-icon :size="28"><StarFilled /></el-icon>
              </div>
              <div class="stat-content">
                <h1 class="stat-number">{{ stats.avgRating }}</h1>
                <p>平均评分 <span class="stat-badge up">↑ 0.3</span></p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表行1：活动分类统计 + 学院分布 -->
      <el-row :gutter="16">
        <el-col :span="12">
          <el-card shadow="hover" class="chart-card">
            <template #header>
              <div class="chart-header">
                <h4>活动分类统计</h4>
              </div>
            </template>
            <div ref="trendChartRef" class="chart-container" />
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover" class="chart-card">
            <template #header>
              <div class="chart-header">
                <h4>各学院报名分布</h4>
              </div>
            </template>
            <div ref="collegeChartRef" class="chart-container" />
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表行2：评分分布 + 活动类型分布 -->
      <el-row :gutter="16" class="chart-row">
        <el-col :span="12">
          <el-card shadow="hover" class="chart-card">
            <template #header>
              <div class="chart-header">
                <h4>评分分布</h4>
              </div>
            </template>
            <div ref="ratingChartRef" class="chart-container" />
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover" class="chart-card">
            <template #header>
              <div class="chart-header">
                <h4>活动类型分布</h4>
              </div>
            </template>
            <div ref="typeChartRef" class="chart-container" />
          </el-card>
        </el-col>
      </el-row>

      <!-- 最近反馈 -->
      <el-row :gutter="16" class="chart-row">
        <el-col :span="24">
          <el-card shadow="hover" class="chart-card">
            <template #header>
              <div class="chart-header">
                <h4>最近反馈</h4>
              </div>
            </template>
            <div v-if="recentFeedbacks.length === 0" class="empty-feedbacks">
              暂无反馈记录
            </div>
            <div v-else class="feedback-list">
              <div
                v-for="(fb, idx) in recentFeedbacks"
                :key="idx"
                class="feedback-item"
              >
                <div class="feedback-item-header">
                  <span class="feedback-user">{{ fb.userName }}</span>
                  <el-rate
                    :model-value="fb.rating"
                    disabled
                    size="small"
                    style="display: inline-flex"
                  />
                  <span class="feedback-time">{{ fb.createTime }}</span>
                </div>
                <div class="feedback-item-content">{{ fb.content }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { statisticsApi, feedbackApi, activityApi } from '@/api'
import { Document, UserFilled, CircleCheckFilled, StarFilled } from '@element-plus/icons-vue'
import { createBarOption, createPieOption } from '@/utils/charts'

// ============= 状态 =============
const loading = ref(true)
const error = ref(false)

const stats = reactive({
  totalActivities: 0,
  totalEnrollments: 0,
  approvalRate: 0,
  avgRating: 0,
})


const recentFeedbacks = ref<any[]>([])

// 图表DOM引用
const trendChartRef = ref<HTMLElement>()
const collegeChartRef = ref<HTMLElement>()
const ratingChartRef = ref<HTMLElement>()
const typeChartRef = ref<HTMLElement>()

// ECharts 实例
let trendInstance: echarts.ECharts | null = null
let collegeInstance: echarts.ECharts | null = null
let ratingInstance: echarts.ECharts | null = null
let typeInstance: echarts.ECharts | null = null

// ============= 默认 Mock 数据 =============
const defaultCategory = {
  names: ['宣讲会', '开放日', '夏令营', '咨询会', '线上直播', '其他'],
  values: [3, 2, 1, 2, 2, 0],
}


function initCharts() {
  if (trendChartRef.value) {
    trendInstance = echarts.init(trendChartRef.value)
    trendInstance.setOption(createBarOption(['加载中'], [0], '活动数'))
  }

  if (collegeChartRef.value) {
    collegeInstance = echarts.init(collegeChartRef.value)
    collegeInstance.setOption(createBarOption(['加载中'], [0], '报名数'))
  }

  if (ratingChartRef.value) {
    ratingInstance = echarts.init(ratingChartRef.value)
    ratingInstance.setOption(createPieOption([{ value: 1, name: '加载中...' }]))
  }

  if (typeChartRef.value) {
    typeInstance = echarts.init(typeChartRef.value)
    typeInstance.setOption(
      createPieOption([{ value: 1, name: '加载中...' }], { radius: '65%', center: ['50%', '50%'] }),
    )
  }
}

// ============= 数据获取 =============
async function fetchStats() {
  const res = await statisticsApi.getDashboard()
  const dash = res?.data || res
  if (dash) {
    stats.totalActivities = dash.totalActivities ?? 0
    stats.totalEnrollments = dash.totalEnrollments ?? 0
    stats.approvalRate = dash.approvalRate ?? 0
    stats.avgRating = dash.avgRating ?? 0
  }

  // Fetch category stats for the first chart
  const catRes: any = await activityApi.listSchool({ page: 1, size: 100 })
  const catData = catRes?.data || catRes
  const catList: any[] = catData?.list || catData?.records || []
  if (trendInstance && catList.length > 0) {
    const catMap: Record<string, number> = {}
    catList.forEach((a: any) => { const c = a.category || '其他'; catMap[c] = (catMap[c] || 0) + 1 })
    const names = Object.keys(catMap)
    const values = Object.values(catMap)
    trendInstance.setOption(createBarOption(names, values, '活动数'))
  }

  const colRes = await statisticsApi.getCollegeDist()
  const col = colRes?.data || colRes
  const colList = col?.list || col?.records || (Array.isArray(col) ? col : [])
  if (colList.length > 0 && collegeInstance) {
    collegeInstance.setOption(
      createBarOption(
        colList.map((c: any) => c.collegeName || c.name || '未知'),
        colList.map((c: any) => c.enrollmentCount || c.count || c.value || 0),
        '报名数',
      ),
    )
  }

  const rateRes = await statisticsApi.getRatingDist()
  const rateData = rateRes?.data || rateRes
  if (rateData && ratingInstance) {
    let entries: { value: number; name: string }[] = []
    if (typeof rateData === 'object' && !Array.isArray(rateData)) {
      entries = Object.entries(rateData).map(([k, v]) => ({
        value: Number(v),
        name: k.includes('分') ? k : `${k}分`,
      }))
    }
    ratingInstance.setOption(entries.length > 0 ? createPieOption(entries) : createPieOption([{value:1,name:'暂无数据'}]))
  }

  // Type distribution from activity category data
  if (typeInstance && catList.length > 0) {
    const typeMap: Record<string, number> = {}
    catList.forEach((a: any) => { const t = a.type === 'ONLINE' ? '线上活动' : '线下活动'; typeMap[t] = (typeMap[t] || 0) + 1 })
    const tNames = Object.keys(typeMap), tValues = Object.values(typeMap)
    typeInstance.setOption(createPieOption(tNames.map((n,i)=>({value:tValues[i],name:n})), {radius:'65%',center:['50%','50%']}))
  }
}

async function fetchRecentFeedbacks() {
  try {
    const res = await feedbackApi.listSchool({ page: 1, size: 3 })
    const data = res?.data || res
    const records = data?.list || data?.records || (Array.isArray(data) ? data : [])
    recentFeedbacks.value = records.slice(0, 3)
  } catch { recentFeedbacks.value = [] }
}

async function fetchAll() {
  loading.value = true
  error.value = false
  try {
    await Promise.all([fetchStats(), fetchRecentFeedbacks()])
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

// ============= 窗口resize =============
function handleResize() {
  trendInstance?.resize()
  collegeInstance?.resize()
  ratingInstance?.resize()
  typeInstance?.resize()
}

// ============= 生命周期 =============
onMounted(async () => {
  // Init charts first (show loading), then fetch data
  initCharts()
  await fetchAll()

  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendInstance?.dispose()
  collegeInstance?.dispose()
  ratingInstance?.dispose()
  typeInstance?.dispose()
})
</script>

<style scoped>
.school-dashboard {
  padding: 4px 0;
}

.stat-row {
  margin-bottom: 16px;
}

/* ========== Stat Cards with Gradient Headers ========== */
.stat-card {
  cursor: default;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  border-radius: 12px;
  overflow: hidden;
  border: none;
  position: relative;
}
.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  border-radius: 12px 12px 0 0;
}
.stat-card-blue::before {
  background: linear-gradient(90deg, #1a56db, #3b82f6);
}
.stat-card-green::before {
  background: linear-gradient(90deg, #16a34a, #22c55e);
}
.stat-card-orange::before {
  background: linear-gradient(90deg, #ea580c, #f97316);
}
.stat-card-purple::before {
  background: linear-gradient(90deg, #7c3aed, #a855f7);
}

:deep(.stat-card .el-card__body) {
  padding: 20px 20px 20px;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-card-inner {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.stat-icon-blue {
  background: linear-gradient(135deg, #3b82f6, #1a56db);
}

.stat-icon-green {
  background: linear-gradient(135deg, #22c55e, #16a34a);
}

.stat-icon-orange {
  background: linear-gradient(135deg, #f97316, #ea580c);
}

.stat-icon-purple {
  background: linear-gradient(135deg, #a855f7, #7c3aed);
}

.stat-content h1.stat-number {
  font-size: 30px;
  margin: 0;
  color: #111827;
  font-weight: 700;
  transition: color 0.3s ease;
}

.stat-content p {
  color: #6b7280;
  margin: 4px 0 0;
  font-size: 13px;
}

.stat-badge {
  display: inline-block;
  font-size: 12px;
  padding: 1px 6px;
  border-radius: 4px;
  font-weight: 600;
}
.stat-badge.up {
  color: #10b981;
  background: rgba(16, 185, 129, 0.1);
}
.stat-badge.down {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
}

/* ========== Chart Cards ========== */
.chart-card {
  margin-bottom: 0;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  transition: box-shadow 0.25s ease, transform 0.25s ease;
}
.chart-card:hover {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.chart-row {
  margin-top: 16px;
}

.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.chart-header h4 {
  margin: 0;
  font-size: 15px;
  color: #1f2937;
  font-weight: 600;
}

.chart-subtitle {
  font-size: 12px;
  color: #9ca3af;
}

.chart-container {
  width: 100%;
  height: 300px;
}

/* Recent Feedbacks */
.empty-feedbacks {
  text-align: center;
  color: #9ca3af;
  padding: 40px 0;
  font-size: 14px;
}

.feedback-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feedback-item {
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
  border: 1px solid #f3f4f6;
  transition: background 0.2s ease;
}
.feedback-item:hover {
  background: #f3f4f6;
}

.feedback-item-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.feedback-user {
  font-weight: 600;
  color: #111827;
  font-size: 14px;
}

.feedback-time {
  color: #9ca3af;
  font-size: 12px;
  margin-left: auto;
}

.feedback-item-content {
  color: #4b5563;
  font-size: 13px;
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
