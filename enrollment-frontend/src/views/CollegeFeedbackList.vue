<template>
  <div class="college-feedbacks">
    <h2 class="page-title">反馈管理</h2>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card stat-total">
        <div class="stat-icon"><el-icon size="28"><ChatLineSquare /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">反馈总数</div>
        </div>
      </div>
      <div class="stat-card stat-pending">
        <div class="stat-icon"><el-icon size="28"><Warning /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.pendingReply }}</div>
          <div class="stat-label">待回复</div>
        </div>
      </div>
      <div class="stat-card stat-replied">
        <div class="stat-icon"><el-icon size="28"><CircleCheck /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.replied }}</div>
          <div class="stat-label">已回复</div>
        </div>
      </div>
      <div class="stat-card stat-rating">
        <div class="stat-icon"><el-icon size="28"><Star /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.avgRating }}</div>
          <div class="stat-label">平均评分</div>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-select
        v-model="filterActivityId"
        placeholder="选择活动"
        clearable
        style="width: 200px"
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
        placeholder="回复状态"
        clearable
        style="width: 140px; margin-left: 12px"
        @change="handleSearch"
      >
        <el-option label="待回复" value="PENDING" />
        <el-option label="已回复" value="REPLIED" />
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
      border
      stripe
      v-loading="loading"
      element-loading-text="加载中..."
    >
      <el-table-column prop="feedbackId" label="ID" width="70" />
      <el-table-column prop="userName" label="提交人" width="110" />
      <el-table-column label="角色" width="80">
        <template #default="{ row }">
          <el-tag :type="roleTagType(row.userRole)" size="small">
            {{ roleLabel(row.userRole) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="activityTitle" label="活动" min-width="150" show-overflow-tooltip />
      <el-table-column label="评分" width="180">
        <template #default="{ row }">
          <el-rate :model-value="row.rating" disabled show-score text-color="#ff9900" />
        </template>
      </el-table-column>
      <el-table-column prop="content" label="反馈内容" min-width="200" show-overflow-tooltip />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag
            :type="row.status === '已回复' || row.status === 'REPLIED' ? 'success' : 'warning'"
            size="small"
          >
            {{ feedbackStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="提交时间" width="170" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status !== '已回复' && row.status !== 'REPLIED'"
            size="small"
            type="primary"
            @click="openReply(row)"
          >
            回复
          </el-button>
          <el-tag v-else type="success" size="small">已回复</el-tag>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空状态 -->
    <el-empty v-if="!loading && list.length === 0" description="暂无反馈数据" />

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

    <!-- 回复弹窗 -->
    <el-dialog
      v-model="replyVisible"
      title="回复反馈"
      width="560px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <div class="reply-dialog-content">
        <!-- 原始反馈 -->
        <div class="feedback-original">
          <div class="feedback-header">
            <span class="feedback-user">{{ currentFeedback?.userName }}</span>
            <el-rate
              :model-value="currentFeedback?.rating || 0"
              disabled
              show-score
              size="small"
            />
            <span class="feedback-time">{{ currentFeedback?.createTime }}</span>
          </div>
          <p class="feedback-text">{{ currentFeedback?.content }}</p>
          <div
            v-if="currentFeedback?.attachmentCount && currentFeedback.attachmentCount > 0"
            class="feedback-attachments"
          >
            <el-icon><Paperclip /></el-icon>
            <span>包含 {{ currentFeedback.attachmentCount }} 个附件</span>
          </div>
        </div>

        <!-- 回复内容 -->
        <div class="reply-section">
          <label class="reply-label">回复内容</label>
          <el-input
            v-model="replyContent"
            type="textarea"
            :rows="5"
            placeholder="请输入回复内容"
            maxlength="1000"
            show-word-limit
          />
        </div>

        <!-- 附件上传 -->
        <div class="reply-section">
          <label class="reply-label">上传附件（选填）</label>
          <el-upload
            v-model:file-list="uploadFiles"
            :http-request="handleFileUpload"
            :limit="5"
            list-type="text"
            :before-upload="beforeUpload"
            :on-exceed="handleExceed"
          >
            <el-button type="primary" plain size="small">
              <el-icon style="margin-right: 4px"><Upload /></el-icon>选择文件
            </el-button>
            <template #tip>
              <div class="upload-tip">单个文件不超过 10MB，最多上传 5 个文件</div>
            </template>
          </el-upload>
        </div>
      </div>

      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitReply">
          确认回复
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ChatLineSquare,
  Warning,
  CircleCheck,
  Star,
  Paperclip,
  Upload,
} from '@element-plus/icons-vue'
import { feedbackApi } from '@/api'
import request from '@/api/request'
import type { Feedback } from '@/types'
import type { UploadFile, UploadRequestOptions } from 'element-plus'

// ---- 筛选 ----
const filterActivityId = ref('')
const filterStatus = ref('')

// ---- 分页 ----
const pagination = reactive({ page: 1, size: 20 })
const total = ref(0)

// ---- 列表 ----
const list = ref<Feedback[]>([])
const loading = ref(false)
const errorMsg = ref('')

// ---- 活动选项 ----
const activityOptions = ref<{ label: string; value: number }[]>([])

// ---- 回复弹窗 ----
const replyVisible = ref(false)
const submitting = ref(false)
const replyContent = ref('')
const uploadFiles = ref<UploadFile[]>([])
let currentFeedback: Feedback | null = null

// ---- 统计 ----
const stats = computed(() => {
  const totalCount = list.value.length
  const pendingReply = list.value.filter(
    (f) => f.status !== '已回复' && f.status !== 'REPLIED'
  ).length
  const replied = list.value.filter(
    (f) => f.status === '已回复' || f.status === 'REPLIED'
  ).length
  const ratings = list.value.map((f) => f.rating).filter((r) => r > 0)
  const avgRating = ratings.length > 0
    ? (ratings.reduce((a, b) => a + b, 0) / ratings.length).toFixed(1)
    : '0.0'
  return { total: totalCount, pendingReply, replied, avgRating }
})

// ---- 标签辅助 ----
const roleLabelMap: Record<string, string> = {
  STUDENT: '学生',
  TEACHER: '教师',
  student: '学生',
  teacher: '教师',
}

const roleTagTypeMap: Record<string, string> = {
  STUDENT: 'info',
  TEACHER: 'success',
  student: 'info',
  teacher: 'success',
}

function roleLabel(r: string): string {
  return roleLabelMap[r] || r
}

function roleTagType(r: string): string {
  return roleTagTypeMap[r] || 'info'
}

function feedbackStatusLabel(status: string): string {
  if (status === '已回复' || status === 'REPLIED') return '已回复'
  if (status === '待回复' || status === 'PENDING') return '待回复'
  return status
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
    if (filterStatus.value) params.status = filterStatus.value
    if (filterActivityId.value) params.activityId = filterActivityId.value

    const res: any = await feedbackApi.listCollege(params)
    if (res?.data?.records) {
      list.value = res.data.records
      total.value = res.data.total || 0
    } else if (res?.records) {
      list.value = res.records
      total.value = res.total || 0
    } else {
      list.value = []
      total.value = 0
    }

    // 提取活动选项
    const activitySet = new Map<string, number>()
    list.value.forEach((f: any) => {
      const key: string = f.activityTitle || ''
      const val: number = f.activityId || 0
      if (key && !activitySet.has(key)) {
        activitySet.set(key, val)
      }
    })
    activityOptions.value = Array.from(activitySet.entries()).map(([label, value]) => ({
      label,
      value,
    }))
  } catch (err: any) {
    errorMsg.value = err?.response?.data?.message || err?.message || '加载反馈数据失败，请稍后重试'
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
  filterActivityId.value = ''
  filterStatus.value = ''
  pagination.page = 1
  fetchData()
}

// ---- 回复 ----
function openReply(row: Feedback) {
  currentFeedback = row
  replyContent.value = ''
  uploadFiles.value = []
  replyVisible.value = true
}

async function handleFileUpload(options: UploadRequestOptions) {
  // 上传到服务器，返回文件 ID
  const formData = new FormData()
  formData.append('file', options.file)
  try {
    const res: any = await request.post('/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    // 将返回的附件信息挂到 UploadFile 上
    ;(options.file as any).uploadedId = res?.data?.id || res?.id
    options.onSuccess!(res)
  } catch (err) {
    options.onError!(err as any)
  }
}

function beforeUpload(file: File) {
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB')
  }
  return isLt10M
}

function handleExceed() {
  ElMessage.warning('最多上传 5 个文件')
}

async function submitReply() {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  if (!currentFeedback) return

  submitting.value = true
  try {
    // 收集已上传文件的 ID
    const attachmentIds: number[] = uploadFiles.value
      .map((f) => (f as any).uploadedId)
      .filter((id: any) => id != null)

    await feedbackApi.reply(currentFeedback.feedbackId, {
      replyContent: replyContent.value,
      attachmentIds,
    })
    ElMessage.success('回复成功')
    replyVisible.value = false
    fetchData()
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || err?.message || '回复失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.college-feedbacks {
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
.stat-pending { border-left-color: #e6a23c; }
.stat-replied { border-left-color: #67c23a; }
.stat-rating { border-left-color: #ff9900; }

.stat-total .stat-icon { color: #1a56db; }
.stat-pending .stat-icon { color: #e6a23c; }
.stat-replied .stat-icon { color: #67c23a; }
.stat-rating .stat-icon { color: #ff9900; }

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

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

/* 回复弹窗 */
.reply-dialog-content {
  padding: 4px 0;
}

.feedback-original {
  background: #f7f8fa;
  border-radius: 8px;
  padding: 14px 16px;
  margin-bottom: 20px;
}

.feedback-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.feedback-user {
  font-weight: 600;
  color: #1a3a5c;
}

.feedback-time {
  font-size: 12px;
  color: #999;
  margin-left: auto;
}

.feedback-text {
  font-size: 14px;
  color: #333;
  line-height: 1.7;
  white-space: pre-wrap;
  margin: 0;
}

.feedback-attachments {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 10px;
  font-size: 13px;
  color: #888;
}

.reply-section {
  margin-bottom: 16px;
}

.reply-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>
