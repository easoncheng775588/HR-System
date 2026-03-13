package com.hr.service.impl;

import com.hr.entity.DemandRequirement;
import com.hr.entity.InterviewerChoiceRequest;
import com.hr.entity.Resume;
import com.hr.entity.ResumeDispatch;
import com.hr.entity.User;
import com.hr.mapper.DemandRequirementMapper;
import com.hr.mapper.RecruitmentRequestMapper;
import com.hr.mapper.ResumeDispatchMapper;
import com.hr.mapper.ResumeMapper;
import com.hr.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResumeServiceImplTest {

    @Mock
    private ResumeMapper resumeMapper;

    @Mock
    private ResumeDispatchMapper resumeDispatchMapper;

    @Mock
    private DemandRequirementMapper demandRequirementMapper;

    @Mock
    private RecruitmentRequestMapper recruitmentRequestMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private ResumeServiceImpl resumeService;

    @Test
    void saveResumeRejectsCreateWhenOperatorMissing() {
        Resume resume = new Resume();
        resume.setCandidateName("候选人A");
        resume.setRelatedRequestIds("1");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> resumeService.saveResume(resume));
        assertEquals("提交人不能为空", exception.getMessage());
    }

    @Test
    void saveResumeSupplierCreateDefaultsStatusAndSupplier() {
        Resume resume = new Resume();
        resume.setCandidateName("候选人A");
        resume.setRelatedRequestIds("1");
        resume.setCreateUserId("9001");
        resume.setCreateUserName("供应商HR");
        resume.setUpdateUserId("9001");
        resume.setUpdateUserName("供应商HR");

        User supplierHr = user("9001", "供应商HR", "供应商HR");
        when(userMapper.getUserById("9001")).thenReturn(supplierHr);
        when(userMapper.getRoleNamesByUserId("9001")).thenReturn(Collections.singletonList("供应商HR"));
        when(resumeMapper.selectSupplierNameByUserId("9001")).thenReturn("供应商A");

        AtomicReference<Resume> insertedRef = new AtomicReference<>();
        when(resumeMapper.insert(any(Resume.class))).thenAnswer(invocation -> {
            Resume inserting = invocation.getArgument(0);
            inserting.setResumeId(8L);
            insertedRef.set(inserting);
            return 1;
        });
        when(resumeMapper.selectByPrimaryKey(8L)).thenAnswer(invocation -> insertedRef.get());

        Resume saved = resumeService.saveResume(resume);

        assertEquals("待审核", saved.getStatus());
        assertEquals("供应商A", saved.getSupplierName());
        assertEquals("候选人A", saved.getApplicantName());
    }

    @Test
    void saveResumeSupplierUpdateResetsStatusToPendingReview() {
        Resume existing = new Resume();
        existing.setResumeId(9L);
        existing.setCandidateName("候选人B");
        existing.setRelatedRequestIds("15");
        existing.setCreateUserId("9001");
        existing.setCreateUserName("供应商HR");
        existing.setStatus("需修改简历材料");

        Resume updatePayload = new Resume();
        updatePayload.setResumeId(9L);
        updatePayload.setCandidateName("候选人B-更新");
        updatePayload.setRelatedRequestIds("15");
        updatePayload.setUpdateUserId("9001");
        updatePayload.setUpdateUserName("供应商HR");

        User supplierHr = user("9001", "供应商HR", "供应商HR");
        when(userMapper.getUserById("9001")).thenReturn(supplierHr);
        when(userMapper.getRoleNamesByUserId("9001")).thenReturn(Collections.singletonList("供应商HR"));
        when(resumeMapper.selectByPrimaryKey(9L)).thenReturn(existing);

        AtomicReference<Resume> updatedRef = new AtomicReference<>();
        when(resumeMapper.updateByPrimaryKey(any(Resume.class))).thenAnswer(invocation -> {
            updatedRef.set(invocation.getArgument(0));
            return 1;
        });
        when(resumeMapper.selectByPrimaryKey(9L)).thenAnswer(invocation -> {
            Resume updated = updatedRef.get();
            if (updated == null) {
                return existing;
            }
            Resume result = new Resume();
            result.setResumeId(updated.getResumeId());
            result.setStatus(updated.getStatus());
            result.setCreateUserId(updated.getCreateUserId());
            result.setCreateUserName(updated.getCreateUserName());
            result.setUpdateUserId(updated.getUpdateUserId());
            result.setUpdateUserName(updated.getUpdateUserName());
            return result;
        });

        Resume saved = resumeService.saveResume(updatePayload);
        assertEquals("待审核", saved.getStatus());
    }

    @Test
    void deleteByIdRejectsWhenOperatorMissing() {
        Resume resume = new Resume();
        resume.setResumeId(1L);
        resume.setCreateUserId("9001");
        resume.setStatus("待简历初筛");
        when(resumeMapper.selectByPrimaryKey(1L)).thenReturn(resume);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> resumeService.deleteById(1L, null, null));
        assertEquals("操作人不能为空", exception.getMessage());
        verify(resumeMapper, never()).deleteByPrimaryKey(any(Long.class));
    }

    @Test
    void getAllSupplierHrOnlySeesOwnResumes() {
        User supplierHr = user("9001", "供应商HR", "供应商HR");
        when(userMapper.getUserById("9001")).thenReturn(supplierHr);
        when(userMapper.getRoleNamesByUserId("9001")).thenReturn(Collections.singletonList("供应商HR"));

        Resume own = new Resume();
        own.setResumeId(1L);
        own.setCreateUserId("9001");
        own.setStatus("待简历初筛");

        Resume other = new Resume();
        other.setResumeId(2L);
        other.setCreateUserId("9002");
        other.setStatus("待简历初筛");

        when(resumeMapper.selectAll()).thenReturn(Arrays.asList(own, other));
        when(resumeDispatchMapper.selectByResumeIds(Arrays.asList(1L, 2L))).thenReturn(Collections.emptyList());

        List<Resume> result = resumeService.getAll("9001", null);

        assertEquals(1, result.size());
        assertEquals(Long.valueOf(1L), result.get(0).getResumeId());
    }

    @Test
    void interviewerCannotConfirmWhenAlreadyConfirmedByOther() {
        User interviewer = user("7001", "面试官A", "面试官");
        when(userMapper.getUserById("7001")).thenReturn(interviewer);
        when(userMapper.getRoleNamesByUserId("7001")).thenReturn(Collections.singletonList("面试官"));

        Resume resume = new Resume();
        resume.setResumeId(10L);
        when(resumeMapper.selectByPrimaryKey(10L)).thenReturn(resume);

        ResumeDispatch mine = dispatch(10L, "7001", "PENDING");
        ResumeDispatch otherConfirmed = dispatch(10L, "7002", "CONFIRMED");
        when(resumeDispatchMapper.selectByResumeId(10L)).thenReturn(Arrays.asList(mine, otherConfirmed));

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> resumeService.interviewerChoice(10L, confirmRequest("7001", "面试官A", "面试官"))
        );

        assertEquals("该简历已被其他面试官确认", exception.getMessage());
        verify(resumeDispatchMapper, never()).updateStatusByResumeAndInterviewer(
            any(Long.class), any(String.class), any(String.class), any(), any(String.class), any(String.class)
        );
    }

    @Test
    void dispatchDemandOptionsOnlyReturnsEmptyDemandStatus() {
        User outsourcing = user("1002", "外包招聘管理岗", "外包招聘管理岗");
        when(userMapper.getUserById("1002")).thenReturn(outsourcing);
        when(userMapper.getRoleNamesByUserId("1002")).thenReturn(Collections.singletonList("外包招聘管理岗"));

        DemandRequirement emptyStatus = demand(1L, 11L, "系统研发岗 / A室", null);
        DemandRequirement blankStatus = demand(2L, 12L, "系统研发岗 / B室", " ");
        DemandRequirement dispatched = demand(3L, 13L, "系统研发岗 / C室", "已分发");
        when(demandRequirementMapper.selectAll()).thenReturn(Arrays.asList(emptyStatus, blankStatus, dispatched));

        List<Map<String, Object>> options = resumeService.getDispatchDemandOptions("1002", "外包招聘管理岗");

        assertEquals(2, options.size());
        assertEquals(1L, options.get(0).get("demandId"));
        assertEquals(2L, options.get(1).get("demandId"));
    }

    @Test
    void screenResumeRejectsWhenStatusIsNotPendingReview() {
        User outsourcing = user("1002", "外包招聘管理岗", "外包招聘管理岗");
        when(userMapper.getUserById("1002")).thenReturn(outsourcing);
        when(userMapper.getRoleNamesByUserId("1002")).thenReturn(Collections.singletonList("外包招聘管理岗"));

        Resume resume = new Resume();
        resume.setResumeId(30L);
        resume.setStatus("已通过简历初筛");
        when(resumeMapper.selectByPrimaryKey(30L)).thenReturn(resume);

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> resumeService.screenResume(30L, "PASS", "1002", "外包岗", "外包招聘管理岗")
        );

        assertEquals("仅待审核简历可执行初筛", exception.getMessage());
    }

    @Test
    void interviewerChoiceFlagsReflectConfirmedByOther() {
        User interviewer = user("7001", "面试官A", "面试官");
        when(userMapper.getUserById("7001")).thenReturn(interviewer);
        when(userMapper.getRoleNamesByUserId("7001")).thenReturn(Collections.singletonList("面试官"));

        Resume resume = new Resume();
        resume.setResumeId(20L);
        resume.setCreateUserId("9001");
        resume.setStatus("已通过简历初筛");
        when(resumeMapper.selectAll()).thenReturn(Collections.singletonList(resume));

        ResumeDispatch minePending = dispatch(20L, "7001", "PENDING");
        ResumeDispatch otherConfirmed = dispatch(20L, "7002", "CONFIRMED");
        when(resumeDispatchMapper.selectByResumeIds(Collections.singletonList(20L)))
            .thenReturn(Arrays.asList(minePending, otherConfirmed));

        List<Resume> result = resumeService.getAll("7001", "面试官");

        assertEquals(1, result.size());
        assertEquals("待选择", result.get(0).getCurrentInterviewerChoiceStatus());
        assertFalse(Boolean.TRUE.equals(result.get(0).getCanInterviewerConfirm()));
        assertFalse(Boolean.TRUE.equals(result.get(0).getCanInterviewerAbandon()));
        assertEquals("7002", result.get(0).getConfirmedInterviewerId());
    }

    @Test
    void interviewerChoiceFlagsAllowPendingInterviewerActionsWhenNoOtherConfirmation() {
        User interviewer = user("7001", "面试官A", "面试官");
        when(userMapper.getUserById("7001")).thenReturn(interviewer);
        when(userMapper.getRoleNamesByUserId("7001")).thenReturn(Collections.singletonList("面试官"));

        Resume resume = new Resume();
        resume.setResumeId(21L);
        resume.setCreateUserId("9001");
        resume.setStatus("已通过简历初筛");
        when(resumeMapper.selectAll()).thenReturn(Collections.singletonList(resume));

        ResumeDispatch minePending = dispatch(21L, "7001", "PENDING");
        when(resumeDispatchMapper.selectByResumeIds(Collections.singletonList(21L)))
            .thenReturn(Collections.singletonList(minePending));

        List<Resume> result = resumeService.getAll("7001", "面试官");

        assertEquals(1, result.size());
        assertTrue(Boolean.TRUE.equals(result.get(0).getCanInterviewerConfirm()));
        assertTrue(Boolean.TRUE.equals(result.get(0).getCanInterviewerAbandon()));
    }

    @Test
    void getAllUsesDispatchAssignmentAsInterviewerRoleFallback() {
        User interviewerWithoutRoleText = user("7009", "面试官缺省角色", null);
        when(userMapper.getUserById("7009")).thenReturn(interviewerWithoutRoleText);
        when(userMapper.getRoleNamesByUserId("7009")).thenReturn(Collections.emptyList());

        Resume resume = new Resume();
        resume.setResumeId(31L);
        resume.setCreateUserId("9001");
        resume.setStatus("已通过简历初筛");
        when(resumeMapper.selectAll()).thenReturn(Collections.singletonList(resume));

        ResumeDispatch minePending = dispatch(31L, "7009", "PENDING");
        when(resumeDispatchMapper.selectByInterviewerId("7009")).thenReturn(Collections.singletonList(minePending));
        when(resumeDispatchMapper.selectByResumeIds(Collections.singletonList(31L)))
            .thenReturn(Collections.singletonList(minePending));

        List<Resume> result = resumeService.getAll("7009", "");

        assertEquals(1, result.size());
        assertEquals(Long.valueOf(31L), result.get(0).getResumeId());
        assertTrue(Boolean.TRUE.equals(result.get(0).getCanInterviewerConfirm()));
    }

    private User user(String userId, String realName, String position) {
        User user = new User();
        user.setUserId(userId);
        user.setRealName(realName);
        user.setPosition(position);
        user.setStatus("ACTIVE");
        return user;
    }

    private ResumeDispatch dispatch(Long resumeId, String interviewerId, String status) {
        ResumeDispatch dispatch = new ResumeDispatch();
        dispatch.setResumeId(resumeId);
        dispatch.setInterviewerId(interviewerId);
        dispatch.setInterviewerName(interviewerId);
        dispatch.setDispatchStatus(status);
        return dispatch;
    }

    private DemandRequirement demand(Long id, Long requestId, String label, String demandStatus) {
        DemandRequirement demand = new DemandRequirement();
        demand.setDemandId(id);
        demand.setSourceRecruitmentRequestId(requestId);
        demand.setPositionOrgName(label);
        demand.setDemandStatus(demandStatus);
        return demand;
    }

    private InterviewerChoiceRequest confirmRequest(String userId, String userName, String role) {
        InterviewerChoiceRequest request = new InterviewerChoiceRequest();
        request.setChoice("CONFIRM");
        request.setOperatorUserId(userId);
        request.setOperatorUserName(userName);
        request.setOperatorRole(role);
        request.setInterviewMethod("ONLINE");
        request.setMeetingNo("test-1001");
        request.setAvailableStartTime("2026-03-13 10:00:00");
        request.setAvailableEndTime("2026-03-13 11:00:00");
        return request;
    }
}
