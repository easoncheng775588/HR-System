# 人力资源管理系统数据库设计文档

## 1. 数据库概述

本数据库设计文档描述了人力资源管理系统的数据库结构，包括表结构、字段定义、索引设计和表关系等内容。系统采用 MySQL 数据库，使用 InnoDB 存储引擎，支持事务处理和外键约束。

## 2. 数据库结构

### 2.1 数据库创建

```sql
CREATE DATABASE IF NOT EXISTS hr_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hr_system;
```

### 2.2 表结构设计

#### 2.2.1 用户表 (sys_user)

| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| user_id | VARCHAR(20) | NO | PRI | 用户ID |
| username | VARCHAR(50) | NO | UNI | 用户名 |
| password | VARCHAR(255) | NO | | 密码 |
| real_name | VARCHAR(50) | NO | | 真实姓名 |
| email | VARCHAR(100) | YES | | 邮箱 |
| phone | VARCHAR(20) | YES | | 电话 |
| department | VARCHAR(100) | YES | | 部门 |
| position | VARCHAR(100) | YES | | 职位 |
| status | VARCHAR(20) | NO | | 状态（ACTIVE：激活，INACTIVE：未激活） |
| create_time | DATETIME | NO | | 创建时间 |
| create_user_id | VARCHAR(20) | YES | | 创建用户ID |
| create_user_name | VARCHAR(50) | YES | | 创建用户姓名 |
| update_time | DATETIME | NO | | 更新时间 |
| update_user_id | VARCHAR(20) | YES | | 更新用户ID |
| update_user_name | VARCHAR(50) | YES | | 更新用户姓名 |

**索引设计**：
- PRIMARY KEY: user_id
- UNIQUE INDEX: username
- INDEX: status

#### 2.2.2 角色表 (sys_role)

| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| role_id | BIGINT | NO | PRI | 角色ID |
| role_name | VARCHAR(50) | NO | UNI | 角色名称 |
| role_code | VARCHAR(50) | NO | UNI | 角色代码 |
| description | VARCHAR(255) | YES | | 角色描述 |
| status | VARCHAR(20) | NO | | 状态（ACTIVE：激活，INACTIVE：未激活） |
| create_time | DATETIME | NO | | 创建时间 |
| create_user_id | VARCHAR(20) | YES | | 创建用户ID |
| create_user_name | VARCHAR(50) | YES | | 创建用户姓名 |
| update_time | DATETIME | NO | | 更新时间 |
| update_user_id | VARCHAR(20) | YES | | 更新用户ID |
| update_user_name | VARCHAR(50) | YES | | 更新用户姓名 |

**索引设计**：
- PRIMARY KEY: role_id
- UNIQUE INDEX: role_name
- UNIQUE INDEX: role_code

#### 2.2.3 权限表 (sys_permission)

| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| permission_id | BIGINT | NO | PRI | 权限ID |
| permission_name | VARCHAR(100) | NO | | 权限名称 |
| permission_code | VARCHAR(100) | NO | UNI | 权限代码 |
| permission_type | VARCHAR(20) | NO | | 权限类型 |
| parent_id | BIGINT | YES | | 父权限ID |
| path | VARCHAR(255) | YES | | 路径 |
| icon | VARCHAR(100) | YES | | 图标 |
| sort_order | INT | YES | | 排序顺序 |
| status | VARCHAR(20) | NO | | 状态（ACTIVE：激活，INACTIVE：未激活） |
| create_time | DATETIME | NO | | 创建时间 |
| create_user_id | VARCHAR(20) | YES | | 创建用户ID |
| create_user_name | VARCHAR(50) | YES | | 创建用户姓名 |
| update_time | DATETIME | NO | | 更新时间 |
| update_user_id | VARCHAR(20) | YES | | 更新用户ID |
| update_user_name | VARCHAR(50) | YES | | 更新用户姓名 |

**索引设计**：
- PRIMARY KEY: permission_id
- UNIQUE INDEX: permission_code
- INDEX: parent_id
- INDEX: status

#### 2.2.4 用户角色关联表 (sys_user_role)

| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| user_role_id | BIGINT | NO | PRI | 用户角色关联ID |
| user_id | VARCHAR(20) | NO | | 用户ID |
| role_id | BIGINT | NO | | 角色ID |
| create_time | DATETIME | NO | | 创建时间 |
| create_user_id | VARCHAR(20) | YES | | 创建用户ID |
| create_user_name | VARCHAR(50) | YES | | 创建用户姓名 |

**索引设计**：
- PRIMARY KEY: user_role_id
- INDEX: user_id
- INDEX: role_id

#### 2.2.5 角色权限关联表 (sys_role_permission)

| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| role_permission_id | BIGINT | NO | PRI | 角色权限关联ID |
| role_id | BIGINT | NO | | 角色ID |
| permission_id | BIGINT | NO | | 权限ID |
| create_time | DATETIME | NO | | 创建时间 |
| create_user_id | VARCHAR(20) | YES | | 创建用户ID |
| create_user_name | VARCHAR(50) | YES | | 创建用户姓名 |

**索引设计**：
- PRIMARY KEY: role_permission_id
- INDEX: role_id
- INDEX: permission_id

#### 2.2.6 系统参数表 (sys_param)

| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| param_id | VARCHAR(50) | NO | PRI | 参数ID |
| param_code | VARCHAR(100) | NO | MUL | 参数代码 |
| param_name | VARCHAR(100) | NO | | 参数名称 |
| param_value | VARCHAR(500) | NO | | 参数值 |
| param_type | VARCHAR(50) | NO | MUL | 参数类型 |
| status | VARCHAR(20) | NO | MUL | 状态（ACTIVE：激活，INACTIVE：未激活） |
| sort_order | INT | NO | | 排序顺序 |
| create_time | DATETIME | NO | | 创建时间 |
| create_user_id | VARCHAR(50) | NO | | 创建用户ID |
| create_user_name | VARCHAR(100) | NO | | 创建用户姓名 |
| update_time | DATETIME | NO | | 更新时间 |
| update_user_id | VARCHAR(50) | NO | | 更新用户ID |
| update_user_name | VARCHAR(100) | NO | | 更新用户姓名 |

