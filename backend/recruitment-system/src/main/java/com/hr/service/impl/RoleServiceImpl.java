package com.hr.service.impl;

import com.hr.entity.Role;
import com.hr.mapper.RoleMapper;
import com.hr.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public List<Role> getAllRoles() {
        return roleMapper.getAllRoles();
    }

    @Override
    public Role getRoleById(Long roleId) {
        Role role = roleMapper.getRoleById(roleId);
        if (role == null) {
            return null;
        }
        role.setUserIds(roleMapper.getUserIdsByRoleId(roleId));
        return role;
    }

    @Override
    @Transactional
    public int createRole(Role role) {
        Integer roleNameCount = roleMapper.countByRoleName(role.getRoleName(), null);
        if (roleNameCount != null && roleNameCount > 0) {
            throw new RuntimeException("Role name already exists");
        }

        Date now = new Date();
        if (role.getStatus() == null || role.getStatus().trim().isEmpty()) {
            role.setStatus("ACTIVE");
        }
        if (role.getCreateTime() == null) {
            role.setCreateTime(now);
        }
        if (role.getUpdateTime() == null) {
            role.setUpdateTime(now);
        }
        if (role.getCreateUserId() == null || role.getCreateUserId().trim().isEmpty()) {
            role.setCreateUserId("1001");
        }
        if (role.getCreateUserName() == null || role.getCreateUserName().trim().isEmpty()) {
            role.setCreateUserName("SYSTEM");
        }
        if (role.getUpdateUserId() == null || role.getUpdateUserId().trim().isEmpty()) {
            role.setUpdateUserId(role.getCreateUserId());
        }
        if (role.getUpdateUserName() == null || role.getUpdateUserName().trim().isEmpty()) {
            role.setUpdateUserName(role.getCreateUserName());
        }
        if (role.getRoleCode() == null || role.getRoleCode().trim().isEmpty()) {
            role.setRoleCode("ROLE_" + System.currentTimeMillis());
        }

        int affected = roleMapper.insertRole(role);
        if (affected > 0) {
            bindRoleUsers(role.getRoleId(), role.getUserIds(), role.getCreateUserId(), role.getCreateUserName());
        }
        return affected;
    }

    @Override
    @Transactional
    public int updateRole(Long roleId, Role role) {
        Role existing = roleMapper.getRoleById(roleId);
        if (existing == null) {
            return 0;
        }

        Integer roleNameCount = roleMapper.countByRoleName(role.getRoleName(), roleId);
        if (roleNameCount != null && roleNameCount > 0) {
            throw new RuntimeException("Role name already exists");
        }

        role.setRoleId(roleId);
        role.setRoleCode(existing.getRoleCode());
        role.setUpdateTime(new Date());
        if (role.getUpdateUserId() == null || role.getUpdateUserId().trim().isEmpty()) {
            role.setUpdateUserId("1001");
        }
        if (role.getUpdateUserName() == null || role.getUpdateUserName().trim().isEmpty()) {
            role.setUpdateUserName("SYSTEM");
        }
        if (role.getStatus() == null || role.getStatus().trim().isEmpty()) {
            role.setStatus(existing.getStatus());
        }

        int affected = roleMapper.updateRole(role);
        if (affected > 0) {
            bindRoleUsers(roleId, role.getUserIds(), role.getUpdateUserId(), role.getUpdateUserName());
        }
        return affected;
    }

    @Override
    @Transactional
    public int deleteRole(Long roleId) {
        Role role = roleMapper.getRoleById(roleId);
        if (role == null) {
            return 0;
        }
        if ("SUPER_ADMIN".equals(role.getRoleCode())) {
            throw new RuntimeException("SUPER_ADMIN role cannot be deleted");
        }
        roleMapper.deleteUserRolesByRoleId(roleId);
        roleMapper.deleteRolePermissionsByRoleId(roleId);
        return roleMapper.deleteRole(roleId);
    }

    private void bindRoleUsers(Long roleId, List<String> userIds, String operatorId, String operatorName) {
        roleMapper.deleteUserRolesByRoleId(roleId);
        if (userIds != null && !userIds.isEmpty()) {
            roleMapper.batchInsertUserRoles(roleId, userIds, operatorId, operatorName);
        }
    }
}
