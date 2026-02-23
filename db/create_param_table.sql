-- 插入岗位参数数据
INSERT INTO sys_param (
    param_id, param_code, param_name, param_value, param_type, 
    status, sort_order, create_user_id, create_user_name, 
    update_user_id, update_user_name
) VALUES

('PARAM_POSITION_008', 'POSITION', '管理员', '管理员', 'POSITION', 'ACTIVE', 8, '1001', '系统管理员', '1001', '系统管理员')
ON DUPLICATE KEY UPDATE
    param_name = VALUES(param_name),
    param_value = VALUES(param_value),
    param_type = VALUES(param_type),
    status = VALUES(status),
    sort_order = VALUES(sort_order),
    update_time = CURRENT_TIMESTAMP,
    update_user_id = VALUES(update_user_id),
    update_user_name = VALUES(update_user_name);
