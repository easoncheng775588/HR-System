# 人力资源管理系统接口设计文档

## 1. 接口概述

本接口设计文档描述了人力资源管理系统的API接口设计，包括接口路径、请求方法、参数定义、响应格式和功能描述等内容。系统采用RESTful风格的API设计，使用JSON格式进行数据交换。

## 2. 基础信息

### 2.1 接口前缀

所有API接口的前缀为：`/api`

### 2.2 响应格式

所有API接口的响应格式统一为：

```json
{
  "returnCode": "SUC0000",
  "errorMsg": "",
  "body": {}
}
```

| 字段名 | 类型 | 描述 |
| :--- | :--- | :--- |
| returnCode | String | 响应码，SUC0000表示成功，其他表示失败 |
| errorMsg | String | 错误信息，成功时为空字符串 |
| body | Object | 响应数据，具体结构根据接口而定 |

### 2.3 认证方式

系统使用JWT进行认证，认证信息需要在请求头中携带：

```
Authorization: Bearer <token>
```

## 3. 认证接口

### 3.1 登录接口

**接口路径**：`/api/auth/login`

**请求方法**：POST

**请求参数**：

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

**请求示例**：

```json
{
  "username": "admin",
  "password": "123321"
}
```

**响应示例**：

```json
{
  "returnCode": "SUC0000",
  "errorMsg": "",
  "body": {
    "token": "MTgwMTE2Mzc0OToxNzg5NzA4MzQ5",
    "user": {
      "userId": "1001",
      "username": "admin",
      "realName": "系统管理员",
      "department": "人力资源部",
      "position": "系统管理员",
      "status": "ACTIVE"
    },
    "permissions": [
      {
        "permission_id": "P001",
        "permission_name": "系统管理",
        "permission_code": "SYSTEM_MANAGE",
        "permission_type": "menu",
        "parent_id": null,
        "path": "/system",
        "icon": "SettingOutlined",
        "sort_order": 1
      }
    ]
  }
}
```

### 3.2 获取用户信息接口

**接口路径**：`/api/auth/user`

**请求方法**：GET

**请求头**：

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| Authorization | String | 是 | Bearer <token> |

**响应示例**：

```json
{
  "returnCode": "SUC0000",
  "errorMsg": "",
  "body": {
    "user": {
      "userId": "1001",
      "username": "admin",
      "realName": "系统管理员",
      "department": "人力资源部",
      "position": "系统管理员",
      "status": "ACTIVE"
    },
    "permissions": [
      {
        "permission_id": "P001",
        "permission_name": "系统管理",
        "permission_code": "SYSTEM_MANAGE",
        "permission_type": "menu",
        "parent_id": null,
        "path": "/system",
        "icon": "SettingOutlined",
        "sort_order": 1
      }
    ]
  }
}
```

## 4. 用人申请接口

### 4.1 保存草稿接口

**接口路径**：`/api/recruitment-request/save-draft`

**请求方法**：POST

**请求参数**：

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| requestTitle | String | 是 | 申请标题 |
| totalRecruitmentCount | Integer | 是 | 总编制人数 |
| vacancyCount | Integer | 是 | 空缺编制 |
| interviewer | String | 是 | 面试官 |
| positionOrTeam | String | 是 | 岗位/用人班组 |
| teamManager | String | 是 | 所属团队经理 |
| category | String | 是 | 所属分类 |
| technicalPlatform | String | 是 | 技术平台 |
| type | String | 是 | 所属类型 |
| supplementCount | Integer | 是 | 补充人数 |
| urgentRequirement | String | 是 | 是否近期紧急要求 |
| proposedLevel | String | 是 | 建议级别 |
| experienceYears | String | 是 | 相关经验年限要求 |
| positionResponsibility | String | 是 | 岗位职责 |
| createUserId | String | 是 | 创建用户ID |
| createUserName | String | 是 | 创建用户姓名 |
| updateUserId | String | 是 | 更新用户ID |
| updateUserName | String | 是 | 更新用户姓名 |

**请求示例**：

