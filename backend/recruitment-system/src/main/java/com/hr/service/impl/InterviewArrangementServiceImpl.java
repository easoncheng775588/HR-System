package com.hr.service.impl;

import com.hr.entity.ConfirmInterviewTimeRequest;
import com.hr.entity.InterviewEvaluation;
import com.hr.entity.PendingInterviewResumeVO;
import com.hr.entity.Resume;
import com.hr.entity.ResumeDispatch;
import com.hr.entity.User;
import com.hr.mapper.InterviewEvaluationMapper;
import com.hr.mapper.ResumeDispatchMapper;
import com.hr.mapper.ResumeMapper;
import com.hr.mapper.UserMapper;
import com.hr.service.InterviewArrangementService;
import com.hr.service.InterviewMessageService;
import com.hr.service.InterviewPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class InterviewArrangementServiceImpl implements InterviewArrangementService {

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ResumeMapper resumeMapper;

    @Autowired
    private ResumeDispatchMapper resumeDispatchMapper;

    @Autowired
    private InterviewEvaluationMapper interviewEvaluationMapper;

    @Autowired
    private InterviewPermissionService interviewPermissionService;

    @Autowired
    private InterviewMessageService interviewMessageService;

    @Override
    public List<PendingInterviewResumeVO> getPendingList(String viewerId, String viewerRole) {
        String normalizedViewerId = trimToNull(viewerId);
        if (normalizedViewerId == null) {
            throw new RuntimeException("无权限访问待安排面试列表");
        }
        User viewer = userMapper.getUserById(normalizedViewerId);
        if (viewer == null) {
            throw new RuntimeException("无权限访问待安排面试列表");
        }
        Set<String> roles = enrichInterviewerRoleByDispatch(
            normalizedViewerId,
            interviewPermissionService.normalizeRoles(viewer, viewerRole)
        );
        ensurePendingListAccess(normalizedViewerId, roles);

        List<Resume> resumes = resumeMapper.selectAll();
        if (resumes == null || resumes.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> resumeIds = new ArrayList<>();
        for (Resume resume : resumes) {
            resumeIds.add(resume.getResumeId());
        }
        Map<Long, ResumeDispatch> activeDispatchMap = new LinkedHashMap<>();
        for (ResumeDispatch dispatch : resumeDispatchMapper.selectByResumeIds(resumeIds)) {
            if (!isTrackableDispatchStatus(dispatch.getDispatchStatus())) {
                continue;
            }
            Long resumeId = dispatch.getResumeId();
            if (resumeId == null) {
                continue;
            }
            ResumeDispatch existing = activeDispatchMap.get(resumeId);
            if (existing == null || dispatchPriority(dispatch) > dispatchPriority(existing)) {
                activeDispatchMap.put(resumeId, dispatch);
            }
        }

        Map<Long, String> evaluationStatusMap = buildEvaluationStatusMap();
        List<PendingInterviewResumeVO> result = new ArrayList<>();
        for (Resume resume : resumes) {
            ResumeDispatch activeDispatch = activeDispatchMap.get(resume.getResumeId());
            if (activeDispatch == null) {
                continue;
            }
            if (!canViewPendingResume(normalizedViewerId, roles, resume, activeDispatch)) {
                continue;
            }
            PendingInterviewResumeVO row = new PendingInterviewResumeVO();
            row.setResumeId(resume.getResumeId());
            row.setCandidateName(resume.getCandidateName());
            row.setGender(resume.getGender());
            row.setSupplierName(resume.getSupplierName());
            row.setStatus(resume.getStatus());
            row.setDispatchStatus(trimToEmpty(activeDispatch.getDispatchStatus()));
            row.setWorkYears(trimToEmpty(resume.getItWorkYears()));
            row.setAppliedLevel(trimToEmpty(resume.getAppliedLevel()));
            row.setCandidatePlatform(trimToEmpty(resume.getCandidatePlatform()));
            row.setConfirmedInterviewerId(activeDispatch.getInterviewerId());
            row.setConfirmedInterviewerName(activeDispatch.getInterviewerName());
            row.setInterviewMethod(activeDispatch.getInterviewMethod());
            row.setMeetingNo(activeDispatch.getMeetingNo());
            row.setAvailableStartTime(activeDispatch.getAvailableStartTime());
            row.setAvailableEndTime(activeDispatch.getAvailableEndTime());
            row.setConfirmedInterviewTime(activeDispatch.getConfirmedInterviewTime());
            row.setCanConfirmInterviewTime(canConfirmInterviewTime(normalizedViewerId, roles, activeDispatch));
            row.setCanLaunchEvaluation(canLaunchEvaluation(normalizedViewerId, roles, activeDispatch));
            row.setEvaluationStatus(evaluationStatusMap.getOrDefault(resume.getResumeId(), ""));
            result.add(row);
        }
        return result;
    }

    @Override
    @Transactional
    public void confirmInterviewTime(Long resumeId, ConfirmInterviewTimeRequest request) {
        if (resumeId == null) {
            throw new RuntimeException("简历ID不能为空");
        }
        if (request == null) {
            throw new RuntimeException("面试时间不能为空");
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
        if (!(interviewPermissionService.hasRole(roles, InterviewPermissionService.ROLE_OUTSOURCING_MANAGER)
            || interviewPermissionService.isSuperAdmin(operatorUserId, roles))) {
            throw new RuntimeException("仅外包招聘管理岗可确认面试时间");
        }

        String interviewTimeText = trimToNull(request.getInterviewTime());
        if (interviewTimeText == null) {
            throw new RuntimeException("面试时间不能为空");
        }
        Date interviewTime = parseDateTime(interviewTimeText, "面试时间不能为空");

        ResumeDispatch confirmedDispatch = resumeDispatchMapper.selectConfirmedByResumeId(resumeId);
        if (confirmedDispatch == null) {
            throw new RuntimeException("简历未被面试官确认，不能确认面试时间");
        }
        Date now = new Date();
        resumeDispatchMapper.updateConfirmedInterviewTime(
            confirmedDispatch.getDispatchId(),
            interviewTime,
            operatorUserId,
            defaultValue(request.getOperatorUserName(), operator.getRealName(), "系统用户"),
            now
        );

        Resume resume = resumeMapper.selectByPrimaryKey(resumeId);
        String candidateName = resume == null ? "候选人" : defaultValue(resume.getCandidateName(), "候选人");
        String supplierHrUserId = resume == null ? null : trimToNull(resume.getCreateUserId());
        interviewMessageService.sendInterviewTimeConfirmedMessage(
            candidateName,
            confirmedDispatch.getInterviewerId(),
            supplierHrUserId
        );
    }

    private Map<Long, String> buildEvaluationStatusMap() {
        Map<Long, String> statusMap = new LinkedHashMap<>();
        List<InterviewEvaluation> evaluations = interviewEvaluationMapper.selectAll();
        if (evaluations == null) {
            return statusMap;
        }
        for (InterviewEvaluation evaluation : evaluations) {
            if (evaluation.getResumeId() == null || statusMap.containsKey(evaluation.getResumeId())) {
                continue;
            }
            statusMap.put(evaluation.getResumeId(), defaultValue(evaluation.getApprovalStatus(), ""));
        }
        return statusMap;
    }

    private void ensurePendingListAccess(String viewerId, Set<String> roles) {
        if (interviewPermissionService.isSuperAdmin(viewerId, roles)
            || interviewPermissionService.hasRole(roles, InterviewPermissionService.ROLE_OUTSOURCING_MANAGER)
            || interviewPermissionService.hasRole(roles, InterviewPermissionService.ROLE_INTERVIEWER)
            || interviewPermissionService.hasRole(roles, InterviewPermissionService.ROLE_SUPPLIER_HR)) {
            return;
        }
        throw new RuntimeException("无权限访问待安排面试列表");
    }

    private boolean canViewPendingResume(String viewerId, Set<String> roles, Resume resume, ResumeDispatch activeDispatch) {
        if (interviewPermissionService.isSuperAdmin(viewerId, roles)
            || interviewPermissionService.hasRole(roles, InterviewPermissionService.ROLE_OUTSOURCING_MANAGER)) {
            return true;
        }
        if (interviewPermissionService.hasRole(roles, InterviewPermissionService.ROLE_INTERVIEWER)) {
            return viewerId.equals(trimToNull(activeDispatch.getInterviewerId()));
        }
        if (interviewPermissionService.hasRole(roles, InterviewPermissionService.ROLE_SUPPLIER_HR)) {
            if (!isConfirmedDispatch(activeDispatch.getDispatchStatus())) {
                return false;
            }
            return viewerId.equals(trimToNull(resume.getCreateUserId()));
        }
        return false;
    }

    private boolean canConfirmInterviewTime(String viewerId, Set<String> roles, ResumeDispatch activeDispatch) {
        if (!isConfirmedDispatch(activeDispatch.getDispatchStatus())) {
            return false;
        }
        return interviewPermissionService.isSuperAdmin(viewerId, roles)
            || interviewPermissionService.hasRole(roles, InterviewPermissionService.ROLE_OUTSOURCING_MANAGER);
    }

    private boolean canLaunchEvaluation(String viewerId, Set<String> roles, ResumeDispatch activeDispatch) {
        if (!isConfirmedDispatch(activeDispatch.getDispatchStatus())) {
            return false;
        }
        if (activeDispatch.getConfirmedInterviewTime() == null) {
            return false;
        }
        if (interviewPermissionService.isSuperAdmin(viewerId, roles)) {
            return true;
        }
        if (!interviewPermissionService.hasRole(roles, InterviewPermissionService.ROLE_INTERVIEWER)) {
            return false;
        }
        return viewerId.equals(trimToNull(activeDispatch.getInterviewerId()));
    }

    private Set<String> enrichInterviewerRoleByDispatch(String viewerId, Set<String> roles) {
        Set<String> effectiveRoles = new LinkedHashSet<>();
        if (roles != null) {
            effectiveRoles.addAll(roles);
        }
        if (viewerId == null || interviewPermissionService.hasRole(effectiveRoles, InterviewPermissionService.ROLE_INTERVIEWER)) {
            return effectiveRoles;
        }
        List<ResumeDispatch> assigned = resumeDispatchMapper.selectByInterviewerId(viewerId);
        if (assigned != null && !assigned.isEmpty()) {
            effectiveRoles.add(InterviewPermissionService.ROLE_INTERVIEWER);
        }
        return effectiveRoles;
    }

    private boolean isTrackableDispatchStatus(String status) {
        return "PENDING".equals(trimToEmpty(status)) || "CONFIRMED".equals(trimToEmpty(status));
    }

    private boolean isConfirmedDispatch(String status) {
        return "CONFIRMED".equals(trimToEmpty(status));
    }

    private int dispatchPriority(ResumeDispatch dispatch) {
        String status = trimToEmpty(dispatch.getDispatchStatus());
        if ("CONFIRMED".equals(status)) {
            return 2;
        }
        if ("PENDING".equals(status)) {
            return 1;
        }
        return 0;
    }

    private Date parseDateTime(String value, String errorMessage) {
        try {
            return new SimpleDateFormat(DATE_TIME_PATTERN).parse(value);
        } catch (ParseException e) {
            try {
                return new SimpleDateFormat(DATE_PATTERN).parse(value);
            } catch (ParseException ignored) {
                throw new RuntimeException(errorMessage);
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

    private String trimToEmpty(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? "" : trimmed;
    }

    private String defaultValue(String first, String fallback) {
        if (first != null && !first.trim().isEmpty()) {
            return first.trim();
        }
        return fallback;
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
