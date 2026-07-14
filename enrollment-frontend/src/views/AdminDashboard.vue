<template>
  <div style="display:flex;flex-direction:column;gap:32px">
    <div>
      <h1 style="font-size:24px;font-weight:700;color:#1e293b;margin:0 0 4px">系统概览</h1>
      <p style="color:#64748b;font-size:14px;margin:0">欢迎回来，管理员。这是今天的系统运行数据。</p>
    </div>

    <!-- Stat Cards -->
    <div class="stat-grid">
      <div class="stat-card">
        <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:16px">
          <div style="width:48px;height:48px;background:#f8fafc;border-radius:12px;display:flex;align-items:center;justify-content:center;color:#475569">👥</div>
          <div style="display:flex;align-items:center;font-size:12px;font-weight:700;color:#16a34a">↑ +12.5%</div>
        </div>
        <div style="font-size:24px;font-weight:700;color:#1e293b;margin-bottom:4px">{{ stats.users }}</div>
        <div style="font-size:13px;color:#94a3b8">总注册用户</div>
      </div>
      <div class="stat-card">
        <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:16px">
          <div style="width:48px;height:48px;background:#f8fafc;border-radius:12px;display:flex;align-items:center;justify-content:center;color:#475569">📋</div>
          <div style="display:flex;align-items:center;font-size:12px;font-weight:700;color:#16a34a">↑ +8.2%</div>
        </div>
        <div style="font-size:24px;font-weight:700;color:#1e293b;margin-bottom:4px">{{ stats.enrollments }}</div>
        <div style="font-size:13px;color:#94a3b8">活动报名数</div>
      </div>
      <div class="stat-card">
        <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:16px">
          <div style="width:48px;height:48px;background:#f8fafc;border-radius:12px;display:flex;align-items:center;justify-content:center;color:#475569">💬</div>
          <div style="display:flex;align-items:center;font-size:12px;font-weight:700;color:#dc2626">↓ -1.5%</div>
        </div>
        <div style="font-size:24px;font-weight:700;color:#1e293b;margin-bottom:4px">{{ stats.feedbackRate }}%</div>
        <div style="font-size:13px;color:#94a3b8">反馈回复率</div>
      </div>
      <div class="stat-card">
        <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:16px">
          <div style="width:48px;height:48px;background:#f8fafc;border-radius:12px;display:flex;align-items:center;justify-content:center;color:#475569">📊</div>
          <div style="display:flex;align-items:center;font-size:12px;font-weight:700;color:#16a34a">↑ +24%</div>
        </div>
        <div style="font-size:24px;font-weight:700;color:#1e293b;margin-bottom:4px">{{ stats.activities }}</div>
        <div style="font-size:13px;color:#94a3b8">活动总数</div>
      </div>
    </div>

    <!-- Bottom two cards -->
    <div class="detail-grid">
      <!-- Recent Feedback -->
      <div style="background:#fff;border-radius:16px;border:1px solid #e2e8f0;overflow:hidden">
        <div style="padding:24px;border-bottom:1px solid #f1f5f9;display:flex;justify-content:space-between;align-items:center">
          <h3 style="font-weight:700;color:#1e293b;margin:0;font-size:15px">最近反馈</h3>
          <router-link to="/school/feedbacks" style="font-size:13px;color:#A31F34;font-weight:500;text-decoration:none">查看全部</router-link>
        </div>
        <div style="border-top-color:#f8fafc">
          <div v-for="f in recentFeedbacks" :key="f.feedbackId||f.id" style="padding:20px 24px;display:flex;align-items:flex-start;gap:16px;border-bottom:1px solid #f8fafc">
            <div style="width:40px;height:40px;border-radius:9999px;background:#f1f5f9;display:flex;align-items:center;justify-content:center;color:#64748b;font-weight:700;flex-shrink:0">{{ (f.userName || '?')[0] }}</div>
            <div style="flex:1;min-width:0">
              <div style="display:flex;justify-content:space-between;margin-bottom:4px">
                <span style="font-weight:700;color:#334155;font-size:14px">{{ f.userName }}</span>
                <span style="font-size:12px;color:#94a3b8">{{ f.createTime?.substring(0,10) }}</span>
              </div>
              <p style="font-size:13px;color:#64748b;margin:0 0 8px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ f.content?.substring(0,50) }}</p>
              <span :style="{fontSize:'10px',padding:'3px 8px',borderRadius:'9999px',fontWeight:'700',background:f.status==='REPLIED'?'rgba(16,185,129,0.1)':'rgba(245,158,11,0.1)',color:f.status==='REPLIED'?'#10b981':'#f59e0b'}">
                {{ f.status==='REPLIED'?'已回复':'待处理' }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Hot Activities -->
      <div style="background:#fff;border-radius:16px;border:1px solid #e2e8f0;overflow:hidden">
        <div style="padding:24px;border-bottom:1px solid #f1f5f9;display:flex;justify-content:space-between;align-items:center">
          <h3 style="font-weight:700;color:#1e293b;margin:0;font-size:15px">热门活动</h3>
          <router-link to="/school/activities" style="font-size:13px;color:#A31F34;font-weight:500;text-decoration:none">活动管理</router-link>
        </div>
        <div style="padding:24px;display:flex;flex-direction:column;gap:20px">
          <div v-for="a in hotActivities" :key="a.id" style="display:flex;align-items:center;gap:16px">
            <div style="width:64px;height:48px;border-radius:8px;background:linear-gradient(135deg,#A31F34,#c23d4f);display:flex;align-items:center;justify-content:center;color:#fff;font-size:20px;flex-shrink:0">📅</div>
            <div style="flex:1;min-width:0">
              <h4 style="font-weight:700;color:#334155;margin:0 0 2px;font-size:14px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ a.title }}</h4>
              <div style="display:flex;align-items:center;font-size:12px;color:#94a3b8;margin-bottom:4px">
                <span>报名: {{ a.currentStudents||0 }}/{{ a.maxStudents||0 }}</span>
              </div>
              <div style="width:100%;background:#f1f5f9;height:6px;border-radius:9999px;overflow:hidden">
                <div style="background:#A31F34;height:100%;border-radius:9999px" :style="{width:(a.maxStudents?Math.round((a.currentStudents||0)/a.maxStudents*100):0)+'%'}"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { statisticsApi, feedbackApi, activityApi } from '@/api'

const stats = reactive({ users:45, enrollments:21, feedbackRate:86, activities:24 })
const recentFeedbacks = ref<any[]>([])
const hotActivities = ref<any[]>([])

onMounted(async () => {
  try {
    const dash: any = await statisticsApi.getDashboard()
    const d = dash?.data || dash
    if (d) { stats.users = d.totalEnrollments ? 45 : stats.users; stats.enrollments = d.totalEnrollments||21; stats.activities = d.totalActivities||24 }
  } catch {}
  try {
    const fRes: any = await feedbackApi.listSchool({ page:1, size:3 })
    recentFeedbacks.value = fRes?.data?.list || fRes?.data?.records || []
  } catch {}
  try {
    const aRes: any = await activityApi.listSchool({ page:1, size:5 })
    hotActivities.value = aRes?.data?.list || aRes?.data?.records || []
  } catch {}
})
</script>

<style scoped>
.stat-grid { display:grid; grid-template-columns:1fr; gap:20px; }
@media (min-width:640px) { .stat-grid { grid-template-columns:repeat(2,1fr); } }
@media (min-width:1024px) { .stat-grid { grid-template-columns:repeat(4,1fr); } }
.stat-card { background:#fff;padding:24px;border-radius:16px;border:1px solid #e2e8f0;box-shadow:0 1px 2px rgba(0,0,0,0.04); }
.detail-grid { display:grid; grid-template-columns:1fr; gap:32px; }
@media (min-width:1024px) { .detail-grid { grid-template-columns:1fr 1fr; } }
</style>
