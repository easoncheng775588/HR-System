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
import com.hr.service.InterviewPermissionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntryManagementServiceImplTest {

    @Mock
    private EntryRecordMapper entryRecordMapper;

    @Mock
    private ResumeDispatchMapper resumeDispatchMapper;

    @Mock
    private RecruitmentRequestMapper recruitmentRequestMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private OrgUnitMapper orgUnitMapper;

    @Mock
    private InterviewPermissionService interviewPermissionService;

    @InjectMocks
    private EntryManagementServiceImpl entryManagementService;

    @Captor
    private ArgumentCaptor<EntryRecord> entryCaptor;

    @Test
    void createFromApprovedEvaluationBuildsEntryRecord() throws Exception {
        InterviewEvaluation evaluation = new InterviewEvaluation();
        evaluation.setEvaluationId(8L);
        evaluation.setResumeId(6L);
        evaluation.setCandidateName("候选人甲");
        evaluation.setInterviewDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2026-03-17 14:00:00"));
        evaluation.setEntryLevelSuggestion("P6");
        evaluation.setPlatform("Java");
        evaluation.setUpdateUserId("1007");
        evaluation.setUpdateUserName("micro");

        ResumeDispatch dispatch = new ResumeDispatch();
        dispatch.setRecruitmentRequestId(15L);
        when(entryRecordMapper.selectByEvaluationId(8L)).thenReturn(null);
        when(resumeDispatchMapper.selectConfirmedByResumeId(6L)).thenReturn(dispatch);

        RecruitmentRequest request = new RecruitmentRequest();
        request.setRecruitmentRequestId(15L);
        request.setOrgUnitName("办公系统开发室");
        when(recruitmentRequestMapper.selectByPrimaryKey(15L)).thenReturn(request);

        entryManagementService.createFromApprovedEvaluation(evaluation);

        verify(entryRecordMapper).insert(entryCaptor.capture());
        assertEquals(Long.valueOf(8L), entryCaptor.getValue().getEvaluationId());
        assertEquals("候选人甲", entryCaptor.getValue().getCandidateName());
        assertEquals("办公系统开发室", entryCaptor.getValue().getHiredDepartment());
        assertEquals("P6", entryCaptor.getValue().getPositionLevel());
        assertEquals("Java", entryCaptor.getValue().getTechnicalPlatform());
    }

    @Test
    void getEntryListReturnsOwnRequestEntriesForRoomManagerAndTeamEntriesForTeamManager() {
        EntryRecord ownEntry = entry(1L, 11L);
        EntryRecord sameTeamEntry = entry(2L, 12L);
        EntryRecord otherTeamEntry = entry(3L, 13L);
        when(entryRecordMapper.selectAll()).thenReturn(List.of(ownEntry, sameTeamEntry, otherTeamEntry));

        User teamManager = user("2001", "团队经理甲", "基础业务开发团队", "办公系统开发室");
        when(userMapper.getUserById("2001")).thenReturn(teamManager);
        when(interviewPermissionService.normalizeRoles(teamManager, "团队经理"))
            .thenReturn(Set.of(InterviewPermissionService.ROLE_TEAM_MANAGER));
        when(interviewPermissionService.isSuperAdmin("2001", Set.of(InterviewPermissionService.ROLE_TEAM_MANAGER))).thenReturn(false);
        when(interviewPermissionService.hasRole(Set.of(InterviewPermissionService.ROLE_TEAM_MANAGER), InterviewPermissionService.ROLE_OUTSOURCING_MANAGER)).thenReturn(false);
        when(interviewPermissionService.hasRole(Set.of(InterviewPermissionService.ROLE_TEAM_MANAGER), InterviewPermissionService.ROLE_ROOM_MANAGER)).thenReturn(false);
        when(interviewPermissionService.hasRole(Set.of(InterviewPermissionService.ROLE_TEAM_MANAGER), InterviewPermissionService.ROLE_TEAM_MANAGER)).thenReturn(true);

        RecruitmentRequest ownRequest = request(11L, "2001", "基础业务开发团队");
        RecruitmentRequest sameTeamRequest = request(12L, "2002", "基础业务开发团队");
        RecruitmentRequest otherTeamRequest = request(13L, "2003", "数据平台团队");
        when(recruitmentRequestMapper.selectByPrimaryKey(11L)).thenReturn(ownRequest);
        when(recruitmentRequestMapper.selectByPrimaryKey(12L)).thenReturn(sameTeamRequest);
        when(recruitmentRequestMapper.selectByPrimaryKey(13L)).thenReturn(otherTeamRequest);

        List<EntryRecord> result = entryManagementService.getEntryList("2001", "团队经理");

        assertEquals(2, result.size());
        assertEquals(List.of(1L, 2L), result.stream().map(EntryRecord::getEntryRecordId).toList());
    }

    @Test
    void updateEntryRejectsInvalidStatus() {
        User operator = user("1002", "外包岗", "人力资源团队", "人力资源团队");
        when(userMapper.getUserById("1002")).thenReturn(operator);
        when(interviewPermissionService.normalizeRoles(operator, "外包招聘管理岗"))
            .thenReturn(Set.of(InterviewPermissionService.ROLE_OUTSOURCING_MANAGER));
        when(interviewPermissionService.isSuperAdmin("1002", Set.of(InterviewPermissionService.ROLE_OUTSOURCING_MANAGER))).thenReturn(false);
        when(interviewPermissionService.hasRole(Set.of(InterviewPermissionService.ROLE_OUTSOURCING_MANAGER), InterviewPermissionService.ROLE_OUTSOURCING_MANAGER)).thenReturn(true);

        EntryRecord record = entry(9L, 11L);
        when(entryRecordMapper.selectByPrimaryKey(9L)).thenReturn(record);

        EntryRecordUpdateRequest request = new EntryRecordUpdateRequest();
        request.setOperatorUserId("1002");
        request.setOperatorUserName("外包岗");
        request.setOperatorRole("外包招聘管理岗");
        request.setEntryStatus("未知状态");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> entryManagementService.updateEntry(9L, request));
        assertEquals("入场状态不支持", exception.getMessage());
    }

    private EntryRecord entry(Long entryId, Long requestId) {
        EntryRecord entry = new EntryRecord();
        entry.setEntryRecordId(entryId);
        entry.setSourceRecruitmentRequestId(requestId);
        return entry;
    }

    private RecruitmentRequest request(Long requestId, String createUserId, String team) {
        RecruitmentRequest request = new RecruitmentRequest();
        request.setRecruitmentRequestId(requestId);
        request.setCreateUserId(createUserId);
        request.setTeam(team);
        return request;
    }

    private User user(String userId, String realName, String teamName, String department) {
        User user = new User();
        user.setUserId(userId);
        user.setRealName(realName);
        user.setTeamName(teamName);
        user.setDepartment(department);
        user.setStatus("ACTIVE");
        return user;
    }
}