**索引设计**：
- PRIMARY KEY: param_id
- INDEX: param_code
- INDEX: param_type
- INDEX: status

#### 2.2.7 系统配置表 (sys_config)

| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| config_id | BIGINT | NO | PRI | 配置ID |
| config_key | VARCHAR(100) | NO | UNI | 配置键 |
| config_value | VARCHAR(500) | NO | | 配置值 |
| description | VARCHAR(255) | YES | | 配置描述 |
| status | VARCHAR(20) | NO | | 状态（ACTIVE：激活，INACTIVE：未激活） |
| create_time | DATETIME | NO | | 创建时间 |
| create_user_id | VARCHAR(20) | YES | | 创建用户ID |
| create_user_name | VARCHAR(50) | YES | | 创建用户姓名 |
| update_time | DATETIME | NO | | 更新时间 |
| update_user_id | VARCHAR(20) | YES | | 更新用户ID |
| update_user_name | VARCHAR(50) | YES | | 更新用户姓名 |

**索引设计**：
- PRIMARY KEY: config_id
- UNIQUE INDEX: config_key
- INDEX: status

#### 2.2.8 用人申请表 (recruitment_request)

| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| recruitment_request_id | BIGINT | PRIMARY KEY AUTO_INCREMENT | 用人申请ID |
| request_title | VARCHAR(255) | NOT NULL | 申请标题 |
| total_recruitment_count | INT | NOT NULL | 总编制人数 |
| vacancy_count | INT | NOT NULL | 空缺编制 |
| interviewer | VARCHAR(100) | NOT NULL | 面试官 |
| position_or_team | VARCHAR(255) | NOT NULL | 岗位/用人班组 |
| team_manager | VARCHAR(100) | NOT NULL | 所属团队经理 |
| category | VARCHAR(50) | NOT NULL | 所属分类 |
| technical_platform | VARCHAR(50) | NOT NULL | 技术平台 |
| type | VARCHAR(50) | NOT NULL | 所属类型 |
| supplement_count | INT | NOT NULL | 补充人数 |
| urgent_requirement | VARCHAR(10) | NOT NULL | 是否近期紧急要求 |
| proposed_level | VARCHAR(50) | NOT NULL | 建议级别 |
| experience_years | VARCHAR(50) | NOT NULL | 相关经验年限要求 |
| position_responsibility | TEXT | NOT NULL | 岗位职责 |
| approval_status | VARCHAR(20) | DEFAULT 'PENDING' | 审批状态（PENDING：待审批，1STAPPROVED：一级审批通过，2NDAPPROVED：二级审批通过，3RDAPPROVED：三级审批通过，REJECTED：已拒绝） |
| approval_user_id | VARCHAR(20) | | 审批人ID |
| approval_user_name | VARCHAR(50) | | 审批人姓名 |
| approval_time | DATETIME | | 审批时间 |
| approval_comment | TEXT | | 审批意见 |
| position_publish_status | VARCHAR(20) | DEFAULT 'UNPUBLISHED' | 岗位发布状态（UNPUBLISHED：未发布，PUBLISHED：已发布） |
| current_approval_level | INT | | 当前审批级别 |
| approval_level1_status | VARCHAR(20) | | 一级审批状态 |
| approval_level1_user_id | VARCHAR(20) | | 一级审批人ID |
| approval_level1_user_name | VARCHAR(50) | | 一级审批人姓名 |
| approval_level1_time | DATETIME | | 一级审批时间 |
| approval_level1_comment | TEXT | | 一级审批意见 |
| approval_level2_status | VARCHAR(20) | | 二级审批状态 |
| approval_level2_user_id | VARCHAR(20) | | 二级审批人ID |
| approval_level2_user_name | VARCHAR(50) | | 二级审批人姓名 |
| approval_level2_time | DATETIME | | 二级审批时间 |
| approval_level2_comment | TEXT | | 二级审批意见 |
| approval_level3_status | VARCHAR(20) | | 三级审批状态 |
| approval_level3_user_id | VARCHAR(20) | | 三级审批人ID |
| approval_level3_user_name | VARCHAR(50) | | 三级审批人姓名 |
| approval_level3_time | DATETIME | | 三级审批时间 |
| approval_level3_comment | TEXT | | 三级审批意见 |
| create_time | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| create_user_id | VARCHAR(20) | NOT NULL | 创建用户ID |
| create_user_name | VARCHAR(50) | NOT NULL | 创建用户姓名 |
| update_time | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| update_user_id | VARCHAR(20) | NOT NULL | 更新用户ID |
| update_user_name | VARCHAR(50) | NOT NULL | 更新用户姓名 |

**索引设计**：
- PRIMARY KEY: recruitment_request_id
- INDEX: create_time
- INDEX: approval_status

#### 2.2.9 审批历史表 (approval_history)

| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| approval_history_id | BIGINT | PRIMARY KEY AUTO_INCREMENT | 审批历史ID |
| recruitment_request_id | BIGINT | NOT NULL, FOREIGN KEY REFERENCES recruitment_request(recruitment_request_id) ON DELETE CASCADE | 用人申请ID |
| approval_level | INT | NOT NULL | 审批级别 |
| approver_id | VARCHAR(20) | NOT NULL | 审批人ID |
| approver_name | VARCHAR(50) | NOT NULL | 审批人姓名 |
| approval_status | VARCHAR(20) | NOT NULL | 审批状态（APPROVED：通过，REJECTED：拒绝） |
| approval_comment | TEXT | | 审批意见 |
| approval_time | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | 审批时间 |

