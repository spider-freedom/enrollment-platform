<template>
  <div class="activity-detail" v-loading="loading">
    <div v-if="error" class="error-state">
      <el-result icon="error" title="加载失败" :sub-title="error">
        <template #extra><el-button type="primary" @click="fetchActivity">{{ $t("重新加载") }}</el-button></template>
      </el-result>
    </div>
    <div v-else-if="!activity && !loading" class="empty-state">
      <el-result icon="info" title="活动未找到" sub-title="该活动不存在或已被删除" />
    </div>

    <template v-else-if="activity">
      <div class="hero" :style="{ background: 'linear-gradient(135deg, #1a3a5c, #2d6a9f)' }">
        <h1>{{ activity.title }}</h1>
        <el-tag :type="activityTypeTagType(activity.type)">{{ ACTIVITY_TYPE_MAP[activity.type] || activity.type }}</el-tag>
        <el-tag :type="getDisplayStatusTagType(getDisplayStatus(activity))" style="margin-left:8px">{{ getDisplayStatus(activity) }}</el-tag>
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
        <h3>{{ $t("活动详情") }}</h3>
        <p>{{ activity.description }}</p>
      </el-card>

      <!-- Enroll Button -->
      <div style="text-align:center;margin-top:24px">
        <el-button v-if="alreadyEnrolled" type="info" size="large" disabled style="width:200px">{{ $t("已报名") }}</el-button>
        <el-button v-else-if="canEnroll(activity)" type="primary" size="large" style="width:200px" :loading="enrolling" @click="handleEnroll">{{ $t("立即报名") }}</el-button>
        <el-button v-else type="info" size="large" disabled style="width:200px">
          {{ getDisplayStatus(activity) === '报名已截止' ? '报名已截止' : getDisplayStatus(activity) === '已结束' ? '已结束' : '暂不可报名' }}
        </el-button>
      </div>

      <!-- Enroll Dialog -->
      <el-dialog v-model="dialogVisible" title="完善报名信息" width="480px" top="8vh" :close-on-click-modal="false">
        <el-form label-width="80px">
          <el-form-item label="我的学院"><el-input :model-value="myCollege" disabled /></el-form-item>
          <el-form-item label="个人简介">
            <el-input v-model="intro" type="textarea" :rows="4" placeholder="简单介绍一下自己..." maxlength="500" show-word-limit />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible=false">{{ $t("取消") }}</el-button>
          <el-button type="primary" :loading="enrolling" @click="submitEnroll">{{ $t("确认报名") }}</el-button>
        </template>
      </el-dialog>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { activityApi, enrollmentApi } from '@/api'
import { useUserStore } from '@/stores/user'
import type { Activity } from '@/types'
import { ACTIVITY_TYPE_MAP, TARGET_AUDIENCE_MAP, activityTypeTagType, getDisplayStatus, getDisplayStatusTagType, canEnroll } from '@/utils/constants'

const route = useRoute()
const store = useUserStore()
const activity = ref<Activity | null>(null)
const loading = ref(false)
const error = ref('')
const enrolling = ref(false)
const alreadyEnrolled = ref(false)
const dialogVisible = ref(false)
const intro = ref('')
const myCollege = computed(() => store.userInfo?.collegeName || '未知学院')

async function fetchActivity() {
  loading.value = true; error.value = ''
  try {
    const res = await activityApi.getById(Number(route.params.id))
    activity.value = res?.data || res
    await checkEnrollment()
  } catch (e: any) {
    error.value = e?.response?.data?.message || e?.message || '加载失败'
  } finally { loading.value = false }
}

async function checkEnrollment() {
  try {
    const res: any = await enrollmentApi.listMy({ page:1, size:100 })
    let enrolls: any[] = []
    if (res?.data?.list) enrolls = res.data.list
    else if (res?.data?.records) enrolls = res.data.records
    else if (res?.records) enrolls = res.records
    else if (Array.isArray(res)) enrolls = res
    alreadyEnrolled.value = enrolls.some((e: any) => e.activityId === activity.value!.id && e.status !== 'WITHDRAWN')
  } catch { alreadyEnrolled.value = false }
}

function handleEnroll() {
  intro.value = ''
  dialogVisible.value = true
}

async function submitEnroll() {
  if (!activity.value) return
  enrolling.value = true
  try {
    await enrollmentApi.submit({ activityId: activity.value.id, intro: intro.value })
    ElMessage.success('报名成功！')
    alreadyEnrolled.value = true
    dialogVisible.value = false
    await fetchActivity()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || e?.message || '报名失败')
  } finally { enrolling.value = false }
}

onMounted(() => { fetchActivity() })
</script>

<style scoped>
.hero { padding: 48px; color: #fff; border-radius: 8px; margin-bottom: 20px; }
.hero h1 { margin: 0 0 12px; font-size: 24px; }
.hero .el-tag { margin-right: 8px; }
.detail-card { margin-bottom: 20px; }
.error-state, .empty-state { padding: 60px 0; }
</style>
