package com.hr.entity;

import java.util.Date;

/**
 * 录用记录实体类
 * 用于存储录用相关信息
 */
public class OfferRecord {
    /**
     * 录用记录ID
     */
    private Long offerRecordId;
    
    /**
     * 简历ID
     */
    private Long resumeId;
    
    /**
     * 招聘申请ID
     */
    private Long recruitmentRequestId;
    
    /**
     * 候选人姓名
     */
    private String candidateName;
    
    /**
     * 联系电话
     */
    private String contactPhone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 录用岗位
     */
    private String position;
    
    /**
     * 录用状态
     */
    private String status;
    
    /**
     * 录用时间
     */
    private Date offerTime;
    
    /**
     * 入职时间
     */
    private Date entryTime;
    
    /**
     * 邮件发送状态
     */
    private String emailStatus;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 创建用户ID
     */
    private String createUserId;
    
    /**
     * 创建用户姓名
     */
    private String createUserName;
    
    /**
     * 更新时间
     */
    private Date updateTime;
    
    /**
     * 更新用户ID
     */
    private String updateUserId;
    
    /**
     * 更新用户姓名
     */
    private String updateUserName;

    // Getters and Setters
    public Long getOfferRecordId() {
        return offerRecordId;
    }

    public void setOfferRecordId(Long offerRecordId) {
        this.offerRecordId = offerRecordId;
    }

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public Long getRecruitmentRequestId() {
        return recruitmentRequestId;
    }

    public void setRecruitmentRequestId(Long recruitmentRequestId) {
        this.recruitmentRequestId = recruitmentRequestId;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getOfferTime() {
        return offerTime;
    }

    public void setOfferTime(Date offerTime) {
        this.offerTime = offerTime;
    }

    public Date getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(Date entryTime) {
        this.entryTime = entryTime;
    }

    public String getEmailStatus() {
        return emailStatus;
    }

    public void setEmailStatus(String emailStatus) {
        this.emailStatus = emailStatus;
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
}
