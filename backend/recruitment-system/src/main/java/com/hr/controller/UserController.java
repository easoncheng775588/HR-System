package com.hr.controller;

import com.hr.entity.User;
import com.hr.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public Map<String, Object> getAllUsers() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<User> users = userService.getAllUsers();
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", users);
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "获取用户列表失败：" + e.getMessage());
            result.put("body", null);
        }
        return result;
    }
    
    @GetMapping("/{userId}")
    public Map<String, Object> getUserById(@PathVariable String userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            User user = userService.getUserById(userId);
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", user);
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "获取用户信息失败：" + e.getMessage());
            result.put("body", null);
        }
        return result;
    }
    
    @PostMapping
    public Map<String, Object> createUser(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        try {
            int count = userService.createUser(user);
            if (count > 0) {
                result.put("returnCode", "SUC0000");
                result.put("errorMsg", "");
                result.put("body", "用户创建成功");
            } else {
                result.put("returnCode", "ERR0002");
                result.put("errorMsg", "用户创建失败");
                result.put("body", null);
            }
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "用户创建失败：" + e.getMessage());
            result.put("body", null);
        }
        return result;
    }
    
    @PutMapping("/{userId}")
    public Map<String, Object> updateUser(@PathVariable String userId, @RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        try {
            user.setUserId(userId);
            int count = userService.updateUser(user);
            if (count > 0) {
                result.put("returnCode", "SUC0000");
                result.put("errorMsg", "");
                result.put("body", "用户更新成功");
            } else {
                result.put("returnCode", "ERR0002");
                result.put("errorMsg", "用户更新失败");
                result.put("body", null);
            }
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "用户更新失败：" + e.getMessage());
            result.put("body", null);
        }
        return result;
    }
    
    @DeleteMapping("/{userId}")
    public Map<String, Object> deleteUser(@PathVariable String userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            int count = userService.deleteUser(userId);
            if (count > 0) {
                result.put("returnCode", "SUC0000");
                result.put("errorMsg", "");
                result.put("body", "用户删除成功");
            } else {
                result.put("returnCode", "ERR0002");
                result.put("errorMsg", "用户删除失败");
                result.put("body", null);
            }
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "用户删除失败：" + e.getMessage());
            result.put("body", null);
        }
        return result;
    }
}