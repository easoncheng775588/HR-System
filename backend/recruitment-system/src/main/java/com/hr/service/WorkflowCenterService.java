package com.hr.service;

import com.hr.entity.WorkflowApprovalActionRequest;
import com.hr.entity.WorkflowDetail;
import com.hr.entity.WorkflowInitiatedItem;
import com.hr.entity.WorkflowProcessedItem;
import com.hr.entity.WorkflowTodoItem;

import java.util.List;

public interface WorkflowCenterService {
    List<WorkflowTodoItem> getMyTodo(String userId, String userRole);

    List<WorkflowInitiatedItem> getMyInitiated(String userId);

    List<WorkflowProcessedItem> getMyProcessed(String userId);

    WorkflowDetail getWorkflowDetail(Long requestId, String viewerId, String viewerName, String viewerRole);

    void approve(Long requestId, WorkflowApprovalActionRequest actionRequest);
}
