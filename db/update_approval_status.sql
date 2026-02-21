-- 更新现有用人申请数据，将状态改为待审批
USE hr_system;

-- 将所有已提交的申请的审批状态设置为待审批
UPDATE recruitment_request 
SET approval_status = 'PENDING' 
WHERE status = 'SUBMITTED' AND (approval_status IS NULL OR approval_status = '');

-- 查询更新结果
SELECT 
    recruitment_request_id,
    request_title,
    status,
    approval_status,
    create_time
FROM recruitment_request
ORDER BY create_time DESC;