```json
{
  "requestTitle": "2024年技术部门招聘申请",
  "totalRecruitmentCount": 50,
  "vacancyCount": 8,
  "interviewer": "张三",
  "positionOrTeam": "前端开发组",
  "teamManager": "李四",
  "category": "technical",
  "technicalPlatform": "frontend",
  "type": "fulltime",
  "supplementCount": 5,
  "urgentRequirement": "yes",
  "proposedLevel": "senior",
  "experienceYears": "3-5",
  "positionResponsibility": "负责公司前端开发工作，包括网站、APP等产品的前端实现，确保用户体验良好。",
  "createUserId": "1001",
  "createUserName": "系统管理员",
  "updateUserId": "1001",
  "updateUserName": "系统管理员"
}
```

**响应示例**：

```json
{
  "returnCode": "SUC0000",
  "errorMsg": "",
  "body": {
    "recruitmentRequestId": 1,
    "requestTitle": "2024年技术部门招聘申请",
    "totalRecruitmentCount": 50,
    "vacancyCount": 8,
    "interviewer": "张三",
    "positionOrTeam": "前端开发组",
    "teamManager": "李四",
    "category": "technical",
    "technicalPlatform": "frontend",
    "type": "fulltime",
    "supplementCount": 5,
    "urgentRequirement": "yes",
    "proposedLevel": "senior",
    "experienceYears": "3-5",
    "positionResponsibility": "负责公司前端开发工作，包括网站、APP等产品的前端实现，确保用户体验良好。",
    "status": "DRAFT",
    "approvalStatus": "PENDING",
    "createTime": "2024-01-01 10:00:00",
    "createUserId": "1001",
    "createUserName": "系统管理员",
    "updateTime": "2024-01-01 10:00:00",
    "updateUserId": "1001",
    "updateUserName": "系统管理员"
  }
}
```

### 4.2 提交申请接口

**接口路径**：`/api/recruitment-request/submit`

**请求方法**：POST

**请求参数**：同保存草稿接口

**响应示例**：

```json
{
  "returnCode": "SUC0000",
  "errorMsg": "",
  "body": {
    "recruitmentRequestId": 1,
    "requestTitle": "2024年技术部门招聘申请",
    "totalRecruitmentCount": 50,
    "vacancyCount": 8,
    "interviewer": "张三",
    "positionOrTeam": "前端开发组",
    "teamManager": "李四",
    "category": "technical",
    "technicalPlatform": "frontend",
    "type": "fulltime",
    "supplementCount": 5,
    "urgentRequirement": "yes",
    "proposedLevel": "senior",
    "experienceYears": "3-5",
    "positionResponsibility": "负责公司前端开发工作，包括网站、APP等产品的前端实现，确保用户体验良好。",
    "status": "SUBMITTED",
    "approvalStatus": "PENDING",
    "createTime": "2024-01-01 10:00:00",
    "createUserId": "1001",
    "createUserName": "系统管理员",
    "updateTime": "2024-01-01 10:05:00",
    "updateUserId": "1001",
    "updateUserName": "系统管理员"
  }
}
```

### 4.3 查询所有申请接口

**接口路径**：`/api/recruitment-request/list`

**请求方法**：GET

**响应示例**：

```json
{
  "returnCode": "SUC0000",
  "errorMsg": "",
  "body": [
    {
      "recruitmentRequestId": 1,
      "requestTitle": "2024年技术部门招聘申请",
      "totalRecruitmentCount": 50,
      "vacancyCount": 8,
      "interviewer": "张三",
      "positionOrTeam": "前端开发组",
      "teamManager": "李四",
      "category": "technical",
      "technicalPlatform": "frontend",
      "type": "fulltime",
      "supplementCount": 5,
      "urgentRequirement": "yes",
      "proposedLevel": "senior",
      "experienceYears": "3-5",
      "positionResponsibility": "负责公司前端开发工作，包括网站、APP等产品的前端实现，确保用户体验良好。",
      "status": "SUBMITTED",
      "approvalStatus": "PENDING",
      "createTime": "2024-01-01 10:00:00",
      "createUserId": "1001",
      "createUserName": "系统管理员",
      "updateTime": "2024-01-01 10:05:00",
      "updateUserId": "1001",
      "updateUserName": "系统管理员"
    }
  ]
}
```

### 4.4 根据ID查询申请接口

**接口路径**：`/api/recruitment-request/{id}`

**请求方法**：GET

**路径参数**：

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 申请ID |

**响应示例**：

