-- 查看现有权限和角色数据
USE hr_system;

-- 查看所有权限
SELECT * FROM sys_permission;

-- 查看所有角色
SELECT * FROM sys_role;

-- 查看角色权限关联
SELECT * FROM sys_role_permission;

-- 查看用户角色关联
SELECT * FROM sys_user_role;