**索引设计**：
- PRIMARY KEY: approval_history_id
- INDEX: recruitment_request_id

#### 2.2.10 简历表 (resume)

| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| resume_id | BIGINT | PRIMARY KEY AUTO_INCREMENT | 简历ID |
| recruitment_request_id | BIGINT | FOREIGN KEY REFERENCES recruitment_request(recruitment_request_id) | 招聘申请ID |
| job_title | VARCHAR(255) | | 岗位标题 |
| applicant_name | VARCHAR(50) | NOT NULL | 申请人姓名 |
| contact_phone | VARCHAR(20) | NOT NULL | 联系电话 |
| email | VARCHAR(100) | NOT NULL | 邮箱 |
| education | VARCHAR(50) | NOT NULL | 学历 |
| work_experience | VARCHAR(255) | NOT NULL | 工作经验 |
| resume_file_name | VARCHAR(255) | NOT NULL | 简历文件名 |
| resume_file_url | VARCHAR(255) | NOT NULL | 简历文件URL |
| status | VARCHAR(20) | NOT NULL DEFAULT 'PENDING_SCREENING' | 简历状态（PENDING_SCREENING：待筛选，SCREENED：已筛选，INTERVIEW：面试中，HIRED：已录用，REJECTED：已拒绝） |
| interview_status | VARCHAR(20) | NOT NULL DEFAULT 'NOT_SCHEDULED' | 面试状态（NOT_SCHEDULED：未安排，FIRST_ROUND：一面中，SECOND_ROUND：二面中，THIRD_ROUND：三面中，PASSED：全部通过，FAILED：未通过） |
| create_time | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| create_user_id | VARCHAR(20) | NOT NULL | 创建用户ID |
| create_user_name | VARCHAR(50) | NOT NULL | 创建用户姓名 |
| update_time | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| update_user_id | VARCHAR(20) | NOT NULL | 更新用户ID |
| update_user_name | VARCHAR(50) | NOT NULL | 更新用户姓名 |

**索引设计**：
- PRIMARY KEY: resume_id
- INDEX: recruitment_request_id
- INDEX: status
- INDEX: interview_status
- INDEX: create_time

#### 2.2.11 面试记录表 (interview_record)

| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| interview_record_id | BIGINT | PRIMARY KEY AUTO_INCREMENT | 面试记录ID |
| resume_id | BIGINT | FOREIGN KEY REFERENCES resume(resume_id) | 简历ID |
| recruitment_request_id | BIGINT | FOREIGN KEY REFERENCES recruitment_request(recruitment_request_id) | 招聘申请ID |
| interview_round | VARCHAR(20) | NOT NULL | 面试环节（FIRST_ROUND：一面，SECOND_ROUND：二面，THIRD_ROUND：三面） |
| interviewer_id | VARCHAR(20) | | 面试官ID |
| interviewer_name | VARCHAR(50) | | 面试官姓名 |
| interviewer_role | VARCHAR(20) | | 面试官角色（ROOM_MANAGER：室经理，TEAM_MANAGER：团队经理，DEPARTMENT_HEAD：分管总，ADMIN：管理员） |
| interview_time | DATETIME | | 面试时间 |
| interview_result | VARCHAR(20) | | 面试结果（PASSED：通过，FAILED：不通过） |
| interview_comment | TEXT | | 面试评语 |
| create_time | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| create_user_id | VARCHAR(20) | NOT NULL | 创建用户ID |
| create_user_name | VARCHAR(50) | NOT NULL | 创建用户姓名 |
| update_time | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |
| update_user_id | VARCHAR(20) | NOT NULL | 更新用户ID |
| update_user_name | VARCHAR(50) | NOT NULL | 更新用户姓名 |

**索引设计**：
- PRIMARY KEY: interview_record_id
- INDEX: resume_id
- INDEX: recruitment_request_id
- INDEX: interview_round
- INDEX: interview_result
- INDEX: create_time

#### 2.2.12 邮件模板表 (email_template)

| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| template_id | BIGINT | NO | PRI | 模板ID |
| template_name | VARCHAR(100) | NO | UNI | 模板名称 |
| template_code | VARCHAR(100) | NO | UNI | 模板代码 |
| subject | VARCHAR(255) | NO | | 邮件主题 |
| content | TEXT | NO | | 邮件内容 |
| status | VARCHAR(20) | NO | | 状态（ACTIVE：激活，INACTIVE：未激活） |
| create_time | DATETIME | NO | | 创建时间 |
| create_user_id | VARCHAR(20) | YES | | 创建用户ID |
| create_user_name | VARCHAR(50) | YES | | 创建用户姓名 |
| update_time | DATETIME | NO | | 更新时间 |
| update_user_id | VARCHAR(20) | YES | | 更新用户ID |
| update_user_name | VARCHAR(50) | YES | | 更新用户姓名 |

**索引设计**：
- PRIMARY KEY: template_id
- UNIQUE INDEX: template_name
- UNIQUE INDEX: template_code
- INDEX: status

#### 2.2.13 录用记录表 (offer_record)

