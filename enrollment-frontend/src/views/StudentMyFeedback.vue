<template>
  <div class="my-feedback" v-loading="loading">
    <div class="page-header">
      <h3>我的反馈</h3>
    </div>

    <!-- Feedback List -->
    <div v-if="!loading && feedbacks.length > 0" class="feedback-list">
      <el-card
        v-for="fb in feedbacks"
        :key="fb.feedbackId"
        class="feedback-card"
        shadow="never"
      >
        <!-- Card Header -->
        <div class="fb-header">
          <div class="fb-header-left">
            <span class="fb-activity-title">{{ fb.activityTitle || '活动反馈' }}</span>
            <div class="fb-rating">
              <el-rate
                :model-value="fb.rating"
                disabled
                show-score
                size="small"
              />
            </div>
          </div>
          <el-tag
            :type="fb.status === 'REPLIED' ? 'success' : 'info'"
            size="default"
            effect="plain"
          >
            {{ feedbackStatusLabel(fb.status) }}
          </el-tag>
        </div>

        <!-- Feedback Content -->
        <div class="fb-body">
          <p class="fb-content">{{ fb.content }}</p>
          <div class="fb-meta">
            <span>提交时间：{{ fb.createTime }}</span>
            <span v-if="fb.attachmentCount">附件：{{ fb.attachmentCount }} 个</span>
          </div>
        </div>

        <!-- Reply Box -->
        <div v-if="fb.reply" class="fb-reply">
          <div class="reply-header">
            <el-icon><ChatDotRound /></el-icon>
            <strong>{{ fb.replyUserName || '管理员' }} 回复：</strong>
            <span class="reply-time">{{ fb.replyTime }}</span>
          </div>
          <p class="reply-content">{{ fb.reply }}</p>
        </div>
      </el-card>
    </div>

    <!-- Empty State -->
    <el-empty
      v-if="!loading && feedbacks.length === 0 && !errorMessage"
      description="暂无反馈记录"
      :image-size="160"
    >
      <template #default>
        <el-button type="primary" @click="$router.push('/student/enrollments')">
          查看我的报名
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
        <el-button type="primary" @click="fetchFeedbacks">重新加载</el-button>
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
import { ElMessage } from 'element-plus'
import { ChatDotRound } from '@element-plus/icons-vue'
import { feedbackApi } from '@/api'
import { parseListResponse } from '@/utils/api'
import type { Feedback } from '@/types'

const FEEDBACK_STATUS_MAP: Record<string, string> = {
  SUBMITTED: '已提交',
  REPLIED: '已回复',
  CLOSED: '已关闭',
}

const feedbacks = ref<Feedback[]>([])
const loading = ref(false)
const errorMessage = ref('')
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

function feedbackStatusLabel(status: string): string {
  return FEEDBACK_STATUS_MAP[status] || status
}

async function fetchFeedbacks() {
  loading.value = true
  errorMessage.value = ''
  try {
    const res: any = await feedbackApi.listMy({ page: page.value, size: pageSize.value })
    const r = parseListResponse(res); feedbacks.value = r.list; total.value = r.total
  } catch (err: any) {
    feedbacks.value = []
    total.value = 0
    errorMessage.value = err?.response?.data?.message || '加载反馈记录失败，请稍后重试'
    if (err?.response?.status !== 401) {
      ElMessage.error(errorMessage.value)
    }
  } finally {
    loading.value = false
  }
}

function handlePageChange(newPage: number) {
  page.value = newPage
  fetchFeedbacks()
}

onMounted(() => {
  fetchFeedbacks()
})
</script>

<style scoped>
.my-feedback {
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
}

/* Feedback List */
.feedback-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feedback-card {
  border-radius: 12px;
  border: 1px solid #eee;
  transition: box-shadow 0.2s;
}

.feedback-card:hover {
  box-shadow: 0 4px 16px rgba(26, 86, 219, 0.08);
}

/* Header */
.fb-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.fb-header-left {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.fb-activity-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
}

.fb-rating {
  display: flex;
  align-items: center;
}

/* Body */
.fb-body {
  margin-bottom: 0;
}

.fb-content {
  margin: 0 0 12px;
  font-size: 14px;
  line-height: 1.7;
  color: #444;
  white-space: pre-wrap;
}

.fb-meta {
  display: flex;
  gap: 20px;
  font-size: 12px;
  color: #aaa;
}

/* Reply */
.fb-reply {
  margin-top: 16px;
  padding: 16px 18px;
  background: #f0fdf4;
  border-left: 3px solid #22c55e;
  border-radius: 8px;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  font-size: 14px;
  color: #166534;
}

.reply-header strong {
  font-weight: 600;
}

.reply-time {
  margin-left: auto;
  font-size: 12px;
  color: #888;
  font-weight: 400;
}

.reply-content {
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
  color: #166534;
  white-space: pre-wrap;
}

/* Pagination */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding: 16px 0;
}
</style>
