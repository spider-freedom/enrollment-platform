import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi, userApi } from '@/api'
import type { UserInfo } from '@/types'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value)
  const currentRole = computed(() => (userInfo.value?.role || '').toLowerCase())
  const isStudent = computed(() => currentRole.value === 'student')
  const isTeacher = computed(() => currentRole.value === 'teacher')
  const isCollegeAdmin = computed(() => currentRole.value === 'college_admin')
  const isSchoolAdmin = computed(() => currentRole.value === 'school_admin')

  async function login(username: string, password: string) {
    const res: any = await authApi.login(username, password)
    if (!res || res.code !== 200) {
      throw new Error(res?.message || '登录失败')
    }
    token.value = res.data.token
    localStorage.setItem('token', res.data.token)
    await fetchProfile()
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  async function fetchProfile() {
    const res: any = await userApi.getProfile()
    userInfo.value = res.data
  }

  function hasRole(role: string): boolean {
    return currentRole.value === role
  }

  return { token, userInfo, isLoggedIn, currentRole, isStudent, isTeacher, isCollegeAdmin, isSchoolAdmin, login, logout, fetchProfile, hasRole }
})
