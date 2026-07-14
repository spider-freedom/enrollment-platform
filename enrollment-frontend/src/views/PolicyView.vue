<template>
  <div class="policy-page">
    <div class="policy-hero">
      <h1>招生政策</h1>
      <p>新疆大学2026年本科招生政策与历年录取参考</p>
    </div>

    <div class="policy-body">
      <!-- Filter -->
      <div class="policy-filter">
        <div class="policy-filter-label">年份筛选</div>
        <div class="policy-filter-chips">
          <button v-for="y in years" :key="y" :class="['policy-chip',{active:selected===y}]" @click="selected=y">{{ y }}年</button>
        </div>
      </div>

      <!-- Policy List -->
      <div class="policy-list">
        <div v-for="p in filteredPolicies" :key="p.id" class="policy-item">
          <div class="policy-item-left">
            <div class="policy-icon">📋</div>
            <div class="policy-info">
              <h3>{{ p.title }}</h3>
              <span class="policy-meta">{{ p.date }} · <span :class="'policy-badge-'+(p.type==='章程'?'red':'gold')">{{ p.type }}</span></span>
            </div>
          </div>
          <button class="policy-dl-btn">查看详情</button>
        </div>
      </div>

      <!-- FAQ -->
      <div class="policy-faq">
        <h2>常见问题</h2>
        <div class="policy-faq-grid">
          <div v-for="(faq,i) in faqs" :key="i" class="policy-faq-card">
            <p class="faq-q"><span>Q.</span> {{ faq.q }}</p>
            <p class="faq-a">{{ faq.a }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const selected = ref(2026)
const years = [2026, 2025, 2024]

const policies = [
  { id:1, title:'新疆大学2026年普通本科招生章程', type:'章程', date:'2026-04-15', year:2026, content:'全面规范招生工作的纲领性文件，包括招生计划、录取规则、收费标准等' },
  { id:2, title:'新疆大学2026年艺术类专业招生简章', type:'简章', date:'2026-01-10', year:2026, content:'面向全国招收艺术类本科生，含美术、音乐、舞蹈等专业方向' },
  { id:3, title:'新疆大学2026年高水平运动队招生办法', type:'办法', date:'2026-02-20', year:2026, content:'招收足球、篮球、田径高水平运动员，降分录取' },
  { id:4, title:'新疆大学2026年"国家专项计划"招生简章', type:'简章', date:'2026-05-01', year:2026, content:'面向新疆及中西部地区农村和脱贫地区定向招生' },
  { id:5, title:'新疆大学2026年少数民族预科班招生办法', type:'办法', date:'2026-05-15', year:2026, content:'面向新疆少数民族考生，预科一年后转入本科学习' },
  { id:6, title:'新疆大学2025年录取分数线统计', type:'数据', date:'2025-08-01', year:2025, content:'2025年各省各批次录取最低分及位次汇总' },
  { id:7, title:'新疆大学2025年普通本科招生章程', type:'章程', date:'2025-04-10', year:2025, content:'2025年招生工作规范文件' },
  { id:8, title:'新疆大学2024年录取分数线统计', type:'数据', date:'2024-08-01', year:2024, content:'2024年各省各批次录取最低分及位次汇总' },
  { id:9, title:'新疆大学2024年普通本科招生章程', type:'章程', date:'2024-04-10', year:2024, content:'2024年招生工作规范文件' },
]

const filteredPolicies = computed(() => policies.filter(p=>p.year===selected))

const faqs = [
  { q:'新疆大学是一所什么样的大学？', a:'新疆大学是国家"双一流"建设高校、部省合建高校、国家"211工程"重点建设高校，是新疆办学规模最大、历史最悠久、学科门类最齐全的综合性大学。' },
  { q:'2026年招多少人？', a:'2026年新疆大学计划招收本科生约7500人，涵盖90余个本科专业。具体分省分专业计划以各省级招生考试机构公布的为准。' },
  { q:'录取分数线大概多少？', a:'新疆本地理科一本线约480分左右，文科约510分。外省录取分数线因省份不同差异较大，一般高于当地一本线20-60分。热门专业如计算机、软件工程等分数线更高。' },
  { q:'有少数民族加分政策吗？', a:'新疆大学严格执行国家和自治区少数民族招生政策。新疆区内少数民族考生在统考成绩总分基础上增加一定分值投档，具体加分政策以当年文件为准。' },
  { q:'宿舍条件怎么样？', a:'学校宿舍为4-6人间，配有独立卫生间、空调、热水器和宽带网络。每栋宿舍楼设有洗衣房、开水房等公共设施。新生统一安排住宿。' },
  { q:'学费多少？', a:'普通本科专业学费为3500-5000元/年，艺术类专业为8000元/年。住宿费为800-1200元/年。学校设有完善的奖助学金体系，困难学生可申请助学贷款。' },
]
</script>

<style scoped>
.policy-page { min-height:calc(100vh - 64px); }
.policy-hero { background:#A31F34; padding:60px 24px; text-align:center; color:#fff; position:relative; overflow:hidden; }
.policy-hero::before { content:''; position:absolute; inset:0; opacity:0.05; background-image:radial-gradient(#C9A96E 1px,transparent 1px); background-size:20px 20px; }
.policy-hero h1 { position:relative; font-size:40px; font-weight:800; margin:0 0 8px; }
.policy-hero p { position:relative; font-size:15px; opacity:0.8; margin:0; }
.policy-body { max-width:900px; margin:0 auto; padding:40px 24px; }

.policy-filter { background:#fff; border-radius:20px; border:1px solid #e2e8f0; padding:20px 24px; margin-bottom:32px; box-shadow:0 1px 3px rgba(0,0,0,0.04); }
.policy-filter-label { font-size:13px; color:#94a3b8; margin-bottom:12px; }
.policy-filter-chips { display:flex; gap:8px; }
.policy-chip { padding:8px 20px; border:1px solid #e2e8f0; border-radius:10px; background:#fff; font-size:14px; color:#64748b; cursor:pointer; transition:all 0.2s; }
.policy-chip:hover { border-color:#A31F34; color:#A31F34; }
.policy-chip.active { background:#A31F34; color:#fff; border-color:#A31F34; }

.policy-list { display:flex; flex-direction:column; margin-bottom:48px; }
.policy-item { display:flex; justify-content:space-between; align-items:center; padding:24px 28px; background:#fff; border:1px solid #f1f5f9; transition:background 0.2s; }
.policy-item:hover { background:#f8fafc; }
.policy-item:first-child { border-radius:16px 16px 0 0; }
.policy-item:last-child { border-radius:0 0 16px 16px; border-bottom-color:#e2e8f0; }
.policy-item + .policy-item { border-top:none; }
.policy-item-left { display:flex; align-items:center; gap:16px; }
.policy-icon { width:48px;height:48px;background:rgba(201,169,110,0.1);border-radius:14px;display:flex;align-items:center;justify-content:center;font-size:22px;flex-shrink:0; }
.policy-info h3 { font-size:16px; font-weight:700; color:#1e293b; margin:0 0 6px; }
.policy-meta { font-size:12px; color:#94a3b8; }
.policy-badge-red { display:inline; padding:2px 8px; background:rgba(163,31,52,0.08); color:#A31F34; border-radius:4px; font-size:11px; font-weight:600; }
.policy-badge-gold { display:inline; padding:2px 8px; background:rgba(201,169,110,0.1); color:#C9A96E; border-radius:4px; font-size:11px; font-weight:600; }
.policy-dl-btn { padding:10px 24px; border:1px solid #e2e8f0; border-radius:12px; background:#fff; color:#64748b; font-size:13px; cursor:pointer; transition:all 0.2s; }
.policy-dl-btn:hover { background:#A31F34; color:#fff; border-color:#A31F34; }

.policy-faq h2 { font-size:28px; font-weight:700; color:#1e293b; margin:0 0 24px; }
.policy-faq-grid { display:grid; grid-template-columns:1fr; gap:16px; }
@media (min-width:768px) { .policy-faq-grid { grid-template-columns:1fr 1fr; } }
.policy-faq-card { background:#fff; border:1px solid #e2e8f0; border-radius:14px; padding:20px 24px; }
.faq-q { font-size:14px; font-weight:700; color:#1e293b; margin:0 0 8px; }
.faq-q span { color:#A31F34; }
.faq-a { font-size:13px; color:#64748b; line-height:1.7; margin:0; padding-left:24px; }
</style>
