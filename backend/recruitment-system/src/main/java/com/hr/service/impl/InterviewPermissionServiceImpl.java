package com.hr.service.impl;

import com.hr.entity.User;
import com.hr.mapper.UserMapper;
import com.hr.service.InterviewPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class InterviewPermissionServiceImpl implements InterviewPermissionService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Set<String> normalizeRoles(User user, String rawRoleText) {
        Set<String> roles = new LinkedHashSet<>();
        if (user != null) {
            if (SYSTEM_USER_ID.equals(user.getUserId())) {
                roles.add(ROLE_SUPER_ADMIN);
            }
            List<String> roleNames = userMapper.getRoleNamesByUserId(user.getUserId());
            if (roleNames != null) {
                for (String roleName : roleNames) {
                    String normalized = normalizeRoleName(roleName);
                    if (!normalized.isEmpty()) {
                        roles.add(normalized);
                    }
                }
            }
            String normalizedPosition = normalizeRoleName(user.getPosition());
            if (!normalizedPosition.isEmpty()) {
                roles.add(normalizedPosition);
            }
        }
        String normalizedText = normalizeRoleName(rawRoleText);
        if (!normalizedText.isEmpty()) {
            roles.add(normalizedText);
        }
        return roles;
    }

    @Override
    public boolean isSuperAdmin(String userId, Set<String> roles) {
        return SYSTEM_USER_ID.equals(userId) || hasRole(roles, ROLE_SUPER_ADMIN);
    }

    @Override
    public boolean hasRole(Set<String> roles, String targetRole) {
        return roles != null && roles.contains(targetRole);
    }

    @Override
    public String normalizeRoleName(String roleName) {
        if (roleName == null) {
            return "";
        }
        String value = roleName.trim();
        if (value.isEmpty()) {
            return "";
        }
        if (value.contains("供应商HR")) {
            return ROLE_SUPPLIER_HR;
        }
        if (value.contains("外包招聘管理")) {
            return ROLE_OUTSOURCING_MANAGER;
        }
        if (value.contains("面试官")) {
            return ROLE_INTERVIEWER;
        }
        if (value.contains("室经理")) {
            return ROLE_ROOM_MANAGER;
        }
        if (value.contains("团队经理")) {
            return ROLE_TEAM_MANAGER;
        }
        if (value.contains("管理员") || value.contains("系统管理员")) {
            return ROLE_SUPER_ADMIN;
        }
        return value;
    }
}
