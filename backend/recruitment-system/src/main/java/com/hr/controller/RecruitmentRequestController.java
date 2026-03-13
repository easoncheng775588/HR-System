package com.hr.controller;

import com.hr.common.BusinessException;
import com.hr.common.Response;
import com.hr.entity.RecruitmentRequest;
import com.hr.service.RecruitmentRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/recruitment-request")
@CrossOrigin(origins = "*")
public class RecruitmentRequestController {

    private static final Logger logger = LoggerFactory.getLogger(RecruitmentRequestController.class);

    @Autowired
    private RecruitmentRequestService recruitmentRequestService;

    private void validateRecruitmentRequest(RecruitmentRequest request) {
        if (request == null) {
            throw new BusinessException("ERR0001", "申请数据不能为空");
        }
        if (request.getRequestTitle() == null || request.getRequestTitle().trim().isEmpty()) {
            throw new BusinessException("ERR0002", "申请标题不能为空");
        }
        if (request.getRequestType() == null || request.getRequestType().trim().isEmpty()) {
            throw new BusinessException("ERR0002", "所属类型不能为空");
        }
        if (request.getSupplementCount() == null || request.getSupplementCount() <= 0) {
            throw new BusinessException("ERR0005", "补充人数必须大于0");
        }
        if (request.getTechnicalPlatform() == null || request.getTechnicalPlatform().trim().isEmpty()) {
            throw new BusinessException("ERR0006", "技术平台不能为空");
        }
        if (request.getCategory() == null || request.getCategory().trim().isEmpty()) {
            throw new BusinessException("ERR0007", "所属分类不能为空");
        }
        if (request.getProposedLevel() == null || request.getProposedLevel().trim().isEmpty()) {
            throw new BusinessException("ERR0008", "建议级别不能为空");
        }
        if (request.getExperienceYears() == null || request.getExperienceYears().trim().isEmpty()) {
            throw new BusinessException("ERR0009", "相关经验年限要求不能为空");
        }
        if (request.getPositionResponsibility() == null || request.getPositionResponsibility().trim().isEmpty()) {
            throw new BusinessException("ERR0010", "岗位职责不能为空");
        }
        if (request.getSkillRequirement() == null || request.getSkillRequirement().trim().isEmpty()) {
            throw new BusinessException("ERR0011", "任职要求不能为空");
        }
        if (request.getSkillRequirement().length() > 500) {
            throw new BusinessException("ERR0011", "任职要求长度不能超过500");
        }
        if (request.getPositionResponsibility().length() > 500) {
            throw new BusinessException("ERR0010", "岗位职责长度不能超过500");
        }
        if (request.getRemark() != null && request.getRemark().length() > 500) {
            throw new BusinessException("ERR0012", "备注长度不能超过500");
        }
    }

    private void validateDraftRequest(RecruitmentRequest request) {
        if (request == null) {
            throw new BusinessException("ERR0001", "申请数据不能为空");
        }
    }

    @PostMapping("/save-draft")
    public Response<RecruitmentRequest> saveDraft(@RequestBody RecruitmentRequest request,
                                                  @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            applyOperatorFromAuthHeader(request, authHeader, false);
            validateDraftRequest(request);
            RecruitmentRequest savedRequest = recruitmentRequestService.saveDraft(request);
            return Response.success(savedRequest);
        } catch (BusinessException e) {
            logger.error("save draft failed: {}", e.getMessage());
            return Response.fail(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("save draft failed", e);
            return Response.fail("保存草稿失败: " + e.getMessage());
        }
    }

