<template>
  <div class="user-management">
    <div class="page-header"><h2>本院用户管理</h2><p>管理本学院的学生、教师和管理员账号</p></div>

    <div class="stats-row">
      <div class="stat-card c1"><div class="stat-icon">👥</div><div class="stat-value">{{ stats.total }}</div><div class="stat-label">用户总数</div></div>
      <div class="stat-card c2"><div class="stat-icon">🛡️</div><div class="stat-value">{{ stats.admins }}</div><div class="stat-label">管理员</div></div>
      <div class="stat-card c3"><div class="stat-icon">👨‍🏫</div><div class="stat-value">{{ stats.teachers }}</div><div class="stat-label">教师</div></div>
      <div class="stat-card c4"><div class="stat-icon">🎓</div><div class="stat-value">{{ stats.students }}</div><div class="stat-label">学生</div></div>
    </div>

    <div class="card">
      <div class="card-header">
        <div class="filter-bar">
          <el-input v-model="filterKeyword" placeholder="搜索用户名/姓名/邮箱" clearable style="width:260px" @clear="filterKeyword=''" />
          <el-select v-model="filterRole" placeholder="角色筛选" clearable style="width:140px">
            <el-option label="学院管理员" value="COLLEGE_ADMIN" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="学生" value="STUDENT" />
          </el-select>
          <el-button type="primary" @click="fetchUsers">刷新</el-button>
        </div>
        <div class="header-actions">
          <el-button type="success" @click="triggerImport">📤 导入CSV</el-button>
        </div>
      </div>
      <input ref="fileInput" type="file" accept=".csv" style="display:none" @change="handleImportUsers" />

      <el-alert v-if="errorMsg" :title="errorMsg" type="error" show-icon closable style="margin-bottom:16px" @close="errorMsg=''" />

      <el-table :data="filteredList" v-loading="loading" border stripe>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column label="角色" width="110">
          <template #default="{ row }">
            <el-tag v-if="isCollegeAdmin(row.role)" type="warning" size="small">学院管理员</el-tag>
            <el-tag v-else-if="isTeacher(row.role)" type="success" size="small">教师</el-tag>
            <el-tag v-else-if="isStudent(row.role)" type="info" size="small">学生</el-tag>
            <el-tag v-else size="small">{{ row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="major" label="专业" width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.major || '-' }}</template>
        </el-table-column>
        <el-table-column prop="grade" label="年级" width="80">
          <template #default="{ row }">{{ row.grade || '-' }}</template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.email || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="(row.status||'').toUpperCase() === 'ACTIVE'"
              active-text="正常"
              inactive-text="禁用"
              inline-prompt
              size="small"
              @change="(val: boolean) => handleToggleStatus(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime || row.create_time) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button v-if="isTeacher(row.role)" type="warning" size="small" @click="handlePromote(row)" plain>提升为学院管理员</el-button>
            <el-button v-if="isCollegeAdmin(row.role)" type="primary" size="small" @click="handleDemote(row)" plain>降为教师</el-button>
            <el-button size="small" @click="handleResetPassword(row)" plain>重置密码</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)" plain>删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && userList.length === 0" description="暂无用户数据" />
      <el-empty v-else-if="!loading && userList.length > 0 && filteredList.length === 0" description="没有匹配的用户" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { collegeUserApi } from '@/api'

const userList = ref<any[]>([])
const loading = ref(false)
const errorMsg = ref('')
const filterKeyword = ref('')
const filterRole = ref('')
const fileInput = ref<HTMLInputElement | null>(null)

function isStudent(r: string) { return (r||'').toUpperCase() === 'STUDENT' }
function isTeacher(r: string) { return (r||'').toUpperCase() === 'TEACHER' }
function isCollegeAdmin(r: string) { return (r||'').toUpperCase() === 'COLLEGE_ADMIN' }
function formatTime(t: string) { return t ? t.substring(0, 19).replace('T',' ') : '-' }

const stats = computed(() => {
  const total = userList.value.length
  const admins = userList.value.filter(u => isCollegeAdmin(u.role)).length
  const teachers = userList.value.filter(u => isTeacher(u.role)).length
  const students = userList.value.filter(u => isStudent(u.role)).length
  return { total, admins, teachers, students }
})

const filteredList = computed(() => {
  let data = userList.value
  if (filterRole.value) {
    data = data.filter(u => (u.role||'').toUpperCase() === filterRole.value.toUpperCase())
  }
  if (filterKeyword.value.trim()) {
    const kw = filterKeyword.value.trim().toLowerCase()
    data = data.filter(u =>
      (u.username||'').toLowerCase().includes(kw) ||
      (u.name||'').toLowerCase().includes(kw) ||
      (u.email||'').toLowerCase().includes(kw)
    )
  }
  return data
})

