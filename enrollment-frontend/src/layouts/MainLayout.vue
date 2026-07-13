<template>
  <el-container class="main-layout">
    <el-header class="header">
      <div class="header-left">
        <span class="header-logo">🏫</span>
        <h2>新疆大学招生宣传报名平台</h2>
      </div>
      <div class="header-right">
        <el-popover placement="bottom-end" :width="340" trigger="click">
          <template #reference>
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" style="cursor:pointer">
              <el-icon :size="22"><Bell /></el-icon>
            </el-badge>
          </template>
          <div style="font-weight:600;padding:4px 0 12px;border-bottom:1px solid #eee;display:flex;justify-content:space-between">
            消息通知 <span style="font-size:11px;color:#999;cursor:pointer;font-weight:400" @click="markAllRead()">全部已读</span>
          </div>
          <div style="max-height:300px;overflow-y:auto">
            <div v-for="(n,i) in filteredNotifications" :key="i" style="padding:10px 0;border-bottom:1px solid #f5f5f5;cursor:pointer" @click="handleNotifyClick(n)">
              <div style="font-size:13px;font-weight:500">{{ n.icon }} {{ n.title }}</div>
              <div style="font-size:12px;color:#999;margin-top:2px">{{ n.desc }}</div>
              <div style="font-size:11px;color:#bbb;margin-top:2px">{{ n.time }}</div>
            </div>
          </div>
        </el-popover>
        <el-tag :type="roleTagType" size="small">{{ roleLabel }}</el-tag>
        <span class="user-name">{{ store.userInfo?.name || '用户' }}</span>
        <el-dropdown>
          <span class="user-info"><el-icon><ArrowDown /></el-icon></span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="$router.push(profilePath)">个人主页</el-dropdown-item>
              <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-container>
      <el-aside width="220px">
        <div class="sidebar-brand">
          <span class="sidebar-logo">🏫</span>
          <span class="sidebar-title">XJU</span>
        </div>
        <el-menu :default-active="activeMenu" router :ellipsis="false">
          <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </el-menu-item>
        </el-menu>
        <div class="sidebar-footer">© 2026 新疆大学</div>
      </el-aside>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Bell, ArrowDown, List, Checked, EditPen, User, DataAnalysis, Plus, Management } from '@element-plus/icons-vue'
import { activityApi, enrollmentApi, feedbackApi, approvalApi } from '@/api'
import { ACTIVITY_STATUS_MAP, ENROLL_STATUS_MAP } from '@/utils/constants'

const store = useUserStore()
const router = useRouter()
const route = useRoute()
const unreadCount = ref(0)

const roleLabelMap: Record<string, string> = {
  STUDENT: '学生', TEACHER: '教师', COLLEGE_ADMIN: '学院管理员', SCHOOL_ADMIN: '学校管理员',
  student: '学生', teacher: '教师', college_admin: '学院管理员', school_admin: '学校管理员',
}
const roleTagTypeMap: Record<string, string> = {
  STUDENT: 'info', TEACHER: 'success', COLLEGE_ADMIN: 'warning', SCHOOL_ADMIN: 'danger'
}

const roleLabel = computed(() => roleLabelMap[store.currentRole] || store.currentRole || '未知')
const roleTagType = computed(() => roleTagTypeMap[store.currentRole] || 'info')

const notifications = ref<any[]>([])

