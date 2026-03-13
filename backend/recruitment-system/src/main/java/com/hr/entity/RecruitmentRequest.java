package com.hr.entity;

import java.util.Date;

/**
 * 用人申请实体类
 * 映射数据库recruitment_request表，存储用人申请相关信息
 * 包含申请基本信息、审批状态、发布状态等字段
 */
public class RecruitmentRequest {
    private Long recruitmentRequestId;
    private String requestTitle;
    private Integer totalRecruitmentCount;
    private Integer vacancyCount;
    private String team;
    private String applicationDepartment;
    private String orgUnitName;
    private String requestType;
    private String remark;
    private String submitterRoleType;
    private String finalApproverUserId;
    private String finalApproverUserName;
    private String technicalPlatform;
    private String category;
    private Integer supplementCount;
    private String urgentRequirement;
    private String proposedLevel;
    private String experienceYears;
    private String skillRequirement;
    private String positionResponsibility;
    private String interviewerId;
    private String interviewerName;
    private String approvalStatus;
    private String approvalUserId;
    private String approvalUserName;
    private Date approvalTime;
    private String approvalComment;
    private Integer currentApprovalLevel;
    private String approvalLevel1Status;
    private String approvalLevel1UserId;
    private String approvalLevel1UserName;
    private Date approvalLevel1Time;
    private String approvalLevel1Comment;
    private String approvalLevel2Status;
    private String approvalLevel2UserId;
    private String approvalLevel2UserName;
    private Date approvalLevel2Time;
    private String approvalLevel2Comment;
    private String approvalLevel3Status;
    private String approvalLevel3UserId;
    private String approvalLevel3UserName;
    private Date approvalLevel3Time;
    private String approvalLevel3Comment;
    private String status;
    private Date createTime;
    private String createUserId;
    private String createUserName;
    private Date updateTime;
    private String updateUserId;
    private String updateUserName;

    // Getters and Setters
    public Long getRecruitmentRequestId() {
        return recruitmentRequestId;
    }

    public void setRecruitmentRequestId(Long recruitmentRequestId) {
        this.recruitmentRequestId = recruitmentRequestId;
    }

    public String getRequestTitle() {
        return requestTitle;
    }

    public void setRequestTitle(String requestTitle) {
        this.requestTitle = requestTitle;
    }

    public Integer getTotalRecruitmentCount() {
        return totalRecruitmentCount;
    }

    public void setTotalRecruitmentCount(Integer totalRecruitmentCount) {
        this.totalRecruitmentCount = totalRecruitmentCount;
    }

    public Integer getVacancyCount() {
        return vacancyCount;
    }

