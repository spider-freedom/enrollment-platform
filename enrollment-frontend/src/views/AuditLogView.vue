<template>
  <div class="audit-log-view">
    <h2 class="page-title">操作日志</h2>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-select
        v-model="filterAction"
        placeholder="操作类型"
        clearable
        style="width: 160px"
        @change="handleSearch"
      >
        <el-option
          v-for="a in actionOptions"
          :key="a.value"
          :label="a.label"
          :value="a.value"
        />
      </el-select>
      <el-select
        v-model="filterModule"
        placeholder="所属模块"
        clearable
        style="width: 160px; margin-left: 12px"
        @change="handleSearch"
      >
        <el-option
          v-for="m in moduleOptions"
          :key="m.value"
          :label="m.label"
          :value="m.value"
        />
      </el-select>
      <el-button type="primary" style="margin-left: 12px" @click="handleSearch">搜索</el-button>
      <el-button style="margin-left: 8px" @click="handleReset">重置</el-button>
    </div>

    <!-- 错误提示 -->
    <el-alert
      v-if="errorMsg"
      :title="errorMsg"
      type="error"
      show-icon
      closable
      style="margin-bottom: 16px"
      @close="errorMsg = ''"
    />

    <!-- 表格 -->
    <el-table
      :data="list"
      border
      stripe
      v-loading="loading"
      element-loading-text="加载中..."
    >
      <el-table-column prop="createTime" label="操作时间" width="170" />
      <el-table-column prop="username" label="操作人" width="140">
        <template #default="{ row }">
          <span v-if="row.username">{{ row.username }}</span>
          <el-tag v-else type="info" size="small">未登录</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-tag :type="actionTagType(row.action)" size="small">
            {{ actionLabel(row.action) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="模块" width="130">
        <template #default="{ row }">
          <el-tag :type="moduleTagType(row.module)" size="small">
            {{ moduleLabel(row.module) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="target" label="请求路径" min-width="200" show-overflow-tooltip />
      <el-table-column prop="ip" label="IP地址" width="150" />
    </el-table>

    <!-- 空状态 -->
    <el-empty v-if="!loading && list.length === 0" description="暂无操作日志" />

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @size-change="handleSearch"
        @current-change="handleSearch"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { auditApi } from '@/api'

// ---- 筛选 ----
const filterAction = ref('')
const filterModule = ref('')

// ---- 分页 ----
const pagination = reactive({ page: 1, size: 20 })
const total = ref(0)

// ---- 列表 ----
const list = ref<any[]>([])
const loading = ref(false)
const errorMsg = ref('')

// ---- 操作类型选项 ----
const actionOptions = [
  { label: '登录', value: 'LOGIN' },
  { label: '注册', value: 'REGISTER' },
  { label: '提交', value: 'SUBMIT' },
  { label: '审批通过', value: 'APPROVE' },
  { label: '审批拒绝', value: 'REJECT' },
  { label: '回复', value: 'REPLY' },
  { label: '删除', value: 'DELETE' },
  { label: '导出', value: 'EXPORT' },
  { label: '撤回', value: 'WITHDRAW' },
  { label: '批量审批', value: 'BATCH_APPROVE' },
  { label: '批量操作', value: 'BATCH' },
  { label: '修改密码', value: 'CHANGE_PASSWORD' },
  { label: '更新资料', value: 'UPDATE_PROFILE' },
]

// ---- 模块选项 ----
const moduleOptions = [
  { label: '认证', value: 'AUTH' },
  { label: '用户', value: 'USER' },
  { label: '报名', value: 'ENROLLMENT' },
  { label: '审批', value: 'APPROVAL' },
  { label: '反馈', value: 'FEEDBACK' },
  { label: '活动', value: 'ACTIVITY' },
  { label: '院系用户', value: 'COLLEGE_USER' },
  { label: '附件', value: 'ATTACHMENT' },
  { label: '统计', value: 'STATISTICS' },
  { label: 'AI', value: 'AI' },
]

// ---- 操作类型映射 ----
const actionLabelMap: Record<string, string> = {
  LOGIN: '登录',
  REGISTER: '注册',
  SUBMIT: '提交',
  APPROVE: '审批通过',
  REJECT: '审批拒绝',
  REPLY: '回复',
  DELETE: '删除',
  EXPORT: '导出',
  WITHDRAW: '撤回',
  BATCH_APPROVE: '批量审批',
  BATCH: '批量操作',
  CHANGE_PASSWORD: '修改密码',
  UPDATE_PROFILE: '更新资料',
}

const actionTagTypeMap: Record<string, string> = {
  LOGIN: 'success',
  REGISTER: '',
  SUBMIT: 'warning',
  APPROVE: 'success',
  REJECT: 'danger',
  REPLY: '',
  DELETE: 'danger',
  EXPORT: '',
  WITHDRAW: 'info',
  BATCH_APPROVE: 'success',
  BATCH: 'warning',
  CHANGE_PASSWORD: 'info',
  UPDATE_PROFILE: 'info',
}

// ---- 模块映射 ----
const moduleLabelMap: Record<string, string> = {
  AUTH: '认证',
  USER: '用户',
  ENROLLMENT: '报名',
  APPROVAL: '审批',
  FEEDBACK: '反馈',
  ACTIVITY: '活动',
  COLLEGE_USER: '院系用户',
  ATTACHMENT: '附件',
  STATISTICS: '统计',
  AI: 'AI',
}

const moduleTagTypeMap: Record<string, string> = {
  AUTH: 'danger',
  USER: '',
  ENROLLMENT: 'warning',
  APPROVAL: 'success',
  FEEDBACK: '',
  ACTIVITY: '',
  COLLEGE_USER: 'info',
  ATTACHMENT: 'info',
  STATISTICS: 'info',
  AI: '',
}

function actionLabel(a: string): string {
  return actionLabelMap[a] || a
}

function actionTagType(a: string): string {
  return actionTagTypeMap[a] || 'info'
}

function moduleLabel(m: string): string {
  return moduleLabelMap[m] || m
}

function moduleTagType(m: string): string {
  return moduleTagTypeMap[m] || 'info'
}

// ---- 数据获取 ----
async function fetchData() {
  loading.value = true
  errorMsg.value = ''
  try {
    const params: any = {
      page: pagination.page,
      size: pagination.size,
    }
    if (filterAction.value) params.action = filterAction.value
    if (filterModule.value) params.module = filterModule.value

    const res: any = await auditApi.list(params)
    if (res?.data?.list) {
      list.value = res.data.list
      total.value = res.data.total || 0
    } else if (res?.data) {
      list.value = res.data.list || []
      total.value = res.data.total || 0
    } else {
      list.value = []
      total.value = 0
    }
  } catch (err: any) {
    errorMsg.value = err?.response?.data?.message || err?.message || '加载操作日志失败'
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  filterAction.value = ''
  filterModule.value = ''
  pagination.page = 1
  fetchData()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.audit-log-view {
  padding: 0;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a3a5c;
  margin-bottom: 20px;
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

/* 表格 */
:deep(.el-table) {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.el-table th) {
  background-color: #f7f8fa;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
