package com.hr.service.impl;

import com.hr.entity.OrgUnit;
import com.hr.entity.User;
import com.hr.mapper.OrgUnitMapper;
import com.hr.mapper.UserMapper;
import com.hr.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final String COMPANY_NAME = "永隆信息有限公司";
    private static final String DIRECT_CATEGORY = "直属人员";
    private static final String OTHER_CATEGORY = "其他";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrgUnitMapper orgUnitMapper;

    @Override
    public List<User> getAllUsers() {
        return enrichUsers(userMapper.getAllUsers());
    }

    @Override
    public User getUserById(String userId) {
        return enrichUser(userMapper.getUserById(userId), getActiveOrgUnits());
    }

    @Override
    public User getUserByUsername(String username) {
        return enrichUser(userMapper.getUserByUsername(username), getActiveOrgUnits());
    }

    @Override
    public int createUser(User user) {
        applyDepartmentFields(user, false);
        if (user.getUserId() == null || user.getUserId().isEmpty()) {
            user.setUserId(generateUserId());
        }
        if (user.getCreateTime() == null) {
            user.setCreateTime(new Date());
        }
        if (user.getUpdateTime() == null) {
            user.setUpdateTime(new Date());
        }
        if (user.getCreateUserId() == null) {
            user.setCreateUserId("1001");
        }
        if (user.getCreateUserName() == null) {
            user.setCreateUserName("系统");
        }
        if (user.getUpdateUserId() == null) {
            user.setUpdateUserId("1001");
        }
        if (user.getUpdateUserName() == null) {
            user.setUpdateUserName("系统");
        }
        if (user.getStatus() == null) {
            user.setStatus("ACTIVE");
        }
        return userMapper.insertUser(user);
    }

    private String generateUserId() {
        return String.valueOf(System.currentTimeMillis());
    }

    @Override
    public int updateUser(User user) {
        User existingUser = userMapper.getUserById(user.getUserId());
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }
        if ((user.getPassword() == null || user.getPassword().trim().isEmpty()) && existingUser.getPassword() != null) {
            user.setPassword(existingUser.getPassword());
        }
        applyDepartmentFields(user, true);
        if (user.getUpdateTime() == null) {
            user.setUpdateTime(new Date());
        }
        if (user.getUpdateUserId() == null) {
            user.setUpdateUserId("1001");
        }
        if (user.getUpdateUserName() == null) {
            user.setUpdateUserName("系统");
        }
        return userMapper.updateUser(user);
    }

    @Override
    public int deleteUser(String userId) {
        return userMapper.deleteUser(userId);
    }

    @Override
    public List<String> getRoleIdsByUserId(String userId) {
        return userMapper.getRoleIdsByUserId(userId);
    }

    @Override
    public List<User> getUsersByPosition(String position) {
        return enrichUsers(userMapper.getUsersByPosition(position));
    }

    @Override
    public List<User> searchActiveUsers(String keyword) {
        return enrichUsers(userMapper.searchActiveUsers(keyword == null ? "" : keyword.trim()));
    }

    @Override
    public User getActiveTeamManagerByDepartment(String department) {
        return userMapper.getActiveTeamManagerByDepartment(department);
    }

    @Override
    public Map<String, Object> getDepartmentOptions() {
        List<OrgUnit> orgUnits = getActiveOrgUnits();
        Map<String, List<OrgUnit>> childrenByParent = groupByParent(orgUnits);
        String companyName = resolveCompanyName(orgUnits);
        List<Map<String, Object>> teamOptions = new ArrayList<>();
        for (OrgUnit orgUnit : orgUnits) {
            if (!companyName.equals(orgUnit.getParentUnitName())) {
                continue;
            }
            if (!isTopLevelDepartmentUnit(orgUnit)) {
                continue;
            }
            List<OrgUnit> children = childrenByParent.getOrDefault(orgUnit.getUnitName(), Collections.emptyList());
            Map<String, Object> teamOption = new LinkedHashMap<>();
            teamOption.put("teamName", orgUnit.getUnitName());
            teamOption.put("teamType", resolveTeamOptionType(orgUnit.getUnitName(), children));
            teamOption.put("requiresGroup", requiresGroup(orgUnit.getUnitName(), children));
            List<Map<String, Object>> groupOptions = children.stream()
                .map(child -> {
                    Map<String, Object> groupOption = new LinkedHashMap<>();
                    groupOption.put("groupName", child.getUnitName());
                    return groupOption;
                })
                .collect(Collectors.toList());
            teamOption.put("groupOptions", groupOptions);
            teamOptions.add(teamOption);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("companyName", companyName);
        result.put("teamOptions", teamOptions);
        return result;
    }

    private List<User> enrichUsers(List<User> users) {
        List<OrgUnit> orgUnits = getActiveOrgUnits();
        return users.stream()
            .map(user -> enrichUser(user, orgUnits))
            .collect(Collectors.toList());
    }

    private User enrichUser(User user, List<OrgUnit> orgUnits) {
        if (user == null) {
            return null;
        }
        Map<String, OrgUnit> orgUnitMap = buildOrgUnitMap(orgUnits);
        String companyName = resolveCompanyName(orgUnits);
        if ((user.getTeamName() == null || user.getTeamName().trim().isEmpty())
            && user.getDepartment() != null && !user.getDepartment().trim().isEmpty()) {
            OrgUnit departmentUnit = orgUnitMap.get(user.getDepartment());
            if (departmentUnit != null) {
                OrgUnit parentUnit = orgUnitMap.get(departmentUnit.getParentUnitName());
                if (departmentUnit.getParentUnitName() == null
                    || departmentUnit.getParentUnitName().trim().isEmpty()
                    || companyName.equals(departmentUnit.getParentUnitName())
                    || (parentUnit != null && "ROOT".equals(parentUnit.getUnitType()))) {
                    user.setTeamName(departmentUnit.getUnitName());
                } else {
                    user.setTeamName(departmentUnit.getParentUnitName());
                    user.setGroupName(departmentUnit.getUnitName());
                }
            }
        }
        fillDepartmentDisplay(user);
        return user;
    }

    private void applyDepartmentFields(User user, boolean updateMode) {
        List<OrgUnit> orgUnits = getActiveOrgUnits();
        Map<String, OrgUnit> orgUnitMap = buildOrgUnitMap(orgUnits);
        Map<String, List<OrgUnit>> childrenByParent = groupByParent(orgUnits);

        String teamName = trimToNull(user.getTeamName());
        String groupName = trimToNull(user.getGroupName());

        if (teamName == null) {
            throw new RuntimeException("团队名称不能为空");
        }

        OrgUnit teamUnit = orgUnitMap.get(teamName);
        if (teamUnit == null) {
            throw new RuntimeException("团队名称不存在");
        }
        String companyName = resolveCompanyName(orgUnits);
        if (!isSelectableTeamUnit(teamUnit, companyName)) {
            throw new RuntimeException("团队名称不存在");
        }

        List<OrgUnit> children = childrenByParent.getOrDefault(teamName, Collections.emptyList());
        boolean groupRequired = requiresGroup(teamName, children);

        if (groupRequired && groupName == null) {
            throw new RuntimeException("所选团队要求必须填写室组名称");
        }
        if (!groupRequired && groupName != null) {
            throw new RuntimeException("所选团队不允许填写室组名称");
        }

        if (groupName != null) {
            OrgUnit groupUnit = orgUnitMap.get(groupName);
            if (groupUnit == null || !teamName.equals(groupUnit.getParentUnitName())) {
                throw new RuntimeException("室组名称不存在或不属于该团队");
            }
        }

        user.setTeamName(teamName);
        user.setGroupName(groupName);
        user.setDepartment(groupName == null ? teamName : groupName);
        fillDepartmentDisplay(user);

        if (updateMode && user.getCreateTime() == null) {
            User existingUser = userMapper.getUserById(user.getUserId());
            if (existingUser != null) {
                user.setCreateTime(existingUser.getCreateTime());
                user.setCreateUserId(existingUser.getCreateUserId());
                user.setCreateUserName(existingUser.getCreateUserName());
            }
        }
    }

    private void fillDepartmentDisplay(User user) {
        String teamName = trimToNull(user.getTeamName());
        String groupName = trimToNull(user.getGroupName());
        if (DIRECT_CATEGORY.equals(teamName)) {
            user.setDepartmentType("DIRECT");
            user.setDepartmentDisplay(groupName == null ? DIRECT_CATEGORY : DIRECT_CATEGORY + " / " + groupName);
            return;
        }
        if (OTHER_CATEGORY.equals(teamName)) {
            user.setDepartmentType("OTHER");
            user.setDepartmentDisplay(groupName == null ? OTHER_CATEGORY : OTHER_CATEGORY + " / " + groupName);
            return;
        }
        if (teamName != null && groupName != null) {
            user.setDepartmentType("TEAM_GROUP");
            user.setDepartmentDisplay(teamName + " / " + groupName);
            return;
        }
        if (teamName != null) {
            user.setDepartmentType("TEAM_ONLY");
            user.setDepartmentDisplay(teamName);
            return;
        }
        user.setDepartmentType(null);
        user.setDepartmentDisplay(user.getDepartment());
    }

    private boolean requiresGroup(String teamName, List<OrgUnit> children) {
        if (DIRECT_CATEGORY.equals(teamName) || OTHER_CATEGORY.equals(teamName)) {
            return true;
        }
        return !children.isEmpty();
    }

    private String resolveTeamOptionType(String teamName, List<OrgUnit> children) {
        if (DIRECT_CATEGORY.equals(teamName)) {
            return "DIRECT_CATEGORY";
        }
        if (OTHER_CATEGORY.equals(teamName)) {
            return "OTHER_CATEGORY";
        }
        return children.isEmpty() ? "DIRECT" : "NORMAL";
    }

    private boolean isTopLevelDepartmentUnit(OrgUnit orgUnit) {
        return "TEAM".equals(orgUnit.getUnitType()) || "CATEGORY".equals(orgUnit.getUnitType());
    }

    private boolean isSelectableTeamUnit(OrgUnit orgUnit, String companyName) {
        if (orgUnit == null || !isTopLevelDepartmentUnit(orgUnit)) {
            return false;
        }
        return companyName.equals(orgUnit.getParentUnitName());
    }

    private String resolveCompanyName(List<OrgUnit> orgUnits) {
        for (OrgUnit orgUnit : orgUnits) {
            if ("ROOT".equals(orgUnit.getUnitType())) {
                return orgUnit.getUnitName();
            }
        }
        return COMPANY_NAME;
    }

    private Map<String, OrgUnit> buildOrgUnitMap(List<OrgUnit> orgUnits) {
        Map<String, OrgUnit> orgUnitMap = new HashMap<>();
        for (OrgUnit orgUnit : orgUnits) {
            orgUnitMap.put(orgUnit.getUnitName(), orgUnit);
        }
        return orgUnitMap;
    }

    private Map<String, List<OrgUnit>> groupByParent(List<OrgUnit> orgUnits) {
        Map<String, List<OrgUnit>> childrenByParent = new HashMap<>();
        for (OrgUnit orgUnit : orgUnits) {
            if (orgUnit.getParentUnitName() == null || orgUnit.getParentUnitName().trim().isEmpty()) {
                continue;
            }
            childrenByParent.computeIfAbsent(orgUnit.getParentUnitName(), key -> new ArrayList<>()).add(orgUnit);
        }
        return childrenByParent;
    }

    private List<OrgUnit> getActiveOrgUnits() {
        return orgUnitMapper.getActiveOrgUnits();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
