package com.hr.common;

import java.util.HashMap;
import java.util.Map;

public class ErrorMessage {
    
    private static final Map<String, String> ERROR_MESSAGES = new HashMap<>();
    
    static {
        initErrorMessages();
    }
    
    private static void initErrorMessages() {
        AuthControllerMessages.init();
        UserControllerMessages.init();
        ResumeControllerMessages.init();
        InterviewRecordControllerMessages.init();
        OfferRecordControllerMessages.init();
        FileUploadControllerMessages.init();
        SysParamControllerMessages.init();
        EmailTemplateControllerMessages.init();
    }
    
    public static String get(String key) {
        return ERROR_MESSAGES.getOrDefault(key, "未知错误");
    }
    
    public static String get(String key, Object... args) {
        String message = ERROR_MESSAGES.get(key);
        if (message == null) {
            return "未知错误";
        }
        return String.format(message, args);
    }
    
    public static class AuthControllerMessages {
        private static void init() {
            ERROR_MESSAGES.put("auth.login.empty.request", "登录失败：请求参数为空");
            ERROR_MESSAGES.put("auth.login.empty.username", "登录失败：用户名不能为空");
            ERROR_MESSAGES.put("auth.login.username.too.long", "登录失败：用户名长度不能超过50个字符");
            ERROR_MESSAGES.put("auth.login.empty.password", "登录失败：密码不能为空");
            ERROR_MESSAGES.put("auth.login.password.too.long", "登录失败：密码长度不能超过100个字符");
            ERROR_MESSAGES.put("auth.login.user.not.exist", "用户不存在");
            ERROR_MESSAGES.put("auth.login.password.wrong", "密码错误");
            ERROR_MESSAGES.put("auth.login.user.disabled", "用户已被禁用");
            ERROR_MESSAGES.put("auth.login.param.error", "登录失败：参数错误");
            ERROR_MESSAGES.put("auth.login.param.format.error", "登录失败：参数格式错误");
            ERROR_MESSAGES.put("auth.login.failed", "登录失败：%s");
            ERROR_MESSAGES.put("auth.no.auth.info", "未提供认证信息");
            ERROR_MESSAGES.put("auth.invalid.token", "无效的token");
            ERROR_MESSAGES.put("auth.user.not.exist", "用户不存在");
            ERROR_MESSAGES.put("auth.get.user.info.param.error", "获取用户信息失败：参数错误");
            ERROR_MESSAGES.put("auth.get.user.info.param.format.error", "获取用户信息失败：参数格式错误");
            ERROR_MESSAGES.put("auth.get.user.info.failed", "获取用户信息失败：%s");
        }
    }
    
    public static class UserControllerMessages {
        private static void init() {
            ERROR_MESSAGES.put("user.get.list.failed", "获取用户列表失败：%s");
            ERROR_MESSAGES.put("user.get.info.failed", "获取用户信息失败：%s");
            ERROR_MESSAGES.put("user.create.failed", "用户创建失败");
            ERROR_MESSAGES.put("user.create.failed.exception", "用户创建失败：%s");
            ERROR_MESSAGES.put("user.update.failed", "用户更新失败");
            ERROR_MESSAGES.put("user.update.failed.exception", "用户更新失败：%s");
            ERROR_MESSAGES.put("user.delete.failed", "用户删除失败");
            ERROR_MESSAGES.put("user.delete.failed.exception", "用户删除失败：%s");
            ERROR_MESSAGES.put("user.search.failed", "获取用户列表失败：%s");
        }
    }
    
    public static class ResumeControllerMessages {
        private static void init() {
            ERROR_MESSAGES.put("resume.submit.failed", "提交简历失败：%s");
            ERROR_MESSAGES.put("resume.get.failed", "查询简历失败：%s");
            ERROR_MESSAGES.put("resume.get.by.id.failed", "查询简历失败：%s");
            ERROR_MESSAGES.put("resume.get.by.request.failed", "查询简历失败：%s");
            ERROR_MESSAGES.put("resume.update.status.failed", "更新简历状态失败：%s");
        }
    }
    
