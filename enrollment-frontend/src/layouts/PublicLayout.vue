<template>
  <div class="public-layout">
    <!-- Header -->
    <header class="pub-header">
      <div class="pub-header-inner">
        <router-link to="/" class="pub-logo">
          <img src="/images/xju-logo.png" style="width:36px;height:auto" alt="新疆大学" />
          <div class="pub-logo-text">
            <span class="pub-logo-main">新疆大学</span>
            <span class="pub-logo-sub">2026招生宣传系统</span>
          </div>
        </router-link>

        <nav class="pub-nav">
          <router-link v-for="item in navItems" :key="item.path" :to="item.path" class="pub-nav-link">
            {{ item.label }}
          </router-link>

          <span class="pub-nav-divider"></span>
          <el-dropdown trigger="click" @command="switchLang">
            <span class="lang-dropdown">{{ langOptions.find(l=>l.value===locale)?.label || '中文' }} ▾</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-for="l in langOptions" :key="l.value" :command="l.value" :class="{active:locale===l.value}">{{ l.label }}</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <template v-if="store.isLoggedIn">
            <span class="pub-nav-divider"></span>
            <router-link :to="dashboardPath" class="pub-nav-user">
              <el-icon :size="16"><User /></el-icon>
              <span>{{ store.userInfo?.name }}</span>
            </router-link>
            <button class="pub-btn-logout" @click="handleLogout">
              <el-icon :size="16"><SwitchButton /></el-icon>
            </button>
          </template>
          <template v-else>
            <span class="pub-nav-divider"></span>
            <router-link to="/login" class="pub-btn-outline">登录</router-link>
            <router-link to="/register" class="pub-btn-solid">注册</router-link>
          </template>
        </nav>

        <button class="pub-menu-btn" @click="mobileOpen=!mobileOpen">
          <el-icon :size="22"><component :is="mobileOpen ? 'Close' : 'Menu'" /></el-icon>
        </button>
      </div>

      <!-- Mobile menu -->
      <div v-if="mobileOpen" class="pub-mobile-menu">
        <router-link v-for="item in navItems" :key="item.path" :to="item.path" class="pub-mobile-link" @click="mobileOpen=false">
          {{ item.label }}
        </router-link>
        <div class="pub-mobile-divider"></div>
        <template v-if="store.isLoggedIn">
          <router-link :to="dashboardPath" class="pub-mobile-link" @click="mobileOpen=false">
            {{ store.userInfo?.name }}
          </router-link>
          <button class="pub-mobile-link" @click="handleLogout">退出登录</button>
        </template>
        <template v-else>
          <router-link to="/login" class="pub-mobile-btn" @click="mobileOpen=false">登录</router-link>
          <router-link to="/register" class="pub-mobile-btn gold" @click="mobileOpen=false">注册</router-link>
        </template>
      </div>
    </header>

    <!-- Main -->
    <main class="pub-main">
      <router-view />
    </main>

    <!-- Footer -->
    <footer class="pub-footer">
      <div class="pub-footer-grid">
        <div style="grid-column:span 2">
          <h3 class="pub-footer-title">新疆大学</h3>
          <p class="pub-footer-desc">
            新疆大学位于新疆维吾尔自治区首府乌鲁木齐市，是国家"双一流"建设高校、部省合建高校、国家"211工程"重点建设高校。
          </p>
          <p class="pub-footer-copy">© 2026 新疆大学招生办公室 版权所有</p>
        </div>
        <div>
          <h4 class="pub-footer-head">快速链接</h4>
          <router-link to="/about" class="pub-footer-link">关于新大</router-link>
          <router-link to="/majors" class="pub-footer-link">专业目录</router-link>
          <router-link to="/policy" class="pub-footer-link">历年分数</router-link>
          <router-link to="/ai-assistant" class="pub-footer-link">咨询助手</router-link>
        </div>
        <div>
          <h4 class="pub-footer-head">联系我们</h4>
          <p class="pub-footer-link">地址：新疆乌鲁木齐市天山区胜利路666号</p>
          <p class="pub-footer-link">电话：0991-8585671 / 8585672</p>
          <p class="pub-footer-link">邮编：830046</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import { User, SwitchButton, Menu, Close } from '@element-plus/icons-vue'
import { langOptions } from '@/i18n'

const store = useUserStore()
const router = useRouter()
const { locale } = useI18n()
const mobileOpen = ref(false)

function switchLang(lang: string) {
  locale.value = lang
  localStorage.setItem('lang', lang)
}

const navItems = [
  { key:'home', path:'/' },
  { key:'about', path:'/about' },
  { key:'policy', path:'/policy' },
  { key:'majors', path:'/majors' },
  { key:'activities', path:'/activities' },
  { key:'ai', path:'/ai-assistant' },
]

const dashboardPath = computed(() => {
  const r = (store.currentRole || '').toLowerCase()
  if (r === 'school_admin') return '/school/dashboard'
  if (r === 'college_admin') return '/college/activities'
  if (r === 'teacher') return '/teacher/activities'
  return '/student/activities'
})

