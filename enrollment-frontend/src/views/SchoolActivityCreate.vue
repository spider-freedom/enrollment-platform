<template>
  <div class="activity-create" v-loading="pageLoading">
    <div class="page-header">
      <h3>{{ isEdit ? '编辑活动' : '创建活动' }}</h3>
      <el-button @click="router.back()">返回列表</el-button>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="130px"
      label-position="right"
    >
      <!-- 第1部分：基本信息 -->
      <el-card shadow="never" class="section-card">
        <template #header>
          <div class="section-header">
            <span class="section-title">基本信息</span>
          </div>
        </template>
        <el-form-item label="活动标题" prop="title">
          <el-input
            v-model="form.title"
            placeholder="请输入活动标题"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="活动描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="6"
            placeholder="请输入活动详细描述，包括活动内容、参与要求、注意事项等"
            maxlength="5000"
            show-word-limit
          />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="6">
            <el-form-item label="线上线下" prop="type">
              <el-select v-model="form.type" placeholder="请选择" style="width: 100%">
                <el-option label="线上活动" value="ONLINE" />
                <el-option label="线下活动" value="OFFLINE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="活动分类" prop="category">
              <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
                <el-option v-for="c in categoryOptions" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="活动状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择" style="width: 100%">
                <el-option label="草稿" value="DRAFT" />
                <el-option label="已发布" value="PUBLISHED" />
                <el-option label="进行中" value="ONGOING" />
                <el-option label="已结束" value="ENDED" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="招生对象" prop="targetAudience">
              <el-select v-model="form.targetAudience" placeholder="请选择" style="width: 100%">
                <el-option label="仅学生" value="student" />
                <el-option label="仅教师" value="teacher" />
                <el-option label="学生+教师" value="all" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="活动地点" prop="location">
          <el-input v-model="form.location" placeholder="请输入活动地点（线上活动可填写会议链接或标注'线上'）" />
        </el-form-item>
      </el-card>

      <!-- 第2部分：时间与容量 -->
      <el-card shadow="never" class="section-card">
        <template #header>
          <div class="section-header">
            <span class="section-title">时间与容量</span>
          </div>
        </template>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="活动开始时间" prop="startTime">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                placeholder="选择活动开始时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="活动结束时间" prop="endTime">
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                placeholder="选择活动结束时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="报名开始日期" prop="enrollStart">
              <el-date-picker
                v-model="form.enrollStart"
                type="date"
                placeholder="选择报名开始日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="报名截止日期" prop="enrollEnd">
              <el-date-picker
                v-model="form.enrollEnd"
                type="date"
                placeholder="选择报名截止日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="学生上限">
              <el-input-number
                v-model="form.maxStudents"
                :min="0"
                :max="10000"
                placeholder="0为不限"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="教师上限">
              <el-input-number
                v-model="form.maxTeachers"
                :min="0"
                :max="1000"
                placeholder="0为不限"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="每校上限">
              <el-input-number
                v-model="form.maxPerSchool"
                :min="1"
                :max="1000"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-card>

      <!-- 第3部分：审批流程与规则 -->
      <el-card shadow="never" class="section-card">
        <template #header>
          <div class="section-header">
            <span class="section-title">审批流程与规则</span>
          </div>
        </template>
        <el-form-item label="审批流程" prop="workflowKey">
          <el-select v-model="form.workflowKey" placeholder="选择审批流程" style="width: 100%">
            <el-option label="学院审批 + 校级审批" value="college_school_approval" />
            <el-option label="仅校级审批" value="school_approval" />
            <el-option label="仅学院审批" value="college_approval" />
          </el-select>
        </el-form-item>
      </el-card>

      <!-- 第4部分：自定义字段 -->
      <el-card shadow="never" class="section-card">
        <template #header>
          <div class="section-header">
            <span class="section-title">自定义字段</span>
            <el-button type="primary" size="small" text @click="addCustomField">
              <el-icon style="margin-right: 2px"><Plus /></el-icon>添加字段
            </el-button>
          </div>
        </template>
        <el-empty v-if="form.customFields.length === 0" description="暂未添加自定义字段" :image-size="60" />
        <div v-for="(field, index) in form.customFields" :key="index" class="custom-field-row">
          <el-row :gutter="12" align="middle">
            <el-col :span="6">
              <el-input v-model="field.name" placeholder="字段名称" />
            </el-col>
            <el-col :span="5">
              <el-select v-model="field.type" placeholder="字段类型" style="width: 100%">
                <el-option label="文本" value="text" />
                <el-option label="数字" value="number" />
                <el-option label="日期" value="date" />
                <el-option label="下拉选择" value="select" />
              </el-select>
            </el-col>
            <el-col :span="4">
              <el-switch
                v-model="field.required"
                active-text="必填"
                inactive-text="选填"
              />
            </el-col>
            <el-col :span="7" v-if="field.type === 'select'">
              <el-input v-model="field.options" placeholder="选项（逗号分隔）" />
            </el-col>
            <el-col :span="2">
              <el-button type="danger" size="small" text @click="removeCustomField(index)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </el-col>
          </el-row>
        </div>
      </el-card>

      <!-- 第5部分：活动横幅 -->
      <el-card shadow="never" class="section-card">
        <template #header>
          <div class="section-header">
            <span class="section-title">活动横幅</span>
          </div>
        </template>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="封面图片URL">
              <el-input v-model="form.bannerUrl" placeholder="请输入封面图片链接地址" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设为轮播展示">
              <el-switch v-model="form.isBanner" active-text="是" inactive-text="否" inline-prompt />
              <div style="font-size:12px;color:#999;margin-top:4px">开启后该活动将在首页轮播图中展示</div>
            </el-form-item>
          </el-col>
        </el-row>
        <div v-if="form.bannerUrl" class="banner-preview">
          <span class="preview-label">预览：</span>
          <el-image
            :src="form.bannerUrl"
            fit="cover"
            class="banner-image"
            :preview-src-list="[form.bannerUrl]"
          >
            <template #error>
              <div class="image-error">
                <el-icon><PictureFilled /></el-icon>
                <span>图片加载失败</span>
              </div>
            </template>
          </el-image>
        </div>
      </el-card>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <el-button @click="router.back()">取消</el-button>
        <el-button type="default" :loading="submitting" @click="handleSaveDraft">
          保存草稿
        </el-button>
        <el-button type="primary" :loading="submitting" @click="handlePublish">
          {{ isEdit ? '保存修改' : '发布活动' }}
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Delete, PictureFilled } from '@element-plus/icons-vue'
import { activityApi } from '@/api'
import { ACTIVITY_CATEGORY_OPTIONS } from '@/utils/constants'

