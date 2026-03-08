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
  build: {
    chunkSizeWarningLimit: 900,
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes('node_modules')) return

          const normalized = id.replace(/\\/g, '/')
          if (
            normalized.includes('/react/') ||
            normalized.includes('/react-dom/') ||
            normalized.includes('/scheduler/')
          ) return 'vendor-react'

          if (
            normalized.includes('/react-router/') ||
            normalized.includes('/react-router-dom/') ||
            normalized.includes('/@remix-run/router/')
          ) return 'vendor-router'

          if (normalized.includes('/axios/')) return 'vendor-axios'
        }
      }
    }
  },
  server: {
    port: 5173,
    open: true,
    cors: true,
    hmr: {
      overlay: true
    }
  }
})
