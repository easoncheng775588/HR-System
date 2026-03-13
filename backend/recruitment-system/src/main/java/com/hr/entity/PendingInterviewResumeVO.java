package com.hr.entity;

import java.util.Date;

public class PendingInterviewResumeVO {
    private Long resumeId;
    private String candidateName;
    private String gender;
    private String supplierName;
    private String status;
    private String dispatchStatus;
    private String confirmedInterviewerId;
    private String confirmedInterviewerName;
    private String interviewMethod;
    private String meetingNo;
    private Date availableStartTime;
    private Date availableEndTime;
    private Date confirmedInterviewTime;
    private Boolean canConfirmInterviewTime;
    private Boolean canLaunchEvaluation;
    private String evaluationStatus;

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDispatchStatus() {
        return dispatchStatus;
    }

    public void setDispatchStatus(String dispatchStatus) {
        this.dispatchStatus = dispatchStatus;
    }

    public String getConfirmedInterviewerId() {
        return confirmedInterviewerId;
    }

    public void setConfirmedInterviewerId(String confirmedInterviewerId) {
        this.confirmedInterviewerId = confirmedInterviewerId;
    }

    public String getConfirmedInterviewerName() {
        return confirmedInterviewerName;
    }

    public void setConfirmedInterviewerName(String confirmedInterviewerName) {
        this.confirmedInterviewerName = confirmedInterviewerName;
    }

    public String getInterviewMethod() {
        return interviewMethod;
    }

    public void setInterviewMethod(String interviewMethod) {
        this.interviewMethod = interviewMethod;
    }

    public String getMeetingNo() {
        return meetingNo;
    }

    public void setMeetingNo(String meetingNo) {
        this.meetingNo = meetingNo;
    }

    public Date getAvailableStartTime() {
        return availableStartTime;
    }

    public void setAvailableStartTime(Date availableStartTime) {
        this.availableStartTime = availableStartTime;
    }

    public Date getAvailableEndTime() {
        return availableEndTime;
    }

    public void setAvailableEndTime(Date availableEndTime) {
        this.availableEndTime = availableEndTime;
    }

    public Date getConfirmedInterviewTime() {
        return confirmedInterviewTime;
    }

    public void setConfirmedInterviewTime(Date confirmedInterviewTime) {
        this.confirmedInterviewTime = confirmedInterviewTime;
    }

    public Boolean getCanConfirmInterviewTime() {
        return canConfirmInterviewTime;
    }

    public void setCanConfirmInterviewTime(Boolean canConfirmInterviewTime) {
        this.canConfirmInterviewTime = canConfirmInterviewTime;
    }

    public Boolean getCanLaunchEvaluation() {
        return canLaunchEvaluation;
    }

    public void setCanLaunchEvaluation(Boolean canLaunchEvaluation) {
        this.canLaunchEvaluation = canLaunchEvaluation;
    }

    public String getEvaluationStatus() {
        return evaluationStatus;
    }

    public void setEvaluationStatus(String evaluationStatus) {
        this.evaluationStatus = evaluationStatus;
    }
}
