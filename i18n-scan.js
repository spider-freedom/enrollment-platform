// 扫描 Vue 文件中的中文硬编码文本，生成 i18n key 并替换
const fs = require('fs')
const path = require('path')
const crypto = require('crypto')

const viewsDir = path.join(__dirname, 'enrollment-frontend/src/views')
const layoutsDir = path.join(__dirname, 'enrollment-frontend/src/layouts')
const zhFile = path.join(__dirname, 'enrollment-frontend/src/locales/zh.json')

// 读取现有中文 locale
const zh = JSON.parse(fs.readFileSync(zhFile, 'utf-8'))

// 生成短 key
function shortKey(text) {
  const hash = crypto.createHash('md5').update(text).digest('hex').substring(0, 6)
  return 't_' + hash
}

// 扫描并替换所有 .vue 文件
function processDir(dir) {
  const entries = fs.readdirSync(dir, { withFileTypes: true })
  for (const e of entries) {
    const full = path.join(dir, e.name)
    if (e.isDirectory()) processDir(full)
    else if (e.name.endsWith('.vue')) processFile(full)
  }
}

function processFile(file) {
  let content = fs.readFileSync(file, 'utf-8')
  let changed = false

  // 匹配 template 中的中文文本（不在 {{ }} 或 $t() 内的）
  const templateRegex = />([^<]*[一-鿿][^<]*)</g
  const textRegex = /[一-鿿]{2,}/

  content = content.replace(templateRegex, (match, text) => {
    if (text.includes('$t(') || text.includes('{{')) return match
    const trimmed = text.trim()
    if (!trimmed || trimmed.length < 2) return match

    // Check if text is mostly Chinese
    const cnCount = (trimmed.match(/[一-鿿]/g) || []).length
    if (cnCount < 2) return match

    const key = shortKey(trimmed)
    if (!zh[key]) zh[key] = trimmed
    changed = true
    return match.replace(trimmed, `{{ $t('${key}') }}`)
  })

  if (changed) {
    fs.writeFileSync(file, content, 'utf-8')
    console.log('Updated:', path.relative(__dirname, file))
  }
}

// 执行
processDir(viewsDir)
processDir(layoutsDir)

// 保存中文 locale
fs.writeFileSync(zhFile, JSON.stringify(zh, null, 2), 'utf-8')
console.log('\nDone. zh.json updated with', Object.keys(zh).length, 'keys')