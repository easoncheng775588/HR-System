package com.hr.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class RecruitmentRequestSchemaInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(RecruitmentRequestSchemaInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public RecruitmentRequestSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            jdbcTemplate.execute("ALTER TABLE recruitment_request ADD COLUMN IF NOT EXISTS category VARCHAR(50) DEFAULT '其他' COMMENT '所属分类'");
            jdbcTemplate.execute("ALTER TABLE recruitment_request ADD COLUMN IF NOT EXISTS interviewer_id VARCHAR(20) DEFAULT NULL COMMENT '面试官ID'");
            jdbcTemplate.execute("ALTER TABLE recruitment_request ADD COLUMN IF NOT EXISTS interviewer_name VARCHAR(50) DEFAULT NULL COMMENT '面试官姓名'");
            jdbcTemplate.execute("UPDATE recruitment_request SET category = '其他' WHERE category IS NULL OR category = ''");
        } catch (Exception e) {
            logger.warn("Recruitment request schema upgrade skipped or failed: {}", e.getMessage());
        }
    }
}