| 字段名 | 数据类型 | 约束 | 描述 |
| :--- | :--- | :--- | :--- |
| offer_id | BIGINT | NO | PRI | 录用记录ID |
| resume_id | BIGINT | | 简历ID |
| recruitment_request_id | BIGINT | | 招聘申请ID |
| applicant_name | VARCHAR(50) | NO | | 申请人姓名 |
| position | VARCHAR(100) | NO | | 录用岗位 |
| salary | DECIMAL(10,2) | NO | | 薪资 |
| entry_time | DATETIME | NO | | 入职时间 |
| status | VARCHAR(20) | NO | | 状态（PENDING：待确认，ACCEPTED：已接受，REJECTED：已拒绝） |
| create_time | DATETIME | NO | | 创建时间 |
| create_user_id | VARCHAR(20) | YES | | 创建用户ID |
| create_user_name | VARCHAR(50) | YES | | 创建用户姓名 |
| update_time | DATETIME | NO | | 更新时间 |
| update_user_id | VARCHAR(20) | YES | | 更新用户ID |
| update_user_name | VARCHAR(50) | YES | | 更新用户姓名 |

**索引设计**：
- PRIMARY KEY: offer_id
- INDEX: resume_id
- INDEX: recruitment_request_id
- INDEX: status
- INDEX: create_time

## 3. 表关系图

```
+------------------+       +-----------------------+
|    sys_user      |       |  recruitment_request  |
+------------------+       +-----------------------+
| user_id (PK)     |<----+ | recruitment_request_id (PK) |
| username         |     | +-----------------------+
| password         |     |           |
| real_name        |     |           |
+------------------+     |           |
                         |           |
+------------------+     |           |
|    sys_role      |     |           |
+------------------+     |           |
| role_id (PK)     |     |           |
| role_name        |     |           |
+------------------+     |           |
                         |           |
+------------------+     |           |
| sys_permission   |     |           |
+------------------+     |           |
| permission_id (PK)|     |           |
| permission_name  |     |           |
+------------------+     |           |
                         |           |
+------------------+     |           |
|  sys_user_role   |     |           |
+------------------+     |           |
| user_role_id (PK)|     |           |
| user_id (FK)     |-----+           |
| role_id (FK)     |----------------+-----------+ |
+------------------+     |           |
                         |           |
+------------------+     |           |
| sys_role_permission |  |           |
+------------------+     |           |
| role_permission_id (PK)|           |
| role_id (FK)     |-----+           |
| permission_id (FK)|                |
+------------------+                |
                                     |
+------------------+       +-----------------------+
|      resume      |       |                        |
+------------------+       |                        |
| resume_id (PK)   |<----+ |                        |
| recruitment_request_id (FK) |-----+               |
| applicant_name   |     | |                        |
| contact_phone    |     | |                        |
+------------------+     | |                        |
                         | |                        |
+------------------+     | |                        |
| interview_record |     | |                        |
+------------------+     | |                        |
| interview_record_id (PK)| |                        |
| resume_id (FK)   |-----+ |                        |
| recruitment_request_id (FK) |----------------+ |
+------------------+                              |
                                                  |
+------------------+                              |
|   offer_record   |                              |
+------------------+                              |
| offer_id (PK)    |                              |
| resume_id (FK)   |-----------------------------+ |
| recruitment_request_id (FK) |----------------+ |
+------------------+                              |
                                                  |
+------------------+                              |
| approval_history |                              |
+------------------+                              |
| approval_history_id (PK) |                      |
| recruitment_request_id (FK) |----------------+ |
| approver_id      |                              |
+------------------+                              |
                                                  |
+------------------+                              |
|    sys_param     |                              |
+------------------+                              |
| param_id (PK)    |                              |
| param_code       |                              |
+------------------+                              |
                                                  |
+------------------+                              |
|    sys_config    |                              |
+------------------+                              |
| config_id (PK)   |                              |
| config_key       |                              |
+------------------+                              |
                                                  |
+------------------+                              |
| email_template   |                              |
+------------------+                              |
| template_id (PK) |                              |
| template_name    |                              |
+------------------+
```

## 4. 数据字典

### 4.1 角色数据

| 角色ID | 角色名称 | 角色代码 | 描述 |
| :--- | :--- | :--- | :--- |
| 1 | 系统管理员 | SYSTEM_ADMIN | 拥有系统所有权限 |
| 2 | 人事管理员 | HR_ADMIN | 负责人事相关操作 |
| 3 | 部门经理 | DEPARTMENT_MANAGER | 负责部门相关操作和审批 |
| 4 | 普通员工 | EMPLOYEE | 只能查看和提交申请 |
| 5 | 外包招聘岗 | OUTSOURCING_RECRUITER | 负责外包人员招聘和面试安排 |

### 4.2 权限数据

| 权限ID | 权限名称 | 权限代码 | 路径 | 描述 |
| :--- | :--- | :--- | :--- | :--- |
| 1 | 系统管理 | SYSTEM_MANAGE | /system | 系统管理权限 |
| 2 | 用户管理 | USER_MANAGE | /user-management | 用户管理权限 |
| 3 | 角色管理 | ROLE_MANAGE | /role-management | 角色管理权限 |
| 4 | 权限管理 | PERMISSION_MANAGE | /permission-management | 权限管理权限 |
| 5 | 系统参数管理 | SYS_PARAM_MANAGE | /sys-param-management | 系统参数管理权限 |
| 6 | 系统配置管理 | SYS_CONFIG_MANAGE | /sys-config-management | 系统配置管理权限 |
| 7 | 邮件模板管理 | EMAIL_TEMPLATE_MANAGE | /email-template-management | 邮件模板管理权限 |
| 8 | 用人申请 | RECRUITMENT_REQUEST | /recruitment-request | 用人申请权限 |
| 9 | 审批管理 | APPROVAL_MANAGE | /approval-management | 审批管理权限 |
| 10 | 岗位发布 | POSITION_PUBLISH | /position-publishing | 岗位发布权限 |
| 11 | 简历提交 | RESUME_SUBMISSION | /resume-submission | 简历提交权限 |
| 12 | 简历筛选 | RESUME_SCREENING | /resume-screening | 简历筛选权限 |
| 13 | 面试安排 | INTERVIEW_SCHEDULING | /interview-scheduling | 面试安排权限 |
| 14 | 录用管理 | OFFER_MANAGE | /offer-management | 录用管理权限 |

