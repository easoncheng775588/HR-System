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
            addColumn("category", "VARCHAR(50) DEFAULT '其他' COMMENT '所属分类'");
            addColumn("application_department", "VARCHAR(200) DEFAULT NULL COMMENT '申请部门展示值'");
            addColumn("org_unit_name", "VARCHAR(100) DEFAULT NULL COMMENT '组织单元名称'");
            addColumn("request_type", "VARCHAR(20) DEFAULT NULL COMMENT '所属类型'");
            addColumn("remark", "VARCHAR(500) DEFAULT NULL COMMENT '备注'");
            addColumn("submitter_role_type", "VARCHAR(20) DEFAULT NULL COMMENT '提交人角色类型'");
            addColumn("final_approver_user_id", "VARCHAR(20) DEFAULT NULL COMMENT '最终审批人ID'");
            addColumn("final_approver_user_name", "VARCHAR(50) DEFAULT NULL COMMENT '最终审批人姓名'");
            addColumn("interviewer_id", "VARCHAR(20) DEFAULT NULL COMMENT '面试官ID'");
            addColumn("interviewer_name", "VARCHAR(50) DEFAULT NULL COMMENT '面试官姓名'");
            addColumn("approval_status", "VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '审批状态'");
            addColumn("approval_user_id", "VARCHAR(20) DEFAULT NULL COMMENT '审批人ID'");
            addColumn("approval_user_name", "VARCHAR(50) DEFAULT NULL COMMENT '审批人姓名'");
            addColumn("approval_time", "DATETIME DEFAULT NULL COMMENT '审批时间'");
            addColumn("approval_comment", "TEXT COMMENT '审批意见'");
            addColumn("current_approval_level", "INT NOT NULL DEFAULT 0 COMMENT '当前审批级别'");
            addColumn("approval_level1_status", "VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '一级审批状态'");
            addColumn("approval_level1_user_id", "VARCHAR(20) DEFAULT NULL COMMENT '一级审批人ID'");
            addColumn("approval_level1_user_name", "VARCHAR(50) DEFAULT NULL COMMENT '一级审批人姓名'");
            addColumn("approval_level1_time", "DATETIME DEFAULT NULL COMMENT '一级审批时间'");
            addColumn("approval_level1_comment", "TEXT COMMENT '一级审批意见'");
            addColumn("approval_level2_status", "VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '二级审批状态'");
            addColumn("approval_level2_user_id", "VARCHAR(20) DEFAULT NULL COMMENT '二级审批人ID'");
            addColumn("approval_level2_user_name", "VARCHAR(50) DEFAULT NULL COMMENT '二级审批人姓名'");
            addColumn("approval_level2_time", "DATETIME DEFAULT NULL COMMENT '二级审批时间'");
            addColumn("approval_level2_comment", "TEXT COMMENT '二级审批意见'");
            addColumn("approval_level3_status", "VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '三级审批状态'");
            addColumn("approval_level3_user_id", "VARCHAR(20) DEFAULT NULL COMMENT '三级审批人ID'");
            addColumn("approval_level3_user_name", "VARCHAR(50) DEFAULT NULL COMMENT '三级审批人姓名'");
            addColumn("approval_level3_time", "DATETIME DEFAULT NULL COMMENT '三级审批时间'");
            addColumn("approval_level3_comment", "TEXT COMMENT '三级审批意见'");
            jdbcTemplate.execute("UPDATE recruitment_request SET category = '其他' WHERE category IS NULL OR category = ''");
            jdbcTemplate.execute(
                "UPDATE recruitment_request SET application_department = team " +
                    "WHERE (application_department IS NULL OR application_department = '') AND team IS NOT NULL AND team <> ''"
            );
            jdbcTemplate.execute(
                "UPDATE recruitment_request SET org_unit_name = team " +
                    "WHERE (org_unit_name IS NULL OR org_unit_name = '') AND team IS NOT NULL AND team <> ''"
            );
            jdbcTemplate.execute(
                "UPDATE recruitment_request SET request_type = 'NEW_DEMAND' " +
                    "WHERE request_type IS NULL OR request_type = ''"
            );
            jdbcTemplate.execute(
                "UPDATE recruitment_request SET remark = '' " +
                    "WHERE remark IS NULL"
            );
            jdbcTemplate.execute(
                "UPDATE recruitment_request SET approval_status = " +
                    "CASE " +
                    "WHEN approval_status IS NOT NULL AND approval_status <> '' THEN approval_status " +
                    "WHEN status = 'SUBMITTED' THEN 'PENDING' " +
                    "WHEN status IS NOT NULL AND status <> '' THEN status " +
                    "ELSE 'DRAFT' END"
            );
            dropColumnIfExists("position_publish_status");
            jdbcTemplate.execute(
                "UPDATE recruitment_request SET current_approval_level = " +
                    "CASE approval_status " +
                    "WHEN 'PENDING' THEN 1 " +
                    "WHEN '1STAPPROVED' THEN 2 " +
                    "WHEN '2NDAPPROVED' THEN 3 " +
                    "WHEN '3RDAPPROVED' THEN 4 " +
                    "WHEN 'APPROVED' THEN 4 " +
                    "ELSE 0 END " +
                    "WHERE current_approval_level IS NULL"
            );
            jdbcTemplate.execute("UPDATE recruitment_request SET approval_level1_status = 'PENDING' WHERE approval_level1_status IS NULL OR approval_level1_status = ''");
            jdbcTemplate.execute("UPDATE recruitment_request SET approval_level2_status = 'PENDING' WHERE approval_level2_status IS NULL OR approval_level2_status = ''");
            jdbcTemplate.execute("UPDATE recruitment_request SET approval_level3_status = 'PENDING' WHERE approval_level3_status IS NULL OR approval_level3_status = ''");
        } catch (Exception e) {
            logger.warn("Recruitment request schema upgrade skipped or failed: {}", e.getMessage());
        }
    }

    private void addColumn(String columnName, String definition) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'recruitment_request' AND COLUMN_NAME = ?",
            Integer.class,
            columnName
        );
        if (count == null || count == 0) {
            jdbcTemplate.execute("ALTER TABLE recruitment_request ADD COLUMN " + columnName + " " + definition);
            logger.info("Added recruitment_request column: {}", columnName);
        }
    }

    private void dropColumnIfExists(String columnName) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'recruitment_request' AND COLUMN_NAME = ?",
            Integer.class,
            columnName
        );
        if (count != null && count > 0) {
            jdbcTemplate.execute("ALTER TABLE recruitment_request DROP COLUMN " + columnName);
            logger.info("Dropped recruitment_request column: {}", columnName);
        }
    }
}
