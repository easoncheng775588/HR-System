package com.hr.controller;

import com.hr.entity.RecruitmentRequest;
import com.hr.service.RecruitmentRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/recruitment-request")
@CrossOrigin(origins = "*")
public class RecruitmentRequestController {

    @Autowired
    private RecruitmentRequestService recruitmentRequestService;

    /**
     * 保存草稿
     * @param request 用人申请对象
     * @return 响应结果
     */
    @PostMapping("/save-draft")
    public Map<String, Object> saveDraft(@RequestBody RecruitmentRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            RecruitmentRequest savedRequest = recruitmentRequestService.saveDraft(request);
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", savedRequest);
        } catch (Exception e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "保存草稿失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }

    /**
     * 提交申请
     * @param request 用人申请对象
     * @return 响应结果
     */
    @PostMapping("/submit")
    public Map<String, Object> submitRequest(@RequestBody RecruitmentRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            RecruitmentRequest submittedRequest = recruitmentRequestService.submitRequest(request);
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", submittedRequest);
        } catch (Exception e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "提交申请失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }

    /**
     * 查询所有申请
     * @return 响应结果
     */
    @GetMapping("/list")
    public Map<String, Object> getAll() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", recruitmentRequestService.getAll());
        } catch (Exception e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "查询失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }

    /**
     * 根据ID查询申请
     * @param id 申请ID
     * @return 响应结果
     */
    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            RecruitmentRequest request = recruitmentRequestService.getById(id);
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", request);
        } catch (Exception e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "查询失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }

    /**
     * 审批通过
     * @param id 申请ID
     * @param params 包含审批意见的参数
     * @return 响应结果
     */
    @PostMapping("/{id}/approve")
    public Map<String, Object> approve(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Map<String, Object> response = new HashMap<>();
        try {
            recruitmentRequestService.approveRequest(id, params);
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", null);
        } catch (Exception e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "审批失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }

    /**
     * 审批拒绝
     * @param id 申请ID
     * @param params 包含审批意见的参数
     * @return 响应结果
     */
    @PostMapping("/{id}/reject")
    public Map<String, Object> reject(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Map<String, Object> response = new HashMap<>();
        try {
            recruitmentRequestService.rejectRequest(id, params);
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", null);
        } catch (Exception e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "审批失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }

    /**
     * 查询待审批列表
     * @return 响应结果
     */
    @GetMapping("/approval/pending")
    public Map<String, Object> getPendingApproval() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", recruitmentRequestService.getPendingApproval());
        } catch (Exception e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "查询失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }

    /**
     * 根据审批状态查询
     * @param approvalStatus 审批状态
     * @return 响应结果
     */
    @GetMapping("/approval/status/{approvalStatus}")
    public Map<String, Object> getByApprovalStatus(@PathVariable String approvalStatus) {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", recruitmentRequestService.getByApprovalStatus(approvalStatus));
        } catch (Exception e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "查询失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }

    /**
     * 更新未审批的申请
     * @param id 申请ID
     * @param request 用人申请对象
     * @return 响应结果
     */
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody RecruitmentRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            request.setRecruitmentRequestId(id);
            RecruitmentRequest updatedRequest = recruitmentRequestService.updateRequest(request);
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", updatedRequest);
        } catch (Exception e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "更新失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }

    /**
     * 更新岗位发布状态
     * @param id 申请ID
     * @param params 包含发布状态的参数
     * @return 响应结果
     */
    @PutMapping("/{id}/publish-status")
    public Map<String, Object> updatePublishStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Map<String, Object> response = new HashMap<>();
        try {
            String publishStatus = (String) params.get("publishStatus");
            RecruitmentRequest updatedRequest = recruitmentRequestService.updatePublishStatus(id, publishStatus);
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", updatedRequest);
        } catch (Exception e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "更新发布状态失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }
}