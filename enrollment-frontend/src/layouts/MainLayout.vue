<template>
  <div style="display:flex;height:100vh;background:#f8f9fa;font-family:'PingFang SC','Microsoft YaHei',sans-serif;overflow:hidden">
    <!-- Sidebar -->
    <aside :style="{width:'240px',background:'#fff',borderRight:'1px solid #e2e8f0',display:'flex',flexDirection:'column',flexShrink:'0'}">
      <div style="padding:20px 20px 16px;border-bottom:1px solid #f1f5f9;display:flex;align-items:center;gap:10px">
        <img src="/images/xju-logo.png" style="width:40px;height:auto;flex-shrink:0" alt="新疆大学" />
        <div>
          <div style="font-weight:700;font-size:15px;color:#1e293b">{{ $t("新疆大学") }}</div>
          <div style="font-size:11px;color:#94a3b8">{{ $t("招生宣传平台") }}</div>
        </div>
      </div>

      <nav style="flex:1;padding:12px;overflow-y:auto">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
          :style="navItemStyle(item.path)"
        >
          <el-icon :size="18"><component :is="item.icon" /></el-icon>
          <span style="font-weight:500;font-size:14px">{{ item.label }}</span>
        </router-link>
      </nav>

      <div style="padding:12px;border-top:1px solid #f1f5f9">
        <div style="display:flex;align-items:center;gap:10px;padding:8px;margin-bottom:8px">
          <el-avatar :size="36" :src="store.userInfo?.avatar||''" icon="UserFilled" />
          <div style="flex:1;overflow:hidden">
            <div style="font-size:13px;font-weight:600;color:#334155;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ store.userInfo?.name }}</div>
            <div style="font-size:11px;color:#94a3b8">{{ roleLabel }}</div>
          </div>
        </div>
        <el-button style="width:100%;color:#64748b;border-color:#e2e8f0" size="small" @click="handleLogout">
          <el-icon :size="14"><SwitchButton /></el-icon>
          <span style="margin-left:4px">{{ $t("退出登录") }}</span>
        </el-button>
      </div>
    </aside>

    <!-- Main Area -->
    <div style="flex:1;display:flex;flex-direction:column;min-width:0">
      <header :style="{height:'56px',background:'#A31F34',color:'#fff',display:'flex',alignItems:'center',justifyContent:'space-between',padding:'0 24px',flexShrink:'0'}">
        <div style="display:flex;align-items:center;gap:16px">
          <span style="font-weight:700;font-size:16px">{{ pageTitle }}</span>
        </div>
        <div style="display:flex;align-items:center;gap:20px">
          <div class="header-date">{{ currentDate }}</div>
          <div class="notify-wrapper">
            <el-popover placement="bottom-end" :width="340" trigger="click">
              <template #reference>
                <el-badge :value="unreadCount" :hidden="unreadCount===0">
                  <el-icon :size="18" color="#fff" style="cursor:pointer"><Bell /></el-icon>
                </el-badge>
              </template>
              <div style="font-weight:600;padding:4px 0 12px;border-bottom:1px solid #eee;display:flex;justify-content:space-between;font-size:14px">
                {{ $t("消息通知") }} <span style="font-size:12px;color:#999;cursor:pointer" @click="markAllRead()">{{ $t("全部已读") }}</span>
              </div>
              <div style="max-height:260px;overflow-y:auto">
                <div v-for="(n,i) in filteredNotifications" :key="i" style="display:flex;gap:8px;padding:10px 0;border-bottom:1px solid #f5f5f5;cursor:pointer" @click="handleNotifyClick(n)">
                  <span style="font-size:16px;flex-shrink:0">{{ n.icon }}</span>
                  <div>
                    <div style="font-size:13px;font-weight:500">{{ n.title }}</div>
                    <div style="font-size:12px;color:#888">{{ n.desc }}</div>
                    <div style="font-size:11px;color:#bbb">{{ n.time }}</div>
                  </div>
                </div>
              </div>
              <div v-if="filteredNotifications.length===0" style="text-align:center;color:#bbb;padding:20px 0">{{ $t("暂无消息") }}</div>
            </el-popover>
          </div>
        </div>
      </header>

      <main style="flex:1;overflow-y:auto;padding:20px 24px;background:linear-gradient(180deg,#f8fafc 0%,#f0f4f8 100%)">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Bell, List, Checked, EditPen, User, DataAnalysis, Plus, Management, UserFilled, SwitchButton } from '@element-plus/icons-vue'
import { activityApi, enrollmentApi, feedbackApi, approvalApi } from '@/api'

