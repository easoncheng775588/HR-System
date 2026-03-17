package com.hr.service.impl;

import com.hr.entity.ArrivalConfirmation;
import com.hr.entity.ArrivalConfirmationStatusEnum;
import com.hr.entity.EntryRecord;
import com.hr.entity.OrgUnit;
import com.hr.entity.Resume;
import com.hr.entity.Supplier;
import com.hr.entity.SubmitArrivalConfirmationRequest;
import com.hr.entity.User;
import com.hr.entity.WorkflowApproveRequest;
import com.hr.mapper.ArrivalConfirmationApprovalHistoryMapper;
import com.hr.mapper.ArrivalConfirmationMapper;
import com.hr.mapper.EntryRecordMapper;
import com.hr.mapper.OrgUnitMapper;
import com.hr.mapper.RecruitmentRequestMapper;
import com.hr.mapper.ResumeMapper;
import com.hr.mapper.UserMapper;
import com.hr.mapper.WorkflowProcessLogMapper;
import com.hr.service.InterviewPermissionService;
import com.hr.service.SupplierService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArrivalConfirmationServiceImplTest {

    @Mock
    private ArrivalConfirmationMapper arrivalConfirmationMapper;
    @Mock
    private ArrivalConfirmationApprovalHistoryMapper arrivalConfirmationApprovalHistoryMapper;
    @Mock
    private EntryRecordMapper entryRecordMapper;
    @Mock
    private ResumeMapper resumeMapper;
    @Mock
    private RecruitmentRequestMapper recruitmentRequestMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private OrgUnitMapper orgUnitMapper;
    @Mock
    private SupplierService supplierService;
    @Mock
    private InterviewPermissionService interviewPermissionService;
    @Mock
    private WorkflowProcessLogMapper workflowProcessLogMapper;

    @InjectMocks
    private ArrivalConfirmationServiceImpl arrivalConfirmationService;

    @Test
    void submitCreatesPendingSupplierHrFlow() throws Exception {
        User outsourcingManager = user("1002", "外包岗", "人力资源团队", "");
        when(userMapper.getUserById("1002")).thenReturn(outsourcingManager);
        when(interviewPermissionService.normalizeRoles(outsourcingManager, "外包招聘管理岗"))
            .thenReturn(Set.of(InterviewPermissionService.ROLE_OUTSOURCING_MANAGER));
        when(interviewPermissionService.isSuperAdmin("1002", Set.of(InterviewPermissionService.ROLE_OUTSOURCING_MANAGER)))
            .thenReturn(false);
        when(interviewPermissionService.hasRole(Set.of(InterviewPermissionService.ROLE_OUTSOURCING_MANAGER), InterviewPermissionService.ROLE_OUTSOURCING_MANAGER))
            .thenReturn(true);

        EntryRecord entryRecord = new EntryRecord();
        entryRecord.setEntryRecordId(5L);
        entryRecord.setResumeId(9L);
        entryRecord.setSourceRecruitmentRequestId(11L);
        entryRecord.setCandidateName("候选人甲");
        entryRecord.setHiredDepartment("办公系统开发室");
        when(entryRecordMapper.selectByPrimaryKey(5L)).thenReturn(entryRecord);
        when(arrivalConfirmationMapper.selectLatestByEntryRecordId(5L)).thenReturn(null);

        Resume resume = new Resume();
        resume.setResumeId(9L);
        resume.setCandidateName("候选人甲");
        resume.setSupplierName("供应商A");
        resume.setCreateUserId("1003");
        resume.setCreateUserName("供应商HR甲");
        when(resumeMapper.selectByPrimaryKey(9L)).thenReturn(resume);

        Supplier supplier = new Supplier();
        supplier.setSupplierId(3L);
        supplier.setSupplierName("供应商A");
        supplier.setStatus("ACTIVE");
        supplier.setUserIds(Collections.singletonList("1003"));
        when(supplierService.getSupplierById(3L)).thenReturn(supplier);

        OrgUnit orgUnit = new OrgUnit();
        orgUnit.setUnitName("办公系统开发室");
        when(orgUnitMapper.getByUnitName("办公系统开发室")).thenReturn(orgUnit);

        User supplierHr = user("1003", "供应商HR甲", "供应商A", "");
        when(userMapper.getUserById("1003")).thenReturn(supplierHr);

        User roomManager = user("1007", "室经理甲", "办公系统开发室", "");
        when(userMapper.getUserById("1007")).thenReturn(roomManager);
        when(interviewPermissionService.normalizeRoles(roomManager, InterviewPermissionService.ROLE_ROOM_MANAGER))
            .thenReturn(Set.of(InterviewPermissionService.ROLE_ROOM_MANAGER));
        when(interviewPermissionService.hasRole(Set.of(InterviewPermissionService.ROLE_ROOM_MANAGER), InterviewPermissionService.ROLE_ROOM_MANAGER))
            .thenReturn(true);

        User hrTeamManager = user("1005", "HR团队经理", "人力资源团队", "人力资源团队");
        when(userMapper.getActiveUsersByRoleKeyword(InterviewPermissionService.ROLE_TEAM_MANAGER))
            .thenReturn(Collections.singletonList(hrTeamManager));

        when(arrivalConfirmationMapper.insert(any(ArrivalConfirmation.class))).thenAnswer(invocation -> {
            ArrivalConfirmation entity = invocation.getArgument(0);
            entity.setArrivalConfirmationId(21L);
            return 1;
        });
        when(arrivalConfirmationMapper.selectByPrimaryKey(21L)).thenAnswer(invocation -> {
            ArrivalConfirmation confirmation = new ArrivalConfirmation();
            confirmation.setArrivalConfirmationId(21L);
            confirmation.setApprovalStatus(ArrivalConfirmationStatusEnum.PENDING_SUPPLIER_HR.name());
            return confirmation;
        });

        SubmitArrivalConfirmationRequest request = new SubmitArrivalConfirmationRequest();
        request.setEntryRecordId(5L);
        request.setSupplierId(3L);
        request.setSupplierHrUserId("1003");
        request.setTargetOrgUnitName("办公系统开发室");
        request.setRoomManagerUserId("1007");
        request.setEntryDate("2026-03-18");
        request.setPositionLevel("P6");
        request.setOperatorUserId("1002");
        request.setOperatorUserName("外包岗");
        request.setOperatorUserRole("外包招聘管理岗");

        ArrivalConfirmation saved = arrivalConfirmationService.submit(request);

        assertEquals(Long.valueOf(21L), saved.getArrivalConfirmationId());
        assertEquals(ArrivalConfirmationStatusEnum.PENDING_SUPPLIER_HR.name(), saved.getApprovalStatus());
        verify(workflowProcessLogMapper).insert(any());
    }

    @Test
    void approveByRoomManagerCompletesFlowAndBackwritesEntryRecord() throws Exception {
        ArrivalConfirmation confirmation = new ArrivalConfirmation();
        confirmation.setArrivalConfirmationId(21L);
        confirmation.setEntryRecordId(5L);
        confirmation.setSupplierHrUserId("1003");
        confirmation.setHrTeamManagerUserId("1005");
        confirmation.setRoomManagerUserId("1007");
        confirmation.setApprovalStatus(ArrivalConfirmationStatusEnum.PENDING_ROOM_MANAGER.name());
        confirmation.setCurrentApprovalLevel(3);
        confirmation.setEntryDate(new SimpleDateFormat("yyyy-MM-dd").parse("2026-03-18"));
        when(arrivalConfirmationMapper.selectByPrimaryKey(21L)).thenReturn(confirmation);

        User roomManager = user("1007", "室经理甲", "办公系统开发室", "");
        when(userMapper.getUserById("1007")).thenReturn(roomManager);
        when(interviewPermissionService.normalizeRoles(roomManager, "室经理"))
            .thenReturn(Set.of(InterviewPermissionService.ROLE_ROOM_MANAGER));
        when(interviewPermissionService.isSuperAdmin("1007", Set.of(InterviewPermissionService.ROLE_ROOM_MANAGER)))
            .thenReturn(false);

        EntryRecord entryRecord = new EntryRecord();
        entryRecord.setEntryRecordId(5L);
        entryRecord.setArrivalStatus("");
        when(entryRecordMapper.selectByPrimaryKey(5L)).thenReturn(entryRecord);

        WorkflowApproveRequest request = new WorkflowApproveRequest();
        request.setAction("APPROVE");
        request.setApprovalUserId("1007");
        request.setApprovalUserName("室经理甲");
        request.setApprovalUserRole("室经理");
        request.setApprovalComment("通过");

        arrivalConfirmationService.approve(21L, request);

        assertEquals(ArrivalConfirmationStatusEnum.APPROVED.name(), confirmation.getApprovalStatus());
        assertEquals(Integer.valueOf(4), confirmation.getCurrentApprovalLevel());
        assertEquals("正常入场", entryRecord.getArrivalStatus());
        assertEquals(new SimpleDateFormat("yyyy-MM-dd").parse("2026-03-18"), entryRecord.getActualEntryDate());
        verify(arrivalConfirmationApprovalHistoryMapper).insert(any());
        verify(entryRecordMapper).updateByPrimaryKey(entryRecord);
        verify(workflowProcessLogMapper, times(2)).insert(any());
    }

    @Test
    void getListReturnsSupplierHrOwnRecordsOnly() {
        User supplierHr = user("1003", "供应商HR甲", "供应商A", "");
        when(userMapper.getUserById("1003")).thenReturn(supplierHr);
        when(interviewPermissionService.normalizeRoles(supplierHr, "供应商HR"))
            .thenReturn(Set.of(InterviewPermissionService.ROLE_SUPPLIER_HR));
        when(interviewPermissionService.isSuperAdmin("1003", Set.of(InterviewPermissionService.ROLE_SUPPLIER_HR)))
            .thenReturn(false);
        when(interviewPermissionService.hasRole(Set.of(InterviewPermissionService.ROLE_SUPPLIER_HR), InterviewPermissionService.ROLE_OUTSOURCING_MANAGER))
            .thenReturn(false);
        when(interviewPermissionService.hasRole(Set.of(InterviewPermissionService.ROLE_SUPPLIER_HR), InterviewPermissionService.ROLE_ROOM_MANAGER))
            .thenReturn(false);
        when(interviewPermissionService.hasRole(Set.of(InterviewPermissionService.ROLE_SUPPLIER_HR), InterviewPermissionService.ROLE_TEAM_MANAGER))
            .thenReturn(false);
        when(interviewPermissionService.hasRole(Set.of(InterviewPermissionService.ROLE_SUPPLIER_HR), InterviewPermissionService.ROLE_SUPPLIER_HR))
            .thenReturn(true);

        ArrivalConfirmation a = new ArrivalConfirmation();
        a.setArrivalConfirmationId(1L);
        a.setCandidateName("候选人甲");
        a.setSupplierHrUserId("1003");
        a.setSupplierHrUserName("供应商HR甲");
        a.setApprovalStatus(ArrivalConfirmationStatusEnum.PENDING_SUPPLIER_HR.name());
        ArrivalConfirmation b = new ArrivalConfirmation();
        b.setArrivalConfirmationId(2L);
        b.setCandidateName("候选人乙");
        b.setSupplierHrUserId("1012");
        b.setApprovalStatus(ArrivalConfirmationStatusEnum.PENDING_SUPPLIER_HR.name());
        when(arrivalConfirmationMapper.selectAll()).thenReturn(List.of(a, b));

        assertEquals(1, arrivalConfirmationService.getList("1003", "供应商HR").size());
        assertEquals(Long.valueOf(1L), arrivalConfirmationService.getList("1003", "供应商HR").get(0).getArrivalConfirmationId());
    }

    @Test
    void submitRejectsWhenSupplierHrDoesNotMatchResumeCreator() {
        User outsourcingManager = user("1002", "外包岗", "人力资源团队", "");
        when(userMapper.getUserById("1002")).thenReturn(outsourcingManager);
        when(interviewPermissionService.normalizeRoles(outsourcingManager, "外包招聘管理岗"))
            .thenReturn(Set.of(InterviewPermissionService.ROLE_OUTSOURCING_MANAGER));
        when(interviewPermissionService.isSuperAdmin("1002", Set.of(InterviewPermissionService.ROLE_OUTSOURCING_MANAGER)))
            .thenReturn(false);
        when(interviewPermissionService.hasRole(Set.of(InterviewPermissionService.ROLE_OUTSOURCING_MANAGER), InterviewPermissionService.ROLE_OUTSOURCING_MANAGER))
            .thenReturn(true);

        EntryRecord entryRecord = new EntryRecord();
        entryRecord.setEntryRecordId(5L);
        entryRecord.setResumeId(9L);
        when(entryRecordMapper.selectByPrimaryKey(5L)).thenReturn(entryRecord);
        when(arrivalConfirmationMapper.selectLatestByEntryRecordId(5L)).thenReturn(null);

        Resume resume = new Resume();
        resume.setResumeId(9L);
        resume.setCreateUserId("1003");
        when(resumeMapper.selectByPrimaryKey(9L)).thenReturn(resume);

        Supplier supplier = new Supplier();
        supplier.setSupplierId(3L);
        supplier.setUserIds(Collections.singletonList("1003"));
        when(supplierService.getSupplierById(3L)).thenReturn(supplier);

        SubmitArrivalConfirmationRequest request = new SubmitArrivalConfirmationRequest();
        request.setEntryRecordId(5L);
        request.setSupplierId(3L);
        request.setSupplierHrUserId("9999");
        request.setOperatorUserId("1002");
        request.setOperatorUserRole("外包招聘管理岗");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> arrivalConfirmationService.submit(request));
        assertEquals("供应商HR必须为该候选人简历创建人", ex.getMessage());
    }

    private User user(String userId, String realName, String department, String teamName) {
        User user = new User();
        user.setUserId(userId);
        user.setRealName(realName);
        user.setDepartment(department);
        user.setTeamName(teamName);
        user.setStatus("ACTIVE");
        return user;
    }
}
