import axios from 'axios'

const buildAuthHeader = (token: string): string =>
  token.startsWith('Bearer ') ? token : `Bearer ${token}`

export const CACHE_CONFIG = {
  DEFAULT_EXPIRY: 30 * 60 * 1000,
  CLEANUP_INTERVAL: 60 * 60 * 1000,
} as const

export const cleanExpiredCache = (): void => {
  try {
    const now = Date.now()
    const keys = Object.keys(localStorage)
    const keysToRemove = new Set<string>()

    keys.forEach((key) => {
      if (!key.endsWith('Timestamp')) return

      const timestamp = localStorage.getItem(key)
      if (!timestamp) return

      const cachedTime = Number.parseInt(timestamp, 10)
      const expiryKey = key.replace('Timestamp', 'Expiry')
      const expiry = Number.parseInt(
        localStorage.getItem(expiryKey) || String(CACHE_CONFIG.DEFAULT_EXPIRY),
        10,
      )

      if (now - cachedTime > expiry) {
        const dataKey = key.replace('Timestamp', '')
        keysToRemove.add(dataKey)
        keysToRemove.add(key)
        keysToRemove.add(expiryKey)
      }
    })

    keysToRemove.forEach((key) => localStorage.removeItem(key))
  } catch (error) {
    console.error('清理缓存失败:', error)
  }
}

cleanExpiredCache()
setInterval(cleanExpiredCache, CACHE_CONFIG.CLEANUP_INTERVAL)

class CacheManager {
  static set(key: string, data: unknown, expiry = CACHE_CONFIG.DEFAULT_EXPIRY): void {
    try {
      const now = Date.now()
      localStorage.setItem(key, JSON.stringify(data))
      localStorage.setItem(`${key}Timestamp`, String(now))
      localStorage.setItem(`${key}Expiry`, String(expiry))
    } catch (error) {
      console.error('存储缓存失败:', error)
    }
  }

  static get<T = unknown>(key: string): T | null {
    try {
      const now = Date.now()
      const timestamp = localStorage.getItem(`${key}Timestamp`)
      if (!timestamp) return null

      const cachedTime = Number.parseInt(timestamp, 10)
      const expiry = Number.parseInt(
        localStorage.getItem(`${key}Expiry`) || String(CACHE_CONFIG.DEFAULT_EXPIRY),
        10,
      )

      if (now - cachedTime > expiry) {
        this.remove(key)
        return null
      }

      const data = localStorage.getItem(key)
      return data ? (JSON.parse(data) as T) : null
    } catch (error) {
      console.error('读取缓存失败:', error)
      return null
    }
  }

  static remove(key: string): void {
    localStorage.removeItem(key)
    localStorage.removeItem(`${key}Timestamp`)
    localStorage.removeItem(`${key}Expiry`)
  }

  static clear(): void {
    const keys = Object.keys(localStorage)
    keys.forEach((key) => {
      if (key.endsWith('Timestamp') || key.endsWith('Expiry')) {
        localStorage.removeItem(key)
        if (key.endsWith('Timestamp')) {
          localStorage.removeItem(key.replace('Timestamp', ''))
        }
      }
    })
  }

  static clearByPrefix(prefix: string): void {
    Object.keys(localStorage).forEach((key) => {
      if (key.startsWith(prefix)) localStorage.removeItem(key)
    })
  }

  static clearAllApiCache(): void {
    Object.keys(localStorage).forEach((key) => {
      if (key.startsWith('api_cache_') || key.endsWith('Timestamp') || key.endsWith('Expiry')) {
        localStorage.removeItem(key)
      }
    })
  }
}

const api = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
    'Cache-Control': 'no-cache, no-store, must-revalidate',
    Pragma: 'no-cache',
    Expires: '0',
  },
})

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = buildAuthHeader(token)
    }

    const cfg = config as typeof config & { retryCount?: number }
    if (!cfg.retryCount) cfg.retryCount = 0

    return config
  },
  (error) => Promise.reject(error),
)

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config as (typeof error.config & { retryCount?: number }) | undefined

    if (error.response?.data?.returnCode === 'ERR0007') {
      localStorage.removeItem('token')
      if (window.location.pathname !== '/login') window.location.href = '/login'
      return Promise.reject(error)
    }

    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      if (window.location.pathname !== '/login') window.location.href = '/login'
      return Promise.reject(error)
    }

    if ((error.code === 'ECONNABORTED' || !error.response) && originalRequest) {
      originalRequest.retryCount = originalRequest.retryCount || 0
      if (originalRequest.retryCount < 3) {
        originalRequest.retryCount += 1
        const url = String(originalRequest.url || '')
        const maskedUrl = url.replace(/\/api\/[^/]+\/[^/]+\/[^/]+/, '/api/****/****/****')
        console.log(`请求重试 (${originalRequest.retryCount}/3): ${maskedUrl}`)

        const delay = 2 ** originalRequest.retryCount * 1000
        await new Promise((resolve) => setTimeout(resolve, delay))
        return api(originalRequest)
      }
    }

    console.error('API Error:', error.response ? error.response.data : error.message)
    return Promise.reject(error)
  },
)

export default api
export { CacheManager }
