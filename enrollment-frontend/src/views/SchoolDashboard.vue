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
                <h1 class="stat-number">{{ animatedStats.totalActivities }}</h1>
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
                <h1 class="stat-number">{{ animatedStats.totalEnrollments }}</h1>
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
                <h1 class="stat-number">{{ animatedStats.approvalRate }}%</h1>
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
                <h1 class="stat-number">{{ animatedStats.avgRating }}</h1>
                <p>平均评分 <span class="stat-badge up">↑ 0.3</span></p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表行1：报名趋势 + 学院分布 -->
      <el-row :gutter="16">
        <el-col :span="12">
          <el-card shadow="hover" class="chart-card">
            <template #header>
              <div class="chart-header">
                <h4>报名趋势</h4>
                <span class="chart-subtitle">近6个月</span>
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
import { ref, reactive, onMounted, onBeforeUnmount, watch } from 'vue'
import * as echarts from 'echarts'
import { statisticsApi, feedbackApi } from '@/api'
import {
  Document,
  UserFilled,
  CircleCheckFilled,
  StarFilled,
} from '@element-plus/icons-vue'

// ============= 状态 =============
const loading = ref(true)
const error = ref(false)

const stats = reactive({
  totalActivities: 0,
  totalEnrollments: 0,
  approvalRate: 0,
  avgRating: 0,
})

// Animated stats for smooth number transitions
const animatedStats = reactive({
  totalActivities: 0,
  totalEnrollments: 0,
  approvalRate: 0,
  avgRating: 0,
})

function animateValue(key: keyof typeof animatedStats, target: number) {
  const start = animatedStats[key]
  const diff = target - start
  if (diff === 0) return
  const duration = 600
  const startTime = performance.now()
  function step(now: number) {
    const elapsed = now - startTime
    const progress = Math.min(elapsed / duration, 1)
    const eased = 1 - Math.pow(1 - progress, 3)
    ;(animatedStats as any)[key] = Math.round((start + diff * eased) * 10) / 10
    if (progress < 1) {
      requestAnimationFrame(step)
    }
  }
  requestAnimationFrame(step)
}

watch(() => stats.totalActivities, (v) => animateValue('totalActivities', v))
watch(() => stats.totalEnrollments, (v) => animateValue('totalEnrollments', v))
watch(() => stats.approvalRate, (v) => animateValue('approvalRate', v))
watch(() => stats.avgRating, (v) => animateValue('avgRating', v))

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
const defaultTrend = {
  months: ['1月', '2月', '3月', '4月', '5月', '6月'],
  values: [80, 120, 200, 350, 480, 620],
}

const defaultCollege = {
  names: ['计算机学院', '数学学院', '物理学院', '化学学院', '生科学院', '人文学院'],
  values: [320, 260, 180, 150, 120, 90],
}

const defaultRating: Record<string, number> = {
  '5分': 580,
  '4分': 320,
  '3分': 150,
  '2分': 80,
  '1分': 30,
}

const defaultTypes: { name: string; label: string; value: number }[] = [
  { name: 'OFFLINE', label: '线下宣讲', value: 18 },
  { name: 'ONLINE', label: '线上直播', value: 8 },
  { name: 'OPEN_DAY', label: '开放日', value: 5 },
  { name: 'INTERVIEW', label: '面试', value: 3 },
]

// ============= 图表初始化 =============
function createLineOption(
  categories: string[],
  data: number[],
  name: string,
) {
  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' },
      backgroundColor: '#fff',
      borderColor: '#e5e7eb',
      textStyle: { color: '#374151' },
    },
    legend: {
      bottom: 0,
      textStyle: { color: '#6b7280' },
    },
    grid: { left: 50, right: 20, top: 20, bottom: 40 },
    xAxis: {
      type: 'category',
      data: categories,
      boundaryGap: false,
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      axisTick: { show: false },
      axisLabel: { color: '#6b7280' },
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f3f4f6' } },
      axisLabel: { color: '#6b7280' },
    },
    series: [
      {
        name,
        type: 'line',
        data,
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { color: '#1a56db', width: 3 },
        itemStyle: { color: '#1a56db' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(26, 86, 219, 0.25)' },
            { offset: 1, color: 'rgba(26, 86, 219, 0.02)' },
          ]),
        },
      },
    ],
  }
}