### 4.3 状态数据

#### 4.3.1 通用状态

| 状态值 | 描述 |
| :--- | :--- |
| ACTIVE | 激活 |
| INACTIVE | 未激活 |

#### 4.3.2 用人申请状态

| 状态值 | 描述 |
| :--- | :--- |
| DRAFT | 草稿 |
| SUBMITTED | 已提交 |

#### 4.3.3 审批状态

| 状态值 | 描述 |
| :--- | :--- |
| PENDING | 待审批 |
| 1STAPPROVED | 一级审批通过 |
| 2NDAPPROVED | 二级审批通过 |
| 3RDAPPROVED | 三级审批通过 |
| APPROVED | 已通过 |
| REJECTED | 已拒绝 |

#### 4.3.4 岗位发布状态

| 状态值 | 描述 |
| :--- | :--- |
| UNPUBLISHED | 未发布 |
| PUBLISHED | 已发布 |

#### 4.3.5 简历状态

| 状态值 | 描述 |
| :--- | :--- |
| PENDING_SCREENING | 待筛选 |
| SCREENED | 已筛选 |
| INTERVIEW | 面试中 |
| HIRED | 已录用 |
| REJECTED | 已拒绝 |

#### 4.3.6 面试状态

| 状态值 | 描述 |
| :--- | :--- |
| NOT_SCHEDULED | 未安排 |
| FIRST_ROUND | 一面中 |
| SECOND_ROUND | 二面中 |
| THIRD_ROUND | 三面中 |
| PASSED | 全部通过 |
| FAILED | 未通过 |

#### 4.3.7 面试结果

| 状态值 | 描述 |
| :--- | :--- |
| PASSED | 通过 |
| FAILED | 不通过 |

#### 4.3.8 录用状态

| 状态值 | 描述 |
| :--- | :--- |
| PENDING | 待确认 |
| ACCEPTED | 已接受 |
| REJECTED | 已拒绝 |

### 4.4 系统参数数据

#### 4.4.1 岗位参数

| 参数ID | 参数代码 | 参数名称 | 参数值 | 参数类型 | 排序 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| PARAM_POSITION_001 | POSITION | 系统管理员 | 系统管理员 | POSITION | 1 |
| PARAM_POSITION_002 | POSITION | 人事管理员 | 人事管理员 | POSITION | 2 |
| PARAM_POSITION_003 | POSITION | 部门经理 | 部门经理 | POSITION | 3 |
| PARAM_POSITION_004 | POSITION | 普通员工 | 普通员工 | POSITION | 4 |
| PARAM_POSITION_005 | POSITION | 室经理 | 室经理 | POSITION | 5 |
| PARAM_POSITION_006 | POSITION | 团队经理 | 团队经理 | POSITION | 6 |
| PARAM_POSITION_007 | POSITION | 分管总 | 分管总 | POSITION | 7 |
| PARAM_POSITION_008 | POSITION | 管理员 | 管理员 | POSITION | 8 |
| PARAM_POSITION_009 | POSITION | 外包招聘岗 | 外包招聘岗 | POSITION | 9 |

#### 4.4.2 面试环节参数

| 参数ID | 参数代码 | 参数名称 | 参数值 | 参数类型 | 排序 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| PARAM_INTERVIEW_ROUND_001 | INTERVIEW_ROUND | 一面 | FIRST_ROUND | INTERVIEW_ROUND | 1 |
| PARAM_INTERVIEW_ROUND_002 | INTERVIEW_ROUND | 二面 | SECOND_ROUND | INTERVIEW_ROUND | 2 |
| PARAM_INTERVIEW_ROUND_003 | INTERVIEW_ROUND | 三面 | THIRD_ROUND | INTERVIEW_ROUND | 3 |

#### 4.4.3 面试官角色参数

| 参数ID | 参数代码 | 参数名称 | 参数值 | 参数类型 | 排序 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| PARAM_INTERVIEWER_ROLE_001 | INTERVIEWER_ROLE | 室经理 | ROOM_MANAGER | INTERVIEWER_ROLE | 1 |
| PARAM_INTERVIEWER_ROLE_002 | INTERVIEWER_ROLE | 团队经理 | TEAM_MANAGER | INTERVIEWER_ROLE | 2 |
| PARAM_INTERVIEWER_ROLE_003 | INTERVIEWER_ROLE | 分管总 | DEPARTMENT_HEAD | INTERVIEWER_ROLE | 3 |
| PARAM_INTERVIEWER_ROLE_004 | INTERVIEWER_ROLE | 管理员 | ADMIN | INTERVIEWER_ROLE | 4 |

## 5. 数据库初始化

### 5.1 初始用户数据

