-- 查看当前数据库表结构
USE hr_system;

-- 查看所有表
SHOW TABLES;

-- 查看用户表结构
DESC sys_user;

-- 查看角色表结构（如果存在）
SHOW TABLES LIKE '%role%';

-- 查看权限相关表
SHOW TABLES LIKE '%permission%';