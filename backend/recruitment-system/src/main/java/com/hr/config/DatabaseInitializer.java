package com.hr.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
                "interviewer VARCHAR(100) NOT NULL COMMENT '面试官', " +
                "position_or_team VARCHAR(255) NOT NULL COMMENT '岗位/用人班组', " +
                "team_manager VARCHAR(100) NOT NULL COMMENT '所属团队经理', " +
                "category VARCHAR(50) NOT NULL COMMENT '所属分类', " +
                "technical_platform VARCHAR(50) NOT NULL COMMENT '技术平台', " +
                "type VARCHAR(50) NOT NULL COMMENT '所属类型', " +
                "supplement_count INT NOT NULL COMMENT '补充人数', " +
                "urgent_requirement VARCHAR(10) NOT NULL COMMENT '是否近期紧急要求', " +
                "proposed_level VARCHAR(50) NOT NULL COMMENT '建议级别', " +
                "experience_years VARCHAR(50) NOT NULL COMMENT '相关经验年限要求', " +
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
                "SELECT COUNT(*) FROM sys_user WHERE user_id = '1001'",
                Integer.class
            );
            
            if (count == 0) {
                // 创建超级管理员用户
                jdbcTemplate.update(
                    "INSERT INTO sys_user (user_id, username, password, real_name, email, phone, department, position, status, create_user_id, create_user_name, update_user_id, update_user_name) " +
                    "VALUES ('1001', 'admin', '123321', '超级管理员', 'admin@hr.com', '13800138000', '人事部', '系统管理员', 'ACTIVE', '1001', '系统', '1001', '系统')"
                );
                System.out.println("超级管理员用户创建成功");
                
                // 创建超级管理员角色
                jdbcTemplate.update(
                    "INSERT INTO sys_role (role_name, role_code, description, status, create_user_id, create_user_name, update_user_id, update_user_name) " +
                    "VALUES ('超级管理员', 'SUPER_ADMIN', '拥有系统所有权限', 'ACTIVE', '1001', '系统', '1001', '系统')"
                );
                System.out.println("超级管理员角色创建成功");
                
                // 创建权限
                String[] permissions = new String[] {
                    "('用人申请管理', 'RECRUITMENT_REQUEST_MANAGE', 'MENU', 0, '/recruitment-request', 'CheckCircleOutlined', 1, 'ACTIVE')",
                    "('用户管理', 'USER_MANAGE', 'MENU', 0, '/user-management', 'UserOutlined', 2, 'ACTIVE')",
                    "('新增用人申请', 'RECRUITMENT_REQUEST_ADD', 'BUTTON', 1, null, null, 1, 'ACTIVE')",
                    "('编辑用人申请', 'RECRUITMENT_REQUEST_EDIT', 'BUTTON', 1, null, null, 2, 'ACTIVE')",
                    "('删除用人申请', 'RECRUITMENT_REQUEST_DELETE', 'BUTTON', 1, null, null, 3, 'ACTIVE')",
                    "('新增用户', 'USER_ADD', 'BUTTON', 2, null, null, 1, 'ACTIVE')",
                    "('编辑用户', 'USER_EDIT', 'BUTTON', 2, null, null, 2, 'ACTIVE')",
                    "('删除用户', 'USER_DELETE', 'BUTTON', 2, null, null, 3, 'ACTIVE')"
                };
                
                for (String perm : permissions) {
                    jdbcTemplate.update(
                        "INSERT INTO sys_permission (permission_name, permission_code, permission_type, parent_id, path, icon, sort_order, status, create_user_id, create_user_name, update_user_id, update_user_name) " +
                        "VALUES " + perm + ", '1001', '系统', '1001', '系统')"
                    );
                }
                System.out.println("权限数据创建成功");
                
                // 为超级管理员角色分配所有权限
                jdbcTemplate.update(
                    "INSERT INTO sys_role_permission (role_id, permission_id, create_user_id, create_user_name) " +
                    "SELECT 1, permission_id, '1001', '系统' FROM sys_permission"
                );
                System.out.println("角色权限关联创建成功");
                
                // 为超级管理员用户分配超级管理员角色
                jdbcTemplate.update(
                    "INSERT INTO sys_user_role (user_id, role_id, create_user_id, create_user_name) " +
                    "VALUES ('1001', 1, '1001', '系统')"
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
}