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
public class InterviewEvaluationSchemaInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(InterviewEvaluationSchemaInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public InterviewEvaluationSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS interview_evaluation (" +
                    "evaluation_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '面试评价ID', " +
                    "resume_id BIGINT NOT NULL COMMENT '简历ID', " +
                    "dispatch_id BIGINT DEFAULT NULL COMMENT '分发ID', " +
                    "candidate_name VARCHAR(100) NOT NULL DEFAULT '' COMMENT '候选人', " +
                    "candidate_gender VARCHAR(20) DEFAULT '' COMMENT '候选人性别', " +
                    "work_years VARCHAR(50) DEFAULT '' COMMENT '工作年限', " +
                    "applied_level VARCHAR(50) DEFAULT '' COMMENT '应聘级别', " +
                    "supplier_name VARCHAR(255) DEFAULT '' COMMENT '来源供应商', " +
                    "interviewer_id VARCHAR(20) NOT NULL DEFAULT '' COMMENT '面试官ID', " +
                    "interviewer_name VARCHAR(50) NOT NULL DEFAULT '' COMMENT '面试官姓名', " +
                    "interviewer_department VARCHAR(200) DEFAULT '' COMMENT '面试官部门', " +
                    "platform VARCHAR(100) DEFAULT '' COMMENT '技术平台', " +
                    "entry_level_suggestion VARCHAR(50) DEFAULT '' COMMENT '入场职级建议', " +
                    "score VARCHAR(20) DEFAULT '' COMMENT '面试评价得分', " +
                    "interview_method VARCHAR(20) DEFAULT '' COMMENT '面试方式', " +
                    "interview_date DATETIME DEFAULT NULL COMMENT '面试日期', " +
                    "hire_suggestion VARCHAR(20) DEFAULT '' COMMENT '录用建议', " +
                    "attachment_name VARCHAR(255) DEFAULT '' COMMENT '附件名称', " +
                    "attachment_url VARCHAR(500) DEFAULT '' COMMENT '附件URL', " +
                    "approval_status VARCHAR(30) NOT NULL DEFAULT 'PENDING_OUTSOURCING' COMMENT '审批状态', " +
                    "current_approval_level INT NOT NULL DEFAULT 1 COMMENT '当前审批级别', " +
                    "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', " +
                    "create_user_id VARCHAR(20) NOT NULL DEFAULT '' COMMENT '创建人ID', " +
                    "create_user_name VARCHAR(50) NOT NULL DEFAULT '' COMMENT '创建人', " +
                    "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间', " +
                    "update_user_id VARCHAR(20) NOT NULL DEFAULT '' COMMENT '更新人ID', " +
                    "update_user_name VARCHAR(50) NOT NULL DEFAULT '' COMMENT '更新人'" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试评价表'"
            );

            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS interview_evaluation_approval_history (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID', " +
                    "evaluation_id BIGINT NOT NULL COMMENT '面试评价ID', " +
                    "approval_level INT NOT NULL COMMENT '审批级别', " +
                    "approver_id VARCHAR(20) NOT NULL DEFAULT '' COMMENT '审批人ID', " +
                    "approver_name VARCHAR(50) NOT NULL DEFAULT '' COMMENT '审批人姓名', " +
                    "approver_role VARCHAR(50) NOT NULL DEFAULT '' COMMENT '审批人角色', " +
                    "action VARCHAR(20) NOT NULL DEFAULT '' COMMENT '审批动作', " +
                    "comment VARCHAR(500) DEFAULT '' COMMENT '审批意见', " +
                    "approval_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间'" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试评价审批历史表'"
            );

            createIndex("idx_interview_eval_resume_id", "interview_evaluation", "resume_id");
            createIndex("idx_interview_eval_status", "interview_evaluation", "approval_status");
            createIndex("idx_interview_eval_interviewer_id", "interview_evaluation", "interviewer_id");
            createIndex("idx_interview_eval_create_user", "interview_evaluation", "create_user_id");
            createIndex("idx_interview_eval_history_eval_id", "interview_evaluation_approval_history", "evaluation_id");
            createIndex("idx_interview_eval_history_approver_id", "interview_evaluation_approval_history", "approver_id");
        } catch (Exception e) {
            logger.warn("Interview evaluation schema initializer failed: {}", e.getMessage());
        }
    }

    private void createIndex(String indexName, String tableName, String columnExpr) {
        try {
            jdbcTemplate.execute("CREATE INDEX " + indexName + " ON " + tableName + "(" + columnExpr + ")");
        } catch (Exception e) {
            logger.info("Index {} exists or failed: {}", indexName, e.getMessage());
        }
    }
}