const router = useRouter()
const route = useRoute()
const categoryOptions = ACTIVITY_CATEGORY_OPTIONS

// ============= 模式判断 =============
const isEdit = ref(false)
const editId = ref<number | null>(null)
const pageLoading = ref(false)

// ============= 表单 =============
const formRef = ref()

interface CustomField {
  name: string
  type: string
  required: boolean
  options: string
}

const form = reactive({
  title: '',
  description: '',
  type: 'OFFLINE',
  category: '宣讲会',
  status: 'DRAFT',
  targetAudience: 'all',
  location: '',
  startTime: '',
  endTime: '',
  enrollStart: '',
  enrollEnd: '',
  maxStudents: 200,
  maxTeachers: 10,
  maxPerSchool: 50,
  workflowKey: 'college_school_approval',
  bannerUrl: '',
  isBanner: false,
  linkUrl: '',
  customFields: [] as CustomField[],
})

const rules = {
  title: [
    { required: true, message: '请输入活动标题', trigger: 'blur' },
    { min: 2, max: 200, message: '标题长度在2到200个字符之间', trigger: 'blur' },
  ],
  description: [
    { required: true, message: '请输入活动描述', trigger: 'blur' },
  ],
  type: [
    { required: true, message: '请选择活动类型', trigger: 'change' },
  ],
  targetAudience: [
    { required: true, message: '请选择招生对象', trigger: 'change' },
  ],
  location: [
    { required: true, message: '请输入活动地点', trigger: 'blur' },
  ],
  startTime: [
    { required: true, message: '请选择活动开始时间', trigger: 'change' },
  ],
  endTime: [
    { required: true, message: '请选择活动结束时间', trigger: 'change' },
  ],
  enrollStart: [
    { required: true, message: '请选择报名开始日期', trigger: 'change' },
  ],
  enrollEnd: [
    { required: true, message: '请选择报名截止日期', trigger: 'change' },
  ],
  workflowKey: [
    { required: true, message: '请选择审批流程', trigger: 'change' },
  ],
}

