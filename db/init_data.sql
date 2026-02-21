-- 使用数据库
USE hr_system;

-- 插入初始化数据
INSERT INTO recruitment_request (
    request_title, 
    total_recruitment_count, 
    vacancy_count, 
    interviewer, 
    position_or_team, 
    team_manager, 
    category, 
    technical_platform, 
    type, 
    supplement_count, 
    urgent_requirement, 
    proposed_level, 
    experience_years, 
    position_responsibility, 
    status, 
    create_user_id, 
    create_user_name, 
    update_user_id, 
    update_user_name
) VALUES (
    '2024年技术部门招聘申请', 
    50, 
    8, 
    '张三', 
    '前端开发组', 
    '李四', 
    'technical', 
    'frontend', 
    'fulltime', 
    5, 
    'yes', 
    'senior', 
    '3-5', 
    '负责公司前端开发工作，包括网站、APP等产品的前端实现，确保用户体验良好。', 
    'DRAFT', 
    '1001', 
    '系统管理员', 
    '1001', 
    '系统管理员'
);
