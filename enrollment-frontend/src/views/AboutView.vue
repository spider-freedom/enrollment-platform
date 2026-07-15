<template>
  <div>
    <!-- Page Header -->
    <section class="ab-header">
      <h1>关于新大</h1>
    </section>

    <!-- Intro -->
    <section class="ab-intro">
      <div class="ab-intro-grid">
        <div class="ab-intro-img"></div>
        <div class="ab-intro-text">
          <span class="ab-badge">百年新大</span>
          <h2>扎根边疆，面向世界</h2>
          <p>
            新疆大学始建于1924年，前身是新疆俄文法政专门学校。近百年来，学校始终与国家同呼吸、共命运。
            学校是国家"双一流"建设高校、部省合建高校、国家"211工程"重点建设高校，被誉为"新疆高等教育的一面旗帜"。
            学校现有三个校区，占地面积4700余亩，现有各类在校学生3.7万余人。
          </p>
          <div class="ab-facts">
            <div><span class="ab-fact-num">1924</span><span class="ab-fact-label">建校</span></div>
            <div><span class="ab-fact-num">3</span><span class="ab-fact-label">双一流学科</span></div>
          </div>
        </div>
      </div>
    </section>

    <!-- Campuses -->
    <section class="ab-campuses">
      <h2 class="ab-section-title">三大校区</h2>
      <div class="ab-campus-grid">
        <div class="ab-campus-card">
          <div class="ab-campus-icon">🏛</div>
          <h3>红湖校区</h3>
          <p>主校区，位于乌鲁木齐市天山区胜利路666号，历史悠久，文化底蕴深厚</p>
        </div>
        <div class="ab-campus-card">
          <div class="ab-campus-icon">🏗</div>
          <h3>博达校区</h3>
          <p>新校区，现代化教学设施，美丽的校园环境，位于水磨沟区</p>
        </div>
        <div class="ab-campus-card">
          <div class="ab-campus-icon">🏫</div>
          <h3>友好校区</h3>
          <p>位于乌鲁木齐市沙依巴克区，交通便利，紧邻市中心</p>
        </div>
      </div>
    </section>

    <!-- Motto -->
    <section class="ab-motto">
      <div class="ab-motto-bg"></div>
      <div class="ab-motto-content">
        <div class="ab-motto-icon">🎓</div>
        <h2>团结 紧张 质朴 活泼</h2>
        <p>—— 新疆大学校训</p>
      </div>
    </section>
  </div>
</template>

<style scoped>
.ab-header { background:linear-gradient(180deg,rgba(0,0,0,0.55),rgba(0,0,0,0.4)),url('/images/campus-building.jpg') center/cover no-repeat; padding:100px 24px; text-align:center; position:relative; overflow:hidden; }
.ab-header h1 { position:relative; color:#fff; font-size:44px; font-weight:800; margin:0; text-shadow:0 2px 8px rgba(0,0,0,0.3); }

.ab-intro { padding:80px 24px; background:#fff; }
.ab-intro-grid { max-width:1100px; margin:0 auto; display:flex; gap:64px; align-items:center; flex-wrap:wrap; }
.ab-intro-img { flex:1; min-width:300px; height:360px; border-radius:24px;
  background: url('/images/campus-building.jpg') center/cover no-repeat;
  box-shadow:0 20px 60px rgba(0,0,0,0.15); }
.ab-campus-card:nth-child(1) { background: linear-gradient(180deg, #fff 0%, #fef5f5 100%); border-left:4px solid #A31F34; }
.ab-campus-card:nth-child(2) { background: linear-gradient(180deg, #fff 0%, #f5f7fe 100%); border-left:4px solid #2E7FB9; }
.ab-campus-card:nth-child(3) { background: linear-gradient(180deg, #fff 0%, #fdfaf3 100%); border-left:4px solid #C9A96E; }
.ab-intro-text { flex:1; min-width:300px; }
.ab-badge { display:inline-block; padding:6px 16px; background:rgba(163,31,52,0.08); color:#A31F34; border-radius:9999px; font-size:13px; font-weight:700; margin-bottom:16px; }
.ab-intro-text h2 { font-size:32px; font-weight:700; color:#1e293b; margin:0 0 16px; }
.ab-intro-text p { font-size:15px; color:#64748b; line-height:1.8; margin:0 0 24px; }
.ab-facts { display:grid; grid-template-columns:1fr 1fr; gap:16px; }
.ab-fact-num { display:block; font-size:28px; font-weight:800; color:#A31F34; }
.ab-fact-label { font-size:13px; color:#94a3b8; }

.ab-campuses { padding:80px 24px; background:#f8fafc; }
.ab-section-title { text-align:center; font-size:32px; font-weight:700; color:#1e293b; margin:0 0 48px; }
.ab-campus-grid { max-width:1000px; margin:0 auto; display:grid; grid-template-columns:1fr; gap:24px; }
@media (min-width:768px) { .ab-campus-grid { grid-template-columns:repeat(3,1fr); } }
.ab-campus-card { background:#fff; padding:40px 32px; border-radius:24px; border:1px solid #e2e8f0; box-shadow:0 1px 3px rgba(0,0,0,0.04); transition:transform 0.3s,box-shadow 0.3s; }
.ab-campus-card:hover { transform:translateY(-4px); box-shadow:0 12px 32px rgba(0,0,0,0.08); }
.ab-campus-icon { font-size:36px; margin-bottom:16px; }
.ab-campus-card h3 { font-size:20px; font-weight:700; color:#1e293b; margin:0 0 8px; }
.ab-campus-card p { font-size:13px; color:#64748b; line-height:1.7; margin:0; }

.ab-motto { position:relative; background:#A31F34; padding:100px 24px; text-align:center; color:#fff; overflow:hidden; }
.ab-motto-bg { position:absolute; inset:0; opacity:0.03;
  background:linear-gradient(45deg,#A31F34 25%,#C9A96E 25%,#C9A96E 50%,#A31F34 50%,#A31F34 75%,#C9A96E 75%,#C9A96E 100%);background-size:40px 40px; }
.ab-motto-content { position:relative; z-index:2; }
.ab-motto-icon { font-size:48px; opacity:0.3; margin-bottom:16px; }
.ab-motto h2 { font-size:38px; font-weight:700; margin:0 0 8px; letter-spacing:0.15em; }
.ab-motto p { font-size:14px; opacity:0.7; font-style:italic; margin:0; }
</style>
