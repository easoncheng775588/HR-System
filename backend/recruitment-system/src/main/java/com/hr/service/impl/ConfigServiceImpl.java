package com.hr.service.impl;

import com.hr.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConfigServiceImpl implements ConfigService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    // 配置缓存
    private Map<String, String> configCache = new HashMap<>();
    
    /**
     * 系统启动时加载配置
     */
    @PostConstruct
    public void init() {
        reloadConfig();
    }
    
    /**
     * 每5分钟重新加载配置
     */
    @Scheduled(fixedRate = 300000) // 5分钟 = 300000毫秒
    public void scheduledReloadConfig() {
        reloadConfig();
    }
    
    @Override
    public String getConfig(String key, String defaultValue) {
        return configCache.getOrDefault(key, defaultValue);
    }
    
    @Override
    public void reloadConfig() {
        try {
            String sql = "SELECT config_key, config_value FROM sys_config";
            List<Map<String, Object>> configs = jdbcTemplate.queryForList(sql);
            
            Map<String, String> newCache = new HashMap<>();
            for (Map<String, Object> config : configs) {
                String key = (String) config.get("config_key");
                String value = (String) config.get("config_value");
                newCache.put(key, value);
            }
            
            configCache = newCache;
            System.out.println("配置已重新加载: " + configCache);
        } catch (Exception e) {
            System.err.println("加载配置失败: " + e.getMessage());
        }
    }
}