<template>
  <div class="teacher-my-feedback">
    <div class="page-header">
      <h2>我的反馈记录</h2>
      <p class="subtitle">查看您提交的活动反馈及管理员回复</p>
    </div>

    <div v-loading="loading" class="content-area">
      <el-empty
        v-if="!loading && feedbacks.length === 0"
        description="暂无反馈记录，参与活动后可以提交反馈"
        style="padding: 60px 0"
      />

      <el-card
        v-for="fb in feedbacks"
        v-else
        :key="fb.feedbackId"
        class="feedback-card"
      >
        <div class="fb-header">
          <div class="fb-header-left">
            <span class="fb-college">{{ fb.college || '未知院系' }}</span>
            <span class="fb-rating">
              <el-rate
                :model-value="fb.rating"
                disabled
                show-score
                size="small"
              />
            </span>
          </div>
          <el-tag
            :type="fb.status === 'REPLIED' ? 'success' : 'info'"
            size="small"
          >
            {{ fb.status === 'SUBMITTED' ? '待回复' : fb.status === 'REPLIED' ? '已回复' : fb.status || '待回复' }}
          </el-tag>
        </div>

        <p class="fb-content">{{ fb.content }}</p>

        <div v-if="fb.reply" class="fb-reply">
          <div class="fb-reply-header">
            <strong>{{ fb.replyUserName || '管理员' }} 回复：</strong>
            <span class="fb-reply-time">{{ fb.replyTime }}</span>
          </div>
          <p>{{ fb.reply }}</p>
        </div>

        <div class="fb-footer">
          <span class="fb-time">{{ fb.createTime }}</span>
          <span v-if="fb.attachmentCount" class="fb-attachments">
            附件：{{ fb.attachmentCount }} 个
          </span>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { feedbackApi } from '@/api'
import type { Feedback } from '@/types'

const feedbacks = ref<Feedback[]>([])
const loading = ref(false)

async function fetchFeedbacks() {
  loading.value = true
  try {
    const res: any = await feedbackApi.listMy()
    if (res?.data?.list) {
      feedbacks.value = res.data.list
    } else if (res?.data?.records) {
      feedbacks.value = res.data.records
    } else if (res?.records) {
      feedbacks.value = res.records
    } else if (Array.isArray(res)) {
      feedbacks.value = res
    } else {
      feedbacks.value = []
    }
  } catch {
    feedbacks.value = []
    ElMessage.error('加载反馈记录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchFeedbacks()
})
</script>

<style scoped>
.teacher-my-feedback {
  max-width: 900px;
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

.content-area {
  min-height: 300px;
}

.feedback-card {
  margin-bottom: 16px;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
}

.feedback-card :deep(.el-card__body) {
  padding: 20px 24px;
}

.fb-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.fb-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.fb-college {
  font-size: 14px;
  font-weight: 600;
  color: #1a56db;
  background: #eff6ff;
  padding: 3px 10px;
  border-radius: 6px;
}

.fb-rating :deep(.el-rate) {
  height: auto;
}

.fb-content {
  color: #374151;
  line-height: 1.7;
  font-size: 14px;
  margin: 0 0 14px 0;
  white-space: pre-wrap;
}

.fb-reply {
  background: #f0fdf4;
  border-left: 3px solid #22c55e;
  padding: 14px 16px;
  border-radius: 0 8px 8px 0;
  margin-bottom: 12px;
}

.fb-reply-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  font-size: 13px;
}

.fb-reply-header strong {
  color: #166534;
}

.fb-reply-time {
  font-size: 12px;
  color: #9ca3af;
}

.fb-reply p {
  margin: 0;
  color: #15803d;
  font-size: 14px;
  line-height: 1.6;
}

.fb-footer {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #9ca3af;
  padding-top: 10px;
  border-top: 1px solid #f3f4f6;
}

.fb-attachments {
  color: #1a56db;
}
</style>
