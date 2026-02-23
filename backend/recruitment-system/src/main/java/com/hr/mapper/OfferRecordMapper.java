package com.hr.mapper;

import com.hr.entity.OfferRecord;
import java.util.List;

/**
 * 录用记录Mapper接口
 * 用于录用记录的数据库操作
 */
public interface OfferRecordMapper {
    /**
     * 保存录用记录
     * @param offerRecord 录用记录对象
     * @return 影响的行数
     */
    int insert(OfferRecord offerRecord);

    /**
     * 更新录用记录
     * @param offerRecord 录用记录对象
     * @return 影响的行数
     */
    int updateByPrimaryKey(OfferRecord offerRecord);

    /**
     * 根据ID查询录用记录
     * @param id 录用记录ID
     * @return 录用记录对象
     */
    OfferRecord selectByPrimaryKey(Long id);

    /**
     * 查询所有录用记录
     * @return 录用记录列表
     */
    List<OfferRecord> selectAll();

    /**
     * 根据简历ID查询录用记录
     * @param resumeId 简历ID
     * @return 录用记录对象
     */
    OfferRecord selectByResumeId(Long resumeId);

    /**
     * 根据招聘申请ID查询录用记录
     * @param recruitmentRequestId 招聘申请ID
     * @return 录用记录列表
     */
    List<OfferRecord> selectByRecruitmentRequestId(Long recruitmentRequestId);

    /**
     * 根据状态查询录用记录
     * @param status 录用状态
     * @return 录用记录列表
     */
    List<OfferRecord> selectByStatus(String status);
}
