package com.hr.service.impl;

import com.hr.entity.ApprovalHistory;
import com.hr.entity.Message;
import com.hr.entity.OrgUnit;
import com.hr.entity.RecruitmentRequest;
import com.hr.entity.Staffing;
import com.hr.entity.User;
import com.hr.entity.WorkflowProcessLog;
import com.hr.mapper.ApprovalHistoryMapper;
import com.hr.mapper.OrgUnitMapper;
import com.hr.mapper.RecruitmentRequestMapper;
import com.hr.mapper.StaffingMapper;
import com.hr.mapper.UserMapper;
import com.hr.mapper.WorkflowProcessLogMapper;
import com.hr.service.MessageService;
import com.hr.service.RecruitmentRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RecruitmentRequestServiceImpl implements RecruitmentRequestService {

    private static final Logger logger = LoggerFactory.getLogger(RecruitmentRequestServiceImpl.class);
    private static final String ROLE_ROOM_MANAGER = "室经理";
    private static final String ROLE_DIRECT_TEAM_MANAGER = "直属团队经理";
    private static final String ROLE_TEAM_MANAGER = "团队经理";
    private static final String ROLE_STAFFING_MANAGER = "编制管理岗";
    private static final String ROLE_OUTSOURCING_MANAGER = "外包招聘管理岗";
    private static final String ROLE_DIRECTOR = "分管总";
    private static final String ROLE_SUPER_ADMIN = "超级管理员";
    private static final String ROOM_MANAGER_TYPE = "ROOM_MANAGER";
    private static final String DIRECT_TEAM_MANAGER_TYPE = "DIRECT_TEAM_MANAGER";
    private static final String SYSTEM_USER_ID = "1001";
    private static final String SYSTEM_USER_NAME = "系统用户";
    private static final Map<String, String> DIRECT_TEAM_APPROVER_NAME_MAP = Map.of(
        "人力资源团队", "邓检生",
        "技术管理团队", "邓检生",
        "综合管理团队", "邓检生"
    );

    @Autowired
    private RecruitmentRequestMapper recruitmentRequestMapper;

    @Autowired
    private ApprovalHistoryMapper approvalHistoryMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrgUnitMapper orgUnitMapper;

    @Autowired
    private StaffingMapper staffingMapper;

    @Autowired
    private WorkflowProcessLogMapper workflowProcessLogMapper;

    @Autowired
    private MessageService messageService;

    @Override
    public RecruitmentRequest saveDraft(RecruitmentRequest request) {
        if (request.getCreateUserId() == null) {
            request.setCreateUserId(SYSTEM_USER_ID);
            request.setCreateUserName(SYSTEM_USER_NAME);
        }
        User submitter = userMapper.getUserById(request.getCreateUserId());
        if ((request.getCreateUserName() == null || request.getCreateUserName().trim().isEmpty()) && submitter != null) {
            request.setCreateUserName(submitter.getRealName());
        }
        if (submitter != null) {
            populateRequestContext(request, submitter, false);
        }
        request.setUpdateUserId(request.getCreateUserId());
        request.setUpdateUserName(request.getCreateUserName());

        if (request.getApprovalStatus() == null) {
            request.setApprovalStatus("DRAFT");
        }
        if (request.getStatus() == null || request.getStatus().trim().isEmpty()) {
            request.setStatus("DRAFT");
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
            request.setCreateUserId(SYSTEM_USER_ID);
            request.setCreateUserName(SYSTEM_USER_NAME);
        }

        User submitter = userMapper.getUserById(request.getCreateUserId());
        if (submitter == null && !SYSTEM_USER_ID.equals(request.getCreateUserId())) {
            throw new RuntimeException("当前用户未配置有效部门信息");
        }
        if (submitter != null) {
            populateRequestContext(request, submitter, true);
        }

        request.setApprovalStatus("PENDING");
        request.setStatus("SUBMITTED");
        if (request.getPositionPublishStatus() == null || request.getPositionPublishStatus().trim().isEmpty()) {
            request.setPositionPublishStatus("NOT_PUBLISHED");
        }
        request.setCurrentApprovalLevel(1);
        request.setApprovalLevel1Status("PENDING");
        request.setApprovalLevel2Status("PENDING");
        request.setApprovalLevel3Status("PENDING");

        Date now = new Date();
        request.setCreateTime(now);
        request.setUpdateTime(now);
        if (request.getCreateUserName() == null || request.getCreateUserName().trim().isEmpty()) {
            request.setCreateUserName(submitter != null ? submitter.getRealName() : SYSTEM_USER_NAME);
        }
        request.setUpdateUserId(request.getCreateUserId());
        request.setUpdateUserName(request.getCreateUserName());

        if (request.getRecruitmentRequestId() == null) {
            recruitmentRequestMapper.insert(request);
        } else {
            recruitmentRequestMapper.updateByPrimaryKey(request);
        }

        recordWorkflowSubmitLog(
            request.getRecruitmentRequestId(),
            request.getApplicationDepartment(),
            request.getCreateUserId(),
            request.getCreateUserName(),
            ROOM_MANAGER_TYPE.equals(request.getSubmitterRoleType()) ? ROLE_ROOM_MANAGER : ROLE_DIRECT_TEAM_MANAGER
        );
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
    public List<RecruitmentRequest> getAll(String viewerId, String viewerRole) {
        List<RecruitmentRequest> allRequests = recruitmentRequestMapper.selectAll();
        if (viewerId == null || viewerId.trim().isEmpty()) {
            return allRequests;
        }

        User viewer = userMapper.getUserById(viewerId);
        if (viewer == null) {
            return new ArrayList<>();
        }

        Set<String> normalizedRoles = resolveNormalizedRoles(viewer, viewerRole);
        if (normalizedRoles.contains(ROLE_SUPER_ADMIN)
            || normalizedRoles.contains(ROLE_OUTSOURCING_MANAGER)
            || normalizedRoles.contains(ROLE_STAFFING_MANAGER)) {
            return allRequests;
        }

        List<RecruitmentRequest> visibleRequests = new ArrayList<>();
        for (RecruitmentRequest request : allRequests) {
            if (canViewRequest(viewer, normalizedRoles, request)) {
                visibleRequests.add(request);
            }
        }
        return visibleRequests;
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
        approvalParams.put("status", "APPROVED");

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
        approvalParams.put("status", "REJECTED");

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
        if (!"DRAFT".equals(existingRequest.getApprovalStatus()) && !"PENDING".equals(existingRequest.getApprovalStatus())) {
            throw new RuntimeException("当前状态不允许编辑");
        }

        User submitter = userMapper.getUserById(existingRequest.getCreateUserId());
        if (submitter != null) {
            request.setCreateUserId(existingRequest.getCreateUserId());
            request.setCreateUserName(existingRequest.getCreateUserName());
            populateRequestContext(request, submitter, true);
        }

        existingRequest.setRequestTitle(request.getRequestTitle());
        existingRequest.setTotalRecruitmentCount(request.getTotalRecruitmentCount());
        existingRequest.setVacancyCount(request.getVacancyCount());
        existingRequest.setTeam(request.getTeam());
        existingRequest.setApplicationDepartment(request.getApplicationDepartment());
        existingRequest.setOrgUnitName(request.getOrgUnitName());
        existingRequest.setRequestType(request.getRequestType());
        existingRequest.setRemark(request.getRemark());
        existingRequest.setSubmitterRoleType(request.getSubmitterRoleType());
        existingRequest.setFinalApproverUserId(request.getFinalApproverUserId());
        existingRequest.setFinalApproverUserName(request.getFinalApproverUserName());
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
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            existingRequest.setStatus(request.getStatus());
        }

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

        existingRequest.setPositionPublishStatus(normalizePublishStatus(publishStatus));
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
            approvalParams.put("status", "SUBMITTED");
        } else if (currentLevel == 2) {
            approvalParams.put("approvalLevel2Status", "APPROVED");
            approvalParams.put("approvalLevel2UserId", params.get("approvalUserId"));
            approvalParams.put("approvalLevel2UserName", params.get("approvalUserName"));
            approvalParams.put("approvalLevel2Time", now);
            approvalParams.put("approvalLevel2Comment", params.get("approvalComment"));
            approvalParams.put("currentApprovalLevel", 3);
            approvalParams.put("approvalStatus", "2NDAPPROVED");
            approvalParams.put("status", "SUBMITTED");
        } else if (currentLevel == 3) {
            String approvalUserId = String.valueOf(params.get("approvalUserId"));
            if (!SYSTEM_USER_ID.equals(approvalUserId) && !canHandleFinalApproval(approvalUserId, request)) {
                if (DIRECT_TEAM_MANAGER_TYPE.equals(request.getSubmitterRoleType())) {
                    throw new RuntimeException("仅指定分管总可进行第三级审批");
                }
                throw new RuntimeException("仅申请部门所属团队经理可进行第三级审批");
            }

            approvalParams.put("approvalLevel3Status", "APPROVED");
            approvalParams.put("approvalLevel3UserId", params.get("approvalUserId"));
            approvalParams.put("approvalLevel3UserName", params.get("approvalUserName"));
            approvalParams.put("approvalLevel3Time", now);
            approvalParams.put("approvalLevel3Comment", params.get("approvalComment"));
            approvalParams.put("currentApprovalLevel", 4);
            approvalParams.put("approvalStatus", "3RDAPPROVED");
            approvalParams.put("status", "APPROVED");
        } else {
            throw new RuntimeException("审批流程已完成");
        }

        recruitmentRequestMapper.updateThreeLevelApprovalStatus(approvalParams);
        recordApprovalHistory(id, currentLevel, params, "APPROVED");
        RecruitmentRequest updatedRequest = recruitmentRequestMapper.selectByPrimaryKey(id);
        if (updatedRequest != null && isFinalApproved(updatedRequest)) {
            sendCompletionNotifications(updatedRequest);
        }
        return updatedRequest;
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
        approvalParams.put("status", "REJECTED");
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
            } else if ("团队经理".equals(userRole)
                && currentLevel == 3
                && "2NDAPPROVED".equals(approvalStatus)
                && !DIRECT_TEAM_MANAGER_TYPE.equals(request.getSubmitterRoleType())) {
                rolePending.add(request);
            } else if ("分管总".equals(userRole)
                && currentLevel == 3
                && "2NDAPPROVED".equals(approvalStatus)
                && DIRECT_TEAM_MANAGER_TYPE.equals(request.getSubmitterRoleType())) {
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
        if (request.getApplicationDepartment() == null || request.getApplicationDepartment().trim().isEmpty()) {
            request.setApplicationDepartment(request.getTeam());
        }
        if (request.getRequestType() == null || request.getRequestType().trim().isEmpty()) {
            request.setRequestType("NEW_DEMAND");
        }
        if (request.getRemark() == null) {
            request.setRemark("");
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

    private String normalizePublishStatus(String publishStatus) {
        if (publishStatus == null || publishStatus.trim().isEmpty()) {
            return "NOT_PUBLISHED";
        }
        if ("UNPUBLISHED".equalsIgnoreCase(publishStatus) || "NOT_PUBLISHED".equalsIgnoreCase(publishStatus)) {
            return "NOT_PUBLISHED";
        }
        return publishStatus.trim();
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

    private void recordWorkflowSubmitLog(Long requestId, String department, String userId, String userName, String operatorRole) {
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
        log.setOperatorRole(operatorRole);
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

    private void populateRequestContext(RecruitmentRequest request, User submitter, boolean strictSubmission) {
        if (submitter == null) {
            return;
        }

        String roleType = resolveSubmitterRoleType(submitter);
        if (strictSubmission && roleType == null) {
            throw new RuntimeException("仅室经理或直属团队经理可提交用人申请");
        }

        String applicationDepartment = resolveApplicationDepartment(submitter);
        String orgUnitName = resolveOrgUnitName(submitter, roleType);
        if (strictSubmission && (applicationDepartment == null || orgUnitName == null)) {
            throw new RuntimeException("当前用户未配置有效部门信息");
        }

        Staffing staffing = orgUnitName == null ? null : staffingMapper.getStaffingByOrgUnitName(orgUnitName);
        if (strictSubmission && staffing == null) {
            throw new RuntimeException("当前申请部门未配置编制信息");
        }

        request.setApplicationDepartment(applicationDepartment);
        request.setOrgUnitName(orgUnitName);
        request.setTeam(applicationDepartment);
        request.setSubmitterRoleType(roleType);
        request.setTotalRecruitmentCount(staffing == null ? 0 : staffing.getTotalHeadcount());
        request.setVacancyCount(staffing == null ? 0 : staffing.getVacancyHeadcount());

        if (strictSubmission && request.getSupplementCount() != null && staffing != null
            && request.getSupplementCount() > staffing.getVacancyHeadcount()) {
            throw new RuntimeException("补充人数不能超过空缺编制数");
        }

        if (ROOM_MANAGER_TYPE.equals(roleType) && strictSubmission) {
            String teamName = submitter.getTeamName();
            if (teamName == null || teamName.trim().isEmpty()) {
                teamName = resolveTeamNameByDepartment(orgUnitName);
            }
            User teamManager = findTeamManager(teamName);
            if (teamManager == null) {
                throw new RuntimeException("未找到所属团队经理");
            }
            request.setFinalApproverUserId(teamManager.getUserId());
            request.setFinalApproverUserName(teamManager.getRealName());
        } else if (DIRECT_TEAM_MANAGER_TYPE.equals(roleType)) {
            String teamName = submitter.getTeamName();
            String approverName = DIRECT_TEAM_APPROVER_NAME_MAP.get(teamName);
            if (strictSubmission && (approverName == null || approverName.trim().isEmpty())) {
                throw new RuntimeException("未找到直属团队对应的分管总");
            }
            User director = approverName == null ? null : userMapper.getActiveUserByRealName(approverName);
            if (strictSubmission && director == null) {
                throw new RuntimeException("未找到直属团队对应的分管总");
            }
            request.setFinalApproverUserId(director == null ? null : director.getUserId());
            request.setFinalApproverUserName(director == null ? approverName : director.getRealName());
        }
    }

    private String resolveSubmitterRoleType(User submitter) {
        Set<String> roles = resolveNormalizedRoles(submitter, null);
        if (roles.contains(ROLE_ROOM_MANAGER)) {
            return ROOM_MANAGER_TYPE;
        }
        if (roles.contains(ROLE_DIRECT_TEAM_MANAGER)) {
            return DIRECT_TEAM_MANAGER_TYPE;
        }

        String position = submitter.getPosition() == null ? "" : submitter.getPosition();
        if (position.contains(ROLE_ROOM_MANAGER)) {
            return ROOM_MANAGER_TYPE;
        }
        if (position.contains(ROLE_TEAM_MANAGER) && isDirectTeam(submitter.getTeamName())) {
            return DIRECT_TEAM_MANAGER_TYPE;
        }
        return null;
    }

    private Set<String> resolveNormalizedRoles(User user, String viewerRole) {
        Set<String> roles = new LinkedHashSet<>();
        if (user != null) {
            if (SYSTEM_USER_ID.equals(user.getUserId())) {
                roles.add(ROLE_SUPER_ADMIN);
            }
            for (String roleName : userMapper.getRoleNamesByUserId(user.getUserId())) {
                roles.add(normalizeRoleName(roleName));
            }
            roles.add(normalizeRoleName(user.getPosition()));
            if (ROOM_MANAGER_TYPE.equals(resolveSubmitterRoleTypeFallback(user))) {
                roles.add(ROLE_ROOM_MANAGER);
            }
            if (DIRECT_TEAM_MANAGER_TYPE.equals(resolveSubmitterRoleTypeFallback(user))) {
                roles.add(ROLE_DIRECT_TEAM_MANAGER);
            }
        }
        if (viewerRole != null && !viewerRole.trim().isEmpty()) {
            roles.add(normalizeRoleName(viewerRole));
        }
        roles.remove("");
        return roles;
    }

    private String resolveSubmitterRoleTypeFallback(User user) {
        if (user == null) {
            return null;
        }
        String position = user.getPosition() == null ? "" : user.getPosition();
        if (position.contains(ROLE_ROOM_MANAGER)) {
            return ROOM_MANAGER_TYPE;
        }
        if (position.contains(ROLE_TEAM_MANAGER) && isDirectTeam(user.getTeamName())) {
            return DIRECT_TEAM_MANAGER_TYPE;
        }
        return null;
    }

    private String normalizeRoleName(String roleName) {
        if (roleName == null) {
            return "";
        }
        String value = roleName.trim();
        if (value.contains("超级管理员") || value.contains("系统管理员") || value.contains("管理员")) {
            return ROLE_SUPER_ADMIN;
        }
        if (value.contains("编制管理")) {
            return ROLE_STAFFING_MANAGER;
        }
        if (value.contains("外包招聘")) {
            return ROLE_OUTSOURCING_MANAGER;
        }
        if (value.contains("直属团队经理")) {
            return ROLE_DIRECT_TEAM_MANAGER;
        }
        if (value.contains("分管总")) {
            return ROLE_DIRECTOR;
        }
        if (value.contains("团队经理")) {
            return ROLE_TEAM_MANAGER;
        }
        if (value.contains("室经理")) {
            return ROLE_ROOM_MANAGER;
        }
        return value;
    }

    private String resolveApplicationDepartment(User submitter) {
        if (submitter.getDepartmentDisplay() != null && !submitter.getDepartmentDisplay().trim().isEmpty()) {
            return submitter.getDepartmentDisplay();
        }
        if (submitter.getDepartment() != null && !submitter.getDepartment().trim().isEmpty()) {
            return submitter.getDepartment();
        }
        if (submitter.getTeamName() != null && submitter.getGroupName() != null) {
            return submitter.getTeamName() + " / " + submitter.getGroupName();
        }
        return submitter.getTeamName();
    }

    private String resolveOrgUnitName(User submitter, String roleType) {
        if (ROOM_MANAGER_TYPE.equals(roleType) && submitter.getGroupName() != null && !submitter.getGroupName().trim().isEmpty()) {
            return submitter.getGroupName();
        }
        if (submitter.getTeamName() != null && !submitter.getTeamName().trim().isEmpty()) {
            return submitter.getTeamName();
        }
        if (submitter.getGroupName() != null && !submitter.getGroupName().trim().isEmpty()) {
            return submitter.getGroupName();
        }
        return submitter.getDepartment();
    }

    private boolean isDirectTeam(String teamName) {
        if (teamName == null || teamName.trim().isEmpty()) {
            return false;
        }
        List<OrgUnit> orgUnits = orgUnitMapper.getActiveOrgUnits();
        for (OrgUnit orgUnit : orgUnits) {
            if (teamName.equals(orgUnit.getParentUnitName()) && "GROUP".equals(orgUnit.getUnitType())) {
                return false;
            }
        }
        return true;
    }

    private boolean canViewRequest(User viewer, Set<String> normalizedRoles, RecruitmentRequest request) {
        if (request == null) {
            return false;
        }
        if (normalizedRoles.contains(ROLE_ROOM_MANAGER) || normalizedRoles.contains(ROLE_DIRECT_TEAM_MANAGER)) {
            return viewer.getUserId().equals(request.getCreateUserId());
        }
        if (normalizedRoles.contains(ROLE_TEAM_MANAGER)) {
            String viewerTeam = viewer.getTeamName() == null ? resolveTeamNameByDepartment(viewer.getDepartment()) : viewer.getTeamName();
            String requestTeam = resolveTeamNameByDepartment(resolveRequestOrgUnit(request));
            return viewerTeam != null && viewerTeam.equals(requestTeam);
        }
        if (normalizedRoles.contains(ROLE_DIRECTOR)) {
            return viewer.getUserId().equals(request.getFinalApproverUserId());
        }
        return false;
    }

    private String resolveRequestOrgUnit(RecruitmentRequest request) {
        if (request.getOrgUnitName() != null && !request.getOrgUnitName().trim().isEmpty()) {
            return request.getOrgUnitName();
        }
        if (request.getApplicationDepartment() != null && !request.getApplicationDepartment().trim().isEmpty()) {
            return request.getApplicationDepartment();
        }
        return request.getTeam();
    }

    private boolean canHandleFinalApproval(String approvalUserId, RecruitmentRequest request) {
        if (approvalUserId == null || request == null) {
            return false;
        }
        if (DIRECT_TEAM_MANAGER_TYPE.equals(request.getSubmitterRoleType())) {
            return approvalUserId.equals(request.getFinalApproverUserId());
        }
        String teamName = resolveTeamNameByDepartment(resolveRequestOrgUnit(request));
        User teamManager = findTeamManager(teamName);
        return teamManager != null && approvalUserId.equals(teamManager.getUserId());
    }

    private User findTeamManager(String teamName) {
        if (teamName == null || teamName.trim().isEmpty()) {
            return null;
        }

        User manager = userMapper.getActiveTeamManagerByDepartment(teamName);
        if (manager != null) {
            return manager;
        }

        List<User> managers = userMapper.getActiveUsersByRoleName(ROLE_TEAM_MANAGER);
        for (User candidate : managers) {
            if (candidate == null) {
                continue;
            }
            if (teamName.equals(candidate.getTeamName())) {
                return candidate;
            }
            if (teamName.equals(resolveTeamNameByDepartment(candidate.getDepartment()))) {
                return candidate;
            }
        }
        return null;
    }

    private boolean isFinalApproved(RecruitmentRequest request) {
        if (request == null) {
            return false;
        }
        return "3RDAPPROVED".equals(request.getApprovalStatus()) || "APPROVED".equals(request.getApprovalStatus());
    }

    private void sendCompletionNotifications(RecruitmentRequest request) {
        try {
            Set<String> notifiedUserIds = new LinkedHashSet<>();
            if (request.getCreateUserId() != null && !request.getCreateUserId().trim().isEmpty()) {
                createApprovalCompleteMessage(request, request.getCreateUserId(), request.getCreateUserName());
                notifiedUserIds.add(request.getCreateUserId());
            }

            List<User> outsourcingManagers = new ArrayList<>();
            outsourcingManagers.addAll(userMapper.getActiveUsersByRoleName("外包招聘管理岗"));
            outsourcingManagers.addAll(userMapper.getActiveUsersByRoleName("外包招聘岗"));
            outsourcingManagers.addAll(userMapper.getActiveUsersByRoleName("外包招聘管理"));
            for (User manager : outsourcingManagers) {
                if (manager == null || manager.getUserId() == null || notifiedUserIds.contains(manager.getUserId())) {
                    continue;
                }
                createApprovalCompleteMessage(request, manager.getUserId(), manager.getRealName());
                notifiedUserIds.add(manager.getUserId());
            }
        } catch (Exception e) {
            logger.error("审批完成通知发送失败: {}", e.getMessage(), e);
        }
    }

    private void createApprovalCompleteMessage(RecruitmentRequest request, String targetUserId, String targetUserName) {
        Message message = new Message();
        message.setTitle("用人需求审批提醒");
        message.setContent("用人需求审批提醒：" + nullSafe(request.getApplicationDepartment()) + "的用人申请需求已审批完成，请及时查看！");
        message.setType("SYSTEM");
        message.setTargetUserId(targetUserId);
        message.setCreateUserId(SYSTEM_USER_ID);
        message.setCreateUserName(SYSTEM_USER_NAME);
        messageService.createMessage(message);
        logger.info("已发送审批完成通知: targetUserId={}, targetUserName={}", targetUserId, targetUserName);
    }

    private String nullSafe(String value) {
        return value == null ? "-" : value;
    }
}
