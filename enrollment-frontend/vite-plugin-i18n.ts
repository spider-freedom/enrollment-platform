// Vite 插件：编译时自动将 Vue 模板中的中文字符串包裹为 $t()
import type { Plugin } from 'vite'

export function i18nAutoPlugin(): Plugin {
  return {
    name: 'i18n-auto',
    transform(code: string, id: string) {
      if (!id.endsWith('.vue')) return null

      // 只处理 template 部分（<template> ... </template>）
      const templateMatch = code.match(/<template[^>]*>([\s\S]*)<\/template>/)
      if (!templateMatch) return null

      let template = templateMatch[1]
      let modified = false

      // 替换纯文本节点中的中文： >中文< → >{{ $t('中文') }}<
      template = template.replace(/>([^<>{]*[一-鿿][^<>{]*)</g, (match, text) => {
        const trimmed = text.trim()
        if (!trimmed || trimmed.length < 2) return match
        // Skip if already $t(), {{ }}, or pure English/numbers
        if (text.includes('$t(') || text.includes('{{') || !/[一-鿿]/.test(text)) return match
        modified = true
        return `>{{ $t('${trimmed}') }}<`
      })

      // 替换属性中的中文 placeholder="中文" → :placeholder="$t('中文')"
      template = template.replace(/ placeholder="([^"]*[一-鿿][^"]*)"/g, (match, text) => {
        modified = true
        return ` :placeholder="$t('${text}')"`
      })
      template = template.replace(/ label="([^"]*[一-鿿][^"]*)"/g, (match, text) => {
        modified = true
        return ` :label="$t('${text}')"`
      })

      if (!modified) return null

      const result = code.replace(templateMatch[1], template)
      return { code: result, map: null }
    },
  }
}
