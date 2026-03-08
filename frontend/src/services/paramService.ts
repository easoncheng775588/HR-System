import api, { CacheManager } from '../utils/api';

const PARAM_CACHE_KEY = 'sys_params';
const PARAM_CACHE_EXPIRY = 30 * 60 * 1000;

export const PARAM_TYPES = {
  LEVEL: 'LEVEL',
  TEAM: 'TEAM',
  CATEGORY: 'CATEGORY',
  PLATFORM: 'PLATFORM',
  TYPE: 'TYPE'
};

export const paramService = {
  async getParamsByType(paramType) {
    const cacheKey = `${PARAM_CACHE_KEY}_${paramType}`;
    const cached = CacheManager.get(cacheKey);
    if (cached) {
      console.log(`paramService: 从缓存获取 ${paramType} 参数`, cached);
      return cached;
    }
    
    console.log(`paramService: 从API获取 ${paramType} 参数`);
    const response = await api.get(`/api/sys/params/active/type/${paramType}`);
    if (response.data && response.data.returnCode === 'SUC0000') {
      const params = response.data.body || [];
      console.log(`paramService: ${paramType} 参数获取成功`, params);
      CacheManager.set(cacheKey, params, PARAM_CACHE_EXPIRY);
      return params;
    }
    console.log(`paramService: ${paramType} 参数获取失败`);
    return [];
  },

  async getAllActiveParams() {
    const cacheKey = `${PARAM_CACHE_KEY}_all`;
    const cached = CacheManager.get(cacheKey);
    if (cached) {
      return cached;
    }
    
    const response = await api.get('/api/sys/params/active');
    if (response.data && response.data.returnCode === 'SUC0000') {
      const params = response.data.body || [];
      CacheManager.set(cacheKey, params, PARAM_CACHE_EXPIRY);
      return params;
    }
    return [];
  },

  clearCache() {
    CacheManager.clearByPrefix(PARAM_CACHE_KEY);
  },

  getParamValue(params, paramCode) {
    if (!params || !Array.isArray(params)) return paramCode;
    const param = params.find(p => p.paramCode === paramCode);
    return param ? param.paramName : paramCode;
  },

  createParamMap(params) {
    if (!params || !Array.isArray(params)) return {};
    const map = {};
    params.forEach(param => {
      map[param.paramCode] = param.paramName;
    });
    return map;
  }
};

export default paramService;