async function fetchUsers() {
  loading.value = true; errorMsg.value = ''
  try {
    const res: any = await collegeUserApi.listUsers()
    userList.value = Array.isArray(res?.data) ? res.data : Array.isArray(res) ? res : (res?.data?.records || res?.records || [])
  } catch (err: any) {
    errorMsg.value = err?.response?.data?.message || '获取用户列表失败'
  } finally { loading.value = false }
}

async function handleToggleStatus(row: any, active: boolean) {
  try {
    const status = active ? 'ACTIVE' : 'DISABLED'
    await collegeUserApi.toggleStatus(row.id, status)
    row.status = status
    ElMessage.success(`已${active ? '启用' : '禁用'}「${row.name}」`)
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '操作失败')
  }
}

async function handlePromote(row: any) {
  try {
    await ElMessageBox.confirm(`确定将教师「${row.name}」提升为学院管理员吗？`, '确认操作', { confirmButtonText:'确定提升', cancelButtonText:'取消', type:'warning' })
    await collegeUserApi.promote(row.id)
    ElMessage.success(`已提升「${row.name}」为学院管理员`)
    await fetchUsers()
  } catch (err: any) {
    if (err !== 'cancel') ElMessage.error(err?.response?.data?.message || '操作失败')
  }
}

async function handleDemote(row: any) {
  try {
    await ElMessageBox.confirm(`确定将「${row.name}」降为教师吗？`, '确认操作', { confirmButtonText:'确定降级', cancelButtonText:'取消', type:'warning' })
    await collegeUserApi.demote(row.id)
    ElMessage.success(`已降级「${row.name}」为教师`)
    await fetchUsers()
  } catch (err: any) {
    if (err !== 'cancel') ElMessage.error(err?.response?.data?.message || '操作失败')
  }
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm(`确定删除用户「${row.name}」吗？`, '确认删除', { confirmButtonText:'确定删除', cancelButtonText:'取消', type:'error' })
    await collegeUserApi.deleteUser(row.id)
    ElMessage.success(`已删除「${row.name}」`)
    await fetchUsers()
  } catch (err: any) {
    if (err !== 'cancel') ElMessage.error(err?.response?.data?.message || '操作失败')
  }
}

async function handleResetPassword(row: any) {
  try {
    await ElMessageBox.confirm(`确定重置「${row.name}」的密码为 123456 吗？`, '确认操作', { confirmButtonText:'确定重置', cancelButtonText:'取消', type:'warning' })
    await collegeUserApi.resetPassword(row.id)
    ElMessage.success(`密码已重置为 123456`)
  } catch (err: any) {
    if (err !== 'cancel') ElMessage.error(err?.response?.data?.message || '操作失败')
  }
}

function triggerImport() { fileInput.value?.click() }

async function handleImportUsers(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  try {
    const res: any = await collegeUserApi.importUsers(file)
    ElMessage.success(`导入完成：成功 ${res?.data?.success || 0}，失败 ${res?.data?.fail || 0}`)
    await fetchUsers()
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '导入失败')
  } finally { input.value = '' }
}

onMounted(() => fetchUsers())
</script>

<style scoped>
.user-management { max-width: 1200px; }
.page-header { margin-bottom: 20px; }
.page-header h2 { font-size: 22px; font-weight: 700; }
.page-header p { color: #999; font-size: 13px; margin-top: 4px; }

.stats-row { display: grid; grid-template-columns: repeat(4,1fr); gap: 16px; margin-bottom: 20px; }
.stat-card { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,.06); border: 1px solid #eee; display: flex; align-items: center; gap: 14px; }
.stat-card .stat-icon { font-size: 32px; }
.stat-card .stat-value { font-size: 28px; font-weight: 800; line-height: 1.2; }
.stat-card .stat-label { font-size: 12px; color: #999; }
.stat-card.c1 { border-left: 4px solid #1a56db; }
.stat-card.c2 { border-left: 4px solid #e6a23c; }
.stat-card.c3 { border-left: 4px solid #67c23a; }
.stat-card.c4 { border-left: 4px solid #409eff; }

.card { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,.06); border: 1px solid #eee; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; flex-wrap: wrap; gap: 10px; }
.filter-bar { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
.header-actions { display: flex; gap: 8px; }
</style>
