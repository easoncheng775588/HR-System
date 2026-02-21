-- 修复eric用户的用户名，将"eric shen"改为"eric"
USE hr_system;

UPDATE sys_user 
SET username = 'eric'
WHERE user_id = 'eric';

-- 验证修改结果
SELECT user_id, username, password, real_name, email, phone, department, position, status 
FROM sys_user 
WHERE user_id = 'eric';