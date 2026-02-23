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

    @Override
    public EmailTemplate getDefaultOfferTemplate() {
        logger.debug("获取默认的录用邮件模板");
        
        try {
            // 首先尝试从数据库获取
            EmailTemplate template = emailTemplateMapper.selectByType("OFFER");
            
            if (template == null) {
                // 如果数据库中没有，创建默认模板
                logger.debug("数据库中没有录用邮件模板，创建默认模板");
                template = createDefaultOfferTemplate();
                save(template);
            }
            
            return template;
        } catch (Exception e) {
            logger.error("获取默认录用邮件模板失败: {}", e.getMessage());
            // 如果出现异常，返回一个内存中的默认模板
            return createDefaultOfferTemplate();
        }
    }
    
    /**
     * 创建默认的录用邮件模板
     * @return 默认邮件模板
     */
    private EmailTemplate createDefaultOfferTemplate() {
        EmailTemplate template = new EmailTemplate();
        template.setTemplateName("录用邀约模板");
        template.setTemplateType("OFFER");
        template.setSubject("录用邀约");
        template.setContent("亲爱的{name}：\n\n您好！\n\n非常高兴地通知您，经过我司的面试评估，您已通过所有面试环节，我们诚挚地邀请您加入我们的团队。\n\n【录用详情】\n岗位：{position}\n入职时间：{entryTime}\n\n我们相信您的加入将为公司带来新的活力和价值。如果您对录用条件有任何疑问，或需要进一步的信息，请随时与我们联系。\n\n期待您的回复！\n\n此致\n敬礼\n\n{companyName}\n{date}");
        template.setStatus("ACTIVE");
        return template;
    }
}