    public static class InterviewRecordControllerMessages {
        private static void init() {
            ERROR_MESSAGES.put("interview.save.failed", "保存面试记录失败：%s");
            ERROR_MESSAGES.put("interview.get.failed", "查询面试记录失败：%s");
            ERROR_MESSAGES.put("interview.get.by.resume.failed", "查询面试记录失败：%s");
            ERROR_MESSAGES.put("interview.get.by.request.failed", "查询面试记录失败：%s");
            ERROR_MESSAGES.put("interview.update.result.failed", "更新面试结果失败：%s");
            ERROR_MESSAGES.put("interview.get.pending.count.failed", "获取待面试数量失败：%s");
            ERROR_MESSAGES.put("interview.delete.failed", "删除面试记录失败：%s");
        }
    }
    
    public static class OfferRecordControllerMessages {
        private static void init() {
            ERROR_MESSAGES.put("offer.save.failed", "保存录用记录失败: %s");
            ERROR_MESSAGES.put("offer.get.failed", "查询录用记录失败: %s");
            ERROR_MESSAGES.put("offer.get.by.id.failed", "查询录用记录失败: %s");
            ERROR_MESSAGES.put("offer.get.by.resume.failed", "查询录用记录失败: %s");
            ERROR_MESSAGES.put("offer.get.by.request.failed", "查询录用记录失败: %s");
            ERROR_MESSAGES.put("offer.get.eligible.failed", "获取可录用的候选人失败: %s");
            ERROR_MESSAGES.put("offer.send.email.failed", "发送录用邮件失败: %s");
            ERROR_MESSAGES.put("offer.not.exist", "录用记录不存在");
            ERROR_MESSAGES.put("offer.update.status.failed", "更新录用状态失败: %s");
        }
    }
    
    public static class FileUploadControllerMessages {
        private static void init() {
            ERROR_MESSAGES.put("file.empty", "文件为空");
            ERROR_MESSAGES.put("file.create.dir.failed", "无法创建上传目录: %s");
            ERROR_MESSAGES.put("file.name.invalid", "文件名格式无效");
            ERROR_MESSAGES.put("file.upload.failed", "文件上传失败：%s");
            ERROR_MESSAGES.put("file.upload.exception", "上传失败：%s");
        }
    }
    
    public static class SysParamControllerMessages {
        private static void init() {
            ERROR_MESSAGES.put("param.get.failed", "查询参数失败: %s");
            ERROR_MESSAGES.put("param.get.by.type.failed", "查询参数失败: %s");
            ERROR_MESSAGES.put("param.get.by.code.failed", "查询参数失败: %s");
            ERROR_MESSAGES.put("param.get.by.type.and.code.failed", "查询参数失败: %s");
            ERROR_MESSAGES.put("param.save.failed", "保存参数失败");
            ERROR_MESSAGES.put("param.save.failed.exception", "保存参数失败: %s");
        }
    }
    
    public static class EmailTemplateControllerMessages {
        private static void init() {
            ERROR_MESSAGES.put("email.template.get.failed", "查询邮件模板失败：%s");
            ERROR_MESSAGES.put("email.template.get.by.type.failed", "查询邮件模板失败：%s");
            ERROR_MESSAGES.put("email.template.save.failed", "保存邮件模板失败：%s");
            ERROR_MESSAGES.put("email.template.delete.failed", "删除邮件模板失败：%s");
        }
    }
    
    public static class RecruitmentRequestControllerMessages {
        private static void init() {
            ERROR_MESSAGES.put("request.submit.failed", "提交用人申请失败：%s");
            ERROR_MESSAGES.put("request.get.failed", "查询用人申请失败：%s");
            ERROR_MESSAGES.put("request.get.by.id.failed", "查询用人申请失败：%s");
            ERROR_MESSAGES.put("request.update.failed", "更新用人申请失败：%s");
            ERROR_MESSAGES.put("request.delete.failed", "删除用人申请失败：%s");
            ERROR_MESSAGES.put("request.approve.failed", "审批用人申请失败：%s");
        }
    }
}
