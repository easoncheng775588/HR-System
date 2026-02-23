package com.hr.service;

import com.hr.entity.OfferRecord;
import java.util.List;

/**
 * 录用记录服务接口
 * 用于定义录用管理的业务逻辑方法
 */
public interface OfferRecordService {
    /**
     * 保存录用记录
     * @param offerRecord 录用记录对象
     * @return 保存后的录用记录对象
     */
    OfferRecord save(OfferRecord offerRecord);

    /**
     * 根据ID查询录用记录
     * @param id 录用记录ID
     * @return 录用记录对象
     */
    OfferRecord getById(Long id);

    /**
     * 查询所有录用记录
     * @return 录用记录列表
     */
    List<OfferRecord> getAll();

    /**
     * 根据简历ID查询录用记录
     * @param resumeId 简历ID
     * @return 录用记录对象
     */
    OfferRecord getByResumeId(Long resumeId);

    /**
     * 根据招聘申请ID查询录用记录
     * @param recruitmentRequestId 招聘申请ID
     * @return 录用记录列表
     */
    List<OfferRecord> getByRecruitmentRequestId(Long recruitmentRequestId);

    /**
     * 根据状态查询录用记录
     * @param status 录用状态
     * @return 录用记录列表
     */
    List<OfferRecord> getByStatus(String status);

    /**
     * 发送录用邮件
     * @param offerRecordId 录用记录ID
     * @return 是否发送成功
     */
    boolean sendOfferEmail(Long offerRecordId);

    /**
     * 获取可录用的候选人（三面通过的）
     * @return 候选人列表
     */
    List<OfferRecord> getEligibleCandidates();
}
