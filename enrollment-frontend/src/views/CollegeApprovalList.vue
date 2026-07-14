<template>
  <div class="college-approvals">
    <h2 class="page-title">报名审批管理</h2>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card stat-pending">
        <div class="stat-icon"><el-icon size="28"><Clock /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.pending }}</div>
          <div class="stat-label">待审核</div>
        </div>
      </div>
      <div class="stat-card stat-approved">
        <div class="stat-icon"><el-icon size="28"><CircleCheck /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.approved }}</div>
          <div class="stat-label">已通过</div>
        </div>
      </div>
      <div class="stat-card stat-rejected">
        <div class="stat-icon"><el-icon size="28"><CircleClose /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.rejected }}</div>
          <div class="stat-label">已拒绝</div>
        </div>
      </div>
      <div class="stat-card stat-rate">
        <div class="stat-icon"><el-icon size="28"><TrendCharts /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.approvalRate }}%</div>
          <div class="stat-label">通过率</div>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-input
        v-model="filterKeyword"
        placeholder="搜索申请人或招生学校"
        clearable
        style="width: 240px"
        @clear="handleSearch"
        @keyup.enter="handleSearch"
      />
      <el-select
        v-model="filterActivityId"
        placeholder="选择活动"
        clearable
        style="width: 200px; margin-left: 12px"
        @change="handleSearch"
      >
        <el-option
          v-for="act in activityOptions"
          :key="act.value"
          :label="act.label"
          :value="act.value"
        />
      </el-select>
      <el-select
        v-model="filterStatus"
        placeholder="审核状态"
        clearable
        style="width: 140px; margin-left: 12px"
        @change="handleSearch"
      >
        <el-option
          v-for="s in statusOptions"
          :key="s.value"
          :label="s.label"
          :value="s.value"
        />
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

    <!-- 批量操作 -->
    <div class="batch-bar" v-if="selectedRows.length > 0">
      <span class="batch-tip">已选择 {{ selectedRows.length }} 项</span>
      <el-button type="success" size="small" style="margin-left: 12px" @click="handleBatchAction('APPROVE')">
        批量通过
      </el-button>
      <el-button type="danger" size="small" style="margin-left: 8px" @click="handleBatchAction('REJECT')">
        批量拒绝
      </el-button>
    </div>

    <!-- 表格 -->
    <el-table
      :data="list"
      border
      stripe
      v-loading="loading"
      element-loading-text="加载中..."
      @selection-change="handleSelection"
    >
      <el-table-column type="selection" width="50" />
      <el-table-column prop="applicantName" label="申请人" width="120" />
      <el-table-column label="角色" width="90">
        <template #default="{ row }">
          <el-tag :type="roleTagType(row.applicantRole)" size="small">
            {{ roleLabel(row.applicantRole) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="activityTitle" label="活动" min-width="180" show-overflow-tooltip />
      <el-table-column prop="gpa" label="GPA" width="80">
        <template #default="{ row }">
          {{ row.gpa != null ? row.gpa : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="提交时间" width="160" />
      <el-table-column label="审核状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.currentStatus)" size="small">
            {{ statusLabel(row.currentStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button
            v-if="row.currentStatus === 'SUBMITTED'"
            size="small"
            type="success"
            @click="handleApprove(row)"
          >
            通过
          </el-button>
          <el-button
            v-if="row.currentStatus === 'SUBMITTED'"
            size="small"
            type="danger"
            @click="handleReject(row)"
          >
            拒绝
          </el-button>
          <el-button
            v-if="row.status === 'SUBMITTED' || row.status === 'APPROVING'"
            size="small"
            type="warning"
            plain
            @click="handleAiSuggest(row)"
          >
            🤖 AI
          </el-button>
          <el-tag v-else-if="row.status === 'APPROVED'" type="success" size="small">已通过</el-tag>
          <el-tag v-else-if="row.status === 'REJECTED'" type="danger" size="small">已拒绝</el-tag>
          <el-tag v-else type="info" size="small">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空状态 -->
    <el-empty v-if="!loading && list.length === 0" description="暂无审批数据" />

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

    <!-- 拒绝原因弹窗 -->
    <el-dialog v-model="rejectVisible" title="拒绝报名" width="480px" destroy-on-close>
      <div class="reject-dialog-content">
        <p class="reject-info">
          申请人：<strong>{{ currentReject?.userName }}</strong>
          &nbsp;|&nbsp; 活动：{{ currentReject?.activityTitle }}
        </p>
        <el-input
          v-model="rejectReason"
          type="textarea"
          :rows="4"
          placeholder="请输入拒绝原因（选填）"
          maxlength="500"
          show-word-limit
        />
      </div>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" :loading="rejecting" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>

    <!-- 批量拒绝原因弹窗 -->
    <el-dialog v-model="batchRejectVisible" title="批量拒绝" width="480px" destroy-on-close>
      <div class="reject-dialog-content">
        <p class="reject-info">已选择 <strong>{{ selectedRows.length }}</strong> 条报名记录</p>
        <el-input
          v-model="batchRejectReason"
          type="textarea"
          :rows="4"
          placeholder="请输入拒绝原因（选填）"
          maxlength="500"
          show-word-limit
        />
      </div>
      <template #footer>
        <el-button @click="batchRejectVisible = false">取消</el-button>
        <el-button type="danger" :loading="rejecting" @click="confirmBatchReject">确认拒绝</el-button>
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
import { Clock, CircleCheck, CircleClose, TrendCharts } from '@element-plus/icons-vue'
import { aiApi, approvalApi } from '@/api'
import type { Enrollment } from '@/types'

// ---- 筛选状态 ----
const filterStatus = ref('')
const filterKeyword = ref('')
const filterActivityId = ref('')

// ---- 分页 ----
const pagination = reactive({ page: 1, size: 20 })
const total = ref(0)

// ---- 列表 ----
const list = ref<Enrollment[]>([])
const loading = ref(false)
const errorMsg = ref('')

// ---- 选中 ----
const selectedRows = ref<Enrollment[]>([])

// ---- 拒绝弹窗 ----
const rejectVisible = ref(false)
const rejecting = ref(false)
const rejectReason = ref('')
let currentReject: Enrollment | null = null

// ---- 批量拒绝弹窗 ----
const batchRejectVisible = ref(false)
const batchRejectReason = ref('')

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

async function handleAiSuggest(row: Enrollment) {
  aiCurrentRow.value = row
  aiVisible.value = true
  aiLoading.value = true
  aiResult.value = null
  try {
    const res: any = await aiApi.approvalSuggest({
      studentName: row.userName,
      studentRole: roleLabel(row.userRole),
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

// ---- 状态选项 ----
const statusOptions = [
  { label: '待审核', value: 'SUBMITTED' },
  { label: '审批中', value: 'APPROVING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已拒绝', value: 'REJECTED' },
]

// ---- 活动选项（从数据中提取） ----
const activityOptions = ref<{ label: string; value: number }[]>([])

// ---- 状态映射 ----
const statusLabelMap: Record<string, string> = {
  SUBMITTED: '待审核',
  APPROVING: '审批中',
  APPROVED: '已通过',
  REJECTED: '已拒绝',
  WITHDRAWN: '已撤回',
}

const statusTagTypeMap: Record<string, string> = {
  SUBMITTED: 'warning',
  APPROVING: '',
  APPROVED: 'success',
  REJECTED: 'danger',
  WITHDRAWN: 'info',
}

const roleLabelMap: Record<string, string> = {
  STUDENT: '学生',
  TEACHER: '教师',
  COLLEGE_ADMIN: '管理员',
}

const roleTagTypeMap: Record<string, string> = {
  STUDENT: 'info',
  TEACHER: 'success',
  COLLEGE_ADMIN: 'warning',
}

function statusLabel(s: string): string {
  return statusLabelMap[s] || s
}

function statusTagType(s: string): string {
  return statusTagTypeMap[s] || 'info'
}

function roleLabel(r: string): string {
  return roleLabelMap[r] || r
}

function roleTagType(r: string): string {
  return roleTagTypeMap[r] || 'info'
}

// ---- 统计（基于全部数据，忽略分页） ----
const stats = computed(() => {
  const pending = list.value.filter((e) => e.status === 'SUBMITTED' || e.status === 'APPROVING').length
  const approved = list.value.filter((e) => e.status === 'APPROVED').length
  const rejected = list.value.filter((e) => e.status === 'REJECTED').length
  const totalCount = list.value.length || 1
  const approvalRate = Math.round((approved / totalCount) * 100)
  return { pending, approved, rejected, approvalRate }
})

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
    if (filterStatus.value) params.status = filterStatus.value
    if (filterActivityId.value) params.activityId = filterActivityId.value

    const res: any = await approvalApi.listCollege(params)
    if (res?.data?.list) {
      list.value = res.data.list
      total.value = res.data.total || 0
    } else if (res?.data?.records) {
      list.value = res.data.records
      total.value = res.data.total || 0
      // 提取活动选项
      const activitySet = new Map<string, number>()
      list.value.forEach((e) => {
        if (!activitySet.has(e.activityTitle)) {
          activitySet.set(e.activityTitle, e.activityId)
        }
      })
      activityOptions.value = Array.from(activitySet.entries()).map(([label, value]) => ({
        label,
        value,
      }))
    } else if (res?.records) {
      list.value = res.records
      total.value = res.total || 0
    } else {
      list.value = []
      total.value = 0
    }
  } catch (err: any) {
    errorMsg.value = err?.response?.data?.message || err?.message || '加载审批数据失败，请稍后重试'
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
  filterStatus.value = ''
  filterKeyword.value = ''
  filterActivityId.value = ''
  pagination.page = 1
  fetchData()
}

// ---- 单选操作 ----
async function handleApprove(row: any) {
  try {
    await ElMessageBox.confirm(
      `确定要通过「${row.applicantName}」的报名申请吗？`,
      '确认通过',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'success' }
    )
  } catch {
    return
  }
  try {
    await approvalApi.approve({ enrollmentId: row.enrollmentId, action: 'APPROVE' })
    ElMessage.success(`已通过 ${row.applicantName} 的报名`)
    fetchData()
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || err?.message || '操作失败')
  }
}

function handleReject(row: Enrollment) {
  currentReject = row
  rejectReason.value = ''
  rejectVisible.value = true
}

async function confirmReject() {
  if (!currentReject) return
  rejecting.value = true
  try {
    await approvalApi.reject({
      enrollmentId: currentReject.enrollmentId,
      action: 'REJECT',
      rejectReason: rejectReason.value,
    })
    ElMessage.success(`已拒绝 ${currentReject.applicantName} 的报名`)
    rejectVisible.value = false
    fetchData()
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || err?.message || '操作失败')
  } finally {
    rejecting.value = false
  }
}

// ---- 批量操作 ----
function handleSelection(rows: Enrollment[]) {
  selectedRows.value = rows
}

function handleBatchAction(action: 'APPROVE' | 'REJECT') {
  if (action === 'REJECT') {
    batchRejectReason.value = ''
    batchRejectVisible.value = true
    return
  }
  ElMessageBox.confirm(
    `确定要批量通过选中的 ${selectedRows.value.length} 条报名吗？`,
    '批量通过',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'success' }
  )
    .then(async () => {
      try {
        const ids = selectedRows.value.map((r: any) => r.enrollmentId)
        await approvalApi.batchApprove({ enrollmentIds: ids, action: 'APPROVE' })
        ElMessage.success('批量通过成功')
        fetchData()
      } catch (err: any) {
        ElMessage.error(err?.response?.data?.message || err?.message || '操作失败')
      }
    })
    .catch(() => {})
}

async function confirmBatchReject() {
  if (selectedRows.value.length === 0) return
  rejecting.value = true
  try {
    const ids = selectedRows.value.map((r: any) => r.enrollmentId)
    await approvalApi.batchApprove({
      enrollmentIds: ids,
      action: 'REJECT',
      rejectReason: batchRejectReason.value,
    })
    ElMessage.success('批量拒绝成功')
    batchRejectVisible.value = false
    fetchData()
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || err?.message || '操作失败')
  } finally {
    rejecting.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.college-approvals {
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

.stat-pending { border-left-color: #e6a23c; }
.stat-approved { border-left-color: #67c23a; }
.stat-rejected { border-left-color: #f56c6c; }
.stat-rate { border-left-color: #1a56db; }

.stat-pending .stat-icon { color: #e6a23c; }
.stat-approved .stat-icon { color: #67c23a; }
.stat-rejected .stat-icon { color: #f56c6c; }
.stat-rate .stat-icon { color: #1a56db; }

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

/* 批量操作栏 */
.batch-bar {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  margin-bottom: 12px;
  background: #ebf5ff;
  border-radius: 8px;
  border: 1px solid #bdd7f5;
}

.batch-tip {
  font-size: 14px;
  color: #1a56db;
  font-weight: 500;
}

/* 表格 */
:deep(.el-table) {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.el-table th) {
  background-color: #f7f8fa;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

/* 拒绝弹窗 */
.reject-dialog-content {
  padding: 4px 0;
}

.reject-info {
  font-size: 14px;
  color: #555;
  margin-bottom: 12px;
  line-height: 1.6;
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
