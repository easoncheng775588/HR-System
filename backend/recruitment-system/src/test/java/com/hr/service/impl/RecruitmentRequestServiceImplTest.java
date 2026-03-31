package com.hr.service.impl;

import com.hr.entity.RecruitmentRequest;
import com.hr.entity.OrgUnit;
import com.hr.entity.Message;
import com.hr.entity.Staffing;
import com.hr.entity.User;
import com.hr.mapper.ApprovalHistoryMapper;
import com.hr.mapper.OrgUnitMapper;
import com.hr.mapper.RecruitmentRequestMapper;
import com.hr.mapper.StaffingMapper;
import com.hr.mapper.UserMapper;
import com.hr.mapper.WorkflowProcessLogMapper;
import com.hr.service.DemandManagementService;
import com.hr.service.MessageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecruitmentRequestServiceImplTest {

    @Mock
    private RecruitmentRequestMapper recruitmentRequestMapper;

    @Mock
    private ApprovalHistoryMapper approvalHistoryMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private OrgUnitMapper orgUnitMapper;

    @Mock
    private StaffingMapper staffingMapper;

    @Mock
    private WorkflowProcessLogMapper workflowProcessLogMapper;

    @Mock
    private MessageService messageService;

    @Mock
    private DemandManagementService demandManagementService;

    @InjectMocks
    private RecruitmentRequestServiceImpl recruitmentRequestService;

    @Captor
    private ArgumentCaptor<RecruitmentRequest> requestCaptor;

    @Captor
    private ArgumentCaptor<Message> messageCaptor;

    @Test
    void saveDraftSetsLegacyStatusBeforeInsert() {
        RecruitmentRequest request = new RecruitmentRequest();
        request.setRequestTitle("草稿申请");
        request.setCreateUserId("2001");
        request.setCreateUserName("王室经理");

        when(userMapper.getUserById("2001")).thenReturn(activeUser("2001", "王室经理", "室经理", "零售平台开发室", "零售业务开发团队", "零售平台开发室"));
        when(userMapper.getRoleNamesByUserId("2001")).thenReturn(Collections.singletonList("室经理"));
        when(staffingMapper.getStaffingByOrgUnitName("零售平台开发室")).thenReturn(staffing("零售平台开发室", 8, 2));

        recruitmentRequestService.saveDraft(request);

        verify(recruitmentRequestMapper).insert(requestCaptor.capture());
        assertEquals("DRAFT", requestCaptor.getValue().getApprovalStatus());
        assertEquals("DRAFT", ReflectionTestUtils.getField(requestCaptor.getValue(), "status"));
    }

    @Test
    void submitRequestSetsDefaultApprovalStatusBeforeInsert() {
        RecruitmentRequest request = new RecruitmentRequest();
        request.setRequestTitle("Java开发");
        request.setRequestType("LEAVE");
        request.setTechnicalPlatform("Java");
        request.setCategory("社招");
        request.setSupplementCount(1);
        request.setUrgentRequirement("NO");
        request.setProposedLevel("P6");
        request.setExperienceYears("3年");
        request.setSkillRequirement("Spring Boot");
        request.setPositionResponsibility("负责后端开发");
        request.setCreateUserId("2001");
        request.setCreateUserName("王室经理");

        User submitter = activeUser("2001", "王室经理", "室经理", "零售平台开发室", "零售业务开发团队", "零售平台开发室");
        Staffing staffing = staffing("零售平台开发室", 8, 2);
        User teamManager = activeUser("2100", "赵团队经理", "团队经理", "零售业务开发团队", "零售业务开发团队", null);

        when(userMapper.getUserById("2001")).thenReturn(submitter);
        when(userMapper.getRoleNamesByUserId("2001")).thenReturn(Collections.singletonList("室经理"));
        when(staffingMapper.getStaffingByOrgUnitName("零售平台开发室")).thenReturn(staffing);
        when(orgUnitMapper.getByUnitName("零售平台开发室")).thenReturn(orgUnit("零售平台开发室", "GROUP", "零售业务开发团队"));
        when(userMapper.getActiveTeamManagerByDepartment("零售业务开发团队")).thenReturn(teamManager);

        when(recruitmentRequestMapper.insert(any(RecruitmentRequest.class))).thenAnswer(invocation -> {
            RecruitmentRequest inserted = invocation.getArgument(0);
            inserted.setRecruitmentRequestId(1L);
            return 1;
        });

        recruitmentRequestService.submitRequest(request);

        verify(recruitmentRequestMapper).insert(requestCaptor.capture());
        assertEquals("PENDING", requestCaptor.getValue().getApprovalStatus());
        assertEquals("零售业务开发团队 / 零售平台开发室", requestCaptor.getValue().getApplicationDepartment());
        assertEquals("零售平台开发室", requestCaptor.getValue().getOrgUnitName());
        assertEquals(Integer.valueOf(8), requestCaptor.getValue().getTotalRecruitmentCount());
        assertEquals(Integer.valueOf(2), requestCaptor.getValue().getVacancyCount());
        assertEquals("ROOM_MANAGER", requestCaptor.getValue().getSubmitterRoleType());
    }

    @Test
    void submitRequestRejectsNonManagerRole() {
        RecruitmentRequest request = new RecruitmentRequest();
        request.setRequestTitle("普通员工提交");
        request.setRequestType("NEW_DEMAND");
        request.setTechnicalPlatform("Java");
        request.setCategory("社招");
        request.setSupplementCount(1);
        request.setUrgentRequirement("NO");
        request.setProposedLevel("P6");
        request.setExperienceYears("3年");
        request.setSkillRequirement("Spring Boot");
        request.setPositionResponsibility("负责后端开发");
        request.setCreateUserId("3001");

        when(userMapper.getUserById("3001")).thenReturn(activeUser("3001", "普通员工", "开发工程师", "零售平台开发室", "零售业务开发团队", "零售平台开发室"));
        when(userMapper.getRoleNamesByUserId("3001")).thenReturn(Collections.singletonList("开发工程师"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> recruitmentRequestService.submitRequest(request));
        assertEquals("仅室经理或直属团队经理可提交用人申请", exception.getMessage());
        verify(recruitmentRequestMapper, never()).insert(any());
    }

    @Test
    void submitRequestFallsBackToTeamRoleManagerWhenDepartmentLookupMisses() {
        RecruitmentRequest request = new RecruitmentRequest();
        request.setRequestTitle("室经理提交");
        request.setRequestType("NEW_DEMAND");
        request.setTechnicalPlatform("Java");
        request.setCategory("社招");
        request.setSupplementCount(1);
        request.setUrgentRequirement("NO");
        request.setProposedLevel("P6");
        request.setExperienceYears("3年");
        request.setSkillRequirement("Spring Boot");
        request.setPositionResponsibility("负责后端开发");
        request.setCreateUserId("2001");
        request.setCreateUserName("王室经理");

        User submitter = activeUser("2001", "王室经理", "室经理", "零售平台开发室", "零售业务开发团队", "零售平台开发室");
        User fallbackManager = activeUser("2101", "李团队经理", null, "零售平台开发室", "零售业务开发团队", null);
        Staffing staffing = staffing("零售平台开发室", 8, 2);

        when(userMapper.getUserById("2001")).thenReturn(submitter);
        when(userMapper.getRoleNamesByUserId("2001")).thenReturn(Collections.singletonList("室经理"));
        when(staffingMapper.getStaffingByOrgUnitName("零售平台开发室")).thenReturn(staffing);
        when(orgUnitMapper.getByUnitName("零售平台开发室")).thenReturn(orgUnit("零售平台开发室", "GROUP", "零售业务开发团队"));
        when(userMapper.getActiveTeamManagerByDepartment("零售业务开发团队")).thenReturn(null);
        when(userMapper.getActiveUsersByRoleName("团队经理")).thenReturn(Collections.singletonList(fallbackManager));
        when(recruitmentRequestMapper.insert(any(RecruitmentRequest.class))).thenAnswer(invocation -> {
            RecruitmentRequest inserted = invocation.getArgument(0);
            inserted.setRecruitmentRequestId(11L);
            return 1;
        });

        recruitmentRequestService.submitRequest(request);

        verify(recruitmentRequestMapper).insert(requestCaptor.capture());
        assertEquals("2101", requestCaptor.getValue().getFinalApproverUserId());
        assertEquals("李团队经理", requestCaptor.getValue().getFinalApproverUserName());
    }

    @Test
    void submitRequestAllowsDirectTeamManagerResolvedFromTeamManagerRole() {
        RecruitmentRequest request = new RecruitmentRequest();
        request.setRequestTitle("直属团队提交");
        request.setRequestType("NEW_DEMAND");
        request.setTechnicalPlatform("Java");
        request.setCategory("社招");
        request.setSupplementCount(1);
        request.setUrgentRequirement("NO");
        request.setProposedLevel("P6");
        request.setExperienceYears("3年");
        request.setSkillRequirement("Spring Boot");
        request.setPositionResponsibility("负责后端开发");
        request.setCreateUserId("1006");
        request.setCreateUserName("孙治洲");

        User submitter = activeUser("1006", "孙治洲", null, "人力资源团队", "人力资源团队", null);
        Staffing staffing = staffing("人力资源团队", 10, 2);
        User director = activeUser("6001", "邓检生", "分管总", "直属人员 / 分管总", "直属人员", "分管总");

        when(userMapper.getUserById("1006")).thenReturn(submitter);
        when(userMapper.getRoleNamesByUserId("1006")).thenReturn(Collections.singletonList("人力资源团队经理"));
        when(orgUnitMapper.getActiveOrgUnits()).thenReturn(Collections.singletonList(orgUnit("直属人员", "CATEGORY", "永隆信息有限公司")));
        when(staffingMapper.getStaffingByOrgUnitName("人力资源团队")).thenReturn(staffing);
        when(userMapper.getActiveUserByRealName("邓检生")).thenReturn(director);
        when(recruitmentRequestMapper.insert(any(RecruitmentRequest.class))).thenAnswer(invocation -> {
            RecruitmentRequest inserted = invocation.getArgument(0);
            inserted.setRecruitmentRequestId(13L);
            return 1;
        });

        recruitmentRequestService.submitRequest(request);

        verify(recruitmentRequestMapper).insert(requestCaptor.capture());
        assertEquals("DIRECT_TEAM_MANAGER", requestCaptor.getValue().getSubmitterRoleType());
        assertEquals("人力资源团队", requestCaptor.getValue().getApplicationDepartment());
        assertEquals("6001", requestCaptor.getValue().getFinalApproverUserId());
    }

    @Test
    void submitRequestUsesSelectedResponsibleOrgUnitWhenUserHasMultipleResponsibleRooms() {
        RecruitmentRequest request = new RecruitmentRequest();
        request.setRequestTitle("跨室组补员");
        request.setRequestType("NEW_DEMAND");
        request.setTechnicalPlatform("开放");
        request.setCategory("系统研发岗");
        request.setSupplementCount(1);
        request.setUrgentRequirement("NO");
        request.setProposedLevel("PG");
        request.setExperienceYears("3-5年");
        request.setSkillRequirement("Spring Boot");
        request.setPositionResponsibility("负责后端开发");
        request.setCreateUserId("2001");
        request.setCreateUserName("王室经理");
        request.setOrgUnitName("办公系统开发室");

        User submitter = activeUser("2001", "王室经理", "室经理", "零售平台开发室", "零售业务开发团队", "零售平台开发室");
        Staffing selectedStaffing = staffing("办公系统开发室", 12, 3);
        selectedStaffing.setResponsibleUserId("2001");
        Staffing otherStaffing = staffing("零售平台开发室", 8, 2);
        otherStaffing.setResponsibleUserId("2001");
        User teamManager = activeUser("2100", "韦武", "团队经理", "基础业务开发团队", "基础业务开发团队", null);

        when(userMapper.getUserById("2001")).thenReturn(submitter);
        when(userMapper.getRoleNamesByUserId("2001")).thenReturn(Collections.singletonList("室经理"));
        when(staffingMapper.getStaffingsByResponsibleUserId("2001")).thenReturn(Arrays.asList(selectedStaffing, otherStaffing));
        when(staffingMapper.getStaffingByOrgUnitName("办公系统开发室")).thenReturn(selectedStaffing);
        when(orgUnitMapper.getByUnitName("办公系统开发室")).thenReturn(orgUnit("办公系统开发室", "GROUP", "基础业务开发团队"));
        when(userMapper.getActiveTeamManagerByDepartment("基础业务开发团队")).thenReturn(teamManager);
        when(recruitmentRequestMapper.insert(any(RecruitmentRequest.class))).thenAnswer(invocation -> {
            RecruitmentRequest inserted = invocation.getArgument(0);
            inserted.setRecruitmentRequestId(21L);
            return 1;
        });

        recruitmentRequestService.submitRequest(request);

        verify(recruitmentRequestMapper).insert(requestCaptor.capture());
        assertEquals("办公系统开发室", requestCaptor.getValue().getOrgUnitName());
        assertEquals("基础业务开发团队 / 办公系统开发室", requestCaptor.getValue().getApplicationDepartment());
        assertEquals(Integer.valueOf(12), requestCaptor.getValue().getTotalRecruitmentCount());
        assertEquals(Integer.valueOf(3), requestCaptor.getValue().getVacancyCount());
        assertEquals("2100", requestCaptor.getValue().getFinalApproverUserId());
    }

    @Test
    void directTeamFinalApprovalSendsMessagesToApplicantAndOutsourcingManagers() {
        RecruitmentRequest request = new RecruitmentRequest();
        request.setRecruitmentRequestId(9L);
        request.setRequestTitle("技术管理团队补员");
        request.setTeam("技术管理团队");
        request.setApplicationDepartment("技术管理团队");
        request.setOrgUnitName("技术管理团队");
        request.setCreateUserId("4001");
        request.setCreateUserName("直属团队经理");
        request.setCurrentApprovalLevel(3);
        request.setApprovalStatus("2NDAPPROVED");
        request.setSubmitterRoleType("DIRECT_TEAM_MANAGER");
        request.setFinalApproverUserId("6001");
        request.setFinalApproverUserName("邓检生");

        RecruitmentRequest approved = new RecruitmentRequest();
        approved.setRecruitmentRequestId(9L);
        approved.setApplicationDepartment("技术管理团队");
        approved.setCreateUserId("4001");
        approved.setCreateUserName("直属团队经理");
        approved.setCurrentApprovalLevel(4);
        approved.setApprovalStatus("3RDAPPROVED");

        when(recruitmentRequestMapper.selectByPrimaryKey(9L)).thenReturn(request, approved);
        when(userMapper.getActiveUsersByRoleName("外包招聘管理岗")).thenReturn(Arrays.asList(
            activeUser("5001", "外包招聘A", "外包招聘管理岗", "人力资源团队", "人力资源团队", null),
            activeUser("4001", "直属团队经理", "外包招聘管理岗", "技术管理团队", "技术管理团队", null)
        ));

        recruitmentRequestService.threeLevelApproveRequest(9L, Map.of(
            "approvalUserId", "6001",
            "approvalUserName", "邓检生",
            "approvalComment", "通过"
        ));

        verify(messageService, times(2)).createMessage(any());
    }

    @Test
    void finalApprovalSupportsOutsourcingManagerRoleAlias() {
        RecruitmentRequest request = new RecruitmentRequest();
        request.setRecruitmentRequestId(12L);
        request.setRequestTitle("团队补员");
        request.setTeam("基础业务开发团队");
        request.setApplicationDepartment("办公系统开发室");
        request.setOrgUnitName("办公系统开发室");
        request.setCreateUserId("1007");
        request.setCreateUserName("胡俊峰");
        request.setCurrentApprovalLevel(3);
        request.setApprovalStatus("2NDAPPROVED");
        request.setSubmitterRoleType("ROOM_MANAGER");
        request.setFinalApproverUserId("1009");
        request.setFinalApproverUserName("韦武");

        RecruitmentRequest approved = new RecruitmentRequest();
        approved.setRecruitmentRequestId(12L);
        approved.setApplicationDepartment("办公系统开发室");
        approved.setCreateUserId("1007");
        approved.setCreateUserName("胡俊峰");
        approved.setCurrentApprovalLevel(4);
        approved.setApprovalStatus("3RDAPPROVED");

        when(recruitmentRequestMapper.selectByPrimaryKey(12L)).thenReturn(request, approved);
        when(userMapper.getActiveTeamManagerByDepartment("办公系统开发室")).thenReturn(activeUser("1009", "韦武", "团队经理", "办公系统开发室", "基础业务开发团队", null));
        when(userMapper.getActiveUsersByRoleName("外包招聘管理岗")).thenReturn(Collections.emptyList());
        when(userMapper.getActiveUsersByRoleName("外包招聘岗")).thenReturn(Collections.emptyList());
        when(userMapper.getActiveUsersByRoleName("外包招聘管理")).thenReturn(Collections.singletonList(
            activeUser("1002", "梁秋怡", null, "人力资源团队", "人力资源团队", null)
        ));

        recruitmentRequestService.threeLevelApproveRequest(12L, Map.of(
            "approvalUserId", "1009",
            "approvalUserName", "韦武",
            "approvalComment", "通过"
        ));

        verify(messageService, times(2)).createMessage(any());
    }

    @Test
    void completionNotificationUsesDisplayDepartmentText() {
        RecruitmentRequest request = new RecruitmentRequest();
        request.setRecruitmentRequestId(14L);
        request.setRequestTitle("团队补员");
        request.setTeam("办公系统开发室");
        request.setApplicationDepartment("办公系统开发室");
        request.setOrgUnitName("办公系统开发室");
        request.setCreateUserId("1007");
        request.setCreateUserName("胡俊峰");
        request.setCurrentApprovalLevel(3);
        request.setApprovalStatus("2NDAPPROVED");
        request.setSubmitterRoleType("ROOM_MANAGER");
        request.setFinalApproverUserId("1009");
        request.setFinalApproverUserName("韦武");

        RecruitmentRequest approved = new RecruitmentRequest();
        approved.setRecruitmentRequestId(14L);
        approved.setTeam("办公系统开发室");
        approved.setApplicationDepartment("办公系统开发室");
        approved.setOrgUnitName("办公系统开发室");
        approved.setCreateUserId("1007");
        approved.setCreateUserName("胡俊峰");
        approved.setCurrentApprovalLevel(4);
        approved.setApprovalStatus("3RDAPPROVED");

        when(recruitmentRequestMapper.selectByPrimaryKey(14L)).thenReturn(request, approved);
        when(orgUnitMapper.getByUnitName("办公系统开发室")).thenReturn(orgUnit("办公系统开发室", "GROUP", "基础业务开发团队"));
        when(userMapper.getActiveTeamManagerByDepartment("基础业务开发团队")).thenReturn(activeUser("1009", "韦武", "团队经理", "办公系统开发室", "基础业务开发团队", null));
        when(userMapper.getActiveUsersByRoleName("外包招聘管理岗")).thenReturn(Collections.emptyList());
        when(userMapper.getActiveUsersByRoleName("外包招聘岗")).thenReturn(Collections.emptyList());
        when(userMapper.getActiveUsersByRoleName("外包招聘管理")).thenReturn(Collections.emptyList());

        recruitmentRequestService.threeLevelApproveRequest(14L, Map.of(
            "approvalUserId", "1009",
            "approvalUserName", "韦武",
            "approvalComment", "通过"
        ));

        verify(messageService).createMessage(messageCaptor.capture());
        assertTrue(messageCaptor.getValue().getContent().contains("基础业务开发团队 / 办公系统开发室"));
    }

    @Test
    void secondLevelApprovalSendsPendingMessageToTeamManager() {
        RecruitmentRequest request = new RecruitmentRequest();
        request.setRecruitmentRequestId(15L);
        request.setRequestTitle("团队补员");
        request.setTeam("基础业务开发团队 / 办公系统开发室");
        request.setApplicationDepartment("基础业务开发团队 / 办公系统开发室");
        request.setOrgUnitName("办公系统开发室");
        request.setCreateUserId("1007");
        request.setCreateUserName("胡俊峰");
        request.setCurrentApprovalLevel(2);
        request.setApprovalStatus("1STAPPROVED");
        request.setSubmitterRoleType("ROOM_MANAGER");
        request.setFinalApproverUserId("1009");
        request.setFinalApproverUserName("韦武");

        RecruitmentRequest transferred = new RecruitmentRequest();
        transferred.setRecruitmentRequestId(15L);
        transferred.setTeam("基础业务开发团队 / 办公系统开发室");
        transferred.setApplicationDepartment("基础业务开发团队 / 办公系统开发室");
        transferred.setOrgUnitName("办公系统开发室");
        transferred.setCreateUserId("1007");
        transferred.setCreateUserName("胡俊峰");
        transferred.setCurrentApprovalLevel(3);
        transferred.setApprovalStatus("2NDAPPROVED");
        transferred.setSubmitterRoleType("ROOM_MANAGER");
        transferred.setFinalApproverUserId("1009");
        transferred.setFinalApproverUserName("韦武");

        when(recruitmentRequestMapper.selectByPrimaryKey(15L)).thenReturn(request, transferred);
        when(orgUnitMapper.getByUnitName("办公系统开发室")).thenReturn(orgUnit("办公系统开发室", "GROUP", "基础业务开发团队"));
        when(userMapper.getActiveTeamManagerByDepartment("基础业务开发团队"))
            .thenReturn(activeUser("1009", "韦武", "团队经理", "基础业务开发团队", "基础业务开发团队", null));

        recruitmentRequestService.threeLevelApproveRequest(15L, Map.of(
            "approvalUserId", "1002",
            "approvalUserName", "梁秋怡",
            "approvalComment", "通过"
        ));

        verify(messageService).createMessage(messageCaptor.capture());
        assertEquals("1009", messageCaptor.getValue().getTargetUserId());
        assertTrue(messageCaptor.getValue().getContent().contains("基础业务开发团队 / 办公系统开发室"));
    }

    @Test
    void getAllByViewerReturnsOwnRequestsForRoomManager() {
        RecruitmentRequest own = new RecruitmentRequest();
        own.setRecruitmentRequestId(1L);
        own.setCreateUserId("2001");

        RecruitmentRequest other = new RecruitmentRequest();
        other.setRecruitmentRequestId(2L);
        other.setCreateUserId("2002");

        when(recruitmentRequestMapper.selectAll()).thenReturn(Arrays.asList(own, other));
        when(userMapper.getUserById("2001")).thenReturn(activeUser("2001", "王室经理", "室经理", "零售平台开发室", "零售业务开发团队", "零售平台开发室"));
        when(userMapper.getRoleNamesByUserId("2001")).thenReturn(Collections.singletonList("室经理"));

        List<RecruitmentRequest> visible = recruitmentRequestService.getAll("2001", "室经理");

        assertEquals(1, visible.size());
        assertEquals(Long.valueOf(1L), visible.get(0).getRecruitmentRequestId());
    }

    @Test
    void getByIdNormalizesApplicationDepartmentForDisplay() {
        RecruitmentRequest stored = new RecruitmentRequest();
        stored.setRecruitmentRequestId(7L);
        stored.setOrgUnitName("办公系统开发室");
        stored.setApplicationDepartment("办公系统开发室");
        stored.setTeam("办公系统开发室");

        when(recruitmentRequestMapper.selectByPrimaryKey(7L)).thenReturn(stored);
        when(orgUnitMapper.getByUnitName("办公系统开发室")).thenReturn(orgUnit("办公系统开发室", "GROUP", "基础业务开发团队"));

        RecruitmentRequest result = recruitmentRequestService.getById(7L);

        assertEquals("基础业务开发团队 / 办公系统开发室", result.getApplicationDepartment());
    }

    private Staffing staffing(String orgUnitName, int total, int vacancy) {
        Staffing staffing = new Staffing();
        staffing.setOrgUnitName(orgUnitName);
        staffing.setTotalHeadcount(total);
        staffing.setVacancyHeadcount(vacancy);
        return staffing;
    }

    private OrgUnit orgUnit(String unitName, String unitType, String parentUnitName) {
        OrgUnit orgUnit = new OrgUnit();
        orgUnit.setUnitName(unitName);
        orgUnit.setUnitType(unitType);
        orgUnit.setParentUnitName(parentUnitName);
        orgUnit.setStatus("ACTIVE");
        return orgUnit;
    }

    private User activeUser(String userId, String realName, String position, String department, String teamName, String groupName) {
        User user = new User();
        user.setUserId(userId);
        user.setRealName(realName);
        user.setPosition(position);
        user.setDepartment(department);
        user.setTeamName(teamName);
        user.setGroupName(groupName);
        if (groupName != null && !groupName.isEmpty()) {
            user.setDepartmentDisplay(teamName + " / " + groupName);
        } else {
            user.setDepartmentDisplay(department);
        }
        user.setStatus("ACTIVE");
        return user;
    }
}
