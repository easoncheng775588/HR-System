package com.hr.service.impl;

import com.hr.entity.InterviewEvaluation;
import com.hr.entity.InterviewEvaluationStatusEnum;
import com.hr.entity.Resume;
import com.hr.entity.ResumeDispatch;
import com.hr.entity.SubmitInterviewEvaluationRequest;
import com.hr.entity.User;
import com.hr.entity.WorkflowApproveRequest;
import com.hr.mapper.InterviewEvaluationApprovalHistoryMapper;
import com.hr.mapper.InterviewEvaluationMapper;
import com.hr.mapper.ResumeDispatchMapper;
import com.hr.mapper.ResumeMapper;
import com.hr.mapper.UserMapper;
import com.hr.mapper.WorkflowProcessLogMapper;
import com.hr.service.InterviewMessageService;
import com.hr.service.InterviewPermissionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterviewEvaluationServiceImplTest {

    @Mock
    private InterviewEvaluationMapper interviewEvaluationMapper;

    @Mock
    private InterviewEvaluationApprovalHistoryMapper interviewEvaluationApprovalHistoryMapper;

    @Mock
    private ResumeMapper resumeMapper;

    @Mock
    private ResumeDispatchMapper resumeDispatchMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private InterviewPermissionService interviewPermissionService;

    @Mock
    private InterviewMessageService interviewMessageService;

    @Mock
    private WorkflowProcessLogMapper workflowProcessLogMapper;

    @InjectMocks
    private InterviewEvaluationServiceImpl interviewEvaluationService;

    @Test
    void submitCreatesPendingOutsourcingEvaluation() throws Exception {
        User interviewer = user("2001", "面试官A", "面试官", "基础业务开发团队 / 办公系统开发室");
        when(userMapper.getUserById("2001")).thenReturn(interviewer);
        when(interviewPermissionService.normalizeRoles(interviewer, "面试官"))
            .thenReturn(Set.of(InterviewPermissionService.ROLE_INTERVIEWER));
        when(interviewPermissionService.hasRole(Set.of(InterviewPermissionService.ROLE_INTERVIEWER), InterviewPermissionService.ROLE_INTERVIEWER))
            .thenReturn(true);

        Resume resume = new Resume();
        resume.setResumeId(1L);
        resume.setCandidateName("候选人甲");
        resume.setGender("男");
        resume.setItWorkYears("5年");
        resume.setAppliedLevel("P6");
        resume.setSupplierName("供应商A");
        resume.setCandidatePlatform("Java");
        when(resumeMapper.selectByPrimaryKey(1L)).thenReturn(resume);

        ResumeDispatch confirmed = new ResumeDispatch();
        confirmed.setDispatchId(9L);
        confirmed.setInterviewerId("2001");
        confirmed.setInterviewerName("面试官A");
        confirmed.setInterviewMethod("线上");
        confirmed.setConfirmedInterviewTime(new Date());
        when(resumeDispatchMapper.selectConfirmedByResumeId(1L)).thenReturn(confirmed);

        when(interviewEvaluationMapper.selectLatestByResumeId(1L)).thenReturn(null);
        when(interviewEvaluationMapper.insert(any(InterviewEvaluation.class))).thenAnswer(invocation -> {
            InterviewEvaluation entity = invocation.getArgument(0);
            entity.setEvaluationId(88L);
            return 1;
        });
        when(interviewEvaluationMapper.selectByPrimaryKey(88L)).thenAnswer(invocation -> {
            InterviewEvaluation entity = new InterviewEvaluation();
            entity.setEvaluationId(88L);
            entity.setApprovalStatus(InterviewEvaluationStatusEnum.PENDING_OUTSOURCING.name());
            return entity;
        });

        SubmitInterviewEvaluationRequest request = new SubmitInterviewEvaluationRequest();
        request.setResumeId(1L);
        request.setOperatorUserId("2001");
        request.setOperatorUserName("面试官A");
        request.setOperatorRole("面试官");
        request.setEntryLevelSuggestion("P6");
        request.setScore("90");
        request.setInterviewMethod("线上");
        request.setInterviewDate("2026-03-13 11:30:00");
        request.setHireSuggestion("良好");
        request.setAttachmentName("面试评价表.pdf");
        request.setAttachmentUrl("/api/uploads/interview/eval.pdf");

        InterviewEvaluation saved = interviewEvaluationService.submit(request);

        assertEquals(Long.valueOf(88L), saved.getEvaluationId());
        assertEquals(InterviewEvaluationStatusEnum.PENDING_OUTSOURCING.name(), saved.getApprovalStatus());
        verify(workflowProcessLogMapper).insert(any());
    }

    @Test
    void approveByOutsourcingManagerMovesToRoomManagerStage() {
        InterviewEvaluation evaluation = new InterviewEvaluation();
        evaluation.setEvaluationId(88L);
        evaluation.setApprovalStatus(InterviewEvaluationStatusEnum.PENDING_OUTSOURCING.name());
        evaluation.setCurrentApprovalLevel(1);
        evaluation.setInterviewerId("2001");
        evaluation.setInterviewerDepartment("基础业务开发团队 / 办公系统开发室");
        when(interviewEvaluationMapper.selectByPrimaryKey(88L)).thenReturn(evaluation);

        User manager = user("1002", "外包岗", "外包招聘管理岗", "人力资源团队");
        when(userMapper.getUserById("1002")).thenReturn(manager);
        when(interviewPermissionService.normalizeRoles(manager, "外包招聘管理岗"))
            .thenReturn(Set.of(InterviewPermissionService.ROLE_OUTSOURCING_MANAGER));
        when(interviewPermissionService.isSuperAdmin("1002", Set.of(InterviewPermissionService.ROLE_OUTSOURCING_MANAGER)))
            .thenReturn(false);
        when(interviewPermissionService.hasRole(Set.of(InterviewPermissionService.ROLE_OUTSOURCING_MANAGER), InterviewPermissionService.ROLE_OUTSOURCING_MANAGER))
            .thenReturn(true);

        WorkflowApproveRequest request = new WorkflowApproveRequest();
        request.setAction("APPROVE");
        request.setApprovalUserId("1002");
        request.setApprovalUserName("外包岗");
        request.setApprovalUserRole("外包招聘管理岗");
        request.setApprovalComment("通过");

        interviewEvaluationService.approve(88L, request);

        assertEquals(InterviewEvaluationStatusEnum.PENDING_ROOM_MANAGER.name(), evaluation.getApprovalStatus());
        assertEquals(Integer.valueOf(2), evaluation.getCurrentApprovalLevel());
        verify(interviewEvaluationApprovalHistoryMapper).insert(any());
        verify(workflowProcessLogMapper, times(2)).insert(any());
        verify(interviewEvaluationMapper).updateByPrimaryKey(evaluation);
    }

    @Test
    void approveByRoomManagerCompletesFlowAndSendsMessage() throws Exception {
        InterviewEvaluation evaluation = new InterviewEvaluation();
        evaluation.setEvaluationId(90L);
        evaluation.setApprovalStatus(InterviewEvaluationStatusEnum.PENDING_ROOM_MANAGER.name());
        evaluation.setCurrentApprovalLevel(2);
        evaluation.setInterviewerId("2001");
        evaluation.setInterviewerName("面试官A");
        evaluation.setInterviewerDepartment("基础业务开发团队 / 办公系统开发室");
        evaluation.setCandidateName("候选人乙");
        evaluation.setEntryLevelSuggestion("P7");
        evaluation.setInterviewDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2026-03-13 16:00:00"));
        when(interviewEvaluationMapper.selectByPrimaryKey(90L)).thenReturn(evaluation);

        User roomManager = user("3001", "室经理A", "室经理", "基础业务开发团队 / 办公系统开发室");
        when(userMapper.getUserById("3001")).thenReturn(roomManager);
        when(interviewPermissionService.normalizeRoles(roomManager, "室经理"))
            .thenReturn(Set.of(InterviewPermissionService.ROLE_ROOM_MANAGER));
        when(interviewPermissionService.isSuperAdmin("3001", Set.of(InterviewPermissionService.ROLE_ROOM_MANAGER)))
            .thenReturn(false);
        when(interviewPermissionService.hasRole(Set.of(InterviewPermissionService.ROLE_ROOM_MANAGER), InterviewPermissionService.ROLE_ROOM_MANAGER))
            .thenReturn(true);
        when(userMapper.getActiveRoomManagerByDepartmentAndRoleKeyword("办公系统开发室", "室经理")).thenReturn(roomManager);

        WorkflowApproveRequest request = new WorkflowApproveRequest();
        request.setAction("APPROVE");
        request.setApprovalUserId("3001");
        request.setApprovalUserName("室经理A");
        request.setApprovalUserRole("室经理");
        request.setApprovalComment("通过");

        interviewEvaluationService.approve(90L, request);

        assertEquals(InterviewEvaluationStatusEnum.APPROVED.name(), evaluation.getApprovalStatus());
        assertEquals(Integer.valueOf(3), evaluation.getCurrentApprovalLevel());
        verify(workflowProcessLogMapper, times(2)).insert(any());
        verify(interviewMessageService).sendEvaluationCompletedMessage(
            eq("2001"), eq("3001"), eq("面试官A"), eq("基础业务开发团队 / 办公系统开发室"), eq("候选人乙"), eq("P7"), eq("2026-03-13 16:00:00")
        );
    }

    @Test
    void approveAtRoomManagerStageUsesCurrentApproverAsNotificationRecipient() throws Exception {
        InterviewEvaluation evaluation = new InterviewEvaluation();
        evaluation.setEvaluationId(92L);
        evaluation.setApprovalStatus(InterviewEvaluationStatusEnum.PENDING_ROOM_MANAGER.name());
        evaluation.setCurrentApprovalLevel(2);
        evaluation.setInterviewerId("1008");
        evaluation.setInterviewerName("陈秀芳");
        evaluation.setInterviewerDepartment("办公系统开发室");
        evaluation.setCandidateName("候选人丙");
        evaluation.setEntryLevelSuggestion("P6");
        evaluation.setInterviewDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2026-03-13 16:30:00"));
        when(interviewEvaluationMapper.selectByPrimaryKey(92L)).thenReturn(evaluation);

        User approver = user("1001", "系统用户", "系统管理员", "人事部");
        when(userMapper.getUserById("1001")).thenReturn(approver);
        when(interviewPermissionService.normalizeRoles(approver, "室经理"))
            .thenReturn(Set.of(InterviewPermissionService.ROLE_SUPER_ADMIN));
        when(interviewPermissionService.isSuperAdmin("1001", Set.of(InterviewPermissionService.ROLE_SUPER_ADMIN)))
            .thenReturn(true);

        WorkflowApproveRequest request = new WorkflowApproveRequest();
        request.setAction("APPROVE");
        request.setApprovalUserId("1001");
        request.setApprovalUserName("系统用户");
        request.setApprovalUserRole("室经理");
        request.setApprovalComment("通过");

        interviewEvaluationService.approve(92L, request);

        verify(interviewMessageService).sendEvaluationCompletedMessage(
            eq("1008"), eq("1001"), eq("陈秀芳"), eq("办公系统开发室"), eq("候选人丙"), eq("P6"), eq("2026-03-13 16:30:00")
        );
    }

    @Test
    void rejectSendsBackToInterviewer() {
        InterviewEvaluation evaluation = new InterviewEvaluation();
        evaluation.setEvaluationId(91L);
        evaluation.setApprovalStatus(InterviewEvaluationStatusEnum.PENDING_OUTSOURCING.name());
        evaluation.setCurrentApprovalLevel(1);
        evaluation.setInterviewerId("2001");
        when(interviewEvaluationMapper.selectByPrimaryKey(91L)).thenReturn(evaluation);

        User manager = user("1002", "外包岗", "外包招聘管理岗", "人力资源团队");
        when(userMapper.getUserById("1002")).thenReturn(manager);
        when(interviewPermissionService.normalizeRoles(manager, "外包招聘管理岗"))
            .thenReturn(Set.of(InterviewPermissionService.ROLE_OUTSOURCING_MANAGER));
        when(interviewPermissionService.isSuperAdmin("1002", Set.of(InterviewPermissionService.ROLE_OUTSOURCING_MANAGER)))
            .thenReturn(false);
        when(interviewPermissionService.hasRole(Set.of(InterviewPermissionService.ROLE_OUTSOURCING_MANAGER), InterviewPermissionService.ROLE_OUTSOURCING_MANAGER))
            .thenReturn(true);

        WorkflowApproveRequest request = new WorkflowApproveRequest();
        request.setAction("REJECT");
        request.setApprovalUserId("1002");
        request.setApprovalUserName("外包岗");
        request.setApprovalUserRole("外包招聘管理岗");
        request.setApprovalComment("驳回");

        interviewEvaluationService.approve(91L, request);

        assertEquals(InterviewEvaluationStatusEnum.REJECTED.name(), evaluation.getApprovalStatus());
        assertEquals(Integer.valueOf(1), evaluation.getCurrentApprovalLevel());
        verify(interviewEvaluationApprovalHistoryMapper).insert(any());
        verify(workflowProcessLogMapper).insert(any());
        verify(interviewMessageService).sendEvaluationRejectedMessage("2001");
    }

    @Test
    void submitRejectsWhenInterviewTimeNotConfirmed() {
        User interviewer = user("2001", "面试官A", "面试官", "基础业务开发团队 / 办公系统开发室");
        when(userMapper.getUserById("2001")).thenReturn(interviewer);
        when(interviewPermissionService.normalizeRoles(interviewer, "面试官"))
            .thenReturn(Set.of(InterviewPermissionService.ROLE_INTERVIEWER));
        when(interviewPermissionService.hasRole(Set.of(InterviewPermissionService.ROLE_INTERVIEWER), InterviewPermissionService.ROLE_INTERVIEWER))
            .thenReturn(true);

        Resume resume = new Resume();
        resume.setResumeId(1L);
        when(resumeMapper.selectByPrimaryKey(1L)).thenReturn(resume);

        ResumeDispatch confirmed = new ResumeDispatch();
        confirmed.setDispatchId(9L);
        confirmed.setInterviewerId("2001");
        confirmed.setInterviewerName("面试官A");
        confirmed.setConfirmedInterviewTime(null);
        when(resumeDispatchMapper.selectConfirmedByResumeId(1L)).thenReturn(confirmed);

        SubmitInterviewEvaluationRequest request = new SubmitInterviewEvaluationRequest();
        request.setResumeId(1L);
        request.setOperatorUserId("2001");
        request.setOperatorRole("面试官");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> interviewEvaluationService.submit(request));
        assertEquals("面试时间未确认，不能发起面试评价", ex.getMessage());
    }

    private User user(String userId, String realName, String position, String department) {
        User user = new User();
        user.setUserId(userId);
        user.setRealName(realName);
        user.setPosition(position);
        user.setDepartment(department);
        user.setStatus("ACTIVE");
        return user;
    }
}
