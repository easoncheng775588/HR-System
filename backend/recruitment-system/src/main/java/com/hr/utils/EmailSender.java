package com.hr.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 邮件发送工具类
 * 用于发送邮件
 */
@Component
public class EmailSender {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailSender.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String from;

    /**
     * 发送简单邮件
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     * @return 是否发送成功
     */
    public boolean sendSimpleEmail(String to, String subject, String content) {
        logger.debug("发送简单邮件: to={}, subject={}, content={}", to, subject, content);
        
        try {
            // 检查邮件配置是否正确
            if ("your_email@qq.com".equals(from)) {
                // 配置不正确，返回失败
                logger.error("邮件配置未设置，请在application.properties中配置正确的邮箱信息");
                return false;
            }
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            logger.info("邮件发送成功: to={}", to);
            return true;
        } catch (Exception e) {
            logger.error("邮件发送失败: {}", e.getMessage());
            return false;
        }
    }
}
