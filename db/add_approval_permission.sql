-- 增加审批权限和设置eric用户具有审批权限
USE hr_system;

-- 1. 插入审批权限
INSERT INTO sys_permission (permission_id, permission_code, permission_name, description, status, create_time, create_user_id, create_user_name)
VALUES ('APPROVAL_MANAGE', 'APPROVAL_MANAGE', '审批管理', '用人申请审批权限', 'ACTIVE', NOW(), '1001', '系统')
ON DUPLICATE KEY UPDATE permission_name = '审批管理', description = '用人申请审批权限';

-- 2. 创建审批角色
INSERT INTO sys_role (role_id, role_name, role_code, description, status, create_time, create_user_id, create_user_name)
VALUES (2, '审批管理员', 'APPROVAL_ADMIN', '拥有审批管理权限', 'ACTIVE', NOW(), '1001', '系统')
ON DUPLICATE KEY UPDATE role_name = '审批管理员', description = '拥有审批管理权限';

-- 3. 给审批角色分配审批权限
INSERT INTO sys_role_permission (role_id, permission_id, create_time, create_user_id, create_user_name)
VALUES (2, 'APPROVAL_MANAGE', NOW(), '1001', '系统')
ON DUPLICATE KEY UPDATE create_time = NOW();

-- 4. 给eric用户分配审批角色
INSERT INTO sys_user_role (user_id, role_id, create_time, create_user_id, create_user_name)
VALUES ('eric', 2, NOW(), '1001', '系统')
ON DUPLICATE KEY UPDATE create_time = NOW();

-- 5. 验证结果
SELECT '权限表数据：' as info;
SELECT * FROM sys_permission;

SELECT '角色表数据：' as info;
SELECT * FROM sys_role;

SELECT '角色权限关联数据：' as info;
SELECT * FROM sys_role_permission;

SELECT '用户角色关联数据：' as info;
SELECT * FROM sys_user_role;

SELECT 'eric用户的角色：' as info;
SELECT u.*, r.role_name, r.role_code 
FROM sys_user u 
LEFT JOIN sys_user_role ur ON u.user_id = ur.user_id 
LEFT JOIN sys_role r ON ur.role_id = r.role_id 
WHERE u.user_id = 'eric';