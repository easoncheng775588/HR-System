package com.hr.mapper;

import com.hr.entity.RecruitmentRequest;
import java.util.List;
import java.util.Map;

public interface RecruitmentRequestMapper {
    /**
     * 保存用人申请
     * @param request 用人申请对象
     * @return 影响的行数
     */
    int insert(RecruitmentRequest request);

    /**
     * 更新用人申请
     * @param request 用人申请对象
     * @return 影响的行数
     */
    int updateByPrimaryKey(RecruitmentRequest request);

    /**
     * 根据ID查询用人申请
     * @param id 用人申请ID
     * @return 用人申请对象
     */
    RecruitmentRequest selectByPrimaryKey(Long id);

    /**
     * 查询所有用人申请
     * @return 用人申请列表
     */
    List<RecruitmentRequest> selectAll();

    /**
     * 更新审批状态
     * @param params 包含审批信息的Map
     * @return 影响的行数
     */
    int updateApprovalStatus(Map<String, Object> params);

    /**
     * 更新三级审批状态
     * @param params 包含三级审批信息的Map
     * @return 影响的行数
     */
    int updateThreeLevelApprovalStatus(Map<String, Object> params);

    /**
     * 查询待审批的用人申请
     * @return 待审批的用人申请列表
     */
    List<RecruitmentRequest> selectPendingApproval();

    /**
     * 根据审批状态查询用人申请
     * @param approvalStatus 审批状态
     * @return 用人申请列表
     */
    List<RecruitmentRequest> selectByApprovalStatus(String approvalStatus);
}