```json
{
  "returnCode": "SUC0000",
  "errorMsg": "",
  "body": {
    "recruitmentRequestId": 1,
    "requestTitle": "2024年技术部门招聘申请",
    "totalRecruitmentCount": 50,
    "vacancyCount": 8,
    "interviewer": "张三",
    "positionOrTeam": "前端开发组",
    "teamManager": "李四",
    "category": "technical",
    "technicalPlatform": "frontend",
    "type": "fulltime",
    "supplementCount": 5,
    "urgentRequirement": "yes",
    "proposedLevel": "senior",
    "experienceYears": "3-5",
    "positionResponsibility": "负责公司前端开发工作，包括网站、APP等产品的前端实现，确保用户体验良好。",
    "status": "SUBMITTED",
    "approvalStatus": "PENDING",
    "createTime": "2024-01-01 10:00:00",
    "createUserId": "1001",
    "createUserName": "系统管理员",
    "updateTime": "2024-01-01 10:05:00",
    "updateUserId": "1001",
    "updateUserName": "系统管理员"
  }
}
```

### 4.5 审批通过接口

**接口路径**：`/api/recruitment-request/{id}/approve`

**请求方法**：POST

**路径参数**：

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 申请ID |

**请求参数**：

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| approvalUserId | String | 是 | 审批人ID |
| approvalUserName | String | 是 | 审批人姓名 |
| approvalComment | String | 否 | 审批意见 |

**请求示例**：

```json
{
  "approvalUserId": "1002",
  "approvalUserName": "部门经理",
  "approvalComment": "同意该申请"
}
```

**响应示例**：

```json
{
  "returnCode": "SUC0000",
  "errorMsg": "",
  "body": null
}
```

### 4.6 审批拒绝接口

**接口路径**：`/api/recruitment-request/{id}/reject`

**请求方法**：POST

**路径参数**：

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 申请ID |

**请求参数**：同审批通过接口

**响应示例**：

```json
{
  "returnCode": "SUC0000",
  "errorMsg": "",
  "body": null
}
```

### 4.7 查询待审批列表接口

**接口路径**：`/api/recruitment-request/approval/pending`

**请求方法**：GET

**响应示例**：

```json
{
  "returnCode": "SUC0000",
  "errorMsg": "",
  "body": [
    {
      "recruitmentRequestId": 1,
      "requestTitle": "2024年技术部门招聘申请",
      "totalRecruitmentCount": 50,
      "vacancyCount": 8,
      "interviewer": "张三",
      "positionOrTeam": "前端开发组",
      "teamManager": "李四",
      "category": "technical",
      "technicalPlatform": "frontend",
      "type": "fulltime",
      "supplementCount": 5,
      "urgentRequirement": "yes",
      "proposedLevel": "senior",
      "experienceYears": "3-5",
      "positionResponsibility": "负责公司前端开发工作，包括网站、APP等产品的前端实现，确保用户体验良好。",
      "status": "SUBMITTED",
      "approvalStatus": "PENDING",
      "createTime": "2024-01-01 10:00:00",
      "createUserId": "1001",
      "createUserName": "系统管理员",
      "updateTime": "2024-01-01 10:05:00",
      "updateUserId": "1001",
      "updateUserName": "系统管理员"
    }
  ]
}
```

### 4.8 根据审批状态查询接口

**接口路径**：`/api/recruitment-request/approval/status/{approvalStatus}`

**请求方法**：GET

**路径参数**：

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| approvalStatus | String | 是 | 审批状态（PENDING：待审批，APPROVED：已通过，REJECTED：已拒绝） |

**响应示例**：

```json
{
  "returnCode": "SUC0000",
  "errorMsg": "",
  "body": [
    {
      "recruitmentRequestId": 1,
      "requestTitle": "2024年技术部门招聘申请",
      "totalRecruitmentCount": 50,
      "vacancyCount": 8,
      "interviewer": "张三",
      "positionOrTeam": "前端开发组",
      "teamManager": "李四",
      "category": "technical",
      "technicalPlatform": "frontend",
      "type": "fulltime",
      "supplementCount": 5,
      "urgentRequirement": "yes",
      "proposedLevel": "senior",
      "experienceYears": "3-5",
      "positionResponsibility": "负责公司前端开发工作，包括网站、APP等产品的前端实现，确保用户体验良好。",
      "status": "SUBMITTED",
      "approvalStatus": "APPROVED",
      "createTime": "2024-01-01 10:00:00",
      "createUserId": "1001",
      "createUserName": "系统管理员",
      "updateTime": "2024-01-01 11:00:00",
      "updateUserId": "1002",
      "updateUserName": "部门经理"
    }
  ]
}
```

