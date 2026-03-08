package com.hr.entity;

import java.util.List;

public class WorkflowDetail {
    private RecruitmentRequest request;
    private List<ApprovalHistory> approvalHistory;
    private List<WorkflowNodeConfig> nodeConfigs;
    private List<WorkflowProcessLog> processLogs;

    public RecruitmentRequest getRequest() {
        return request;
    }

    public void setRequest(RecruitmentRequest request) {
        this.request = request;
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
