<template>
  <div class="school-approvals" v-loading="loading">
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
      <h3>报名审批（校级）</h3>

      <!-- 统计卡片 -->
      <el-row :gutter="16" class="stats-row">
        <el-col :span="6">
          <div class="mini-stat" style="border-left-color: #1a56db">
            <div class="mini-stat-value">{{ stats.total }}</div>
            <div class="mini-stat-label">审批总数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="mini-stat" style="border-left-color: #f59e0b">
            <div class="mini-stat-value">{{ stats.pending }}</div>
            <div class="mini-stat-label">待审批</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="mini-stat" style="border-left-color: #16a34a">
            <div class="mini-stat-value">{{ stats.approved }}</div>
            <div class="mini-stat-label">已通过</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="mini-stat" style="border-left-color: #ef4444">
            <div class="mini-stat-value">{{ stats.approvalRate }}%</div>
            <div class="mini-stat-label">通过率</div>
          </div>
        </el-col>
      </el-row>

      <!-- 筛选栏 -->
      <el-card shadow="never" class="filter-card">
        <el-form :inline="true" :model="filters" class="filter-form">
          <el-form-item label="活动">
            <el-select
              v-model="filters.activityId"
              placeholder="全部活动"
              clearable
              style="width: 200px"
              filterable
            >
              <el-option
                v-for="a in activityOptions"
                :key="a.id"
                :label="a.title"
                :value="a.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="学院">
            <el-select
              v-model="filters.college"
              placeholder="全部学院"
              clearable
              style="width: 160px"
            >
              <el-option
                v-for="c in collegeOptions"
                :key="c"
                :label="c"
                :value="c"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select
              v-model="filters.status"
              placeholder="全部状态"
              clearable
              style="width: 140px"
            >
              <el-option label="待学院审批" value="SUBMITTED" />
              <el-option label="待学校审批" value="APPROVING" />
              <el-option label="已通过" value="APPROVED" />
              <el-option label="已驳回" value="REJECTED" />
              <el-option label="已通过" value="APPROVED" />
              <el-option label="已拒绝" value="REJECTED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
            <el-button type="success" @click="handleExport" icon="Download">导出Excel</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 批量操作栏 -->
      <div class="batch-bar" v-if="selectedRows.length > 0">
        <span class="batch-info">已选择 {{ selectedRows.length }} 条记录</span>
        <el-button type="success" size="small" @click="handleBatchApprove">
          批量通过
        </el-button>
        <el-button type="danger" size="small" @click="handleBatchReject">
          批量拒绝
        </el-button>
      </div>

      <!-- 空状态 -->
      <el-empty
        v-if="!loading && list.length === 0"
        description="暂无审批记录"
      />

      <!-- 表格 -->
      <el-table
        v-else
        :data="list"
        border
        stripe
        class="approval-table"
        @selection-change="handleSelectionChange"
        ref="tableRef"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="applicantName" label="申请人" width="100" />
        <el-table-column label="角色" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.applicantRole === 'TEACHER' ? 'warning' : ''" effect="plain">
              {{ row.applicantRole === 'STUDENT' ? '学生' : row.applicantRole === 'TEACHER' ? '教师' : row.applicantRole || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="collegeName" label="学院" width="120" show-overflow-tooltip />
        <el-table-column prop="activityTitle" label="活动" min-width="160" show-overflow-tooltip />
        <el-table-column prop="targetSchool" label="来源学校" width="140" show-overflow-tooltip />
        <el-table-column label="GPA" width="80" align="center">
          <template #default="{ row }">
            {{ row.gpa ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column label="学院审批" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="collegeStatusColor(row)" size="small">
              {{ collegeStatusLabel(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="当前状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.currentStatus)" size="small">
              {{ statusLabel(row.currentStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="160" />
        <el-table-column label="操作" width="230" fixed="right" align="center">
          <template #default="{ row }">
            <template v-if="row.currentStatus === 'APPROVING'">
              <el-button size="small" type="success" link @click="openApproveDialog(row)">
                通过
              </el-button>
              <el-button size="small" type="danger" link @click="openRejectDialog(row)">
                拒绝
              </el-button>
              <el-button size="small" type="warning" plain @click="handleAiSuggest(row)">
                🤖 AI
              </el-button>
            </template>
            <span v-else class="no-action">--</span>
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

    <!-- 审批弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form :model="dialogForm" label-width="80px">
        <el-form-item label="申请人">
          <span>{{ currentRow?.userName }}</span>
        </el-form-item>
        <el-form-item label="活动">
          <span>{{ currentRow?.activityTitle }}</span>
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input
            v-model="dialogForm.comment"
            type="textarea"
            :rows="3"
            :placeholder="dialogAction === 'approve' ? '审批通过（选填）' : '请填写拒绝原因'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button
          :type="dialogAction === 'approve' ? 'success' : 'danger'"
          :loading="dialogSubmitting"
          @click="confirmDialog"
        >
          {{ dialogAction === 'approve' ? '确认通过' : '确认拒绝' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- AI 审批建议弹窗 -->
    <el-dialog v-model="aiVisible" title="AI 审批建议" width="520px" destroy-on-close>
      <div class="ai-dialog-content" v-if="aiResult">
        <div class="ai-student-info">
          <p>申请人：<strong>{{ aiCurrentRow?.userName }}</strong> | 活动：{{ aiCurrentRow?.activityTitle }}</p>
        </div>
        <div class="ai-suggestion" :class="aiSuggestionClass">
          <span class="ai-label">建议：</span>
          <span class="ai-value">{{ aiResult.suggestion }}</span>
        </div>
        <div class="ai-reason">
          <span class="ai-label">理由：</span>
          <span>{{ aiResult.reason }}</span>
        </div>
        <div class="ai-risk">
          <span class="ai-label">风险点：</span>
          <span>{{ aiResult.risk }}</span>
        </div>
      </div>
      <div v-else-if="aiLoading" class="ai-loading" v-loading="true" element-loading-text="AI 分析中..."></div>
      <template #footer>
        <el-button @click="aiVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { aiApi, approvalApi, activityApi } from '@/api'
import { downloadFile } from '@/api/request'
import type { Enrollment } from '@/types'

// ============= 状态 =============
const loading = ref(true)
const error = ref(false)
const list = ref<Enrollment[]>([])
const total = ref(0)
const selectedRows = ref<Enrollment[]>([])
const tableRef = ref()

const stats = reactive({
  total: 0,
  pending: 0,
  approved: 0,
  approvalRate: 0,
})

const filters = reactive({
  activityId: '' as string | number,
  college: '',
  status: '',
})

const pagination = reactive({
  page: 1,
  size: 20,
})

// 筛选选项
const activityOptions = ref<{ id: number; title: string }[]>([])
const collegeOptions = ref<string[]>([
  '计算机学院', '数学学院', '物理学院', '化学学院', '生科学院', '人文学院',
])

// 弹窗
const dialogVisible = ref(false)
const dialogSubmitting = ref(false)
const dialogAction = ref<'approve' | 'reject'>('approve')
const dialogForm = reactive({ comment: '' })
const currentRow = ref<Enrollment | null>(null)

const dialogTitle = computed(() =>
  dialogAction.value === 'approve' ? '审批通过' : '审批拒绝',
)

// ---- AI 审批建议 ----
const aiVisible = ref(false)
const aiLoading = ref(false)
const aiResult = ref<any>(null)
const aiCurrentRow = ref<Enrollment | null>(null)

const aiSuggestionClass = computed(() => {
  if (!aiResult.value) return ''
  const s = aiResult.value.suggestion
  if (s === '建议通过') return 'ai-suggest-pass'
  if (s === '建议谨慎') return 'ai-suggest-caution'
  if (s === '建议拒绝') return 'ai-suggest-reject'
  return ''
})

function roleLabelAI(role: string) {
  if (role === 'student') return '学生'
  if (role === 'teacher') return '教师'
  return role || '-'
}

async function handleAiSuggest(row: Enrollment) {
  aiCurrentRow.value = row
  aiVisible.value = true
  aiLoading.value = true
  aiResult.value = null
  try {
    const res: any = await aiApi.approvalSuggest({
      studentName: row.applicantName,
      studentRole: roleLabelAI(row.applicantRole),
      collegeName: (row as any).collegeName || '',
      gpa: (row as any).gpa ?? null,
      targetSchool: row.targetSchool,
      activityTitle: row.activityTitle,
      currentCount: (row as any).currentCount || (row as any).activityCurrentCount || 0,
      maxCount: (row as any).maxCount || (row as any).activityMaxCount || 0,
    })
    aiResult.value = res?.data || res
  } catch (err: any) {
    aiResult.value = {
      suggestion: 'AI服务不可用',
      reason: err?.response?.data?.message || err?.message || '请求失败',
      risk: '无',
    }
  } finally {
    aiLoading.value = false
  }
}

// ============= 辅助函数 =============
function statusType(status: string) {
  const map: Record<string, string> = {
    APPROVED: 'success',
    REJECTED: 'danger',
    APPROVING: 'warning',
    SUBMITTED: 'info',
    已通过: 'success',
    已拒绝: 'danger',
    待审批: 'warning',
    待审核: 'warning',
  }
  return map[status] || 'info'
}

function statusLabel(status: string) {
  const map: Record<string, string> = {
    APPROVED: '已通过',
    REJECTED: '已拒绝',
    APPROVING: '待学校审批',
    SUBMITTED: '待学院审批',
    已通过: '已通过',
    已拒绝: '已拒绝',
    待审批: '待审批',
    待审核: '待审核',
  }
  return map[status] || status || '-'
}

function collegeStatusLabel(row: any) {
  if (row.collegeApprovalStatus) return row.collegeApprovalStatus
  // 推断
  if (row.currentStatus === 'SUBMITTED') return '待审批'
  if (row.currentStatus === 'APPROVED') return '已通过'
  if (row.collegeApproved) return '已通过'
  return '-'
}

function collegeStatusColor(row: any) {
  const s = collegeStatusLabel(row)
  if (s === '已通过') return 'success'
  if (s === '已拒绝') return 'danger'
  if (s === '待审批') return 'warning'
  return 'info'
}

// ============= 数据获取 =============
async function loadActivities() {
  try {
    const res = await activityApi.listSchool({ page: 1, size: 200 })
    const data = res?.data || res
    const records = data?.records || (Array.isArray(data) ? data : [])
    activityOptions.value = records.map((a: any) => ({
      id: a.id,
      title: a.title,
    }))
  } catch {
    // 静默失败
  }
}

async function fetchList() {
  loading.value = true
  error.value = false
  try {
    const params: any = {
      page: pagination.page,
      size: pagination.size,
    }
    if (filters.activityId) params.activityId = filters.activityId
    if (filters.college) params.collegeName = filters.college
    if (filters.status) params.status = filters.status

    const res = await approvalApi.listSchool(params)
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

    // 计算统计
    updateStats()
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

function updateStats() {
  stats.total = total.value
  stats.pending = list.value.filter(
    (e) => e.currentStatus === 'APPROVING' || e.currentStatus === '待审批' || e.currentStatus === '待审核',
  ).length
  stats.approved = list.value.filter(
    (e) => e.status === 'APPROVED' || e.status === '已通过',
  ).length
  stats.approvalRate = stats.total > 0
    ? Math.round((stats.approved / stats.total) * 100)
    : 0
}

// ============= 筛选 =============
function handleSearch() {
  pagination.page = 1
  fetchList()
}

function handleReset() {
  filters.activityId = ''
  filters.college = ''
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
  downloadFile('/enrollment/export?status=' + (filters.status || ''), '报名数据导出.xlsx')
}

function handleSelectionChange(rows: Enrollment[]) {
  selectedRows.value = rows
}

// ============= 审批弹窗 =============
function openApproveDialog(row: Enrollment) {
  dialogAction.value = 'approve'
  currentRow.value = row
  dialogForm.comment = ''
  dialogVisible.value = true
}

function openRejectDialog(row: Enrollment) {
  dialogAction.value = 'reject'
  currentRow.value = row
  dialogForm.comment = ''
  dialogVisible.value = true
}

async function confirmDialog() {
  if (dialogAction.value === 'reject' && !dialogForm.comment) {
    ElMessage.warning('拒绝时请填写审批意见')
    return
  }

  dialogSubmitting.value = true
  try {
    const payload = {
      enrollmentId: currentRow.value?.id,
      comment: dialogForm.comment,
    }

    if (dialogAction.value === 'approve') {
      await approvalApi.approve(payload)
      ElMessage.success(`已通过 ${currentRow.value?.userName} 的报名申请`)
    } else {
      await approvalApi.reject(payload)
      ElMessage.success(`已拒绝 ${currentRow.value?.userName} 的报名申请`)
    }

    dialogVisible.value = false
    fetchList()
  } catch {
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    dialogSubmitting.value = false
  }
}

// ============= 批量操作 =============
async function handleBatchApprove() {
  const pendings = selectedRows.value.filter(
    (r) => r.currentStatus === 'APPROVING',
  )
  if (pendings.length === 0) {
    ElMessage.warning('所选记录中无待审批项')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认批量通过 ${pendings.length} 条报名申请吗？`,
      '批量通过确认',
      { type: 'warning' },
    )
  } catch {
    return
  }

  loading.value = true
  try {
    await approvalApi.batchApprove({
      enrollmentIds: pendings.map((r) => r.id),
      action: 'APPROVE',
    })
    ElMessage.success(`已批量通过 ${pendings.length} 条报名申请`)
    selectedRows.value = []
    fetchList()
  } catch {
    ElMessage.error('批量操作失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

async function handleBatchReject() {
  const pendings = selectedRows.value.filter(
    (r) => r.currentStatus === 'APPROVING',
  )
  if (pendings.length === 0) {
    ElMessage.warning('所选记录中无待审批项')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认批量拒绝 ${pendings.length} 条报名申请吗？`,
      '批量拒绝确认',
      { type: 'warning' },
    )
  } catch {
    return
  }

  loading.value = true
  try {
    await approvalApi.batchApprove({
      enrollmentIds: pendings.map((r) => r.id),
      action: 'REJECT',
    })
    ElMessage.success(`已批量拒绝 ${pendings.length} 条报名申请`)
    selectedRows.value = []
    fetchList()
  } catch {
    ElMessage.error('批量操作失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// ============= 生命周期 =============
onMounted(() => {
  loadActivities()
  fetchList()
})
</script>

<style scoped>
.school-approvals {
  padding: 4px 0;
}

h3 {
  margin: 0 0 16px;
  font-size: 18px;
  color: #111827;
}

.stats-row {
  margin-bottom: 16px;
}

.mini-stat {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-left: 3px solid #1a56db;
  border-radius: 6px;
  padding: 16px;
  text-align: center;
}

.mini-stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #111827;
}

.mini-stat-label {
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
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

.batch-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  margin-bottom: 16px;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 6px;
}

.batch-info {
  font-size: 13px;
  color: #1e40af;
  flex: 1;
}

.approval-table {
  width: 100%;
}

.no-action {
  color: #9ca3af;
  font-size: 13px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* AI 建议弹窗 */
.ai-dialog-content {
  padding: 4px 0;
}

.ai-student-info {
  font-size: 14px;
  color: #555;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.ai-suggestion {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 14px;
  padding: 12px;
  border-radius: 8px;
  background: #f5f7fa;
}

.ai-suggestion.ai-suggest-pass {
  background: #f0fdf4;
  border-left: 4px solid #22c55e;
}

.ai-suggestion.ai-suggest-pass .ai-value {
  color: #16a34a;
  font-weight: 700;
}

.ai-suggestion.ai-suggest-caution {
  background: #fffbeb;
  border-left: 4px solid #f59e0b;
}

.ai-suggestion.ai-suggest-caution .ai-value {
  color: #d97706;
  font-weight: 700;
}

.ai-suggestion.ai-suggest-reject {
  background: #fef2f2;
  border-left: 4px solid #ef4444;
}

.ai-suggestion.ai-suggest-reject .ai-value {
  color: #dc2626;
  font-weight: 700;
}

.ai-label {
  font-size: 13px;
  color: #6b7280;
  white-space: nowrap;
  min-width: 56px;
}

.ai-reason,
.ai-risk {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 10px;
  font-size: 14px;
  color: #374151;
  line-height: 1.6;
}

.ai-loading {
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
