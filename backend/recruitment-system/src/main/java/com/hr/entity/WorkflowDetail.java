package com.hr.entity;

import java.util.List;

public class WorkflowDetail {
    private String processCode;
    private RecruitmentRequest request;
    private InterviewEvaluation interviewEvaluation;
    private ArrivalConfirmation arrivalConfirmation;
    private List<InterviewEvaluationApprovalHistory> interviewEvaluationApprovalHistory;
    private List<ArrivalConfirmationApprovalHistory> arrivalConfirmationApprovalHistory;
    private List<ApprovalHistory> approvalHistory;
    private List<WorkflowNodeConfig> nodeConfigs;
    private List<WorkflowProcessLog> processLogs;

    public String getProcessCode() {
        return processCode;
    }

    public void setProcessCode(String processCode) {
        this.processCode = processCode;
    }

    public RecruitmentRequest getRequest() {
        return request;
    }

    public void setRequest(RecruitmentRequest request) {
        this.request = request;
    }

    public InterviewEvaluation getInterviewEvaluation() {
        return interviewEvaluation;
    }

    public void setInterviewEvaluation(InterviewEvaluation interviewEvaluation) {
        this.interviewEvaluation = interviewEvaluation;
    }

    public ArrivalConfirmation getArrivalConfirmation() {
        return arrivalConfirmation;
    }

    public void setArrivalConfirmation(ArrivalConfirmation arrivalConfirmation) {
        this.arrivalConfirmation = arrivalConfirmation;
    }

    public List<InterviewEvaluationApprovalHistory> getInterviewEvaluationApprovalHistory() {
        return interviewEvaluationApprovalHistory;
    }

    public void setInterviewEvaluationApprovalHistory(List<InterviewEvaluationApprovalHistory> interviewEvaluationApprovalHistory) {
        this.interviewEvaluationApprovalHistory = interviewEvaluationApprovalHistory;
    }

    public List<ArrivalConfirmationApprovalHistory> getArrivalConfirmationApprovalHistory() {
        return arrivalConfirmationApprovalHistory;
    }

    public void setArrivalConfirmationApprovalHistory(List<ArrivalConfirmationApprovalHistory> arrivalConfirmationApprovalHistory) {
        this.arrivalConfirmationApprovalHistory = arrivalConfirmationApprovalHistory;
    }

    public List<ApprovalHistory> getApprovalHistory() {
        return approvalHistory;
    }

    public void setApprovalHistory(List<ApprovalHistory> approvalHistory) {
        this.approvalHistory = approvalHistory;
    }

    public List<WorkflowNodeConfig> getNodeConfigs() {
        return nodeConfigs;
    }

    public void setNodeConfigs(List<WorkflowNodeConfig> nodeConfigs) {
        this.nodeConfigs = nodeConfigs;
    }

    public List<WorkflowProcessLog> getProcessLogs() {
        return processLogs;
    }

    public void setProcessLogs(List<WorkflowProcessLog> processLogs) {
        this.processLogs = processLogs;
    }
}
