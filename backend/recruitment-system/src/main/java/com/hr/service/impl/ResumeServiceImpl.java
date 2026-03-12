package com.hr.service.impl;

import com.hr.entity.DemandRequirement;
import com.hr.entity.RecruitmentRequest;
import com.hr.entity.Resume;
import com.hr.entity.ResumeDispatch;
import com.hr.entity.User;
import com.hr.mapper.DemandRequirementMapper;
import com.hr.mapper.RecruitmentRequestMapper;
import com.hr.mapper.ResumeDispatchMapper;
import com.hr.mapper.ResumeMapper;
import com.hr.mapper.UserMapper;
import com.hr.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ResumeServiceImpl implements ResumeService {

    private static final String SYSTEM_USER_ID = "1001";
    private static final String SYSTEM_USER_NAME = "系统用户";

    private static final String ROLE_SUPPLIER_HR = "供应商HR";
    private static final String ROLE_OUTSOURCING_MANAGER = "外包招聘管理岗";
    private static final String ROLE_INTERVIEWER = "面试官";
    private static final String ROLE_ROOM_MANAGER = "室经理";
    private static final String ROLE_TEAM_MANAGER = "团队经理";
    private static final String ROLE_SUPER_ADMIN = "超级管理员";

    private static final String STATUS_PENDING_SCREEN = "待简历初筛";
    private static final String STATUS_SCREEN_PASS = "已通过简历初筛";
    private static final String STATUS_SCREEN_EDIT = "需修改简历材料";
    private static final String STATUS_SCREEN_REJECT = "未通过简历初筛";

    @Autowired
    private ResumeMapper resumeMapper;

    @Autowired
    private ResumeDispatchMapper resumeDispatchMapper;

    @Autowired
    private DemandRequirementMapper demandRequirementMapper;

    @Autowired
    private RecruitmentRequestMapper recruitmentRequestMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Resume saveResume(Resume resume) {
        if (resume.getCandidateName() == null || resume.getCandidateName().trim().isEmpty()) {
            throw new IllegalArgumentException("候选人不能为空");
        }
        if (resume.getRelatedRequestIds() == null || resume.getRelatedRequestIds().trim().isEmpty()) {
            throw new IllegalArgumentException("关联需求不能为空");
        }

        boolean isCreate = resume.getResumeId() == null;
        String operatorUserId = trimToNull(isCreate ? resume.getCreateUserId() : resume.getUpdateUserId());
        if (operatorUserId == null) {
            throw new RuntimeException(isCreate ? "提交人不能为空" : "更新人不能为空");
        }
        User operator = userMapper.getUserById(operatorUserId);
        if (operator == null) {
            throw new RuntimeException(isCreate ? "提交人不存在" : "更新人不存在");
        }
        Set<String> roles = normalizeRoles(operator, null);

        Resume existing = null;
        if (isCreate) {
            ensureSubmitAccess(operatorUserId, roles);
        } else {
            existing = resumeMapper.selectByPrimaryKey(resume.getResumeId());
            if (existing == null) {
                throw new RuntimeException("简历不存在");
            }
            ensureEditAccess(existing, operatorUserId, roles);
            resume.setCreateTime(existing.getCreateTime());
            resume.setCreateUserId(existing.getCreateUserId());
            resume.setCreateUserName(existing.getCreateUserName());
            if (trimToNull(resume.getStatus()) == null) {
                resume.setStatus(existing.getStatus());
            }
        }

        if (trimToNull(resume.getStatus()) == null) {
            resume.setStatus(STATUS_PENDING_SCREEN);
        }
        resume.setStatus(normalizeStatus(resume.getStatus()));

        if (trimToNull(resume.getCreateUserId()) == null) {
            resume.setCreateUserId(operatorUserId);
        }
        if (trimToNull(resume.getCreateUserName()) == null) {
            resume.setCreateUserName(defaultValue(operator.getRealName(), SYSTEM_USER_NAME));
        }
        if (trimToNull(resume.getUpdateUserId()) == null) {
            resume.setUpdateUserId(operatorUserId);
        }
        if (trimToNull(resume.getUpdateUserName()) == null) {
            resume.setUpdateUserName(defaultValue(operator.getRealName(), SYSTEM_USER_NAME));
        }

        if (trimToNull(resume.getApplicantName()) == null) {
            resume.setApplicantName(resume.getCandidateName());
        }
        if (trimToNull(resume.getJobTitle()) == null) {
            resume.setJobTitle(resume.getAppliedCategory());
        }
        if (trimToNull(resume.getEducation()) == null) {
            resume.setEducation(resume.getHighestDegree());
        }

        if (trimToNull(resume.getSupplierName()) == null) {
            String supplierName = resumeMapper.selectSupplierNameByUserId(resume.getCreateUserId());
            resume.setSupplierName(supplierName == null ? "" : supplierName);
        }
        if (resume.getSupplierRecommendDate() == null) {
            resume.setSupplierRecommendDate(new Date());
        }

        if (isCreate) {
            resumeMapper.insert(resume);
        } else {
            resumeMapper.updateByPrimaryKey(resume);
        }
        return resumeMapper.selectByPrimaryKey(resume.getResumeId());
    }

    @Override
    public Resume getById(Long id) {
        Resume resume = resumeMapper.selectByPrimaryKey(id);
        if (resume != null) {
            resume.setStatus(normalizeStatus(resume.getStatus()));
        }
        return resume;
    }

    @Override
    public List<Resume> getAll(String viewerId, String viewerRole) {
        if (trimToNull(viewerId) == null) {
            throw new RuntimeException("无权限访问简历管理");
        }

        User viewer = userMapper.getUserById(viewerId);
        if (viewer == null) {
            throw new RuntimeException("无权限访问简历管理");
        }
        Set<String> roles = normalizeRoles(viewer, viewerRole);
        ensureListAccess(viewerId, roles);

        List<Resume> resumes = resumeMapper.selectAll();
        if (resumes.isEmpty()) {
            return resumes;
        }

        Map<Long, List<ResumeDispatch>> dispatchByResumeId = buildDispatchMap(resumes);

        List<Resume> filtered = new ArrayList<>();
        for (Resume resume : resumes) {
            resume.setStatus(normalizeStatus(resume.getStatus()));
            List<ResumeDispatch> dispatches = dispatchByResumeId.getOrDefault(resume.getResumeId(), new ArrayList<>());
            if (!canViewResume(resume, dispatches, viewer, viewerId, roles)) {
                continue;
            }
            enrichChoiceFields(resume, dispatches, viewerId, roles);
            filtered.add(resume);
        }
        return filtered;
    }

    @Override
    public List<Resume> getByRecruitmentRequestId(Long recruitmentRequestId) {
        List<Resume> resumes = resumeMapper.selectByRecruitmentRequestId(recruitmentRequestId);
        for (Resume resume : resumes) {
            resume.setStatus(normalizeStatus(resume.getStatus()));
        }
        return resumes;
    }

    @Override
    public List<Resume> getByStatus(String status) {
        return resumeMapper.selectByStatus(status);
    }

    @Override
    public Resume updateStatus(Long id, String status) {
        Resume resume = resumeMapper.selectByPrimaryKey(id);
        if (resume == null) {
            throw new RuntimeException("简历不存在");
        }
        resume.setStatus(normalizeStatus(status));
        resume.setUpdateUserId(SYSTEM_USER_ID);
        resume.setUpdateUserName(SYSTEM_USER_NAME);
        resumeMapper.updateByPrimaryKey(resume);
        return resumeMapper.selectByPrimaryKey(id);
    }

    @Override
    public int deleteById(Long id, String operatorUserId, String operatorRole) {
        Resume resume = resumeMapper.selectByPrimaryKey(id);
        if (resume == null) {
            return 0;
        }

        String opId = trimToNull(operatorUserId);
        if (opId == null) {
            throw new RuntimeException("操作人不能为空");
        }
        User operator = userMapper.getUserById(opId);
        if (operator == null) {
            throw new RuntimeException("操作人不存在");
        }
        Set<String> roles = normalizeRoles(operator, operatorRole);
        ensureDeleteAccess(resume, opId, roles);

        resumeDispatchMapper.deleteByResumeId(id);
        return resumeMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<Map<String, Object>> getRequirementOptions() {
        List<Map<String, Object>> dbOptions = resumeMapper.selectRequirementOptions();
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Boolean> labels = new LinkedHashMap<>();

        if (dbOptions != null) {
            for (Map<String, Object> item : dbOptions) {
                if (item == null) {
                    continue;
                }
                String label = String.valueOf(item.get("label"));
                labels.put(label, true);
                result.add(item);
            }
        }

        String[] defaults = new String[] {"系统研发岗", "测试", "项目助理", "行政", "人力"};
        for (int i = 0; i < defaults.length; i++) {
            String label = defaults[i];
            if (labels.containsKey(label)) {
                continue;
            }
            Map<String, Object> option = new LinkedHashMap<>();
            option.put("value", "INIT_" + (i + 1));
            option.put("label", label);
            result.add(option);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getDispatchDemandOptions(String operatorUserId, String operatorRole) {
        if (trimToNull(operatorUserId) == null) {
            throw new RuntimeException("操作人不能为空");
        }
        User operator = userMapper.getUserById(operatorUserId);
        if (operator == null) {
            throw new RuntimeException("操作人不存在");
        }
        Set<String> roles = normalizeRoles(operator, operatorRole);
        ensureDispatchAccess(operatorUserId, roles);

        List<Map<String, Object>> options = new ArrayList<>();
        for (DemandRequirement demand : demandRequirementMapper.selectAll()) {
            if (trimToNull(demand.getDemandStatus()) != null) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("demandId", demand.getDemandId());
            row.put("value", demand.getDemandId());
            row.put("sourceRecruitmentRequestId", demand.getSourceRecruitmentRequestId());
            row.put("label", demand.getPositionOrgName());
            options.add(row);
        }
        return options;
    }

    @Override
    public void screenResume(Long id, String action, String operatorUserId, String operatorUserName, String operatorRole) {
        if (trimToNull(operatorUserId) == null) {
            throw new RuntimeException("操作人不能为空");
        }
        User operator = userMapper.getUserById(operatorUserId);
        if (operator == null) {
            throw new RuntimeException("操作人不存在");
        }
        Set<String> roles = normalizeRoles(operator, operatorRole);
        ensureScreenAccess(operatorUserId, roles);

        Resume resume = resumeMapper.selectByPrimaryKey(id);
        if (resume == null) {
            throw new RuntimeException("简历不存在");
        }

        String upper = trimToNull(action) == null ? "" : action.trim().toUpperCase();
        if ("PASS".equals(upper)) {
            resume.setStatus(STATUS_SCREEN_PASS);
        } else if ("MATERIAL_EDIT".equals(upper)) {
            resume.setStatus(STATUS_SCREEN_EDIT);
        } else if ("REJECT".equals(upper)) {
            resume.setStatus(STATUS_SCREEN_REJECT);
        } else {
            throw new RuntimeException("初筛动作不支持");
        }
        resume.setUpdateUserId(defaultValue(operatorUserId, SYSTEM_USER_ID));
        resume.setUpdateUserName(defaultValue(operatorUserName, SYSTEM_USER_NAME));
        resumeMapper.updateByPrimaryKey(resume);
    }

    @Override
    public void dispatchResume(Long id, List<Long> demandIds, String operatorUserId, String operatorUserName, String operatorRole) {
        if (trimToNull(operatorUserId) == null) {
            throw new RuntimeException("操作人不能为空");
        }
        User operator = userMapper.getUserById(operatorUserId);
        if (operator == null) {
            throw new RuntimeException("操作人不存在");
        }
        Set<String> roles = normalizeRoles(operator, operatorRole);
        ensureDispatchAccess(operatorUserId, roles);

        Resume resume = resumeMapper.selectByPrimaryKey(id);
        if (resume == null) {
            throw new RuntimeException("简历不存在");
        }
        if (!isPassedStatus(resume.getStatus())) {
            throw new RuntimeException("仅已通过简历初筛的简历可分发");
        }
        if (demandIds == null || demandIds.isEmpty()) {
            throw new RuntimeException("请选择关联需求");
        }

        List<ResumeDispatch> existing = resumeDispatchMapper.selectByResumeId(id);
        boolean hasConfirmed = existing.stream().anyMatch(item -> "CONFIRMED".equals(item.getDispatchStatus()));
        if (hasConfirmed) {
            throw new RuntimeException("该简历已被其他面试官确认");
        }

        Map<String, ResumeDispatch> dispatchByInterviewer = new LinkedHashMap<>();
        Date now = new Date();
        for (Long demandId : new LinkedHashSet<>(demandIds)) {
            DemandRequirement demand = demandRequirementMapper.selectByPrimaryKey(demandId);
            if (demand == null) {
                throw new RuntimeException("分发需求不存在");
            }
            if (trimToNull(demand.getDemandStatus()) != null) {
                throw new RuntimeException("分发需求状态不允许分发");
            }

            Long recruitmentRequestId = demand.getSourceRecruitmentRequestId();
            RecruitmentRequest request = recruitmentRequestId == null ? null : recruitmentRequestMapper.selectByPrimaryKey(recruitmentRequestId);
            if (request == null || trimToNull(request.getInterviewerId()) == null) {
                throw new RuntimeException("关联需求未配置面试官");
            }

            String interviewerId = request.getInterviewerId().trim();
            if (dispatchByInterviewer.containsKey(interviewerId)) {
                continue;
            }
            ResumeDispatch dispatch = new ResumeDispatch();
            dispatch.setResumeId(id);
            dispatch.setRecruitmentRequestId(recruitmentRequestId);
            dispatch.setInterviewerId(interviewerId);
            dispatch.setInterviewerName(defaultValue(request.getInterviewerName(), interviewerId));
            dispatch.setDispatchStatus("PENDING");
            dispatch.setDispatchTime(now);
            dispatch.setCreateTime(now);
            dispatch.setCreateUserId(defaultValue(operatorUserId, SYSTEM_USER_ID));
            dispatch.setCreateUserName(defaultValue(operatorUserName, SYSTEM_USER_NAME));
            dispatch.setUpdateTime(now);
            dispatch.setUpdateUserId(defaultValue(operatorUserId, SYSTEM_USER_ID));
            dispatch.setUpdateUserName(defaultValue(operatorUserName, SYSTEM_USER_NAME));
            dispatchByInterviewer.put(interviewerId, dispatch);
        }

        if (dispatchByInterviewer.isEmpty()) {
            throw new RuntimeException("关联需求未配置面试官");
        }

        resumeDispatchMapper.deleteByResumeId(id);
        for (ResumeDispatch dispatch : dispatchByInterviewer.values()) {
            resumeDispatchMapper.insert(dispatch);
        }
    }

    @Override
    public void interviewerChoice(Long id, String choice, String operatorUserId, String operatorUserName, String operatorRole) {
        if (trimToNull(operatorUserId) == null) {
            throw new RuntimeException("操作人不能为空");
        }
        User operator = userMapper.getUserById(operatorUserId);
        if (operator == null) {
            throw new RuntimeException("操作人不存在");
        }
        Set<String> roles = normalizeRoles(operator, operatorRole);
        ensureInterviewerChoiceAccess(operatorUserId, roles);

        Resume resume = resumeMapper.selectByPrimaryKey(id);
        if (resume == null) {
            throw new RuntimeException("简历不存在");
        }

        List<ResumeDispatch> allDispatches = resumeDispatchMapper.selectByResumeId(id);
        List<ResumeDispatch> mine = allDispatches.stream()
            .filter(item -> operatorUserId.equals(item.getInterviewerId()))
            .collect(Collectors.toList());
        if (mine.isEmpty()) {
            throw new RuntimeException("当前面试官无可操作分发");
        }

        String normalizedChoice = trimToNull(choice) == null ? "" : choice.trim().toUpperCase();
        boolean confirmedByOther = allDispatches.stream().anyMatch(item ->
            "CONFIRMED".equals(item.getDispatchStatus()) && !operatorUserId.equals(item.getInterviewerId()));

        if ("CONFIRM".equals(normalizedChoice)) {
            if (confirmedByOther) {
                throw new RuntimeException("该简历已被其他面试官确认");
            }
            resumeDispatchMapper.updateStatusByResumeAndInterviewer(
                id,
                operatorUserId,
                "CONFIRMED",
                new Date(),
                defaultValue(operatorUserId, SYSTEM_USER_ID),
                defaultValue(operatorUserName, SYSTEM_USER_NAME)
            );
            return;
        }

        if ("ABANDON".equals(normalizedChoice)) {
            resumeDispatchMapper.updateStatusByResumeAndInterviewer(
                id,
                operatorUserId,
                "ABANDONED",
                null,
                defaultValue(operatorUserId, SYSTEM_USER_ID),
                defaultValue(operatorUserName, SYSTEM_USER_NAME)
            );
            return;
        }

        throw new RuntimeException("面试官操作不支持");
    }

    private Map<Long, List<ResumeDispatch>> buildDispatchMap(List<Resume> resumes) {
        List<Long> resumeIds = resumes.stream().map(Resume::getResumeId).collect(Collectors.toList());
        if (resumeIds.isEmpty()) {
            return new LinkedHashMap<>();
        }

        Map<Long, List<ResumeDispatch>> map = new LinkedHashMap<>();
        for (ResumeDispatch dispatch : resumeDispatchMapper.selectByResumeIds(resumeIds)) {
            map.computeIfAbsent(dispatch.getResumeId(), key -> new ArrayList<>()).add(dispatch);
        }
        return map;
    }

    private boolean canViewResume(Resume resume,
                                  List<ResumeDispatch> dispatches,
                                  User viewer,
                                  String viewerId,
                                  Set<String> roles) {
        if (isSuperAdmin(viewerId, roles) || roles.contains(ROLE_OUTSOURCING_MANAGER)) {
            return true;
        }

        if (roles.contains(ROLE_SUPPLIER_HR)) {
            return viewerId != null && viewerId.equals(resume.getCreateUserId());
        }

        if (roles.contains(ROLE_INTERVIEWER)) {
            return viewerId != null && dispatches.stream().anyMatch(item -> viewerId.equals(item.getInterviewerId()));
        }

        if (roles.contains(ROLE_ROOM_MANAGER)) {
            String managerDepartment = viewer == null ? null : trimToNull(viewer.getDepartment());
            if (managerDepartment == null) {
                return false;
            }
            for (ResumeDispatch dispatch : dispatches) {
                if (!"CONFIRMED".equals(dispatch.getDispatchStatus())) {
                    continue;
                }
                User interviewer = userMapper.getUserById(dispatch.getInterviewerId());
                if (interviewer != null && managerDepartment.equals(trimToNull(interviewer.getDepartment()))) {
                    return true;
                }
            }
            return false;
        }

        if (roles.contains(ROLE_TEAM_MANAGER)) {
            String managerTeam = viewer == null ? null : trimToNull(viewer.getTeamName());
            if (managerTeam == null) {
                return false;
            }
            for (ResumeDispatch dispatch : dispatches) {
                if (!"CONFIRMED".equals(dispatch.getDispatchStatus())) {
                    continue;
                }
                User interviewer = userMapper.getUserById(dispatch.getInterviewerId());
                if (interviewer != null && managerTeam.equals(trimToNull(interviewer.getTeamName()))) {
                    return true;
                }
            }
            return false;
        }

        return false;
    }

    private void enrichChoiceFields(Resume resume, List<ResumeDispatch> dispatches, String viewerId, Set<String> roles) {
        ResumeDispatch confirmed = dispatches.stream()
            .filter(item -> "CONFIRMED".equals(item.getDispatchStatus()))
            .findFirst()
            .orElse(null);
        if (confirmed != null) {
            resume.setConfirmedInterviewerId(confirmed.getInterviewerId());
            resume.setConfirmedInterviewerName(confirmed.getInterviewerName());
        } else {
            resume.setConfirmedInterviewerId(null);
            resume.setConfirmedInterviewerName(null);
        }

        if (!roles.contains(ROLE_INTERVIEWER) || viewerId == null) {
            resume.setCanInterviewerConfirm(false);
            resume.setCanInterviewerAbandon(false);
            resume.setCurrentInterviewerChoiceStatus(null);
            return;
        }

        ResumeDispatch mine = dispatches.stream()
            .filter(item -> viewerId.equals(item.getInterviewerId()))
            .findFirst()
            .orElse(null);
        if (mine == null) {
            resume.setCanInterviewerConfirm(false);
            resume.setCanInterviewerAbandon(false);
            resume.setCurrentInterviewerChoiceStatus(null);
            return;
        }

        boolean confirmedByOther = dispatches.stream().anyMatch(item ->
            "CONFIRMED".equals(item.getDispatchStatus()) && !viewerId.equals(item.getInterviewerId()));

        String myStatus = trimToNull(mine.getDispatchStatus());
        if ("CONFIRMED".equals(myStatus)) {
            resume.setCurrentInterviewerChoiceStatus("已确认");
            resume.setCanInterviewerConfirm(false);
            resume.setCanInterviewerAbandon(false);
            return;
        }

        if ("ABANDONED".equals(myStatus)) {
            resume.setCurrentInterviewerChoiceStatus("已放弃");
            resume.setCanInterviewerConfirm(!confirmedByOther && isPassedStatus(resume.getStatus()));
            resume.setCanInterviewerAbandon(false);
            return;
        }

        resume.setCurrentInterviewerChoiceStatus("待选择");
        resume.setCanInterviewerConfirm(!confirmedByOther && isPassedStatus(resume.getStatus()));
        resume.setCanInterviewerAbandon(!confirmedByOther);
    }

    private Set<String> normalizeRoles(User user, String viewerRole) {
        Set<String> roles = new LinkedHashSet<>();
        if (user != null) {
            if (SYSTEM_USER_ID.equals(user.getUserId())) {
                roles.add(ROLE_SUPER_ADMIN);
            }
            List<String> roleNames = userMapper.getRoleNamesByUserId(user.getUserId());
            if (roleNames != null) {
                for (String roleName : roleNames) {
                    String normalized = normalizeRole(roleName);
                    if (!normalized.isEmpty()) {
                        roles.add(normalized);
                    }
                }
            }
            String normalizedPosition = normalizeRole(user.getPosition());
            if (!normalizedPosition.isEmpty()) {
                roles.add(normalizedPosition);
            }
        }
        String normalizedInputRole = normalizeRole(viewerRole);
        if (!normalizedInputRole.isEmpty()) {
            roles.add(normalizedInputRole);
        }
        return roles;
    }

    private String normalizeRole(String roleName) {
        String value = trimToNull(roleName);
        if (value == null) {
            return "";
        }
        if (value.contains("供应商HR")) {
            return ROLE_SUPPLIER_HR;
        }
        if (value.contains("外包招聘管理")) {
            return ROLE_OUTSOURCING_MANAGER;
        }
        if (value.contains("面试官")) {
            return ROLE_INTERVIEWER;
        }
        if (value.contains("室经理")) {
            return ROLE_ROOM_MANAGER;
        }
        if (value.contains("团队经理")) {
            return ROLE_TEAM_MANAGER;
        }
        if (value.contains("超级管理员") || value.contains("系统管理员")) {
            return ROLE_SUPER_ADMIN;
        }
        return value;
    }

    private boolean isSuperAdmin(String userId, Set<String> roles) {
        return SYSTEM_USER_ID.equals(userId) || roles.contains(ROLE_SUPER_ADMIN);
    }

    private void ensureListAccess(String viewerId, Set<String> roles) {
        if (isSuperAdmin(viewerId, roles)
            || roles.contains(ROLE_SUPPLIER_HR)
            || roles.contains(ROLE_OUTSOURCING_MANAGER)
            || roles.contains(ROLE_INTERVIEWER)
            || roles.contains(ROLE_ROOM_MANAGER)
            || roles.contains(ROLE_TEAM_MANAGER)) {
            return;
        }
        throw new RuntimeException("无权限访问简历管理");
    }

    private void ensureSubmitAccess(String operatorUserId, Set<String> roles) {
        if (isSuperAdmin(operatorUserId, roles) || roles.contains(ROLE_SUPPLIER_HR)) {
            return;
        }
        throw new RuntimeException("仅供应商HR可提交简历");
    }

    private void ensureEditAccess(Resume resume, String operatorUserId, Set<String> roles) {
        if (isSuperAdmin(operatorUserId, roles)) {
            return;
        }
        if (!roles.contains(ROLE_SUPPLIER_HR) || !operatorUserId.equals(resume.getCreateUserId())) {
            throw new RuntimeException("无权限编辑该简历");
        }
        if (isPassedStatus(resume.getStatus())) {
            throw new RuntimeException("已通过简历初筛的简历不允许编辑");
        }
    }

    private void ensureDeleteAccess(Resume resume, String operatorUserId, Set<String> roles) {
        if (isSuperAdmin(operatorUserId, roles)) {
            return;
        }
        if (!roles.contains(ROLE_SUPPLIER_HR) || !operatorUserId.equals(resume.getCreateUserId())) {
            throw new RuntimeException("无权限删除该简历");
        }
        if (isPassedStatus(resume.getStatus())) {
            throw new RuntimeException("已通过简历初筛的简历不允许删除");
        }
    }

    private void ensureScreenAccess(String operatorUserId, Set<String> roles) {
        if (isSuperAdmin(operatorUserId, roles) || roles.contains(ROLE_OUTSOURCING_MANAGER)) {
            return;
        }
        throw new RuntimeException("仅外包招聘管理岗可执行初筛");
    }

    private void ensureDispatchAccess(String operatorUserId, Set<String> roles) {
        if (isSuperAdmin(operatorUserId, roles) || roles.contains(ROLE_OUTSOURCING_MANAGER)) {
            return;
        }
        throw new RuntimeException("仅外包招聘管理岗可分发简历");
    }

    private void ensureInterviewerChoiceAccess(String operatorUserId, Set<String> roles) {
        if (isSuperAdmin(operatorUserId, roles) || roles.contains(ROLE_INTERVIEWER)) {
            return;
        }
        throw new RuntimeException("仅面试官可执行确认选择/放弃选择");
    }

    private String normalizeStatus(String status) {
        String value = trimToNull(status);
        if (value == null) {
            return STATUS_PENDING_SCREEN;
        }
        if (STATUS_PENDING_SCREEN.equals(value)
            || STATUS_SCREEN_PASS.equals(value)
            || STATUS_SCREEN_EDIT.equals(value)
            || STATUS_SCREEN_REJECT.equals(value)) {
            return value;
        }
        if ("PENDING_SCREENING".equalsIgnoreCase(value)) {
            return STATUS_PENDING_SCREEN;
        }
        if ("SCREENED".equalsIgnoreCase(value) || "INTERVIEW".equalsIgnoreCase(value) || "HIRED".equalsIgnoreCase(value)) {
            return STATUS_SCREEN_PASS;
        }
        if ("REJECTED".equalsIgnoreCase(value)) {
            return STATUS_SCREEN_REJECT;
        }
        return value;
    }

    private boolean isPassedStatus(String status) {
        return STATUS_SCREEN_PASS.equals(normalizeStatus(status));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String defaultValue(String first, String fallback) {
        String value = trimToNull(first);
        if (value != null) {
            return value;
        }
        return fallback;
    }
}
