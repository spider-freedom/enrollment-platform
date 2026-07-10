import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json; charset=utf-8' },
})

// Authenticated file download (blob, no response unwrapping)
export async function downloadFile(url: string, filename?: string) {
  const token = localStorage.getItem('token')
  const resp = await axios.get(url, {
    baseURL: '/api',
    responseType: 'blob',
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })
  const blob = new Blob([resp.data])
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = filename || url.split('/').pop() || 'export.xlsx'
  link.click()
  URL.revokeObjectURL(link.href)
  ElMessage.success('导出成功')
}

request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) config.headers.Authorization = `Bearer ${token}`
    return config
  },
  (error) => Promise.reject(error)
)

request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    // Don't show message here — let each page handle its own errors
    return Promise.reject(error)
  }
)

export default request