async function fetchNotifications() {
  const role = (store.currentRole || '').toLowerCase()
  const items: any[] = []

  try {
    if (role === 'student' || role === 'teacher') {
      // Recent published activities
      const api = role === 'teacher' ? activityApi.listTeacher : activityApi.listStudent
      const res: any = await api({ page: 1, size: 3 })
      const records = res?.data?.records || res?.records || []
      for (const a of records) {
        items.push({ icon: '📢', title: '新活动发布', desc: `「${a.title}」已开放报名`, time: a.createTime?.substring(0, 10) || '', role, path: `/${role}/activities/${a.id}` })
      }

      // Enrollment status changes
      const enrollRes: any = await enrollmentApi.listMy({ page: 1, size: 5 })
      const enrolls = enrollRes?.data?.records || enrollRes?.records || []
      for (const e of enrolls) {
        if (e.status === 'APPROVED') {
          items.push({ icon: '✅', title: '报名已通过', desc: `活动报名已通过审核`, time: e.approvedAt?.substring(0, 10) || '', role, path: `/${role}/enrollments` })
        } else if (e.status === 'REJECTED') {
          items.push({ icon: '❌', title: '报名被驳回', desc: e.rejectReason ? `原因: ${e.rejectReason}` : '报名未通过审核', time: e.updateTime?.substring(0, 10) || '', role, path: `/${role}/enrollments` })
        }
      }

      // Feedback replies
      const fbRes: any = await feedbackApi.listMy({ page: 1, size: 5 })
      const fbs = fbRes?.data?.records || fbRes?.records || []
      for (const f of fbs) {
        if (f.status === 'REPLIED') {
          items.push({ icon: '📨', title: '反馈收到回复', desc: '管理员回复了您的活动反馈', time: f.replyTime?.substring(0, 10) || '', role, path: `/${role}/my-feedback` })
        }
      }
    }

    if (role === 'college_admin') {
      const appRes: any = await approvalApi.listCollege({ page: 1, size: 1 })
      const pending = appRes?.data?.total || appRes?.total || 0
      if (pending > 0) items.push({ icon: '📋', title: '有待审批报名', desc: `有 ${pending} 条报名申请等待您审批`, time: '', role: 'college_admin', path: '/college/approvals' })

      const fbRes: any = await feedbackApi.listCollege({ page: 1, size: 1 })
      const fbTotal = fbRes?.data?.total || fbRes?.total || 0
      if (fbTotal > 0) items.push({ icon: '📨', title: '反馈管理', desc: `共有 ${fbTotal} 条活动反馈`, time: '', role: 'college_admin', path: '/college/feedbacks' })
    }

    if (role === 'school_admin') {
      const appRes: any = await approvalApi.listSchool({ page: 1, size: 1 })
      const pending = appRes?.data?.total || appRes?.total || 0
      if (pending > 0) items.push({ icon: '📋', title: '有待审批报名', desc: `有 ${pending} 条报名等待学校审批`, time: '', role: 'school_admin', path: '/school/approvals' })

      const fbRes: any = await feedbackApi.listSchool({ page: 1, size: 1 })
      const fbTotal = fbRes?.data?.total || fbRes?.total || 0
      if (fbTotal > 0) items.push({ icon: '📨', title: '反馈管理', desc: `共有 ${fbTotal} 条活动反馈`, time: '', role: 'school_admin', path: '/school/feedbacks' })
    }
  } catch { /* silent */ }

  notifications.value = items
  // Track new notifications vs cached
  const prevCount = parseInt(localStorage.getItem('notifLastCount') || '0')
  if (items.length > prevCount) {
    unreadCount.value = items.length - prevCount
  }
  localStorage.setItem('notifLastCount', String(items.length))
}

const filteredNotifications = computed(() => {
  const role = store.currentRole || ''
  return notifications.value.filter((n: any) => n.role === 'all' || roleLabelMap[n.role] === roleLabelMap[role] || n.role === role)
})

function markAllRead() {
  unreadCount.value = 0
  localStorage.setItem('notifLastCount', String(notifications.value.length))
}

const profilePath = computed(() => {
  let role = (store.currentRole || 'student').toLowerCase()
  return '/' + role.replace('_admin', '') + '/profile'
})

const activeMenu = computed(() => route.path)

