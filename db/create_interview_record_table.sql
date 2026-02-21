-- 使用数据库
USE hr_system;

-- 创建面试记录表
CREATE TABLE IF NOT EXISTS interview_record (
    interview_record_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '面试记录ID',
    resume_id BIGINT COMMENT '简历ID',
    recruitment_request_id BIGINT COMMENT '招聘申请ID',
    interview_round VARCHAR(20) NOT NULL COMMENT '面试环节：FIRST_ROUND-一面，SECOND_ROUND-二面，THIRD_ROUND-三面',
    interviewer_id VARCHAR(20) COMMENT '面试官ID',
    interviewer_name VARCHAR(50) COMMENT '面试官姓名',
    interviewer_role VARCHAR(20) COMMENT '面试官角色：ROOM_MANAGER-室经理，TEAM_MANAGER-团队经理，DEPARTMENT_HEAD-分管总',
    interview_time DATETIME COMMENT '面试时间',
    interview_result VARCHAR(20) COMMENT '面试结果：PASSED-通过，FAILED-不通过',
    interview_comment TEXT COMMENT '面试评语',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user_id VARCHAR(20) COMMENT '创建人ID',
    create_user_name VARCHAR(50) COMMENT '创建人姓名',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_user_id VARCHAR(20) COMMENT '更新人ID',
    update_user_name VARCHAR(50) COMMENT '更新人姓名',
    INDEX idx_resume_id (resume_id),
    INDEX idx_recruitment_request_id (recruitment_request_id),
    INDEX idx_interview_round (interview_round),
    INDEX idx_interview_result (interview_result),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='面试记录表';

-- 修改resume表，增加面试状态字段
ALTER TABLE resume ADD COLUMN interview_status VARCHAR(20) DEFAULT 'NOT_SCHEDULED' COMMENT '面试状态：NOT_SCHEDULED-未安排，FIRST_ROUND-一面中，SECOND_ROUND-二面中，THIRD_ROUND-三面中，PASSED-全部通过，FAILED-未通过' AFTER status;

-- 添加索引
CREATE INDEX idx_interview_status ON resume(interview_status);
