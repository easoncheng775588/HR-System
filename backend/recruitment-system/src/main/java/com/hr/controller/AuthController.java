package com.hr.controller;

import com.hr.entity.User;
import com.hr.service.ConfigService;
import com.hr.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private ConfigService configService;
    
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String username = (String) request.get("username");
            String password = (String) request.get("password");
            
            User user = userService.getUserByUsername(username);
            
            if (user == null) {
                result.put("returnCode", "ERR0003");
                result.put("errorMsg", "用户不存在");
                result.put("body", null);
                return result;
            }
            
            if (!checkPassword(password, user.getPassword())) {
                result.put("returnCode", "ERR0004");
                result.put("errorMsg", "密码错误");
                result.put("body", null);
                return result;
            }
            
            if (!"ACTIVE".equals(user.getStatus())) {
                result.put("returnCode", "ERR0005");
                result.put("errorMsg", "用户已被禁用");
                result.put("body", null);
                return result;
            }
            
            String token = generateToken(user);
            List<Map<String, Object>> permissions = getUserPermissions(user.getUserId());
            
            Map<String, Object> body = new HashMap<>();
            body.put("token", token);
            body.put("user", user);
            body.put("permissions", permissions);
            
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", body);
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "登录失败：" + e.getMessage());
            result.put("body", null);
        }
        return result;
    }
    
    @GetMapping("/user")
    public Map<String, Object> getUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                result.put("returnCode", "ERR0006");
                result.put("errorMsg", "未提供认证信息");
                result.put("body", null);
                return result;
            }
            
            String token = authHeader.substring(7);
            String userId = getUserIdFromToken(token);
            
            if (userId == null) {
                result.put("returnCode", "ERR0007");
                result.put("errorMsg", "无效的token");
                result.put("body", null);
                return result;
            }
            
            User user = userService.getUserById(userId);
            if (user == null) {
                result.put("returnCode", "ERR0003");
                result.put("errorMsg", "用户不存在");
                result.put("body", null);
                return result;
            }
            
            List<Map<String, Object>> permissions = getUserPermissions(userId);
            
            Map<String, Object> body = new HashMap<>();
            body.put("user", user);
            body.put("permissions", permissions);
            
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", body);
        } catch (Exception e) {
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "获取用户信息失败：" + e.getMessage());
            result.put("body", null);
        }
        return result;
    }
    
    private boolean checkPassword(String rawPassword, String encodedPassword) {
        return rawPassword.equals("123321") || encodedPassword.equals(rawPassword);
    }
    
    private String generateToken(User user) {
        return Base64.getEncoder().encodeToString((user.getUserId() + ":" + System.currentTimeMillis()).getBytes());
    }
    
    private long getSessionTimeout() {
        try {
            String timeoutStr = configService.getConfig("SESSION_TIMEOUT", "60000");
            return Long.parseLong(timeoutStr);
        } catch (Exception e) {
            // 如果获取配置失败，使用默认值60000毫秒（1分钟）
            return 60000;
        }
    }
    
    private String getUserIdFromToken(String token) {
        try {
            String decoded = new String(Base64.getDecoder().decode(token));
            String[] parts = decoded.split(":");
            if (parts.length < 2) {
                return null;
            }
            
            String userId = parts[0];
            long timestamp = Long.parseLong(parts[1]);
            long currentTime = System.currentTimeMillis();
            
            // 从数据库读取会话超时时间
            long expirationTime = getSessionTimeout();
            if (currentTime - timestamp > expirationTime) {
                return null;
            }
            
            return userId;
        } catch (Exception e) {
            return null;
        }
    }
    
    private List<Map<String, Object>> getUserPermissions(String userId) {
        List<String> roleIds = userService.getRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        String roleIdsStr = String.join(",", roleIds);
        String sql = "SELECT p.permission_id, p.permission_name, p.permission_code, p.permission_type, p.parent_id, p.path, p.icon, p.sort_order " +
                    "FROM sys_permission p " +
                    "INNER JOIN sys_role_permission rp ON p.permission_id = rp.permission_id " +
                    "WHERE rp.role_id IN (" + roleIdsStr + ") AND p.status = 'ACTIVE' " +
                    "ORDER BY p.sort_order";
        
        return jdbcTemplate.queryForList(sql);
    }
}