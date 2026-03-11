package com.hr.controller;

import com.hr.entity.User;
import com.hr.service.UserService;
import com.hr.common.ErrorMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
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
            result.put("errorMsg", ErrorMessage.get("user.getListFailed", e.getMessage()));
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
            result.put("errorMsg", ErrorMessage.get("user.getInfoFailed", e.getMessage()));
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
                Map<String, Object> body = new HashMap<>();
                body.put("userId", user.getUserId());
                result.put("body", body);
            } else {
                result.put("returnCode", "ERR0002");
                result.put("errorMsg", ErrorMessage.get("user.createFailed"));
                result.put("body", null);
            }
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", ErrorMessage.get("user.createFailedException", e.getMessage()));
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
                result.put("body", userService.getUserById(userId));
            } else {
                result.put("returnCode", "ERR0002");
                result.put("errorMsg", ErrorMessage.get("user.updateFailed"));
                result.put("body", null);
            }
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", ErrorMessage.get("user.updateFailedException", e.getMessage()));
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
                result.put("errorMsg", ErrorMessage.get("user.deleteFailed"));
                result.put("body", null);
            }
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", ErrorMessage.get("user.deleteFailedException", e.getMessage()));
            result.put("body", null);
        }
        return result;
    }
    
    @GetMapping("/by-position/{position}")
    public Map<String, Object> getUsersByPosition(@PathVariable String position) {
        Map<String, Object> result = new HashMap<>();
        try {
            logger.debug("接收到的position参数：{}", position);
            List<User> users = userService.getUsersByPosition(position);
            logger.debug("查询到的用户数量：{}", users.size());
            for (User user : users) {
                logger.debug("用户：{}，姓名：{}，岗位：{}", user.getUserId(), user.getRealName(), user.getPosition());
            }
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", users);
        } catch (Exception e) {
            logger.debug("获取用户列表失败：{}", e.getMessage());
            logger.error("获取用户列表失败", e);
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "获取用户列表失败：" + e.getMessage());
            result.put("body", null);
        }
        return result;
    }

    @GetMapping("/search")
    public Map<String, Object> searchUsers(@RequestParam(name = "keyword", required = false) String keyword) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<User> users = userService.searchActiveUsers(keyword);
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", users);
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "搜索用户失败: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }

    @GetMapping("/department-options")
    public Map<String, Object> getDepartmentOptions() {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", userService.getDepartmentOptions());
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "获取部门选项失败: " + e.getMessage());
            result.put("body", null);
        }
        return result;
    }
}
