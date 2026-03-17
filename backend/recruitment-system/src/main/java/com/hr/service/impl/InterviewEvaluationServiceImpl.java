package com.hr.service.impl;

import com.hr.entity.InterviewEvaluation;
import com.hr.entity.InterviewEvaluationApprovalHistory;
import com.hr.entity.InterviewEvaluationDetailVO;
import com.hr.entity.InterviewEvaluationStatusEnum;
import com.hr.entity.Resume;
import com.hr.entity.ResumeDispatch;
import com.hr.entity.SubmitInterviewEvaluationRequest;
import com.hr.entity.User;
import com.hr.entity.WorkflowApproveRequest;
import com.hr.entity.WorkflowProcessLog;
import com.hr.mapper.InterviewEvaluationApprovalHistoryMapper;
import com.hr.mapper.InterviewEvaluationMapper;
import com.hr.mapper.ResumeDispatchMapper;
import com.hr.mapper.ResumeMapper;
import com.hr.mapper.UserMapper;
import com.hr.mapper.WorkflowProcessLogMapper;
import com.hr.service.EntryManagementService;
import com.hr.service.InterviewEvaluationService;
import com.hr.service.InterviewMessageService;
import com.hr.service.InterviewPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class InterviewEvaluationServiceImpl implements InterviewEvaluationService {

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String PROCESS_CODE = "INTERVIEW_EVALUATION";
    private static final String SYSTEM_USER_ID = "1001";
    private static final String SYSTEM_USER_NAME = "系统用户";

    @Autowired
    private InterviewEvaluationMapper interviewEvaluationMapper;

    @Autowired
    private InterviewEvaluationApprovalHistoryMapper interviewEvaluationApprovalHistoryMapper;

    @Autowired
    private ResumeMapper resumeMapper;

    @Autowired
    private ResumeDispatchMapper resumeDispatchMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private InterviewPermissionService interviewPermissionService;

    @Autowired
    private InterviewMessageService interviewMessageService;

    @Autowired
    private WorkflowProcessLogMapper workflowProcessLogMapper;

    @Autowired
    private EntryManagementService entryManagementService;

    @Override
    @Transactional
    public InterviewEvaluation submit(SubmitInterviewEvaluationRequest request) {
        if (request == null || request.getResumeId() == null) {
            throw new RuntimeException("简历ID不能为空");
        }
        String operatorUserId = trimToNull(request.getOperatorUserId());
        if (operatorUserId == null) {
            throw new RuntimeException("操作人不能为空");
        }
        User operator = userMapper.getUserById(operatorUserId);
        if (operator == null) {
            throw new RuntimeException("操作人不存在");
        }
        Set<String> roles = interviewPermissionService.normalizeRoles(operator, request.getOperatorRole());
        if (!(interviewPermissionService.hasRole(roles, InterviewPermissionService.ROLE_INTERVIEWER)
            || interviewPermissionService.isSuperAdmin(operatorUserId, roles))) {
            throw new RuntimeException("仅面试官可发起面试评价");
        }

        Resume resume = resumeMapper.selectByPrimaryKey(request.getResumeId());
        if (resume == null) {
            throw new RuntimeException("简历不存在");
        }

        ResumeDispatch confirmedDispatch = resumeDispatchMapper.selectConfirmedByResumeId(request.getResumeId());
        if (confirmedDispatch == null) {
            throw new RuntimeException("简历未被面试官确认，不能发起面试评价");
        }
        if (confirmedDispatch.getConfirmedInterviewTime() == null) {
            throw new RuntimeException("面试时间未确认，不能发起面试评价");
        }

        if (!interviewPermissionService.isSuperAdmin(operatorUserId, roles)
            && !operatorUserId.equals(trimToNull(confirmedDispatch.getInterviewerId()))) {
            throw new RuntimeException("仅面试官可发起面试评价");
        }

        InterviewEvaluation latest = interviewEvaluationMapper.selectLatestByResumeId(request.getResumeId());
        if (latest != null
            && !InterviewEvaluationStatusEnum.REJECTED.name().equals(latest.getApprovalStatus())
            && !InterviewEvaluationStatusEnum.APPROVED.name().equals(latest.getApprovalStatus())) {
            throw new RuntimeException("当前简历已有进行中的面试评价流程");
        }

        User interviewer = userMapper.getUserById(confirmedDispatch.getInterviewerId());
        Date now = new Date();
        InterviewEvaluation target = latest != null && InterviewEvaluationStatusEnum.REJECTED.name().equals(latest.getApprovalStatus())
            ? latest
            : new InterviewEvaluation();

        target.setResumeId(resume.getResumeId());
        target.setDispatchId(confirmedDispatch.getDispatchId());
        target.setCandidateName(defaultValue(resume.getCandidateName(), resume.getApplicantName(), "候选人"));
        target.setCandidateGender(defaultValue(resume.getGender(), ""));
        target.setWorkYears(defaultValue(resume.getItWorkYears(), ""));
        target.setAppliedLevel(defaultValue(resume.getAppliedLevel(), ""));
        target.setSupplierName(defaultValue(resume.getSupplierName(), ""));
        target.setInterviewerId(defaultValue(confirmedDispatch.getInterviewerId(), ""));
        target.setInterviewerName(defaultValue(confirmedDispatch.getInterviewerName(), ""));
        target.setInterviewerDepartment(interviewer == null ? "" : defaultValue(interviewer.getDepartment(), ""));
        target.setPlatform(defaultValue(resume.getCandidatePlatform(), ""));
        target.setEntryLevelSuggestion(defaultValue(request.getEntryLevelSuggestion(), ""));
        target.setScore(defaultValue(request.getScore(), ""));
        target.setInterviewMethod(defaultValue(request.getInterviewMethod(), confirmedDispatch.getInterviewMethod(), ""));
        target.setInterviewDate(parseDateTime(defaultValue(request.getInterviewDate(), null), confirmedDispatch.getConfirmedInterviewTime()));
        target.setHireSuggestion(defaultValue(request.getHireSuggestion(), ""));
        target.setAttachmentName(defaultValue(request.getAttachmentName(), ""));
        target.setAttachmentUrl(defaultValue(request.getAttachmentUrl(), ""));
        target.setApprovalStatus(InterviewEvaluationStatusEnum.PENDING_OUTSOURCING.name());
        target.setCurrentApprovalLevel(1);
        target.setUpdateTime(now);
        target.setUpdateUserId(operatorUserId);
        target.setUpdateUserName(defaultValue(request.getOperatorUserName(), operator.getRealName(), SYSTEM_USER_NAME));

        if (target.getEvaluationId() == null) {
            target.setCreateTime(now);
            target.setCreateUserId(operatorUserId);
            target.setCreateUserName(target.getUpdateUserName());
            interviewEvaluationMapper.insert(target);
            logProcess(target.getEvaluationId(), 1, "面试官发起", "SUBMIT", "SUCCESS", operatorUserId, target.getUpdateUserName(), request.getOperatorRole(), "发起面试评价");
        } else {
            interviewEvaluationMapper.updateByPrimaryKey(target);
            logProcess(target.getEvaluationId(), 1, "面试官重提", "RESUBMIT", "SUCCESS", operatorUserId, target.getUpdateUserName(), request.getOperatorRole(), "驳回后重提");
        }
        return interviewEvaluationMapper.selectByPrimaryKey(target.getEvaluationId());
    }

    @Override
    public InterviewEvaluationDetailVO getDetail(Long evaluationId) {
        InterviewEvaluation evaluation = interviewEvaluationMapper.selectByPrimaryKey(evaluationId);
        if (evaluation == null) {
            throw new RuntimeException("面试评价不存在");
        }
        InterviewEvaluationDetailVO detail = new InterviewEvaluationDetailVO();
        detail.setEvaluation(evaluation);
        detail.setApprovalHistory(interviewEvaluationApprovalHistoryMapper.selectByEvaluationId(evaluationId));
        return detail;
    }

    @Override
    @Transactional
    public void approve(Long evaluationId, WorkflowApproveRequest request) {
        InterviewEvaluation evaluation = interviewEvaluationMapper.selectByPrimaryKey(evaluationId);
        if (evaluation == null) {
            throw new RuntimeException("面试评价不存在");
        }
        String approvalStatus = defaultValue(evaluation.getApprovalStatus(), "");
        if (InterviewEvaluationStatusEnum.APPROVED.name().equals(approvalStatus)) {
            throw new RuntimeException("流程已结束，不能重复审批");
        }

        String operatorUserId = trimToNull(request.getApprovalUserId());
        if (operatorUserId == null) {
            throw new RuntimeException("审批人不能为空");
        }
        User operator = userMapper.getUserById(operatorUserId);
        if (operator == null) {
            throw new RuntimeException("审批人不存在");
        }
        Set<String> roles = interviewPermissionService.normalizeRoles(operator, request.getApprovalUserRole());
        ensureApprovalPermission(evaluation, operatorUserId, roles);

        String action = defaultValue(request.getAction(), "").toUpperCase(Locale.ROOT);
        if (!"APPROVE".equals(action) && !"REJECT".equals(action)) {
            throw new RuntimeException("不支持的审批动作");
        }

        Date now = new Date();
        String operatorName = defaultValue(request.getApprovalUserName(), operator.getRealName(), SYSTEM_USER_NAME);
        String operatorRole = defaultValue(request.getApprovalUserRole(), "");
        String approvalComment = defaultValue(request.getApprovalComment(), "");
        Integer approvalLevel = evaluation.getCurrentApprovalLevel() == null ? 1 : evaluation.getCurrentApprovalLevel();

        insertApprovalHistory(evaluation.getEvaluationId(), approvalLevel, operatorUserId, operatorName, operatorRole, action, approvalComment, now);

        if ("APPROVE".equals(action)) {
            if (InterviewEvaluationStatusEnum.PENDING_OUTSOURCING.name().equals(approvalStatus)) {
                evaluation.setApprovalStatus(InterviewEvaluationStatusEnum.PENDING_ROOM_MANAGER.name());
                evaluation.setCurrentApprovalLevel(2);
                logProcess(evaluation.getEvaluationId(), 1, "外包招聘管理岗审批", "APPROVE", "SUCCESS", operatorUserId, operatorName, operatorRole, approvalComment);
                logProcess(evaluation.getEvaluationId(), 2, "流转室经理审批", "TRANSFER", "SUCCESS", operatorUserId, operatorName, operatorRole, "流转到室经理审批");
            } else if (InterviewEvaluationStatusEnum.PENDING_ROOM_MANAGER.name().equals(approvalStatus)) {
                evaluation.setApprovalStatus(InterviewEvaluationStatusEnum.APPROVED.name());
                evaluation.setCurrentApprovalLevel(3);
                logProcess(evaluation.getEvaluationId(), 2, "室经理审批", "APPROVE", "SUCCESS", operatorUserId, operatorName, operatorRole, approvalComment);
                logProcess(evaluation.getEvaluationId(), 3, "流程结束", "FINISH", "SUCCESS", operatorUserId, operatorName, operatorRole, "面试评价审批完成");
                entryManagementService.createFromApprovedEvaluation(evaluation);

                String roomManagerId = operatorUserId;
                Resume relatedResume = evaluation.getResumeId() == null ? null : resumeMapper.selectByPrimaryKey(evaluation.getResumeId());
                interviewMessageService.sendEvaluationCompletedMessage(
                    evaluation.getInterviewerId(),
                    roomManagerId,
                    relatedResume == null ? null : trimToNull(relatedResume.getCreateUserId()),
                    evaluation.getCandidateName()
                );
            } else {
                throw new RuntimeException("当前流程状态不支持审批");
            }
        } else {
            evaluation.setApprovalStatus(InterviewEvaluationStatusEnum.REJECTED.name());
            evaluation.setCurrentApprovalLevel(1);
            logProcess(evaluation.getEvaluationId(), approvalLevel, "审批驳回", "REJECT", "REJECTED", operatorUserId, operatorName, operatorRole, approvalComment);
            interviewMessageService.sendEvaluationRejectedMessage(evaluation.getInterviewerId());
        }

        evaluation.setUpdateTime(now);
        evaluation.setUpdateUserId(operatorUserId);
        evaluation.setUpdateUserName(operatorName);
        interviewEvaluationMapper.updateByPrimaryKey(evaluation);
    }

    @Override
    public List<InterviewEvaluation> getAll() {
        return interviewEvaluationMapper.selectAll();
    }

    @Override
    public List<InterviewEvaluation> getByCreator(String userId) {
        return interviewEvaluationMapper.selectByCreateUserId(userId);
    }

    @Override
    public List<InterviewEvaluation> getByApprover(String userId) {
        return interviewEvaluationMapper.selectByApproverId(userId);
    }

    @Override
    public InterviewEvaluation getById(Long evaluationId) {
        return interviewEvaluationMapper.selectByPrimaryKey(evaluationId);
    }

    private void ensureApprovalPermission(InterviewEvaluation evaluation, String operatorUserId, Set<String> roles) {
        if (interviewPermissionService.isSuperAdmin(operatorUserId, roles)) {
            return;
        }
        String status = defaultValue(evaluation.getApprovalStatus(), "");
        if (InterviewEvaluationStatusEnum.PENDING_OUTSOURCING.name().equals(status)) {
            if (interviewPermissionService.hasRole(roles, InterviewPermissionService.ROLE_OUTSOURCING_MANAGER)) {
                return;
            }
            throw new RuntimeException("当前用户无该节点审批权限");
        }
        if (InterviewEvaluationStatusEnum.PENDING_ROOM_MANAGER.name().equals(status)) {
            if (!interviewPermissionService.hasRole(roles, InterviewPermissionService.ROLE_ROOM_MANAGER)) {
                throw new RuntimeException("当前用户无该节点审批权限");
            }
            User roomManager = resolveRoomManager(evaluation.getInterviewerDepartment());
            if (roomManager != null && operatorUserId.equals(roomManager.getUserId())) {
                return;
            }
            throw new RuntimeException("当前用户无该节点审批权限");
        }
        throw new RuntimeException("流程状态不允许审批");
    }

    private User resolveRoomManager(String department) {
        String dept = normalizeDepartment(department);
        if (dept == null) {
            return null;
        }
        User byRole = userMapper.getActiveRoomManagerByDepartmentAndRoleKeyword(dept, "室经理");
        if (byRole != null) {
            return byRole;
        }
        return userMapper.getActiveRoomManagerByDepartment(dept);
    }

    private String normalizeDepartment(String department) {
        String dept = trimToNull(department);
        if (dept == null) {
            return null;
        }
        int slashIndex = dept.lastIndexOf('/');
        if (slashIndex >= 0 && slashIndex < dept.length() - 1) {
            String leaf = trimToNull(dept.substring(slashIndex + 1));
            if (leaf != null) {
                return leaf;
            }
        }
        return dept;
    }

    private void insertApprovalHistory(Long evaluationId,
                                       Integer approvalLevel,
                                       String approverId,
                                       String approverName,
                                       String approverRole,
                                       String action,
                                       String comment,
                                       Date approvalTime) {
        InterviewEvaluationApprovalHistory history = new InterviewEvaluationApprovalHistory();
        history.setEvaluationId(evaluationId);
        history.setApprovalLevel(approvalLevel);
        history.setApproverId(approverId);
        history.setApproverName(approverName);
        history.setApproverRole(defaultValue(approverRole, ""));
        history.setAction(action);
        history.setComment(comment);
        history.setApprovalTime(approvalTime);
        interviewEvaluationApprovalHistoryMapper.insert(history);
    }

    private void logProcess(Long businessId,
                            Integer nodeOrder,
                            String nodeName,
                            String actionType,
                            String actionResult,
                            String operatorId,
                            String operatorName,
                            String operatorRole,
                            String comment) {
        WorkflowProcessLog log = new WorkflowProcessLog();
        log.setProcessCode(PROCESS_CODE);
        log.setBusinessId(businessId);
        log.setNodeOrder(nodeOrder);
        log.setNodeName(nodeName);
        log.setActionType(actionType);
        log.setActionResult(actionResult);
        log.setOperatorId(defaultValue(operatorId, SYSTEM_USER_ID));
        log.setOperatorName(defaultValue(operatorName, SYSTEM_USER_NAME));
        log.setOperatorRole(defaultValue(operatorRole, ""));
        log.setActionComment(defaultValue(comment, ""));
        log.setActionTime(new Date());
        workflowProcessLogMapper.insert(log);
    }

    private Date parseDateTime(String input, Date fallback) {
        String text = trimToNull(input);
        if (text == null) {
            return fallback;
        }
        try {
            return new SimpleDateFormat(DATE_TIME_PATTERN).parse(text);
        } catch (ParseException e) {
            try {
                return new SimpleDateFormat(DATE_PATTERN).parse(text);
            } catch (ParseException ignored) {
                throw new RuntimeException("面试日期格式不正确");
            }
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String defaultValue(String value, String fallback) {
        String trimmed = trimToNull(value);
        return trimmed == null ? fallback : trimmed;
    }

    private String defaultValue(String first, String second, String fallback) {
        String firstValue = trimToNull(first);
        if (firstValue != null) {
            return firstValue;
        }
        String secondValue = trimToNull(second);
        if (secondValue != null) {
            return secondValue;
        }
        return fallback;
    }
}