```sql
-- 插入系统管理员用户
INSERT INTO sys_user (user_id, username, password, real_name, email, phone, department, position, status, create_user_id, create_user_name, update_user_id, update_user_name)
VALUES ('1001', 'admin', '123321', '系统管理员', 'admin@example.com', '13800138000', '人力资源部', '系统管理员', 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员');

-- 插入人事管理员用户
INSERT INTO sys_user (user_id, username, password, real_name, email, phone, department, position, status, create_user_id, create_user_name, update_user_id, update_user_name)
VALUES ('1002', 'hradmin', '123321', '人事管理员', 'hradmin@example.com', '13800138001', '人力资源部', '人事管理员', 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员');

-- 插入部门经理用户
INSERT INTO sys_user (user_id, username, password, real_name, email, phone, department, position, status, create_user_id, create_user_name, update_user_id, update_user_name)
VALUES ('1003', 'deptmanager', '123321', '部门经理', 'deptmanager@example.com', '13800138002', '技术部', '部门经理', 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员');

-- 插入室经理用户
INSERT INTO sys_user (user_id, username, password, real_name, email, phone, department, position, status, create_user_id, create_user_name, update_user_id, update_user_name)
VALUES ('1004', 'roommanager', '123321', '室经理', 'roommanager@example.com', '13800138003', '技术部', '室经理', 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员');

-- 插入团队经理用户
INSERT INTO sys_user (user_id, username, password, real_name, email, phone, department, position, status, create_user_id, create_user_name, update_user_id, update_user_name)
VALUES ('1005', 'teammanager', '123321', '团队经理', 'teammanager@example.com', '13800138004', '技术部', '团队经理', 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员');

-- 插入分管总用户
INSERT INTO sys_user (user_id, username, password, real_name, email, phone, department, position, status, create_user_id, create_user_name, update_user_id, update_user_name)
VALUES ('1006', 'depthead', '123321', '分管总', 'depthead@example.com', '13800138005', '技术部', '分管总', 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员');

-- 插入外包招聘岗用户
INSERT INTO sys_user (user_id, username, password, real_name, email, phone, department, position, status, create_user_id, create_user_name, update_user_id, update_user_name)
VALUES ('1007', 'outsourcing', '123321', '外包招聘岗', 'outsourcing@example.com', '13800138006', '人力资源部', '外包招聘岗', 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员');
```

### 5.2 初始角色数据

```sql
-- 插入角色数据
INSERT INTO sys_role (role_id, role_name, role_code, description, status, create_user_id, create_user_name, update_user_id, update_user_name)
VALUES (1, '系统管理员', 'SYSTEM_ADMIN', '拥有系统所有权限', 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员'),
       (2, '人事管理员', 'HR_ADMIN', '负责人事相关操作', 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员'),
       (3, '部门经理', 'DEPARTMENT_MANAGER', '负责部门相关操作和审批', 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员'),
       (4, '普通员工', 'EMPLOYEE', '只能查看和提交申请', 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员'),
       (5, '外包招聘岗', 'OUTSOURCING_RECRUITER', '负责外包人员招聘和面试安排', 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员');
```

### 5.3 初始权限数据

```sql
-- 插入权限数据
INSERT INTO sys_permission (permission_id, permission_name, permission_code, permission_type, parent_id, path, icon, sort_order, status, create_user_id, create_user_name, update_user_id, update_user_name)
VALUES (1, '系统管理', 'SYSTEM_MANAGE', 'menu', 0, '/system', 'SettingOutlined', 1, 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员'),
       (2, '用户管理', 'USER_MANAGE', 'menu', 1, '/user-management', 'UserOutlined', 2, 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员'),
       (3, '角色管理', 'ROLE_MANAGE', 'menu', 1, '/role-management', 'TeamOutlined', 3, 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员'),
       (4, '权限管理', 'PERMISSION_MANAGE', 'menu', 1, '/permission-management', 'LockOutlined', 4, 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员'),
       (5, '系统参数管理', 'SYS_PARAM_MANAGE', 'menu', 1, '/sys-param-management', 'ControlOutlined', 5, 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员'),
       (6, '系统配置管理', 'SYS_CONFIG_MANAGE', 'menu', 1, '/sys-config-management', 'ToolOutlined', 6, 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员'),
       (7, '邮件模板管理', 'EMAIL_TEMPLATE_MANAGE', 'menu', 1, '/email-template-management', 'MailOutlined', 7, 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员'),
       (8, '用人申请', 'RECRUITMENT_REQUEST', 'menu', 0, '/recruitment-request', 'FileTextOutlined', 8, 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员'),
       (9, '审批管理', 'APPROVAL_MANAGE', 'menu', 0, '/approval-management', 'CheckCircleOutlined', 9, 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员'),
       (10, '岗位发布', 'POSITION_PUBLISH', 'menu', 0, '/position-publishing', 'EditOutlined', 10, 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员'),
       (11, '简历提交', 'RESUME_SUBMISSION', 'menu', 0, '/resume-submission', 'UploadOutlined', 11, 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员'),
       (12, '简历筛选', 'RESUME_SCREENING', 'menu', 0, '/resume-screening', 'FilterOutlined', 12, 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员'),
       (13, '面试安排', 'INTERVIEW_SCHEDULING', 'menu', 0, '/interview-scheduling', 'CalendarOutlined', 13, 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员'),
       (14, '录用管理', 'OFFER_MANAGE', 'menu', 0, '/offer-management', 'CheckSquareOutlined', 14, 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员');
```

### 5.4 初始用户角色关联数据

```sql
-- 关联用户和角色
INSERT INTO sys_user_role (user_role_id, user_id, role_id, create_user_id, create_user_name)
VALUES (1, '1001', 1, '1001', '系统管理员'),
       (2, '1002', 2, '1001', '系统管理员'),
       (3, '1003', 3, '1001', '系统管理员'),
       (4, '1004', 4, '1001', '系统管理员'),
       (5, '1005', 4, '1001', '系统管理员'),
       (6, '1006', 4, '1001', '系统管理员'),
       (7, '1007', 5, '1001', '系统管理员');
```

### 5.5 初始角色权限关联数据

