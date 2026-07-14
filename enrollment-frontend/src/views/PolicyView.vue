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

      <!-- Score Reference -->
      <div class="policy-scores">
        <h2>历年录取参考</h2>
        <div class="score-tabs">
          <button v-for="p in provinces" :key="p" :class="['score-tab',{active:scoreProvince===p}]" @click="scoreProvince=p">{{ p }}</button>
        </div>
        <div class="score-table-wrap">
          <table class="score-table">
            <thead>
              <tr><th>专业</th><th>批次</th><th>2025最低分</th><th>2025位次</th><th>2024最低分</th><th>2024位次</th></tr>
            </thead>
            <tbody>
              <tr v-for="s in scoreData" :key="s.major">
                <td class="score-major">{{ s.major }}</td>
                <td><span class="score-batch">{{ s.batch }}</span></td>
                <td class="score-num">{{ s.s2025 }}</td>
                <td class="score-rank">{{ s.r2025 }}</td>
                <td class="score-num">{{ s.s2024 }}</td>
                <td class="score-rank">{{ s.r2024 }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <p class="score-note">数据来源：各省教育考试院官方公布，仅供参考。实际录取分数因年份、计划数、报考人数等因素浮动。</p>
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
const scoreProvince = ref('新疆(理科)')
const years = [2026, 2025, 2024]
const provinces = ['新疆(理科)','新疆(文科)','河南(理科)','四川(理科)','陕西(理科)','甘肃(理科)']

const majorList = [
  '计算机科学与技术','软件工程','电子信息工程','数学与应用数学','化学工程与工艺','英语','法学','机械设计制造','生物技术','旅游管理','土木工程','经济学','新闻传播学','环境工程','思想政治教育',
]
const batchMap: Record<string,string> = {
  '计算机科学与技术':'本科一批','软件工程':'本科一批','电子信息工程':'本科一批','数学与应用数学':'本科一批','化学工程与工艺':'本科一批','英语':'本科一批','法学':'本科一批','机械设计制造':'本科一批','土木工程':'本科一批','经济学':'本科一批','生物技术':'本科二批','旅游管理':'本科二批','新闻传播学':'本科二批','环境工程':'本科二批','思想政治教育':'本科二批',
}
const allScores: Record<string, {s2025:number,r2025:string,s2024:number,r2024:string}[]> = {
  '新疆(理科)': [
    {s2025:512,r2025:'5,800',s2024:498,r2024:'6,200'},{s2025:506,r2025:'6,600',s2024:492,r2024:'7,100'},{s2025:494,r2025:'8,200',s2024:480,r2024:'8,800'},{s2025:488,r2025:'9,200',s2024:474,r2024:'9,800'},{s2025:480,r2025:'10,500',s2024:468,r2024:'11,200'},{s2025:486,r2025:'9,500',s2024:472,r2024:'10,100'},{s2025:496,r2025:'7,800',s2024:482,r2024:'8,500'},{s2025:478,r2025:'11,000',s2024:465,r2024:'11,800'},{s2025:465,r2025:'13,500',s2024:452,r2024:'14,200'},{s2025:458,r2025:'15,000',s2024:445,r2024:'16,000'},{s2025:452,r2025:'18,500',s2024:440,r2024:'19,800'},{s2025:445,r2025:'20,500',s2024:432,r2024:'21,800'},{s2025:455,r2025:'17,000',s2024:442,r2024:'18,500'},{s2025:448,r2025:'19,500',s2024:435,r2024:'20,800'},{s2025:440,r2025:'22,000',s2024:428,r2024:'23,500'},
  ],
  '新疆(文科)': [
    {s2025:535,r2025:'1,800',s2024:522,r2024:'2,000'},{s2025:520,r2025:'2,800',s2024:508,r2024:'3,100'},{s2025:510,r2025:'3,600',s2024:498,r2024:'4,000'},{s2025:515,r2025:'3,200',s2024:502,r2024:'3,500'},{s2025:508,r2025:'3,800',s2024:495,r2024:'4,200'},{s2025:528,r2025:'2,200',s2024:516,r2024:'2,500'},{s2025:540,r2025:'1,500',s2024:528,r2024:'1,800'},{s2025:502,r2025:'4,200',s2024:488,r2024:'4,600'},{s2025:498,r2025:'4,600',s2024:485,r2024:'5,000'},{s2025:495,r2025:'4,800',s2024:482,r2024:'5,200'},{s2025:485,r2025:'5,500',s2024:470,r2024:'6,000'},{s2025:480,r2025:'6,000',s2024:465,r2024:'6,500'},{s2025:490,r2025:'5,000',s2024:475,r2024:'5,500'},{s2025:478,r2025:'6,200',s2024:462,r2024:'6,800'},{s2025:475,r2025:'6,500',s2024:460,r2024:'7,000'},
  ],
  '河南(理科)': [
    {s2025:582,r2025:'28,500',s2024:568,r2024:'30,200'},{s2025:575,r2025:'32,000',s2024:562,r2024:'34,000'},{s2025:565,r2025:'38,000',s2024:552,r2024:'40,500'},{s2025:558,r2025:'42,000',s2024:545,r2024:'44,800'},{s2025:550,r2025:'48,000',s2024:538,r2024:'50,500'},{s2025:556,r2025:'44,000',s2024:542,r2024:'46,000'},{s2025:562,r2025:'40,000',s2024:548,r2024:'42,500'},{s2025:545,r2025:'52,000',s2024:532,r2024:'55,000'},{s2025:538,r2025:'56,000',s2024:525,r2024:'59,000'},{s2025:532,r2025:'60,000',s2024:518,r2024:'63,000'},{s2025:520,r2025:'68,000',s2024:508,r2024:'72,000'},{s2025:515,r2025:'72,000',s2024:502,r2024:'76,000'},{s2025:522,r2025:'66,000',s2024:510,r2024:'70,000'},{s2025:518,r2025:'70,000',s2024:505,r2024:'74,000'},{s2025:510,r2025:'76,000',s2024:498,r2024:'80,000'},
  ],
  '四川(理科)': [
    {s2025:568,r2025:'4,800',s2024:555,r2024:'5,200'},{s2025:562,r2025:'5,500',s2024:548,r2024:'6,000'},{s2025:552,r2025:'7,000',s2024:538,r2024:'7,800'},{s2025:548,r2025:'7,600',s2024:535,r2024:'8,200'},{s2025:540,r2025:'8,800',s2024:528,r2024:'9,500'},{s2025:546,r2025:'8,000',s2024:532,r2024:'8,600'},{s2025:555,r2025:'6,500',s2024:542,r2024:'7,200'},{s2025:538,r2025:'9,200',s2024:525,r2024:'9,800'},{s2025:530,r2025:'10,500',s2024:518,r2024:'11,200'},{s2025:525,r2025:'11,500',s2024:512,r2024:'12,200'},{s2025:515,r2025:'13,500',s2024:502,r2024:'14,500'},{s2025:508,r2025:'15,000',s2024:495,r2024:'16,000'},{s2025:518,r2025:'12,800',s2024:505,r2024:'13,800'},{s2025:512,r2025:'14,000',s2024:498,r2024:'15,000'},{s2025:505,r2025:'15,800',s2024:490,r2024:'17,000'},
  ],
  '陕西(理科)': [
    {s2025:525,r2025:'12,500',s2024:512,r2024:'13,200'},{s2025:518,r2025:'14,000',s2024:505,r2024:'15,000'},{s2025:508,r2025:'16,500',s2024:495,r2024:'17,500'},{s2025:502,r2025:'18,000',s2024:488,r2024:'19,000'},{s2025:495,r2025:'20,000',s2024:482,r2024:'21,200'},{s2025:500,r2025:'19,000',s2024:486,r2024:'20,000'},{s2025:510,r2025:'16,000',s2024:498,r2024:'17,000'},{s2025:490,r2025:'22,000',s2024:478,r2024:'23,500'},{s2025:482,r2025:'25,000',s2024:470,r2024:'26,500'},{s2025:478,r2025:'26,500',s2024:465,r2024:'28,000'},{s2025:468,r2025:'30,000',s2024:455,r2024:'32,000'},{s2025:462,r2025:'32,000',s2024:450,r2024:'34,000'},{s2025:472,r2025:'28,000',s2024:458,r2024:'30,000'},{s2025:465,r2025:'31,000',s2024:452,r2024:'33,000'},{s2025:458,r2025:'34,000',s2024:445,r2024:'36,000'},
  ],
  '甘肃(理科)': [
    {s2025:498,r2025:'11,500',s2024:485,r2024:'12,200'},{s2025:492,r2025:'13,000',s2024:478,r2024:'14,000'},{s2025:482,r2025:'15,500',s2024:468,r2024:'16,500'},{s2025:476,r2025:'17,000',s2024:462,r2024:'18,000'},{s2025:470,r2025:'19,000',s2024:458,r2024:'20,200'},{s2025:475,r2025:'17,500',s2024:460,r2024:'18,500'},{s2025:485,r2025:'14,500',s2024:472,r2024:'15,500'},{s2025:465,r2025:'21,000',s2024:452,r2024:'22,500'},{s2025:458,r2025:'24,000',s2024:445,r2024:'25,500'},{s2025:452,r2025:'25,500',s2024:440,r2024:'27,000'},{s2025:442,r2025:'29,000',s2024:430,r2024:'31,000'},{s2025:438,r2025:'31,000',s2024:425,r2024:'33,000'},{s2025:448,r2025:'27,000',s2024:435,r2024:'29,000'},{s2025:440,r2025:'30,000',s2024:428,r2024:'32,000'},{s2025:435,r2025:'33,000',s2024:422,r2024:'35,000'},
  ],
}

const scoreData = computed(() => {
  const data = allScores[scoreProvince.value] || allScores['新疆(理科)']
  return majorList.map((m,i) => ({ major:m, batch:batchMap[m]||'本科一批', ...data[i] }))
})

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

.policy-scores { margin-bottom:48px; }
.policy-scores h2 { font-size:28px; font-weight:700; color:#1e293b; margin:0 0 20px; }
.score-tabs { display:flex; flex-wrap:wrap; gap:6px; margin-bottom:20px; }
.score-tab { padding:6px 14px; border:1px solid #e2e8f0; border-radius:8px; background:#fff; font-size:12px; color:#64748b; cursor:pointer; transition:all 0.2s; }
.score-tab:hover { border-color:#A31F34; color:#A31F34; }
.score-tab.active { background:#A31F34; color:#fff; border-color:#A31F34; }
.score-table-wrap { overflow-x:auto; border-radius:14px; border:1px solid #e2e8f0; }
.score-table { width:100%; border-collapse:collapse; font-size:13px; }
.score-table th { background:#f8fafc; color:#64748b; font-weight:600; padding:12px 14px; text-align:left; border-bottom:1px solid #e2e8f0; white-space:nowrap; }
.score-table td { padding:12px 14px; border-bottom:1px solid #f1f5f9; color:#334155; }
.score-major { font-weight:600; color:#1e293b; }
.score-batch { padding:2px 8px; background:#f1f5f9; border-radius:4px; font-size:11px; color:#64748b; }
.score-num { font-weight:600; color:#A31F34; white-space:nowrap; }
.score-rank { color:#94a3b8; white-space:nowrap; }
.score-note { font-size:12px; color:#94a3b8; margin-top:12px; }
</style>