function createBarOption(categories: string[], data: number[], name: string) {
  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff',
      borderColor: '#e5e7eb',
      textStyle: { color: '#374151' },
    },
    legend: {
      bottom: 0,
      textStyle: { color: '#6b7280' },
    },
    grid: { left: 50, right: 20, top: 20, bottom: 60 },
    xAxis: {
      type: 'category',
      data: categories,
      axisLabel: { rotate: 15, color: '#6b7280', fontSize: 11 },
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      axisTick: { show: false },
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f3f4f6' } },
      axisLabel: { color: '#6b7280' },
    },
    series: [
      {
        name,
        type: 'bar',
        data,
        barWidth: 32,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#3b82f6' },
            { offset: 1, color: '#1a56db' },
          ]),
          borderRadius: [6, 6, 0, 0],
        },
        emphasis: {
          itemStyle: { color: '#1a56db' },
        },
      },
    ],
  }
}

function createPieOption(
  data: { value: number; name: string }[],
  config?: { radius?: [string, string]; center?: [string, string] },
) {
  const colors = ['#3b82f6', '#10b981', '#f59e0b', '#8b5cf6', '#ef4444', '#ec4899']
  return {
    tooltip: {
      trigger: 'item',
      backgroundColor: '#fff',
      borderColor: '#e5e7eb',
      textStyle: { color: '#374151' },
      formatter: '{b}: {c} ({d}%)',
    },
    legend: {
      bottom: 0,
      textStyle: { color: '#6b7280' },
    },
    color: colors,
    series: [
      {
        type: 'pie',
        radius: config?.radius || ['45%', '70%'],
        center: config?.center || ['50%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 4,
          borderColor: '#fff',
          borderWidth: 2,
        },
        label: {
          show: true,
          formatter: '{b}\n{d}%',
          color: '#6b7280',
        },
        emphasis: {
          label: { fontSize: 16, fontWeight: 'bold' },
        },
        data,
      },
    ],
  }
}

function initCharts() {
  if (trendChartRef.value) {
    trendInstance = echarts.init(trendChartRef.value)
    trendInstance.setOption(
      createLineOption(defaultTrend.months, defaultTrend.values, '报名人数'),
    )
  }

  if (collegeChartRef.value) {
    collegeInstance = echarts.init(collegeChartRef.value)
    collegeInstance.setOption(
      createBarOption(defaultCollege.names, defaultCollege.values, '报名数'),
    )
  }

  if (ratingChartRef.value) {
    ratingInstance = echarts.init(ratingChartRef.value)
    const ratingData = Object.entries(defaultRating).map(([k, v]) => ({
      value: v,
      name: k,
    }))
    ratingInstance.setOption(createPieOption(ratingData))
  }

  if (typeChartRef.value) {
    typeInstance = echarts.init(typeChartRef.value)
    const typeData = defaultTypes.map((t) => ({
      value: t.value,
      name: t.label,
    }))
    typeInstance.setOption(
      createPieOption(typeData, { radius: '65%', center: ['50%', '50%'] }),
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

  const trendRes = await statisticsApi.getTrend()
  const trend = trendRes?.data || trendRes
  if (trend && trendInstance) {
    const categories = trend.months || trend.labels || defaultTrend.months
    const values = trend.values || trend.data || defaultTrend.values
    trendInstance.setOption(
      createLineOption(categories, values, '报名人数'),
    )
  }

  const colRes = await statisticsApi.getCollegeDist()
  const col = colRes?.data || colRes
  if (Array.isArray(col) && col.length > 0 && collegeInstance) {
    collegeInstance.setOption(
      createBarOption(
        col.map((c: any) => c.collegeName || c.name || '未知'),
        col.map((c: any) => c.enrollmentCount || c.count || c.value || 0),
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
    if (entries.length > 0) {
      ratingInstance.setOption(createPieOption(entries))
    }
  }
}

async function fetchRecentFeedbacks() {
  const res = await feedbackApi.listSchool({ page: 1, size: 3 })
  const data = res?.data || res
  const records = data?.records || (Array.isArray(data) ? data : [])
  recentFeedbacks.value = records.slice(0, 3)
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
  await fetchAll()

  // 延迟初始化图表以确保DOM渲染完成
  setTimeout(() => {
    initCharts()
  }, 100)

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
