import { createI18n } from 'vue-i18n'
import zh from '../locales/zh'
import ja from '../locales/ja'

const i18n = createI18n({
  legacy: false, // Vue 3 Composition API mode
  locale: localStorage.getItem('language') || 'zh-CN',
  fallbackLocale: 'zh-CN',
  messages: {
    'zh-CN': zh,
    'ja-JP': ja
  }
})

export default i18n
