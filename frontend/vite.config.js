import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react({
    // 添加调试选项
    fastRefresh: true,
    include: '**/*.{jsx,tsx}',
    exclude: '**/node_modules/**'
  })],
  // 添加调试选项
  logLevel: 'info',
  clearScreen: false,
  server: {
    port: 5173,
    open: true,
    cors: true,
    hmr: {
      overlay: true
    }
  }
})