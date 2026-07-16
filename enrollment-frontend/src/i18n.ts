import { createI18n } from 'vue-i18n'
import { getTranslation } from './utils/translate'

const saved = localStorage.getItem('lang') || 'zh'

const i18n = createI18n({
  legacy: false,
  locale: saved,
  fallbackLocale: 'zh',
  missingWarn: false,
  fallbackWarn: false,
})

// Override $t: if locale is zh, return text as-is
// For other locales, look up translation from the mapping
const origT = i18n.global.t
i18n.global.t = ((key: string, ...args: any[]): string => {
  const loc = (i18n.global.locale as any).value || 'zh'
  if (loc === 'zh') return key
  const tr = getTranslation(key, loc)
  return tr || key
}) as any

export default i18n

export const langOptions = [
  { value: 'zh', label: '汉语' },
  { value: 'ug', label: 'ئۇيغۇرچە' },
  { value: 'kk', label: 'قازاقشا' },
]