```sql
-- 关联角色和权限
INSERT INTO sys_role_permission (role_permission_id, role_id, permission_id, create_user_id, create_user_name)
VALUES (1, 1, 1, '1001', '系统管理员'),
       (2, 1, 2, '1001', '系统管理员'),
       (3, 1, 3, '1001', '系统管理员'),
       (4, 1, 4, '1001', '系统管理员'),
       (5, 1, 5, '1001', '系统管理员'),
       (6, 1, 6, '1001', '系统管理员'),
       (7, 1, 7, '1001', '系统管理员'),
       (8, 1, 8, '1001', '系统管理员'),
       (9, 1, 9, '1001', '系统管理员'),
       (10, 1, 10, '1001', '系统管理员'),
       (11, 1, 11, '1001', '系统管理员'),
       (12, 1, 12, '1001', '系统管理员'),
       (13, 1, 13, '1001', '系统管理员'),
       (14, 1, 14, '1001', '系统管理员'),
       (15, 2, 8, '1001', '系统管理员'),
       (16, 2, 9, '1001', '系统管理员'),
       (17, 2, 10, '1001', '系统管理员'),
       (18, 2, 11, '1001', '系统管理员'),
       (19, 2, 12, '1001', '系统管理员'),
       (20, 2, 13, '1001', '系统管理员'),
       (21, 2, 14, '1001', '系统管理员'),
       (22, 3, 8, '1001', '系统管理员'),
       (23, 3, 9, '1001', '系统管理员'),
       (24, 4, 8, '1001', '系统管理员'),
       (25, 5, 8, '1001', '系统管理员'),
       (26, 5, 10, '1001', '系统管理员'),
       (27, 5, 11, '1001', '系统管理员'),
       (28, 5, 12, '1001', '系统管理员'),
       (29, 5, 13, '1001', '系统管理员');
```

### 5.6 初始系统参数数据

```sql
-- 插入岗位参数数据
INSERT INTO sys_param (
    param_id, param_code, param_name, param_value, param_type, 
    status, sort_order, create_user_id, create_user_name, 
    update_user_id, update_user_name
)
VALUES
('PARAM_POSITION_001', 'POSITION', '系统管理员', '系统管理员', 'POSITION', 'ACTIVE', 1, '1001', '系统管理员', '1001', '系统管理员'),
('PARAM_POSITION_002', 'POSITION', '人事管理员', '人事管理员', 'POSITION', 'ACTIVE', 2, '1001', '系统管理员', '1001', '系统管理员'),
('PARAM_POSITION_003', 'POSITION', '部门经理', '部门经理', 'POSITION', 'ACTIVE', 3, '1001', '系统管理员', '1001', '系统管理员'),
('PARAM_POSITION_004', 'POSITION', '普通员工', '普通员工', 'POSITION', 'ACTIVE', 4, '1001', '系统管理员', '1001', '系统管理员'),
('PARAM_POSITION_005', 'POSITION', '室经理', '室经理', 'POSITION', 'ACTIVE', 5, '1001', '系统管理员', '1001', '系统管理员'),
('PARAM_POSITION_006', 'POSITION', '团队经理', '团队经理', 'POSITION', 'ACTIVE', 6, '1001', '系统管理员', '1001', '系统管理员'),
('PARAM_POSITION_007', 'POSITION', '分管总', '分管总', 'POSITION', 'ACTIVE', 7, '1001', '系统管理员', '1001', '系统管理员'),
('PARAM_POSITION_008', 'POSITION', '管理员', '管理员', 'POSITION', 'ACTIVE', 8, '1001', '系统管理员', '1001', '系统管理员'),
('PARAM_POSITION_009', 'POSITION', '外包招聘岗', '外包招聘岗', 'POSITION', 'ACTIVE', 9, '1001', '系统管理员', '1001', '系统管理员');

-- 插入面试环节参数数据
INSERT INTO sys_param (
    param_id, param_code, param_name, param_value, param_type, 
    status, sort_order, create_user_id, create_user_name, 
    update_user_id, update_user_name
)
VALUES
('PARAM_INTERVIEW_ROUND_001', 'INTERVIEW_ROUND', '一面', 'FIRST_ROUND', 'INTERVIEW_ROUND', 'ACTIVE', 1, '1001', '系统管理员', '1001', '系统管理员'),
('PARAM_INTERVIEW_ROUND_002', 'INTERVIEW_ROUND', '二面', 'SECOND_ROUND', 'INTERVIEW_ROUND', 'ACTIVE', 2, '1001', '系统管理员', '1001', '系统管理员'),
('PARAM_INTERVIEW_ROUND_003', 'INTERVIEW_ROUND', '三面', 'THIRD_ROUND', 'INTERVIEW_ROUND', 'ACTIVE', 3, '1001', '系统管理员', '1001', '系统管理员');

-- 插入面试官角色参数数据
INSERT INTO sys_param (
    param_id, param_code, param_name, param_value, param_type, 
    status, sort_order, create_user_id, create_user_name, 
    update_user_id, update_user_name
)
VALUES
('PARAM_INTERVIEWER_ROLE_001', 'INTERVIEWER_ROLE', '室经理', 'ROOM_MANAGER', 'INTERVIEWER_ROLE', 'ACTIVE', 1, '1001', '系统管理员', '1001', '系统管理员'),
('PARAM_INTERVIEWER_ROLE_002', 'INTERVIEWER_ROLE', '团队经理', 'TEAM_MANAGER', 'INTERVIEWER_ROLE', 'ACTIVE', 2, '1001', '系统管理员', '1001', '系统管理员'),
('PARAM_INTERVIEWER_ROLE_003', 'INTERVIEWER_ROLE', '分管总', 'DEPARTMENT_HEAD', 'INTERVIEWER_ROLE', 'ACTIVE', 3, '1001', '系统管理员', '1001', '系统管理员'),
('PARAM_INTERVIEWER_ROLE_004', 'INTERVIEWER_ROLE', '管理员', 'ADMIN', 'INTERVIEWER_ROLE', 'ACTIVE', 4, '1001', '系统管理员', '1001', '系统管理员');
```

### 5.7 初始邮件模板数据

