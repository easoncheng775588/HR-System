-- 检查eric用户的完整权限信息
USE hr_system;

SELECT 'eric用户信息：' as info;
SELECT * FROM sys_user WHERE user_id = 'eric';

SELECT 'eric用户的角色：' as info;
SELECT ur.*, r.role_name, r.role_code 
FROM sys_user_role ur 
JOIN sys_role r ON ur.role_id = r.role_id 
WHERE ur.user_id = 'eric';

SELECT 'eric用户角色的权限：' as info;
SELECT p.permission_id, p.permission_name, p.permission_code, p.permission_type, p.path
FROM sys_permission p
JOIN sys_role_permission rp ON p.permission_id = rp.permission_id
JOIN sys_user_role ur ON rp.role_id = ur.role_id
WHERE ur.user_id = 'eric';

SELECT '所有可用权限：' as info;
SELECT * FROM sys_permission;