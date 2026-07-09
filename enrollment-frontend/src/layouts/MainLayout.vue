<template>
  <el-container class="main-layout">
    <el-header class="header">
      <div class="header-left">
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
            消息通知 <span style="font-size:11px;color:#999;cursor:pointer;font-weight:400" @click="unreadCount=0">全部已读</span>
          </div>
          <div style="max-height:300px;overflow-y:auto">
            <div v-for="(n,i) in notifications" :key="i" style="padding:10px 0;border-bottom:1px solid #f5f5f5;cursor:pointer" @click="handleNotifyClick(n)">
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
        <el-menu :default-active="activeMenu" router :ellipsis="false">
          <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </el-menu-item>
        </el-menu>
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
import { Bell, ArrowDown, List, Checked, EditPen, User, DataAnalysis, Plus, Management, Clock } from '@element-plus/icons-vue'

const store = useUserStore()
const router = useRouter()
const route = useRoute()
const unreadCount = ref(0)

const roleLabelMap: Record<string, string> = {
  STUDENT: '学生', TEACHER: '教师', COLLEGE_ADMIN: '学院管理员', SCHOOL_ADMIN: '学校管理员'
}
const roleTagTypeMap: Record<string, string> = {
  STUDENT: 'info', TEACHER: 'success', COLLEGE_ADMIN: 'warning', SCHOOL_ADMIN: 'danger'
}

const roleLabel = computed(() => roleLabelMap[store.currentRole] || store.currentRole || '未知')
const roleTagType = computed(() => roleTagTypeMap[store.currentRole] || 'info')

const notifications = ref([
  { icon:'✅', title:'报名审批通过', desc:'「寒假招生宣传活动」已通过学院审核，进入学校审核', time:'2小时前', path:'/student/enrollments' },
  { icon:'📨', title:'反馈收到回复', desc:'管理员回复了您对「寒假招生宣传活动」的反馈', time:'1天前', path:'/student/my-feedback' },
  { icon:'📢', title:'新活动发布', desc:'招生办发布了「线上招生政策宣讲与答疑」', time:'3天前', path:'/student/activities' },
])

function handleNotifyClick(n: any) {
  unreadCount.value = Math.max(0, unreadCount.value - 1)
  if (n.path) router.push(n.path)
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
    { path: '/school/audit', label: '操作日志', icon: Clock },
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

onMounted(() => { unreadCount.value = 3 })
</script>

<style scoped>
.main-layout { height: 100vh; }
.header { display: flex; align-items: center; justify-content: space-between; background: #1a3a5c; color: #fff; padding: 0 24px; }
.header-left h2 { font-size: 18px; margin: 0; }
.header-right { display: flex; align-items: center; gap: 16px; }
.user-name { color: rgba(255,255,255,.85); font-size: 13px; }
.el-aside { background: #f5f7fa; border-right: 1px solid #e4e7ed; }
.el-menu { border-right: none; height: 100%; }
.el-main { background: #f0f2f5; padding: 20px; }
</style>
