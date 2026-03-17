package com.hr.service.impl;

import com.hr.entity.EntryRecord;
import com.hr.entity.EntryRecordUpdateRequest;
import com.hr.entity.InterviewEvaluation;
import com.hr.entity.OrgUnit;
import com.hr.entity.RecruitmentRequest;
import com.hr.entity.ResumeDispatch;
import com.hr.entity.User;
import com.hr.mapper.EntryRecordMapper;
import com.hr.mapper.OrgUnitMapper;
import com.hr.mapper.RecruitmentRequestMapper;
import com.hr.mapper.ResumeDispatchMapper;
import com.hr.mapper.UserMapper;
import com.hr.service.EntryManagementService;
import com.hr.service.InterviewPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class EntryManagementServiceImpl implements EntryManagementService {

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String STATUS_ACCEPT = "接受入场";
    private static final String STATUS_GIVE_UP = "放弃入场";
    private static final String SYSTEM_USER_ID = "1001";
    private static final String SYSTEM_USER_NAME = "系统用户";

    @Autowired
    private EntryRecordMapper entryRecordMapper;

    @Autowired
    private ResumeDispatchMapper resumeDispatchMapper;

    @Autowired
    private RecruitmentRequestMapper recruitmentRequestMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrgUnitMapper orgUnitMapper;

    @Autowired
    private InterviewPermissionService interviewPermissionService;

    @Override
    @Transactional
    public void createFromApprovedEvaluation(InterviewEvaluation evaluation) {
        if (evaluation == null || evaluation.getEvaluationId() == null) {
            return;
        }
        if (entryRecordMapper.selectByEvaluationId(evaluation.getEvaluationId()) != null) {
            return;
        }

        ResumeDispatch confirmedDispatch = resumeDispatchMapper.selectConfirmedByResumeId(evaluation.getResumeId());
        if (confirmedDispatch == null || confirmedDispatch.getRecruitmentRequestId() == null) {
            throw new RuntimeException("面试评价缺少关联用人申请，无法生成入场记录");
        }

        RecruitmentRequest request = recruitmentRequestMapper.selectByPrimaryKey(confirmedDispatch.getRecruitmentRequestId());
        if (request == null) {
            throw new RuntimeException("关联用人申请不存在，无法生成入场记录");
        }

        Date now = new Date();
        EntryRecord entryRecord = new EntryRecord();
        entryRecord.setEvaluationId(evaluation.getEvaluationId());
        entryRecord.setResumeId(evaluation.getResumeId());
        entryRecord.setSourceRecruitmentRequestId(request.getRecruitmentRequestId());
        entryRecord.setCandidateName(defaultValue(evaluation.getCandidateName(), "候选人"));
        entryRecord.setInterviewTime(evaluation.getInterviewDate());
        entryRecord.setHiredDepartment(resolveHiredDepartment(request, evaluation));
        entryRecord.setPositionLevel(defaultValue(evaluation.getEntryLevelSuggestion(), evaluation.getAppliedLevel(), ""));
        entryRecord.setTechnicalPlatform(defaultValue(evaluation.getPlatform(), ""));
        entryRecord.setEntryStatus("");
        entryRecord.setArrivalStatus("");
        entryRecord.setCreateTime(now);
        entryRecord.setCreateUserId(defaultValue(evaluation.getUpdateUserId(), SYSTEM_USER_ID));
        entryRecord.setCreateUserName(defaultValue(evaluation.getUpdateUserName(), SYSTEM_USER_NAME));
        entryRecord.setUpdateTime(now);
        entryRecord.setUpdateUserId(entryRecord.getCreateUserId());
        entryRecord.setUpdateUserName(entryRecord.getCreateUserName());
        entryRecordMapper.insert(entryRecord);
    }

    @Override
    public List<EntryRecord> getEntryList(String viewerId, String viewerRole) {
        if (trimToNull(viewerId) == null) {
            throw new RuntimeException("查看人不能为空");
        }
        User viewer = userMapper.getUserById(viewerId);
        if (viewer == null) {
            throw new RuntimeException("查看人不存在");
        }
        Set<String> roles = interviewPermissionService.normalizeRoles(viewer, viewerRole);
        ensureListAccess(viewerId, roles);

        List<EntryRecord> result = new ArrayList<>();
        for (EntryRecord entryRecord : entryRecordMapper.selectAll()) {
            if (entryRecord == null) {
                continue;
            }
            if (canViewEntry(entryRecord, viewer, viewerId, roles)) {
                result.add(entryRecord);
            }
        }
        return result;
    }

    @Override
    @Transactional
    public EntryRecord updateEntry(Long entryRecordId, EntryRecordUpdateRequest request) {
        if (entryRecordId == null) {
            throw new RuntimeException("入场记录ID不能为空");
        }
        if (request == null) {
            throw new RuntimeException("更新参数不能为空");
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
        ensureListAccess(operatorUserId, roles);

        EntryRecord entryRecord = entryRecordMapper.selectByPrimaryKey(entryRecordId);
        if (entryRecord == null) {
            throw new RuntimeException("入场记录不存在");
        }
        if (!canViewEntry(entryRecord, operator, operatorUserId, roles)) {
            throw new RuntimeException("无权限编辑该入场记录");
        }

        entryRecord.setEntryStatus(normalizeEntryStatus(request.getEntryStatus()));
        entryRecord.setPlannedEntryDate(parseOptionalDate(request.getPlannedEntryDate()));
        entryRecord.setActualEntryDate(parseOptionalDate(request.getActualEntryDate()));
        entryRecord.setArrivalStatus(defaultValue(trimToNull(request.getArrivalStatus()), ""));
        entryRecord.setUpdateTime(new Date());
        entryRecord.setUpdateUserId(operatorUserId);
        entryRecord.setUpdateUserName(defaultValue(request.getOperatorUserName(), operator.getRealName(), SYSTEM_USER_NAME));
        entryRecordMapper.updateByPrimaryKey(entryRecord);
        return entryRecordMapper.selectByPrimaryKey(entryRecordId);
    }

    private boolean canViewEntry(EntryRecord entryRecord, User viewer, String viewerId, Set<String> roles) {
        if (interviewPermissionService.isSuperAdmin(viewerId, roles)
            || interviewPermissionService.hasRole(roles, InterviewPermissionService.ROLE_OUTSOURCING_MANAGER)) {
            return true;
        }

        RecruitmentRequest request = entryRecord.getSourceRecruitmentRequestId() == null
            ? null
            : recruitmentRequestMapper.selectByPrimaryKey(entryRecord.getSourceRecruitmentRequestId());
        if (request == null) {
            return false;
        }

        if (interviewPermissionService.hasRole(roles, InterviewPermissionService.ROLE_ROOM_MANAGER)) {
            return viewerId.equals(trimToNull(request.getCreateUserId()));
        }

        if (interviewPermissionService.hasRole(roles, InterviewPermissionService.ROLE_TEAM_MANAGER)) {
            if (viewerId.equals(trimToNull(request.getCreateUserId()))) {
                return true;
            }
            String requestTeam = resolveRequestTeam(request);
            String viewerTeam = resolveUserTeam(viewer);
            return requestTeam != null && requestTeam.equals(viewerTeam);
        }
        return false;
    }

    private void ensureListAccess(String viewerId, Set<String> roles) {
        if (interviewPermissionService.isSuperAdmin(viewerId, roles)) {
            return;
        }
        if (interviewPermissionService.hasRole(roles, InterviewPermissionService.ROLE_OUTSOURCING_MANAGER)
            || interviewPermissionService.hasRole(roles, InterviewPermissionService.ROLE_ROOM_MANAGER)
            || interviewPermissionService.hasRole(roles, InterviewPermissionService.ROLE_TEAM_MANAGER)) {
            return;
        }
        throw new RuntimeException("无权限访问入场管理");
    }

    private String resolveHiredDepartment(RecruitmentRequest request, InterviewEvaluation evaluation) {
        String orgUnit = trimToNull(request.getOrgUnitName());
        if (orgUnit != null) {
            return orgUnit;
        }
        String applicationDepartment = trimToNull(request.getApplicationDepartment());
        if (applicationDepartment != null) {
            return applicationDepartment;
        }
        String interviewerDepartment = trimToNull(evaluation.getInterviewerDepartment());
        if (interviewerDepartment != null) {
            return interviewerDepartment;
        }
        return defaultValue(trimToNull(request.getTeam()), "");
    }

    private String resolveRequestTeam(RecruitmentRequest request) {
        String team = trimToNull(request.getTeam());
        if (team != null) {
            return team;
        }
        String orgUnit = trimToNull(request.getOrgUnitName());
        if (orgUnit != null) {
            return resolveTeamName(orgUnit);
        }
        String applicationDepartment = trimToNull(request.getApplicationDepartment());
        if (applicationDepartment != null) {
            return resolveTeamName(applicationDepartment);
        }
        return null;
    }

    private String resolveUserTeam(User user) {
        String team = trimToNull(user.getTeamName());
        if (team != null) {
            return team;
        }
        return resolveTeamName(user.getDepartment());
    }

    private String resolveTeamName(String departmentOrUnit) {
        String value = trimToNull(departmentOrUnit);
        if (value == null) {
            return null;
        }
        if (value.contains("/")) {
            String[] parts = value.split("/");
            if (parts.length > 0) {
                String first = trimToNull(parts[0]);
                if (first != null) {
                    return first;
                }
            }
        }
        OrgUnit unit = orgUnitMapper.getByUnitName(value);
        if (unit == null) {
            return value;
        }
        String parent = trimToNull(unit.getParentUnitName());
        return parent == null ? value : parent;
    }

    private String normalizeEntryStatus(String value) {
        String status = trimToNull(value);
        if (status == null) {
            return "";
        }
        if (STATUS_ACCEPT.equals(status) || STATUS_GIVE_UP.equals(status)) {
            return status;
        }
        throw new RuntimeException("入场状态不支持");
    }

    private Date parseOptionalDate(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        try {
            if (trimmed.length() <= 10) {
                return new SimpleDateFormat(DATE_PATTERN, Locale.ROOT).parse(trimmed);
            }
            return new SimpleDateFormat(DATE_TIME_PATTERN, Locale.ROOT).parse(trimmed);
        } catch (ParseException e) {
            throw new RuntimeException("日期格式不正确");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String defaultValue(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            String trimmed = trimToNull(value);
            if (trimmed != null) {
                return trimmed;
            }
        }
        return "";
    }
}
