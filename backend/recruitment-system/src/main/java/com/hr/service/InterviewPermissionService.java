package com.hr.service;

import com.hr.entity.User;

import java.util.Set;

public interface InterviewPermissionService {
    String ROLE_SUPPLIER_HR = "供应商HR";
    String ROLE_OUTSOURCING_MANAGER = "外包招聘管理岗";
    String ROLE_INTERVIEWER = "面试官";
    String ROLE_ROOM_MANAGER = "室经理";
    String ROLE_TEAM_MANAGER = "团队经理";
    String ROLE_SUPER_ADMIN = "超级管理员";
    String SYSTEM_USER_ID = "1001";

    Set<String> normalizeRoles(User user, String rawRoleText);

    boolean isSuperAdmin(String userId, Set<String> roles);

    boolean hasRole(Set<String> roles, String targetRole);

    String normalizeRoleName(String roleName);
}
