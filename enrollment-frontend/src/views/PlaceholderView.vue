<template>
  <div class="placeholder-page">
    <div class="placeholder-card">
      <div class="placeholder-icon">{{ icons[route] || '📄' }}</div>
      <h2>{{ titles[route] || '页面建设中' }}</h2>
      <p>{{ descs[route] || '该页面正在建设完善中，敬请期待。' }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = computed(() => useRoute().name as string)

const icons: Record<string, string> = {
  About:'🏫', Policy:'📋', Majors:'📚', AIAssistant:'🤖',
}
const titles: Record<string, string> = {
  About:'关于新大', Policy:'招生政策', Majors:'专业查询', AIAssistant:'AI智能咨询',
}
const descs: Record<string, string> = {
  About:'百年名校，扎根边疆，面向世界',
  Policy:'2026年招生政策与历年录取分数线',
  Majors:'90+本科专业，涵盖理、工、文、法、经、管',
  AIAssistant:'智能招生咨询助手，随时随地为您答疑',
}
</script>

<style scoped>
.placeholder-page {
  min-height: calc(100vh - 64px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
}
.placeholder-card {
  background: #fff;
  border-radius: 24px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  padding: 64px 48px;
  text-align: center;
  max-width: 480px;
  width: 100%;
}
.placeholder-icon { font-size: 48px; margin-bottom: 16px; }
.placeholder-card h2 { font-size: 24px; font-weight: 700; color: #1e293b; margin: 0 0 8px; }
.placeholder-card p { font-size: 14px; color: #94a3b8; margin: 0; line-height: 1.6; }
</style>
