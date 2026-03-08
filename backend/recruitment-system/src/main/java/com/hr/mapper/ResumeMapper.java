package com.hr.mapper;

import com.hr.entity.Resume;
import java.util.List;
import java.util.Map;

public interface ResumeMapper {
    /**
     * 保存简历
     * @param resume 简历对象
     * @return 影响的行数
     */
    int insert(Resume resume);

    /**
     * 更新简历
     * @param resume 简历对象
     * @return 影响的行数
     */
    int updateByPrimaryKey(Resume resume);

    /**
     * 根据ID查询简历
     * @param id 简历ID
     * @return 简历对象
     */
    Resume selectByPrimaryKey(Long id);

    /**
     * 查询所有简历
     * @return 简历列表
     */
    List<Resume> selectAll();

    /**
     * 根据招聘申请ID查询简历
     * @param recruitmentRequestId 招聘申请ID
     * @return 简历列表
     */
    List<Resume> selectByRecruitmentRequestId(Long recruitmentRequestId);

    /**
     * 根据状态查询简历
     * @param status 简历状态
     * @return 简历列表
     */
    List<Resume> selectByStatus(String status);

    int deleteByPrimaryKey(Long id);

    List<Map<String, Object>> selectRequirementOptions();

    String selectSupplierNameByUserId(String userId);
}
