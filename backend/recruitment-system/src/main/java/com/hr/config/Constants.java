package com.hr.config;

/**
 * 系统常量类
 * 存储系统中使用的常量值，避免硬编码
 * 包含用户、角色、参数类型、状态等系统常量
 */
public class Constants {
    
    // 用户相关常量
    public static final String SUPER_ADMIN_USER_ID = "1001";
    public static final String SUPER_ADMIN_USERNAME = "admin";
    public static final String SUPER_ADMIN_REAL_NAME = "超级管理员";
    public static final String SYSTEM_USER = "系统";
    
    // 角色相关常量
    public static final String SUPER_ADMIN_ROLE_NAME = "超级管理员";
    public static final String SUPER_ADMIN_ROLE_CODE = "SUPER_ADMIN";
    public static final String SUPER_ADMIN_ROLE_DESC = "拥有系统所有权限";
    
    // 参数类型常量
    public static final String PARAM_TYPE_LEVEL = "LEVEL";
    public static final String PARAM_TYPE_TEAM = "TEAM";
    
    // 状态常量
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_DISABLED = "DISABLED";
    
    // 权限类型常量
    public static final String PERMISSION_TYPE_MENU = "MENU";
    public static final String PERMISSION_TYPE_BUTTON = "BUTTON";
    
    // 审批状态常量
    public static final String APPROVAL_STATUS_PENDING = "PENDING";
    public static final String APPROVAL_STATUS_APPROVED = "APPROVED";
    public static final String APPROVAL_STATUS_REJECTED = "REJECTED";
    
    // 招聘申请状态常量
    public static final String RECRUITMENT_STATUS_DRAFT = "DRAFT";
    public static final String RECRUITMENT_STATUS_SUBMITTED = "SUBMITTED";
    
    // 邮件状态常量
    public static final String EMAIL_STATUS_PENDING = "PENDING";
    public static final String EMAIL_STATUS_SENT = "SENT";
    public static final String EMAIL_STATUS_FAILED = "FAILED";
}
