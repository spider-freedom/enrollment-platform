import { createI18n } from 'vue-i18n'
import { getTranslation } from './utils/translate'

const saved = localStorage.getItem('lang') || 'zh'

const i18n = createI18n({
  legacy: false,
  locale: saved,
  fallbackLocale: 'zh',
  messages: {},
  // 自定义：$t('任意中文文本') 在中文模式返回原文，其他语言返回翻译
  missingWarn: false,
  fallbackWarn: false,
})

// 全局 t 函数增强：支持直接用中文文本查询翻译
const originalT = i18n.global.t.bind(i18n.global)
i18n.global.t = function(key: string, ...args: any[]): string {
  const locale = i18n.global.locale.value as string
  if (locale === 'zh') return key
  const translated = getTranslation(key, locale)
  return translated || key
} as any

export default i18n

export const langOptions = [
  { value: 'zh', label: '中文' },
  { value: 'ug', label: 'ئۇيغۇرچە' },
  { value: 'kk', label: 'قازاقشا' },
]
