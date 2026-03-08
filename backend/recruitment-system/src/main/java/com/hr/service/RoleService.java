package com.hr.service;

import com.hr.entity.Role;

import java.util.List;

public interface RoleService {

    List<Role> getAllRoles();

    Role getRoleById(Long roleId);

    int createRole(Role role);

    int updateRole(Long roleId, Role role);

    int deleteRole(Long roleId);
}