### 4.9 更新申请接口

**接口路径**：`/api/recruitment-request/{id}`

**请求方法**：PUT

**路径参数**：

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 申请ID |

**请求参数**：同保存草稿接口

**响应示例**：

```json
{
  "returnCode": "SUC0000",
  "errorMsg": "",
  "body": {
    "recruitmentRequestId": 1,
    "requestTitle": "2024年技术部门招聘申请（更新）",
    "totalRecruitmentCount": 50,
    "vacancyCount": 10,
    "interviewer": "张三",
    "positionOrTeam": "前端开发组",
    "teamManager": "李四",
    "category": "technical",
    "technicalPlatform": "frontend",
    "type": "fulltime",
    "supplementCount": 5,
    "urgentRequirement": "yes",
    "proposedLevel": "senior",
    "experienceYears": "3-5",
    "positionResponsibility": "负责公司前端开发工作，包括网站、APP等产品的前端实现，确保用户体验良好。",
    "status": "DRAFT",
    "approvalStatus": "PENDING",
    "createTime": "2024-01-01 10:00:00",
    "createUserId": "1001",
    "createUserName": "系统管理员",
    "updateTime": "2024-01-01 10:30:00",
    "updateUserId": "1001",
    "updateUserName": "系统管理员"
  }
}
```

### 4.10 更新岗位发布状态接口

**接口路径**：`/api/recruitment-request/{id}/publish-status`

**请求方法**：PUT

**路径参数**：

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 申请ID |

**请求参数**：

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| publishStatus | String | 是 | 发布状态（UNPUBLISHED：未发布，PUBLISHED：已发布） |

**请求示例**：

```json
{
  "publishStatus": "PUBLISHED"
}
```

**响应示例**：

```json
{
  "returnCode": "SUC0000",
  "errorMsg": "",
  "body": {
    "recruitmentRequestId": 1,
    "requestTitle": "2024年技术部门招聘申请",
    "totalRecruitmentCount": 50,
    "vacancyCount": 8,
    "interviewer": "张三",
    "positionOrTeam": "前端开发组",
    "teamManager": "李四",
    "category": "technical",
    "technicalPlatform": "frontend",
    "type": "fulltime",
    "supplementCount": 5,
    "urgentRequirement": "yes",
    "proposedLevel": "senior",
    "experienceYears": "3-5",
    "positionResponsibility": "负责公司前端开发工作，包括网站、APP等产品的前端实现，确保用户体验良好。",
    "status": "SUBMITTED",
    "approvalStatus": "APPROVED",
    "positionPublishStatus": "PUBLISHED",
    "createTime": "2024-01-01 10:00:00",
    "createUserId": "1001",
    "createUserName": "系统管理员",
    "updateTime": "2024-01-01 11:30:00",
    "updateUserId": "1002",
    "updateUserName": "部门经理"
  }
}
```

## 5. 简历接口

### 5.1 提交简历接口

**接口路径**：`/api/resume/submit`

**请求方法**：POST

**请求参数**：

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| recruitmentRequestId | Long | 是 | 招聘申请ID |
| jobTitle | String | 是 | 岗位标题 |
| applicantName | String | 是 | 申请人姓名 |
| contactPhone | String | 是 | 联系电话 |
| email | String | 是 | 邮箱 |
| education | String | 是 | 学历 |
| workExperience | String | 是 | 工作经验 |
| resumeFileName | String | 是 | 简历文件名 |
| resumeFileUrl | String | 是 | 简历文件URL |

**请求示例**：

```json
{
  "recruitmentRequestId": 1,
  "jobTitle": "前端开发工程师",
  "applicantName": "王五",
  "contactPhone": "13800138000",
  "email": "wangwu@example.com",
  "education": "本科",
  "workExperience": "5年",
  "resumeFileName": "王五简历.pdf",
  "resumeFileUrl": "/uploads/1234567890.pdf"
}
```

**响应示例**：

