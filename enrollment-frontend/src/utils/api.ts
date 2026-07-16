// 统一 API 响应解析：兼容 data.list / data.records / Array 三种格式
export function parseListResponse(res: any): { list: any[]; total: number } {
  if (!res) return { list: [], total: 0 }
  const data = res.data || res
  const list = data?.list || data?.records || (Array.isArray(data) ? data : [])
  const total = data?.total || list.length
  return { list, total }
}

// 统一 API 响应解析：单个对象 data 字段
export function parseObjectResponse(res: any): any {
  if (!res) return null
  return res?.data || (typeof res === 'object' && !Array.isArray(res) ? res : null)
}
