import request from './request'
import type { Activity, Enrollment, Feedback, PageResult } from '@/types'

// Auth
export const authApi = {
  login: (username: string, password: string) =>
    request.post('/auth/login', { username, password }),
  register: (data: any) => request.post('/auth/register', data),
}

// User
export const userApi = {
  getProfile: () => request.get('/user/profile'),
  updateProfile: (data: any) => request.put('/user/profile', data),
  changePassword: (data: any) => request.put('/user/password', data),
  uploadAvatar: (file: File) => {
    const fd = new FormData()
    fd.append('file', file)
    return request.post('/user/avatar', fd, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}

// Activity
export const activityApi = {
  listStudent: (params: any) => request.get('/activity/list/student', { params }),
  listTeacher: (params: any) => request.get('/activity/list/teacher', { params }),
  listCollege: (params: any) => request.get('/activity/list/college', { params }),
  listSchool: (params: any) => request.get('/activity/list/school', { params }),
  getById: (id: number) => request.get(`/activity/${id}`),
  getBanners: () => request.get('/activity/banners'),
  toggleBanner: (id: number, isBanner: number) => request.post(`/activity/${id}/banner`, { isBanner }),
  create: (data: any) => request.post('/activity/create', data),
  update: (id: number, data: any) => request.put(`/activity/update/${id}`, data),
  delete: (id: number) => request.delete(`/activity/delete/${id}`),
}

// Enrollment
export const enrollmentApi = {
  submit: (data: any) => request.post('/enrollment/submit', data),
  listMy: (params: any) => request.get('/enrollment/my', { params }),
  listCollege: (params: any) => request.get('/enrollment/college', { params }),
  listSchool: (params: any) => request.get('/enrollment/school', { params }),
  getById: (id: number) => request.get(`/enrollment/${id}`),
  withdraw: (id: number) => request.post(`/enrollment/${id}/withdraw`),
}

// Approval
export const approvalApi = {
  listCollege: (params: any) => request.get('/approval/college', { params }),
  listSchool: (params: any) => request.get('/approval/school', { params }),
  approve: (data: any) => request.post('/approval/approve', data),
  reject: (data: any) => request.post('/approval/reject', data),
  batchApprove: (data: any) => request.post('/approval/batch', data),
}

// Feedback
export const feedbackApi = {
  submitStudent: (data: any) => request.post('/feedback/student', data),
  submitTeacher: (data: any) => request.post('/feedback/teacher', data),
  listCollege: (params: any) => request.get('/feedback/college', { params }),
  listSchool: (params: any) => request.get('/feedback/school', { params }),
  getDetail: (id: number) => request.get(`/feedback/${id}`),
  reply: (id: number, data: any) => request.post(`/feedback/${id}/reply`, data),
  listMy: (params?: any) => request.get('/feedback/my', { params }),
}

// Statistics
export const statisticsApi = {
  getDashboard: () => request.get('/statistics/dashboard'),
  getTrend: () => request.get('/statistics/trend'),
  getCollegeDist: () => request.get('/statistics/college'),
  getRatingDist: () => request.get('/statistics/rating'),
}

// College User
export const collegeUserApi = {
  listUsers: () => request.get('/college/users/list'),
  listAdmins: () => request.get('/college/users/admins'),
  promote: (userId: number) => request.post(`/college/users/${userId}/promote`),
  demote: (userId: number) => request.post(`/college/users/${userId}/demote`),
  deleteUser: (userId: number) => request.delete(`/college/users/${userId}`),
  resetPassword: (id: number) => request.post(`/college/users/${id}/reset-password`),
  toggleStatus: (id: number, status: string) => request.post(`/college/users/${id}/status`, { status }),
  importUsers: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/college/users/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}

// Policy
export const policyApi = {
  list: () => request.get('/ai/policy/list'),
  getById: (id: number) => request.get(`/ai/policy/${id}`),
  create: (data: any) => request.post('/policy/create', data),
  update: (id: number, data: any) => request.put(`/policy/update/${id}`, data),
  delete: (id: number) => request.delete(`/policy/delete/${id}`),
  upload: (file: File) => {
    const fd = new FormData(); fd.append('file', file)
    return request.post('/policy/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
  },
}

// AI
export const aiApi = {
  suggestSchool: (keyword: string) => request.get('/ai/suggest-school', { params: { keyword } }),
  normalizeSchool: (name: string) => request.get('/ai/normalize-school', { params: { name } }),
  analyzeFeedback: (content: string) => request.post('/ai/analyze-feedback', { content }),
  approvalSuggest: (data: any) => request.post('/ai/approval/suggest', data),
}

// School User
export const schoolUserApi = {
  listAll: (params: any) => request.get('/school/users/list', { params }),
  promoteToCollegeAdmin: (id: number) => request.post(`/school/users/${id}/promote-college`),
  promoteToSchoolAdmin: (id: number) => request.post(`/school/users/${id}/promote-school`),
  demoteToTeacher: (id: number) => request.post(`/school/users/${id}/demote`),
  toggleStatus: (id: number, status: string) => request.post(`/school/users/${id}/status`, { status }),
  resetPassword: (id: number) => request.post(`/school/users/${id}/reset-password`),
  deleteUser: (id: number) => request.delete(`/school/users/${id}`),
}