const roleMenuMap: Record<string, { path: string; label: string; icon: any }[]> = {
  student: [
    { path: '/student/activities', label: '活动列表', icon: List },
    { path: '/student/enrollments', label: '已报名活动', icon: Checked },
    { path: '/student/my-feedback', label: '我的反馈', icon: EditPen },
    { path: '/student/profile', label: '个人主页', icon: User },
  ],
  teacher: [
    { path: '/teacher/activities', label: '活动列表', icon: List },
    { path: '/teacher/enrollments', label: '已报名活动', icon: Checked },
    { path: '/teacher/my-feedback', label: '我的反馈', icon: EditPen },
    { path: '/teacher/profile', label: '个人主页', icon: User },
  ],
  college_admin: [
    { path: '/college/activities', label: '活动列表', icon: List },
    { path: '/college/approvals', label: '报名审批', icon: Checked },
    { path: '/college/feedbacks', label: '反馈管理', icon: EditPen },
    { path: '/college/users', label: '用户管理', icon: Management },
    { path: '/college/profile', label: '个人主页', icon: User },
  ],
  school_admin: [
    { path: '/school/dashboard', label: '数据大屏', icon: DataAnalysis },
    { path: '/school/activities', label: '活动管理', icon: Management },
    { path: '/school/activities/create', label: '创建活动', icon: Plus },
    { path: '/school/approvals', label: '报名审批', icon: Checked },
    { path: '/school/feedbacks', label: '反馈管理', icon: EditPen },
    { path: '/school/users', label: '用户管理', icon: User },
    { path: '/school/profile', label: '个人主页', icon: User },
  ],
}

const menuItems = computed(() => {
  const role = (store.currentRole || '').toLowerCase()
  return roleMenuMap[role] || []
})

function handleLogout() {
  store.logout()
  router.push('/login')
}

onMounted(() => {
  fetchNotifications()
})
function handleNotifyClick(n: any) {
  unreadCount.value = Math.max(0, unreadCount.value - 1)
  localStorage.setItem('notifLastCount', String(notifications.value.length - unreadCount.value))
  if (n.path) router.push(n.path)
}
</script>

<style scoped>
.main-layout { height: 100vh; }

/* ========== Header ========== */
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #0a1628 0%, #1a3a6b 50%, #1a56db 100%);
  color: #fff;
  padding: 0 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.18);
  z-index: 10;
  position: relative;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}
.header-logo {
  font-size: 26px;
  line-height: 1;
}
.header-left h2 {
  font-size: 18px;
  margin: 0;
  font-weight: 600;
  letter-spacing: 0.5px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 18px;
}
.user-name {
  color: rgba(255, 255, 255, 0.9);
  font-size: 13px;
}

/* ========== Sidebar ========== */
.el-aside {
  background: #f8f9fb;
  border-right: 1px solid #e8eaed;
  display: flex;
  flex-direction: column;
}
.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 20px 16px;
  border-bottom: 1px solid #e8eaed;
}
.sidebar-logo {
  font-size: 28px;
  line-height: 1;
}
.sidebar-title {
  font-size: 20px;
  font-weight: 700;
  color: #1a3a6b;
  letter-spacing: 1px;
}
.sidebar-footer {
  margin-top: auto;
  padding: 14px 20px;
  font-size: 11px;
  color: #9ca3af;
  border-top: 1px solid #e8eaed;
  text-align: center;
}

.el-menu {
  border-right: none;
  flex: 1;
  background: transparent;
}
.el-menu .el-menu-item {
  margin: 2px 8px;
  border-radius: 8px;
  transition: all 0.2s ease;
}
.el-menu .el-menu-item:hover {
  background: #e8ecf4 !important;
  color: #1a56db;
}
.el-menu .el-menu-item.is-active {
  background: linear-gradient(135deg, #1a56db, #2563eb) !important;
  color: #fff !important;
  box-shadow: 0 2px 8px rgba(26, 86, 219, 0.25);
}

/* ========== Main Content ========== */
.el-main {
  background: #f5f7fa;
  background-image:
    radial-gradient(ellipse at 20% 50%, rgba(26, 86, 219, 0.03) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 20%, rgba(26, 86, 219, 0.02) 0%, transparent 50%);
  padding: 24px;
}

/* ========== Global overrides (unscoped) ========== */
</style>

<style>
/* Better card shadows and rounded corners for all el-card children in main */
.el-main .el-card {
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  transition: box-shadow 0.25s ease, transform 0.25s ease;
}
.el-main .el-card.is-always-shadow:hover,
.el-main .el-card.is-hover-shadow:hover {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

/* Table header styling */
.el-main .el-table th.el-table__cell {
  background: #f1f5f9;
  color: #374151;
  font-weight: 600;
}

/* Button consistent radius */
.el-main .el-button {
  border-radius: 8px;
}

/* Input / Select consistent radius */
.el-main .el-input__wrapper,
.el-main .el-select .el-input__wrapper {
  border-radius: 8px;
}
</style>
