import axios from 'axios';

// 缓存清理函数
const cleanExpiredCache = () => {
  try {
    const now = Date.now();
    const cacheExpiry = 24 * 60 * 60 * 1000; // 24小时缓存
    
    // 获取所有localStorage键
    const keys = Object.keys(localStorage);
    
    // 清理过期的缓存数据
    keys.forEach(key => {
      // 检查是否是缓存数据的时间戳键
      if (key.endsWith('Timestamp')) {
        const timestamp = localStorage.getItem(key);
        if (timestamp) {
          const cachedTime = parseInt(timestamp);
          if (now - cachedTime > cacheExpiry) {
            // 清理对应的缓存数据
            const dataKey = key.replace('Timestamp', '');
            localStorage.removeItem(dataKey);
            localStorage.removeItem(key);
            console.log(`清理过期缓存: ${dataKey}`);
          }
        }
      }
    });
  } catch (error) {
    console.error('清理缓存失败:', error);
  }
};

// 初始化时清理过期缓存
cleanExpiredCache();

// 设置定时器，每小时清理一次过期缓存
setInterval(cleanExpiredCache, 60 * 60 * 1000);

const api = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
    'Cache-Control': 'no-cache, no-store, must-revalidate',
    'Pragma': 'no-cache',
    'Expires': '0'
  }
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    // 初始化重试计数
    if (!config.retryCount) {
      config.retryCount = 0;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    const originalRequest = error.config;
    
    // 处理token过期的情况
    if (error.response && error.response.data && error.response.data.returnCode === 'ERR0007') {
      // 清除本地存储的token
      localStorage.removeItem('token');
      // 跳转到登录页面
      window.location.href = '/login';
      return Promise.reject(error);
    }
    
    // 添加重试机制
    if (error.code === 'ECONNABORTED' || !error.response) {
      // 只对网络错误和超时进行重试
      if (originalRequest.retryCount < 3) {
        originalRequest.retryCount += 1;
        console.log(`请求重试 (${originalRequest.retryCount}/3): ${originalRequest.url}`);
        // 指数退避策略
        const delay = Math.pow(2, originalRequest.retryCount) * 1000;
        await new Promise(resolve => setTimeout(resolve, delay));
        return api(originalRequest);
      }
    }
    
    console.error('API Error:', error.response ? error.response.data : error.message);
    return Promise.reject(error);
  }
);

export default api;
export { cleanExpiredCache };