-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS hr_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE hr_system;

-- 创建用人申请表
CREATE TABLE IF NOT EXISTS recruitment_request (
    recruitment_request_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用人申请ID',
    request_title VARCHAR(255) NOT NULL COMMENT '申请标题',
    total_recruitment_count INT NOT NULL COMMENT '总编制人数',
    vacancy_count INT NOT NULL COMMENT '空缺编制',
    interviewer VARCHAR(100) NOT NULL COMMENT '面试官',
    position_or_team VARCHAR(255) NOT NULL COMMENT '岗位/用人班组',
    team_manager VARCHAR(100) NOT NULL COMMENT '所属团队经理',
    category VARCHAR(50) NOT NULL COMMENT '所属分类',
    technical_platform VARCHAR(50) NOT NULL COMMENT '技术平台',
    type VARCHAR(50) NOT NULL COMMENT '所属类型',
    supplement_count INT NOT NULL COMMENT '补充人数',
    urgent_requirement VARCHAR(10) NOT NULL COMMENT '是否近期紧急要求',
    proposed_level VARCHAR(50) NOT NULL COMMENT '建议级别',
    experience_years VARCHAR(50) NOT NULL COMMENT '相关经验年限要求',
    position_responsibility TEXT NOT NULL COMMENT '岗位职责',
    status VARCHAR(20) NOT NULL COMMENT '状态（DRAFT：草稿，SUBMITTED：已提交）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user_id VARCHAR(20) NOT NULL COMMENT '创建用户ID',
    create_user_name VARCHAR(50) NOT NULL COMMENT '创建用户姓名',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_user_id VARCHAR(20) NOT NULL COMMENT '更新用户ID',
    update_user_name VARCHAR(50) NOT NULL COMMENT '更新用户姓名'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用人申请表';

-- 创建索引
CREATE INDEX idx_status ON recruitment_request(status);
CREATE INDEX idx_create_time ON recruitment_request(create_time);