```sql
-- 插入邮件模板数据
INSERT INTO email_template (
    template_id, template_name, template_code, subject, content, 
    status, create_user_id, create_user_name, 
    update_user_id, update_user_name
)
VALUES
(1, '录用邀约模板', 'OFFER_TEMPLATE', '录用邀约', '亲爱的{name}：\n\n您好！\n\n非常高兴地通知您，经过我司的面试评估，您已通过所有面试环节，我们诚挚地邀请您加入我们的团队。\n\n【录用详情】\n岗位：{position}\n入职时间：{entryTime}\n\n我们相信您的加入将为公司带来新的活力和价值。如果您对录用条件有任何疑问，或需要进一步的信息，请随时与我们联系。\n\n期待您的回复！\n\n此致\n敬礼\n\n{companyName}\n{date}', 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员'),
(2, '面试邀请模板', 'INTERVIEW_TEMPLATE', '面试邀请', '亲爱的{name}：\n\n您好！\n\n感谢您对我司职位的关注。我们已经收到您的简历，并经过初步筛选，认为您符合我们的招聘要求。\n\n我们诚挚地邀请您参加面试，具体安排如下：\n\n【面试详情】\n面试岗位：{position}\n面试时间：{interviewTime}\n面试地点：{interviewLocation}\n面试官：{interviewerName}\n\n请提前15分钟到达面试地点，并携带身份证原件。如果您无法按时参加面试，请提前24小时通知我们。\n\n期待您的到来！\n\n此致\n敬礼\n\n{companyName}\n{date}', 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员'),
(3, '面试结果通知模板', 'INTERVIEW_RESULT_TEMPLATE', '面试结果通知', '亲爱的{name}：\n\n您好！\n\n感谢您参加我司的面试。经过我们的综合评估，您的面试结果如下：\n\n【面试结果】\n面试岗位：{position}\n面试结果：{result}\n\n{comment}\n\n如果您对面试结果有任何疑问，或需要进一步的信息，请随时与我们联系。\n\n此致\n敬礼\n\n{companyName}\n{date}', 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员');
```

### 5.8 初始系统配置数据

```sql
-- 插入系统配置数据
INSERT INTO sys_config (
    config_id, config_key, config_value, description, 
    status, create_user_id, create_user_name, 
    update_user_id, update_user_name
)
VALUES
(1, 'COMPANY_NAME', '示例公司', '公司名称', 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员'),
(2, 'COMPANY_ADDRESS', '北京市朝阳区示例大厦', '公司地址', 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员'),
(3, 'COMPANY_PHONE', '010-12345678', '公司电话', 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员'),
(4, 'COMPANY_EMAIL', 'hr@example.com', '公司邮箱', 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员'),
(5, 'SYSTEM_NAME', '人力资源管理系统', '系统名称', 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员'),
(6, 'SYSTEM_VERSION', '1.0.0', '系统版本', 'ACTIVE', '1001', '系统管理员', '1001', '系统管理员');
```

### 5.9 初始用人申请数据

```sql
-- 插入初始化数据
INSERT INTO recruitment_request (
    recruitment_request_id, request_title, 
    total_recruitment_count, vacancy_count, 
    interviewer, position_or_team, 
    team_manager, category, 
    technical_platform, type, 
    supplement_count, urgent_requirement, 
    proposed_level, experience_years, 
    position_responsibility, status, 
    approval_status, position_publish_status, 
    create_user_id, create_user_name, 
    update_user_id, update_user_name
)
VALUES
(1, '2024年技术部门招聘申请', 
 50, 8, 
 '张三', '前端开发组', 
 '李四', 'technical', 
 'frontend', 'fulltime', 
 5, 'yes', 
 'senior', '3-5', 
 '负责公司前端开发工作，包括网站、APP等产品的前端实现，确保用户体验良好。', 'SUBMITTED', 
 'APPROVED', 'PUBLISHED', 
 '1001', '系统管理员', 
 '1001', '系统管理员'),
(2, '2024年后端开发招聘申请', 
 30, 5, 
 '王五', '后端开发组', 
 '赵六', 'technical', 
 'backend', 'fulltime', 
 3, 'yes', 
 'senior', '3-5', 
 '负责公司后端开发工作，包括API接口开发、数据库设计等，确保系统稳定运行。', 'SUBMITTED', 
 'PENDING', 'UNPUBLISHED', 
 '1001', '系统管理员', 
 '1001', '系统管理员');
```

## 6. 数据库维护

### 6.1 备份策略

- **每日备份**：使用 mysqldump 工具对数据库进行全量备份
- **每周备份**：对数据库进行增量备份
- **每月备份**：对数据库进行完整备份并归档

### 6.2 性能优化

- **索引优化**：根据查询频率和数据量，定期检查和优化索引
- **查询优化**：分析慢查询日志，优化复杂查询
- **分区表**：对于大表，考虑使用分区表提高查询性能
- **缓存策略**：使用 Redis 等缓存工具，减轻数据库压力

### 6.3 安全措施

- **密码加密**：用户密码使用 MD5 或更安全的加密算法
- **权限控制**：严格控制数据库用户权限，最小化权限原则
- **SQL注入防护**：使用参数化查询，防止 SQL 注入攻击
- **定期审计**：定期审计数据库操作日志，发现异常行为

## 7. 总结

本数据库设计文档详细描述了人力资源管理系统的数据库结构，包括表结构、字段定义、索引设计、表关系和初始化数据等内容。设计遵循了数据库设计的最佳实践，确保了系统的可扩展性、可靠性和安全性。

通过合理的表结构设计和索引优化，系统能够高效处理各种人力资源管理业务，包括用户管理、权限控制、用人申请、简历管理和面试安排等功能。同时，完善的备份和安全措施确保了数据的安全性和可靠性。

本设计文档可作为系统开发和维护的参考依据，为后续的系统升级和扩展提供了基础。