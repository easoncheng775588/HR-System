package com.hr.entity;

import java.util.Date;

public class EntryRecord {
    private Long entryRecordId;
    private Long evaluationId;
    private Long resumeId;
    private Long sourceRecruitmentRequestId;
    private String candidateName;
    private Date interviewTime;
    private String hiredDepartment;
    private String positionLevel;
    private String technicalPlatform;
    private String entryStatus;
    private Date plannedEntryDate;
    private Date actualEntryDate;
    private String arrivalStatus;
    private Date createTime;
    private String createUserId;
    private String createUserName;
    private Date updateTime;
    private String updateUserId;
    private String updateUserName;

    public Long getEntryRecordId() {
        return entryRecordId;
    }

    public void setEntryRecordId(Long entryRecordId) {
        this.entryRecordId = entryRecordId;
    }

    public Long getEvaluationId() {
        return evaluationId;
    }

    public void setEvaluationId(Long evaluationId) {
        this.evaluationId = evaluationId;
    }

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public Long getSourceRecruitmentRequestId() {
        return sourceRecruitmentRequestId;
    }

    public void setSourceRecruitmentRequestId(Long sourceRecruitmentRequestId) {
        this.sourceRecruitmentRequestId = sourceRecruitmentRequestId;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public Date getInterviewTime() {
        return interviewTime;
    }

    public void setInterviewTime(Date interviewTime) {
        this.interviewTime = interviewTime;
    }

    public String getHiredDepartment() {
        return hiredDepartment;
    }

    public void setHiredDepartment(String hiredDepartment) {
        this.hiredDepartment = hiredDepartment;
    }

    public String getPositionLevel() {
        return positionLevel;
    }

    public void setPositionLevel(String positionLevel) {
        this.positionLevel = positionLevel;
    }

    public String getTechnicalPlatform() {
        return technicalPlatform;
    }

    public void setTechnicalPlatform(String technicalPlatform) {
        this.technicalPlatform = technicalPlatform;
    }

    public String getEntryStatus() {
        return entryStatus;
    }

    public void setEntryStatus(String entryStatus) {
        this.entryStatus = entryStatus;
    }

    public Date getPlannedEntryDate() {
        return plannedEntryDate;
    }

    public void setPlannedEntryDate(Date plannedEntryDate) {
        this.plannedEntryDate = plannedEntryDate;
    }

    public Date getActualEntryDate() {
        return actualEntryDate;
    }

    public void setActualEntryDate(Date actualEntryDate) {
        this.actualEntryDate = actualEntryDate;
    }

    public String getArrivalStatus() {
        return arrivalStatus;
    }

    public void setArrivalStatus(String arrivalStatus) {
        this.arrivalStatus = arrivalStatus;
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
