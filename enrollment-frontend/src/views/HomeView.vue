<template>
  <div class="home">
    <!-- Hero -->
    <section class="hero">
      <div class="hero-bg"></div>
      <div class="hero-content">
        <h1 class="hero-title">{{ $t('home.hero1') }}<br/><span class="hero-gold">{{ $t('home.hero2') }}</span></h1>
        <p class="hero-desc">{{ $t('home.heroDesc') }}</p>
        <div class="hero-btns">
          <router-link to="/majors" class="hero-btn-red">{{ $t('home.btnMajors') }} <span style="margin-left:4px">→</span></router-link>
          <router-link to="/ai-assistant" class="hero-btn-glass">{{ $t('home.btnAI') }}</router-link>
        </div>
      </div>
    </section>

    <!-- Stats -->
    <section class="stats">
      <div class="stats-grid">
        <div class="stat-item"><div class="stat-num">1924</div><div class="stat-label">建校年份</div></div>
        <div class="stat-item"><div class="stat-num">3</div><div class="stat-label">双一流学科</div></div>
        <div class="stat-item"><div class="stat-num">3.7万+</div><div class="stat-label">在校学生</div></div>
        <div class="stat-item"><div class="stat-num">4700+ 亩</div><div class="stat-label">校园面积</div></div>
      </div>
    </section>

    <!-- Features -->
    <section class="features">
      <div class="section-label">为什么选择新大</div>
      <h2 class="section-title">一流平台，成就未来</h2>
      <div class="section-divider"></div>
      <div class="features-grid">
        <div class="feature-card">
          <div class="feature-icon red">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 10v6M2 10l10-5 10 5-10 5z"/><path d="M6 12v5c0 2 4 3 6 3s6-1 6-3v-5"/></svg>
          </div>
          <h3>学术顶尖</h3>
          <p>双一流建设高校，拥有多个国家级重点学科和科研平台，师资力量雄厚</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon gold">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><line x1="3" y1="9" x2="21" y2="9"/><line x1="9" y1="21" x2="9" y2="9"/></svg>
          </div>
          <h3>专业多元</h3>
          <p>涵盖理、工、文、法、经、管等多个学科门类，90+本科专业供你选择</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon blue">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><circle cx="12" cy="12" r="3"/><line x1="12" y1="2" x2="12" y2="6"/><line x1="12" y1="18" x2="12" y2="22"/><line x1="2" y1="12" x2="6" y2="12"/><line x1="18" y1="12" x2="22" y2="12"/></svg>
          </div>
          <h3>校园生活</h3>
          <p>现代化宿舍、图书馆、体育馆等设施一应俱全，多彩社团活动等你参与</p>
        </div>
      </div>
    </section>

    <!-- CTA -->
    <section class="cta">
      <div class="cta-pattern"></div>
      <div class="cta-content">
        <h2 class="cta-title">在这里，续写你的青春故事</h2>
        <router-link to="/register" class="cta-btn">立即注册报名</router-link>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
/* Hero */
.hero { position:relative; height:600px; display:flex; align-items:center; justify-content:center; overflow:hidden; }
.hero-bg { position:absolute; inset:0;
  background: linear-gradient(180deg, rgba(0,0,0,0.55) 0%, rgba(0,0,0,0.25) 50%, rgba(0,0,0,0.5) 100%),
    url('/images/campus-hero.jpg') center/cover no-repeat;
}
.hero-content { position:relative; z-index:2; text-align:center; max-width:720px; padding:0 24px; }
.hero-title { font-size:48px; font-weight:800; color:#fff; margin:0 0 16px; line-height:1.2; }
.hero-gold { color:#C9A96E; }
.hero-desc { font-size:18px; color:rgba(255,255,255,0.8); margin:0 0 32px; line-height:1.6; }
.hero-btns { display:flex; gap:16px; justify-content:center; flex-wrap:wrap; }
.hero-btn-red { background:#A31F34; color:#fff; padding:14px 32px; border-radius:50px; text-decoration:none; font-size:16px; font-weight:600; transition:background 0.2s; display:inline-flex; align-items:center; }
.hero-btn-red:hover { background:#821829; }
.hero-btn-glass { background:rgba(255,255,255,0.15); backdrop-filter:blur(12px); color:#fff; padding:14px 32px; border-radius:50px; text-decoration:none; font-size:16px; font-weight:500; border:1px solid rgba(255,255,255,0.2); transition:background 0.2s; }
.hero-btn-glass:hover { background:rgba(255,255,255,0.25); }

/* Stats */
.stats { background:#fff; border-bottom:1px solid #f1f5f9; padding:40px 0; }
.stats-grid { max-width:1000px; margin:0 auto; display:grid; grid-template-columns:repeat(4,1fr); gap:24px; padding:0 24px; }
.stat-item { text-align:center; }
.stat-num { font-size:36px; font-weight:800; color:#A31F34; }
.stat-label { font-size:13px; color:#94a3b8; margin-top:4px; }

/* Features */
.features { padding:80px 24px; background:#f8fafc; text-align:center; }
.section-label { font-size:13px; color:#A31F34; font-weight:600; text-transform:uppercase; letter-spacing:1px; margin-bottom:8px; }
.section-title { font-size:32px; font-weight:700; color:#1e293b; margin:0 0 16px; }
.section-divider { width:60px; height:3px; background:#C9A96E; margin:0 auto 48px; border-radius:2px; }
.features-grid { max-width:960px; margin:0 auto; display:grid; grid-template-columns:1fr; gap:24px; }
@media (min-width:768px) { .features-grid { grid-template-columns:repeat(3,1fr); } }
.feature-card { background:#fff; padding:40px 28px; border-radius:16px; border:1px solid #e2e8f0; box-shadow:0 1px 3px rgba(0,0,0,0.04); text-align:center; transition:box-shadow 0.3s,transform 0.3s; }
.feature-card:hover { box-shadow:0 8px 24px rgba(0,0,0,0.08); transform:translateY(-2px); }
.feature-icon { width:48px; height:48px; border-radius:12px; display:inline-flex; align-items:center; justify-content:center; font-size:22px; margin-bottom:16px; }
.feature-icon.red { background:rgba(163,31,52,0.1); color:#A31F34; }
.feature-icon.gold { background:rgba(201,169,110,0.15); color:#C9A96E; }
.feature-icon.blue { background:rgba(46,127,185,0.1); color:#2E7FB9; }
.feature-card h3 { font-size:18px; font-weight:700; color:#1e293b; margin:0 0 8px; }
.feature-card p { font-size:13px; color:#64748b; line-height:1.7; margin:0; }

/* CTA */
.cta { position:relative; background:linear-gradient(180deg,rgba(130,24,41,0.85),rgba(163,31,52,0.8)),url('/images/campus-students.jpg') center/cover no-repeat; padding:80px 24px; text-align:center; overflow:hidden; }
.cta-pattern { position:absolute; inset:0; opacity:0.06; background-image:radial-gradient(#C9A96E 1px,transparent 1px); background-size:20px 20px; }
.cta-content { position:relative; z-index:2; }
.cta-title { font-size:36px; font-weight:700; color:#fff; margin:0 0 32px; }
.cta-btn { background:#C9A96E; color:#fff; padding:16px 48px; border-radius:50px; text-decoration:none; font-size:18px; font-weight:700; display:inline-block; transition:opacity 0.2s; box-shadow:0 8px 24px rgba(0,0,0,0.2); }
.cta-btn:hover { opacity:0.9; }
</style>
