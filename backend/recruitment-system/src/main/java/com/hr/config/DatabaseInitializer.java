package com.hr.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据库初始化配置类
 * 负责在系统启动时初始化数据库表结构和基础数据
 * 实现CommandLineRunner接口，在Spring Boot应用启动后执行
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) throws Exception {
        System.out.println("开始初始化数据库...");
        
        try {
            // 创建数据库
            jdbcTemplate.execute("CREATE DATABASE IF NOT EXISTS hr_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            System.out.println("数据库 hr_system 创建成功");
            
            // 使用数据库
            jdbcTemplate.execute("USE hr_system");
            
            // 创建用人申请表
            String createTableSQL = "CREATE TABLE IF NOT EXISTS recruitment_request (" +
                "recruitment_request_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用人申请ID', " +
                "request_title VARCHAR(255) NOT NULL COMMENT '申请标题', " +
                "total_recruitment_count INT NOT NULL COMMENT '总编制人数', " +
                "vacancy_count INT NOT NULL COMMENT '空缺编制', " +
                "team VARCHAR(100) NOT NULL COMMENT '所属团队', " +
                "technical_platform VARCHAR(50) NOT NULL COMMENT '技术平台', " +
                "supplement_count INT NOT NULL COMMENT '补充人数', " +
                "urgent_requirement VARCHAR(10) NOT NULL COMMENT '是否近期紧急要求', " +
                "proposed_level VARCHAR(50) NOT NULL COMMENT '建议级别', " +
                "experience_years VARCHAR(50) NOT NULL COMMENT '相关经验年限要求', " +
                "skill_requirement TEXT COMMENT '技能要求描述', " +
                "position_responsibility TEXT NOT NULL COMMENT '岗位职责', " +
                "status VARCHAR(20) NOT NULL COMMENT '状态（DRAFT：草稿，SUBMITTED：已提交）', " +
                "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', " +
                "create_user_id VARCHAR(20) NOT NULL COMMENT '创建用户ID', " +
                "create_user_name VARCHAR(50) NOT NULL COMMENT '创建用户姓名', " +
                "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间', " +
                "update_user_id VARCHAR(20) NOT NULL COMMENT '更新用户ID', " +
                "update_user_name VARCHAR(50) NOT NULL COMMENT '更新用户姓名'" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用人申请表'";
            
            jdbcTemplate.execute(createTableSQL);
            System.out.println("表 recruitment_request 创建成功");
            
            // 创建索引（MySQL不支持CREATE INDEX IF NOT EXISTS，先检查索引是否存在）
            try {
                jdbcTemplate.execute("CREATE INDEX idx_status ON recruitment_request(status)");
                System.out.println("索引 idx_status 创建成功");
            } catch (Exception e) {
                System.out.println("索引 idx_status 已存在或创建失败: " + e.getMessage());
            }
            
            try {
                jdbcTemplate.execute("CREATE INDEX idx_create_time ON recruitment_request(create_time)");
                System.out.println("索引 idx_create_time 创建成功");
            } catch (Exception e) {
                System.out.println("索引 idx_create_time 已存在或创建失败: " + e.getMessage());
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
            System.out.println("表 sys_user 创建成功");
            
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
            System.out.println("表 sys_role 创建成功");
            
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
            System.out.println("表 sys_permission 创建成功");
            
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
            System.out.println("表 sys_user_role 创建成功");
            
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
            System.out.println("表 sys_role_permission 创建成功");
            
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
            System.out.println("表 offer_record 创建成功");
            
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
            System.out.println("表 email_template 创建成功");
            
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
            System.out.println("表 approval_history 创建成功");
            
            // 创建审批历史表索引
            try {
                jdbcTemplate.execute("CREATE INDEX idx_recruitment_request_id ON approval_history(recruitment_request_id)");
                System.out.println("索引 idx_recruitment_request_id 创建成功");
            } catch (Exception e) {
                System.out.println("索引 idx_recruitment_request_id 已存在或创建失败: " + e.getMessage());
            }
            
            // 创建索引
            try {
                jdbcTemplate.execute("CREATE INDEX idx_resume_id ON offer_record(resume_id)");
                System.out.println("索引 idx_resume_id 创建成功");
            } catch (Exception e) {
                System.out.println("索引 idx_resume_id 已存在或创建失败: " + e.getMessage());
            }
            
            try {
                jdbcTemplate.execute("CREATE INDEX idx_status ON offer_record(status)");
                System.out.println("索引 idx_status 创建成功");
            } catch (Exception e) {
                System.out.println("索引 idx_status 已存在或创建失败: " + e.getMessage());
            }
            
            try {
                jdbcTemplate.execute("CREATE INDEX idx_email_status ON offer_record(email_status)");
                System.out.println("索引 idx_email_status 创建成功");
            } catch (Exception e) {
                System.out.println("索引 idx_email_status 已存在或创建失败: " + e.getMessage());
            }
            
            try {
                jdbcTemplate.execute("CREATE INDEX idx_template_type ON email_template(template_type)");
                System.out.println("索引 idx_template_type 创建成功");
            } catch (Exception e) {
                System.out.println("索引 idx_template_type 已存在或创建失败: " + e.getMessage());
            }
            
            try {
                jdbcTemplate.execute("CREATE INDEX idx_status ON email_template(status)");
                System.out.println("索引 idx_status 创建成功");
            } catch (Exception e) {
                System.out.println("索引 idx_status 已存在或创建失败: " + e.getMessage());
            }
            
            // 初始化建议级别参数
            initProposedLevelParams();
            
            // 初始化所属团队参数
            initTeamParams();
            
            // 初始化超级管理员数据
            initSuperAdmin();
            
            System.out.println("数据库初始化完成！");
            
        } catch (Exception e) {
            System.err.println("数据库初始化失败: " + e.getMessage());
            e.printStackTrace();
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
                    System.out.println("超级管理员用户创建成功");
                    
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
                    System.out.println("超级管理员角色创建成功");
                    
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
                    System.out.println("权限数据创建成功");
                    
                    // 为超级管理员角色分配所有权限
                    jdbcTemplate.update(
                        "INSERT INTO sys_role_permission (role_id, permission_id, create_user_id, create_user_name) " +
                        "SELECT 1, permission_id, ?, ? FROM sys_permission",
                        Constants.SUPER_ADMIN_USER_ID,
                        Constants.SYSTEM_USER
                    );
                    System.out.println("角色权限关联创建成功");
                    
                    // 为超级管理员用户分配超级管理员角色
                    jdbcTemplate.update(
                        "INSERT INTO sys_user_role (user_id, role_id, create_user_id, create_user_name) " +
                        "VALUES (?, 1, ?, ?)",
                        Constants.SUPER_ADMIN_USER_ID,
                        Constants.SUPER_ADMIN_USER_ID,
                        Constants.SYSTEM_USER
                    );
                    System.out.println("用户角色关联创建成功");
                    
                    System.out.println("超级管理员数据初始化完成！");
                } else {
                    System.out.println("超级管理员用户已存在，跳过初始化");
                }
            } catch (Exception e) {
                System.err.println("超级管理员数据初始化失败: " + e.getMessage());
                e.printStackTrace();
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
                System.out.println("建议级别参数初始化完成！");
            } else {
                System.out.println("建议级别参数已存在，跳过初始化");
            }
        } catch (Exception e) {
            System.err.println("建议级别参数初始化失败: " + e.getMessage());
            e.printStackTrace();
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
                System.out.println("所属团队参数初始化完成！");
            } else {
                System.out.println("所属团队参数已存在，跳过初始化");
            }
        } catch (Exception e) {
            System.err.println("所属团队参数初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}