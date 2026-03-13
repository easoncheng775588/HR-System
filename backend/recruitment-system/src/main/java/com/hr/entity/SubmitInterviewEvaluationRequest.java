package com.hr.entity;

public class SubmitInterviewEvaluationRequest {
    private Long resumeId;
    private String operatorUserId;
    private String operatorUserName;
    private String operatorRole;
    private String entryLevelSuggestion;
    private String score;
    private String interviewMethod;
    private String interviewDate;
    private String hireSuggestion;
    private String attachmentName;
    private String attachmentUrl;

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public String getOperatorUserId() {
        return operatorUserId;
    }

    public void setOperatorUserId(String operatorUserId) {
        this.operatorUserId = operatorUserId;
    }

    public String getOperatorUserName() {
        return operatorUserName;
    }

    public void setOperatorUserName(String operatorUserName) {
        this.operatorUserName = operatorUserName;
    }

    public String getOperatorRole() {
        return operatorRole;
    }

    public void setOperatorRole(String operatorRole) {
        this.operatorRole = operatorRole;
    }

    public String getEntryLevelSuggestion() {
        return entryLevelSuggestion;
    }

    public void setEntryLevelSuggestion(String entryLevelSuggestion) {
        this.entryLevelSuggestion = entryLevelSuggestion;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getInterviewMethod() {
        return interviewMethod;
    }

    public void setInterviewMethod(String interviewMethod) {
        this.interviewMethod = interviewMethod;
    }

    public String getInterviewDate() {
        return interviewDate;
    }

    public void setInterviewDate(String interviewDate) {
        this.interviewDate = interviewDate;
    }

    public String getHireSuggestion() {
        return hireSuggestion;
    }

    public void setHireSuggestion(String hireSuggestion) {
        this.hireSuggestion = hireSuggestion;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }
}