    public void setVacancyCount(Integer vacancyCount) {
        this.vacancyCount = vacancyCount;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getApplicationDepartment() {
        return applicationDepartment;
    }

    public void setApplicationDepartment(String applicationDepartment) {
        this.applicationDepartment = applicationDepartment;
    }

    public String getOrgUnitName() {
        return orgUnitName;
    }

    public void setOrgUnitName(String orgUnitName) {
        this.orgUnitName = orgUnitName;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getSubmitterRoleType() {
        return submitterRoleType;
    }

    public void setSubmitterRoleType(String submitterRoleType) {
        this.submitterRoleType = submitterRoleType;
    }

    public String getFinalApproverUserId() {
        return finalApproverUserId;
    }

    public void setFinalApproverUserId(String finalApproverUserId) {
        this.finalApproverUserId = finalApproverUserId;
    }

    public String getFinalApproverUserName() {
        return finalApproverUserName;
    }

    public void setFinalApproverUserName(String finalApproverUserName) {
        this.finalApproverUserName = finalApproverUserName;
    }

    public String getTechnicalPlatform() {
        return technicalPlatform;
    }

    public void setTechnicalPlatform(String technicalPlatform) {
        this.technicalPlatform = technicalPlatform;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getSupplementCount() {
        return supplementCount;
    }

    public void setSupplementCount(Integer supplementCount) {
        this.supplementCount = supplementCount;
    }

    public String getUrgentRequirement() {
        return urgentRequirement;
    }

    public void setUrgentRequirement(String urgentRequirement) {
        this.urgentRequirement = urgentRequirement;
    }

    public String getProposedLevel() {
        return proposedLevel;
    }

    public void setProposedLevel(String proposedLevel) {
        this.proposedLevel = proposedLevel;
    }

    public String getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(String experienceYears) {
        this.experienceYears = experienceYears;
    }

    public String getSkillRequirement() {
        return skillRequirement;
    }

    public void setSkillRequirement(String skillRequirement) {
        this.skillRequirement = skillRequirement;
    }

    public String getPositionResponsibility() {
        return positionResponsibility;
    }

    public void setPositionResponsibility(String positionResponsibility) {
        this.positionResponsibility = positionResponsibility;
    }

    public String getInterviewerId() {
        return interviewerId;
    }

    public void setInterviewerId(String interviewerId) {
        this.interviewerId = interviewerId;
    }

    public String getInterviewerName() {
        return interviewerName;
    }

    public void setInterviewerName(String interviewerName) {
        this.interviewerName = interviewerName;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public String getApprovalUserId() {
        return approvalUserId;
    }

    public void setApprovalUserId(String approvalUserId) {
        this.approvalUserId = approvalUserId;
    }

    public String getApprovalUserName() {
        return approvalUserName;
    }

    public void setApprovalUserName(String approvalUserName) {
        this.approvalUserName = approvalUserName;
    }

    public Date getApprovalTime() {
        return approvalTime;
    }

    public void setApprovalTime(Date approvalTime) {
        this.approvalTime = approvalTime;
    }

    public String getApprovalComment() {
        return approvalComment;
    }

    public void setApprovalComment(String approvalComment) {
        this.approvalComment = approvalComment;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(String createUserId) {
        this.createUserId = createUserId;
    }

    public String getCreateUserName() {
        return createUserName;
    }

    public void setCreateUserName(String createUserName) {
        this.createUserName = createUserName;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getUpdateUserId() {
        return updateUserId;
    }

    public void setUpdateUserId(String updateUserId) {
        this.updateUserId = updateUserId;
    }

    public String getUpdateUserName() {
        return updateUserName;
    }

    public void setUpdateUserName(String updateUserName) {
        this.updateUserName = updateUserName;
    }

    public Integer getCurrentApprovalLevel() {
        return currentApprovalLevel;
    }

    public void setCurrentApprovalLevel(Integer currentApprovalLevel) {
        this.currentApprovalLevel = currentApprovalLevel;
    }

    public String getApprovalLevel1Status() {
        return approvalLevel1Status;
    }

    public void setApprovalLevel1Status(String approvalLevel1Status) {
        this.approvalLevel1Status = approvalLevel1Status;
    }

    public String getApprovalLevel1UserId() {
        return approvalLevel1UserId;
    }

    public void setApprovalLevel1UserId(String approvalLevel1UserId) {
        this.approvalLevel1UserId = approvalLevel1UserId;
    }

    public String getApprovalLevel1UserName() {
        return approvalLevel1UserName;
    }

    public void setApprovalLevel1UserName(String approvalLevel1UserName) {
        this.approvalLevel1UserName = approvalLevel1UserName;
    }

    public Date getApprovalLevel1Time() {
        return approvalLevel1Time;
    }

    public void setApprovalLevel1Time(Date approvalLevel1Time) {
        this.approvalLevel1Time = approvalLevel1Time;
    }

    public String getApprovalLevel1Comment() {
        return approvalLevel1Comment;
    }

    public void setApprovalLevel1Comment(String approvalLevel1Comment) {
        this.approvalLevel1Comment = approvalLevel1Comment;
    }

    public String getApprovalLevel2Status() {
        return approvalLevel2Status;
    }

    public void setApprovalLevel2Status(String approvalLevel2Status) {
        this.approvalLevel2Status = approvalLevel2Status;
    }

    public String getApprovalLevel2UserId() {
        return approvalLevel2UserId;
    }

    public void setApprovalLevel2UserId(String approvalLevel2UserId) {
        this.approvalLevel2UserId = approvalLevel2UserId;
    }

    public String getApprovalLevel2UserName() {
        return approvalLevel2UserName;
    }

    public void setApprovalLevel2UserName(String approvalLevel2UserName) {
        this.approvalLevel2UserName = approvalLevel2UserName;
    }

    public Date getApprovalLevel2Time() {
        return approvalLevel2Time;
    }

    public void setApprovalLevel2Time(Date approvalLevel2Time) {
        this.approvalLevel2Time = approvalLevel2Time;
    }

    public String getApprovalLevel2Comment() {
        return approvalLevel2Comment;
    }

    public void setApprovalLevel2Comment(String approvalLevel2Comment) {
        this.approvalLevel2Comment = approvalLevel2Comment;
    }

    public String getApprovalLevel3Status() {
        return approvalLevel3Status;
    }

    public void setApprovalLevel3Status(String approvalLevel3Status) {
        this.approvalLevel3Status = approvalLevel3Status;
    }

    public String getApprovalLevel3UserId() {
        return approvalLevel3UserId;
    }

    public void setApprovalLevel3UserId(String approvalLevel3UserId) {
        this.approvalLevel3UserId = approvalLevel3UserId;
    }

    public String getApprovalLevel3UserName() {
        return approvalLevel3UserName;
    }

    public void setApprovalLevel3UserName(String approvalLevel3UserName) {
        this.approvalLevel3UserName = approvalLevel3UserName;
    }

    public Date getApprovalLevel3Time() {
        return approvalLevel3Time;
    }

    public void setApprovalLevel3Time(Date approvalLevel3Time) {
        this.approvalLevel3Time = approvalLevel3Time;
    }

    public String getApprovalLevel3Comment() {
        return approvalLevel3Comment;
    }

    public void setApprovalLevel3Comment(String approvalLevel3Comment) {
        this.approvalLevel3Comment = approvalLevel3Comment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
