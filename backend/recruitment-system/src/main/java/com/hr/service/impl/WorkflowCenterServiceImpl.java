package com.hr.service.impl;

import com.hr.entity.ApprovalHistory;
import com.hr.entity.OrgUnit;
import com.hr.entity.RecruitmentRequest;
import com.hr.entity.User;
import com.hr.entity.WorkflowApprovalActionRequest;
import com.hr.entity.WorkflowDetail;
import com.hr.entity.WorkflowInitiatedItem;
import com.hr.entity.WorkflowNodeConfig;
import com.hr.entity.WorkflowProcessLog;
import com.hr.entity.WorkflowProcessedItem;
import com.hr.entity.WorkflowTodoItem;
import com.hr.mapper.ApprovalHistoryMapper;
import com.hr.mapper.OrgUnitMapper;
import com.hr.mapper.RecruitmentRequestMapper;
import com.hr.mapper.UserMapper;
import com.hr.mapper.WorkflowNodeConfigMapper;
import com.hr.mapper.WorkflowProcessLogMapper;
import com.hr.service.RecruitmentRequestService;
import com.hr.service.WorkflowCenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class WorkflowCenterServiceImpl implements WorkflowCenterService {

    private static final String PROCESS_CODE = "RECRUITMENT_REQUEST";
    private static final String PROCESS_NAME = "用人申请流程";
    private static final String DIRECT_TEAM_MANAGER_TYPE = "DIRECT_TEAM_MANAGER";

    @Autowired
    private RecruitmentRequestMapper recruitmentRequestMapper;

    @Autowired
    private ApprovalHistoryMapper approvalHistoryMapper;

    @Autowired
    private OrgUnitMapper orgUnitMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkflowNodeConfigMapper workflowNodeConfigMapper;

    @Autowired
    private WorkflowProcessLogMapper workflowProcessLogMapper;

    @Autowired
    private RecruitmentRequestService recruitmentRequestService;

    @Override
    public List<WorkflowTodoItem> getMyTodo(String userId, String userRole) {
        List<RecruitmentRequest> requests = recruitmentRequestMapper.selectAll();
        Map<Integer, WorkflowNodeConfig> nodeConfigMap = getNodeConfigMap();
        String normalizedRole = normalizeRole(userRole);
        boolean admin = isAdminRole(normalizedRole);

        List<WorkflowTodoItem> result = new ArrayList<>();
        for (RecruitmentRequest req : requests) {
            if (!isPending(req)) {
                continue;
            }

            Integer level = req.getCurrentApprovalLevel();
            if (level == null || level <= 0) {
                continue;
            }

            WorkflowNodeConfig nodeConfig = nodeConfigMap.get(level);
            if (nodeConfig == null) {
                continue;
            }

            String approverRole = resolveApproverRole(req, nodeConfig);
            if (!admin && !roleMatch(normalizedRole, approverRole)) {
                continue;
            }

            if (level == 3 && !canLevelThreeHandle(userId, normalizedRole, req)) {
                continue;
            }

            WorkflowTodoItem item = new WorkflowTodoItem();
            item.setRequestId(req.getRecruitmentRequestId());
            item.setProcessName(PROCESS_NAME);
            item.setSummary(buildSummary(req));
            item.setCurrentNode(resolveNodeName(req, nodeConfig));
            item.setApplicant(req.getCreateUserName());
            item.setApplicantDept(req.getTeam());
            item.setArriveTime(resolveArriveTime(req));
            result.add(item);
        }

        result.sort(Comparator.comparing(WorkflowTodoItem::getArriveTime, Comparator.nullsLast(Date::compareTo)).reversed());
        return result;
    }

    @Override
    public List<WorkflowInitiatedItem> getMyInitiated(String userId) {
        List<RecruitmentRequest> requests = recruitmentRequestMapper.selectAll();
        Map<Integer, WorkflowNodeConfig> nodeConfigMap = getNodeConfigMap();
        return requests.stream()
            .filter(req -> Objects.equals(String.valueOf(req.getCreateUserId()), String.valueOf(userId)))
            .sorted(Comparator.comparing(RecruitmentRequest::getCreateTime, Comparator.nullsLast(Date::compareTo)).reversed())
            .map(req -> {
                WorkflowInitiatedItem item = new WorkflowInitiatedItem();
                item.setRequestId(req.getRecruitmentRequestId());
                item.setProcessName(PROCESS_NAME);
                item.setSummary(buildSummary(req));
                item.setCurrentNode(resolveCurrentNode(req, nodeConfigMap));
                item.setApplicantDept(req.getTeam());
                item.setProcessStatus(resolveProcessStatus(req.getApprovalStatus()));
                item.setStartTime(req.getCreateTime());
                return item;
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<WorkflowProcessedItem> getMyProcessed(String userId) {
        List<ApprovalHistory> myHistory = approvalHistoryMapper.selectByApproverId(userId);
        List<WorkflowProcessedItem> result = new ArrayList<>();
        for (ApprovalHistory history : myHistory) {
            RecruitmentRequest req = recruitmentRequestMapper.selectByPrimaryKey(history.getRecruitmentRequestId());
            if (req == null) {
                continue;
            }

            WorkflowProcessedItem item = new WorkflowProcessedItem();
            item.setRequestId(req.getRecruitmentRequestId());
            item.setProcessName(PROCESS_NAME);
            item.setSummary(buildSummary(req));
            item.setApplicant(req.getCreateUserName());
            item.setApplicantDept(req.getTeam());
            item.setApplyTime(req.getCreateTime());
            item.setHandleTime(history.getApprovalTime());
            result.add(item);
        }

        result.sort(Comparator.comparing(WorkflowProcessedItem::getHandleTime, Comparator.nullsLast(Date::compareTo)).reversed());
        return result;
    }

    @Override
    public WorkflowDetail getWorkflowDetail(Long requestId, String viewerId, String viewerName, String viewerRole) {
        RecruitmentRequest request = recruitmentRequestMapper.selectByPrimaryKey(requestId);
        if (request == null) {
            throw new RuntimeException("流程不存在");
        }

        if (viewerId != null && !viewerId.trim().isEmpty()) {
            logAction(
                requestId,
                request.getCurrentApprovalLevel(),
                resolveCurrentNode(request, getNodeConfigMap()),
                "VIEW",
                "SUCCESS",
                viewerId,
                viewerName,
                viewerRole,
                "查看流程详情"
            );
        }

        WorkflowDetail detail = new WorkflowDetail();
        detail.setRequest(request);
        detail.setApprovalHistory(approvalHistoryMapper.selectByRecruitmentRequestId(requestId));
        detail.setNodeConfigs(workflowNodeConfigMapper.selectByProcessCode(PROCESS_CODE));
        detail.setProcessLogs(workflowProcessLogMapper.selectByBusinessId(PROCESS_CODE, requestId));
        return detail;
    }

    @Override
    @Transactional
    public void approve(Long requestId, WorkflowApprovalActionRequest actionRequest) {
        RecruitmentRequest existing = recruitmentRequestMapper.selectByPrimaryKey(requestId);
        if (existing == null) {
            throw new RuntimeException("流程不存在");
        }
        if (isFinished(existing)) {
            throw new RuntimeException("流程已结束，不能重复审批");
        }

        Map<Integer, WorkflowNodeConfig> nodeMap = getNodeConfigMap();
        Integer currentLevel = existing.getCurrentApprovalLevel();
        WorkflowNodeConfig currentNode = nodeMap.get(currentLevel);
        if (currentNode == null) {
            throw new RuntimeException("当前流程节点配置缺失");
        }

        String operatorRole = normalizeRole(actionRequest.getApprovalUserRole());
        String approverRole = resolveApproverRole(existing, currentNode);
        if (!isAdminRole(operatorRole) && !roleMatch(operatorRole, approverRole)) {
            throw new RuntimeException("当前用户无该节点审批权限");
        }
        if (currentLevel == 3 && !canLevelThreeHandle(actionRequest.getApprovalUserId(), operatorRole, existing)) {
            throw new RuntimeException("当前用户无该节点审批权限");
        }

        String action = actionRequest.getAction() == null ? "" : actionRequest.getAction().trim().toUpperCase(Locale.ROOT);
        Map<String, Object> params = new HashMap<>();
        params.put("approvalUserId", actionRequest.getApprovalUserId());
        params.put("approvalUserName", actionRequest.getApprovalUserName());
        params.put("approvalComment", actionRequest.getApprovalComment());

        if ("APPROVE".equals(action)) {
            recruitmentRequestService.threeLevelApproveRequest(requestId, params);
            RecruitmentRequest updated = recruitmentRequestMapper.selectByPrimaryKey(requestId);

            logAction(
                requestId,
                currentLevel,
                currentNode.getNodeName(),
                "APPROVE",
                "SUCCESS",
                actionRequest.getApprovalUserId(),
                actionRequest.getApprovalUserName(),
                actionRequest.getApprovalUserRole(),
                actionRequest.getApprovalComment()
            );

            if (isFinished(updated)) {
                logAction(
                    requestId,
                    updated.getCurrentApprovalLevel(),
                    "流程结束",
                    "FINISH",
                    "SUCCESS",
                    actionRequest.getApprovalUserId(),
                    actionRequest.getApprovalUserName(),
                    actionRequest.getApprovalUserRole(),
                    "流程审批完成"
                );
            } else {
                logAction(
                    requestId,
                    updated.getCurrentApprovalLevel(),
                    resolveCurrentNode(updated, nodeMap),
                    "TRANSFER",
                    "SUCCESS",
                    actionRequest.getApprovalUserId(),
                    actionRequest.getApprovalUserName(),
                    actionRequest.getApprovalUserRole(),
                    "流转到下一环节"
                );
            }
            return;
        }

        if ("REJECT".equals(action)) {
            recruitmentRequestService.threeLevelRejectRequest(requestId, params);
            logAction(
                requestId,
                currentLevel,
                currentNode.getNodeName(),
                "REJECT",
                "SUCCESS",
                actionRequest.getApprovalUserId(),
                actionRequest.getApprovalUserName(),
                actionRequest.getApprovalUserRole(),
                actionRequest.getApprovalComment()
            );
            logAction(
                requestId,
                currentLevel,
                "流程结束",
                "FINISH",
                "REJECTED",
                actionRequest.getApprovalUserId(),
                actionRequest.getApprovalUserName(),
                actionRequest.getApprovalUserRole(),
                "流程已拒绝结束"
            );
            return;
        }

        throw new RuntimeException("不支持的审批动作");
    }

    private Map<Integer, WorkflowNodeConfig> getNodeConfigMap() {
        return workflowNodeConfigMapper.selectByProcessCode(PROCESS_CODE)
            .stream()
            .collect(Collectors.toMap(WorkflowNodeConfig::getNodeOrder, v -> v, (a, b) -> a));
    }

    private boolean isPending(RecruitmentRequest req) {
        String status = req.getApprovalStatus();
        return "PENDING".equals(status) || "1STAPPROVED".equals(status) || "2NDAPPROVED".equals(status);
    }

    private boolean isFinished(RecruitmentRequest req) {
        if (req.getCurrentApprovalLevel() != null && req.getCurrentApprovalLevel() >= 4) {
            return true;
        }
        String status = req.getApprovalStatus();
        return "REJECTED".equals(status) || "3RDAPPROVED".equals(status) || "APPROVED".equals(status);
    }

    private String resolveCurrentNode(RecruitmentRequest req, Map<Integer, WorkflowNodeConfig> nodeMap) {
        if (isFinished(req)) {
            return "流程结束";
        }
        Integer currentLevel = req.getCurrentApprovalLevel();
        WorkflowNodeConfig cfg = currentLevel == null ? null : nodeMap.get(currentLevel);
        return cfg == null ? "-" : resolveNodeName(req, cfg);
    }

    private String resolveProcessStatus(String approvalStatus) {
        if ("REJECTED".equals(approvalStatus)) {
            return "已拒绝";
        }
        if ("3RDAPPROVED".equals(approvalStatus) || "APPROVED".equals(approvalStatus)) {
            return "已通过";
        }
        return "处理中";
    }

    private Date resolveArriveTime(RecruitmentRequest req) {
        Integer level = req.getCurrentApprovalLevel();
        if (level == null || level <= 1) {
            return req.getCreateTime();
        }
        if (level == 2) {
            return req.getApprovalLevel1Time() == null ? req.getCreateTime() : req.getApprovalLevel1Time();
        }
        if (level == 3) {
            return req.getApprovalLevel2Time() == null ? req.getCreateTime() : req.getApprovalLevel2Time();
        }
        return req.getUpdateTime();
    }

    private String buildSummary(RecruitmentRequest req) {
        return "岗位:" + nullSafe(req.getRequestTitle()) + " | 申请部门:" + nullSafe(req.getTeam()) + " | 补充:" + (req.getSupplementCount() == null ? 0 : req.getSupplementCount());
    }

    private String nullSafe(String text) {
        return text == null ? "-" : text;
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "";
        }
        String value = role.trim();
        if (value.contains("外包招聘")) {
            return "外包招聘管理岗";
        }
        if (value.contains("编制管理")) {
            return "编制管理岗";
        }
        if (value.contains("直属团队经理")) {
            return "直属团队经理";
        }
        if (value.contains("团队经理")) {
            return "团队经理";
        }
        if (value.contains("分管总")) {
            return "分管总";
        }
        if (value.contains("管理员")) {
            return "管理员";
        }
        return value;
    }

    private boolean isAdminRole(String normalizedRole) {
        return "管理员".equals(normalizedRole) || "系统管理员".equals(normalizedRole);
    }

    private boolean roleMatch(String roleA, String roleB) {
        return Objects.equals(roleA, roleB);
    }

    private boolean canTeamManagerHandle(String userId, RecruitmentRequest request) {
        if (userId == null || request == null) {
            return false;
        }
        if ("1001".equals(userId)) {
            return true;
        }

        String teamName = resolveTeamNameByDepartment(request.getTeam());
        if (teamName == null || teamName.trim().isEmpty()) {
            return false;
        }

        User manager = findTeamManager(teamName);
        return manager != null && userId.equals(manager.getUserId());
    }

    private boolean canLevelThreeHandle(String userId, String normalizedRole, RecruitmentRequest request) {
        if (userId == null || request == null) {
            return false;
        }
        if (DIRECT_TEAM_MANAGER_TYPE.equals(request.getSubmitterRoleType())) {
            return "分管总".equals(normalizedRole) && userId.equals(request.getFinalApproverUserId());
        }
        return "团队经理".equals(normalizedRole) && canTeamManagerHandle(userId, request);
    }

    private String resolveApproverRole(RecruitmentRequest request, WorkflowNodeConfig nodeConfig) {
        if (nodeConfig == null) {
            return "";
        }
        if (nodeConfig.getNodeOrder() != null && nodeConfig.getNodeOrder() == 3 && DIRECT_TEAM_MANAGER_TYPE.equals(request.getSubmitterRoleType())) {
            return "分管总";
        }
        return normalizeRole(nodeConfig.getApproverRole());
    }

    private String resolveNodeName(RecruitmentRequest request, WorkflowNodeConfig nodeConfig) {
        if (nodeConfig == null) {
            return "-";
        }
        if (nodeConfig.getNodeOrder() != null && nodeConfig.getNodeOrder() == 3 && DIRECT_TEAM_MANAGER_TYPE.equals(request.getSubmitterRoleType())) {
            return "分管总审批";
        }
        return nodeConfig.getNodeName();
    }

    private String resolveTeamNameByDepartment(String department) {
        if (department == null || department.trim().isEmpty()) {
            return "";
        }
        OrgUnit unit = orgUnitMapper.getByUnitName(department);
        if (unit == null) {
            return department;
        }
        if (unit.getParentUnitName() == null || unit.getParentUnitName().trim().isEmpty()) {
            return unit.getUnitName();
        }
        return unit.getParentUnitName();
    }

    private User findTeamManager(String teamName) {
        if (teamName == null || teamName.trim().isEmpty()) {
            return null;
        }

        User manager = userMapper.getActiveTeamManagerByDepartment(teamName);
        if (manager != null) {
            return manager;
        }

        List<User> managers = userMapper.getActiveUsersByRoleName("团队经理");
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

    private void logAction(Long requestId, Integer nodeOrder, String nodeName, String actionType, String actionResult,
                           String operatorId, String operatorName, String operatorRole, String comment) {
        WorkflowProcessLog log = new WorkflowProcessLog();
        log.setProcessCode(PROCESS_CODE);
        log.setBusinessId(requestId);
        log.setNodeOrder(nodeOrder);
        log.setNodeName(nodeName);
        log.setActionType(actionType);
        log.setActionResult(actionResult);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setOperatorRole(operatorRole);
        log.setActionComment(comment);
        log.setActionTime(new Date());
        workflowProcessLogMapper.insert(log);
    }
}
