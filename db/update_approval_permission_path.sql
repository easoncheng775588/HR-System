-- 更新审批管理权限的path
USE hr_system;

UPDATE sys_permission 
SET path = '/approval-management'
WHERE permission_code = 'APPROVAL_MANAGE';

-- 验证结果
SELECT '所有权限：' as info;
SELECT permission_id, permission_name, permission_code, permission_type, path, status
FROM sys_permission;

SELECT 'eric用户角色的所有权限：' as info;
SELECT p.permission_id, p.permission_name, p.permission_code, p.permission_type, p.path
FROM sys_permission p
JOIN sys_role_permission rp ON p.permission_id = rp.permission_id
JOIN sys_user_role ur ON rp.role_id = ur.role_id
WHERE ur.user_id = 'eric';