const submitting = ref(false)

// ============= 自定义字段 =============
function addCustomField() {
  form.customFields.push({
    name: '',
    type: 'text',
    required: false,
    options: '',
  })
}

function removeCustomField(index: number) {
  form.customFields.splice(index, 1)
}

// ============= 数据加载（编辑模式） =============
async function loadActivity(id: number) {
  pageLoading.value = true
  try {
    const res = await activityApi.getById(id)
    const data = res?.data || res
    if (data) {
      form.title = data.title || ''
      form.description = data.description || ''
      form.type = data.type || 'OFFLINE'
      form.category = data.category || '宣讲会'
      form.status = data.status || 'DRAFT'
      form.targetAudience = String(data.targetAudience ?? 'all')
      form.location = data.location || ''
      form.startTime = data.startTime || ''
      form.endTime = data.endTime || ''
      form.enrollStart = data.enrollStart || ''
      form.enrollEnd = data.enrollEnd || ''
      form.maxStudents = data.maxStudents ?? 200
      form.maxTeachers = data.maxTeachers ?? 10
      form.maxPerSchool = data.maxPerSchool ?? 50
      form.workflowKey = data.workflowKey || 'college_school_approval'
      form.bannerUrl = data.bannerUrl || ''
      form.isBanner = data.isBanner === 1 || data.isBanner === true
      form.linkUrl = data.linkUrl || ''
      form.customFields = data.customFields || []
    }
  } catch {
    ElMessage.error('加载活动数据失败')
    router.back()
  } finally {
    pageLoading.value = false
  }
}

// ============= 提交 =============
function buildPayload() {
  const toDateTime = (val: string) => {
    if (!val) return ''
    // Already ISO format with T
    if (val.includes('T')) return val
    // Date-only string, append time
    return val + 'T00:00:00'
  }
  return {
    title: form.title,
    description: form.description,
    type: form.type,
    category: form.category,
    status: form.status,
    targetAudience: form.targetAudience === 'student' ? 1 : form.targetAudience === 'teacher' ? 2 : 3,
    location: form.location,
    startTime: form.startTime,
    endTime: form.endTime,
    enrollStart: toDateTime(form.enrollStart),
    enrollEnd: toDateTime(form.enrollEnd),
    maxStudents: form.maxStudents,
    maxTeachers: form.maxTeachers,
    maxPerSchool: form.maxPerSchool,
    workflowKey: form.workflowKey,
    bannerUrl: form.bannerUrl,
    isBanner: form.isBanner ? 1 : 0,
  }
}

async function handleSaveDraft() {
  submitting.value = true
  try {
    const payload = buildPayload()
    if (isEdit.value && editId.value) {
      await activityApi.update(editId.value, payload)
      ElMessage.success('草稿已保存')
    } else {
      await activityApi.create(payload)
      ElMessage.success('草稿已保存')
    }
    router.push('/school/activities')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

async function handlePublish() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const payload = buildPayload()
    if (isEdit.value && editId.value) {
      await activityApi.update(editId.value, payload)
      ElMessage.success('活动修改成功！')
    } else {
      await activityApi.create(payload)
      ElMessage.success('活动发布成功！')
    }
    router.push('/school/activities')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

// ============= 生命周期 =============
onMounted(() => {
  const id = route.query.id || route.params.id
  if (id) {
    isEdit.value = true
    editId.value = Number(id)
    loadActivity(editId.value)
  }
})
</script>

<style scoped>
.activity-create {
  padding: 4px 0;
  max-width: 1000px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h3 {
  margin: 0;
  font-size: 18px;
  color: #111827;
}

.section-card {
  margin-bottom: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title {
  font-weight: 600;
  font-size: 15px;
  color: #1f2937;
}

.custom-field-row {
  margin-bottom: 12px;
  padding: 10px 12px;
  background: #f9fafb;
  border-radius: 6px;
}

.banner-preview {
  margin-top: 12px;
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.preview-label {
  color: #6b7280;
  font-size: 13px;
  line-height: 100px;
}

.banner-image {
  width: 320px;
  height: 100px;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 320px;
  height: 100px;
  background: #f3f4f6;
  color: #9ca3af;
  font-size: 12px;
  gap: 4px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 0;
  position: sticky;
  bottom: 0;
  background: #fff;
  border-top: 1px solid #f3f4f6;
  z-index: 10;
}
</style>
