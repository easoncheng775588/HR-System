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

-- 创建录用记录表
CREATE TABLE IF NOT EXISTS offer_record (
    offer_record_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '录用记录ID',
    resume_id BIGINT NOT NULL COMMENT '简历ID',
    recruitment_request_id BIGINT NOT NULL COMMENT '招聘申请ID',
    candidate_name VARCHAR(100) NOT NULL COMMENT '候选人姓名',
    contact_phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    email VARCHAR(100) NOT NULL COMMENT '邮箱',
    position VARCHAR(255) NOT NULL COMMENT '录用岗位',
    status VARCHAR(20) NOT NULL COMMENT '录用状态',
    offer_time DATETIME COMMENT '录用时间',
    entry_time DATETIME COMMENT '入职时间',
    email_status VARCHAR(20) COMMENT '邮件发送状态',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user_id VARCHAR(20) NOT NULL COMMENT '创建用户ID',
    create_user_name VARCHAR(50) NOT NULL COMMENT '创建用户姓名',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_user_id VARCHAR(20) NOT NULL COMMENT '更新用户ID',
    update_user_name VARCHAR(50) NOT NULL COMMENT '更新用户姓名'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='录用记录表';

-- 创建邮件模板表
CREATE TABLE IF NOT EXISTS email_template (
    template_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
    template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
    template_type VARCHAR(50) NOT NULL COMMENT '模板类型',
    subject VARCHAR(255) NOT NULL COMMENT '邮件主题',
    content TEXT NOT NULL COMMENT '邮件内容',
    status VARCHAR(20) NOT NULL COMMENT '状态',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user_id VARCHAR(20) NOT NULL COMMENT '创建用户ID',
    create_user_name VARCHAR(50) NOT NULL COMMENT '创建用户姓名',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_user_id VARCHAR(20) NOT NULL COMMENT '更新用户ID',
    update_user_name VARCHAR(50) NOT NULL COMMENT '更新用户姓名'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮件模板表';

-- 创建索引
CREATE INDEX idx_resume_id ON offer_record(resume_id);
CREATE INDEX idx_status ON offer_record(status);
CREATE INDEX idx_email_status ON offer_record(email_status);
CREATE INDEX idx_template_type ON email_template(template_type);
CREATE INDEX idx_status ON email_template(status);
