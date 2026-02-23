package com.hr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 招聘系统应用程序主类
 * 负责启动Spring Boot应用程序，初始化所有组件和配置
 * 
 * @MapperScan 指定MyBatis映射器扫描路径
 * @EnableScheduling 启用Spring定时任务支持
 */
@SpringBootApplication
@MapperScan("com.hr.mapper")
@EnableScheduling
public class RecruitmentSystemApplication {
    /**
     * 应用程序入口方法
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(RecruitmentSystemApplication.class, args);
    }
}