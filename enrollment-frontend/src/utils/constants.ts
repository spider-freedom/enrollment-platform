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

// 判断能否报名: 活动已发布或进行中
export function canEnroll(status: string): boolean {
  return status === 'PUBLISHED' || status === 'ONGOING'
}

// 活动类型筛选选项（学生端只用 线上/线下，不用中文假类型）
export const ACTIVITY_TYPE_FILTERS = [
  { label: '全部', value: '' },
  { label: '线上活动', value: 'ONLINE' },
  { label: '线下活动', value: 'OFFLINE' },
]
