-- 为eric用户添加更多权限
USE hr_system;

-- 添加用人申请管理权限
INSERT INTO sys_permission (permission_name, permission_code, permission_type, path, status, create_time, create_user_id, create_user_name)
VALUES ('用人申请管理', 'RECRUITMENT_MANAGE', 'MENU', '/recruitment-request', 'ACTIVE', NOW(), '1001', '系统')
ON DUPLICATE KEY UPDATE permission_name = '用人申请管理', path = '/recruitment-request';

-- 添加用户管理权限
INSERT INTO sys_permission (permission_name, permission_code, permission_type, path, status, create_time, create_user_id, create_user_name)
VALUES ('用户管理', 'USER_MANAGE', 'MENU', '/user-management', 'ACTIVE', NOW(), '1001', '系统')
ON DUPLICATE KEY UPDATE permission_name = '用户管理', path = '/user-management';

-- 为审批管理员角色分配所有权限
INSERT INTO sys_role_permission (role_id, permission_id, create_time, create_user_id, create_user_name)
SELECT 2, permission_id, NOW(), '1001', '系统'
FROM sys_permission 
WHERE permission_code IN ('RECRUITMENT_MANAGE', 'USER_MANAGE')
ON DUPLICATE KEY UPDATE create_time = NOW();

-- 验证结果
SELECT 'eric用户角色的所有权限：' as info;
SELECT p.permission_id, p.permission_name, p.permission_code, p.permission_type, p.path
FROM sys_permission p
JOIN sys_role_permission rp ON p.permission_id = rp.permission_id
JOIN sys_user_role ur ON rp.role_id = ur.role_id
WHERE ur.user_id = 'eric';