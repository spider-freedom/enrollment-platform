<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-top">
        <div class="login-icon-box">
          <img src="/images/xju-logo.png" style="width:48px;height:auto;filter:brightness(0) invert(1)" alt="新疆大学" />
        </div>
        <h1 class="login-title">欢迎登录</h1>
        <p class="login-sub">新疆大学招生宣传平台</p>
      </div>

      <!-- Role selector -->
      <div class="role-selector">
        <button
          v-for="r in roles"
          :key="r.value"
          :class="['role-btn', { active: selectedRole === r.value }]"
          @click="selectedRole = r.value"
        >{{ r.label }}</button>
      </div>

      <div class="form-wrap">
        <div class="input-group">
          <el-icon :size="18" class="input-icon"><User /></el-icon>
          <input
            v-model="form.username"
            class="login-input"
            placeholder="学号 / 工号"
            autocomplete="off"
            @input="form.username=form.username.replace(/[一-鿿]/g,'')"
            @keyup.enter="handleLogin"
          />
        </div>
        <div class="input-group">
          <el-icon :size="18" class="input-icon"><Lock /></el-icon>
          <input
            v-model="form.password"
            :type="showPwd ? 'text' : 'password'"
            class="login-input"
            placeholder="密码"
            autocomplete="new-password"
            @keyup.enter="handleLogin"
          />
          <el-icon :size="18" class="input-eye" @click="showPwd=!showPwd" style="cursor:pointer">
            <component :is="showPwd ? 'View' : 'Hide'" />
          </el-icon>
        </div>

        <el-button
          type="primary"
          size="large"
          :loading="loading"
          @click="handleLogin"
          style="width:100%;height:48px;font-size:16px;font-weight:600;margin-bottom:16px"
        >
          登 录
        </el-button>

        <div class="login-links">
          <router-link to="/register">还没有账号？立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { User, Lock, View, Hide } from '@element-plus/icons-vue'

const router = useRouter()
const store = useUserStore()
const formRef = ref()
const loading = ref(false)
const showPwd = ref(false)
const selectedRole = ref('')

const roles = [
  { label: '学生', value: 'student' },
  { label: '教师', value: 'teacher' },
  { label: '学院管理员', value: 'college_admin' },
  { label: '学校管理员', value: 'school_admin' },
]

const form = reactive({ username: '', password: '' })

const rolePathMap: Record<string, string> = {
  STUDENT:'/student/activities', TEACHER:'/teacher/activities',
  COLLEGE_ADMIN:'/college/activities', SCHOOL_ADMIN:'/school/dashboard',
  student:'/student/activities', teacher:'/teacher/activities',
  college_admin:'/college/activities', school_admin:'/school/dashboard',
}

const roleMap: Record<string, string> = {
  student:'STUDENT', teacher:'TEACHER', college_admin:'COLLEGE_ADMIN', school_admin:'SCHOOL_ADMIN',
}

onMounted(() => {
  form.username = ''; form.password = ''; selectedRole.value = ''
})

async function handleLogin() {
  if (!form.username) { ElMessage.warning('请输入用户名'); return }
  if (!form.password) { ElMessage.warning('请输入密码'); return }
  if (!selectedRole.value) { ElMessage.warning('请选择身份'); return }
  loading.value = true
  try {
    await store.login(form.username, form.password)
    const actualRole = store.currentRole
    if (selectedRole.value && roleMap[selectedRole.value]) {
      if (roleMap[selectedRole.value] !== actualRole?.toUpperCase()) {
        store.logout()
        ElMessage.error('身份不匹配！请选择正确的身份登录')
        loading.value = false
        return
      }
    }
    ElMessage.success('登录成功')
    router.push(actualRole ? (rolePathMap[actualRole] || '/student/activities') : '/student/activities')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || e?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(130,24,41,0.85) 0%, rgba(163,31,52,0.8) 50%, rgba(194,61,79,0.85) 100%),
    url('/images/campus-library.jpg') center/cover no-repeat;
  padding: 20px;
}
.login-card {
  width: 420px;
  background: #fff;
  border-radius: 24px;
  box-shadow: 0 24px 80px rgba(0,0,0,0.25);
  overflow: hidden;
}
.login-top {
  text-align: center;
  padding: 36px 40px 8px;
}
.login-icon-box {
  width: 56px; height: 56px;
  background: #A31F34;
  border-radius: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(163,31,52,0.3);
  margin-bottom: 16px;
}
.login-icon-text {
  color: #fff;
  font-size: 24px;
  font-weight: 800;
}
.login-title {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 6px;
}
.login-sub {
  font-size: 13px;
  color: #94a3b8;
  margin: 0;
}

/* Role selector */
.role-selector {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  padding: 0 40px 20px;
}
.role-btn {
  padding: 8px 0;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #fff;
  color: #64748b;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 500;
}
.role-btn:hover { border-color: #A31F34; color: #A31F34; }
.role-btn.active {
  background: #A31F34;
  color: #fff;
  border-color: #A31F34;
  box-shadow: 0 2px 8px rgba(163,31,52,0.2);
}

.form-wrap { padding: 0 40px 32px; }
.input-group {
  position: relative;
  margin-bottom: 16px;
}
.input-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
  z-index: 1;
}
.input-eye {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
  z-index: 1;
}
.login-input {
  width: 100%;
  padding: 12px 40px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  font-size: 14px;
  background: #f8fafc;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
  box-sizing: border-box;
  color: #334155;
}
.login-input::placeholder { color: #94a3b8; }
.login-input:focus {
  border-color: #A31F34;
  box-shadow: 0 0 0 3px rgba(163,31,52,0.1);
}
.login-links { text-align: center; font-size: 13px; }
.login-links a { color: #A31F34; text-decoration: none; }
.login-links a:hover { text-decoration: underline; }
</style>
