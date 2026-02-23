package com.hr.mapper;

import com.hr.entity.EmailTemplate;
import java.util.List;

/**
 * 邮件模板Mapper接口
 * 用于邮件模板的数据库操作
 */
public interface EmailTemplateMapper {
    /**
     * 保存邮件模板
     * @param emailTemplate 邮件模板对象
     * @return 影响的行数
     */
    int insert(EmailTemplate emailTemplate);

    /**
     * 更新邮件模板
     * @param emailTemplate 邮件模板对象
     * @return 影响的行数
     */
    int updateByPrimaryKey(EmailTemplate emailTemplate);

    /**
     * 根据ID查询邮件模板
     * @param id 邮件模板ID
     * @return 邮件模板对象
     */
    EmailTemplate selectByPrimaryKey(Long id);

    /**
     * 查询所有邮件模板
     * @return 邮件模板列表
     */
    List<EmailTemplate> selectAll();

    /**
     * 根据类型查询邮件模板
     * @param templateType 模板类型
     * @return 邮件模板对象
     */
    EmailTemplate selectByType(String templateType);

    /**
     * 根据状态查询邮件模板
     * @param status 模板状态
     * @return 邮件模板列表
     */
    List<EmailTemplate> selectByStatus(String status);
}