```json
{
  "returnCode": "SUC0000",
  "errorMsg": "",
  "body": {
    "resumeId": 1,
    "recruitmentRequestId": 1,
    "jobTitle": "前端开发工程师",
    "applicantName": "王五",
    "contactPhone": "13800138000",
    "email": "wangwu@example.com",
    "education": "本科",
    "workExperience": "5年",
    "resumeFileName": "王五简历.pdf",
    "resumeFileUrl": "/uploads/1234567890.pdf",
    "status": "PENDING_SCREENING",
    "interviewStatus": "NOT_SCHEDULED",
    "createTime": "2024-01-02 09:00:00",
    "createUserId": "1001",
    "createUserName": "系统用户",
    "updateTime": "2024-01-02 09:00:00",
    "updateUserId": "1001",
    "updateUserName": "系统用户"
  }
}
```

### 5.2 查询所有简历接口

**接口路径**：`/api/resume/list`

**请求方法**：GET

**响应示例**：

```json
{
  "returnCode": "SUC0000",
  "errorMsg": "",
  "body": [
    {
      "resumeId": 1,
      "recruitmentRequestId": 1,
      "jobTitle": "前端开发工程师",
      "applicantName": "王五",
      "contactPhone": "13800138000",
      "email": "wangwu@example.com",
      "education": "本科",
      "workExperience": "5年",
      "resumeFileName": "王五简历.pdf",
      "resumeFileUrl": "/uploads/1234567890.pdf",
      "status": "PENDING_SCREENING",
      "interviewStatus": "NOT_SCHEDULED",
      "createTime": "2024-01-02 09:00:00",
      "createUserId": "1001",
      "createUserName": "系统用户",
      "updateTime": "2024-01-02 09:00:00",
      "updateUserId": "1001",
      "updateUserName": "系统用户"
    }
  ]
}
```

### 5.3 根据ID查询简历接口

**接口路径**：`/api/resume/{id}`

**请求方法**：GET

**路径参数**：

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 简历ID |

**响应示例**：

```json
{
  "returnCode": "SUC0000",
  "errorMsg": "",
  "body": {
    "resumeId": 1,
    "recruitmentRequestId": 1,
    "jobTitle": "前端开发工程师",
    "applicantName": "王五",
    "contactPhone": "13800138000",
    "email": "wangwu@example.com",
    "education": "本科",
    "workExperience": "5年",
    "resumeFileName": "王五简历.pdf",
    "resumeFileUrl": "/uploads/1234567890.pdf",
    "status": "PENDING_SCREENING",
    "interviewStatus": "NOT_SCHEDULED",
    "createTime": "2024-01-02 09:00:00",
    "createUserId": "1001",
    "createUserName": "系统用户",
    "updateTime": "2024-01-02 09:00:00",
    "updateUserId": "1001",
    "updateUserName": "系统用户"
  }
}
```

### 5.4 根据招聘申请ID查询简历接口

**接口路径**：`/api/resume/recruitment-request/{recruitmentRequestId}`

**请求方法**：GET

**路径参数**：

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| recruitmentRequestId | Long | 是 | 招聘申请ID |

**响应示例**：

```json
{
  "returnCode": "SUC0000",
  "errorMsg": "",
  "body": [
    {
      "resumeId": 1,
      "recruitmentRequestId": 1,
      "jobTitle": "前端开发工程师",
      "applicantName": "王五",
      "contactPhone": "13800138000",
      "email": "wangwu@example.com",
      "education": "本科",
      "workExperience": "5年",
      "resumeFileName": "王五简历.pdf",
      "resumeFileUrl": "/uploads/1234567890.pdf",
      "status": "PENDING_SCREENING",
      "interviewStatus": "NOT_SCHEDULED",
      "createTime": "2024-01-02 09:00:00",
      "createUserId": "1001",
      "createUserName": "系统用户",
      "updateTime": "2024-01-02 09:00:00",
      "updateUserId": "1001",
      "updateUserName": "系统用户"
    }
  ]
}
```

### 5.5 根据状态查询简历接口

**接口路径**：`/api/resume/status/{status}`

**请求方法**：GET

**路径参数**：

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| status | String | 是 | 简历状态（PENDING_SCREENING：待筛选，SCREENED：已筛选，INTERVIEW：面试中，HIRED：已录用，REJECTED：已拒绝） |

**响应示例**：

