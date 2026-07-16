<template>
  <div class="ai-page">
    <div class="ai-container">
      <!-- Header -->
      <div class="ai-header">
        <div class="ai-header-left">
          <div class="ai-header-icon">X</div>
          <div>
            <h2>新大招生 AI 数字人</h2>
            <span class="ai-online">● 在线咨询中</span>
          </div>
        </div>
      </div>

      <!-- Chat Area -->
      <div class="ai-chat" ref="chatRef">
        <div v-for="(m,i) in messages" :key="i" :class="['ai-msg', m.role==='user' ? 'ai-msg-user' : 'ai-msg-bot']">
          <div :class="['ai-bubble', m.role==='user' ? 'ai-bubble-user' : 'ai-bubble-bot']">{{ m.text }}</div>
          <div class="ai-time">{{ m.time }}</div>
        </div>
        <div v-if="typing" class="ai-msg ai-msg-bot">
          <div class="ai-bubble ai-bubble-bot">
            <span class="ai-dot">●</span><span class="ai-dot">●</span><span class="ai-dot">●</span>
          </div>
        </div>
      </div>

      <!-- Suggestions -->
      <div class="ai-suggestions">
        <button v-for="s in suggestions" :key="s" class="ai-sug-btn" @click="sendMsg(s)">{{ s }}</button>
      </div>

      <!-- Input -->
      <div class="ai-input-area">
        <input v-model="input" class="ai-input" placeholder="输入你的问题..." @keyup.enter="sendMsg(input)" />
        <button class="ai-send-btn" @click="sendMsg(input)">发送</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { aiApi } from '@/api'
import { ElMessage } from 'element-plus'

const input = ref('')
const typing = ref(false)
const chatRef = ref<any>(null)
const messages = ref<{role:string,text:string,time:string}[]>([
  {role:'bot',text:'你好！我是新大招生AI助手，有什么可以帮你的吗？',time:'刚刚'},
])

const suggestions = ['今年录取分数线是多少','双一流学科有哪些','校园开放日什么时候','宿舍条件怎么样']

async function sendMsg(text: string) {
  if (!text.trim()) return
  const t = text.trim()
  messages.value.push({role:'user',text:t,time:'刚刚'})
  messages.value.push({role:'bot',text:'',time:'刚刚'})
  input.value = ''
  typing.value = true
  scrollDown()
  const botIdx = messages.value.length - 1
  try {
    const resp = await fetch('/api/ai/chat/stream', {
      method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify({question:t}),
    })
    const reader = resp.body?.getReader()
    if (!reader) throw new Error('no reader')
    const decoder = new TextDecoder()
    let buffer = ''
    while (true) {
      const {done, value} = await reader.read()
      if (done) break
      buffer += decoder.decode(value, {stream:true})
      // Parse SSE data lines
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''
      for (const line of lines) {
        if (line.startsWith('data:')) {
          const chunk = line.substring(5).trim()
          messages.value[botIdx].text += chunk
          scrollDown()
        }
      }
    }
  } catch {
    if (!messages.value[botIdx].text) {
      messages.value[botIdx].text = '抱歉，AI服务暂时不可用，请稍后重试。'
    }
  } finally {
    typing.value = false
    scrollDown()
  }
}

function scrollDown() {
  nextTick(() => {
    if (chatRef.value) chatRef.value.scrollTop = chatRef.value.scrollHeight
  })
}
</script>

<style scoped>
.ai-page { min-height:calc(100vh - 64px); display:flex; align-items:center; justify-content:center; padding:24px;
  background: linear-gradient(135deg, rgba(10,20,40,0.7), rgba(30,60,100,0.5)), url('/images/campus-library.jpg') center/cover no-repeat fixed; }
.ai-container { width:100%; max-width:800px; height:calc(100vh - 160px); background:linear-gradient(180deg,#fdfbf7 0%,#f8f6f0 100%); border-radius:24px; border:1px solid #e8e0d0; box-shadow:0 12px 48px rgba(0,0,0,0.15); display:flex; flex-direction:column; overflow:hidden; }

.ai-header { padding:18px 24px; background:linear-gradient(135deg,#1a1a2e,#2d2d44); color:#fff; display:flex; align-items:center; }
.ai-header-left { display:flex; align-items:center; gap:12px; }
.ai-header-icon { width:38px;height:38px;background:linear-gradient(135deg,#A31F34,#c23d4f);border-radius:12px;display:flex;align-items:center;justify-content:center;color:#fff;font-weight:800;font-size:18px; }
.ai-header h2 { font-size:16px;font-weight:700;margin:0; }
.ai-online { font-size:11px;color:#10b981; }

.ai-chat { flex:1;overflow-y:auto;padding:24px;background:linear-gradient(180deg,#faf8f4 0%,#f5f0e8 100%);display:flex;flex-direction:column;gap:16px; }
.ai-msg { max-width:82%; }
.ai-msg-user { align-self:flex-end; }
.ai-msg-bot { align-self:flex-start; }
.ai-bubble { padding:14px 18px;border-radius:18px;font-size:14px;line-height:1.7; }
.ai-bubble-user { background:linear-gradient(135deg,#1a56db,#2563eb);color:#fff;border-bottom-right-radius:6px;box-shadow:0 2px 8px rgba(37,99,235,0.2); }
.ai-bubble-bot { background:#fff;color:#334155;border:1px solid #e8e0d0;box-shadow:0 2px 8px rgba(0,0,0,0.04);border-bottom-left-radius:6px;white-space:pre-wrap;word-break:break-word; }
.ai-time { font-size:10px;color:#94a3b8;margin-top:4px; }
.ai-msg-user .ai-time { text-align:right; }

.ai-dot { display:inline-block;animation:bounce 1.4s infinite;font-size:8px;margin:0 2px;color:#94a3b8; }
.ai-dot:nth-child(2) { animation-delay:0.2s; }
.ai-dot:nth-child(3) { animation-delay:0.4s; }
@keyframes bounce { 0%,80%,100% { opacity:0.2; } 40% { opacity:1; } }

.ai-suggestions { padding:10px 24px;background:linear-gradient(180deg,#fdfbf7,#f8f5ef);border-top:1px solid #ede6d8;display:flex;flex-wrap:wrap;gap:8px;overflow-x:auto; }
.ai-sug-btn { padding:7px 16px;background:#fff;border:1px solid #d4c9b0;border-radius:20px;font-size:12px;color:#6b5e4a;cursor:pointer;white-space:nowrap;transition:all 0.2s; }
.ai-sug-btn:hover { border-color:#A31F34;color:#A31F34;background:#fdf5f5; }

.ai-input-area { padding:14px 24px;border-top:1px solid #ede6d8;background:#fff;display:flex;gap:12px; }
.ai-input { flex:1;padding:12px 18px;border:2px solid #e8e0d0;border-radius:16px;font-size:14px;background:#fdfbf7;outline:none;transition:border-color 0.3s; }
.ai-input:focus { border-color:#A31F34;box-shadow:0 0 0 4px rgba(163,31,52,0.06); }
.ai-send-btn { padding:12px 28px;background:linear-gradient(135deg,#A31F34,#c23d4f);color:#fff;border:none;border-radius:14px;font-size:14px;font-weight:600;cursor:pointer;box-shadow:0 4px 12px rgba(163,31,52,0.25);transition:all 0.2s; }
.ai-send-btn:hover { transform:translateY(-1px);box-shadow:0 6px 16px rgba(163,31,52,0.35); }
</style>
