package com.hr.entity;

import java.util.Date;

public class InterviewRecord {
    private Long interviewRecordId;
    private Long resumeId;
    private Long recruitmentRequestId;
    private String interviewRound;
    private String interviewerId;
    private String interviewerName;
    private String interviewerRole;
    private Date interviewTime;
    private String interviewResult;
    private String interviewComment;
    private Date createTime;
    private String createUserId;
    private String createUserName;
    private Date updateTime;
    private String updateUserId;
    private String updateUserName;

    // Getters and Setters
    public Long getInterviewRecordId() {
        return interviewRecordId;
    }

    public void setInterviewRecordId(Long interviewRecordId) {
        this.interviewRecordId = interviewRecordId;
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

    public String getInterviewRound() {
        return interviewRound;
    }

    public void setInterviewRound(String interviewRound) {
        this.interviewRound = interviewRound;
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

    public String getInterviewerRole() {
        return interviewerRole;
    }

    public void setInterviewerRole(String interviewerRole) {
        this.interviewerRole = interviewerRole;
    }

    public Date getInterviewTime() {
        return interviewTime;
    }

    public void setInterviewTime(Date interviewTime) {
        this.interviewTime = interviewTime;
    }

    public String getInterviewResult() {
        return interviewResult;
    }

    public void setInterviewResult(String interviewResult) {
        this.interviewResult = interviewResult;
    }

    public String getInterviewComment() {
        return interviewComment;
    }

    public void setInterviewComment(String interviewComment) {
        this.interviewComment = interviewComment;
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
