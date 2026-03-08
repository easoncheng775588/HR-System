package com.hr.mapper;

import com.hr.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMapper {

    List<Role> getAllRoles();

    Role getRoleById(@Param("roleId") Long roleId);

    int insertRole(Role role);

    int updateRole(Role role);

    int deleteRole(@Param("roleId") Long roleId);

    int deleteUserRolesByRoleId(@Param("roleId") Long roleId);

    int deleteRolePermissionsByRoleId(@Param("roleId") Long roleId);

    int batchInsertUserRoles(@Param("roleId") Long roleId,
                             @Param("userIds") List<String> userIds,
                             @Param("createUserId") String createUserId,
                             @Param("createUserName") String createUserName);

    List<String> getUserIdsByRoleId(@Param("roleId") Long roleId);

    Integer countByRoleName(@Param("roleName") String roleName, @Param("excludeRoleId") Long excludeRoleId);
}
