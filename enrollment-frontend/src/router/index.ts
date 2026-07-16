import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('@/layouts/PublicLayout.vue'),
      children: [
        { path: '', name: 'Home', component: () => import('@/views/HomeView.vue') },
        { path: 'about', name: 'About', component: () => import('@/views/AboutView.vue') },
        { path: 'policy', name: 'Policy', component: () => import('@/views/PolicyView.vue') },
        { path: 'majors', name: 'Majors', component: () => import('@/views/MajorsView.vue') },
        { path: 'activities', name: 'Activities', component: () => import('@/views/StudentActivityList.vue') },
        { path: 'ai-assistant', name: 'AIAssistant', component: () => import('@/views/AIAssistantView.vue') },
        { path: 'login', name: 'Login', component: () => import('@/views/LoginView.vue') },
        { path: 'register', name: 'Register', component: () => import('@/views/RegisterView.vue') },
      ],
    },
    {
      path: '/student',
      component: () => import('@/layouts/MainLayout.vue'),
      meta: { role: 'student' },
      children: [
        { path: '', name: 'StudentDashboard', component: () => import('@/views/StudentDashboard.vue') },
        { path: 'activities', name: 'StudentActivities', component: () => import('@/views/StudentActivityList.vue') },
        { path: 'activities/:id', name: 'StudentActivityDetail', component: () => import('@/views/StudentActivityDetail.vue') },
        { path: 'enrollments', name: 'StudentEnrollments', component: () => import('@/views/StudentEnrollments.vue') },
        { path: 'feedback/:enrollmentId', name: 'StudentFeedbackSubmit', component: () => import('@/views/StudentFeedbackSubmit.vue') },
        { path: 'my-feedback', name: 'StudentMyFeedback', component: () => import('@/views/StudentMyFeedback.vue') },
        { path: 'profile', name: 'StudentProfile', component: () => import('@/views/ProfileView.vue') },
      ],
    },
    {
      path: '/teacher',
      component: () => import('@/layouts/MainLayout.vue'),
      meta: { role: 'teacher' },
      children: [
        { path: 'activities', name: 'TeacherActivities', component: () => import('@/views/TeacherActivityList.vue') },
        { path: 'activities/:id', name: 'TeacherActivityDetail', component: () => import('@/views/TeacherActivityDetail.vue') },
        { path: 'enrollments', name: 'TeacherEnrollments', component: () => import('@/views/TeacherEnrollments.vue') },
        { path: 'feedback/:enrollmentId', name: 'TeacherFeedbackSubmit', component: () => import('@/views/TeacherFeedbackSubmit.vue') },
        { path: 'my-feedback', name: 'TeacherMyFeedback', component: () => import('@/views/TeacherMyFeedback.vue') },
        { path: 'profile', name: 'TeacherProfile', component: () => import('@/views/ProfileView.vue') },
      ],
    },
    {
      path: '/college',
      component: () => import('@/layouts/MainLayout.vue'),
      meta: { role: 'college_admin' },
      children: [
        { path: 'activities', name: 'CollegeActivities', component: () => import('@/views/CollegeActivityList.vue') },
        { path: 'activities/create', name: 'CollegeActivityCreate', component: () => import('@/views/SchoolActivityCreate.vue') },
        { path: 'activities/:id', name: 'CollegeActivityDetail', component: () => import('@/views/SchoolActivityCreate.vue') },
        { path: 'approvals', name: 'CollegeApprovals', component: () => import('@/views/CollegeApprovalList.vue') },
        { path: 'feedbacks', name: 'CollegeFeedbacks', component: () => import('@/views/CollegeFeedbackList.vue') },
        { path: 'users', name: 'CollegeUsers', component: () => import('@/views/CollegeUserManagement.vue') },
        { path: 'profile', name: 'CollegeProfile', component: () => import('@/views/ProfileView.vue') },
      ],
    },
    {
      path: '/school',
      component: () => import('@/layouts/MainLayout.vue'),
      meta: { role: 'school_admin' },
      children: [
        { path: 'dashboard', name: 'SchoolDashboard', component: () => import('@/views/SchoolDashboard.vue') },
        { path: 'activities', name: 'SchoolActivities', component: () => import('@/views/SchoolActivityList.vue') },
        { path: 'activities/:id', name: 'SchoolActivityDetail', component: () => import('@/views/SchoolActivityCreate.vue') },
        { path: 'activities/create', name: 'SchoolActivityCreate', component: () => import('@/views/SchoolActivityCreate.vue') },
        { path: 'approvals', name: 'SchoolApprovals', component: () => import('@/views/SchoolApprovalList.vue') },
        { path: 'feedbacks', name: 'SchoolFeedbacks', component: () => import('@/views/SchoolFeedbackList.vue') },
        { path: 'users', name: 'SchoolUsers', component: () => import('@/views/SchoolUserManagement.vue') },
        { path: 'profile', name: 'SchoolProfile', component: () => import('@/views/ProfileView.vue') },
      ],
    },
  ],
})

router.beforeEach(async (to, _from, next) => {
  const store = useUserStore()
  if (to.path === '/login' || to.path === '/register') { next(); return }
  // Public routes don't require auth
  const publicPaths = ['/', '/about', '/policy', '/majors', '/activities', '/ai-assistant']
  if (publicPaths.includes(to.path)) { next(); return }
  if (!store.isLoggedIn) { next('/login'); return }
  if (!store.userInfo) {
    try { await store.fetchProfile() } catch { store.logout(); next('/login'); return }
  }
  const roleRouteMap: Record<string, string> = {
    student:'/student', teacher:'/teacher', college_admin:'/college', school_admin:'/school',
  }
  const allowedPrefix = roleRouteMap[store.currentRole]
  if (allowedPrefix && !to.path.startsWith(allowedPrefix)) {
    next(allowedPrefix + '/activities'); return
  }
  next()
})

export default router
