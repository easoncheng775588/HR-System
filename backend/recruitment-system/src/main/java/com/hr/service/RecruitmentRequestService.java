package com.hr.service;

import com.hr.entity.RecruitmentRequest;
import com.hr.entity.ApprovalHistory;
import java.util.List;
import java.util.Map;

public interface RecruitmentRequestService {
    /**
     * 保存草稿
     * @param request 用人申请对象
     * @return 保存后的对象
     */
    RecruitmentRequest saveDraft(RecruitmentRequest request);

    /**
     * 提交申请
     * @param request 用人申请对象
     * @return 提交后的对象
     */
    RecruitmentRequest submitRequest(RecruitmentRequest request);

    /**
     * 根据ID查询申请
     * @param id 申请ID
     * @return 申请对象
     */
    RecruitmentRequest getById(Long id);

    /**
     * 查询所有申请
     * @return 申请列表
     */
    List<RecruitmentRequest> getAll();

    /**
     * 审批通过
     * @param id 申请ID
     * @param params 包含审批信息的参数
     */
    void approveRequest(Long id, Map<String, Object> params);

    /**
     * 审批拒绝
     * @param id 申请ID
     * @param params 包含审批信息的参数
     */
    void rejectRequest(Long id, Map<String, Object> params);

    /**
     * 三级审批通过
     * @param id 申请ID
     * @param params 包含审批信息的参数
     */
    void threeLevelApproveRequest(Long id, Map<String, Object> params);

    /**
     * 三级审批拒绝
     * @param id 申请ID
     * @param params 包含审批信息的参数
     */
    void threeLevelRejectRequest(Long id, Map<String, Object> params);

    /**
     * 获取待当前用户审批的申请
     * @param userRole 用户角色
     * @return 待审批的申请列表
     */
    List<RecruitmentRequest> getPendingApprovalByRole(String userRole);

    /**
     * 查询待审批的申请
     * @return 待审批的申请列表
     */
    List<RecruitmentRequest> getPendingApproval();

    /**
     * 根据审批状态查询申请
     * @param approvalStatus 审批状态
     * @return 申请列表
     */
    List<RecruitmentRequest> getByApprovalStatus(String approvalStatus);

    /**
     * 更新待审批的申请
     * @param request 用人申请对象
     * @return 更新后的用人申请
     */
    RecruitmentRequest updateRequest(RecruitmentRequest request);

    /**
     * 更新岗位发布状态
     * @param id 申请ID
     * @param publishStatus 发布状态
     * @return 更新后的用人申请
     */
    RecruitmentRequest updatePublishStatus(Long id, String publishStatus);

    /**
     * 获取申请的审批历史
     * @param recruitmentRequestId 申请ID
     * @return 审批历史列表
     */
    List<ApprovalHistory> getApprovalHistoryByRequestId(Long recruitmentRequestId);
}