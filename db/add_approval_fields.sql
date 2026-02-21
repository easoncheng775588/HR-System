-- 修改用人申请表结构，增加审批相关字段
USE hr_system;

-- 修改status字段，增加更多状态
-- 状态说明：
-- DRAFT：草稿
-- SUBMITTED：已提交待审批
-- APPROVED：已通过
-- REJECTED：已拒绝

-- 添加审批相关字段
ALTER TABLE recruitment_request 
ADD COLUMN approval_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '审批状态（PENDING：待审批，APPROVED：已通过，REJECTED：已拒绝）' AFTER status,
ADD COLUMN approval_user_id VARCHAR(20) COMMENT '审批人ID' AFTER approval_status,
ADD COLUMN approval_user_name VARCHAR(50) COMMENT '审批人姓名' AFTER approval_user_id,
ADD COLUMN approval_time DATETIME COMMENT '审批时间' AFTER approval_user_name,
ADD COLUMN approval_comment TEXT COMMENT '审批意见' AFTER approval_time;

-- 更新现有数据的审批状态
UPDATE recruitment_request SET approval_status = 'PENDING' WHERE status = 'SUBMITTED';

-- 添加索引
CREATE INDEX idx_approval_status ON recruitment_request(approval_status);