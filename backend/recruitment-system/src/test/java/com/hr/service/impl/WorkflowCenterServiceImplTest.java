package com.hr.service.impl;

import com.hr.entity.InterviewEvaluation;
import com.hr.entity.RecruitmentRequest;
import com.hr.entity.OrgUnit;
import com.hr.entity.User;
import com.hr.entity.WorkflowNodeConfig;
import com.hr.entity.WorkflowTodoItem;
import com.hr.mapper.ApprovalHistoryMapper;
import com.hr.mapper.InterviewEvaluationApprovalHistoryMapper;
import com.hr.mapper.InterviewEvaluationMapper;
import com.hr.mapper.OrgUnitMapper;
import com.hr.mapper.RecruitmentRequestMapper;
import com.hr.mapper.UserMapper;
import com.hr.mapper.WorkflowNodeConfigMapper;
import com.hr.mapper.WorkflowProcessLogMapper;
import com.hr.service.InterviewEvaluationService;
import com.hr.service.RecruitmentRequestService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowCenterServiceImplTest {

    @Mock
    private RecruitmentRequestMapper recruitmentRequestMapper;

    @Mock
    private ApprovalHistoryMapper approvalHistoryMapper;

    @Mock
    private OrgUnitMapper orgUnitMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private WorkflowNodeConfigMapper workflowNodeConfigMapper;

    @Mock
    private WorkflowProcessLogMapper workflowProcessLogMapper;

    @Mock
    private RecruitmentRequestService recruitmentRequestService;

    @Mock
    private InterviewEvaluationMapper interviewEvaluationMapper;

    @Mock
    private InterviewEvaluationApprovalHistoryMapper interviewEvaluationApprovalHistoryMapper;

    @Mock
    private InterviewEvaluationService interviewEvaluationService;

    @InjectMocks
    private WorkflowCenterServiceImpl workflowCenterService;

    @Test
    void getMyTodoReturnsLevelThreeTaskForTeamManagerWhenTeamFieldStoresDisplayText() {
        RecruitmentRequest request = new RecruitmentRequest();
        request.setRecruitmentRequestId(7L);
        request.setRequestTitle("办公系统补员");
        request.setTeam("基础业务开发团队 / 办公系统开发室");
        request.setApplicationDepartment("基础业务开发团队 / 办公系统开发室");
        request.setOrgUnitName("办公系统开发室");
        request.setCreateUserName("胡俊峰");
        request.setCurrentApprovalLevel(3);
        request.setApprovalStatus("2NDAPPROVED");
        request.setSubmitterRoleType("ROOM_MANAGER");

        WorkflowNodeConfig node = new WorkflowNodeConfig();
        node.setNodeOrder(3);
        node.setNodeName("团队经理审批");
        node.setApproverRole("团队经理");

        User teamManager = new User();
        teamManager.setUserId("1009");
        teamManager.setRealName("韦武");
        teamManager.setTeamName("基础业务开发团队");
        teamManager.setDepartment("基础业务开发团队");
        teamManager.setStatus("ACTIVE");

        when(recruitmentRequestMapper.selectAll()).thenReturn(Collections.singletonList(request));
        when(interviewEvaluationMapper.selectAll()).thenReturn(Collections.emptyList());
        when(workflowNodeConfigMapper.selectByProcessCode("RECRUITMENT_REQUEST")).thenReturn(Collections.singletonList(node));
        when(orgUnitMapper.getByUnitName("办公系统开发室")).thenReturn(orgUnit("办公系统开发室", "GROUP", "基础业务开发团队"));
        when(userMapper.getActiveTeamManagerByDepartment("基础业务开发团队")).thenReturn(teamManager);

        List<WorkflowTodoItem> items = workflowCenterService.getMyTodo("1009", "团队经理");

        assertEquals(1, items.size());
        assertEquals(Long.valueOf(7L), items.get(0).getRequestId());
        assertEquals("基础业务开发团队 / 办公系统开发室", items.get(0).getApplicantDept());
    }

    @Test
    void getMyTodoReturnsInterviewEvaluationForRoomManagerMatchedByRoleAndLeafDepartment() {
        when(recruitmentRequestMapper.selectAll()).thenReturn(Collections.emptyList());

        InterviewEvaluation evaluation = new InterviewEvaluation();
        evaluation.setEvaluationId(11L);
        evaluation.setCandidateName("候选人甲");
        evaluation.setInterviewerName("陈秀芳");
        evaluation.setEntryLevelSuggestion("P6");
        evaluation.setInterviewerDepartment("基础业务开发团队 / 办公系统开发室");
        evaluation.setApprovalStatus("PENDING_ROOM_MANAGER");
        when(interviewEvaluationMapper.selectAll()).thenReturn(Collections.singletonList(evaluation));

        User roomManager = new User();
        roomManager.setUserId("1007");
        roomManager.setRealName("胡俊峰");
        roomManager.setDepartment("办公系统开发室");
        roomManager.setStatus("ACTIVE");
        when(userMapper.getActiveRoomManagerByDepartmentAndRoleKeyword("办公系统开发室", "室经理")).thenReturn(roomManager);

        List<WorkflowTodoItem> items = workflowCenterService.getMyTodo("1007", "室经理");

        assertEquals(1, items.size());
        assertEquals(Long.valueOf(11L), items.get(0).getBusinessId());
        assertEquals("室经理审批", items.get(0).getCurrentNode());
    }

    private OrgUnit orgUnit(String unitName, String unitType, String parentUnitName) {
        OrgUnit orgUnit = new OrgUnit();
        orgUnit.setUnitName(unitName);
        orgUnit.setUnitType(unitType);
        orgUnit.setParentUnitName(parentUnitName);
        orgUnit.setStatus("ACTIVE");
        return orgUnit;
    }
}
