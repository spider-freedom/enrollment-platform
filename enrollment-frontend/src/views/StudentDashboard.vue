<template>
  <div style="display:flex;flex-direction:column;gap:32px">
    <!-- Profile Banner -->
    <div style="position:relative;padding:40px;background:#fff;border-radius:24px;border:1px solid #e2e8f0;overflow:hidden;box-shadow:0 1px 2px rgba(0,0,0,0.04)">
      <div style="position:absolute;top:0;right:0;width:256px;height:256px;background:rgba(201,169,110,0.05);border-radius:9999px;transform:translate(50%,-50%)"></div>
      <div style="position:relative;z-index:10;display:flex;align-items:flex-start;gap:32px;flex-wrap:wrap">
        <div style="width:96px;height:96px;background:#f1f5f9;border-radius:20px;display:flex;align-items:center;justify-content:center;font-size:32px;font-weight:700;color:#94a3b8;border:4px solid #fff;box-shadow:0 4px 12px rgba(0,0,0,0.1);flex-shrink:0">
          {{ store.userInfo?.name?.[0] || '?' }}
        </div>
        <div style="flex:1;min-width:200px">
          <h1 style="font-size:30px;font-weight:700;color:#1e293b;margin:0 0 8px">{{ store.userInfo?.name || '用户' }}</h1>
          <div style="display:flex;gap:8px;flex-wrap:wrap;margin-bottom:12px">
            <span style="padding:4px 12px;background:rgba(201,169,110,0.1);color:#C9A96E;border-radius:9999px;font-size:12px;font-weight:600;border:1px solid rgba(201,169,110,0.2)">{{ $t("考生身份") }}</span>
            <span style="padding:4px 12px;background:#f1f5f9;color:#64748b;border-radius:9999px;font-size:12px;font-weight:600;border:1px solid #e2e8f0">{{ $t("手机已验证") }}</span>
          </div>
          <p style="color:#64748b;font-size:13px;max-width:480px;margin:0">{{ $t("欢迎来到新疆大学招生系统。您可以在这里管理您的报名活动、查看咨询回复以及更新个人信息。") }}</p>
        </div>
        <router-link to="/student/profile" style="padding:10px 24px;background:#1e293b;color:#fff;border-radius:12px;font-weight:700;font-size:14px;text-decoration:none;box-shadow:0 2px 8px rgba(0,0,0,0.12);flex-shrink:0;align-self:center">{{ $t("编辑资料") }}</router-link>
      </div>
    </div>

    <!-- Two Cards -->
    <div style="display:grid;grid-template-columns:1fr;gap:32px" class="dashboard-grid">
      <!-- My Activities -->
      <div style="background:#fff;border-radius:24px;border:1px solid #e2e8f0;padding:32px;box-shadow:0 1px 2px rgba(0,0,0,0.04)">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:24px">
          <h3 style="font-size:20px;font-weight:700;color:#1e293b;display:flex;align-items:center;gap:8px;margin:0">
            <span style="color:#C9A96E;font-size:22px">📅</span>{{ $t("我的活动") }}</h3>
          <router-link to="/student/activities" style="font-size:12px;font-weight:700;color:#C9A96E;text-decoration:none">{{ $t("查看全部") }}</router-link>
        </div>
        <div style="display:flex;flex-direction:column;gap:16px">
          <div v-if="myActivities.length" v-for="a in myActivities" :key="a.id" style="padding:16px;border-radius:16px;background:#f8fafc;border:1px solid #f1f5f9;display:flex;align-items:center;justify-content:space-between">
            <div style="display:flex;align-items:center;gap:16px">
              <div style="width:48px;height:48px;background:#fff;border-radius:12px;display:flex;align-items:center;justify-content:center;color:#A31F34;box-shadow:0 1px 3px rgba(0,0,0,0.06)">📅</div>
              <div>
                <h4 style="font-weight:700;color:#334155;margin:0 0 2px;font-size:14px">{{ a.activityTitle || a.title }}</h4>
                <p style="font-size:12px;color:#94a3b8;margin:0">{{ a.createTime?.substring(0,10) || a.date }}</p>
              </div>
            </div>
            <span style="font-size:10px;padding:4px 10px;background:rgba(16,185,129,0.1);color:#10b981;border-radius:9999px;font-weight:700">{{ $t("已报名") }}</span>
          </div>
          <div style="padding:40px;border:2px dashed #e2e8f0;border-radius:16px;display:flex;flex-direction:column;align-items:center;justify-content:center;text-align:center">
            <p style="color:#94a3b8;font-size:13px;margin:0 0 16px">{{ $t("参加更多活动，深入了解新大") }}</p>
            <router-link to="/activities" style="color:#A31F34;font-size:13px;font-weight:700;display:flex;align-items:center;gap:4px;text-decoration:none">{{ $t("去探索更多活动") }}<span>→</span></router-link>
          </div>
        </div>
      </div>

      <!-- My Inquiries -->
      <div style="background:#fff;border-radius:24px;border:1px solid #e2e8f0;padding:32px;box-shadow:0 1px 2px rgba(0,0,0,0.04)">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:24px">
          <h3 style="font-size:20px;font-weight:700;color:#1e293b;display:flex;align-items:center;gap:8px;margin:0">
            <span style="color:#C9A96E;font-size:22px">💬</span>{{ $t("我的反馈") }}</h3>
          <router-link to="/student/my-feedback" style="font-size:12px;font-weight:700;color:#C9A96E;text-decoration:none">{{ $t("查看全部") }}</router-link>
        </div>
        <div style="display:flex;flex-direction:column;gap:16px">
          <div v-if="myFeedback.length" v-for="f in myFeedback" :key="f.feedbackId||f.id" style="padding:16px;border-radius:16px;background:#f8fafc;border:1px solid #f1f5f9">
            <div style="display:flex;justify-content:space-between;margin-bottom:8px">
              <p style="font-size:13px;font-weight:700;color:#334155;margin:0;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ f.content?.substring(0,30) || '反馈' }}</p>
              <span :style="{fontSize:'10px',padding:'3px 8px',borderRadius:'9999px',fontWeight:'700',background:f.status==='REPLIED'?'rgba(16,185,129,0.1)':'rgba(245,158,11,0.1)',color:f.status==='REPLIED'?'#10b981':'#f59e0b'}">
                {{ f.status==='REPLIED' ? '已回复' : '待处理' }}
              </span>
            </div>
            <p v-if="f.reply" style="font-size:12px;color:#94a3b8;margin:0;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">"{{ f.reply }}"</p>
          </div>
          <div v-else style="padding:40px;border:2px dashed #e2e8f0;border-radius:16px;text-align:center">
            <p style="color:#94a3b8;font-size:13px;margin:0">{{ $t("暂无反馈记录") }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { enrollmentApi, feedbackApi } from '@/api'

const store = useUserStore()
const myActivities = ref<any[]>([])
const myFeedback = ref<any[]>([])

onMounted(async () => {
  try {
    const eRes: any = await enrollmentApi.listMy({ page:1, size:3 })
    myActivities.value = eRes?.data?.list || eRes?.data?.records || []
    const fRes: any = await feedbackApi.listMy({ page:1, size:3 })
    myFeedback.value = fRes?.data?.list || fRes?.data?.records || []
  } catch {}
})
</script>

<style scoped>
.dashboard-grid { grid-template-columns: 1fr; }
@media (min-width: 768px) { .dashboard-grid { grid-template-columns: 1fr 1fr; } }
</style>