```json
{
  "returnCode": "SUC0000",
  "errorMsg": "",
  "body": [
    {
      "resumeId": 1,
      "recruitmentRequestId": 1,
      "jobTitle": "前端开发工程师",
      "applicantName": "王五",
      "contactPhone": "13800138000",
      "email": "wangwu@example.com",
      "education": "本科",
      "workExperience": "5年",
      "resumeFileName": "王五简历.pdf",
      "resumeFileUrl": "/uploads/1234567890.pdf",
      "status": "PENDING_SCREENING",
      "interviewStatus": "NOT_SCHEDULED",
      "createTime": "2024-01-02 09:00:00",
      "createUserId": "1001",
      "createUserName": "系统用户",
      "updateTime": "2024-01-02 09:00:00",
      "updateUserId": "1001",
      "updateUserName": "系统用户"
    }
  ]
}
```

### 5.6 更新简历状态接口

**接口路径**：`/api/resume/{id}/status`

**请求方法**：PUT

**路径参数**：

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 简历ID |

**请求参数**：

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| status | String | 是 | 简历状态 |

**请求示例**：

```json
{
  "status": "SCREENED"
}
```

**响应示例**：

```json
{
  "returnCode": "SUC0000",
  "errorMsg": "",
  "body": {
    "resumeId": 1,
    "recruitmentRequestId": 1,
    "jobTitle": "前端开发工程师",
    "applicantName": "王五",
    "contactPhone": "13800138000",
    "email": "wangwu@example.com",
    "education": "本科",
    "workExperience": "5年",
    "resumeFileName": "王五简历.pdf",
    "resumeFileUrl": "/uploads/1234567890.pdf",
    "status": "SCREENED",
    "interviewStatus": "NOT_SCHEDULED",
    "createTime": "2024-01-02 09:00:00",
    "createUserId": "1001",
    "createUserName": "系统用户",
    "updateTime": "2024-01-02 10:00:00",
    "updateUserId": "1001",
    "updateUserName": "系统用户"
  }
}
```

## 6. 面试记录接口

### 6.1 保存面试记录接口

**接口路径**：`/api/interview/save`

**请求方法**：POST

**请求参数**：

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| resumeId | Long | 是 | 简历ID |
| recruitmentRequestId | Long | 是 | 招聘申请ID |
| interviewRound | String | 是 | 面试环节（FIRST_ROUND：一面，SECOND_ROUND：二面，THIRD_ROUND：三面） |
| interviewerId | String | 是 | 面试官ID |
| interviewerName | String | 是 | 面试官姓名 |
| interviewerRole | String | 是 | 面试官角色（ROOM_MANAGER：室经理，TEAM_MANAGER：团队经理，DEPARTMENT_HEAD：分管总） |
| interviewTime | String | 是 | 面试时间 |

**请求示例**：

```json
{
  "resumeId": 1,
  "recruitmentRequestId": 1,
  "interviewRound": "FIRST_ROUND",
  "interviewerId": "1003",
  "interviewerName": "技术总监",
  "interviewerRole": "DEPARTMENT_HEAD",
  "interviewTime": "2024-01-03 14:00:00"
}
```

**响应示例**：

```json
{
  "returnCode": "SUC0000",
  "errorMsg": "",
  "body": {
    "interviewRecordId": 1,
    "resumeId": 1,
    "recruitmentRequestId": 1,
    "interviewRound": "FIRST_ROUND",
    "interviewerId": "1003",
    "interviewerName": "技术总监",
    "interviewerRole": "DEPARTMENT_HEAD",
    "interviewTime": "2024-01-03 14:00:00",
    "createTime": "2024-01-02 11:00:00",
    "createUserId": "1001",
    "createUserName": "系统用户",
    "updateTime": "2024-01-02 11:00:00",
    "updateUserId": "1001",
    "updateUserName": "系统用户"
  }
}
```

### 6.2 查询所有面试记录接口

**接口路径**：`/api/interview/list`

**请求方法**：GET

**响应示例**：

```json
{
  "returnCode": "SUC0000",
  "errorMsg": "",
  "body": [
    {
      "interviewRecordId": 1,
      "resumeId": 1,
      "recruitmentRequestId": 1,
      "interviewRound": "FIRST_ROUND",
      "interviewerId": "1003",
      "interviewerName": "技术总监",
      "interviewerRole": "DEPARTMENT_HEAD",
      "interviewTime": "2024-01-03 14:00:00",
      "createTime": "2024-01-02 11:00:00",
      "createUserId": "1001",
      "createUserName": "系统用户",
      "updateTime": "2024-01-02 11:00:00",
      "updateUserId": "1001",
      "updateUserName": "系统用户"
    }
  ]
}
```

