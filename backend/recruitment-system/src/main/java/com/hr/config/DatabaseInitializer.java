package com.hr.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据库初始化配置类
 * 负责在系统启动时初始化数据库表结构和基础数据
 * 实现CommandLineRunner接口，在Spring Boot应用启动后执行
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) throws Exception {
        logger.info("开始初始化数据库...");
        
        try {
            // 创建数据库
            jdbcTemplate.execute("CREATE DATABASE IF NOT EXISTS hr_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            logger.info("数据库 hr_system 创建成功");
            
            // 使用数据库
            jdbcTemplate.execute("USE hr_system");
            
            // 创建用人申请表
            String createTableSQL = "CREATE TABLE IF NOT EXISTS recruitment_request (" +
                "recruitment_request_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用人申请ID', " +
                "request_title VARCHAR(255) NOT NULL COMMENT '申请标题', " +
                "total_recruitment_count INT NOT NULL COMMENT '总编制人数', " +
                "vacancy_count INT NOT NULL COMMENT '空缺编制', " +
                "team VARCHAR(100) NOT NULL COMMENT '所属团队', " +
                "application_department VARCHAR(200) DEFAULT NULL COMMENT '申请部门展示值', " +
                "org_unit_name VARCHAR(100) DEFAULT NULL COMMENT '组织单元名称', " +
                "request_type VARCHAR(20) DEFAULT NULL COMMENT '所属类型', " +
                "remark VARCHAR(500) DEFAULT NULL COMMENT '备注', " +
                "submitter_role_type VARCHAR(20) DEFAULT NULL COMMENT '提交人角色类型', " +
                "final_approver_user_id VARCHAR(20) DEFAULT NULL COMMENT '最终审批人ID', " +
                "final_approver_user_name VARCHAR(50) DEFAULT NULL COMMENT '最终审批人姓名', " +
                "technical_platform VARCHAR(50) NOT NULL COMMENT '技术平台', " +
                "category VARCHAR(50) DEFAULT '其他' COMMENT '所属分类', " +
                "supplement_count INT NOT NULL COMMENT '补充人数', " +
                "urgent_requirement VARCHAR(10) NOT NULL COMMENT '是否近期紧急要求', " +
                "proposed_level VARCHAR(50) NOT NULL COMMENT '建议级别', " +
                "experience_years VARCHAR(50) NOT NULL COMMENT '相关经验年限要求', " +
                "skill_requirement TEXT COMMENT '技能要求描述', " +
                "position_responsibility TEXT NOT NULL COMMENT '岗位职责', " +
                "interviewer_id VARCHAR(20) DEFAULT NULL COMMENT '面试官ID', " +
                "interviewer_name VARCHAR(50) DEFAULT NULL COMMENT '面试官姓名', " +
                "approval_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '审批状态', " +
                "approval_user_id VARCHAR(20) DEFAULT NULL COMMENT '审批人ID', " +
                "approval_user_name VARCHAR(50) DEFAULT NULL COMMENT '审批人姓名', " +
                "approval_time DATETIME DEFAULT NULL COMMENT '审批时间', " +
                "approval_comment TEXT COMMENT '审批意见', " +
                "current_approval_level INT NOT NULL DEFAULT 0 COMMENT '当前审批级别', " +
                "approval_level1_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '一级审批状态', " +
                "approval_level1_user_id VARCHAR(20) DEFAULT NULL COMMENT '一级审批人ID', " +
                "approval_level1_user_name VARCHAR(50) DEFAULT NULL COMMENT '一级审批人姓名', " +
                "approval_level1_time DATETIME DEFAULT NULL COMMENT '一级审批时间', " +
                "approval_level1_comment TEXT COMMENT '一级审批意见', " +
                "approval_level2_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '二级审批状态', " +
                "approval_level2_user_id VARCHAR(20) DEFAULT NULL COMMENT '二级审批人ID', " +
                "approval_level2_user_name VARCHAR(50) DEFAULT NULL COMMENT '二级审批人姓名', " +
                "approval_level2_time DATETIME DEFAULT NULL COMMENT '二级审批时间', " +
                "approval_level2_comment TEXT COMMENT '二级审批意见', " +
                "approval_level3_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '三级审批状态', " +
                "approval_level3_user_id VARCHAR(20) DEFAULT NULL COMMENT '三级审批人ID', " +
                "approval_level3_user_name VARCHAR(50) DEFAULT NULL COMMENT '三级审批人姓名', " +
                "approval_level3_time DATETIME DEFAULT NULL COMMENT '三级审批时间', " +
                "approval_level3_comment TEXT COMMENT '三级审批意见', " +
                "status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '兼容旧流程状态字段', " +
                "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', " +
                "create_user_id VARCHAR(20) NOT NULL COMMENT '创建用户ID', " +
                "create_user_name VARCHAR(50) NOT NULL COMMENT '创建用户姓名', " +
                "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间', " +
                "update_user_id VARCHAR(20) NOT NULL COMMENT '更新用户ID', " +
                "update_user_name VARCHAR(50) NOT NULL COMMENT '更新用户姓名'" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用人申请表'";
            
            jdbcTemplate.execute(createTableSQL);
            logger.info("表 recruitment_request 创建成功");
            
            // 创建索引（MySQL不支持CREATE INDEX IF NOT EXISTS，先检查索引是否存在）
            try {
                jdbcTemplate.execute("CREATE INDEX idx_status ON recruitment_request(status)");
                logger.info("索引 idx_status 创建成功");
            } catch (Exception e) {
                logger.info("索引 idx_status 已存在或创建失败: {}", e.getMessage());
            }

            try {
                jdbcTemplate.execute("CREATE INDEX idx_approval_status ON recruitment_request(approval_status)");
                logger.info("索引 idx_approval_status 创建成功");
            } catch (Exception e) {
                logger.info("索引 idx_approval_status 已存在或创建失败: {}", e.getMessage());
            }
            
            try {
                jdbcTemplate.execute("CREATE INDEX idx_create_time ON recruitment_request(create_time)");
                logger.info("索引 idx_create_time 创建成功");
            } catch (Exception e) {
                logger.info("索引 idx_create_time 已存在或创建失败: {}", e.getMessage());
            }
            
            // 创建用户表
            String createUserTableSQL = "CREATE TABLE IF NOT EXISTS sys_user (" +
                "user_id VARCHAR(20) PRIMARY KEY COMMENT '用户ID', " +
                "username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名', " +
                "password VARCHAR(255) NOT NULL COMMENT '密码', " +
                "real_name VARCHAR(50) NOT NULL COMMENT '真实姓名', " +
                "email VARCHAR(100) COMMENT '邮箱', " +
                "phone VARCHAR(20) COMMENT '电话', " +
                "department VARCHAR(100) COMMENT '部门', " +
                "team_name VARCHAR(100) COMMENT '团队名称', " +
                "group_name VARCHAR(100) COMMENT '室组名称', " +
                "position VARCHAR(100) COMMENT '职位', " +
                "status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态（ACTIVE：启用，DISABLED：禁用）', " +
                "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', " +
                "create_user_id VARCHAR(20) COMMENT '创建用户ID', " +
                "create_user_name VARCHAR(50) COMMENT '创建用户姓名', " +
                "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间', " +
                "update_user_id VARCHAR(20) COMMENT '更新用户ID', " +
                "update_user_name VARCHAR(50) COMMENT '更新用户姓名'" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表'";
            
            jdbcTemplate.execute(createUserTableSQL);
            logger.info("表 sys_user 创建成功");
            ensureUserColumns();
            
            // 创建角色表
            String createRoleTableSQL = "CREATE TABLE IF NOT EXISTS sys_role (" +
                "role_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID', " +
                "role_name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名称', " +
                "role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码', " +
                "description VARCHAR(255) COMMENT '角色描述', " +
                "status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态（ACTIVE：启用，DISABLED：禁用）', " +
                "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', " +
                "create_user_id VARCHAR(20) COMMENT '创建用户ID', " +
                "create_user_name VARCHAR(50) COMMENT '创建用户姓名', " +
                "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间', " +
                "update_user_id VARCHAR(20) COMMENT '更新用户ID', " +
                "update_user_name VARCHAR(50) COMMENT '更新用户姓名'" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表'";
            
            jdbcTemplate.execute(createRoleTableSQL);
            logger.info("表 sys_role 创建成功");
            
            // 创建权限表
            String createPermissionTableSQL = "CREATE TABLE IF NOT EXISTS sys_permission (" +
                "permission_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID', " +
                "permission_name VARCHAR(100) NOT NULL COMMENT '权限名称', " +
                "permission_code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码', " +
                "permission_type VARCHAR(20) NOT NULL COMMENT '权限类型（MENU：菜单，BUTTON：按钮）', " +
                "parent_id BIGINT DEFAULT 0 COMMENT '父权限ID', " +
                "path VARCHAR(255) COMMENT '路由路径', " +
                "icon VARCHAR(100) COMMENT '图标', " +
                "sort_order INT DEFAULT 0 COMMENT '排序', " +
                "status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态（ACTIVE：启用，DISABLED：禁用）', " +
                "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', " +
                "create_user_id VARCHAR(20) COMMENT '创建用户ID', " +
                "create_user_name VARCHAR(50) COMMENT '创建用户姓名', " +
                "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间', " +
                "update_user_id VARCHAR(20) COMMENT '更新用户ID', " +
                "update_user_name VARCHAR(50) COMMENT '更新用户姓名'" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表'";
            
            jdbcTemplate.execute(createPermissionTableSQL);
            logger.info("表 sys_permission 创建成功");
            
            // 创建用户角色关联表
            String createUserRoleTableSQL = "CREATE TABLE IF NOT EXISTS sys_user_role (" +
                "user_role_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户角色ID', " +
                "user_id VARCHAR(20) NOT NULL COMMENT '用户ID', " +
                "role_id BIGINT NOT NULL COMMENT '角色ID', " +
                "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', " +
                "create_user_id VARCHAR(20) COMMENT '创建用户ID', " +
                "create_user_name VARCHAR(50) COMMENT '创建用户姓名'" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表'";
            
            jdbcTemplate.execute(createUserRoleTableSQL);
            logger.info("表 sys_user_role 创建成功");
            
            // 创建角色权限关联表
            String createRolePermissionTableSQL = "CREATE TABLE IF NOT EXISTS sys_role_permission (" +
                "role_permission_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色权限ID', " +
                "role_id BIGINT NOT NULL COMMENT '角色ID', " +
                "permission_id BIGINT NOT NULL COMMENT '权限ID', " +
                "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', " +
                "create_user_id VARCHAR(20) COMMENT '创建用户ID', " +
                "create_user_name VARCHAR(50) COMMENT '创建用户姓名'" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表'";
            
            jdbcTemplate.execute(createRolePermissionTableSQL);
            logger.info("表 sys_role_permission 创建成功");
            
            // 创建录用记录表
            String createOfferRecordTableSQL = "CREATE TABLE IF NOT EXISTS offer_record (" +
                "offer_record_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '录用记录ID', " +
                "resume_id BIGINT NOT NULL COMMENT '简历ID', " +
                "recruitment_request_id BIGINT NOT NULL COMMENT '招聘申请ID', " +
                "candidate_name VARCHAR(100) NOT NULL COMMENT '候选人姓名', " +
                "contact_phone VARCHAR(20) NOT NULL COMMENT '联系电话', " +
                "email VARCHAR(100) NOT NULL COMMENT '邮箱', " +
                "position VARCHAR(255) NOT NULL COMMENT '录用岗位', " +
                "status VARCHAR(20) NOT NULL COMMENT '录用状态', " +
                "offer_time DATETIME COMMENT '录用时间', " +
                "entry_time DATETIME COMMENT '入职时间', " +
                "email_status VARCHAR(20) COMMENT '邮件发送状态', " +
                "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', " +
                "create_user_id VARCHAR(20) NOT NULL COMMENT '创建用户ID', " +
                "create_user_name VARCHAR(50) NOT NULL COMMENT '创建用户姓名', " +
                "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间', " +
                "update_user_id VARCHAR(20) NOT NULL COMMENT '更新用户ID', " +
                "update_user_name VARCHAR(50) NOT NULL COMMENT '更新用户姓名'" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='录用记录表'";
            
            jdbcTemplate.execute(createOfferRecordTableSQL);
            logger.info("表 offer_record 创建成功");
            
            // 创建邮件模板表
            String createEmailTemplateTableSQL = "CREATE TABLE IF NOT EXISTS email_template (" +
                "template_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID', " +
                "template_name VARCHAR(100) NOT NULL COMMENT '模板名称', " +
                "template_type VARCHAR(50) NOT NULL COMMENT '模板类型', " +
                "subject VARCHAR(255) NOT NULL COMMENT '邮件主题', " +
                "content TEXT NOT NULL COMMENT '邮件内容', " +
                "status VARCHAR(20) NOT NULL COMMENT '状态', " +
                "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', " +
                "create_user_id VARCHAR(20) NOT NULL COMMENT '创建用户ID', " +
                "create_user_name VARCHAR(50) NOT NULL COMMENT '创建用户姓名', " +
                "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间', " +
                "update_user_id VARCHAR(20) NOT NULL COMMENT '更新用户ID', " +
                "update_user_name VARCHAR(50) NOT NULL COMMENT '更新用户姓名'" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮件模板表'";
            
            jdbcTemplate.execute(createEmailTemplateTableSQL);
            logger.info("表 email_template 创建成功");
            
            // 创建审批历史表
            String createApprovalHistoryTableSQL = "CREATE TABLE IF NOT EXISTS approval_history (" +
                "approval_history_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '审批历史ID', " +
                "recruitment_request_id BIGINT NOT NULL COMMENT '用人申请ID', " +
                "approval_level INT NOT NULL COMMENT '审批级别', " +
                "approver_id VARCHAR(20) NOT NULL COMMENT '审批人ID', " +
                "approver_name VARCHAR(50) NOT NULL COMMENT '审批人姓名', " +
                "approval_status VARCHAR(20) NOT NULL COMMENT '审批状态（APPROVED：通过，REJECTED：拒绝）', " +
                "approval_comment TEXT COMMENT '审批意见', " +
                "approval_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间', " +
                "FOREIGN KEY (recruitment_request_id) REFERENCES recruitment_request(recruitment_request_id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审批历史表'";
            
            jdbcTemplate.execute(createApprovalHistoryTableSQL);
            logger.info("表 approval_history 创建成功");

            ensureMessageTable();
            ensureDemandManagementTables();
            cleanupLegacyPositionPublishingArtifacts();
            
            // 创建简历表
            String createResumeTableSQL = "CREATE TABLE IF NOT EXISTS resume (" +
                "resume_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '简历ID', " +
                "recruitment_request_id BIGINT COMMENT '招聘申请ID', " +
                "job_title VARCHAR(255) COMMENT '应聘岗位', " +
                "applicant_name VARCHAR(100) COMMENT '申请人姓名', " +
                "contact_phone VARCHAR(20) COMMENT '联系电话', " +
                "email VARCHAR(100) COMMENT '邮箱', " +
                "education VARCHAR(100) COMMENT '学历', " +
                "work_experience TEXT COMMENT '工作经验', " +
                "resume_file_name VARCHAR(255) DEFAULT '' COMMENT '简历文件名', " +
                "resume_file_url VARCHAR(500) DEFAULT '' COMMENT '简历文件URL', " +
                "status VARCHAR(50) COMMENT '状态', " +
                "interview_status VARCHAR(50) COMMENT '面试状态', " +
                "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', " +
                "create_user_id VARCHAR(20) COMMENT '创建用户ID', " +
                "create_user_name VARCHAR(50) COMMENT '创建用户姓名', " +
                "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间', " +
                "update_user_id VARCHAR(20) COMMENT '更新用户ID', " +
                "update_user_name VARCHAR(50) COMMENT '更新用户姓名'" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历表'";
            
            jdbcTemplate.execute(createResumeTableSQL);
            logger.info("表 resume 创建成功");
            
            // 创建面试记录表
            String createInterviewRecordTableSQL = "CREATE TABLE IF NOT EXISTS interview_record (" +
                "interview_record_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '面试记录ID', " +
                "resume_id BIGINT NOT NULL COMMENT '简历ID', " +
                "recruitment_request_id BIGINT NOT NULL COMMENT '招聘申请ID', " +
                "interview_round VARCHAR(50) NOT NULL COMMENT '面试轮次', " +
                "interviewer_id VARCHAR(20) NOT NULL COMMENT '面试官ID', " +
                "interviewer_name VARCHAR(50) NOT NULL COMMENT '面试官姓名', " +
                "interviewer_role VARCHAR(50) NOT NULL COMMENT '面试官角色', " +
                "interview_time DATETIME NOT NULL COMMENT '面试时间', " +
                "interview_result VARCHAR(50) COMMENT '面试结果', " +
                "interview_comment TEXT COMMENT '面试意见', " +
                "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', " +
                "create_user_id VARCHAR(20) COMMENT '创建用户ID', " +
                "create_user_name VARCHAR(50) COMMENT '创建用户姓名', " +
                "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间', " +
                "update_user_id VARCHAR(20) COMMENT '更新用户ID', " +
                "update_user_name VARCHAR(50) COMMENT '更新用户姓名'" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试记录表'";
            
            jdbcTemplate.execute(createInterviewRecordTableSQL);
            logger.info("表 interview_record 创建成功");
            
            // 创建审批历史表索引
            try {
                jdbcTemplate.execute("CREATE INDEX idx_recruitment_request_id ON approval_history(recruitment_request_id)");
                logger.info("索引 idx_recruitment_request_id 创建成功");
            } catch (Exception e) {
                logger.info("索引 idx_recruitment_request_id 已存在或创建失败: {}", e.getMessage());
            }
            
            // 创建索引
            try {
                jdbcTemplate.execute("CREATE INDEX idx_resume_id ON offer_record(resume_id)");
                logger.info("索引 idx_resume_id 创建成功");
            } catch (Exception e) {
                logger.info("索引 idx_resume_id 已存在或创建失败: {}", e.getMessage());
            }
            
            try {
                jdbcTemplate.execute("CREATE INDEX idx_status ON offer_record(status)");
                logger.info("索引 idx_status 创建成功");
            } catch (Exception e) {
                logger.info("索引 idx_status 已存在或创建失败: {}", e.getMessage());
            }
            
            try {
                jdbcTemplate.execute("CREATE INDEX idx_email_status ON offer_record(email_status)");
                logger.info("索引 idx_email_status 创建成功");
            } catch (Exception e) {
                logger.info("索引 idx_email_status 已存在或创建失败: {}", e.getMessage());
            }
            
            try {
                jdbcTemplate.execute("CREATE INDEX idx_template_type ON email_template(template_type)");
                logger.info("索引 idx_template_type 创建成功");
            } catch (Exception e) {
                logger.info("索引 idx_template_type 已存在或创建失败: {}", e.getMessage());
            }
            
            try {
                jdbcTemplate.execute("CREATE INDEX idx_status ON email_template(status)");
                logger.info("索引 idx_status 创建成功");
            } catch (Exception e) {
                logger.info("索引 idx_status 已存在或创建失败: {}", e.getMessage());
            }
            
            // 初始化建议级别参数
            initProposedLevelParams();
            
            // 初始化所属团队参数
            initTeamParams();
            
            // 初始化超级管理员数据
            initSuperAdmin();
            ensureDirectorRole();
            ensureDirectorUsers();
            
            logger.info("数据库初始化完成！");
            
        } catch (Exception e) {
            logger.error("数据库初始化失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void initSuperAdmin() {
        try {
                // 检查超级管理员是否已存在
                Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM sys_user WHERE user_id = ?",
                    Integer.class,
                    Constants.SUPER_ADMIN_USER_ID
                );
                
                if (count == null || count == 0) {
                    // 创建超级管理员用户
                    String encodedPassword = passwordEncoder.encode("123321");
                    jdbcTemplate.update(
                        "INSERT INTO sys_user (user_id, username, password, real_name, email, phone, department, position, status, create_user_id, create_user_name, update_user_id, update_user_name) " +
                        "VALUES (?, ?, ?, ?, 'admin@hr.com', '13800138000', '人事部', '系统管理员', ?, ?, ?, ?, ?)",
                        Constants.SUPER_ADMIN_USER_ID,
                        Constants.SUPER_ADMIN_USERNAME,
                        encodedPassword,
                        Constants.SUPER_ADMIN_REAL_NAME,
                        Constants.STATUS_ACTIVE,
                        Constants.SUPER_ADMIN_USER_ID,
                        Constants.SYSTEM_USER,
                        Constants.SUPER_ADMIN_USER_ID,
                        Constants.SYSTEM_USER
                    );
                    logger.info("超级管理员用户创建成功");
                    
                    // 创建超级管理员角色
                    jdbcTemplate.update(
                        "INSERT INTO sys_role (role_name, role_code, description, status, create_user_id, create_user_name, update_user_id, update_user_name) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                        Constants.SUPER_ADMIN_ROLE_NAME,
                        Constants.SUPER_ADMIN_ROLE_CODE,
                        Constants.SUPER_ADMIN_ROLE_DESC,
                        Constants.STATUS_ACTIVE,
                        Constants.SUPER_ADMIN_USER_ID,
                        Constants.SYSTEM_USER,
                        Constants.SUPER_ADMIN_USER_ID,
                        Constants.SYSTEM_USER
                    );
                    logger.info("超级管理员角色创建成功");
                    
                    // 创建权限
                    Object[][] permissions = new Object[][] {
                        {"用人申请管理", "RECRUITMENT_REQUEST_MANAGE", Constants.PERMISSION_TYPE_MENU, 0, "/recruitment-request", "CheckCircleOutlined", 1, Constants.STATUS_ACTIVE},
                        {"用户管理", "USER_MANAGE", Constants.PERMISSION_TYPE_MENU, 0, "/user-management", "UserOutlined", 2, Constants.STATUS_ACTIVE},
                        {"新增用人申请", "RECRUITMENT_REQUEST_ADD", Constants.PERMISSION_TYPE_BUTTON, 1, null, null, 1, Constants.STATUS_ACTIVE},
                        {"编辑用人申请", "RECRUITMENT_REQUEST_EDIT", Constants.PERMISSION_TYPE_BUTTON, 1, null, null, 2, Constants.STATUS_ACTIVE},
                        {"删除用人申请", "RECRUITMENT_REQUEST_DELETE", Constants.PERMISSION_TYPE_BUTTON, 1, null, null, 3, Constants.STATUS_ACTIVE},
                        {"新增用户", "USER_ADD", Constants.PERMISSION_TYPE_BUTTON, 2, null, null, 1, Constants.STATUS_ACTIVE},
                        {"编辑用户", "USER_EDIT", Constants.PERMISSION_TYPE_BUTTON, 2, null, null, 2, Constants.STATUS_ACTIVE},
                        {"删除用户", "USER_DELETE", Constants.PERMISSION_TYPE_BUTTON, 2, null, null, 3, Constants.STATUS_ACTIVE}
                    };
                    
                    for (Object[] perm : permissions) {
                        jdbcTemplate.update(
                            "INSERT INTO sys_permission (permission_name, permission_code, permission_type, parent_id, path, icon, sort_order, status, create_user_id, create_user_name, update_user_id, update_user_name) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                            perm[0], perm[1], perm[2], perm[3], perm[4], perm[5], perm[6], perm[7],
                            Constants.SUPER_ADMIN_USER_ID, Constants.SYSTEM_USER,
                            Constants.SUPER_ADMIN_USER_ID, Constants.SYSTEM_USER
                        );
                    }
                    logger.info("权限数据创建成功");
                    
                    // 为超级管理员角色分配所有权限
                    jdbcTemplate.update(
                        "INSERT INTO sys_role_permission (role_id, permission_id, create_user_id, create_user_name) " +
                        "SELECT 1, permission_id, ?, ? FROM sys_permission",
                        Constants.SUPER_ADMIN_USER_ID,
                        Constants.SYSTEM_USER
                    );
                    logger.info("角色权限关联创建成功");
                    
                    // 为超级管理员用户分配超级管理员角色
                    jdbcTemplate.update(
                        "INSERT INTO sys_user_role (user_id, role_id, create_user_id, create_user_name) " +
                        "VALUES (?, 1, ?, ?)",
                        Constants.SUPER_ADMIN_USER_ID,
                        Constants.SUPER_ADMIN_USER_ID,
                        Constants.SYSTEM_USER
                    );
                    logger.info("用户角色关联创建成功");
                    
                    logger.info("超级管理员数据初始化完成！");
                } else {
                    logger.info("超级管理员用户已存在，跳过初始化");
                }
            } catch (Exception e) {
                logger.error("超级管理员数据初始化失败: {}", e.getMessage(), e);
            }
    }
    
    private void initProposedLevelParams() {
        try {
            // 检查建议级别参数是否已存在
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_param WHERE param_type = ?",
                Integer.class,
                Constants.PARAM_TYPE_LEVEL
            );
            
            if (count == null || count == 0) {
                // 初始化建议级别参数
                Object[][] levels = new Object[][] {
                    {"LEVEL_001", "ENTRY_LEVEL", "初级", "初级", Constants.PARAM_TYPE_LEVEL, Constants.STATUS_ACTIVE, 1, Constants.SUPER_ADMIN_USER_ID, Constants.SYSTEM_USER, Constants.SUPER_ADMIN_USER_ID, Constants.SYSTEM_USER},
                    {"LEVEL_002", "MID_LEVEL", "中级", "中级", Constants.PARAM_TYPE_LEVEL, Constants.STATUS_ACTIVE, 2, Constants.SUPER_ADMIN_USER_ID, Constants.SYSTEM_USER, Constants.SUPER_ADMIN_USER_ID, Constants.SYSTEM_USER},
                    {"LEVEL_003", "SENIOR_LEVEL", "高级", "高级", Constants.PARAM_TYPE_LEVEL, Constants.STATUS_ACTIVE, 3, Constants.SUPER_ADMIN_USER_ID, Constants.SYSTEM_USER, Constants.SUPER_ADMIN_USER_ID, Constants.SYSTEM_USER},
                    {"LEVEL_004", "EXPERT_LEVEL", "资深", "资深", Constants.PARAM_TYPE_LEVEL, Constants.STATUS_ACTIVE, 4, Constants.SUPER_ADMIN_USER_ID, Constants.SYSTEM_USER, Constants.SUPER_ADMIN_USER_ID, Constants.SYSTEM_USER}
                };
                
                String sql = "INSERT INTO sys_param (param_id, param_code, param_name, param_value, param_type, status, sort_order, create_user_id, create_user_name, update_user_id, update_user_name) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                
                for (Object[] level : levels) {
                    jdbcTemplate.update(sql, level);
                }
                logger.info("建议级别参数初始化完成！");
            } else {
                logger.info("建议级别参数已存在，跳过初始化");
            }
        } catch (Exception e) {
            logger.error("建议级别参数初始化失败: {}", e.getMessage(), e);
        }
    }
    
    private void initTeamParams() {
        try {
            // 检查所属团队参数是否已存在
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_param WHERE param_type = ?",
                Integer.class,
                Constants.PARAM_TYPE_TEAM
            );
            
            if (count == null || count == 0) {
                // 初始化所属团队参数
                Object[][] teams = new Object[][] {
                    {"TEAM_001", "RETAIL", "零售", "零售", Constants.PARAM_TYPE_TEAM, Constants.STATUS_ACTIVE, 1, Constants.SUPER_ADMIN_USER_ID, Constants.SYSTEM_USER, Constants.SUPER_ADMIN_USER_ID, Constants.SYSTEM_USER},
                    {"TEAM_002", "WHOLESALE", "批发", "批发", Constants.PARAM_TYPE_TEAM, Constants.STATUS_ACTIVE, 2, Constants.SUPER_ADMIN_USER_ID, Constants.SYSTEM_USER, Constants.SUPER_ADMIN_USER_ID, Constants.SYSTEM_USER},
                    {"TEAM_003", "BASIC", "基础", "基础", Constants.PARAM_TYPE_TEAM, Constants.STATUS_ACTIVE, 3, Constants.SUPER_ADMIN_USER_ID, Constants.SYSTEM_USER, Constants.SUPER_ADMIN_USER_ID, Constants.SYSTEM_USER},
                    {"TEAM_004", "DATA", "数据", "数据", Constants.PARAM_TYPE_TEAM, Constants.STATUS_ACTIVE, 4, Constants.SUPER_ADMIN_USER_ID, Constants.SYSTEM_USER, Constants.SUPER_ADMIN_USER_ID, Constants.SYSTEM_USER}
                };
                
                String sql = "INSERT INTO sys_param (param_id, param_code, param_name, param_value, param_type, status, sort_order, create_user_id, create_user_name, update_user_id, update_user_name) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                
                for (Object[] team : teams) {
                    jdbcTemplate.update(sql, team);
                }
                logger.info("所属团队参数初始化完成！");
            } else {
                logger.info("所属团队参数已存在，跳过初始化");
            }
        } catch (Exception e) {
            logger.error("所属团队参数初始化失败: {}", e.getMessage(), e);
        }
    }

    private void ensureMessageTable() {
        String createMessageTableSQL = "CREATE TABLE IF NOT EXISTS message (" +
            "message_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID', " +
            "title VARCHAR(200) NOT NULL COMMENT '消息标题', " +
            "content TEXT NOT NULL COMMENT '消息内容', " +
            "type VARCHAR(50) NOT NULL COMMENT '消息类型', " +
            "status VARCHAR(20) NOT NULL DEFAULT 'UNREAD' COMMENT '消息状态', " +
            "priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '优先级', " +
            "target_user_id VARCHAR(20) DEFAULT NULL COMMENT '目标用户ID', " +
            "target_user_role VARCHAR(50) DEFAULT NULL COMMENT '目标角色', " +
            "create_user_id VARCHAR(20) NOT NULL COMMENT '创建用户ID', " +
            "create_user_name VARCHAR(50) NOT NULL COMMENT '创建用户姓名', " +
            "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', " +
            "read_time DATETIME DEFAULT NULL COMMENT '已读时间', " +
            "is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除'" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表'";

        jdbcTemplate.execute(createMessageTableSQL);
        logger.info("表 message 创建成功");

        try {
            jdbcTemplate.execute("CREATE INDEX idx_message_target_user_id ON message(target_user_id)");
            logger.info("索引 idx_message_target_user_id 创建成功");
        } catch (Exception e) {
            logger.info("索引 idx_message_target_user_id 已存在或创建失败: {}", e.getMessage());
        }

        try {
            jdbcTemplate.execute("CREATE INDEX idx_message_status ON message(status)");
            logger.info("索引 idx_message_status 创建成功");
        } catch (Exception e) {
            logger.info("索引 idx_message_status 已存在或创建失败: {}", e.getMessage());
        }
    }

    private void ensureDemandManagementTables() {
        String createDemandRequirementSQL = "CREATE TABLE IF NOT EXISTS demand_requirement (" +
            "demand_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '需求ID', " +
            "source_recruitment_request_id BIGINT NOT NULL COMMENT '来源用人申请ID', " +
            "position_org_name VARCHAR(255) NOT NULL COMMENT '岗位/用人室组', " +
            "vacancy_count INT NOT NULL DEFAULT 0 COMMENT '空缺岗位', " +
            "technical_platform VARCHAR(50) NOT NULL COMMENT '技术平台', " +
            "recruit_level VARCHAR(50) NOT NULL COMMENT '招聘级别', " +
            "recruit_count INT NOT NULL COMMENT '招聘数量', " +
            "position_responsibility TEXT NOT NULL COMMENT '岗位职责', " +
            "recruit_requirement TEXT NOT NULL COMMENT '招聘要求', " +
            "acceptance_status VARCHAR(30) NOT NULL DEFAULT '未接收' COMMENT '需求接收状态', " +
            "dispatch_supplier_count INT NOT NULL DEFAULT 0 COMMENT '分发供应商数', " +
            "received_supplier_count INT NOT NULL DEFAULT 0 COMMENT '已接收供应商数', " +
            "demand_status VARCHAR(20) NOT NULL DEFAULT '待分发' COMMENT '需求状态', " +
            "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', " +
            "create_user_id VARCHAR(20) NOT NULL COMMENT '创建用户ID', " +
            "create_user_name VARCHAR(50) NOT NULL COMMENT '创建用户姓名', " +
            "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间', " +
            "update_user_id VARCHAR(20) NOT NULL COMMENT '更新用户ID', " +
            "update_user_name VARCHAR(50) NOT NULL COMMENT '更新用户姓名', " +
            "UNIQUE KEY uk_source_recruitment_request_id (source_recruitment_request_id)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='需求管理主表'";
        jdbcTemplate.execute(createDemandRequirementSQL);
        logger.info("表 demand_requirement 创建成功");

        String createDemandDispatchSQL = "CREATE TABLE IF NOT EXISTS demand_dispatch (" +
            "dispatch_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分发ID', " +
            "demand_id BIGINT NOT NULL COMMENT '需求ID', " +
            "supplier_id BIGINT NOT NULL COMMENT '供应商ID', " +
            "supplier_name VARCHAR(100) NOT NULL COMMENT '供应商名称', " +
            "hr_user_id VARCHAR(20) NOT NULL COMMENT '供应商HR用户ID', " +
            "hr_user_name VARCHAR(50) NOT NULL COMMENT '供应商HR姓名', " +
            "receive_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '接收状态', " +
            "dispatch_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分发时间', " +
            "receive_time DATETIME DEFAULT NULL COMMENT '接收时间', " +
            "create_user_id VARCHAR(20) NOT NULL COMMENT '创建用户ID', " +
            "create_user_name VARCHAR(50) NOT NULL COMMENT '创建用户姓名', " +
            "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', " +
            "update_user_id VARCHAR(20) NOT NULL COMMENT '更新用户ID', " +
            "update_user_name VARCHAR(50) NOT NULL COMMENT '更新用户姓名', " +
            "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间', " +
            "UNIQUE KEY uk_demand_supplier_hr (demand_id, supplier_id, hr_user_id)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='需求分发表'";
        jdbcTemplate.execute(createDemandDispatchSQL);
        logger.info("表 demand_dispatch 创建成功");

        String createDemandOperationLogSQL = "CREATE TABLE IF NOT EXISTS demand_operation_log (" +
            "log_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID', " +
            "demand_id BIGINT NOT NULL COMMENT '需求ID', " +
            "operation_type VARCHAR(30) NOT NULL COMMENT '操作类型', " +
            "operation_detail VARCHAR(500) NOT NULL COMMENT '操作内容', " +
            "operator_user_id VARCHAR(20) NOT NULL COMMENT '操作人ID', " +
            "operator_user_name VARCHAR(50) NOT NULL COMMENT '操作人姓名', " +
            "operation_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间'" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='需求操作日志表'";
        jdbcTemplate.execute(createDemandOperationLogSQL);
        logger.info("表 demand_operation_log 创建成功");

        try {
            jdbcTemplate.execute("CREATE INDEX idx_demand_dispatch_demand_id ON demand_dispatch(demand_id)");
            logger.info("索引 idx_demand_dispatch_demand_id 创建成功");
        } catch (Exception e) {
            logger.info("索引 idx_demand_dispatch_demand_id 已存在或创建失败: {}", e.getMessage());
        }
        try {
            jdbcTemplate.execute("CREATE INDEX idx_demand_dispatch_hr_user_id ON demand_dispatch(hr_user_id)");
            logger.info("索引 idx_demand_dispatch_hr_user_id 创建成功");
        } catch (Exception e) {
            logger.info("索引 idx_demand_dispatch_hr_user_id 已存在或创建失败: {}", e.getMessage());
        }
        try {
            jdbcTemplate.execute("CREATE INDEX idx_demand_operation_log_demand_id ON demand_operation_log(demand_id)");
            logger.info("索引 idx_demand_operation_log_demand_id 创建成功");
        } catch (Exception e) {
            logger.info("索引 idx_demand_operation_log_demand_id 已存在或创建失败: {}", e.getMessage());
        }
    }

    private void cleanupLegacyPositionPublishingArtifacts() {
        String[] legacyTables = {
            "position_publishing",
            "position_publish",
            "position_publish_log"
        };
        for (String tableName : legacyTables) {
            try {
                jdbcTemplate.execute("DROP TABLE IF EXISTS " + tableName);
                logger.info("已清理历史岗位发布表: {}", tableName);
            } catch (Exception e) {
                logger.warn("清理历史岗位发布表失败 {}: {}", tableName, e.getMessage());
            }
        }

        try {
            jdbcTemplate.update("DELETE FROM sys_permission WHERE path = '/position-publishing'");
            logger.info("已清理历史岗位发布菜单权限");
        } catch (Exception e) {
            logger.warn("清理历史岗位发布菜单权限失败: {}", e.getMessage());
        }
    }

    private void ensureUserColumns() {
        addColumnIfMissing("sys_user", "team_name", "VARCHAR(100) COMMENT '团队名称'");
        addColumnIfMissing("sys_user", "group_name", "VARCHAR(100) COMMENT '室组名称'");
    }

    private void ensureDirectorRole() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM sys_role WHERE role_name = ?",
            Integer.class,
            "分管总"
        );
        if (count != null && count > 0) {
            return;
        }

        jdbcTemplate.update(
            "INSERT INTO sys_role (role_name, role_code, description, status, create_user_id, create_user_name, update_user_id, update_user_name) " +
                "VALUES (?, ?, ?, 'ACTIVE', '1001', '系统', '1001', '系统')",
            "分管总",
            "DIRECTOR",
            "直属团队最终审批角色"
        );
        logger.info("分管总角色创建成功");
    }

    private void ensureDirectorUsers() {
        ensureDirectorUser("1011", "dengjiansheng", "邓检生", "直属人员", "分管总", "分管总");
    }

    private void ensureDirectorUser(String userId, String username, String realName, String teamName, String groupName, String roleName) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM sys_user WHERE real_name = ?",
            Integer.class,
            realName
        );
        if (count == null || count == 0) {
            jdbcTemplate.update(
                "INSERT INTO sys_user (user_id, username, password, real_name, department, team_name, group_name, position, status, create_user_id, create_user_name, update_user_id, update_user_name) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE', '1001', '系统', '1001', '系统')",
                userId,
                username,
                passwordEncoder.encode("123321"),
                realName,
                groupName,
                teamName,
                groupName,
                roleName
            );
            logger.info("分管总用户创建成功: {}", realName);
        } else {
            jdbcTemplate.update(
                "UPDATE sys_user SET department = ?, team_name = ?, group_name = ?, position = ?, status = 'ACTIVE' WHERE real_name = ?",
                groupName,
                teamName,
                groupName,
                roleName,
                realName
            );
        }

        Long roleId = jdbcTemplate.queryForObject(
            "SELECT role_id FROM sys_role WHERE role_name = ? LIMIT 1",
            Long.class,
            roleName
        );
        if (roleId == null) {
            return;
        }

        Integer userRoleCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM sys_user_role WHERE user_id = ? AND role_id = ?",
            Integer.class,
            userId,
            roleId
        );
        if (userRoleCount == null || userRoleCount == 0) {
            jdbcTemplate.update(
                "INSERT INTO sys_user_role (user_id, role_id, create_user_id, create_user_name) VALUES (?, ?, '1001', '系统')",
                userId,
                roleId
            );
            logger.info("分管总角色关联创建成功: {}", realName);
        }
    }

    private void addColumnIfMissing(String tableName, String columnName, String definition) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
            Integer.class,
            tableName,
            columnName
        );
        if (count == null || count == 0) {
            jdbcTemplate.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition);
            logger.info("表 {} 新增字段 {}", tableName, columnName);
        }
    }
}
