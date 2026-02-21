package com.hr.service.impl;

import com.hr.entity.RecruitmentRequest;
import com.hr.mapper.RecruitmentRequestMapper;
import com.hr.service.RecruitmentRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecruitmentRequestServiceImpl implements RecruitmentRequestService {

    @Autowired
    private RecruitmentRequestMapper recruitmentRequestMapper;

    @Override
    public RecruitmentRequest saveDraft(RecruitmentRequest request) {
        // 设置状态为草稿
        request.setStatus("DRAFT");
        // 设置默认的用户信息（实际项目中应该从登录信息中获取）
        if (request.getCreateUserId() == null) {
            request.setCreateUserId("1001");
            request.setCreateUserName("系统用户");
        }
        request.setUpdateUserId("1001");
        request.setUpdateUserName("系统用户");
        
        if (request.getRecruitmentRequestId() == null) {
            // 新增
            recruitmentRequestMapper.insert(request);
        } else {
            // 更新
            recruitmentRequestMapper.updateByPrimaryKey(request);
        }
        return request;
    }

    @Override
    public RecruitmentRequest submitRequest(RecruitmentRequest request) {
        // 设置状态为已提交
        request.setStatus("SUBMITTED");
        // 设置默认的用户信息（实际项目中应该从登录信息中获取）
        if (request.getCreateUserId() == null) {
            request.setCreateUserId("1001");
            request.setCreateUserName("系统用户");
        }
        request.setUpdateUserId("1001");
        request.setUpdateUserName("系统用户");
        
        if (request.getRecruitmentRequestId() == null) {
            // 新增
            recruitmentRequestMapper.insert(request);
        } else {
            // 更新
            recruitmentRequestMapper.updateByPrimaryKey(request);
        }
        return request;
    }

    @Override
    public RecruitmentRequest getById(Long id) {
        return recruitmentRequestMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<RecruitmentRequest> getAll() {
        return recruitmentRequestMapper.selectAll();
    }

    @Override
    public void approveRequest(Long id, Map<String, Object> params) {
        RecruitmentRequest request = recruitmentRequestMapper.selectByPrimaryKey(id);
        if (request == null) {
            throw new RuntimeException("申请不存在");
        }
        if (!"SUBMITTED".equals(request.getStatus())) {
            throw new RuntimeException("只能审批已提交的申请");
        }
        if (!"PENDING".equals(request.getApprovalStatus())) {
            throw new RuntimeException("该申请已经审批过");
        }

        Map<String, Object> approvalParams = new HashMap<>();
        approvalParams.put("recruitmentRequestId", id);
        approvalParams.put("approvalStatus", "APPROVED");
        approvalParams.put("approvalUserId", params.get("approvalUserId"));
        approvalParams.put("approvalUserName", params.get("approvalUserName"));
        approvalParams.put("approvalComment", params.get("approvalComment"));

        recruitmentRequestMapper.updateApprovalStatus(approvalParams);
    }

    @Override
    public void rejectRequest(Long id, Map<String, Object> params) {
        RecruitmentRequest request = recruitmentRequestMapper.selectByPrimaryKey(id);
        if (request == null) {
            throw new RuntimeException("申请不存在");
        }
        if (!"SUBMITTED".equals(request.getStatus())) {
            throw new RuntimeException("只能审批已提交的申请");
        }
        if (!"PENDING".equals(request.getApprovalStatus())) {
            throw new RuntimeException("该申请已经审批过");
        }

        Map<String, Object> approvalParams = new HashMap<>();
        approvalParams.put("recruitmentRequestId", id);
        approvalParams.put("approvalStatus", "REJECTED");
        approvalParams.put("approvalUserId", params.get("approvalUserId"));
        approvalParams.put("approvalUserName", params.get("approvalUserName"));
        approvalParams.put("approvalComment", params.get("approvalComment"));

        recruitmentRequestMapper.updateApprovalStatus(approvalParams);
    }

    @Override
    public List<RecruitmentRequest> getPendingApproval() {
        return recruitmentRequestMapper.selectPendingApproval();
    }

    @Override
    public List<RecruitmentRequest> getByApprovalStatus(String approvalStatus) {
        return recruitmentRequestMapper.selectByApprovalStatus(approvalStatus);
    }

    @Override
    public RecruitmentRequest updateRequest(RecruitmentRequest request) {
        RecruitmentRequest existingRequest = recruitmentRequestMapper.selectByPrimaryKey(request.getRecruitmentRequestId());
        if (existingRequest == null) {
            throw new RuntimeException("申请不存在");
        }
        if (!"PENDING".equals(existingRequest.getApprovalStatus())) {
            throw new RuntimeException("只能修改待审批的申请");
        }

        request.setUpdateUserId("1001");
        request.setUpdateUserName("系统用户");
        recruitmentRequestMapper.updateByPrimaryKey(request);
        return recruitmentRequestMapper.selectByPrimaryKey(request.getRecruitmentRequestId());
    }

    @Override
    public RecruitmentRequest updatePublishStatus(Long id, String publishStatus) {
        RecruitmentRequest existingRequest = recruitmentRequestMapper.selectByPrimaryKey(id);
        if (existingRequest == null) {
            throw new RuntimeException("申请不存在");
        }

        existingRequest.setPositionPublishStatus(publishStatus);
        existingRequest.setUpdateUserId("1001");
        existingRequest.setUpdateUserName("系统用户");
        recruitmentRequestMapper.updateByPrimaryKey(existingRequest);
        return recruitmentRequestMapper.selectByPrimaryKey(id);
    }
}