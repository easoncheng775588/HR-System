package com.hr.entity;

import java.util.List;

public class ArrivalConfirmationDetailVO {
    private ArrivalConfirmation arrivalConfirmation;
    private List<ArrivalConfirmationApprovalHistory> approvalHistory;

    public ArrivalConfirmation getArrivalConfirmation() { return arrivalConfirmation; }
    public void setArrivalConfirmation(ArrivalConfirmation arrivalConfirmation) { this.arrivalConfirmation = arrivalConfirmation; }
    public List<ArrivalConfirmationApprovalHistory> getApprovalHistory() { return approvalHistory; }
    public void setApprovalHistory(List<ArrivalConfirmationApprovalHistory> approvalHistory) { this.approvalHistory = approvalHistory; }
}
