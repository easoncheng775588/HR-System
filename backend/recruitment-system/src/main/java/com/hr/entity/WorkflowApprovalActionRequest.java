package com.hr.entity;

public class WorkflowApprovalActionRequest {
    private String action;
    private String approvalUserId;
    private String approvalUserName;
    private String approvalUserRole;
    private String approvalComment;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
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

    public String getApprovalUserRole() {
        return approvalUserRole;
    }

    public void setApprovalUserRole(String approvalUserRole) {
        this.approvalUserRole = approvalUserRole;
    }

    public String getApprovalComment() {
        return approvalComment;
    }

    public void setApprovalComment(String approvalComment) {
        this.approvalComment = approvalComment;
    }
}
