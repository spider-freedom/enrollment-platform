<template>
  <div class="majors-page">
    <div class="majors-header">
      <h1>{{ $t("专业查询") }}</h1>
      <p>{{ $t("新疆大学开设90+本科专业，涵盖理、工、文、法、经、管等多个学科门类") }}</p>
    </div>

    <!-- Filter Bar -->
    <div class="majors-filter">
      <div class="majors-search">
        <span class="majors-search-icon">🔍</span>
        <input v-model="search" placeholder="搜索专业名称..." class="majors-search-input" />
      </div>
      <div class="majors-chips">
        <button v-for="c in colleges" :key="c" :class="['majors-chip',{active:selected===c}]" @click="selected=selected===c?'':c">{{ c }}</button>
      </div>
    </div>

    <!-- Cards Grid -->
    <div class="majors-grid">
      <div v-for="m in filteredMajors" :key="m.name" class="majors-card">
        <div class="majors-card-top">
          <div class="majors-card-icon">📚</div>
          <span class="majors-card-college">{{ m.college }}</span>
        </div>
        <h3>{{ m.name }}</h3>
        <p>{{ m.desc }}</p>
        <div style="display:flex;flex-wrap:wrap;gap:6px;margin-bottom:16px">
          <span v-for="t in (m.tags||[])" :key="t" class="majors-tag">{{ t }}</span>
        </div>
        <button class="majors-card-btn" @click="selectedMajor=m">{{ $t("查看详情") }}</button>
      </div>
      <div v-if="filteredMajors.length===0" style="grid-column:1/-1;text-align:center;padding:64px 0;color:#94a3b8">
        <div style="font-size:48px;margin-bottom:16px">📖</div>
        <p style="font-size:15px">{{ $t("没有找到匹配的专业") }}</p>
      </div>
    </div>

    <!-- Detail Modal -->
    <div v-if="selectedMajor" class="modal-overlay" @click.self="selectedMajor=null">
      <div class="modal-card">
        <h2>{{ selectedMajor.name }}</h2>
        <span class="modal-college">{{ selectedMajor.college }}</span>
        <p class="modal-desc">{{ selectedMajor.desc }}</p>
        <div class="modal-tags">
          <span v-for="t in (selectedMajor.tags||[])" :key="t" class="majors-tag">{{ t }}</span>
        </div>
        <div class="modal-info">
          <div><b>{{ $t("学制") }}</b>{{ $t("：4年") }}</div>
          <div><b>{{ $t("学位") }}</b>{{ $t("：学士") }}</div>
          <div><b>{{ $t("招生类别") }}</b>{{ $t("：理工类") }}</div>
        </div>
        <button class="modal-close-btn" @click="selectedMajor=null">{{ $t("关闭") }}</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const search = ref('')
const selected = ref('')
const selectedMajor = ref<any>(null)

const colleges = ['全部','计算机科学与技术学院','数学与系统科学学院','信息科学与工程学院','化学化工学院','生命科学与技术学院','物理科学与技术学院','外国语学院','马克思主义学院']

const majors = [
  { name:'软件工程', college:'计算机科学与技术学院', desc:'培养具备软件设计、开发、测试和维护能力的高级工程技术人才', tags:['Java','Python','AI','Web开发'] },
  { name:'计算机科学与技术', college:'计算机科学与技术学院', desc:'研究计算机系统结构、算法设计与分析、人工智能等核心领域', tags:['算法','操作系统','数据库','网络'] },
  { name:'数学与应用数学', college:'数学与系统科学学院', desc:'培养具有扎实数学基础和建模能力的复合型人才', tags:['数学分析','高等代数','概率论','建模'] },
  { name:'统计学', college:'数学与系统科学学院', desc:'培养数据分析、统计建模和数据挖掘专业人才', tags:['数据科学','R语言','机器学习','可视化'] },
  { name:'电子信息工程', college:'信息科学与工程学院', desc:'研究电子技术、通信系统和信号处理', tags:['电路设计','嵌入式','信号处理','5G'] },
  { name:'应用化学', college:'化学化工学院', desc:'培养化学合成、分析和应用研发能力的专业人才', tags:['有机化学','分析化学','材料','催化剂'] },
  { name:'生物技术', college:'生命科学与技术学院', desc:'研究生物工程、基因编辑和生物制药前沿领域', tags:['分子生物','基因工程','发酵','药物'] },
  { name:'物理学', college:'物理科学与技术学院', desc:'研究物质结构、相互作用和运动规律的基础学科', tags:['量子力学','凝聚态','光学','天体物理'] },
  { name:'英语', college:'外国语学院', desc:'培养英语语言能力与跨文化交际能力的专业人才', tags:['翻译','口译','语言学','英美文学'] },
  { name:'思想政治教育', college:'马克思主义学院', desc:'培养马克思主义理论素养和政治教育能力的专门人才', tags:['哲学','政治学','法学','社会学'] },
]

