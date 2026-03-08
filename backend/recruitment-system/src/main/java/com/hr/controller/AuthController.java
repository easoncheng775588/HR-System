package com.hr.controller;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.hr.entity.User;
import com.hr.service.ConfigService;
import com.hr.service.UserService;
import com.hr.common.ErrorMessage;

/**
 * 认证控制器
 * 处理用户登录、获取用户信息等认证相关操作
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private ConfigService configService;
    
/**
 * 用户登录
 * @param request 登录请求参数，包含username和password
 * @return 登录结果，包含token、用户信息和权限列表
 */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 验证请求参数
            if (request == null) {
                logger.warn("登录请求参数为空");
                result.put("returnCode", "ERR0001");
                result.put("errorMsg", ErrorMessage.get("auth.login.empty.request"));
                result.put("body", null);
                return result;
            }
            
            String username = (String) request.get("username");
            // 验证用户名
            if (username == null || username.trim().isEmpty()) {
                logger.warn("用户名为空");
                result.put("returnCode", "ERR0001");
                result.put("errorMsg", ErrorMessage.get("auth.login.empty.username"));
                result.put("body", null);
                return result;
            }
            
            if (username.length() > 50) {
                logger.warn("用户名过长：username={}", username);
                result.put("returnCode", "ERR0001");
                result.put("errorMsg", ErrorMessage.get("auth.login.username.too.long"));
                result.put("body", null);
                return result;
            }
            
            logger.info("用户登录请求：username={}", username);
            
            String password = (String) request.get("password");
            // 验证密码
            if (password == null || password.trim().isEmpty()) {
                logger.warn("密码为空：username={}", username);
                result.put("returnCode", "ERR0001");
                result.put("errorMsg", ErrorMessage.get("auth.login.empty.password"));
                result.put("body", null);
                return result;
            }
            
            if (password.length() > 100) {
                logger.warn("密码过长：username={}", username);
                result.put("returnCode", "ERR0001");
                result.put("errorMsg", ErrorMessage.get("auth.login.password.too.long"));
                result.put("body", null);
                return result;
            }
            
            User user = userService.getUserByUsername(username);
            
            if (user == null) {
                logger.warn("用户不存在：username={}", username);
                result.put("returnCode", "ERR0003");
                result.put("errorMsg", ErrorMessage.get("auth.login.user.not.exist"));
                result.put("body", null);
                return result;
            }
            
            if (!checkPassword(password, user.getPassword())) {
                logger.warn("密码错误：username={}", username);
                result.put("returnCode", "ERR0004");
                result.put("errorMsg", ErrorMessage.get("auth.login.password.wrong"));
                result.put("body", null);
                return result;
            }
            
            if (!"ACTIVE".equals(user.getStatus())) {
                logger.warn("用户已被禁用：username={}, status={}", username, user.getStatus());
                result.put("returnCode", "ERR0005");
                result.put("errorMsg", ErrorMessage.get("auth.login.user.disabled"));
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
            
            logger.info("用户登录成功：username={}, userId={}", username, user.getUserId());
        } catch (NullPointerException e) {
            logger.error("登录失败：参数错误", e);
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", ErrorMessage.get("auth.login.param.error"));
            result.put("body", null);
        } catch (IllegalArgumentException e) {
            logger.error("登录失败：参数格式错误", e);
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", ErrorMessage.get("auth.login.param.format.error"));
            result.put("body", null);
        } catch (Exception e) {
            logger.error("登录失败：{}", e.getMessage(), e);
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", ErrorMessage.get("auth.login.failed", e.getMessage()));
            result.put("body", null);
        }
        return result;
    }
    
/**
 * 获取用户信息
 * @param authHeader 认证头信息，包含Bearer token
 * @return 用户信息和权限列表
 */
    @GetMapping("/user")
    public Map<String, Object> getUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> result = new HashMap<>();
        try {
            logger.info("获取用户信息请求");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("未提供认证信息");
                result.put("returnCode", "ERR0006");
                result.put("errorMsg", ErrorMessage.get("auth.no.auth.info"));
                result.put("body", null);
                return result;
            }
            
            String token = authHeader.substring(7);
            String userId = getUserIdFromToken(token);
            
            if (userId == null) {
                logger.warn("无效的token");
                result.put("returnCode", "ERR0007");
                result.put("errorMsg", ErrorMessage.get("auth.invalid.token"));
                result.put("body", null);
                return result;
            }
            
            User user = userService.getUserById(userId);
            if (user == null) {
                logger.warn("用户不存在：userId={}", userId);
                result.put("returnCode", "ERR0003");
                result.put("errorMsg", ErrorMessage.get("auth.user.not.exist"));
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
            
            logger.info("获取用户信息成功：userId={}, username={}", userId, user.getUsername());
        } catch (NullPointerException e) {
            logger.error("获取用户信息失败：参数错误", e);
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", ErrorMessage.get("auth.getUserInfoParamError"));
            result.put("body", null);
        } catch (IllegalArgumentException e) {
            logger.error("获取用户信息失败：参数格式错误", e);
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", ErrorMessage.get("auth.getUserInfoParamFormatError"));
            result.put("body", null);
        } catch (Exception e) {
            logger.error("获取用户信息失败：{}", e.getMessage(), e);
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", ErrorMessage.get("auth.getUserInfoFailed", e.getMessage()));
            result.put("body", null);
        }
        return result;
    }
    
/**
 * 验证密码
 * @param rawPassword 原始密码
 * @param encodedPassword 加密后的密码
 * @return 密码是否正确
 */
    private boolean checkPassword(String rawPassword, String encodedPassword) {
        logger.debug("验证密码: rawPassword={}, encodedPassword={}, encodedPassword.length()={}", rawPassword, encodedPassword, encodedPassword != null ? encodedPassword.length() : 0);
        
        // 首先检查原始密码是否与数据库中的密码直接匹配
        if (rawPassword.equals(encodedPassword)) {
            logger.debug("密码直接匹配成功");
            return true;
        }
        
        // 然后尝试使用BCrypt验证
        try {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            boolean matches = encoder.matches(rawPassword, encodedPassword);
            logger.debug("BCrypt验证结果: {}", matches);
            return matches;
        } catch (Exception e) {
            // 如果BCrypt验证失败，返回false
            logger.debug("BCrypt验证失败: {}", e.getMessage());
            return false;
        }
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