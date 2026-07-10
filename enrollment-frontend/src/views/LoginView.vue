<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-accent-bar" />
      <div class="login-logo">🏫</div>
      <h1 class="login-title">新疆大学招生宣传报名平台</h1>
      <p class="login-sub">统一身份认证登录</p>
      <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="学号/工号" size="large" prefix-icon="User" clearable />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" prefix-icon="Lock" show-password clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin" size="large" style="width:100%">登 录</el-button>
        </el-form-item>
      </el-form>
      <div class="login-links">
        <router-link to="/register">没有账号？立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const store = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const rolePathMap: Record<string, string> = {
  STUDENT: '/student/activities',
  TEACHER: '/teacher/activities',
  COLLEGE_ADMIN: '/college/activities',
  SCHOOL_ADMIN: '/school/dashboard',
  student: '/student/activities',
  teacher: '/teacher/activities',
  college_admin: '/college/activities',
  school_admin: '/school/dashboard',
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await store.login(form.username, form.password)
    ElMessage.success('登录成功')
    const role = store.currentRole
    const path = role ? rolePathMap[role] : null
    router.push(path || '/student/activities')
  } catch (e: any) {
    const msg = e?.response?.data?.message || e?.message || '登录失败'
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #060e1f 0%, #0f1d3d 30%, #13294b 50%, #1a3a6b 70%, #1a56db 100%);
  position: relative;
  overflow: hidden;
}
.login-page::before {
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
  animation: bg-drift 20s ease-in-out infinite;
}
@keyframes bg-drift {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  33% { transform: translate(2%, 1%) rotate(1deg); }
  66% { transform: translate(-1%, -2%) rotate(-1deg); }
}
.login-card {
  position: relative;
  background: #fff;
  border-radius: 16px;
  padding: 0;
  width: 420px;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.35);
  overflow: hidden;
  animation: card-in 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}
@keyframes card-in {
  from { opacity: 0; transform: translateY(30px) scale(0.97); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}
.login-accent-bar {
  height: 4px;
  background: linear-gradient(90deg, #1a56db, #3b82f6, #60a5fa);
}
.login-logo {
  font-size: 42px;
  text-align: center;
  margin-top: 32px;
  line-height: 1;
}
.login-title {
  font-size: 21px;
  text-align: center;
  margin: 12px 40px 4px;
  color: #1f2937;
  font-weight: 700;
}
.login-sub {
  text-align: center;
  color: #9ca3af;
  font-size: 13px;
  margin: 0 40px 28px;
}
.login-card .el-form {
  padding: 0 40px 24px;
}
.login-links {
  text-align: center;
  padding: 0 40px 32px;
  font-size: 13px;
}
.login-links a { color: #1a56db; text-decoration: none; }
.login-links a:hover { text-decoration: underline; }
</style>
