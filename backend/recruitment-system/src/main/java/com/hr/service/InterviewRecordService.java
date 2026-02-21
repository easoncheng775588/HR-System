package com.hr.service;

import com.hr.entity.InterviewRecord;
import java.util.List;

public interface InterviewRecordService {
    /**
     * 保存面试记录
     * @param interviewRecord 面试记录对象
     * @return 保存后的面试记录对象
     */
    InterviewRecord saveInterviewRecord(InterviewRecord interviewRecord);

    /**
     * 根据ID查询面试记录
     * @param id 面试记录ID
     * @return 面试记录对象
     */
    InterviewRecord getById(Long id);

    /**
     * 查询所有面试记录
     * @return 面试记录列表
     */
    List<InterviewRecord> getAll();

    /**
     * 根据简历ID查询面试记录
     * @param resumeId 简历ID
     * @return 面试记录列表
     */
    List<InterviewRecord> getByResumeId(Long resumeId);

    /**
     * 根据招聘申请ID查询面试记录
     * @param recruitmentRequestId 招聘申请ID
     * @return 面试记录列表
     */
    List<InterviewRecord> getByRecruitmentRequestId(Long recruitmentRequestId);

    /**
     * 更新面试结果
     * @param id 面试记录ID
     * @param interviewResult 面试结果
     * @param interviewComment 面试评语
     * @return 更新后的面试记录对象
     */
    InterviewRecord updateInterviewResult(Long id, String interviewResult, String interviewComment);

    /**
     * 根据简历ID和面试环节查询面试记录
     * @param resumeId 简历ID
     * @param interviewRound 面试环节
     * @return 面试记录对象
     */
    InterviewRecord getByResumeIdAndRound(Long resumeId, String interviewRound);
}
