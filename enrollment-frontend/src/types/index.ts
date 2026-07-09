export interface UserInfo {
  id: number
  username: string
  name: string
  role: 'student' | 'teacher' | 'college_admin' | 'school_admin'
  collegeId?: number
  collegeName?: string
  major?: string
  grade?: string
  gpa?: number
  email?: string
  phone?: string
}

export interface Activity {
  id: number
  title: string
  description: string
  type: string
  status: string
  targetAudience: string
  startTime: string
  endTime: string
  enrollStart: string
  enrollEnd: string
  location: string
  maxStudents: number
  maxTeachers: number
  maxPerSchool: number
  bannerUrl: string
  currentStudents: number
  currentTeachers: number
  attachments?: { name: string; url: string }[]
}

export interface Enrollment {
  id: number
  activityId: number
  activityTitle: string
  userId: number
  userName: string
  userRole: string
  status: string
  targetSchool: string
  groupName: string
  rankInGroup: number
  rejectReason: string
  collegeName: string
  submittedAt: string
}

export interface Feedback {
  feedbackId: number
  userId: number
  userName: string
  userRole: string
  college: string
  content: string
  rating: number
  status: string
  reply: string
  replyUserName: string
  replyTime: string
  attachmentCount: number
  createTime: string
  activityTitle?: string
}

export interface Notification {
  id: number
  title: string
  content: string
  type: string
  isRead: boolean
  createTime: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}
