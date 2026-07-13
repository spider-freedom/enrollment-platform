<template>
  <div class="enrollments" v-loading="loading">
    <div class="page-header">
      <h3>我的报名</h3>
    </div>

    <el-table
      v-if="!loading && list.length > 0"
      :data="list"
      border
      stripe
      style="width: 100%"
    >
      <el-table-column prop="activityTitle" label="活动名称" min-width="200" />
      <el-table-column prop="collegeName" label="学院" width="160" />
      <el-table-column prop="targetSchool" label="目标学院" width="180" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="submittedAt" label="报名时间" width="180" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button
            size="small"
            @click="$router.push(`/student/activities/${row.activityId}`)"
          >
            查看
          </el-button>
          <el-button
            v-if="row.status === 'APPROVED'"
            size="small"
            type="success"
            @click="$router.push(`/student/feedback/${row.id}`)"
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

    <!-- Empty State -->
    <el-empty
      v-if="!loading && list.length === 0 && !errorMessage"
      description="暂无报名记录"
      :image-size="160"
    >
      <template #default>
        <el-button type="primary" @click="$router.push('/student/activities')">
          去看看活动
        </el-button>
      </template>
    </el-empty>

    <!-- Error State -->
    <el-result
      v-if="!loading && errorMessage"
      icon="error"
      title="加载失败"
      :sub-title="errorMessage"
    >
      <template #extra>
        <el-button type="primary" @click="fetchEnrollments">重新加载</el-button>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { enrollmentApi } from '@/api'
import type { Enrollment } from '@/types'

const list = ref<Enrollment[]>([])
const loading = ref(false)
const errorMessage = ref('')
const page = ref(1)
const pageSize = ref(10)
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

function statusType(status: string) {
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
  errorMessage.value = ''
  try {
    const res: any = await enrollmentApi.listMy({
      page: page.value,
      size: pageSize.value,
    })

    // Handle different response shapes
    if (res?.data?.list) {
      list.value = res.data.list
      total.value = res.data.total || 0
    } else if (res?.data?.records) {
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
  } catch (err: any) {
    list.value = []
    total.value = 0
    errorMessage.value = err?.response?.data?.message || '加载报名记录失败，请稍后重试'
    if (err?.response?.status !== 401) {
      ElMessage.error(errorMessage.value)
    }
  } finally {
    loading.value = false
  }
}

async function handleWithdraw(row: Enrollment) {
  try {
    await ElMessageBox.confirm('确认撤回该报名申请？', '提示', {
      type: 'warning',
      confirmButtonText: '确认撤回',
      cancelButtonText: '取消',
    })
  } catch {
    return // User cancelled
  }

  try {
    await enrollmentApi.withdraw(row.id)
    ElMessage.success('已成功撤回报名')
    // Refresh the list to get updated data from server
    await fetchEnrollments()
  } catch (err: any) {
    // The request interceptor already shows ElMessage.error.
    // Don't silently set status — let the server be the source of truth.
    const msg = err?.response?.data?.message || '撤回失败，请稍后重试'
    if (err?.response?.status !== 401) {
      ElMessage.error(msg)
    }
  }
}

function handlePageChange(newPage: number) {
  page.value = newPage
  fetchEnrollments()
}

onMounted(() => {
  fetchEnrollments()
})
</script>

<style scoped>
.enrollments {
  max-width: 1280px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding: 16px 0;
}
</style>
