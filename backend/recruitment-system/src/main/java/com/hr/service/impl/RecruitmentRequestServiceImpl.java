package com.hr.service.impl;

import com.hr.entity.RecruitmentRequest;
import com.hr.entity.ApprovalHistory;
import com.hr.mapper.RecruitmentRequestMapper;
import com.hr.mapper.ApprovalHistoryMapper;
import com.hr.service.RecruitmentRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecruitmentRequestServiceImpl implements RecruitmentRequestService {

    @Autowired
    private RecruitmentRequestMapper recruitmentRequestMapper;
    
    @Autowired
    private ApprovalHistoryMapper approvalHistoryMapper;

    @Override
    public RecruitmentRequest saveDraft(RecruitmentRequest request) {
        // 设置默认的用户信息（实际项目中应该从登录信息中获取）
        if (request.getCreateUserId() == null) {
            request.setCreateUserId("1001");
            request.setCreateUserName("系统用户");
        }
        request.setUpdateUserId("1001");
        request.setUpdateUserName("系统用户");
        
        // 设置默认的审批状态
        if (request.getApprovalStatus() == null) {
            request.setApprovalStatus("DRAFT");
        }
        // 设置默认的岗位发布状态
        if (request.getPositionPublishStatus() == null) {
            request.setPositionPublishStatus("NOT_PUBLISHED");
        }
        // 设置默认的当前审批级别
        if (request.getCurrentApprovalLevel() == null) {
            request.setCurrentApprovalLevel(0);
        }
        // 设置默认的各级审批状态
        if (request.getApprovalLevel1Status() == null) {
            request.setApprovalLevel1Status("PENDING");
        }
        if (request.getApprovalLevel2Status() == null) {
            request.setApprovalLevel2Status("PENDING");
        }
        if (request.getApprovalLevel3Status() == null) {
            request.setApprovalLevel3Status("PENDING");
        }
        
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
        // 设置审批状态为待审批
        request.setApprovalStatus("PENDING");
        // 设置初始审批级别为1（第一级：编制管理岗）
        request.setCurrentApprovalLevel(1);
        // 设置各审批级别的初始状态为待审批
        request.setApprovalLevel1Status("PENDING");
        request.setApprovalLevel2Status("PENDING");
        request.setApprovalLevel3Status("PENDING");
        // 设置默认的用户信息（实际项目中应该从登录信息中获取）
        Date now = new Date();
        if (request.getCreateUserId() == null) {
            request.setCreateUserId("1001");
            request.setCreateUserName("系统用户");
            request.setCreateTime(now);
        }
        request.setUpdateUserId("1001");
        request.setUpdateUserName("系统用户");
        request.setUpdateTime(now);
        
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

        // 将前端传递的更新字段合并到现有记录中
        existingRequest.setRequestTitle(request.getRequestTitle());
        existingRequest.setTeam(request.getTeam());
        existingRequest.setSupplementCount(request.getSupplementCount());
        existingRequest.setProposedLevel(request.getProposedLevel());
        existingRequest.setPositionResponsibility(request.getPositionResponsibility());
        
        // 设置更新信息
        existingRequest.setUpdateUserId("1001");
        existingRequest.setUpdateUserName("系统用户");
        existingRequest.setUpdateTime(new Date());
        
        recruitmentRequestMapper.updateByPrimaryKey(existingRequest);
        return recruitmentRequestMapper.selectByPrimaryKey(existingRequest.getRecruitmentRequestId());
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

    @Override
    public void threeLevelApproveRequest(Long id, Map<String, Object> params) {
        RecruitmentRequest request = recruitmentRequestMapper.selectByPrimaryKey(id);
        if (request == null) {
            throw new RuntimeException("申请不存在");
        }

        Integer currentLevel = request.getCurrentApprovalLevel();
        if (currentLevel == null) {
            throw new RuntimeException("审批流程未初始化");
        }

        Map<String, Object> approvalParams = new HashMap<>();
        approvalParams.put("recruitmentRequestId", id);
        approvalParams.put("approvalLevel1Status", request.getApprovalLevel1Status());
        approvalParams.put("approvalLevel1UserId", request.getApprovalLevel1UserId());
        approvalParams.put("approvalLevel1UserName", request.getApprovalLevel1UserName());
        approvalParams.put("approvalLevel1Time", request.getApprovalLevel1Time());
        approvalParams.put("approvalLevel1Comment", request.getApprovalLevel1Comment());
        approvalParams.put("approvalLevel2Status", request.getApprovalLevel2Status());
        approvalParams.put("approvalLevel2UserId", request.getApprovalLevel2UserId());
        approvalParams.put("approvalLevel2UserName", request.getApprovalLevel2UserName());
        approvalParams.put("approvalLevel2Time", request.getApprovalLevel2Time());
        approvalParams.put("approvalLevel2Comment", request.getApprovalLevel2Comment());
        approvalParams.put("approvalLevel3Status", request.getApprovalLevel3Status());
        approvalParams.put("approvalLevel3UserId", request.getApprovalLevel3UserId());
        approvalParams.put("approvalLevel3UserName", request.getApprovalLevel3UserName());
        approvalParams.put("approvalLevel3Time", request.getApprovalLevel3Time());
        approvalParams.put("approvalLevel3Comment", request.getApprovalLevel3Comment());

        Date now = new Date();

        if (currentLevel == 1) {
            // 第一级审批：编制管理岗
            approvalParams.put("approvalLevel1Status", "APPROVED");
            approvalParams.put("approvalLevel1UserId", params.get("approvalUserId"));
            approvalParams.put("approvalLevel1UserName", params.get("approvalUserName"));
            approvalParams.put("approvalLevel1Time", now);
            approvalParams.put("approvalLevel1Comment", params.get("approvalComment"));
            approvalParams.put("currentApprovalLevel", 2);
            approvalParams.put("approvalStatus", "1STAPPROVED");
        } else if (currentLevel == 2) {
            // 第二级审批：外包招聘岗
            approvalParams.put("approvalLevel2Status", "APPROVED");
            approvalParams.put("approvalLevel2UserId", params.get("approvalUserId"));
            approvalParams.put("approvalLevel2UserName", params.get("approvalUserName"));
            approvalParams.put("approvalLevel2Time", now);
            approvalParams.put("approvalLevel2Comment", params.get("approvalComment"));
            approvalParams.put("currentApprovalLevel", 3);
            approvalParams.put("approvalStatus", "2NDAPPROVED");
        } else if (currentLevel == 3) {
            // 第三级审批：团队经理
            approvalParams.put("approvalLevel3Status", "APPROVED");
            approvalParams.put("approvalLevel3UserId", params.get("approvalUserId"));
            approvalParams.put("approvalLevel3UserName", params.get("approvalUserName"));
            approvalParams.put("approvalLevel3Time", now);
            approvalParams.put("approvalLevel3Comment", params.get("approvalComment"));
            approvalParams.put("currentApprovalLevel", 4); // 审批完成
            approvalParams.put("approvalStatus", "3RDAPPROVED");
        } else {
            throw new RuntimeException("审批流程已完成");
        }

        recruitmentRequestMapper.updateThreeLevelApprovalStatus(approvalParams);
        
        // 记录审批历史
        recordApprovalHistory(id, currentLevel, params, "APPROVED");
    }

    @Override
    public void threeLevelRejectRequest(Long id, Map<String, Object> params) {
        RecruitmentRequest request = recruitmentRequestMapper.selectByPrimaryKey(id);
        if (request == null) {
            throw new RuntimeException("申请不存在");
        }

        Integer currentLevel = request.getCurrentApprovalLevel();
        if (currentLevel == null) {
            throw new RuntimeException("审批流程未初始化");
        }

        Map<String, Object> approvalParams = new HashMap<>();
        approvalParams.put("recruitmentRequestId", id);
        approvalParams.put("approvalLevel1Status", request.getApprovalLevel1Status());
        approvalParams.put("approvalLevel1UserId", request.getApprovalLevel1UserId());
        approvalParams.put("approvalLevel1UserName", request.getApprovalLevel1UserName());
        approvalParams.put("approvalLevel1Time", request.getApprovalLevel1Time());
        approvalParams.put("approvalLevel1Comment", request.getApprovalLevel1Comment());
        approvalParams.put("approvalLevel2Status", request.getApprovalLevel2Status());
        approvalParams.put("approvalLevel2UserId", request.getApprovalLevel2UserId());
        approvalParams.put("approvalLevel2UserName", request.getApprovalLevel2UserName());
        approvalParams.put("approvalLevel2Time", request.getApprovalLevel2Time());
        approvalParams.put("approvalLevel2Comment", request.getApprovalLevel2Comment());
        approvalParams.put("approvalLevel3Status", request.getApprovalLevel3Status());
        approvalParams.put("approvalLevel3UserId", request.getApprovalLevel3UserId());
        approvalParams.put("approvalLevel3UserName", request.getApprovalLevel3UserName());
        approvalParams.put("approvalLevel3Time", request.getApprovalLevel3Time());
        approvalParams.put("approvalLevel3Comment", request.getApprovalLevel3Comment());

        Date now = new Date();

        if (currentLevel == 1) {
            // 第一级审批：编制管理岗
            approvalParams.put("approvalLevel1Status", "REJECTED");
            approvalParams.put("approvalLevel1UserId", params.get("approvalUserId"));
            approvalParams.put("approvalLevel1UserName", params.get("approvalUserName"));
            approvalParams.put("approvalLevel1Time", now);
            approvalParams.put("approvalLevel1Comment", params.get("approvalComment"));
            approvalParams.put("currentApprovalLevel", currentLevel);
        } else if (currentLevel == 2) {
            // 第二级审批：外包招聘岗
            approvalParams.put("approvalLevel2Status", "REJECTED");
            approvalParams.put("approvalLevel2UserId", params.get("approvalUserId"));
            approvalParams.put("approvalLevel2UserName", params.get("approvalUserName"));
            approvalParams.put("approvalLevel2Time", now);
            approvalParams.put("approvalLevel2Comment", params.get("approvalComment"));
            // 第二级审批拒绝，回退到第一级审批
            approvalParams.put("currentApprovalLevel", 1);
            approvalParams.put("approvalLevel1Status", "PENDING");
            approvalParams.put("approvalLevel1UserId", null);
            approvalParams.put("approvalLevel1UserName", null);
            approvalParams.put("approvalLevel1Time", null);
            approvalParams.put("approvalLevel1Comment", null);
        } else if (currentLevel == 3) {
            // 第三级审批：团队经理
            approvalParams.put("approvalLevel3Status", "REJECTED");
            approvalParams.put("approvalLevel3UserId", params.get("approvalUserId"));
            approvalParams.put("approvalLevel3UserName", params.get("approvalUserName"));
            approvalParams.put("approvalLevel3Time", now);
            approvalParams.put("approvalLevel3Comment", params.get("approvalComment"));
            approvalParams.put("currentApprovalLevel", currentLevel);
        } else {
            throw new RuntimeException("审批流程已完成");
        }

        approvalParams.put("approvalStatus", "REJECTED");

        recruitmentRequestMapper.updateThreeLevelApprovalStatus(approvalParams);
        
        // 记录审批历史
        recordApprovalHistory(id, currentLevel, params, "REJECTED");
    }

    @Override
    public List<RecruitmentRequest> getPendingApprovalByRole(String userRole) {
        List<RecruitmentRequest> allPending = recruitmentRequestMapper.selectPendingApproval();
        List<RecruitmentRequest> rolePending = new ArrayList<>();

        System.out.println("获取待审批列表，用户角色：" + userRole);
        System.out.println("所有待审批申请数量：" + allPending.size());

        for (RecruitmentRequest request : allPending) {
            Integer currentLevel = request.getCurrentApprovalLevel();
            String approvalStatus = request.getApprovalStatus();
            
            System.out.println("申请ID：" + request.getRecruitmentRequestId() + ", 当前审批级别：" + currentLevel + ", 审批状态：" + approvalStatus);

            if (currentLevel == null) {
                continue;
            }

            if ("编制管理岗".equals(userRole) && currentLevel == 1 && "PENDING".equals(approvalStatus)) {
                // 第一级审批：编制管理岗，查看状态为PENDING的申请
                System.out.println("添加到编制管理岗待审批列表：" + request.getRecruitmentRequestId());
                rolePending.add(request);
            } else if ("外包招聘岗".equals(userRole) && currentLevel == 2 && "1STAPPROVED".equals(approvalStatus)) {
                // 第二级审批：外包招聘岗，查看状态为1STAPPROVED的申请
                System.out.println("添加到外包招聘岗待审批列表：" + request.getRecruitmentRequestId());
                rolePending.add(request);
            } else if ("团队经理".equals(userRole) && currentLevel == 3 && "2NDAPPROVED".equals(approvalStatus)) {
                // 第三级审批：团队经理，查看状态为2NDAPPROVED的申请
                System.out.println("添加到团队经理待审批列表：" + request.getRecruitmentRequestId());
                rolePending.add(request);
            } else if ("室经理".equals(userRole) || "分管总".equals(userRole) || "总经理".equals(userRole) || "外包供应商".equals(userRole)) {
                // 其他角色可以查看所有待审批的申请
                System.out.println("添加到其他角色待审批列表：" + request.getRecruitmentRequestId());
                rolePending.add(request);
            }
        }

        System.out.println("最终待审批列表数量：" + rolePending.size());
        return rolePending;
    }

    private void recordApprovalHistory(Long recruitmentRequestId, Integer approvalLevel, Map<String, Object> params, String approvalStatus) {
        ApprovalHistory history = new ApprovalHistory();
        history.setRecruitmentRequestId(recruitmentRequestId);
        history.setApprovalLevel(approvalLevel);
        history.setApproverId((String) params.get("approvalUserId"));
        history.setApproverName((String) params.get("approvalUserName"));
        history.setApprovalStatus(approvalStatus);
        history.setApprovalComment((String) params.get("approvalComment"));
        history.setApprovalTime(new Date());
        
        approvalHistoryMapper.insert(history);
    }

    @Override
    public List<ApprovalHistory> getApprovalHistoryByRequestId(Long recruitmentRequestId) {
        return approvalHistoryMapper.selectByRecruitmentRequestId(recruitmentRequestId);
    }
}