const store = useUserStore()
const router = useRouter()
const route = useRoute()
const unreadCount = ref(0)

const roleLabelMap: Record<string, string> = {
  STUDENT:'学生', TEACHER:'教师', COLLEGE_ADMIN:'学院管理员', SCHOOL_ADMIN:'学校管理员',
  student:'学生', teacher:'教师', college_admin:'学院管理员', school_admin:'学校管理员',
}
const roleTagColors: Record<string, string> = {
  SCHOOL_ADMIN:'#A31F34', COLLEGE_ADMIN:'#2E7FB9', TEACHER:'#2E7FB9', STUDENT:'#C9A96E',
}
const roleLabel = computed(() => roleLabelMap[store.currentRole] || store.currentRole || '未知')

const activeColor = computed(() => {
  const r = (store.currentRole||'').toLowerCase()
  if (r==='school_admin') return '#A31F34'
  if (r==='college_admin'||r==='teacher') return '#2E7FB9'
  if (r==='student') return '#C9A96E'
  return '#A31F34'
})

const currentDate = computed(() => {
  const d = new Date()
  const w = ['日','一','二','三','四','五','六']
  return `${d.getFullYear()}年${d.getMonth()+1}月${d.getDate()}日 星期${w[d.getDay()]}`
})

const pageTitle = computed(() => {
  const p = route.path
  if (p.includes('/dashboard')) return '数据大屏'
  if (p.includes('/activities/create')) return '创建活动'
  if (p.includes('/activities')) return '活动管理'
  if (p.includes('/approvals')) return '报名审批'
  if (p.includes('/feedbacks')) return '反馈管理'
  if (p.includes('/users')) return '用户管理'
  if (p.includes('/enrollments')) return '已报名活动'
  if (p.includes('/my-feedback')) return '我的反馈'
  if (p.includes('/profile')) return '个人主页'
  return '新疆大学招生宣传平台'
})

const profilePath = computed(() => {
  const r = (store.currentRole||'student').toLowerCase()
  return '/' + r.replace('_admin','') + '/profile'
})

function isActive(path: string) {
  if (path === route.path) return true
  return route.path.startsWith(path + '/')
}

function navItemStyle(path: string) {
  if (isActive(path)) {
    return { background: activeColor.value, color: '#fff' }
  }
  return { color: '#475569' }
}

const roleMenuMap: Record<string, { path: string; label: string; icon: any }[]> = {
  student: [
    { path:'/student/activities', label:'活动列表', icon:List },
    { path:'/student/enrollments', label:'已报名活动', icon:Checked },
    { path:'/student/my-feedback', label:'我的反馈', icon:EditPen },
    { path:'/student/profile', label:'个人主页', icon:User },
  ],
  teacher: [
    { path:'/teacher/activities', label:'活动列表', icon:List },
    { path:'/teacher/enrollments', label:'已报名活动', icon:Checked },
    { path:'/teacher/my-feedback', label:'我的反馈', icon:EditPen },
    { path:'/teacher/profile', label:'个人主页', icon:User },
  ],
  college_admin: [
    { path:'/college/activities', label:'活动管理', icon:Management },
    { path:'/college/approvals', label:'报名审批', icon:Checked },
    { path:'/college/feedbacks', label:'反馈管理', icon:EditPen },
    { path:'/college/users', label:'用户管理', icon:User },
    { path:'/college/profile', label:'个人主页', icon:User },
  ],
  school_admin: [
    { path:'/school/dashboard', label:'数据大屏', icon:DataAnalysis },
    { path:'/school/activities', label:'活动管理', icon:Management },
    { path:'/school/approvals', label:'报名审批', icon:Checked },
    { path:'/school/feedbacks', label:'反馈管理', icon:EditPen },
    { path:'/school/users', label:'用户管理', icon:User },
    { path:'/school/profile', label:'个人主页', icon:User },
  ],
}

const menuItems = computed(() => {
  return roleMenuMap[(store.currentRole||'').toLowerCase()] || []
})

// -- Notifications --
const notifications = ref<any[]>([])

