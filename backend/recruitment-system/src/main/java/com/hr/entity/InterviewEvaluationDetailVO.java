package com.hr.entity;

import java.util.List;

public class InterviewEvaluationDetailVO {
    private InterviewEvaluation evaluation;
    private List<InterviewEvaluationApprovalHistory> approvalHistory;

    public InterviewEvaluation getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(InterviewEvaluation evaluation) {
        this.evaluation = evaluation;
    }

    public List<InterviewEvaluationApprovalHistory> getApprovalHistory() {
        return approvalHistory;
    }

    public void setApprovalHistory(List<InterviewEvaluationApprovalHistory> approvalHistory) {
        this.approvalHistory = approvalHistory;
    }
}
