<template>
  <div class="activity-detail" v-loading="loading">
    <!-- Not Found State -->
    <el-result
      v-if="!loading && notFound"
      icon="error"
      title="活动不存在"
      sub-title="该活动可能已被删除或链接无效"
    >
      <template #extra>
        <el-button type="primary" @click="$router.push('/student/activities')">
          返回活动列表
        </el-button>
      </template>
    </el-result>

    <!-- Error State -->
    <el-result
      v-else-if="!loading && errorMessage && !activity"
      icon="error"
      title="加载失败"
      :sub-title="errorMessage"
    >
      <template #extra>
        <el-button type="primary" @click="fetchActivity">重新加载</el-button>
        <el-button @click="$router.push('/student/activities')">返回活动列表</el-button>
      </template>
    </el-result>

    <!-- Activity Content -->
    <template v-else-if="activity">
      <!-- Hero Banner -->
      <div class="hero" :style="{ background: heroGradient }">
        <div class="hero-content">
          <div class="hero-tags">
            <el-tag
              :type="activityTypeTagType(activity.type)"
              size="default"
              effect="dark"
            >
              {{ ACTIVITY_TYPE_MAP[activity.type] || activity.type }}
            </el-tag>
            <el-tag
              :type="getDisplayStatusTagType(getDisplayStatus(activity))"
              size="default"
              effect="dark"
            >
              {{ getDisplayStatus(activity) }}
            </el-tag>
          </div>
          <h1>{{ activity.title }}</h1>
          <div class="hero-meta">
            <span>
              <el-icon><Calendar /></el-icon>
              {{ activity.startTime?.substring(0, 10) }} ~ {{ activity.endTime?.substring(0, 10) }}
            </span>
            <span>
              <el-icon><Location /></el-icon>
              {{ activity.location }}
            </span>
            <span>
              <el-icon><User /></el-icon>
              {{ activity.currentStudents || 0 }}/{{ activity.maxStudents }} 人已报名
            </span>
          </div>
        </div>
      </div>

      <!-- Two-Column Layout -->
      <div class="detail-layout">
        <!-- Left Column: Description + Requirements -->
        <div class="detail-left">
          <el-card class="detail-card" shadow="never">
            <template #header>
              <h3 class="card-section-title">活动详情</h3>
            </template>
            <div class="description-content">
              {{ activity.description || '暂无详细描述' }}
            </div>
          </el-card>

          <el-card class="detail-card" shadow="never">
            <template #header>
              <h3 class="card-section-title">基本要求</h3>
            </template>
            <el-descriptions :column="1" border size="default">
              <el-descriptions-item label="招生对象">
                {{ targetAudienceLabel }}
              </el-descriptions-item>
              <el-descriptions-item label="报名时间">
                {{ activity.enrollStart }} ~ {{ activity.enrollEnd }}
              </el-descriptions-item>
              <el-descriptions-item label="活动时间">
                {{ activity.startTime }} ~ {{ activity.endTime }}
              </el-descriptions-item>
              <el-descriptions-item label="活动地点">
                {{ activity.location }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </div>

        <!-- Right Column: Info + Enroll -->
        <div class="detail-right">
          <!-- Quota Card -->
          <el-card class="info-card" shadow="never">
            <template #header>
              <h3 class="card-section-title">名额信息</h3>
            </template>
            <div class="quota-list">
              <div class="quota-item">
                <span class="quota-label">学生名额</span>
                <div class="quota-bar-wrapper">
                  <el-progress
                    :percentage="studentPercent"
                    :stroke-width="14"
                    :color="studentPercent >= 90 ? '#f56c6c' : '#1a56db'"
                  >
                    <span class="quota-text">{{ activity.currentStudents || 0 }} / {{ activity.maxStudents }}</span>
                  </el-progress>
                </div>
              </div>
              <div class="quota-item">
                <span class="quota-label">教师名额</span>
                <div class="quota-bar-wrapper">
                  <el-progress
                    :percentage="teacherPercent"
                    :stroke-width="14"
                    :color="teacherPercent >= 90 ? '#f56c6c' : '#1a56db'"
                  >
                    <span class="quota-text">{{ activity.currentTeachers || 0 }} / {{ activity.maxTeachers }}</span>
                  </el-progress>
                </div>
              </div>
              <div class="quota-item">
                <span class="quota-label">每所学校最多报名</span>
                <span class="quota-value">{{ activity.maxPerSchool ? activity.maxPerSchool + ' 人' : '不限人数' }}</span>
              </div>
            </div>
          </el-card>

          <!-- Enroll Button -->
          <div class="enroll-section">
            <el-button
              v-if="alreadyEnrolled"
              type="info"
              size="large"
              disabled
              style="width: 100%"
            >
              已报名
            </el-button>
            <el-button
              v-else-if="canEnroll(activity)"
              type="primary"
              size="large"
              style="width: 100%"
              :loading="enrolling"
              @click="handleEnroll"
            >
              立即报名
            </el-button>
            <el-button
              v-else
              type="info"
              size="large"
              disabled
              style="width: 100%"
            >
              {{ getDisplayStatus(activity) === '报名已截止' ? '报名已截止' : getDisplayStatus(activity) === '已结束' ? '已结束' : '暂不可报名' }}
            </el-button>
          </div>

          <!-- Enrollment Dialog -->
          <el-dialog
            v-model="enrollDialogVisible"
            title="完善报名信息"
            width="500px"
            top="8vh"
            :close-on-click-modal="false"
          >
            <el-form label-width="80px">
              <el-form-item label="我的学院" required>
                <el-input :model-value="myCollege" disabled />
              </el-form-item>
              <el-form-item label="个人简介">
                <el-input
                  v-model="briefIntro"
                  type="textarea"
                  :rows="4"
                  placeholder="简单介绍一下自己，让招生老师更快了解你..."
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>
            </el-form>
            <template #footer>
              <el-button @click="enrollDialogVisible = false">取消</el-button>
              <el-button type="primary" :loading="enrolling" @click="submitEnroll">
                确认报名
              </el-button>
            </template>
          </el-dialog>

          <!-- Attachments Placeholder -->
          <el-card v-if="activity.attachments && activity.attachments.length > 0" class="info-card" shadow="never">
            <template #header>
              <h3 class="card-section-title">相关附件</h3>
            </template>
            <div
              v-for="(file, idx) in activity.attachments"
              :key="idx"
              class="attachment-item"
            >
              <el-icon><Document /></el-icon>
              <span>{{ file.name || file }}</span>
              <el-button size="small" type="primary" link>下载</el-button>
            </div>
          </el-card>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Calendar, Location, User, Document } from '@element-plus/icons-vue'
