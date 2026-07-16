<template>
  <div class="register-page">
    <div class="register-card">
      <div class="register-accent-bar" />
      <div class="register-logo">🏫</div>
      <h1 class="register-title">注册账号</h1>
      <p class="register-sub">新疆大学 · 招生宣传报名平台</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" placeholder="学号/工号" @input="form.username=form.username.replace(/[^a-zA-Z]/g,'')" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="真实姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" type="password" placeholder="至少6位" show-password />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="确认密码" prop="confirmPwd">
              <el-input v-model="form.confirmPwd" type="password" placeholder="再次输入密码" show-password />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="角色" prop="role">
          <el-radio-group v-model="form.role">
            <el-radio value="STUDENT">学生</el-radio>
            <el-radio value="TEACHER">教师</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="学院" prop="collegeId">
          <el-select v-model="form.collegeId" placeholder="请选择或输入关键词搜索学院" style="width:100%" filterable @change="onCollegeChange">
            <el-option v-for="c in colleges" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.role === 'STUDENT'" label="专业">
          <el-input v-model="form.major" placeholder="专业名称" />
        </el-form-item>
        <el-form-item v-if="form.role === 'STUDENT'" label="年级">
          <el-select v-model="form.grade" placeholder="年级" style="width:100%">
            <el-option label="2025级" value="2025级" />
            <el-option label="2024级" value="2024级" />
            <el-option label="2023级" value="2023级" />
            <el-option label="2022级" value="2022级" />
            <el-option label="2021级" value="2021级" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleRegister" style="width:100%" size="large">注 册</el-button>
        </el-form-item>
        <div style="text-align: center; font-size: 13px; color: #999;">
          已有账号？<router-link to="/login">返回登录</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const colleges = [
  { id: 1, name: '计算机科学与技术学院' },
  { id: 2, name: '数学与系统科学学院' },
  { id: 3, name: '信息科学与工程学院' },
  { id: 4, name: '物理科学与技术学院' },
  { id: 5, name: '化学学院' },
  { id: 6, name: '生命科学与技术学院' },
  { id: 7, name: '经济与管理学院' },
  { id: 8, name: '法学院' },
  { id: 9, name: '中国语言文学学院' },
  { id: 10, name: '软件学院' },
  { id: 11, name: '电气工程学院' },
  { id: 12, name: '机械工程学院' },
  { id: 13, name: '土木工程学院' },
  { id: 14, name: '建筑与城市规划学院' },
  { id: 15, name: '资源与环境科学学院' },
  { id: 16, name: '新闻与传播学院' },
  { id: 17, name: '外国语学院' },
  { id: 18, name: '马克思主义学院' },
  { id: 19, name: '国际文化交流学院' },
  { id: 20, name: '旅游学院' },
  { id: 21, name: '政治与公共管理学院' },
  { id: 22, name: '商学院' },
  { id: 23, name: '艺术学院' },
  { id: 24, name: '体育教学研究部' },
]

const form = reactive({
  username: '',
  password: '',
  confirmPwd: '',
  name: '',
  role: 'STUDENT' as string,
  collegeId: null as number | null,
  collegeName: '',
  major: '',
  grade: '',
  email: '',
  phone: '',
})

const validateConfirm = (_rule: any, value: string, cb: any) => {
  if (value !== form.password) cb(new Error('两次密码不一致'))
  else cb()
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
  ],
  confirmPwd: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' },
  ],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  collegeId: [{ required: true, message: '请选择学院', trigger: 'change' }],
}

function onCollegeChange(val: number) {
  const c = colleges.find(c => c.id === val)
  form.collegeName = c ? c.name : ''
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const payload: any = {
      username: form.username,
      password: form.password,
      name: form.name,
      role: form.role,
      collegeId: form.collegeId,
      collegeName: form.collegeName,
    }
    if (form.major) payload.major = form.major
    if (form.grade) payload.grade = form.grade
    if (form.email) payload.email = form.email
    if (form.phone) payload.phone = form.phone
    const res: any = await authApi.register(payload)
    if (res.code === 200) {
      ElMessage.success('注册成功！请登录')
      router.push('/login')
    } else {
      ElMessage.error(res.message || '注册失败')
    }
  } catch (e: any) {
    const msg = e?.response?.data?.message || e?.message || '注册失败，请检查网络连接'
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #060e1f 0%, #0f1d3d 30%, #13294b 50%, #1a3a6b 70%, #1a56db 100%);
  position: relative;
  overflow: hidden;
  padding: 24px;
}
.register-page::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background:
    radial-gradient(circle at 20% 50%, rgba(26, 86, 219, 0.15) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(59, 130, 246, 0.12) 0%, transparent 40%),
    radial-gradient(circle at 60% 80%, rgba(16, 185, 129, 0.08) 0%, transparent 45%),
    radial-gradient(circle at 40% 30%, rgba(139, 92, 246, 0.06) 0%, transparent 35%);
  animation: bg-drift-reg 20s ease-in-out infinite;
}
@keyframes bg-drift-reg {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  33% { transform: translate(2%, 1%) rotate(1deg); }
  66% { transform: translate(-1%, -2%) rotate(-1deg); }
}
.register-card {
  position: relative;
  background: #fff;
  border-radius: 16px;
  padding: 0;
  width: 520px;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.35);
  overflow: hidden;
  animation: card-in-reg 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}
@keyframes card-in-reg {
  from { opacity: 0; transform: translateY(30px) scale(0.97); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}
.register-accent-bar {
  height: 4px;
  background: linear-gradient(90deg, #1a56db, #3b82f6, #60a5fa);
}
.register-logo {
  font-size: 42px;
  text-align: center;
  margin-top: 28px;
  line-height: 1;
}
.register-title {
  font-size: 22px;
  text-align: center;
  margin: 10px 0 4px;
  font-weight: 700;
  color: #1f2937;
}
.register-sub {
  text-align: center;
  color: #9ca3af;
  font-size: 13px;
  margin-bottom: 22px;
}
.register-card .el-form {
  padding: 0 32px 28px;
}
</style>
