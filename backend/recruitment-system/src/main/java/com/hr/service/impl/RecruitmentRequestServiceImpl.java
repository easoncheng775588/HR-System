package com.hr.service.impl;

import com.hr.entity.ApprovalHistory;
import com.hr.entity.OrgUnit;
import com.hr.entity.RecruitmentRequest;
import com.hr.entity.User;
import com.hr.entity.WorkflowProcessLog;
import com.hr.mapper.ApprovalHistoryMapper;
import com.hr.mapper.OrgUnitMapper;
import com.hr.mapper.RecruitmentRequestMapper;
import com.hr.mapper.UserMapper;
import com.hr.mapper.WorkflowProcessLogMapper;
import com.hr.service.RecruitmentRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecruitmentRequestServiceImpl implements RecruitmentRequestService {

    private static final Logger logger = LoggerFactory.getLogger(RecruitmentRequestServiceImpl.class);

    @Autowired
    private RecruitmentRequestMapper recruitmentRequestMapper;

    @Autowired
    private ApprovalHistoryMapper approvalHistoryMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrgUnitMapper orgUnitMapper;

    @Autowired
    private WorkflowProcessLogMapper workflowProcessLogMapper;

    @Override
    public RecruitmentRequest saveDraft(RecruitmentRequest request) {
        if (request.getCreateUserId() == null) {
            request.setCreateUserId("1001");
            request.setCreateUserName("系统用户");
        }
        request.setUpdateUserId(request.getCreateUserId());
        request.setUpdateUserName(request.getCreateUserName());

        if (request.getApprovalStatus() == null) {
            request.setApprovalStatus("DRAFT");
        }
        if (request.getPositionPublishStatus() == null) {
            request.setPositionPublishStatus("NOT_PUBLISHED");
        }
        if (request.getCurrentApprovalLevel() == null) {
            request.setCurrentApprovalLevel(0);
        }
        if (request.getApprovalLevel1Status() == null) {
            request.setApprovalLevel1Status("PENDING");
        }
        if (request.getApprovalLevel2Status() == null) {
            request.setApprovalLevel2Status("PENDING");
        }
        if (request.getApprovalLevel3Status() == null) {
            request.setApprovalLevel3Status("PENDING");
        }

        ensureDraftDefaults(request);

        if (request.getRecruitmentRequestId() == null) {
            recruitmentRequestMapper.insert(request);
        } else {
            request.setUpdateTime(new Date());
            recruitmentRequestMapper.updateByPrimaryKey(request);
        }
        return request;
    }

    @Override
    public RecruitmentRequest submitRequest(RecruitmentRequest request) {
        if (request.getCreateUserId() == null || request.getCreateUserId().trim().isEmpty()) {
            request.setCreateUserId("1001");
            request.setCreateUserName("系统用户");
        }

        User submitter = userMapper.getUserById(request.getCreateUserId());
        if (submitter != null && !"1001".equals(request.getCreateUserId())) {
            String position = submitter.getPosition() == null ? "" : submitter.getPosition();
            if (!position.contains("室经理")) {
                throw new RuntimeException("仅室经理可提交用人申请");
            }
        }

        if ((request.getTeam() == null || request.getTeam().trim().isEmpty()) && submitter != null) {
            request.setTeam(submitter.getDepartment());
        }

        request.setApprovalStatus("PENDING");
        request.setCurrentApprovalLevel(1);
        request.setApprovalLevel1Status("PENDING");
        request.setApprovalLevel2Status("PENDING");
        request.setApprovalLevel3Status("PENDING");

        Date now = new Date();
        request.setCreateTime(now);
        request.setUpdateTime(now);
        if (request.getCreateUserName() == null || request.getCreateUserName().trim().isEmpty()) {
            request.setCreateUserName(submitter != null ? submitter.getRealName() : "系统用户");
        }
        request.setUpdateUserId(request.getCreateUserId());
        request.setUpdateUserName(request.getCreateUserName());

        if (request.getRecruitmentRequestId() == null) {
            recruitmentRequestMapper.insert(request);
        } else {
            recruitmentRequestMapper.updateByPrimaryKey(request);
        }

        recordWorkflowSubmitLog(request.getRecruitmentRequestId(), request.getTeam(), request.getCreateUserId(), request.getCreateUserName());
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
            throw new RuntimeException("该申请已审批");
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
            throw new RuntimeException("该申请已审批");
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

        existingRequest.setRequestTitle(request.getRequestTitle());
        existingRequest.setTotalRecruitmentCount(request.getTotalRecruitmentCount());
        existingRequest.setVacancyCount(request.getVacancyCount());
        existingRequest.setTeam(request.getTeam());
        existingRequest.setTechnicalPlatform(request.getTechnicalPlatform());
        existingRequest.setCategory(request.getCategory());
        existingRequest.setSupplementCount(request.getSupplementCount());
        existingRequest.setUrgentRequirement(request.getUrgentRequirement());
        existingRequest.setProposedLevel(request.getProposedLevel());
        existingRequest.setExperienceYears(request.getExperienceYears());
        existingRequest.setSkillRequirement(request.getSkillRequirement());
        existingRequest.setPositionResponsibility(request.getPositionResponsibility());
        existingRequest.setInterviewerId(request.getInterviewerId());
        existingRequest.setInterviewerName(request.getInterviewerName());

        existingRequest.setUpdateUserId(request.getUpdateUserId() == null ? "1001" : request.getUpdateUserId());
        existingRequest.setUpdateUserName(request.getUpdateUserName() == null ? "系统用户" : request.getUpdateUserName());
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
        existingRequest.setUpdateTime(new Date());
        recruitmentRequestMapper.updateByPrimaryKey(existingRequest);
        return recruitmentRequestMapper.selectByPrimaryKey(id);
    }

    @Override
    public RecruitmentRequest threeLevelApproveRequest(Long id, Map<String, Object> params) {
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
            approvalParams.put("approvalLevel1Status", "APPROVED");
            approvalParams.put("approvalLevel1UserId", params.get("approvalUserId"));
            approvalParams.put("approvalLevel1UserName", params.get("approvalUserName"));
            approvalParams.put("approvalLevel1Time", now);
            approvalParams.put("approvalLevel1Comment", params.get("approvalComment"));
            approvalParams.put("currentApprovalLevel", 2);
            approvalParams.put("approvalStatus", "1STAPPROVED");
        } else if (currentLevel == 2) {
            approvalParams.put("approvalLevel2Status", "APPROVED");
            approvalParams.put("approvalLevel2UserId", params.get("approvalUserId"));
            approvalParams.put("approvalLevel2UserName", params.get("approvalUserName"));
            approvalParams.put("approvalLevel2Time", now);
            approvalParams.put("approvalLevel2Comment", params.get("approvalComment"));
            approvalParams.put("currentApprovalLevel", 3);
            approvalParams.put("approvalStatus", "2NDAPPROVED");
        } else if (currentLevel == 3) {
            String approvalUserId = String.valueOf(params.get("approvalUserId"));
            if (!"1001".equals(approvalUserId)) {
                String teamName = resolveTeamNameByDepartment(request.getTeam());
                User teamManager = userMapper.getActiveTeamManagerByDepartment(teamName);
                if (teamManager == null || !approvalUserId.equals(teamManager.getUserId())) {
                    throw new RuntimeException("仅申请部门所属团队经理可进行第三级审批");
                }
            }

            approvalParams.put("approvalLevel3Status", "APPROVED");
            approvalParams.put("approvalLevel3UserId", params.get("approvalUserId"));
            approvalParams.put("approvalLevel3UserName", params.get("approvalUserName"));
            approvalParams.put("approvalLevel3Time", now);
            approvalParams.put("approvalLevel3Comment", params.get("approvalComment"));
            approvalParams.put("currentApprovalLevel", 4);
            approvalParams.put("approvalStatus", "3RDAPPROVED");
        } else {
            throw new RuntimeException("审批流程已完成");
        }

        recruitmentRequestMapper.updateThreeLevelApprovalStatus(approvalParams);
        recordApprovalHistory(id, currentLevel, params, "APPROVED");
        return recruitmentRequestMapper.selectByPrimaryKey(id);
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
            approvalParams.put("approvalLevel1Status", "REJECTED");
            approvalParams.put("approvalLevel1UserId", params.get("approvalUserId"));
            approvalParams.put("approvalLevel1UserName", params.get("approvalUserName"));
            approvalParams.put("approvalLevel1Time", now);
            approvalParams.put("approvalLevel1Comment", params.get("approvalComment"));
            approvalParams.put("currentApprovalLevel", currentLevel);
        } else if (currentLevel == 2) {
            approvalParams.put("approvalLevel2Status", "REJECTED");
            approvalParams.put("approvalLevel2UserId", params.get("approvalUserId"));
            approvalParams.put("approvalLevel2UserName", params.get("approvalUserName"));
            approvalParams.put("approvalLevel2Time", now);
            approvalParams.put("approvalLevel2Comment", params.get("approvalComment"));
            approvalParams.put("currentApprovalLevel", 1);
            approvalParams.put("approvalLevel1Status", "PENDING");
            approvalParams.put("approvalLevel1UserId", null);
            approvalParams.put("approvalLevel1UserName", null);
            approvalParams.put("approvalLevel1Time", null);
            approvalParams.put("approvalLevel1Comment", null);
        } else if (currentLevel == 3) {
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
        recordApprovalHistory(id, currentLevel, params, "REJECTED");
    }

    @Override
    public List<RecruitmentRequest> getPendingApprovalByRole(String userRole) {
        List<RecruitmentRequest> allPending = recruitmentRequestMapper.selectPendingApproval();
        List<RecruitmentRequest> rolePending = new ArrayList<>();

        logger.debug("获取待审批列表，用户角色：{}", userRole);
        for (RecruitmentRequest request : allPending) {
            Integer currentLevel = request.getCurrentApprovalLevel();
            String approvalStatus = request.getApprovalStatus();
            if (currentLevel == null) {
                continue;
            }

            if ("编制管理岗".equals(userRole) && currentLevel == 1 && "PENDING".equals(approvalStatus)) {
                rolePending.add(request);
            } else if (("外包招聘岗".equals(userRole) || "外包招聘管理岗".equals(userRole)) && currentLevel == 2 && "1STAPPROVED".equals(approvalStatus)) {
                rolePending.add(request);
            } else if ("团队经理".equals(userRole) && currentLevel == 3 && "2NDAPPROVED".equals(approvalStatus)) {
                rolePending.add(request);
            } else if ("管理员".equals(userRole) || "系统管理员".equals(userRole)) {
                rolePending.add(request);
            }
        }

        return rolePending;
    }

    private void ensureDraftDefaults(RecruitmentRequest request) {
        if (request.getRequestTitle() == null || request.getRequestTitle().trim().isEmpty()) {
            request.setRequestTitle("未命名申请");
        }
        if (request.getTotalRecruitmentCount() == null) {
            request.setTotalRecruitmentCount(0);
        }
        if (request.getVacancyCount() == null) {
            request.setVacancyCount(0);
        }
        if (request.getTeam() == null || request.getTeam().trim().isEmpty()) {
            request.setTeam("未设置部门");
        }
        if (request.getTechnicalPlatform() == null || request.getTechnicalPlatform().trim().isEmpty()) {
            request.setTechnicalPlatform("其他");
        }
        if (request.getCategory() == null || request.getCategory().trim().isEmpty()) {
            request.setCategory("其他");
        }
        if (request.getSupplementCount() == null) {
            request.setSupplementCount(0);
        }
        if (request.getUrgentRequirement() == null || request.getUrgentRequirement().trim().isEmpty()) {
            request.setUrgentRequirement("NO");
        }
        if (request.getProposedLevel() == null || request.getProposedLevel().trim().isEmpty()) {
            request.setProposedLevel("PT");
        }
        if (request.getExperienceYears() == null || request.getExperienceYears().trim().isEmpty()) {
            request.setExperienceYears("-");
        }
        if (request.getPositionResponsibility() == null || request.getPositionResponsibility().trim().isEmpty()) {
            request.setPositionResponsibility("-");
        }
        if (request.getSkillRequirement() == null || request.getSkillRequirement().trim().isEmpty()) {
            request.setSkillRequirement("-");
        }
    }

    private String resolveTeamNameByDepartment(String department) {
        if (department == null || department.trim().isEmpty()) {
            return "";
        }
        OrgUnit orgUnit = orgUnitMapper.getByUnitName(department);
        if (orgUnit == null) {
            return department;
        }
        if (orgUnit.getParentUnitName() == null || orgUnit.getParentUnitName().trim().isEmpty()) {
            return orgUnit.getUnitName();
        }
        return orgUnit.getParentUnitName();
    }

    private void recordWorkflowSubmitLog(Long requestId, String department, String userId, String userName) {
        if (requestId == null) {
            return;
        }
        WorkflowProcessLog log = new WorkflowProcessLog();
        log.setProcessCode("RECRUITMENT_REQUEST");
        log.setBusinessId(requestId);
        log.setNodeOrder(0);
        log.setNodeName("提交申请");
        log.setActionType("SUBMIT");
        log.setActionResult("SUCCESS");
        log.setOperatorId(userId);
        log.setOperatorName(userName);
        log.setOperatorRole("室经理");
        log.setActionComment("申请部门: " + (department == null ? "-" : department));
        log.setActionTime(new Date());
        workflowProcessLogMapper.insert(log);
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
