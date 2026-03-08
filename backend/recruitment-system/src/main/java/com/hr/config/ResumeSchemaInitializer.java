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
public class ResumeSchemaInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ResumeSchemaInitializer.class);
    private final JdbcTemplate jdbcTemplate;

    public ResumeSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            addColumn("related_request_ids", "VARCHAR(1000) DEFAULT '' COMMENT '关联需求ID，逗号分隔'");
            addColumn("related_request_names", "VARCHAR(2000) DEFAULT '' COMMENT '关联需求名称，逗号分隔'");
            addColumn("candidate_name", "VARCHAR(100) DEFAULT '' COMMENT '候选人'");
            addColumn("gender", "VARCHAR(10) DEFAULT '' COMMENT '性别'");
            addColumn("birth_date", "VARCHAR(20) DEFAULT '' COMMENT '出生年月日'");
            addColumn("first_degree", "VARCHAR(100) DEFAULT '' COMMENT '第一学历'");
            addColumn("first_degree_graduate_year", "VARCHAR(20) DEFAULT '' COMMENT '第一学历毕业年份'");
            addColumn("first_degree_school", "VARCHAR(255) DEFAULT '' COMMENT '第一学历毕业院校'");
            addColumn("first_degree_major", "VARCHAR(255) DEFAULT '' COMMENT '第一学历毕业专业'");
            addColumn("first_degree_full_time", "VARCHAR(10) DEFAULT '' COMMENT '第一学历是否全日制'");
            addColumn("highest_degree", "VARCHAR(100) DEFAULT '' COMMENT '最高学历'");
            addColumn("highest_degree_major", "VARCHAR(255) DEFAULT '' COMMENT '最高学历毕业专业'");
            addColumn("highest_degree_graduate_year", "VARCHAR(20) DEFAULT '' COMMENT '最高学历毕业年份'");
            addColumn("highest_degree_school", "VARCHAR(255) DEFAULT '' COMMENT '最高学历毕业院校'");
            addColumn("highest_degree_full_time", "VARCHAR(10) DEFAULT '' COMMENT '最高学历是否全日制'");
            addColumn("english_level", "VARCHAR(50) DEFAULT '' COMMENT '英语水平'");
            addColumn("candidate_platform", "VARCHAR(50) DEFAULT '' COMMENT '候选人技术平台'");
            addColumn("applied_category", "VARCHAR(50) DEFAULT '' COMMENT '申请岗位'");
            addColumn("applied_level", "VARCHAR(50) DEFAULT '' COMMENT '申请职级'");
            addColumn("it_work_years", "VARCHAR(20) DEFAULT '' COMMENT 'IT工作年限'");
            addColumn("it_internship_years", "VARCHAR(20) DEFAULT '' COMMENT 'IT实习年限'");
            addColumn("latest_company", "VARCHAR(255) DEFAULT '' COMMENT '最近服务公司'");
            addColumn("interview_available_start_time", "VARCHAR(30) DEFAULT '' COMMENT '可参加面试开始时间'");
            addColumn("interview_available_end_time", "VARCHAR(30) DEFAULT '' COMMENT '可参加面试截止时间'");
            addColumn("in_shenzhen", "VARCHAR(10) DEFAULT '' COMMENT '候选人是否在深圳'");
            addColumn("onboard_date", "VARCHAR(20) DEFAULT '' COMMENT '可到岗时间'");
            addColumn("supplier_initial_interview", "VARCHAR(10) DEFAULT '' COMMENT '供应商是否已初面'");
            addColumn("written_test_score", "VARCHAR(50) DEFAULT '' COMMENT '笔试成绩'");
            addColumn("supplier_interview_comment", "TEXT COMMENT '供应商初面意见'");
            addColumn("attachment_names", "TEXT COMMENT '附件名称列表'");
            addColumn("attachment_urls", "TEXT COMMENT '附件地址列表'");
            addColumn("remark", "TEXT COMMENT '备注'");
            addColumn("supplier_name", "VARCHAR(255) DEFAULT '' COMMENT '供应商名称'");
            addColumn("supplier_recommend_date", "DATETIME NULL COMMENT '供应商推荐日期'");
        } catch (Exception e) {
            logger.warn("Resume schema upgrade skipped or failed: {}", e.getMessage());
        }
    }

    private void addColumn(String columnName, String definition) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'resume' AND COLUMN_NAME = ?",
            Integer.class,
            columnName
        );
        if (count == null || count == 0) {
            jdbcTemplate.execute("ALTER TABLE resume ADD COLUMN " + columnName + " " + definition);
            logger.info("Added resume column: {}", columnName);
        }
    }
}
