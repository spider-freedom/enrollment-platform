<template>
  <div class="school-feedbacks" v-loading="loading">
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
      <h3>反馈管理（校级）</h3>

      <!-- 统计卡片 -->
      <el-row :gutter="16" class="stats-row">
        <el-col :span="6">
          <div class="mini-stat" style="border-left-color: #1a56db">
            <div class="mini-stat-value">{{ stats.total }}</div>
            <div class="mini-stat-label">反馈总数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="mini-stat" style="border-left-color: #f59e0b">
            <div class="mini-stat-value">{{ stats.pending }}</div>
            <div class="mini-stat-label">待回复</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="mini-stat" style="border-left-color: #16a34a">
            <div class="mini-stat-value">{{ stats.replied }}</div>
            <div class="mini-stat-label">已回复</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="mini-stat" style="border-left-color: #7c3aed">
            <div class="mini-stat-value">{{ stats.avgRating }}</div>
            <div class="mini-stat-label">平均评分</div>
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
              <el-option label="待回复" value="PENDING" />
              <el-option label="已回复" value="REPLIED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
            <el-button type="warning" :loading="aiAnalyzing" @click="runAiAnalysis">
              🤖 AI分析
            </el-button>
            <el-button type="success" @click="handleExport" icon="Download">导出Excel</el-button>
          </el-form-item>
        </el-form>

        <!-- AI分析结果 -->
        <el-alert
          v-if="aiResult"
          :title="'情感倾向: ' + aiResult.sentiment"
          type="info"
          :closable="false"
          show-icon
          style="margin-top: 12px"
        >
          <template #default>
            <div style="margin-top: 4px">
              <span style="color: #6b7280; font-size: 13px">关键词: </span>
              <el-tag
                v-for="(kw, idx) in aiResult.keywords"
                :key="idx"
                size="small"
                style="margin-right: 6px"
                effect="plain"
              >
                {{ kw }}
              </el-tag>
            </div>
          </template>
        </el-alert>
      </el-card>

      <!-- 空状态 -->
      <el-empty
        v-if="!loading && list.length === 0"
        description="暂无反馈记录"
      />

      <!-- 表格 -->
      <el-table
        v-else
        :data="list"
        border
        stripe
        class="feedback-table"
      >
        <el-table-column label="ID" width="70" align="center">
          <template #default="{ row }">
            {{ row.feedbackId || row.id }}
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="提交人" width="100" />
        <el-table-column label="角色" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.userRole === 'TEACHER' ? 'warning' : ''" effect="plain">
              {{ row.userRole === 'STUDENT' ? '学生' : row.userRole === 'TEACHER' ? '教师' : row.userRole || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="college" label="学院" width="120" show-overflow-tooltip />
        <el-table-column label="活动" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.activityTitle || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="评分" width="180" align="center">
          <template #default="{ row }">
            <el-rate :model-value="row.rating" disabled show-score text-color="#f59e0b" />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'REPLIED' ? 'success' : 'warning'" size="small">
              {{ row.status === 'SUBMITTED' ? '待回复' : row.status === 'REPLIED' ? '已回复' : row.status === 'CLOSED' ? '已关闭' : row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="回复人" width="100" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.replyUserName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="160" />
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="openViewDialog(row)">查看</el-button>
            <el-button
              v-if="row.status === 'SUBMITTED'"
              size="small"
              type="success"
              link
              @click="openReplyDialog(row)">回复</el-button>
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

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewVisible" title="反馈详情" width="580px" top="5vh">
      <template v-if="viewRow">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="提交人">{{ viewRow.userName }}</el-descriptions-item>
          <el-descriptions-item label="角色">
            {{ viewRow.userRole === 'STUDENT' ? '学生' : viewRow.userRole === 'TEACHER' ? '教师' : viewRow.userRole || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="学院">{{ viewRow.college }}</el-descriptions-item>
          <el-descriptions-item label="活动">{{ viewRow.activityTitle || '-' }}</el-descriptions-item>
          <el-descriptions-item label="评分">
            <el-rate :model-value="viewRow.rating" disabled show-score text-color="#f59e0b" />
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="viewRow.status === 'REPLIED' ? 'success' : 'warning'" size="small">
              {{ viewRow.status === 'SUBMITTED' ? '待回复' : viewRow.status === 'REPLIED' ? '已回复' : viewRow.status === 'CLOSED' ? '已关闭' : viewRow.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提交时间" :span="2">{{ viewRow.createTime }}</el-descriptions-item>
          <el-descriptions-item label="反馈内容" :span="2">
            <div class="feedback-content">{{ viewRow.content }}</div>
          </el-descriptions-item>
          <el-descriptions-item v-if="viewRow.attachmentCount > 0" label="附件" :span="2">
            {{ viewRow.attachmentCount }} 个附件
          </el-descriptions-item>
          <template v-if="viewRow.reply || viewRow.replyTime">
            <el-descriptions-item label="回复人" :span="2">{{ viewRow.replyUserName || '管理员' }}</el-descriptions-item>
            <el-descriptions-item label="回复时间" :span="2">{{ viewRow.replyTime }}</el-descriptions-item>
            <el-descriptions-item label="回复内容" :span="2">
              <div class="reply-content">{{ viewRow.reply }}</div>
            </el-descriptions-item>
          </template>
        </el-descriptions>
      </template>
    </el-dialog>

    <!-- 回复弹窗 -->
    <el-dialog
      v-model="replyVisible"
      title="回复反馈"
      width="560px"
      top="8vh"
      :close-on-click-modal="false"
    >
      <template v-if="replyRow">
        <div class="reply-feedback-preview">
          <div class="reply-feedback-header">
            <span class="fb-author">{{ replyRow.userName }}</span>
            <el-rate :model-value="replyRow.rating" disabled size="small" />
          </div>
          <div class="fb-content">{{ replyRow.content }}</div>
        </div>

        <el-form :model="replyForm" label-width="80px" class="reply-form">
          <el-form-item label="回复内容" required>
            <el-input
              v-model="replyForm.replyContent"
              type="textarea"
              :rows="5"
              placeholder="请输入回复内容，感谢您的认真反馈..."
              maxlength="2000"
              show-word-limit
            />
          </el-form-item>
          <el-form-item label="附件">
            <el-upload
              action="#"
              :auto-upload="false"
              :limit="3"
              :file-list="replyForm.attachments"
              list-type="text"
            >
              <el-button type="primary" size="small" plain>
                <el-icon style="margin-right: 4px"><Upload /></el-icon>上传附件
              </el-button>
              <template #tip>
                <div class="upload-tip">支持图片、PDF，单个文件不超过5MB</div>
              </template>
            </el-upload>
          </el-form-item>
        </el-form>
      </template>
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" :loading="replySubmitting" @click="submitReply">
          确认回复
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { feedbackApi, activityApi, aiApi } from '@/api'
import { downloadFile } from '@/api/request'
import type { Feedback } from '@/types'

// ============= 状态 =============
const loading = ref(true)
const error = ref(false)
const list = ref<Feedback[]>([])
const total = ref(0)

const stats = reactive({
  total: 0,
  pending: 0,
  replied: 0,
  avgRating: 0,
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

// 查看详情
const viewVisible = ref(false)
const viewRow = ref<Feedback | null>(null)

// 回复弹窗
const replyVisible = ref(false)
const replyRow = ref<Feedback | null>(null)
const replySubmitting = ref(false)
const replyForm = reactive({
  replyContent: '',
  attachments: [] as any[],
})

// AI analysis
const aiAnalyzing = ref(false)
const aiResult = ref<{ sentiment: string; keywords: string[] } | null>(null)

// ============= 辅助函数 =============
function statusLabel(status: string) {
  const map: Record<string, string> = {
    待回复: '待回复',
    已回复: '已回复',
    PENDING: '待回复',
    REPLIED: '已回复',
  }
  return map[status] || status || '-'
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

    const res = await feedbackApi.listSchool(params)
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
    (f) => f.status === 'SUBMITTED',
  ).length
  stats.replied = list.value.filter(
    (f) => f.status === 'REPLIED',
  ).length
  const ratings = list.value.map((f) => f.rating).filter((r) => r > 0)
  stats.avgRating = ratings.length > 0
    ? parseFloat((ratings.reduce((a, b) => a + b, 0) / ratings.length).toFixed(1))
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
  downloadFile('/feedback/export?status=' + (filters.status || ''), '反馈数据导出.xlsx')
}

// ============= 查看详情 =============
function openViewDialog(row: Feedback) {
  viewRow.value = row
  viewVisible.value = true
}

// ============= 回复 =============
function openReplyDialog(row: Feedback) {
  replyRow.value = row
  replyForm.replyContent = ''
  replyForm.attachments = []
  replyVisible.value = true
}

async function submitReply() {
  if (!replyForm.replyContent.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  replySubmitting.value = true
  try {
    const fbId = replyRow.value?.feedbackId || replyRow.value?.id
    await feedbackApi.reply(fbId as number, {
      replyContent: replyForm.replyContent,
    })
    ElMessage.success('回复成功！')
    replyVisible.value = false
    fetchList()
  } catch {
    ElMessage.error('回复失败，请稍后重试')
  } finally {
    replySubmitting.value = false
  }
}

// ============= AI分析 =============
async function runAiAnalysis() {
  const contents = list.value
    .filter((f) => f.content)
    .map((f) => f.content)
  if (contents.length === 0) {
    ElMessage.warning('没有可分析的反馈内容')
    return
  }

  aiAnalyzing.value = true
  aiResult.value = null
  try {
    const res: any = await aiApi.analyzeFeedback(contents[0])
    const data = res?.data || res
    aiResult.value = {
      sentiment: data?.sentiment || data?.label || '未知',
      keywords: data?.keywords || data?.keyWords || data?.tags || [],
    }
  } catch {
    ElMessage.error('AI分析失败，请稍后重试')
  } finally {
    aiAnalyzing.value = false
  }
}

// ============= 生命周期 =============
onMounted(() => {
  loadActivities()
  fetchList()
})
</script>

<style scoped>
.school-feedbacks {
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

.feedback-table {
  width: 100%;
}

.replied-text {
  color: #9ca3af;
  font-size: 13px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 查看详情 */
.feedback-content {
  color: #374151;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
}

.reply-content {
  color: #374151;
  line-height: 1.8;
  white-space: pre-wrap;
  background: #f0fdf4;
  padding: 10px 12px;
  border-radius: 6px;
}

/* 回复弹窗 */
.reply-feedback-preview {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
}

.reply-feedback-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.fb-author {
  font-weight: 600;
  color: #111827;
}

.fb-content {
  color: #4b5563;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 14px;
}

.reply-form {
  margin-top: 4px;
}

.upload-tip {
  font-size: 12px;
  color: #9ca3af;
}
</style>