### 6.3 根据ID查询面试记录接口

**接口路径**：`/api/interview/{id}`

**请求方法**：GET

**路径参数**：

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 面试记录ID |

**响应示例**：

```json
{
  "returnCode": "SUC0000",
  "errorMsg": "",
  "body": {
    "interviewRecordId": 1,
    "resumeId": 1,
    "recruitmentRequestId": 1,
    "interviewRound": "FIRST_ROUND",
    "interviewerId": "1003",
    "interviewerName": "技术总监",
    "interviewerRole": "DEPARTMENT_HEAD",
    "interviewTime": "2024-01-03 14:00:00",
    "createTime": "2024-01-02 11:00:00",
    "createUserId": "1001",
    "createUserName": "系统用户",
    "updateTime": "2024-01-02 11:00:00",
    "updateUserId": "1001",
    "updateUserName": "系统用户"
  }
}
```

### 6.4 根据简历ID查询面试记录接口

**接口路径**：`/api/interview/resume/{resumeId}`

**请求方法**：GET

**路径参数**：

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| resumeId | Long | 是 | 简历ID |

**响应示例**：

```json
{
  "returnCode": "SUC0000",
  "errorMsg": "",
  "body": [
    {
      "interviewRecordId": 1,
      "resumeId": 1,
      "recruitmentRequestId": 1,
      "interviewRound": "FIRST_ROUND",
      "interviewerId": "1003",
      "interviewerName": "技术总监",
      "interviewerRole": "DEPARTMENT_HEAD",
      "interviewTime": "2024-01-03 14:00:00",
      "createTime": "2024-01-02 11:00:00",
      "createUserId": "1001",
      "createUserName": "系统用户",
      "updateTime": "2024-01-02 11:00:00",
      "updateUserId": "1001",
      "updateUserName": "系统用户"
    }
  ]
}
```

### 6.5 根据招聘申请ID查询面试记录接口

**接口路径**：`/api/interview/recruitment-request/{recruitmentRequestId}`

**请求方法**：GET

**路径参数**：

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| recruitmentRequestId | Long | 是 | 招聘申请ID |

**响应示例**：

```json
{
  "returnCode": "SUC0000",
  "errorMsg": "",
  "body": [
    {
      "interviewRecordId": 1,
      "resumeId": 1,
      "recruitmentRequestId": 1,
      "interviewRound": "FIRST_ROUND",
      "interviewerId": "1003",
      "interviewerName": "技术总监",
      "interviewerRole": "DEPARTMENT_HEAD",
      "interviewTime": "2024-01-03 14:00:00",
      "createTime": "2024-01-02 11:00:00",
      "createUserId": "1001",
      "createUserName": "系统用户",
      "updateTime": "2024-01-02 11:00:00",
      "updateUserId": "1001",
      "updateUserName": "系统用户"
    }
  ]
}
```

### 6.6 更新面试结果接口

**接口路径**：`/api/interview/{id}/result`

**请求方法**：PUT

**路径参数**：

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 面试记录ID |

**请求参数**：

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| interviewResult | String | 是 | 面试结果（PASSED：通过，FAILED：不通过） |
| interviewComment | String | 是 | 面试评语 |

**请求示例**：

```json
{
  "interviewResult": "PASSED",
  "interviewComment": "技术能力强，沟通表达良好"
}
```

**响应示例**：

```json
{
  "returnCode": "SUC0000",
  "errorMsg": "",
  "body": {
    "interviewRecordId": 1,
    "resumeId": 1,
    "recruitmentRequestId": 1,
    "interviewRound": "FIRST_ROUND",
    "interviewerId": "1003",
    "interviewerName": "技术总监",
    "interviewerRole": "DEPARTMENT_HEAD",
    "interviewTime": "2024-01-03 14:00:00",
    "interviewResult": "PASSED",
    "interviewComment": "技术能力强，沟通表达良好",
    "createTime": "2024-01-02 11:00:00",
    "createUserId": "1001",
    "createUserName": "系统用户",
    "updateTime": "2024-01-03 15:00:00",
    "updateUserId": "1003",
    "updateUserName": "技术总监"
  }
}
```

