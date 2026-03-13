package com.hr.controller;

import com.hr.entity.WorkflowApprovalActionRequest;
import com.hr.entity.WorkflowDetail;
import com.hr.entity.WorkflowInitiatedItem;
import com.hr.entity.WorkflowApproveRequest;
import com.hr.entity.WorkflowProcessedItem;
import com.hr.entity.WorkflowTodoItem;
import com.hr.service.WorkflowCenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workflow-center")
@CrossOrigin(origins = "*")
public class WorkflowCenterController {

    @Autowired
    private WorkflowCenterService workflowCenterService;

    @GetMapping("/todo")
    public Map<String, Object> getMyTodo(@RequestParam String userId, @RequestParam String userRole) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<WorkflowTodoItem> items = workflowCenterService.getMyTodo(userId, userRole);
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", items);
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "Failed to load todo list: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }

    @GetMapping("/initiated")
    public Map<String, Object> getMyInitiated(@RequestParam String userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<WorkflowInitiatedItem> items = workflowCenterService.getMyInitiated(userId);
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", items);
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "Failed to load initiated list: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }

    @GetMapping("/processed")
    public Map<String, Object> getMyProcessed(@RequestParam String userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<WorkflowProcessedItem> items = workflowCenterService.getMyProcessed(userId);
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", items);
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "Failed to load processed list: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }

    @GetMapping("/detail/{requestId}")
    public Map<String, Object> getDetail(@PathVariable Long requestId,
                                         @RequestParam(required = false) String viewerId,
                                         @RequestParam(required = false) String viewerName,
                                         @RequestParam(required = false) String viewerRole) {
        Map<String, Object> result = new HashMap<>();
        try {
            WorkflowDetail detail = workflowCenterService.getWorkflowDetail("RECRUITMENT_REQUEST", requestId, viewerId, viewerName, viewerRole);
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", detail);
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "Failed to load workflow detail: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }

    @GetMapping("/detail/{processCode}/{businessId}")
    public Map<String, Object> getDetail(@PathVariable String processCode,
                                         @PathVariable Long businessId,
                                         @RequestParam(required = false) String viewerId,
                                         @RequestParam(required = false) String viewerName,
                                         @RequestParam(required = false) String viewerRole) {
        Map<String, Object> result = new HashMap<>();
        try {
            WorkflowDetail detail = workflowCenterService.getWorkflowDetail(processCode, businessId, viewerId, viewerName, viewerRole);
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", detail);
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "Failed to load workflow detail: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }

    @PostMapping("/{requestId}/approve")
    public Map<String, Object> approve(@PathVariable Long requestId, @RequestBody WorkflowApprovalActionRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            workflowCenterService.approve(requestId, request);
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", "OK");
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "Failed to approve workflow: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }

    @PostMapping("/{processCode}/{businessId}/approve")
    public Map<String, Object> approve(@PathVariable String processCode,
                                       @PathVariable Long businessId,
                                       @RequestBody WorkflowApproveRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            workflowCenterService.approve(processCode, businessId, request);
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", "OK");
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "Failed to approve workflow: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }
}
