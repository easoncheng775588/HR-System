-- 查看eric用户详细信息
USE hr_system;

SELECT user_id, username, password, real_name, email, phone, department, position, status 
FROM sys_user 
WHERE username = 'eric';