### 6.7 获取待面试数量接口

**接口路径**：`/api/interview/pending/count`

**请求方法**：GET

**响应示例**：

```json
{
  "returnCode": "SUC0000",
  "errorMsg": "",
  "body": 5
}
```

## 7. 文件上传接口

### 7.1 上传文件接口

**接口路径**：`/api/upload`

**请求方法**：POST

**请求参数**：

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| resume | File | 是 | 上传的文件 |

**响应示例**：

```json
{
  "returnCode": "SUC0000",
  "errorMsg": "",
  "body": {
    "url": "/uploads/1234567890.pdf",
    "name": "王五简历.pdf",
    "size": 102400
  }
}
```

### 7.2 访问上传的文件接口

**接口路径**：`/api/uploads/{filename:.+}`

**请求方法**：GET

**路径参数**：

| 参数名 | 类型 | 必选 | 描述 |
| :--- | :--- | :--- | :--- |
| filename | String | 是 | 文件名 |

**响应**：返回文件内容

## 8. 接口清单

| 模块 | 接口路径 | 请求方法 | 功能描述 |
| :--- | :--- | :--- | :--- |
| **认证** | `/api/auth/login` | POST | 用户登录 |
| **认证** | `/api/auth/user` | GET | 获取用户信息 |
| **用人申请** | `/api/recruitment-request/save-draft` | POST | 保存草稿 |
| **用人申请** | `/api/recruitment-request/submit` | POST | 提交申请 |
| **用人申请** | `/api/recruitment-request/list` | GET | 查询所有申请 |
| **用人申请** | `/api/recruitment-request/{id}` | GET | 根据ID查询申请 |
| **用人申请** | `/api/recruitment-request/{id}/approve` | POST | 审批通过 |
| **用人申请** | `/api/recruitment-request/{id}/reject` | POST | 审批拒绝 |
| **用人申请** | `/api/recruitment-request/approval/pending` | GET | 查询待审批列表 |
| **用人申请** | `/api/recruitment-request/approval/status/{approvalStatus}` | GET | 根据审批状态查询 |
| **用人申请** | `/api/recruitment-request/{id}` | PUT | 更新申请 |
| **用人申请** | `/api/recruitment-request/{id}/publish-status` | PUT | 更新岗位发布状态 |
| **简历** | `/api/resume/submit` | POST | 提交简历 |
| **简历** | `/api/resume/list` | GET | 查询所有简历 |
| **简历** | `/api/resume/{id}` | GET | 根据ID查询简历 |
| **简历** | `/api/resume/recruitment-request/{recruitmentRequestId}` | GET | 根据招聘申请ID查询简历 |
| **简历** | `/api/resume/status/{status}` | GET | 根据状态查询简历 |
| **简历** | `/api/resume/{id}/status` | PUT | 更新简历状态 |
| **面试记录** | `/api/interview/save` | POST | 保存面试记录 |
| **面试记录** | `/api/interview/list` | GET | 查询所有面试记录 |
| **面试记录** | `/api/interview/{id}` | GET | 根据ID查询面试记录 |
| **面试记录** | `/api/interview/resume/{resumeId}` | GET | 根据简历ID查询面试记录 |
| **面试记录** | `/api/interview/recruitment-request/{recruitmentRequestId}` | GET | 根据招聘申请ID查询面试记录 |
| **面试记录** | `/api/interview/{id}/result` | PUT | 更新面试结果 |
| **面试记录** | `/api/interview/pending/count` | GET | 获取待面试数量 |
| **文件上传** | `/api/upload` | POST | 上传文件 |
| **文件上传** | `/api/uploads/{filename:.+}` | GET | 访问上传的文件 |

## 9. 总结

本接口设计文档详细描述了人力资源管理系统的API接口设计，包括认证、用人申请、简历管理、面试安排和文件上传等模块的接口。这些接口遵循RESTful风格，使用统一的响应格式，确保了系统的一致性和可维护性。

通过这些接口，前端应用可以与后端服务进行交互，实现人力资源管理的各项功能。接口设计考虑了系统的业务需求和安全性，提供了完整的功能支持和错误处理机制。

本文档可作为系统开发和集成的参考依据，为前端开发和后端开发提供了清晰的接口规范。