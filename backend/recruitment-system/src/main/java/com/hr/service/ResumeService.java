package com.hr.service;

import com.hr.entity.Resume;
import java.util.List;

public interface ResumeService {
    /**
     * 保存简历
     * @param resume 简历对象
     * @return 保存后的简历对象
     */
    Resume saveResume(Resume resume);

    /**
     * 根据ID查询简历
     * @param id 简历ID
     * @return 简历对象
     */
    Resume getById(Long id);

    /**
     * 查询所有简历
     * @return 简历列表
     */
    List<Resume> getAll();

    /**
     * 根据招聘申请ID查询简历
     * @param recruitmentRequestId 招聘申请ID
     * @return 简历列表
     */
    List<Resume> getByRecruitmentRequestId(Long recruitmentRequestId);

    /**
     * 根据状态查询简历
     * @param status 简历状态
     * @return 简历列表
     */
    List<Resume> getByStatus(String status);

    /**
     * 更新简历状态
     * @param id 简历ID
     * @param status 新状态
     * @return 更新后的简历对象
     */
    Resume updateStatus(Long id, String status);
}
