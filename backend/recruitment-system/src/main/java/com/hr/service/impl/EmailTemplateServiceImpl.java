package com.hr.service.impl;

import com.hr.entity.EmailTemplate;
import com.hr.mapper.EmailTemplateMapper;
import com.hr.service.EmailTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

/**
 * 邮件模板服务实现类
 * 实现邮件模板管理的业务逻辑
 */
@Service
public class EmailTemplateServiceImpl implements EmailTemplateService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailTemplateServiceImpl.class);
    
    @Autowired
    private EmailTemplateMapper emailTemplateMapper;

    @Override
    @Transactional
    public EmailTemplate save(EmailTemplate emailTemplate) {
        logger.debug("保存邮件模板: {}", emailTemplate);
        
        Date now = new Date();
        if (emailTemplate.getTemplateId() == null) {
            // 新增邮件模板
            emailTemplate.setCreateTime(now);
            emailTemplate.setUpdateTime(now);
            emailTemplate.setStatus("ACTIVE");
            emailTemplateMapper.insert(emailTemplate);
        } else {
            // 更新邮件模板
            emailTemplate.setUpdateTime(now);
            emailTemplateMapper.updateByPrimaryKey(emailTemplate);
        }
        
        return emailTemplate;
    }

    @Override
    public EmailTemplate getById(Long id) {
        logger.debug("根据ID查询邮件模板: {}", id);
        return emailTemplateMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<EmailTemplate> getAll() {
        logger.debug("查询所有邮件模板");
        return emailTemplateMapper.selectAll();
    }

    @Override
    public EmailTemplate getByType(String templateType) {
        logger.debug("根据类型查询邮件模板: {}", templateType);
        return emailTemplateMapper.selectByType(templateType);
    }

    @Override
    public List<EmailTemplate> getByStatus(String status) {
        logger.debug("根据状态查询邮件模板: {}", status);
        return emailTemplateMapper.selectByStatus(status);
    }
}