import { activityApi, enrollmentApi, aiApi } from '@/api'
import { useUserStore } from '@/stores/user'
import type { Activity } from '@/types'
import {
  ACTIVITY_TYPE_MAP, TARGET_AUDIENCE_MAP,
  activityTypeTagType, getDisplayStatus, getDisplayStatusTagType, canEnroll,
} from '@/utils/constants'

const route = useRoute()
const router = useRouter()
const store = useUserStore()

const activity = ref<Activity | null>(null)
const loading = ref(false)
const enrolling = ref(false)
const myCollege = computed(() => store.userInfo?.collegeName || '未知学院')
const errorMessage = ref('')
const notFound = ref(false)
const alreadyEnrolled = ref(false)

// AI school suggest dialog
const enrollDialogVisible = ref(false)
const targetSchool = ref('')
const schoolSuggestions = ref<string[]>([])
const briefIntro = ref('')
const suggestLoading = ref(false)

const heroGradient = 'linear-gradient(135deg, #1a3a5c 0%, #1a56db 40%, #4f46e5 100%)'

const targetAudienceLabel = computed(() => {
  const v = activity.value?.targetAudience
  if (v !== undefined && v !== null) {
    return TARGET_AUDIENCE_MAP[v] || String(v)
  }
  return '-'
})

const studentPercent = computed(() => {
  if (!activity.value || !activity.value.maxStudents) return 0
  return Math.round((activity.value.currentStudents || 0) / activity.value.maxStudents * 100)
})

const teacherPercent = computed(() => {
  if (!activity.value || !activity.value.maxTeachers) return 0
  return Math.round((activity.value.currentTeachers || 0) / activity.value.maxTeachers * 100)
})

function statusTagType(status: string): string {
  const map: Record<string, string> = {
    '报名中': 'success',
    '进行中': 'warning',
    '即将开始': '',
    '已结束': 'info',
    '已截止': 'danger',
  }
  return map[status] || 'info'
}

async function suggestSchool() {
  const keyword = targetSchool.value.trim()
  if (!keyword || keyword.length < 1) {
    schoolSuggestions.value = []
    return
  }
  suggestLoading.value = true
  try {
    const res: any = await aiApi.suggestSchool(keyword)
    const data = res?.data || res
    if (Array.isArray(data)) {
      schoolSuggestions.value = data.map((s: any) => typeof s === 'string' ? s : s.name || s.schoolName || '')
    } else {
      schoolSuggestions.value = []
    }
  } catch {
    schoolSuggestions.value = []
  } finally {
    suggestLoading.value = false
  }
}

function selectSchool(school: string) {
  targetSchool.value = school
  schoolSuggestions.value = []
}

