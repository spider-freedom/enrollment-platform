<template>
  <div class="feedback-submit">
    <div class="page-header"><h3>提交活动反馈</h3><p>请对您参加的活动进行评价</p></div>
    <el-card class="form-card">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="活动评分" prop="rating">
          <el-rate v-model="form.rating" :max="5" :texts="['很差','较差','一般','满意','非常满意']" show-text :colors="['#f56c6c','#e6a23c','#67c23a']" size="large" />
        </el-form-item>
        <el-form-item label="反馈内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请分享您的活动体验、收获和建议..." maxlength="2000" show-word-limit />
        </el-form-item>
        <el-form-item label="联系方式" prop="contact">
          <el-input v-model="form.contact" placeholder="手机号或邮箱（选填）" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit" size="large">提交反馈</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { feedbackApi, enrollmentApi } from '@/api'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const activityId = ref<number | null>(null)

const form = reactive({ rating: 0, content: '', contact: '' })
const rules = {
  rating: [{
    validator: (_rule: any, value: number, callback: (e?: Error) => void) => {
      if (!value || value < 1) callback(new Error('请评分'))
      else callback()
    },
    trigger: 'change',
  }],
  content: [{ required: true, message: '请输入反馈内容', trigger: 'blur' }, { min: 10, message: '至少10个字符', trigger: 'blur' }],
}

onMounted(async () => {
  try {
    const enrollmentId = Number(route.params.enrollmentId)
    const enr: any = await enrollmentApi.getById(enrollmentId)
    activityId.value = enr?.data?.activityId || enr?.activityId || null
    if (!activityId.value) {
      // fallback: try to get enrollment list and find it
      const list: any = await enrollmentApi.listMy({ page:1, size:50 })
      const found = (list?.list || list?.data?.list || []).find((e:any) => e.id === enrollmentId || e.enrollmentId === enrollmentId)
      activityId.value = found?.activityId || null
    }
  } catch { activityId.value = null }
})

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (!activityId.value) { ElMessage.error('无法获取活动信息，请返回重试'); return }
  submitting.value = true
  try {
    await feedbackApi.submitStudent({ activityId: activityId.value, content: form.content, rating: form.rating, contact: form.contact || undefined })
    ElMessage.success('反馈提交成功！')
    router.push('/student/my-feedback')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '提交失败')
  } finally { submitting.value = false }
}
</script>

<style scoped>
.feedback-submit { max-width: 720px; margin: 0 auto; }
.page-header { margin-bottom: 24px; }
.page-header h3 { font-size: 20px; margin-bottom: 4px; }
.page-header p { color: #999; font-size: 13px; }
</style>
