<template>
  <div class="college-activities">
    <h2 class="page-title">活动管理</h2>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card stat-total">
        <div class="stat-icon"><el-icon size="28"><List /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">活动总数</div>
        </div>
      </div>
      <div class="stat-card stat-active">
        <div class="stat-icon"><el-icon size="28"><VideoPlay /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.active }}</div>
          <div class="stat-label">进行中</div>
        </div>
      </div>
      <div class="stat-card stat-enrolling">
        <div class="stat-icon"><el-icon size="28"><EditPen /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.enrolling }}</div>
          <div class="stat-label">报名中</div>
        </div>
      </div>
      <div class="stat-card stat-ended">
        <div class="stat-icon"><el-icon size="28"><CircleClose /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.ended }}</div>
          <div class="stat-label">已结束</div>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-input
        v-model="filterKeyword"
        placeholder="搜索活动名称"
        clearable
        style="width: 220px"
        @clear="handleSearch"
        @keyup.enter="handleSearch"
      />
      <el-select
        v-model="filterType"
        placeholder="活动类型"
        clearable
        style="width: 140px; margin-left: 12px"
        @change="handleSearch"
      >
        <el-option
          v-for="t in typeOptions"
          :key="t"
          :label="t"
          :value="t"
        />
      </el-select>
      <el-select
        v-model="filterStatus"
        placeholder="活动状态"
        clearable
        style="width: 140px; margin-left: 12px"
        @change="handleSearch"
      >
        <el-option label="报名中" value="报名中" />
        <el-option label="进行中" value="进行中" />
        <el-option label="已结束" value="已结束" />
        <el-option label="草稿" value="草稿" />
      </el-select>
      <el-button type="primary" style="margin-left: 12px" @click="handleSearch">搜索</el-button>
      <el-button style="margin-left: 8px" @click="handleReset">重置</el-button>
    </div>

    <!-- 错误提示 -->
    <el-alert
      v-if="errorMsg"
      :title="errorMsg"
      type="error"
      show-icon
      closable
      style="margin-bottom: 16px"
      @close="errorMsg = ''"
    />

    <!-- 表格 -->
    <el-table
      :data="list"
      v-loading="loading"
      element-loading-text="加载中..."
      border
      stripe
    >
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="活动名称" min-width="200" show-overflow-tooltip />
      <el-table-column label="类型" width="110">
        <template #default="{ row }">
          <el-tag type="info" size="small">{{ row.type }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag
            :type="activityStatusTagType(row.status)"
            size="small"
          >
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="报名人数" width="120">
        <template #default="{ row }">
          <span class="enroll-count">
            {{ row.currentStudents }}/{{ row.maxStudents }}
          </span>
          <el-progress
            :percentage="enrollPercent(row)"
            :stroke-width="6"
            :show-text="false"
            style="width: 80px"
          />
        </template>
      </el-table-column>
      <el-table-column prop="location" label="地点" width="140" show-overflow-tooltip />
      <el-table-column prop="startTime" label="开始时间" width="140" />
      <el-table-column prop="endTime" label="结束时间" width="140" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="viewDetail(row.id)">
            查看详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空状态 -->
    <el-empty v-if="!loading && list.length === 0" description="暂无活动数据" />

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @size-change="handleSearch"
        @current-change="handleSearch"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { List, VideoPlay, EditPen, CircleClose } from '@element-plus/icons-vue'
import { activityApi } from '@/api'
import type { Activity } from '@/types'

const router = useRouter()

// ---- 筛选 ----
const filterKeyword = ref('')
const filterType = ref('')
const filterStatus = ref('')

// ---- 分页 ----
const pagination = reactive({ page: 1, size: 20 })
const total = ref(0)

// ---- 列表 ----
const list = ref<Activity[]>([])
const loading = ref(false)
const errorMsg = ref('')

// ---- 类型选项 ----
const typeOptions = ref<string[]>([])

// ---- 统计 ----
const stats = computed(() => {
  const totalCount = list.value.length
  const active = list.value.filter((a) => a.status === '进行中').length
  const enrolling = list.value.filter((a) => a.status === '报名中').length
  const ended = list.value.filter((a) => a.status === '已结束').length
  return { total: totalCount, active, enrolling, ended }
})

// ---- 辅助 ----
function activityStatusTagType(status: string): string {
  const map: Record<string, string> = {
    '报名中': 'success',
    '进行中': '',
    '已结束': 'info',
    '草稿': 'warning',
  }
  return map[status] || 'info'
}

function enrollPercent(row: Activity): number {
  if (!row.maxStudents || row.maxStudents === 0) return 0
  return Math.min(Math.round((row.currentStudents / row.maxStudents) * 100), 100)
}

// ---- 数据获取 ----
async function fetchData() {
  loading.value = true
  errorMsg.value = ''
  try {
    const params: any = {
      page: pagination.page,
      size: pagination.size,
    }
    if (filterKeyword.value) params.keyword = filterKeyword.value
    if (filterType.value) params.type = filterType.value
    if (filterStatus.value) params.status = filterStatus.value

    const res: any = await activityApi.listCollege(params)
    if (res?.data?.records) {
      list.value = res.data.records
      total.value = res.data.total || 0
    } else if (res?.records) {
      list.value = res.records
      total.value = res.total || 0
    } else if (Array.isArray(res?.data)) {
      list.value = res.data
      total.value = res.data.length
    } else if (Array.isArray(res)) {
      list.value = res
      total.value = res.length
    } else {
      list.value = []
      total.value = 0
    }

    // 提取活动类型
    const typeSet = new Set<string>()
    list.value.forEach((a) => {
      if (a.type) typeSet.add(a.type)
    })
    typeOptions.value = Array.from(typeSet)
  } catch (err: any) {
    errorMsg.value = err?.response?.data?.message || err?.message || '加载活动数据失败，请稍后重试'
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  filterKeyword.value = ''
  filterType.value = ''
  filterStatus.value = ''
  pagination.page = 1
  fetchData()
}

function viewDetail(id: number) {
  router.push(`/student/activities/${id}`)
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.college-activities {
  padding: 0;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a3a5c;
  margin-bottom: 20px;
}

/* 统计卡片 */
.stats-row {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16px;
  background: #fff;
  border-radius: 12px;
  padding: 20px 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  border-left: 4px solid transparent;
}

.stat-total { border-left-color: #1a56db; }
.stat-active { border-left-color: #67c23a; }
.stat-enrolling { border-left-color: #409eff; }
.stat-ended { border-left-color: #909399; }

.stat-total .stat-icon { color: #1a56db; }
.stat-active .stat-icon { color: #67c23a; }
.stat-enrolling .stat-icon { color: #409eff; }
.stat-ended .stat-icon { color: #909399; }

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1a3a5c;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #8b949e;
  margin-top: 4px;
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

/* 表格 */
:deep(.el-table) {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.el-table th) {
  background-color: #f7f8fa;
}

/* 报名人数 */
.enroll-count {
  display: block;
  font-size: 13px;
  color: #555;
  margin-bottom: 4px;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