function handleLogout() {
  store.logout()
  router.push('/')
}
</script>

<style scoped>
.public-layout { min-height:100vh; display:flex; flex-direction:column; background:#f8fafc; font-family:"PingFang SC","Microsoft YaHei",sans-serif; }

/* Header */
.pub-header { background:#A31F34; color:#fff; position:sticky; top:0; z-index:50; box-shadow:0 2px 12px rgba(0,0,0,0.15); }
.pub-header-inner { max-width:1200px; margin:0 auto; height:64px; display:flex; align-items:center; padding:0 24px; gap:32px; }
.pub-logo { display:flex; align-items:center; gap:10px; text-decoration:none; color:#fff; flex-shrink:0; }
.pub-logo-circle { width:36px; height:36px; background:#fff; border-radius:50%; display:flex; align-items:center; justify-content:center; color:#A31F34; font-size:18px; font-weight:800; }
.pub-logo-text { display:flex; flex-direction:column; }
.pub-logo-main { font-size:16px; font-weight:700; line-height:1.2; }
.pub-logo-sub { font-size:11px; opacity:0.75; }
.pub-nav { display:none; align-items:center; gap:24px; flex:1; }
@media (min-width:900px) { .pub-nav { display:flex; } }
.pub-nav-link { color:#fff; text-decoration:none; font-size:14px; font-weight:500; opacity:0.9; transition:opacity 0.2s; }
.pub-nav-link:hover,.pub-nav-link.router-link-active { opacity:1; color:#C9A96E; }
.pub-nav-divider { width:1px; height:20px; background:rgba(255,255,255,0.2); margin:0 4px; }
.pub-nav-user { display:flex; align-items:center; gap:6px; color:#fff; text-decoration:none; font-size:13px; }
.pub-nav-user:hover { color:#C9A96E; }
.pub-btn-logout { background:none; border:none; color:#fff; cursor:pointer; display:flex; padding:2px; }
.pub-btn-logout:hover { color:#C9A96E; }
.pub-btn-outline { border:1px solid rgba(255,255,255,0.5); color:#fff; padding:6px 16px; border-radius:8px; text-decoration:none; font-size:13px; transition:all 0.2s; }
.pub-btn-outline:hover { background:#fff; color:#A31F34; }
.pub-btn-solid { background:#C9A96E; color:#fff; padding:6px 16px; border-radius:8px; text-decoration:none; font-size:13px; font-weight:600; transition:opacity 0.2s; }
.pub-btn-solid:hover { opacity:0.9; }

.lang-dropdown { color:rgba(255,255,255,0.85); font-size:13px; cursor:pointer; padding:4px 10px; border-radius:6px; transition:all 0.2s; border:1px solid rgba(255,255,255,0.3); }
.lang-dropdown:hover { color:#C9A96E; border-color:#C9A96E; }

.pub-menu-btn { display:block; background:none; border:none; color:#fff; cursor:pointer; margin-left:auto; }
@media (min-width:900px) { .pub-menu-btn { display:none; } }

/* Mobile menu */
.pub-mobile-menu { background:#8B1A2B; border-top:1px solid rgba(255,255,255,0.1); padding:12px 24px; display:flex; flex-direction:column; gap:4px; }
@media (min-width:900px) { .pub-mobile-menu { display:none; } }
.pub-mobile-link { display:block; padding:10px 0; color:#fff; text-decoration:none; border-bottom:1px solid rgba(255,255,255,0.08); font-size:14px; background:none; border-left:none; border-right:none; border-top:none; text-align:left; cursor:pointer; }
.pub-mobile-divider { height:1px; }
.pub-mobile-btn { display:block; text-align:center; padding:10px; border-radius:8px; text-decoration:none; color:#fff; border:1px solid rgba(255,255,255,0.5); font-size:14px; margin-top:8px; }
.pub-mobile-btn.gold { background:#C9A96E; border-color:#C9A96E; }

/* Main */
.pub-main { flex:1; }

/* Footer */
.pub-footer { background:#0f172a; color:#94a3b8; padding:48px 24px; margin-top:auto; }
.pub-footer-grid { max-width:1200px; margin:0 auto; display:grid; grid-template-columns:1fr; gap:32px; }
@media (min-width:768px) { .pub-footer-grid { grid-template-columns:2fr 1fr 1fr; } }
.pub-footer-title { color:#fff; font-size:18px; font-weight:700; margin:0 0 12px; }
.pub-footer-desc { max-width:400px; font-size:13px; line-height:1.7; margin:0 0 16px; }
.pub-footer-copy { font-size:12px; margin:0; }
.pub-footer-head { color:#fff; font-size:14px; font-weight:700; margin:0 0 12px; }
.pub-footer-link { display:block; color:#94a3b8; text-decoration:none; font-size:13px; margin-bottom:8px; }
.pub-footer-link:hover { color:#fff; }
</style>
