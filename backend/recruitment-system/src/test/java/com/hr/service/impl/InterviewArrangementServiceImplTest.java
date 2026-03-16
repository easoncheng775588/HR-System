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
import com.hr.service.InterviewMessageService;
import com.hr.service.InterviewPermissionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterviewArrangementServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private ResumeMapper resumeMapper;

    @Mock
    private ResumeDispatchMapper resumeDispatchMapper;

    @Mock
    private InterviewEvaluationMapper interviewEvaluationMapper;

    @Mock
    private InterviewPermissionService interviewPermissionService;

    @Mock
    private InterviewMessageService interviewMessageService;

    @InjectMocks
    private InterviewArrangementServiceImpl interviewArrangementService;

    @Test
    void getPendingListInterviewerOnlySeesOwnConfirmedResume() {
        User viewer = user("2001", "面试官A", "面试官");
        Set<String> roles = Set.of(InterviewPermissionService.ROLE_INTERVIEWER);
        when(userMapper.getUserById("2001")).thenReturn(viewer);
        when(interviewPermissionService.normalizeRoles(viewer, "面试官")).thenReturn(roles);
        when(interviewPermissionService.isSuperAdmin("2001", roles)).thenReturn(false);
        when(interviewPermissionService.hasRole(roles, InterviewPermissionService.ROLE_OUTSOURCING_MANAGER)).thenReturn(false);
        when(interviewPermissionService.hasRole(roles, InterviewPermissionService.ROLE_INTERVIEWER)).thenReturn(true);

        Resume mine = resume(1L, "候选人甲", "3001");
        mine.setItWorkYears("5年");
        mine.setAppliedLevel("P6");
        mine.setCandidatePlatform("Java");
        Resume other = resume(2L, "候选人乙", "3002");
        when(resumeMapper.selectAll()).thenReturn(Arrays.asList(mine, other));

        ResumeDispatch mineConfirmed = dispatch(1L, 11L, "2001", "CONFIRMED");
        mineConfirmed.setConfirmedInterviewTime(new Date());
        ResumeDispatch otherConfirmed = dispatch(2L, 12L, "2002", "CONFIRMED");
        when(resumeDispatchMapper.selectByResumeIds(Arrays.asList(1L, 2L))).thenReturn(Arrays.asList(mineConfirmed, otherConfirmed));

        InterviewEvaluation evaluation = new InterviewEvaluation();
        evaluation.setResumeId(1L);
        evaluation.setApprovalStatus("PENDING_OUTSOURCING");
        when(interviewEvaluationMapper.selectAll()).thenReturn(Collections.singletonList(evaluation));

        List<PendingInterviewResumeVO> rows = interviewArrangementService.getPendingList("2001", "面试官");

        assertEquals(1, rows.size());
        assertEquals(Long.valueOf(1L), rows.get(0).getResumeId());
        assertEquals("5年", rows.get(0).getWorkYears());
        assertEquals("P6", rows.get(0).getAppliedLevel());
        assertEquals("Java", rows.get(0).getCandidatePlatform());
        assertTrue(Boolean.TRUE.equals(rows.get(0).getCanLaunchEvaluation()));
        assertEquals("PENDING_OUTSOURCING", rows.get(0).getEvaluationStatus());
    }

    @Test
    void getPendingListOutsourcingIncludesPendingDispatchAfterResumeDistributed() {
        User viewer = user("1002", "外包岗A", "外包招聘管理岗");
        Set<String> roles = Set.of(InterviewPermissionService.ROLE_OUTSOURCING_MANAGER);
        when(userMapper.getUserById("1002")).thenReturn(viewer);
        when(interviewPermissionService.normalizeRoles(viewer, "外包招聘管理岗")).thenReturn(roles);
        when(interviewPermissionService.hasRole(roles, InterviewPermissionService.ROLE_INTERVIEWER)).thenReturn(false);
        when(interviewPermissionService.isSuperAdmin("1002", roles)).thenReturn(false);
        when(interviewPermissionService.hasRole(roles, InterviewPermissionService.ROLE_OUTSOURCING_MANAGER)).thenReturn(true);

        Resume pendingResume = resume(3L, "候选人丙", "3003");
        when(resumeMapper.selectAll()).thenReturn(Collections.singletonList(pendingResume));

        ResumeDispatch pendingDispatch = dispatch(3L, 13L, "2008", "PENDING");
        when(resumeDispatchMapper.selectByResumeIds(Collections.singletonList(3L))).thenReturn(Collections.singletonList(pendingDispatch));
        when(interviewEvaluationMapper.selectAll()).thenReturn(Collections.emptyList());

        List<PendingInterviewResumeVO> rows = interviewArrangementService.getPendingList("1002", "外包招聘管理岗");

        assertEquals(1, rows.size());
        assertEquals("PENDING", rows.get(0).getDispatchStatus());
        assertTrue(Boolean.FALSE.equals(rows.get(0).getCanConfirmInterviewTime()));
    }

    @Test
    void confirmInterviewTimeUpdatesDispatchAndSendsMessage() {
        User outsourcingManager = user("1002", "外包岗A", "外包招聘管理岗");
        when(userMapper.getUserById("1002")).thenReturn(outsourcingManager);
        when(interviewPermissionService.normalizeRoles(outsourcingManager, "外包招聘管理岗"))
            .thenReturn(Set.of(InterviewPermissionService.ROLE_OUTSOURCING_MANAGER));
        when(interviewPermissionService.hasRole(Set.of(InterviewPermissionService.ROLE_OUTSOURCING_MANAGER), InterviewPermissionService.ROLE_OUTSOURCING_MANAGER))
            .thenReturn(true);

        ResumeDispatch confirmedDispatch = dispatch(1L, 9L, "7001", "CONFIRMED");
        when(resumeDispatchMapper.selectConfirmedByResumeId(1L)).thenReturn(confirmedDispatch);

        Resume resume = resume(1L, "王小明", "3001");
        when(resumeMapper.selectByPrimaryKey(1L)).thenReturn(resume);

        ConfirmInterviewTimeRequest request = new ConfirmInterviewTimeRequest();
        request.setInterviewTime("2026-03-13 10:30:00");
        request.setOperatorUserId("1002");
        request.setOperatorUserName("外包岗A");
        request.setOperatorRole("外包招聘管理岗");

        interviewArrangementService.confirmInterviewTime(1L, request);

        ArgumentCaptor<Date> timeCaptor = ArgumentCaptor.forClass(Date.class);
        verify(resumeDispatchMapper).updateConfirmedInterviewTime(eq(9L), timeCaptor.capture(), eq("1002"), eq("外包岗A"), any(Date.class));
        verify(interviewMessageService).sendInterviewTimeConfirmedMessage("王小明", "7001", "3001");
        assertTrue(timeCaptor.getValue() != null);
    }

    @Test
    void confirmInterviewTimeAcceptsDateOnlyPayload() {
        User outsourcingManager = user("1002", "外包岗A", "外包招聘管理岗");
        Set<String> roles = Set.of(InterviewPermissionService.ROLE_OUTSOURCING_MANAGER);
        when(userMapper.getUserById("1002")).thenReturn(outsourcingManager);
        when(interviewPermissionService.normalizeRoles(outsourcingManager, "外包招聘管理岗")).thenReturn(roles);
        when(interviewPermissionService.hasRole(roles, InterviewPermissionService.ROLE_OUTSOURCING_MANAGER)).thenReturn(true);

        ResumeDispatch confirmedDispatch = dispatch(1L, 9L, "7001", "CONFIRMED");
        when(resumeDispatchMapper.selectConfirmedByResumeId(1L)).thenReturn(confirmedDispatch);

        Resume resume = resume(1L, "王小明", "3001");
        when(resumeMapper.selectByPrimaryKey(1L)).thenReturn(resume);

        ConfirmInterviewTimeRequest request = new ConfirmInterviewTimeRequest();
        request.setInterviewTime("2026-03-20");
        request.setOperatorUserId("1002");
        request.setOperatorUserName("外包岗A");
        request.setOperatorRole("外包招聘管理岗");

        interviewArrangementService.confirmInterviewTime(1L, request);

        verify(resumeDispatchMapper).updateConfirmedInterviewTime(eq(9L), any(Date.class), eq("1002"), eq("外包岗A"), any(Date.class));
    }

    @Test
    void confirmInterviewTimeRejectsSupplierHr() {
        User supplierHr = user("3001", "供应商HR", "供应商HR");
        when(userMapper.getUserById("3001")).thenReturn(supplierHr);
        when(interviewPermissionService.normalizeRoles(supplierHr, "供应商HR"))
            .thenReturn(Set.of(InterviewPermissionService.ROLE_SUPPLIER_HR));
        when(interviewPermissionService.hasRole(Set.of(InterviewPermissionService.ROLE_SUPPLIER_HR), InterviewPermissionService.ROLE_OUTSOURCING_MANAGER))
            .thenReturn(false);
        when(interviewPermissionService.isSuperAdmin("3001", Set.of(InterviewPermissionService.ROLE_SUPPLIER_HR)))
            .thenReturn(false);

        ConfirmInterviewTimeRequest request = new ConfirmInterviewTimeRequest();
        request.setInterviewTime("2026-03-13 10:30:00");
        request.setOperatorUserId("3001");
        request.setOperatorUserName("供应商HR");
        request.setOperatorRole("供应商HR");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> interviewArrangementService.confirmInterviewTime(1L, request));
        assertEquals("仅外包招聘管理岗可确认面试时间", ex.getMessage());
    }

    private User user(String userId, String realName, String position) {
        User user = new User();
        user.setUserId(userId);
        user.setRealName(realName);
        user.setPosition(position);
        user.setStatus("ACTIVE");
        return user;
    }

    private Resume resume(Long resumeId, String candidateName, String createUserId) {
        Resume resume = new Resume();
        resume.setResumeId(resumeId);
        resume.setCandidateName(candidateName);
        resume.setCreateUserId(createUserId);
        return resume;
    }

    private ResumeDispatch dispatch(Long resumeId, Long dispatchId, String interviewerId, String status) {
        ResumeDispatch dispatch = new ResumeDispatch();
        dispatch.setResumeId(resumeId);
        dispatch.setDispatchId(dispatchId);
        dispatch.setInterviewerId(interviewerId);
        dispatch.setInterviewerName("面试官");
        dispatch.setDispatchStatus(status);
        return dispatch;
    }
}
