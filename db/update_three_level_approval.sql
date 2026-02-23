-- 修改recruitment_request表，添加三级审批相关字段
ALTER TABLE recruitment_request
-- 第一级审批（编制管理岗）
ADD COLUMN approval_level1_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '第一级审批状态（PENDING：待审批，APPROVED：已通过，REJECTED：已拒绝）',
ADD COLUMN approval_level1_user_id VARCHAR(20) COMMENT '第一级审批人ID',
ADD COLUMN approval_level1_user_name VARCHAR(50) COMMENT '第一级审批人姓名',
ADD COLUMN approval_level1_time DATETIME COMMENT '第一级审批时间',
ADD COLUMN approval_level1_comment TEXT COMMENT '第一级审批意见',

-- 第二级审批（外包管理岗）
ADD COLUMN approval_level2_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '第二级审批状态（PENDING：待审批，APPROVED：已通过，REJECTED：已拒绝）',
ADD COLUMN approval_level2_user_id VARCHAR(20) COMMENT '第二级审批人ID',
ADD COLUMN approval_level2_user_name VARCHAR(50) COMMENT '第二级审批人姓名',
ADD COLUMN approval_level2_time DATETIME COMMENT '第二级审批时间',
ADD COLUMN approval_level2_comment TEXT COMMENT '第二级审批意见',

-- 第三级审批（团队经理）
ADD COLUMN approval_level3_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '第三级审批状态（PENDING：待审批，APPROVED：已通过，REJECTED：已拒绝）',
ADD COLUMN approval_level3_user_id VARCHAR(20) COMMENT '第三级审批人ID',
ADD COLUMN approval_level3_user_name VARCHAR(50) COMMENT '第三级审批人姓名',
ADD COLUMN approval_level3_time DATETIME COMMENT '第三级审批时间',
ADD COLUMN approval_level3_comment TEXT COMMENT '第三级审批意见',

-- 审批流程当前阶段
ADD COLUMN current_approval_level INT NOT NULL DEFAULT 1 COMMENT '当前审批阶段（1：第一级，2：第二级，3：第三级，0：已完成）';

-- 修改现有审批状态字段的默认值
ALTER TABLE recruitment_request
MODIFY COLUMN approval_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '整体审批状态（PENDING：待审批，APPROVED：已通过，REJECTED：已拒绝）';

-- 添加索引
CREATE INDEX idx_approval_level1_status ON recruitment_request(approval_level1_status);
CREATE INDEX idx_approval_level2_status ON recruitment_request(approval_level2_status);
CREATE INDEX idx_approval_level3_status ON recruitment_request(approval_level3_status);
CREATE INDEX idx_current_approval_level ON recruitment_request(current_approval_level);
