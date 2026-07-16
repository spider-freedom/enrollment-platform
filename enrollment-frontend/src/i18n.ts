import { createI18n } from 'vue-i18n'
import zh from './locales/zh.json'
import ug from './locales/ug.json'
import kk from './locales/kk.json'

const saved = localStorage.getItem('lang') || 'zh'

const i18n = createI18n({
  legacy: false,
  locale: saved,
  fallbackLocale: 'zh',
  messages: { zh, ug, kk },
})

export default i18n

export const langOptions = [
  { value: 'zh', label: '汉语' },
  { value: 'ug', label: 'ئۇيغۇرچە' },
  { value: 'kk', label: 'قازاقشا' },
]
