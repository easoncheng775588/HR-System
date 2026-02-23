package com.hr.service;

import com.hr.entity.EmailTemplate;
import java.util.List;

/**
 * 邮件模板服务接口
 * 用于定义邮件模板管理的业务逻辑方法
 */
public interface EmailTemplateService {
    /**
     * 保存邮件模板
     * @param emailTemplate 邮件模板对象
     * @return 保存后的邮件模板对象
     */
    EmailTemplate save(EmailTemplate emailTemplate);

    /**
     * 根据ID查询邮件模板
     * @param id 邮件模板ID
     * @return 邮件模板对象
     */
    EmailTemplate getById(Long id);

    /**
     * 查询所有邮件模板
     * @return 邮件模板列表
     */
    List<EmailTemplate> getAll();

    /**
     * 根据类型查询邮件模板
     * @param templateType 模板类型
     * @return 邮件模板对象
     */
    EmailTemplate getByType(String templateType);

    /**
     * 根据状态查询邮件模板
     * @param status 模板状态
     * @return 邮件模板列表
     */
    List<EmailTemplate> getByStatus(String status);

    /**
     * 获取默认的录用邮件模板
     * @return 邮件模板对象
     */
    EmailTemplate getDefaultOfferTemplate();
}