async function fetchActivity() {
  loading.value = true
  errorMessage.value = ''
  notFound.value = false
  try {
    const id = Number(route.params.id)
    if (!id) {
      notFound.value = true
      loading.value = false
      return
    }

    const res: any = await activityApi.getById(id)

    // Handle different response shapes
    if (res?.data) {
      activity.value = res.data
    } else if (res?.id) {
      activity.value = res
    } else {
      notFound.value = true
      loading.value = false
      return
    }

    // Check if already enrolled
    await checkEnrollmentStatus()
  } catch (err: any) {
    activity.value = null
    errorMessage.value = err?.response?.data?.message || '加载活动详情失败，请稍后重试'
    if (err?.response?.status === 404) {
      notFound.value = true
      if (err?.response?.status !== 401) {
        ElMessage.error('活动不存在或已被删除')
      }
    } else if (err?.response?.status !== 401) {
      ElMessage.error(errorMessage.value)
    }
  } finally {
    loading.value = false
  }
}

async function checkEnrollmentStatus() {
  try {
    const res: any = await enrollmentApi.listMy({ page: 1, size: 100 })
    let enrollments: any[] = []
    if (res?.data?.list) {
      enrollments = res.data.list
    } else if (res?.data?.records) {
      enrollments = res.data.records
    } else if (res?.records) {
      enrollments = res.records
    } else if (Array.isArray(res)) {
      enrollments = res
    }

    if (activity.value) {
      alreadyEnrolled.value = enrollments.some(
        (e: any) => e.activityId === activity.value!.id && e.status !== 'WITHDRAWN'
      )
    }
  } catch {
    // Silently fail enrollment check — user can still try to enroll
    alreadyEnrolled.value = false
  }
}

function handleEnroll() {
  if (!activity.value) return
  targetSchool.value = ''
  schoolSuggestions.value = []
  briefIntro.value = ''
  enrollDialogVisible.value = true
}

async function submitEnroll() {
  if (!activity.value) return

  enrolling.value = true
  try {
    await enrollmentApi.submit({
      activityId: activity.value.id,
      targetSchool: targetSchool.value,
      intro: briefIntro.value,
    })
    ElMessage.success('报名成功！')
    alreadyEnrolled.value = true
    enrollDialogVisible.value = false
    // Refresh activity to update counts
    await fetchActivity()
  } catch (err: any) {
    const msg = err?.response?.data?.message || '报名失败，请稍后重试'
    if (err?.response?.status !== 401) {
      ElMessage.error(msg)
    }
  } finally {
    enrolling.value = false
  }
}

onMounted(() => {
  fetchActivity()
})
</script>

<style scoped>
.activity-detail {
  max-width: 1200px;
  margin: 0 auto;
}

/* Hero */
.hero {
  border-radius: 12px;
  margin-bottom: 24px;
  overflow: hidden;
}

.hero-content {
  padding: 48px 56px;
  color: #fff;
}

.hero-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.hero h1 {
  margin: 0 0 16px;
  font-size: 28px;
  font-weight: 700;
  line-height: 1.3;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.hero-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  font-size: 14px;
  opacity: 0.9;
}

.hero-meta span {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* Two-Column Layout */
.detail-layout {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 24px;
  align-items: start;
}

@media (max-width: 900px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }
  .hero-content {
    padding: 32px 24px;
  }
  .hero h1 {
    font-size: 22px;
  }
}

/* Cards */
.detail-card,
.info-card {
  margin-bottom: 20px;
  border-radius: 12px;
  border: 1px solid #eee;
}

.card-section-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
}

.description-content {
  font-size: 14px;
  line-height: 1.8;
  color: #444;
  white-space: pre-wrap;
}

/* Quota */
.quota-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.quota-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.quota-label {
  font-size: 13px;
  color: #888;
  font-weight: 500;
}

.quota-value {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
}

.quota-bar-wrapper {
  width: 100%;
}

.quota-text {
  font-size: 12px;
}

/* Enroll Section */
.enroll-section {
  margin-bottom: 20px;
}

.enroll-section .el-button--large {
  height: 48px;
  font-size: 16px;
  border-radius: 10px;
  font-weight: 600;
}

/* Attachments */
.attachment-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  font-size: 14px;
  color: #444;
}

.attachment-item:not(:last-child) {
  border-bottom: 1px solid #f5f5f5;
}

.attachment-item span {
  flex: 1;
}

/* School Suggest */
.school-suggest-wrapper {
  position: relative;
  width: 100%;
}

.suggest-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  z-index: 100;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  max-height: 200px;
  overflow-y: auto;
}

.suggest-item {
  padding: 10px 14px;
  cursor: pointer;
  font-size: 14px;
  color: #333;
  transition: background 0.15s;
}

.suggest-item:hover {
  background: #f0f5ff;
  color: #1a56db;
}
</style>
