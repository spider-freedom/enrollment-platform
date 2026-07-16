<template>
  <div class="school-activity-list" v-loading="loading">
    <!-- 错误状态 -->
    <el-result
      v-if="error"
      status="error"
      title="数据加载失败"
      sub-title="请检查网络连接后重试"
    >
      <template #extra>
        <el-button type="primary" @click="fetchList">重新加载</el-button>
      </template>
    </el-result>

    <template v-else>
      <!-- 工具栏 -->
      <div class="toolbar">
        <h3>活动管理</h3>
        <el-button type="primary" @click="handleCreate">
          <el-icon style="margin-right: 4px"><Plus /></el-icon>创建活动
        </el-button>
      </div>

      <!-- 筛选栏 -->
      <el-card shadow="never" class="filter-card">
        <el-form :inline="true" :model="filters" class="filter-form">
          <el-form-item label="关键词">
            <el-input
              v-model="filters.keyword"
              placeholder="搜索活动标题"
              clearable
              style="width: 200px"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="线上线下">
            <el-select v-model="filters.type" placeholder="全部" clearable style="width:130px">
              <el-option label="线上活动" value="ONLINE" />
              <el-option label="线下活动" value="OFFLINE" />
            </el-select>
          </el-form-item>
          <el-form-item label="活动分类">
            <el-select v-model="filters.category" placeholder="全部分类" clearable style="width:140px">
              <el-option v-for="c in categoryList" :key="c.value" :label="c.label" :value="c.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="filters.status"
              placeholder="全部状态"
              clearable
              style="width: 140px"
            >
              <el-option label="草稿" value="DRAFT" />
              <el-option label="报名中" value="PUBLISHED" />
              <el-option label="进行中" value="ONGOING" />
              <el-option label="已结束" value="ENDED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
            <el-button type="success" @click="handleExport" icon="Download">导出Excel</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 空状态 -->
      <el-empty
        v-if="!loading && list.length === 0"
        description="暂无活动数据，点击上方按钮创建第一个活动吧"
      />

      <!-- 表格 -->
      <el-table
        v-else
        :data="list"
        border
        stripe
        class="activity-table"
      >
        <el-table-column prop="title" label="活动标题" min-width="160" show-overflow-tooltip />
        <el-table-column label="类型" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="typeTagColor(row.type)" effect="plain" size="small">{{ typeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="活动日期" width="140" align="center">
          <template #default="{ row }">{{ formatDate(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="报名" width="90" align="center">
          <template #default="{ row }">{{ row.currentStudents ?? 0 }}/{{ row.maxStudents ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="审批" width="90">
          <template #default="{ row }">
            <el-tag type="info" effect="plain" size="small">{{ workflowLabel(row.workflowKey) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagColor(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" link @click="handleView(row)">查看</el-button>
            <el-button size="small" :type="row.isBanner === 1 ? 'warning' : 'info'" link @click="toggleBanner(row, row.isBanner !== 1)">{{ row.isBanner === 1 ? '取消轮播' : '设为轮播' }}</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { activityApi } from '@/api'
import { downloadFile } from '@/api/request'
import { ACTIVITY_CATEGORY_FILTERS } from '@/utils/constants'
const categoryList = ACTIVITY_CATEGORY_FILTERS
import type { Activity } from '@/types'

const router = useRouter()

// ============= 状态 =============
const loading = ref(true)
const error = ref(false)
const list = ref<Activity[]>([])
const total = ref(0)

const filters = reactive({
  keyword: '',
  type: '',
  category: '',
  status: '',
})

const pagination = reactive({
  page: 1,
  size: 20,
})

// ============= 辅助函数 =============
function formatDate(dateStr: string) {
  if (!dateStr) return '-'
  return dateStr.length > 10 ? dateStr.slice(0, 16) : dateStr
}

function typeLabel(type: string) {
  const map: Record<string, string> = {
    OFFLINE: '线下宣讲',
    ONLINE: '线上直播',
    OPEN_DAY: '开放日',
    INTERVIEW: '面试',
  }
  return map[type] || type || '-'
}

function typeTagColor(type: string) {
  const map: Record<string, string> = {
    OFFLINE: '',
    ONLINE: 'success',
    OPEN_DAY: 'warning',
    INTERVIEW: 'danger',
  }
  return map[type] || 'info'
}

function audienceLabel(audience: any) {
  const map: Record<string, string> = {
    student: '仅学生',
    teacher: '仅教师',
    all: '学生+教师',
  }
  if (typeof audience === 'number') {
    if (audience === 1) return '仅学生'
    if (audience === 2) return '仅教师'
    if (audience === 3) return '学生+教师'
  }
  return map[String(audience)] || '-'
}

function workflowLabel(key?: string) {
  const map: Record<string, string> = {
    college_school_approval: '学院+校级',
    school_approval: '仅校级',
    college_approval: '仅学院',
  }
  return key ? map[key] || key : '-'
}

function statusLabel(status: string) {
  const map: Record<string, string> = {
    DRAFT: '草稿', PUBLISHED: '报名中', ONGOING: '进行中', ENDED: '已结束',
  }
  return map[status] || status || '-'
}

function statusTagColor(status: string) {
  const map: Record<string, string> = {
    DRAFT: 'info', PUBLISHED: 'success', ONGOING: '', ENDED: 'info',
  }
  return map[status] || 'info'
}

// ============= 数据获取 =============
async function fetchList() {
  loading.value = true
  error.value = false
  try {
    const params: any = {
      page: pagination.page,
      size: pagination.size,
    }
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.type) params.type = filters.type
    if (filters.category) params.category = filters.category
    if (filters.status) params.status = filters.status

    const res = await activityApi.listSchool(params)
    const data = res?.data || res
    if (data && data.list) {
      list.value = data.list
      total.value = data.total ?? 0
    } else if (data && data.records) {
      list.value = data.records
      total.value = data.total ?? 0
    } else if (Array.isArray(data)) {
      list.value = data
      total.value = data.length
    } else {
      list.value = []
      total.value = 0
    }
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

// ============= 筛选 =============
function handleSearch() {
  pagination.page = 1
  fetchList()
}

function handleReset() {
  filters.keyword = ''
  filters.type = ''
  filters.category = ''
  filters.status = ''
  pagination.page = 1
  fetchList()
}

function handlePageChange(page: number) {
  pagination.page = page
  fetchList()
}

function handleSizeChange(size: number) {
  pagination.size = size
  pagination.page = 1
  fetchList()
}

function handleExport() {
  downloadFile('/activity/export', '活动数据导出.xlsx')
}

async function toggleBanner(row: any, val: boolean) {
  try {
    await activityApi.toggleBanner(row.id, val ? 1 : 0)
    row.isBanner = val ? 1 : 0
    ElMessage.success(val ? '已设为轮播' : '已取消轮播')
  } catch { ElMessage.error('操作失败') }
}

// ============= 操作 =============
function handleCreate() {
  router.push('/school/activities/create')
}

function handleEdit(row: Activity) {
  router.push(`/school/activities/create?id=${row.id}`)
}

function handleView(row: Activity) {
  router.push(`/school/activities/${row.id}`)
}

async function handleDelete(row: Activity) {
  try {
    await ElMessageBox.confirm(
      `确认删除活动「${row.title}」吗？删除后将无法恢复。`,
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      },
    )
    try {
      await activityApi.delete(row.id)
      ElMessage.success('活动已删除')
      fetchList()
    } catch {
      ElMessage.error('删除失败，请稍后重试')
    }
  } catch {
    // 用户取消
  }
}

// ============= 生命周期 =============
onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.school-activity-list {
  padding: 4px 0;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.toolbar h3 {
  margin: 0;
  font-size: 18px;
  color: #111827;
}

.filter-card {
  margin-bottom: 16px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-form .el-form-item {
  margin-bottom: 0;
}

.activity-table {
  width: 100%;
}

.date-cell {
  font-size: 12px;
  line-height: 1.6;
}

.date-label {
  color: #9ca3af;
  margin-right: 4px;
}

.full-text {
  color: #ef4444;
  font-weight: 600;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
