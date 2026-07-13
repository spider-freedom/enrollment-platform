// 活动类型映射
export const ACTIVITY_TYPE_MAP: Record<string, string> = {
  ONLINE: '线上活动',
  OFFLINE: '线下活动',
}

// 活动状态映射
export const ACTIVITY_STATUS_MAP: Record<string, string> = {
  DRAFT: '草稿',
  PUBLISHED: '报名中',
  ONGOING: '进行中',
  ENDED: '已结束',
}

// 报名状态映射
export const ENROLL_STATUS_MAP: Record<string, string> = {
  SUBMITTED: '已提交',
  APPROVING: '审批中',
  APPROVED: '已通过',
  REJECTED: '已驳回',
  WITHDRAWN: '已撤回',
}

// 角色中文映射
export const ROLE_LABEL_MAP: Record<string, string> = {
  STUDENT: '学生',
  TEACHER: '教师',
  COLLEGE_ADMIN: '学院管理员',
  SCHOOL_ADMIN: '学校管理员',
  student: '学生',
  teacher: '教师',
  college_admin: '学院管理员',
  school_admin: '学校管理员',
}

// 目标受众映射
export const TARGET_AUDIENCE_MAP: Record<number, string> = {
  1: '仅学生',
  2: '仅教师',
  3: '学生+教师',
}

// 元素 Plus Tag 类型映射
export function activityTypeTagType(type: string): string {
  return type === 'ONLINE' ? 'success' : 'primary'
}

export function activityStatusTagType(status: string): string {
  const map: Record<string, string> = {
    PUBLISHED: 'success',
    ONGOING: 'warning',
    DRAFT: 'info',
    ENDED: 'info',
  }
  return map[status] || 'info'
}

export function enrollStatusTagType(status: string): string {
  const map: Record<string, string> = {
    SUBMITTED: 'info',
    APPROVING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    WITHDRAWN: 'info',
  }
  return map[status] || 'info'
}

// 根据日期计算活动显示状态
export function getDisplayStatus(activity: any): string {
  if (!activity) return '未知'
  const status = activity.status || ''
  if (status === 'DRAFT') return '草稿'
  const now = new Date()
  const endTime = activity.endTime ? new Date(activity.endTime) : null
  const startTime = activity.startTime ? new Date(activity.startTime) : null
  const enrollStart = activity.enrollStart ? new Date(activity.enrollStart) : null
  const enrollEnd = activity.enrollEnd ? new Date(activity.enrollEnd) : null

  if (endTime && now > endTime) return '已结束'
  if (status === 'ENDED') return '已结束'
  if (startTime && now >= startTime && endTime && now <= endTime) return '进行中'
  if (enrollStart && now < enrollStart) return '未开始'
  if (enrollEnd && now > enrollEnd) return '报名已截止'
  if (enrollStart && enrollEnd && now >= enrollStart && now <= enrollEnd) return '报名中'
  return ACTIVITY_STATUS_MAP[status] || status
}

// 判断能否报名: 基于日期而非静态状态
export function canEnroll(activity: any): boolean {
  if (!activity) return false
  const status = activity.status || ''
  if (status === 'DRAFT' || status === 'ENDED') return false
  const now = new Date()
  const enrollStart = activity.enrollStart ? new Date(activity.enrollStart) : null
  const enrollEnd = activity.enrollEnd ? new Date(activity.enrollEnd) : null
  if (enrollStart && now < enrollStart) return false
  if (enrollEnd && now > enrollEnd) return false
  const maxStudents = activity.maxStudents || 0
  const currentStudents = activity.currentStudents || 0
  if (maxStudents > 0 && currentStudents >= maxStudents) return false
  return true
}

export function getDisplayStatusTagType(status: string): string {
  const map: Record<string, string> = {
    '报名中': 'success', '进行中': 'warning', '未开始': 'info',
    '报名已截止': 'danger', '已结束': 'info', '草稿': 'info',
  }
  return map[status] || 'info'
}

// 活动分类映射
export const ACTIVITY_CATEGORY_MAP: Record<string, string> = {
  '宣讲会': '宣讲会',
  '开放日': '开放日',
  '夏令营': '夏令营',
  '咨询会': '咨询会',
  '回访母校': '回访母校',
  '线上直播': '线上直播',
  '走访宣讲': '走访宣讲',
  '招生宣传': '招生宣传',
  '其他': '其他',
}

// 活动分类筛选选项
export const ACTIVITY_CATEGORY_FILTERS = [
  { label: '全部分类', value: '' },
  { label: '宣讲会', value: '宣讲会' },
  { label: '开放日', value: '开放日' },
  { label: '夏令营', value: '夏令营' },
  { label: '咨询会', value: '咨询会' },
  { label: '回访母校', value: '回访母校' },
  { label: '线上直播', value: '线上直播' },
  { label: '走访宣讲', value: '走访宣讲' },
  { label: '招生宣传', value: '招生宣传' },
  { label: '其他', value: '其他' },
]

// 活动分类创建选项
export const ACTIVITY_CATEGORY_OPTIONS = [
  '宣讲会', '开放日', '夏令营', '咨询会', '回访母校', '线上直播', '走访宣讲', '招生宣传', '其他',
]
