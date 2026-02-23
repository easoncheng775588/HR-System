package com.hr.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类
 * 负责配置Spring MVC相关设置，如拦截器注册
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        // 注册缓存控制拦截器，应用到所有请求
        registry.addInterceptor(new CacheControlInterceptor())
                .addPathPatterns("/**");
    }
}