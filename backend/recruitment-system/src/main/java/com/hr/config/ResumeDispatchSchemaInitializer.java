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
public class ResumeDispatchSchemaInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ResumeDispatchSchemaInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public ResumeDispatchSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS resume_dispatch (" +
                    "dispatch_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分发ID', " +
                    "resume_id BIGINT NOT NULL COMMENT '简历ID', " +
                    "recruitment_request_id BIGINT DEFAULT NULL COMMENT '关联需求ID', " +
                    "interviewer_id VARCHAR(20) NOT NULL COMMENT '面试官ID', " +
                    "interviewer_name VARCHAR(50) DEFAULT '' COMMENT '面试官姓名', " +
                    "dispatch_status VARCHAR(30) NOT NULL DEFAULT 'PENDING' COMMENT '分发状态', " +
                    "dispatch_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分发时间', " +
                    "confirm_time DATETIME NULL COMMENT '确认时间', " +
                    "interview_method VARCHAR(20) DEFAULT NULL COMMENT '面试方式', " +
                    "meeting_no VARCHAR(100) DEFAULT NULL COMMENT '会议号', " +
                    "available_start_time DATETIME DEFAULT NULL COMMENT '面试官可面试开始时间', " +
                    "available_end_time DATETIME DEFAULT NULL COMMENT '面试官可面试结束时间', " +
                    "confirmed_interview_time DATETIME DEFAULT NULL COMMENT '已确认面试时间', " +
                    "interview_time_confirm_user_id VARCHAR(20) DEFAULT NULL COMMENT '确认面试时间人ID', " +
                    "interview_time_confirm_user_name VARCHAR(50) DEFAULT NULL COMMENT '确认面试时间人姓名', " +
                    "interview_time_confirm_time DATETIME DEFAULT NULL COMMENT '确认面试时间操作时间', " +
                    "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', " +
                    "create_user_id VARCHAR(20) DEFAULT '' COMMENT '创建人ID', " +
                    "create_user_name VARCHAR(50) DEFAULT '' COMMENT '创建人', " +
                    "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间', " +
                    "update_user_id VARCHAR(20) DEFAULT '' COMMENT '更新人ID', " +
                    "update_user_name VARCHAR(50) DEFAULT '' COMMENT '更新人'" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历分发表'"
            );
            createIndex("idx_resume_dispatch_resume_id", "resume_dispatch", "resume_id");
            createIndex("idx_resume_dispatch_interviewer_id", "resume_dispatch", "interviewer_id");
            createIndex("idx_resume_dispatch_status", "resume_dispatch", "dispatch_status");
            createIndex("uk_resume_dispatch_resume_interviewer", "resume_dispatch", "resume_id, interviewer_id");
            createIndex("idx_resume_dispatch_confirmed_interview_time", "resume_dispatch", "confirmed_interview_time");

            addColumn("interview_method", "VARCHAR(20) DEFAULT NULL COMMENT '面试方式'");
            addColumn("meeting_no", "VARCHAR(100) DEFAULT NULL COMMENT '会议号'");
            addColumn("available_start_time", "DATETIME DEFAULT NULL COMMENT '面试官可面试开始时间'");
            addColumn("available_end_time", "DATETIME DEFAULT NULL COMMENT '面试官可面试结束时间'");
            addColumn("confirmed_interview_time", "DATETIME DEFAULT NULL COMMENT '已确认面试时间'");
            addColumn("interview_time_confirm_user_id", "VARCHAR(20) DEFAULT NULL COMMENT '确认面试时间人ID'");
            addColumn("interview_time_confirm_user_name", "VARCHAR(50) DEFAULT NULL COMMENT '确认面试时间人姓名'");
            addColumn("interview_time_confirm_time", "DATETIME DEFAULT NULL COMMENT '确认面试时间操作时间'");
        } catch (Exception e) {
            logger.warn("Resume dispatch schema initializer failed: {}", e.getMessage());
        }
    }

    private void createIndex(String indexName, String tableName, String columnExpr) {
        try {
            jdbcTemplate.execute("CREATE INDEX " + indexName + " ON " + tableName + "(" + columnExpr + ")");
        } catch (Exception e) {
            logger.info("Index {} exists or failed: {}", indexName, e.getMessage());
        }
    }

    private void addColumn(String columnName, String definition) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'resume_dispatch' AND COLUMN_NAME = ?",
            Integer.class,
            columnName
        );
        if (count == null || count == 0) {
            jdbcTemplate.execute("ALTER TABLE resume_dispatch ADD COLUMN " + columnName + " " + definition);
            logger.info("Added resume_dispatch column: {}", columnName);
        }
    }
}
