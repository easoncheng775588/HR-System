package com.hr.controller;

import com.hr.entity.Role;
import com.hr.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping
    public Map<String, Object> getAllRoles() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Role> roles = roleService.getAllRoles();
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", roles);
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "Failed to get role list: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }

    @GetMapping("/{roleId}")
    public Map<String, Object> getRoleById(@PathVariable Long roleId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Role role = roleService.getRoleById(roleId);
            if (role == null) {
                result.put("returnCode", "ERR0004");
                result.put("errorMsg", "Role does not exist");
                result.put("body", null);
                return result;
            }

            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", role);
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "Failed to get role detail: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }

    @PostMapping
    public Map<String, Object> createRole(@RequestBody Role role) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (role == null || role.getRoleName() == null || role.getRoleName().trim().isEmpty()) {
                result.put("returnCode", "ERR0003");
                result.put("errorMsg", "Role name cannot be empty");
                result.put("body", null);
                return result;
            }

            int count = roleService.createRole(role);
            if (count > 0) {
                result.put("returnCode", "SUC0000");
                result.put("errorMsg", "");
                result.put("body", "Role created successfully");
            } else {
                result.put("returnCode", "ERR0002");
                result.put("errorMsg", "Failed to create role");
                result.put("body", null);
            }
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "Failed to create role: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }

    @PutMapping("/{roleId}")
    public Map<String, Object> updateRole(@PathVariable Long roleId, @RequestBody Role role) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (role == null || role.getRoleName() == null || role.getRoleName().trim().isEmpty()) {
                result.put("returnCode", "ERR0003");
                result.put("errorMsg", "Role name cannot be empty");
                result.put("body", null);
                return result;
            }

            int count = roleService.updateRole(roleId, role);
            if (count > 0) {
                result.put("returnCode", "SUC0000");
                result.put("errorMsg", "");
                result.put("body", "Role updated successfully");
            } else {
                result.put("returnCode", "ERR0004");
                result.put("errorMsg", "Role does not exist");
                result.put("body", null);
            }
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "Failed to update role: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }

    @DeleteMapping("/{roleId}")
    public Map<String, Object> deleteRole(@PathVariable Long roleId) {
        Map<String, Object> result = new HashMap<>();
        try {
            int count = roleService.deleteRole(roleId);
            if (count > 0) {
                result.put("returnCode", "SUC0000");
                result.put("errorMsg", "");
                result.put("body", "Role deleted successfully");
            } else {
                result.put("returnCode", "ERR0004");
                result.put("errorMsg", "Role does not exist");
                result.put("body", null);
            }
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "Failed to delete role: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }
}
