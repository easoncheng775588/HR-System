package com.hr.service.impl;

import com.hr.entity.OrgUnit;
import com.hr.entity.Staffing;
import com.hr.entity.User;
import com.hr.mapper.OrgUnitMapper;
import com.hr.mapper.StaffingMapper;
import com.hr.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaffingServiceImplTest {

    @Mock
    private StaffingMapper staffingMapper;

    @Mock
    private OrgUnitMapper orgUnitMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private StaffingServiceImpl staffingService;

    @Test
    void getResponsibleStaffingsReturnsOnlyGroupRows() {
        Staffing groupStaffing = staffing("办公系统开发室", "2001");
        Staffing teamStaffing = staffing("基础业务开发团队", "2001");

        when(staffingMapper.getStaffingsByResponsibleUserId("2001")).thenReturn(Arrays.asList(groupStaffing, teamStaffing));
        when(orgUnitMapper.getByUnitName("办公系统开发室")).thenReturn(orgUnit("办公系统开发室", "GROUP", "基础业务开发团队"));
        when(orgUnitMapper.getByUnitName("基础业务开发团队")).thenReturn(orgUnit("基础业务开发团队", "TEAM", "永隆信息有限公司"));

        List<Staffing> visible = staffingService.getResponsibleStaffings("2001");

        assertEquals(1, visible.size());
        assertEquals("办公系统开发室", visible.get(0).getOrgUnitName());
    }

    @Test
    void getAllStaffingsReturnsSubordinateRoomsForTeamManager() {
        Staffing ownTeam = staffing("基础业务开发团队", null);
        Staffing ownGroup = staffing("办公系统开发室", "2001");
        Staffing otherGroup = staffing("零售平台开发室", "2002");

        when(staffingMapper.getAllStaffings()).thenReturn(Arrays.asList(ownTeam, ownGroup, otherGroup));
        when(userMapper.getUserById("1009")).thenReturn(activeUser("1009", "韦武", "团队经理", "基础业务开发团队", "基础业务开发团队", null));
        when(userMapper.getRoleNamesByUserId("1009")).thenReturn(Collections.singletonList("团队经理"));
        when(orgUnitMapper.getByUnitName("基础业务开发团队")).thenReturn(orgUnit("基础业务开发团队", "TEAM", "永隆信息有限公司"));
        when(orgUnitMapper.getByUnitName("办公系统开发室")).thenReturn(orgUnit("办公系统开发室", "GROUP", "基础业务开发团队"));
        when(orgUnitMapper.getByUnitName("零售平台开发室")).thenReturn(orgUnit("零售平台开发室", "GROUP", "零售业务开发团队"));

        List<Staffing> visible = staffingService.getAllStaffings("1009", "团队经理");

        assertEquals(2, visible.size());
        assertEquals("基础业务开发团队", visible.get(0).getOrgUnitName());
        assertEquals("办公系统开发室", visible.get(1).getOrgUnitName());
    }

    @Test
    void createStaffingRequiresResponsibleUserForGroup() {
        Staffing staffing = new Staffing();
        staffing.setOrgUnitName("办公系统开发室");
        staffing.setTotalHeadcount(10);
        staffing.setVacancyHeadcount(2);
        staffing.setOutsourcingHeadcount(3);
        staffing.setEmployeeHeadcount(5);

        when(orgUnitMapper.getByUnitName("办公系统开发室")).thenReturn(orgUnit("办公系统开发室", "GROUP", "基础业务开发团队"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> staffingService.createStaffing(staffing));
        assertEquals("室组负责人不能为空", exception.getMessage());
    }

    private Staffing staffing(String orgUnitName, String responsibleUserId) {
        Staffing staffing = new Staffing();
        staffing.setOrgUnitName(orgUnitName);
        staffing.setResponsibleUserId(responsibleUserId);
        staffing.setTotalHeadcount(10);
        staffing.setVacancyHeadcount(2);
        staffing.setOutsourcingHeadcount(3);
        staffing.setEmployeeHeadcount(5);
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
        user.setStatus("ACTIVE");
        return user;
    }
}
