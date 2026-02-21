-- 查看所有用户
USE hr_system;

SELECT user_id, username, password, real_name, email, phone, department, position, status 
FROM sys_user;