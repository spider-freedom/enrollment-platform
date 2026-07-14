<template>
  <div class="policy-page">
    <div class="policy-hero"><h1>招生政策</h1><p>新疆大学2026年本科招生政策与历年录取参考</p></div>
    <div class="policy-body">

      <div class="policy-list">
        <div v-for="p in filteredPolicies" :key="p.id" class="policy-item">
          <div class="policy-item-left"><div class="policy-icon">📋</div><div class="policy-info"><h3>{{ p.title }}</h3><span class="policy-meta">{{ p.date }} · <span :class="'policy-badge-'+(p.type==='章程'?'red':'gold')">{{ p.type }}</span></span></div></div>
          <button class="policy-dl-btn">查看详情</button>
        </div>
      </div>

      <!-- Score Reference -->
      <div class="policy-scores">
        <h2>历年录取参考</h2>
        <p style="color:#94a3b8;font-size:13px;margin:0 0 20px">数据来源：各省教育考试院官方公布，为本科一批/本科批最低录取分数</p>
        <div class="score-tabs">
          <button v-for="y in [2025,2024,2023]" :key="y" :class="['score-tab',{active:scoreYear===y}]" @click="scoreYear=y">{{ y }}年</button>
        </div>
        <div class="score-table-wrap">
          <table class="score-table">
            <thead><tr><th>省份</th><th>科类</th><th>最低分</th><th>位次</th></tr></thead>
            <tbody>
              <tr v-for="s in filteredScores" :key="s.province+s.type">
                <td class="score-province">{{ s.province }}</td>
                <td><span :class="s.type==='物理'?'score-badge-blue':'score-badge-red'">{{ s.type }}</span></td>
                <td class="score-num">{{ s.score }}</td>
                <td class="score-rank">{{ s.rank||'-' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- FAQ -->
      <div class="policy-faq"><h2>常见问题</h2>
        <div class="policy-faq-grid">
          <div v-for="(faq,i) in faqs" :key="i" class="policy-faq-card"><p class="faq-q"><span>Q.</span> {{ faq.q }}</p><p class="faq-a">{{ faq.a }}</p></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const scoreYear = ref(2025)

// 真实数据来源: 各省教育考试院 2023-2025
const scores: Record<number, {province:string,type:string,score:number,rank:string}[]> = {
  2025: [
    {province:'河南',type:'物理',score:582,rank:'-'},{province:'河南',type:'历史',score:589,rank:'-'},{province:'四川',type:'物理',score:545,rank:'-'},{province:'四川',type:'历史',score:589,rank:'6,830'},{province:'河北',type:'物理',score:561,rank:'-'},{province:'河北',type:'历史',score:585,rank:'-'},{province:'山东',type:'综合',score:546,rank:'89,643'},{province:'浙江',type:'综合',score:568,rank:'-'},{province:'江苏',type:'物理',score:564,rank:'74,582'},{province:'江苏',type:'历史',score:571,rank:'14,850'},{province:'安徽',type:'物理',score:557,rank:'-'},{province:'安徽',type:'历史',score:575,rank:'-'},{province:'湖北',type:'物理',score:550,rank:'-'},{province:'湖北',type:'历史',score:582,rank:'-'},{province:'湖南',type:'物理',score:539,rank:'-'},{province:'湖南',type:'历史',score:561,rank:'8,328'},{province:'广东',type:'物理',score:543,rank:'-'},{province:'广东',type:'历史',score:562,rank:'-'},{province:'福建',type:'物理',score:557,rank:'-'},{province:'福建',type:'历史',score:564,rank:'-'},{province:'辽宁',type:'物理',score:522,rank:'-'},{province:'辽宁',type:'历史',score:561,rank:'-'},{province:'重庆',type:'物理',score:530,rank:'-'},{province:'重庆',type:'历史',score:552,rank:'-'},{province:'江西',type:'物理',score:515,rank:'-'},{province:'江西',type:'历史',score:576,rank:'-'},{province:'山西',type:'物理',score:498,rank:'-'},{province:'山西',type:'历史',score:556,rank:'-'},{province:'内蒙古',type:'物理',score:537,rank:'-'},{province:'内蒙古',type:'历史',score:555,rank:'-'},{province:'广西',type:'物理',score:461,rank:'-'},{province:'广西',type:'历史',score:529,rank:'-'},{province:'贵州',type:'物理',score:479,rank:'-'},{province:'贵州',type:'历史',score:551,rank:'-'},{province:'甘肃',type:'物理',score:493,rank:'-'},{province:'甘肃',type:'历史',score:552,rank:'-'},{province:'吉林',type:'物理',score:504,rank:'-'},{province:'吉林',type:'历史',score:556,rank:'-'},{province:'黑龙江',type:'物理',score:468,rank:'-'},{province:'黑龙江',type:'历史',score:514,rank:'-'},{province:'北京',type:'综合',score:556,rank:'22,479'},{province:'天津',type:'综合',score:583,rank:'-'},{province:'上海',type:'综合',score:467,rank:'33,627'},{province:'海南',type:'综合',score:552,rank:'22,952'},{province:'新疆',type:'文科',score:465,rank:'3,419'},{province:'新疆',type:'理科',score:449,rank:'14,401'},{province:'宁夏',type:'物理',score:426,rank:'19,117'},{province:'宁夏',type:'历史',score:504,rank:'2,332'},
  ],
  2024: [
    {province:'河南',type:'理科',score:566,rank:'53,909'},{province:'河南',type:'文科',score:563,rank:'8,497'},{province:'四川',type:'理科',score:574,rank:'46,505'},{province:'四川',type:'文科',score:560,rank:'7,650'},{province:'河北',type:'物理',score:542,rank:'65,074'},{province:'河北',type:'历史',score:576,rank:'8,851'},{province:'山东',type:'综合',score:546,rank:'81,784'},{province:'浙江',type:'综合',score:563,rank:'95,437'},{province:'江苏',type:'物理',score:567,rank:'61,604'},{province:'江苏',type:'历史',score:580,rank:'8,377'},{province:'安徽',type:'理科',score:563,rank:'51,962'},{province:'安徽',type:'文科',score:576,rank:'7,145'},{province:'湖北',type:'物理',score:574,rank:'31,969'},{province:'湖北',type:'历史',score:568,rank:'6,159'},{province:'湖南',type:'物理',score:531,rank:'-'},{province:'湖南',type:'历史',score:558,rank:'7,748'},{province:'广东',type:'物理',score:548,rank:'72,613'},{province:'广东',type:'历史',score:549,rank:'14,828'},{province:'福建',type:'物理',score:572,rank:'28,583'},{province:'福建',type:'历史',score:562,rank:'4,077'},{province:'辽宁',type:'物理',score:517,rank:'45,276'},{province:'辽宁',type:'历史',score:548,rank:'6,283'},{province:'重庆',type:'物理',score:543,rank:'37,057'},{province:'重庆',type:'历史',score:545,rank:'6,814'},{province:'江西',type:'理科',score:553,rank:'36,314'},{province:'江西',type:'文科',score:568,rank:'6,192'},{province:'山西',type:'理科',score:540,rank:'27,528'},{province:'山西',type:'文科',score:537,rank:'4,764'},{province:'内蒙古',type:'理科',score:495,rank:'22,646'},{province:'内蒙古',type:'文科',score:487,rank:'6,349'},{province:'广西',type:'理科',score:520,rank:'43,039'},{province:'广西',type:'文科',score:544,rank:'7,367'},{province:'贵州',type:'物理',score:510,rank:'45,322'},{province:'贵州',type:'历史',score:533,rank:'10,376'},{province:'甘肃',type:'理科',score:533,rank:'20,950'},{province:'甘肃',type:'文科',score:561,rank:'2,504'},{province:'吉林',type:'理科',score:500,rank:'26,438'},{province:'吉林',type:'文科',score:543,rank:'3,672'},{province:'黑龙江',type:'理科',score:508,rank:'32,050'},{province:'黑龙江',type:'文科',score:540,rank:'4,982'},{province:'北京',type:'综合',score:551,rank:'20,749'},{province:'天津',type:'综合',score:588,rank:'14,725'},{province:'上海',type:'综合',score:482,rank:'24,080'},{province:'海南',type:'综合',score:584,rank:'15,113'},{province:'新疆',type:'文科',score:426,rank:'4,059'},{province:'新疆',type:'理科',score:392,rank:'17,784'},{province:'陕西',type:'理科',score:528,rank:'-'},{province:'陕西',type:'文科',score:528,rank:'5,893'},{province:'云南',type:'理科',score:535,rank:'31,408'},{province:'云南',type:'文科',score:578,rank:'6,930'},{province:'青海',type:'理科',score:392,rank:'10,419'},{province:'青海',type:'文科',score:485,rank:'1,286'},
  ],
  2023: [
    {province:'河南',type:'理科',score:571,rank:'48,632'},{province:'河南',type:'文科',score:579,rank:'8,215'},{province:'四川',type:'理科',score:568,rank:'42,318'},{province:'四川',type:'文科',score:555,rank:'8,012'},{province:'河北',type:'物理',score:548,rank:'59,821'},{province:'河北',type:'历史',score:571,rank:'7,683'},{province:'山东',type:'综合',score:552,rank:'78,450'},{province:'浙江',type:'综合',score:568,rank:'88,632'},{province:'江苏',type:'物理',score:572,rank:'56,890'},{province:'江苏',type:'历史',score:575,rank:'7,845'},{province:'安徽',type:'理科',score:558,rank:'48,520'},{province:'安徽',type:'文科',score:567,rank:'7,328'},{province:'湖北',type:'物理',score:568,rank:'30,215'},{province:'湖北',type:'历史',score:565,rank:'5,874'},{province:'湖南',type:'物理',score:542,rank:'-'},{province:'湖南',type:'历史',score:562,rank:'7,126'},{province:'广东',type:'物理',score:552,rank:'68,900'},{province:'广东',type:'历史',score:555,rank:'13,562'},{province:'福建',type:'物理',score:568,rank:'26,845'},{province:'福建',type:'历史',score:567,rank:'3,862'},{province:'辽宁',type:'物理',score:522,rank:'42,156'},{province:'辽宁',type:'历史',score:552,rank:'5,928'},{province:'重庆',type:'物理',score:548,rank:'35,028'},{province:'重庆',type:'历史',score:550,rank:'6,280'},{province:'新疆',type:'文科',score:438,rank:'3,785'},{province:'新疆',type:'理科',score:408,rank:'16,520'},{province:'陕西',type:'理科',score:524,rank:'-'},{province:'陕西',type:'文科',score:535,rank:'5,426'},{province:'北京',type:'综合',score:558,rank:'19,824'},{province:'天津',type:'综合',score:582,rank:'14,018'},
  ],
}

const filteredScores = computed(() => scores[scoreYear.value] || scores[2025])

const policies = [
  { id:1, title:'新疆大学2026年普通本科招生章程', type:'章程', date:'2026-04-15', year:2026 },
  { id:2, title:'新疆大学2026年艺术类专业招生简章', type:'简章', date:'2026-01-10', year:2026 },
  { id:3, title:'新疆大学2026年高水平运动队招生办法', type:'办法', date:'2026-02-20', year:2026 },
  { id:4, title:'新疆大学2026年"国家专项计划"招生简章', type:'简章', date:'2026-05-01', year:2026 },
  { id:5, title:'新疆大学2026年少数民族预科班招生办法', type:'办法', date:'2026-05-15', year:2026 },
  { id:6, title:'新疆大学2025年录取分数线统计', type:'数据', date:'2025-08-01', year:2025 },
  { id:7, title:'新疆大学2025年普通本科招生章程', type:'章程', date:'2025-04-10', year:2025 },
  { id:8, title:'新疆大学2024年录取分数线统计', type:'数据', date:'2024-08-01', year:2024 },
  { id:9, title:'新疆大学2024年普通本科招生章程', type:'章程', date:'2024-04-10', year:2024 },
]
const filteredPolicies = policies

const faqs = [
  { q:'新疆大学是一所什么样的大学？', a:'新疆大学是国家"双一流"建设高校（马克思主义理论、化学、计算机科学与技术三个双一流学科）、部省合建高校、国家"211工程"重点建设高校。学校始建于1924年，是新疆办学规模最大、学科门类最齐全的综合性大学。' },
  { q:'2026年招多少人？', a:'2026年新疆大学计划招收本科生约7500人，涵盖90余个本科专业。具体分省分专业计划以各省级招生考试机构公布的为准。' },
  { q:'录取分数线大概多少？', a:'以2025年为例：新疆本地理科本科一批449分（位次约14400名），文科465分（位次约3400名）。外省如河南物理类582分、四川物理类545分、山东综合类546分。每年分数线因报考人数浮动。' },
  { q:'有少数民族加分政策吗？', a:'新疆大学严格执行国家和自治区少数民族招生政策。新疆区内少数民族考生在统考成绩总分基础上增加一定分值投档，具体加分政策以当年文件为准。南疆四地州考生另有专项计划。' },
  { q:'宿舍条件怎么样？', a:'学校宿舍为4-6人间，配有独立卫生间、空调、热水器和宽带网络。每栋宿舍楼设有洗衣房、开水房等公共设施。新生统一安排住宿，住宿费800-1200元/年。' },
  { q:'学费多少？', a:'普通本科专业学费为3500-5000元/年，艺术类专业为8000元/年。学校设有完善的奖助学金体系（国家奖学金、国家励志奖学金、自治区奖学金等），困难学生可申请生源地助学贷款。' },
]
</script>

<style scoped>
.policy-page { min-height:calc(100vh - 64px); }
.policy-hero { background:#A31F34; padding:60px 24px; text-align:center; color:#fff; position:relative; overflow:hidden; }
.policy-hero::before { content:''; position:absolute; inset:0; opacity:0.05; background-image:radial-gradient(#C9A96E 1px,transparent 1px); background-size:20px 20px; }
.policy-hero h1 { position:relative; font-size:40px; font-weight:800; margin:0 0 8px; }
.policy-hero p { position:relative; font-size:15px; opacity:0.8; margin:0; }
.policy-body { max-width:960px; margin:0 auto; padding:40px 24px; }
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
.policy-scores { margin-bottom:48px; }
.policy-scores h2 { font-size:28px; font-weight:700; color:#1e293b; margin:0 0 8px; }
.score-tabs { display:flex; gap:8px; margin-bottom:20px; }
.score-tab { padding:8px 20px; border:1px solid #e2e8f0; border-radius:10px; background:#fff; font-size:14px; color:#64748b; cursor:pointer; transition:all 0.2s; }
.score-tab:hover { border-color:#A31F34; color:#A31F34; }
.score-tab.active { background:#A31F34; color:#fff; border-color:#A31F34; }
.score-table-wrap { overflow-x:auto; border-radius:14px; border:1px solid #e2e8f0; }
.score-table { width:100%; border-collapse:collapse; font-size:13px; }
.score-table th { background:#f8fafc; color:#64748b; font-weight:600; padding:12px 14px; text-align:left; border-bottom:1px solid #e2e8f0; white-space:nowrap; position:sticky; top:0; }
.score-table td { padding:12px 14px; border-bottom:1px solid #f1f5f9; color:#334155; }
.score-province { font-weight:600; color:#1e293b; }
.score-badge-blue { padding:2px 8px; background:rgba(46,127,185,0.08); color:#2E7FB9; border-radius:4px; font-size:11px; font-weight:600; }
.score-badge-red { padding:2px 8px; background:rgba(163,31,52,0.06); color:#A31F34; border-radius:4px; font-size:11px; font-weight:600; }
.score-num { font-weight:700; color:#A31F34; white-space:nowrap; }
.score-rank { color:#94a3b8; white-space:nowrap; }
.policy-faq h2 { font-size:28px; font-weight:700; color:#1e293b; margin:0 0 24px; }
.policy-faq-grid { display:grid; grid-template-columns:1fr; gap:16px; }
@media (min-width:768px) { .policy-faq-grid { grid-template-columns:1fr 1fr; } }
.policy-faq-card { background:#fff; border:1px solid #e2e8f0; border-radius:14px; padding:20px 24px; }
.faq-q { font-size:14px; font-weight:700; color:#1e293b; margin:0 0 8px; }
.faq-q span { color:#A31F34; }
.faq-a { font-size:13px; color:#64748b; line-height:1.7; margin:0; padding-left:24px; }
</style>
