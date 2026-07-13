<template>
  <div class="activity-detail" v-loading="loading">
    <div v-if="error" class="error-state">
      <el-result icon="error" title="加载失败" :sub-title="error">
        <template #extra>
          <el-button type="primary" @click="fetchActivity">重新加载</el-button>
        </template>
      </el-result>
    </div>

    <div v-else-if="!activity && !loading" class="empty-state">
      <el-result icon="info" title="活动未找到" sub-title="该活动不存在或已被删除" />
    </div>

    <template v-else-if="activity">
      <div class="hero" :style="{ background: 'linear-gradient(135deg, #1a3a5c, #2d6a9f)' }">
        <h1>{{ activity.title }}</h1>
        <el-tag :type="activityTypeTagType(activity.type)">{{ ACTIVITY_TYPE_MAP[activity.type] || activity.type }}</el-tag>
        <el-tag :type="activityStatusTagType(activity.status)" style="margin-left:8px">{{ ACTIVITY_STATUS_MAP[activity.status] || activity.status }}</el-tag>
      </div>
      <el-card class="detail-card">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="活动时间">{{ activity.startTime }} ~ {{ activity.endTime }}</el-descriptions-item>
          <el-descriptions-item label="报名时间">{{ activity.enrollStart }} ~ {{ activity.enrollEnd }}</el-descriptions-item>
          <el-descriptions-item label="活动地点">{{ activity.location }}</el-descriptions-item>
          <el-descriptions-item label="招生对象">{{ TARGET_AUDIENCE_MAP[activity.targetAudience] || activity.targetAudience }}</el-descriptions-item>
          <el-descriptions-item label="学生名额">{{ activity.currentStudents || 0 }}/{{ activity.maxStudents || 0 }}</el-descriptions-item>
          <el-descriptions-item label="教师名额">{{ activity.currentTeachers || 0 }}/{{ activity.maxTeachers || 0 }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
      <el-card class="detail-card">
        <h3>活动详情</h3>
        <p>{{ activity.description }}</p>
      </el-card>
      <div style="text-align:center;margin-top:24px">
        <el-button type="primary" size="large" @click="handleEnroll" :disabled="!canEnroll(activity.status)" :loading="enrolling">
          {{ canEnroll(activity.status) ? '立即报名' : (activity.status === 'ENDED' ? '已结束' : '未开放报名') }}
        </el-button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { activityApi, enrollmentApi } from '@/api'
import type { Activity } from '@/types'
import {
  ACTIVITY_TYPE_MAP, ACTIVITY_STATUS_MAP, TARGET_AUDIENCE_MAP,
  activityTypeTagType, activityStatusTagType, canEnroll,
} from '@/utils/constants'

const route = useRoute()
const activity = ref<Activity | null>(null)
const loading = ref(false)
const error = ref('')
const enrolling = ref(false)

async function fetchActivity() {
  loading.value = true
  error.value = ''
  try {
    const res = await activityApi.getById(Number(route.params.id))
    activity.value = res.data
  } catch (e: any) {
    error.value = e?.response?.data?.message || e?.message || '加载活动详情失败'
  } finally {
    loading.value = false
  }
}

async function handleEnroll() {
  if (!activity.value) return
  enrolling.value = true
  try {
    await enrollmentApi.submit({ activityId: activity.value.id })
    ElMessage.success('报名成功！')
    await fetchActivity()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || e?.message || '报名失败')
  } finally {
    enrolling.value = false
  }
}

onMounted(() => {
  fetchActivity()
})
</script>

<style scoped>
.hero { padding: 48px; color: #fff; border-radius: 8px; margin-bottom: 20px; }
.hero h1 { margin: 0 0 12px; font-size: 24px; }
.hero .el-tag { margin-right: 8px; }
.detail-card { margin-bottom: 20px; }
.error-state,
.empty-state { padding: 60px 0; }
</style>
