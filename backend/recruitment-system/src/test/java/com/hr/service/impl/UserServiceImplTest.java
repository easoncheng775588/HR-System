package com.hr.service.impl;

import com.hr.entity.OrgUnit;
import com.hr.entity.User;
import com.hr.mapper.OrgUnitMapper;
import com.hr.mapper.UserMapper;
import org.apache.ibatis.binding.BindingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private OrgUnitMapper orgUnitMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Test
    void createUserForNormalTeamDerivesDepartmentFromGroup() {
        User user = new User();
        user.setUsername("zhangsan");
        user.setPassword("123456");
        user.setRealName("张三");
        user.setTeamName("零售业务开发团队");
        user.setGroupName("零售平台开发室");

        when(orgUnitMapper.getActiveOrgUnits()).thenReturn(Arrays.asList(
            orgUnit("永隆信息有限公司", "ROOT", null),
            orgUnit("零售业务开发团队", "TEAM", "永隆信息有限公司"),
            orgUnit("零售平台开发室", "GROUP", "零售业务开发团队")
        ));
        when(userMapper.insertUser(any(User.class))).thenReturn(1);

        userService.createUser(user);

        verify(userMapper).insertUser(userCaptor.capture());
        assertEquals("零售业务开发团队", userCaptor.getValue().getTeamName());
        assertEquals("零售平台开发室", userCaptor.getValue().getGroupName());
        assertEquals("零售平台开发室", userCaptor.getValue().getDepartment());
    }

    @Test
    void createUserForDirectTeamRejectsGroupName() {
        User user = new User();
        user.setUsername("lisi");
        user.setPassword("123456");
        user.setRealName("李四");
        user.setTeamName("技术管理团队");
        user.setGroupName("不应该填写");

        when(orgUnitMapper.getActiveOrgUnits()).thenReturn(Arrays.asList(
            orgUnit("永隆信息有限公司", "ROOT", null),
            orgUnit("技术管理团队", "TEAM", "永隆信息有限公司")
        ));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.createUser(user));
        assertEquals("所选团队不允许填写室组名称", exception.getMessage());
    }

    @Test
    void getUserByIdBackfillsLegacyDepartmentDisplay() {
        User storedUser = new User();
        storedUser.setUserId("1002");
        storedUser.setUsername("wangwu");
        storedUser.setRealName("王五");
        storedUser.setDepartment("零售平台开发室");

        when(userMapper.getUserById("1002")).thenReturn(storedUser);
        when(orgUnitMapper.getActiveOrgUnits()).thenReturn(Arrays.asList(
            orgUnit("永隆信息有限公司", "ROOT", null),
            orgUnit("零售业务开发团队", "TEAM", "永隆信息有限公司"),
            orgUnit("零售平台开发室", "GROUP", "零售业务开发团队")
        ));

        User result = userService.getUserById("1002");

        assertEquals("零售业务开发团队", result.getTeamName());
        assertEquals("零售平台开发室", result.getGroupName());
        assertEquals("TEAM_GROUP", result.getDepartmentType());
        assertEquals("零售业务开发团队 / 零售平台开发室", result.getDepartmentDisplay());
    }

    @Test
    void getUserByIdBackfillsDirectTeamDepartmentDisplay() {
        User storedUser = new User();
        storedUser.setUserId("1003");
        storedUser.setUsername("zhaoliu");
        storedUser.setRealName("赵六");
        storedUser.setDepartment("技术管理团队");

        when(userMapper.getUserById("1003")).thenReturn(storedUser);
        when(orgUnitMapper.getActiveOrgUnits()).thenReturn(Arrays.asList(
            orgUnit("永隆信息有限公司", "ROOT", null),
            orgUnit("技术管理团队", "TEAM", "永隆信息有限公司")
        ));

        User result = userService.getUserById("1003");

        assertEquals("技术管理团队", result.getTeamName());
        assertNull(result.getGroupName());
        assertEquals("TEAM_ONLY", result.getDepartmentType());
        assertEquals("技术管理团队", result.getDepartmentDisplay());
    }

    @Test
    void getUserByIdFallsBackToPrimaryRoleForPosition() {
        User storedUser = new User();
        storedUser.setUserId("1004");
        storedUser.setUsername("simone");
        storedUser.setRealName("何诗敏");
        storedUser.setDepartment("人力资源团队");
        storedUser.setTeamName("人力资源团队");

        when(userMapper.getUserById("1004")).thenReturn(storedUser);
        when(userMapper.getRoleNamesByUserId("1004")).thenReturn(Collections.singletonList("编制管理岗"));
        when(orgUnitMapper.getActiveOrgUnits()).thenReturn(Arrays.asList(
            orgUnit("永隆信息有限公司", "ROOT", null),
            orgUnit("人力资源团队", "TEAM", "永隆信息有限公司")
        ));

        User result = userService.getUserById("1004");

        assertEquals("编制管理岗", result.getPosition());
        assertEquals("TEAM_ONLY", result.getDepartmentType());
    }

    @Test
    void getAllUsersDoesNotFailWhenRoleMapperStatementIsMissing() {
        User storedUser = new User();
        storedUser.setUserId("1001");
        storedUser.setUsername("admin");
        storedUser.setRealName("超级管理员");
        storedUser.setDepartment("人事部");

        when(userMapper.getAllUsers()).thenReturn(Collections.singletonList(storedUser));
        when(userMapper.getRoleNamesByUserId("1001"))
            .thenThrow(new BindingException("Invalid bound statement (not found): com.hr.mapper.UserMapper.getRoleNamesByUserId"));
        when(orgUnitMapper.getActiveOrgUnits()).thenReturn(Collections.emptyList());

        java.util.List<User> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("1001", result.get(0).getUserId());
        assertNull(result.get(0).getPosition());
    }

    @Test
    void getDepartmentOptionsBuildsStructuredResponse() {
        when(orgUnitMapper.getActiveOrgUnits()).thenReturn(Arrays.asList(
            orgUnit("永隆信息有限公司", "ROOT", null),
            orgUnit("零售业务开发团队", "TEAM", "永隆信息有限公司"),
            orgUnit("零售平台开发室", "GROUP", "零售业务开发团队"),
            orgUnit("技术管理团队", "TEAM", "永隆信息有限公司"),
            orgUnit("直属人员", "CATEGORY", "永隆信息有限公司"),
            orgUnit("部门总经理", "LEAF", "直属人员"),
            orgUnit("其他", "CATEGORY", "永隆信息有限公司"),
            orgUnit("供应商HR", "LEAF", "其他")
        ));

        Object result = userService.getDepartmentOptions();

        assertEquals("永隆信息有限公司", ((java.util.Map<?, ?>) result).get("companyName"));
        assertEquals(4, ((java.util.List<?>) ((java.util.Map<?, ?>) result).get("teamOptions")).size());
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
