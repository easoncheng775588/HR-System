package com.hr.entity;

import java.util.Date;

public class ApprovalHistory {
    private Long approvalHistoryId;
    private Long recruitmentRequestId;
    private Integer approvalLevel;
    private String approverId;
    private String approverName;
    private String approvalStatus;
    private String approvalComment;
    private Date approvalTime;

    public Long getApprovalHistoryId() {
        return approvalHistoryId;
    }

    public void setApprovalHistoryId(Long approvalHistoryId) {
        this.approvalHistoryId = approvalHistoryId;
    }

    public Long getRecruitmentRequestId() {
        return recruitmentRequestId;
    }

    public void setRecruitmentRequestId(Long recruitmentRequestId) {
        this.recruitmentRequestId = recruitmentRequestId;
    }

    public Integer getApprovalLevel() {
        return approvalLevel;
    }

    public void setApprovalLevel(Integer approvalLevel) {
        this.approvalLevel = approvalLevel;
    }

    public String getApproverId() {
        return approverId;
    }

    public void setApproverId(String approverId) {
        this.approverId = approverId;
    }

    public String getApproverName() {
        return approverName;
    }

    public void setApproverName(String approverName) {
        this.approverName = approverName;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public String getApprovalComment() {
        return approvalComment;
    }

    public void setApprovalComment(String approvalComment) {
        this.approvalComment = approvalComment;
    }

    public Date getApprovalTime() {
        return approvalTime;
    }

    public void setApprovalTime(Date approvalTime) {
        this.approvalTime = approvalTime;
    }
}
