const { defineConfig } = require('cypress')
const path = require('path')
const os = require('os')
const fs = require('fs')

// 创建一个在允许路径内的临时缓存目录
const tempCacheDir = path.join(os.tmpdir(), 'cypress-cache')
if (!fs.existsSync(tempCacheDir)) {
  fs.mkdirSync(tempCacheDir, { recursive: true })
}

module.exports = defineConfig({
  e2e: {
    baseUrl: 'http://localhost:5173',
    setupNodeEvents(_on, _config) {
      // implement node event listeners here
    },
  },
  component: {
    devServer: {
      framework: 'react',
      bundler: 'vite',
    },
  },
  cacheFolder: tempCacheDir,
})
