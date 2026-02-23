package com.hr.controller;

import com.hr.entity.EmailTemplate;
import com.hr.service.EmailTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 邮件模板控制器
 * 用于处理邮件模板管理相关的HTTP请求
 */
@RestController
@RequestMapping("/api/email-template")
@CrossOrigin(origins = "*")
public class EmailTemplateController {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailTemplateController.class);
    
    @Autowired
    private EmailTemplateService emailTemplateService;

    /**
     * 保存邮件模板
     * @param emailTemplate 邮件模板对象
     * @return 响应结果
     */
    @PostMapping("/save")
    public Map<String, Object> save(@RequestBody EmailTemplate emailTemplate) {
        Map<String, Object> result = new HashMap<>();
        logger.debug("保存邮件模板请求: {}", emailTemplate);
        
        try {
            EmailTemplate savedEmailTemplate = emailTemplateService.save(emailTemplate);
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", savedEmailTemplate);
        } catch (Exception e) {
            logger.error("保存邮件模板失败: {}", e.getMessage());
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "保存邮件模板失败: " + e.getMessage());
            result.put("body", null);
        }
        
        return result;
    }

    /**
     * 根据ID查询邮件模板
     * @param id 邮件模板ID
     * @return 响应结果
     */
    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        logger.debug("根据ID查询邮件模板请求: {}", id);
        
        try {
            EmailTemplate emailTemplate = emailTemplateService.getById(id);
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", emailTemplate);
        } catch (Exception e) {
            logger.error("查询邮件模板失败: {}", e.getMessage());
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "查询邮件模板失败: " + e.getMessage());
            result.put("body", null);
        }
        
        return result;
    }

    /**
     * 查询所有邮件模板
     * @return 响应结果
     */
    @GetMapping("/list")
    public Map<String, Object> getAll() {
        Map<String, Object> result = new HashMap<>();
        logger.debug("查询所有邮件模板请求");
        
        try {
            List<EmailTemplate> emailTemplates = emailTemplateService.getAll();
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", emailTemplates);
        } catch (Exception e) {
            logger.error("查询邮件模板失败: {}", e.getMessage());
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "查询邮件模板失败: " + e.getMessage());
            result.put("body", null);
        }
        
        return result;
    }

    /**
     * 根据类型查询邮件模板
     * @param templateType 模板类型
     * @return 响应结果
     */
    @GetMapping("/type/{templateType}")
    public Map<String, Object> getByType(@PathVariable String templateType) {
        Map<String, Object> result = new HashMap<>();
        logger.debug("根据类型查询邮件模板请求: {}", templateType);
        
        try {
            EmailTemplate emailTemplate = emailTemplateService.getByType(templateType);
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", emailTemplate);
        } catch (Exception e) {
            logger.error("查询邮件模板失败: {}", e.getMessage());
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "查询邮件模板失败: " + e.getMessage());
            result.put("body", null);
        }
        
        return result;
    }

    /**
     * 获取默认的录用邮件模板
     * @return 响应结果
     */
    @GetMapping("/default/offer")
    public Map<String, Object> getDefaultOfferTemplate() {
        Map<String, Object> result = new HashMap<>();
        logger.debug("获取默认的录用邮件模板请求");
        
        try {
            EmailTemplate emailTemplate = emailTemplateService.getDefaultOfferTemplate();
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", emailTemplate);
        } catch (Exception e) {
            logger.error("获取默认的录用邮件模板失败: {}", e.getMessage());
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "获取默认的录用邮件模板失败: " + e.getMessage());
            result.put("body", null);
        }
        
        return result;
    }

    /**
     * 更新邮件模板状态
     * @param id 邮件模板ID
     * @param status 新状态
     * @return 响应结果
     */
    @PutMapping("/{id}/status")
    public Map<String, Object> updateStatus(@PathVariable Long id, @RequestParam String status) {
        Map<String, Object> result = new HashMap<>();
        logger.debug("更新邮件模板状态请求: id={}, status={}", id, status);
        
        try {
            EmailTemplate emailTemplate = emailTemplateService.getById(id);
            if (emailTemplate == null) {
                result.put("returnCode", "ERR0001");
                result.put("errorMsg", "邮件模板不存在");
                result.put("body", null);
                return result;
            }
            
            emailTemplate.setStatus(status);
            EmailTemplate updatedEmailTemplate = emailTemplateService.save(emailTemplate);
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", updatedEmailTemplate);
        } catch (Exception e) {
            logger.error("更新邮件模板状态失败: {}", e.getMessage());
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "更新邮件模板状态失败: " + e.getMessage());
            result.put("body", null);
        }
        
        return result;
    }
}
