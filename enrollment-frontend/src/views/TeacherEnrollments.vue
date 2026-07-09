<template>
  <div class="teacher-enrollments">
    <div class="page-header">
      <h2>我的报名记录</h2>
      <p class="subtitle">查看和管理您的活动报名，已通过的活动可以提交反馈</p>
    </div>

    <el-card class="table-card">
      <el-table
        :data="list"
        border
        stripe
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="activityTitle" label="活动名称" min-width="180" />
        <el-table-column prop="submittedAt" label="报名时间" width="170" />
        <el-table-column prop="targetSchool" label="目标学校" width="160" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="collegeName" label="所属学院" width="150" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              @click="$router.push(`/teacher/activities/${row.activityId}`)"
            >
              查看
            </el-button>
            <el-button
              v-if="row.status === 'APPROVED'"
              size="small"
              type="success"
              @click="$router.push(`/teacher/feedback/${row.id}`)"
            >
              反馈
            </el-button>
            <el-button
              v-if="row.status === 'SUBMITTED' || row.status === 'APPROVING'"
              size="small"
              type="danger"
              @click="handleWithdraw(row)"
            >
              撤回
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty
        v-if="!loading && list.length === 0"
        description="暂无报名记录，快去报名参与活动吧"
        style="padding: 60px 0"
      />
    </el-card>

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
import { ElMessage, ElMessageBox } from 'element-plus'
import { enrollmentApi } from '@/api'
import type { Enrollment } from '@/types'

const list = ref<Enrollment[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = 20
const total = ref(0)

const statusMap: Record<string, string> = {
  SUBMITTED: '待审核',
  APPROVING: '审批中',
  APPROVED: '已通过',
  REJECTED: '已拒绝',
  WITHDRAWN: '已撤回',
  DRAFT: '草稿',
}

function statusLabel(status: string): string {
  return statusMap[status] || status
}

function statusType(status: string): string {
  const map: Record<string, string> = {
    APPROVED: 'success',
    SUBMITTED: 'warning',
    APPROVING: '',
    REJECTED: 'danger',
    WITHDRAWN: 'info',
  }
  return map[status] || 'info'
}

async function fetchEnrollments() {
  loading.value = true
  try {
    const res: any = await enrollmentApi.listMy({ page: page.value, size: pageSize })
    if (res?.data?.records) {
      list.value = res.data.records
      total.value = res.data.total || 0
    } else if (res?.records) {
      list.value = res.records
      total.value = res.total || 0
    } else if (Array.isArray(res)) {
      list.value = res
      total.value = res.length
    } else {
      list.value = []
      total.value = 0
    }
  } catch {
    list.value = []
    total.value = 0
    ElMessage.error('加载报名记录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

async function handleWithdraw(row: Enrollment) {
  try {
    await ElMessageBox.confirm(
      `确认撤回对「${row.activityTitle}」的报名申请吗？`,
      '撤回确认',
      {
        confirmButtonText: '确认撤回',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
  } catch {
    return
  }
  try {
    await enrollmentApi.withdraw(row.id)
    ElMessage.success('已成功撤回报名')
    await fetchEnrollments()
  } catch {
    ElMessage.error('撤回失败，请稍后重试')
  }
}

function handlePageChange() {
  fetchEnrollments()
}

onMounted(() => {
  fetchEnrollments()
})
</script>

<style scoped>
.teacher-enrollments {
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

.table-card {
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
}

.table-card :deep(.el-card__body) {
  padding: 0;
}

.table-card :deep(.el-table) {
  border-radius: 12px;
}

.table-card :deep(.el-table th) {
  background-color: #f8fafc;
  color: #374151;
  font-weight: 600;
  font-size: 13px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
