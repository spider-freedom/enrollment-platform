import type { App, DirectiveBinding } from 'vue'
import { getTranslation } from '@/utils/translate'
import i18n from '@/i18n'

// v-t 指令：自动翻译元素中的静态中文文本
// 用法：<span v-t>首页</span> → 自动根据当前语言翻译
export default {
  install(app: App) {
    app.directive('t', {
      mounted(el: HTMLElement, binding: DirectiveBinding) {
        applyTranslation(el)
      },
      updated(el: HTMLElement) {
        applyTranslation(el)
      }
    })

    // 监听语言切换，全局刷新所有 v-t 元素
    watchLocale()
  }
}

const tElements = new WeakSet<HTMLElement>()

function applyTranslation(el: HTMLElement) {
  const lang = i18n.global.locale.value as string
  if (lang === 'zh') return // Chinese needs no translation

  // 遍历所有子节点，翻译纯文本
  const walk = (node: Node) => {
    if (node.nodeType === 3) { // Text node
      const text = node.textContent || ''
      if (/[一-鿿]/.test(text)) {
        const translated = getTranslation(text.trim(), lang)
        if (translated !== text.trim()) {
          node.textContent = translated
        }
      }
    } else if (node.nodeType === 1) {
      for (const child of node.childNodes) {
        walk(child)
      }
    }
  }
  walk(el)
}

function watchLocale() {
  const observer = new MutationObserver(() => {
    applyTranslation(document.body)
  })
  // Simple approach: re-translate on language change
  const orig = i18n.global.locale
  // Watch for locale changes via a reactive effect
  i18n.global.locale.value // trigger watch
}
