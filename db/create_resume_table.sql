-- 使用数据库
USE hr_system;

-- 创建简历表
CREATE TABLE IF NOT EXISTS resume (
    resume_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '简历ID',
    recruitment_request_id BIGINT COMMENT '招聘申请ID',
    job_title VARCHAR(255) COMMENT '岗位标题',
    applicant_name VARCHAR(50) NOT NULL COMMENT '申请人姓名',
    contact_phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    email VARCHAR(100) NOT NULL COMMENT '邮箱',
    education VARCHAR(50) NOT NULL COMMENT '学历',
    work_experience VARCHAR(255) NOT NULL COMMENT '工作经验',
    resume_file_name VARCHAR(255) NOT NULL COMMENT '简历文件名',
    resume_file_url VARCHAR(255) NOT NULL COMMENT '简历文件URL',
    status VARCHAR(20) DEFAULT 'PENDING_SCREENING' COMMENT '简历状态（PENDING_SCREENING：待筛选，SCREENED：已筛选，INTERVIEW：面试中，HIRED：已录用，REJECTED：已拒绝）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user_id VARCHAR(20) COMMENT '创建人ID',
    create_user_name VARCHAR(50) COMMENT '创建人姓名',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_user_id VARCHAR(20) COMMENT '更新人ID',
    update_user_name VARCHAR(50) COMMENT '更新人姓名',
    INDEX idx_recruitment_request_id (recruitment_request_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简历表';
