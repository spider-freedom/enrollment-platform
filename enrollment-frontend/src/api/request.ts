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
  (response) => {
    const res = response.data
    // 后端业务异常（如"您已提交过反馈"、"报名已截止"）以 HTTP 200 返回 {code, message}
    // 必须在此检查 code，否则失败会被页面当作成功处理
    if (res && typeof res === 'object' && typeof res.code === 'number' && res.code !== 200) {
      if (res.code === 401 && localStorage.getItem('token')) {
        localStorage.removeItem('token')
        window.location.href = '/login'
      }
      const err: any = new Error(res.message || '请求失败')
      err.code = res.code
      err.response = { status: res.code, data: res }
      return Promise.reject(err)
    }
    return res
  },
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
