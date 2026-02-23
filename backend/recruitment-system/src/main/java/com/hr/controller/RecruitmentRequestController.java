package com.hr.controller;

import com.hr.entity.RecruitmentRequest;
import com.hr.service.RecruitmentRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/recruitment-request")
@CrossOrigin(origins = "*")
public class RecruitmentRequestController {

    private static final Logger logger = LoggerFactory.getLogger(RecruitmentRequestController.class);

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
            logger.info("开始保存用人申请草稿: {}", request.getRequestTitle());
            RecruitmentRequest savedRequest = recruitmentRequestService.saveDraft(request);
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", savedRequest);
            logger.info("保存用人申请草稿成功: {}", savedRequest.getRecruitmentRequestId());
        } catch (Exception e) {
            logger.error("保存用人申请草稿失败", e);
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
            logger.info("开始提交用人申请: {}", request.getRequestTitle());
            RecruitmentRequest submittedRequest = recruitmentRequestService.submitRequest(request);
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", submittedRequest);
            logger.info("提交用人申请成功: {}", submittedRequest.getRecruitmentRequestId());
        } catch (Exception e) {
            logger.error("提交用人申请失败", e);
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
            logger.info("开始查询所有用人申请");
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", recruitmentRequestService.getAll());
            logger.info("查询所有用人申请成功");
        } catch (Exception e) {
            logger.error("查询所有用人申请失败", e);
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
            logger.info("开始根据ID查询用人申请: {}", id);
            RecruitmentRequest request = recruitmentRequestService.getById(id);
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", request);
            logger.info("根据ID查询用人申请成功: {}", id);
        } catch (Exception e) {
            logger.error("根据ID查询用人申请失败: {}", id, e);
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
            logger.info("开始审批通过用人申请: {}", id);
            recruitmentRequestService.approveRequest(id, params);
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", null);
            logger.info("审批通过用人申请成功: {}", id);
        } catch (Exception e) {
            logger.error("审批通过用人申请失败: {}", id, e);
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
            logger.info("开始审批拒绝用人申请: {}", id);
            recruitmentRequestService.rejectRequest(id, params);
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", null);
            logger.info("审批拒绝用人申请成功: {}", id);
        } catch (Exception e) {
            logger.error("审批拒绝用人申请失败: {}", id, e);
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "审批失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }

    /**
     * 三级审批通过
     * @param id 申请ID
     * @param params 包含审批意见的参数
     * @return 响应结果
     */
    @PostMapping("/{id}/three-level/approve")
    public Map<String, Object> threeLevelApprove(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Map<String, Object> response = new HashMap<>();
        try {
            logger.info("开始三级审批通过用人申请: {}", id);
            recruitmentRequestService.threeLevelApproveRequest(id, params);
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", null);
            logger.info("三级审批通过用人申请成功: {}", id);
        } catch (Exception e) {
            logger.error("三级审批通过用人申请失败: {}", id, e);
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "审批失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }

    /**
     * 三级审批拒绝
     * @param id 申请ID
     * @param params 包含审批意见的参数
     * @return 响应结果
     */
    @PostMapping("/{id}/three-level/reject")
    public Map<String, Object> threeLevelReject(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Map<String, Object> response = new HashMap<>();
        try {
            logger.info("开始三级审批拒绝用人申请: {}", id);
            recruitmentRequestService.threeLevelRejectRequest(id, params);
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", null);
            logger.info("三级审批拒绝用人申请成功: {}", id);
        } catch (Exception e) {
            logger.error("三级审批拒绝用人申请失败: {}", id, e);
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "审批失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }

    /**
     * 根据用户角色获取待审批列表
     * @param userRole 用户角色
     * @return 响应结果
     */
    @GetMapping("/approval/pending/{userRole}")
    public Map<String, Object> getPendingApprovalByRole(@PathVariable String userRole) {
        Map<String, Object> response = new HashMap<>();
        try {
            logger.info("开始根据用户角色查询待审批列表: {}", userRole);
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", recruitmentRequestService.getPendingApprovalByRole(userRole));
            logger.info("根据用户角色查询待审批列表成功: {}", userRole);
        } catch (Exception e) {
            logger.error("根据用户角色查询待审批列表失败: {}", userRole, e);
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "查询失败：" + e.getMessage());
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
            logger.info("开始查询待审批列表");
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", recruitmentRequestService.getPendingApproval());
            logger.info("查询待审批列表成功");
        } catch (Exception e) {
            logger.error("查询待审批列表失败", e);
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
            logger.info("开始根据审批状态查询用人申请: {}", approvalStatus);
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", recruitmentRequestService.getByApprovalStatus(approvalStatus));
            logger.info("根据审批状态查询用人申请成功: {}", approvalStatus);
        } catch (Exception e) {
            logger.error("根据审批状态查询用人申请失败: {}", approvalStatus, e);
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
            logger.info("开始更新用人申请: {}", id);
            request.setRecruitmentRequestId(id);
            RecruitmentRequest updatedRequest = recruitmentRequestService.updateRequest(request);
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", updatedRequest);
            logger.info("更新用人申请成功: {}", id);
        } catch (Exception e) {
            logger.error("更新用人申请失败: {}", id, e);
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
            logger.info("开始更新用人申请发布状态: {}, 状态: {}", id, publishStatus);
            RecruitmentRequest updatedRequest = recruitmentRequestService.updatePublishStatus(id, publishStatus);
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", updatedRequest);
            logger.info("更新用人申请发布状态成功: {}, 状态: {}", id, publishStatus);
        } catch (Exception e) {
            logger.error("更新用人申请发布状态失败: {}", id, e);
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "更新发布状态失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }

    /**
     * 获取申请的审批历史
     * @param recruitmentRequestId 申请ID
     * @return 响应结果
     */
    @GetMapping("/approval-history/{recruitmentRequestId}")
    public Map<String, Object> getApprovalHistory(@PathVariable Long recruitmentRequestId) {
        Map<String, Object> response = new HashMap<>();
        try {
            logger.info("开始查询用人申请审批历史: {}", recruitmentRequestId);
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", recruitmentRequestService.getApprovalHistoryByRequestId(recruitmentRequestId));
            logger.info("查询用人申请审批历史成功: {}", recruitmentRequestId);
        } catch (Exception e) {
            logger.error("查询用人申请审批历史失败: {}", recruitmentRequestId, e);
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "查询失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }
}