const filteredMajors = computed(() => {
  let list = majors
  if (search.value) { const kw=search.value.toLowerCase(); list=list.filter(m=>m.name.toLowerCase().includes(kw)||m.college.toLowerCase().includes(kw)) }
  if (selected.value && selected.value!=='全部') { list=list.filter(m=>m.college===selected.value) }
  return list
})
</script>

<style scoped>
.majors-page { max-width:1100px; margin:0 auto; padding:48px 24px; }
.majors-header { text-align:center; margin-bottom:40px; padding:80px 24px; margin:-48px -24px 40px;
  background:linear-gradient(180deg,rgba(0,0,0,0.5),rgba(0,0,0,0.35)),url('/images/campus-classroom.jpg') center/cover no-repeat; }
.majors-header h1 { font-size:36px; font-weight:800; color:#fff; margin:0 0 8px; text-shadow:0 2px 8px rgba(0,0,0,0.3); }
.majors-header p { font-size:15px; color:rgba(255,255,255,0.85); margin:0; }

.majors-filter { background:#fff; padding:24px 32px; border-radius:16px; border:1px solid #e2e8f0; box-shadow:0 1px 3px rgba(0,0,0,0.04); margin-bottom:32px; }
.majors-search { position:relative; margin-bottom:20px; }
.majors-search-icon { position:absolute; left:14px; top:50%; transform:translateY(-50%); font-size:16px; }
.majors-search-input { width:100%; padding:12px 16px 12px 44px; border:1px solid #e2e8f0; border-radius:12px; font-size:14px; background:#f8fafc; outline:none; box-sizing:border-box; transition:border-color 0.2s,box-shadow 0.2s; }
.majors-search-input:focus { border-color:#A31F34; box-shadow:0 0 0 3px rgba(163,31,52,0.1); }
.majors-chips { display:flex; flex-wrap:wrap; gap:8px; }
.majors-chip { padding:6px 16px; border-radius:9999px; border:1px solid #e2e8f0; background:#fff; color:#64748b; font-size:13px; cursor:pointer; transition:all 0.2s; }
.majors-chip:hover { border-color:#A31F34; color:#A31F34; }
.majors-chip.active { background:#A31F34; color:#fff; border-color:#A31F34; box-shadow:0 2px 8px rgba(163,31,52,0.2); }

.majors-grid { display:grid; grid-template-columns:1fr; gap:24px; }
@media (min-width:640px) { .majors-grid { grid-template-columns:repeat(2,1fr); } }
@media (min-width:960px) { .majors-grid { grid-template-columns:repeat(3,1fr); } }
.majors-card { background:#fff; padding:32px; border-radius:20px; border:1px solid #e2e8f0; box-shadow:0 1px 3px rgba(0,0,0,0.04); transition:box-shadow 0.3s,transform 0.3s; }
.majors-card:hover { box-shadow:0 12px 32px rgba(0,0,0,0.08); transform:translateY(-2px); }
.majors-card-top { display:flex; justify-content:space-between; align-items:center; margin-bottom:12px; }
.majors-card-icon { width:48px;height:48px;background:rgba(163,31,52,0.08);border-radius:14px;display:flex;align-items:center;justify-content:center;font-size:22px; }
.majors-card-college { padding:4px 10px;background:rgba(201,169,110,0.1);color:#C9A96E;border-radius:9999px;font-size:11px;font-weight:600; }
.majors-card h3 { font-size:20px;font-weight:700;color:#1e293b;margin:0 0 8px; }
.majors-card p { font-size:13px;color:#64748b;line-height:1.7;margin:0 0 12px; }
.majors-tag { font-size:10px;padding:4px 8px;background:#f8fafc;color:#64748b;border-radius:6px;border:1px solid #f1f5f9; }
.majors-card-btn { width:100%;padding:12px;border:1px solid #e2e8f0;border-radius:12px;background:#fff;color:#475569;font-size:14px;cursor:pointer;transition:all 0.2s; }
.majors-card-btn:hover { background:#A31F34;color:#fff;border-color:#A31F34; }

.modal-overlay { position:fixed;inset:0;background:rgba(0,0,0,0.35);z-index:100;display:flex;align-items:center;justify-content:center;padding:24px; }
.modal-card { background:#fff;border-radius:20px;padding:40px;max-width:480px;width:100%;text-align:center; }
.modal-card h2 { font-size:22px;font-weight:700;color:#1e293b;margin:0 0 8px; }
.modal-college { display:inline-block;padding:4px 12px;background:rgba(201,169,110,0.1);color:#C9A96E;border-radius:9999px;font-size:12px;font-weight:600;margin-bottom:16px; }
.modal-desc { font-size:14px;color:#64748b;line-height:1.7;margin:0 0 20px; }
.modal-tags { display:flex;flex-wrap:wrap;gap:6px;justify-content:center;margin-bottom:20px; }
.modal-info { display:grid;grid-template-columns:1fr 1fr 1fr;gap:12px;margin-bottom:24px; }
.modal-info div { padding:12px;background:#f8fafc;border-radius:10px;font-size:13px;color:#475569; }
.modal-close-btn { padding:10px 36px;background:#A31F34;color:#fff;border:none;border-radius:12px;font-size:14px;cursor:pointer; }
</style>
