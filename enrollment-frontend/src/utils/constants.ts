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
