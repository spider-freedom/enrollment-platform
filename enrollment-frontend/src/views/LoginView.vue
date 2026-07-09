<template>
  <div class="login-page">
    <div class="login-card">
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
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await store.login(form.username, form.password)
    ElMessage.success('登录成功')
    const role = store.currentRole
    if (role && rolePathMap[role]) {
      router.push(rolePathMap[role])
    } else if (role) {
      const lc = role.toLowerCase()
      const fallback = rolePathMap[Object.keys(rolePathMap).find(k => k.toLowerCase() === lc) || '']
      router.push(fallback || '/student/activities')
    } else {
      router.push('/student/activities')
    }
  } catch (e: any) {
    const msg = e?.response?.data?.message || e?.message || '登录失败'
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page { height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #0f1d3d, #1a3a6b 40%, #1a56db 100%); }
.login-card { background: #fff; border-radius: 16px; padding: 44px 40px; width: 420px; box-shadow: 0 20px 60px rgba(0,0,0,.3); }
.login-title { font-size: 22px; text-align: center; margin-bottom: 4px; color: #1f2937; }
.login-sub { text-align: center; color: #9ca3af; font-size: 13px; margin-bottom: 32px; }
.login-links { text-align: center; margin-top: 16px; font-size: 13px; }
.login-links a { color: #1a56db; text-decoration: none; }
.login-links a:hover { text-decoration: underline; }
</style>
