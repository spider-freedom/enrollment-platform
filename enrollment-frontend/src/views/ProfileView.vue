<template>
  <div class="profile">
    <el-card>
      <template #header><h3>个人信息</h3></template>
      <el-descriptions :column="2" border v-if="store.userInfo">
        <el-descriptions-item label="用户名">{{ store.userInfo.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ store.userInfo.name }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag>{{ roleLabel(store.userInfo.role) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="学院">{{ store.userInfo.collegeName || '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="store.userInfo.major" label="专业">{{ store.userInfo.major }}</el-descriptions-item>
        <el-descriptions-item v-if="store.userInfo.grade" label="年级">{{ store.userInfo.grade }}</el-descriptions-item>
        <el-descriptions-item v-if="store.userInfo.gpa" label="GPA">{{ store.userInfo.gpa }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ store.userInfo.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机">{{ store.userInfo.phone || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card style="margin-top:20px">
      <template #header><h3>编辑个人信息</h3></template>
      <el-form :model="profileForm" label-width="100px" style="max-width:550px">
        <el-form-item label="姓名">
          <el-input v-model="profileForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="学院">
          <el-input v-model="profileForm.collegeName" placeholder="请输入学院名称" />
        </el-form-item>
        <el-form-item v-if="store.userInfo?.major !== undefined" label="专业">
          <el-input v-model="profileForm.major" placeholder="请输入专业" />
        </el-form-item>
        <el-form-item v-if="store.userInfo?.grade !== undefined" label="年级">
          <el-input v-model="profileForm.grade" placeholder="请输入年级" />
        </el-form-item>
        <el-form-item v-if="store.userInfo?.gpa !== undefined" label="GPA">
          <el-input v-model="profileForm.gpa" placeholder="GPA由系统管理" disabled />
          <span style="font-size:12px;color:#999;margin-left:8px">GPA与学号绑定，不可自行修改</span>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSaveProfile">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top:20px">
      <template #header><h3>修改密码</h3></template>
      <el-form :model="pwdForm" label-width="120px" style="max-width:400px">
        <el-form-item label="原密码"><el-input v-model="pwdForm.oldPassword" type="password" show-password /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="pwdForm.newPassword" type="password" show-password /></el-form-item>
        <el-form-item label="确认新密码"><el-input v-model="pwdForm.confirmPassword" type="password" show-password /></el-form-item>
        <el-form-item><el-button type="primary" @click="handleChangePwd">修改密码</el-button></el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api'

const store = useUserStore()
const saving = ref(false)
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const profileForm = reactive({
  name: '', collegeName: '', major: '', grade: '', gpa: '', email: '', phone: '',
})

// Sync profile form from store when userInfo loads
watch(() => store.userInfo, (info: any) => {
  if (info) {
    profileForm.name = info.name || ''
    profileForm.collegeName = info.collegeName || ''
    profileForm.major = info.major || ''
    profileForm.grade = info.grade || ''
    profileForm.gpa = info.gpa || ''
    profileForm.email = info.email || ''
    profileForm.phone = info.phone || ''
  }
}, { immediate: true })

function roleLabel(role: string) {
  const map: Record<string, string> = { student:'学生', teacher:'教师', college_admin:'学院管理员', school_admin:'学校管理员', STUDENT:'学生', TEACHER:'教师', COLLEGE_ADMIN:'学院管理员', SCHOOL_ADMIN:'学校管理员' }
  return map[role] || role
}

async function handleSaveProfile() {
  saving.value = true
  try {
    await userApi.updateProfile({
      name: profileForm.name,
      collegeName: profileForm.collegeName,
      major: profileForm.major,
      grade: profileForm.grade,
      gpa: profileForm.gpa ? Number(profileForm.gpa) : undefined,
      email: profileForm.email,
      phone: profileForm.phone,
    })
    ElMessage.success('个人信息已更新')
    await store.fetchProfile()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function handleChangePwd() {
  if (!pwdForm.oldPassword) { ElMessage.error('请输入原密码'); return }
  if (!pwdForm.newPassword) { ElMessage.error('请输入新密码'); return }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    ElMessage.error('两次密码不一致')
    return
  }
  try {
    await userApi.changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '密码修改失败')
  }
}
</script>