    @PostMapping("/submit")
    public Response<RecruitmentRequest> submitRequest(@RequestBody RecruitmentRequest request,
                                                      @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            applyOperatorFromAuthHeader(request, authHeader, false);
            validateRecruitmentRequest(request);
            RecruitmentRequest submittedRequest = recruitmentRequestService.submitRequest(request);
            return Response.success(submittedRequest);
        } catch (BusinessException e) {
            logger.error("submit request failed: {}", e.getMessage());
            return Response.fail(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("submit request failed", e);
            return Response.fail("提交申请失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public Response<?> getAll(@RequestParam(value = "viewerId", required = false) String viewerId,
                              @RequestParam(value = "viewerRole", required = false) String viewerRole) {
        try {
            if (viewerId == null || viewerId.trim().isEmpty()) {
                return Response.success(recruitmentRequestService.getAll());
            }
            return Response.success(recruitmentRequestService.getAll(viewerId, viewerRole));
        } catch (BusinessException e) {
            return Response.fail(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("list request failed", e);
            return Response.fail("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Response<RecruitmentRequest> getById(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                throw new BusinessException("ERR0008", "申请ID必须大于0");
            }
            RecruitmentRequest request = recruitmentRequestService.getById(id);
            return Response.success(request);
        } catch (BusinessException e) {
            return Response.fail(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("get by id failed: {}", id, e);
            return Response.fail("查询失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/approve")
    public Response<?> approve(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            if (id == null || id <= 0) {
                throw new BusinessException("ERR0008", "申请ID必须大于0");
            }
            recruitmentRequestService.approveRequest(id, params);
            return Response.success(null);
        } catch (BusinessException e) {
            return Response.fail(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("approve failed: {}", id, e);
            return Response.fail("审批失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/reject")
    public Response<?> reject(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            if (id == null || id <= 0) {
                throw new BusinessException("ERR0008", "申请ID必须大于0");
            }
            recruitmentRequestService.rejectRequest(id, params);
            return Response.success(null);
        } catch (BusinessException e) {
            return Response.fail(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("reject failed: {}", id, e);
            return Response.fail("审批失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/three-level/approve")
    public Response<?> threeLevelApprove(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            if (id == null || id <= 0) {
                throw new BusinessException("ERR0008", "申请ID必须大于0");
            }
            RecruitmentRequest updatedRequest = recruitmentRequestService.threeLevelApproveRequest(id, params);
            return Response.success(updatedRequest);
        } catch (BusinessException e) {
            return Response.fail(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("three level approve failed: {}", id, e);
            return Response.fail("审批失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/three-level/reject")
    public Response<?> threeLevelReject(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            if (id == null || id <= 0) {
                throw new BusinessException("ERR0008", "申请ID必须大于0");
            }
            recruitmentRequestService.threeLevelRejectRequest(id, params);
            return Response.success(null);
        } catch (BusinessException e) {
            return Response.fail(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("three level reject failed: {}", id, e);
            return Response.fail("审批失败: " + e.getMessage());
        }
    }

    @GetMapping("/approval/pending/{userRole}")
    public Response<?> getPendingApprovalByRole(@PathVariable String userRole) {
        try {
            if (userRole == null || userRole.trim().isEmpty()) {
                throw new BusinessException("ERR0009", "用户角色不能为空");
            }
            return Response.success(recruitmentRequestService.getPendingApprovalByRole(userRole));
        } catch (BusinessException e) {
            return Response.fail(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("get pending by role failed: {}", userRole, e);
            return Response.fail("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/approval/pending")
    public Response<?> getPendingApproval() {
        try {
            return Response.success(recruitmentRequestService.getPendingApproval());
        } catch (BusinessException e) {
            return Response.fail(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("get pending failed", e);
            return Response.fail("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/approval/status/{approvalStatus}")
    public Response<?> getByApprovalStatus(@PathVariable String approvalStatus) {
        try {
            if (approvalStatus == null || approvalStatus.trim().isEmpty()) {
                throw new BusinessException("ERR0010", "审批状态不能为空");
            }
            return Response.success(recruitmentRequestService.getByApprovalStatus(approvalStatus));
        } catch (BusinessException e) {
            return Response.fail(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("get by approval status failed: {}", approvalStatus, e);
            return Response.fail("查询失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Response<RecruitmentRequest> update(@PathVariable Long id,
                                               @RequestBody RecruitmentRequest request,
                                               @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (id == null || id <= 0) {
                throw new BusinessException("ERR0008", "申请ID必须大于0");
            }
            applyOperatorFromAuthHeader(request, authHeader, true);
            validateRecruitmentRequest(request);
            request.setRecruitmentRequestId(id);
            RecruitmentRequest updatedRequest = recruitmentRequestService.updateRequest(request);
            return Response.success(updatedRequest);
        } catch (BusinessException e) {
            return Response.fail(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("update failed: {}", id, e);
            return Response.fail("更新失败: " + e.getMessage());
        }
    }

    @GetMapping("/approval-history/{recruitmentRequestId}")
    public Response<?> getApprovalHistory(@PathVariable Long recruitmentRequestId) {
        try {
            if (recruitmentRequestId == null || recruitmentRequestId <= 0) {
                throw new BusinessException("ERR0008", "申请ID必须大于0");
            }
            return Response.success(recruitmentRequestService.getApprovalHistoryByRequestId(recruitmentRequestId));
        } catch (BusinessException e) {
            return Response.fail(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("get approval history failed: {}", recruitmentRequestId, e);
            return Response.fail("查询失败: " + e.getMessage());
        }
    }

    private void applyOperatorFromAuthHeader(RecruitmentRequest request, String authHeader, boolean updateOnly) {
        if (request == null) {
            return;
        }
        String userId = resolveUserIdFromAuthHeader(authHeader);
        if (userId == null || userId.trim().isEmpty()) {
            return;
        }
        if (!updateOnly && (request.getCreateUserId() == null || request.getCreateUserId().trim().isEmpty())) {
            request.setCreateUserId(userId);
        }
        if (request.getUpdateUserId() == null || request.getUpdateUserId().trim().isEmpty()) {
            request.setUpdateUserId(userId);
        }
    }

    private String resolveUserIdFromAuthHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        try {
            String token = authHeader.substring(7);
            String decoded = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            if (parts.length == 0 || parts[0].trim().isEmpty()) {
                return null;
            }
            return parts[0].trim();
        } catch (IllegalArgumentException e) {
            logger.warn("invalid authorization token", e);
            return null;
        }
    }
}
