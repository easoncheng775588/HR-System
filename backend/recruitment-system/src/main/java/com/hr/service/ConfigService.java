package com.hr.service;

public interface ConfigService {
    
    /**
     * 获取配置值
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    String getConfig(String key, String defaultValue);
    
    /**
     * 重新加载配置
     */
    void reloadConfig();
}