async function fetchNotifications() {
  const role = (store.currentRole||'').toLowerCase()
  const items: any[] = []
  try {
    if (role==='student'||role==='teacher') {
      const api = role==='teacher' ? activityApi.listTeacher : activityApi.listStudent
      const res: any = await api({ page:1, size:3 })
      const recs = res?.data?.list || res?.data?.records || res?.records || []
      for (const a of recs) items.push({ icon:'📢', title:'新活动发布', desc:`「${a.title}」已开放报名`, time:a.createTime?.substring(0,10)||'', path:`/${role}/activities/${a.id}` })
      const eRes: any = await enrollmentApi.listMy({ page:1, size:5 })
      const es = eRes?.data?.list || eRes?.data?.records || eRes?.records || []
      for (const e of es) {
        if (e.status==='APPROVED') items.push({ icon:'✅', title:'报名已通过', desc:'活动报名已通过审核', time:e.approvedAt?.substring(0,10)||'', path:`/${role}/enrollments` })
        else if (e.status==='REJECTED') items.push({ icon:'❌', title:'报名被驳回', desc:e.rejectReason?`原因:${e.rejectReason}`:'报名未通过审核', time:e.updateTime?.substring(0,10)||'', path:`/${role}/enrollments` })
      }
      const fRes: any = await feedbackApi.listMy({ page:1, size:5 })
      const fs = fRes?.data?.list || fRes?.data?.records || fRes?.records || []
      for (const f of fs) {
        if (f.status==='REPLIED') items.push({ icon:'📨', title:'反馈收到回复', desc:'管理员回复了您的活动反馈', time:f.replyTime?.substring(0,10)||'', path:`/${role}/my-feedback` })
      }
    }
    if (role==='college_admin') {
      const aRes: any = await approvalApi.listCollege({ page:1, size:1 })
      const pending = aRes?.data?.total || aRes?.total || 0
      if (pending>0) items.push({ icon:'📋', title:'有待审批报名', desc:`有 ${pending} 条报名申请等待您审批`, path:'/college/approvals', role:'college_admin' })
      const fRes: any = await feedbackApi.listCollege({ page:1, size:1 })
      const ft = fRes?.data?.total || fRes?.total || 0
      if (ft>0) items.push({ icon:'📨', title:'反馈管理', desc:`共有 ${ft} 条活动反馈`, path:'/college/feedbacks', role:'college_admin' })
    }
    if (role==='school_admin') {
      const aRes: any = await approvalApi.listSchool({ page:1, size:1 })
      const pending = aRes?.data?.total || aRes?.total || 0
      if (pending>0) items.push({ icon:'📋', title:'有待审批报名', desc:`有 ${pending} 条报名等待学校审批`, path:'/school/approvals', role:'school_admin' })
      const fRes: any = await feedbackApi.listSchool({ page:1, size:1 })
      const ft = fRes?.data?.total || fRes?.total || 0
      if (ft>0) items.push({ icon:'📨', title:'反馈管理', desc:`共有 ${ft} 条活动反馈`, path:'/school/feedbacks', role:'school_admin' })
    }
  } catch {}
  notifications.value = items
  const prev = parseInt(localStorage.getItem('notifLastCount')||'0')
  if (items.length>prev) unreadCount.value = items.length-prev
  localStorage.setItem('notifLastCount', String(items.length))
}
const filteredNotifications = computed(() => {
  const r = (store.currentRole || '').toLowerCase()
  return notifications.value.filter((n:any) => !n.role || (n.role||'').toLowerCase()===r)
})
function markAllRead() { unreadCount.value=0; localStorage.setItem('notifLastCount',String(notifications.value.length)) }

function handleLogout() { store.logout(); router.push('/login') }
function handleNotifyClick(n:any) {
  unreadCount.value = Math.max(0, unreadCount.value-1)
  localStorage.setItem('notifLastCount', String(notifications.value.length-unreadCount.value))
  if (n.path) router.push(n.path)
}

onMounted(() => { fetchNotifications() })
</script>

<style>
/* Global styles for navbar items */
.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 10px;
  margin-bottom: 2px;
  text-decoration: none;
  transition: all 0.2s ease;
}
.nav-item:hover:not(.active) {
  background: #f1f5f9;
}
.nav-item.active {
  box-shadow: 0 2px 8px rgba(0,0,0,0.12);
}

/* Header date */
.header-date {
  font-size: 13px;
  opacity: 0.85;
}

/* Notify wrapper */
.notify-wrapper {
  display: flex;
  align-items: center;
}

/* Main content cards */
.el-main .el-card {
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 2px rgba(0,0,0,0.04);
}
.el-main .el-table th.el-table__cell {
  background: #f8fafc;
  color: #475569;
  font-weight: 600;
}
.el-main .el-button {
  border-radius: 8px;
}
.el-main .el-input__wrapper,
.el-main .el-select .el-input__wrapper {
  border-radius: 8px;
}

/* Page title in views */
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 20px;
}
</style>
