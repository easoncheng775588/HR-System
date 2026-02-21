-- 查看eric用户的详细信息，包括字段分隔
USE hr_system;

SELECT 
    user_id,
    username,
    password,
    real_name,
    email,
    phone,
    department,
    position,
    status
FROM sys_user
WHERE username LIKE '%eric%' OR real_name LIKE '%eric%';