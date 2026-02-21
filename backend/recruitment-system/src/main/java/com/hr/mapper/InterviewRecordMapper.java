package com.hr.mapper;

import com.hr.entity.InterviewRecord;
import java.util.List;

public interface InterviewRecordMapper {
    /**
     * 保存面试记录
     * @param interviewRecord 面试记录对象
     * @return 影响的行数
     */
    int insert(InterviewRecord interviewRecord);

    /**
     * 更新面试记录
     * @param interviewRecord 面试记录对象
     * @return 影响的行数
     */
    int updateByPrimaryKey(InterviewRecord interviewRecord);

    /**
     * 根据ID查询面试记录
     * @param id 面试记录ID
     * @return 面试记录对象
     */
    InterviewRecord selectByPrimaryKey(Long id);

    /**
     * 查询所有面试记录
     * @return 面试记录列表
     */
    List<InterviewRecord> selectAll();

    /**
     * 根据简历ID查询面试记录
     * @param resumeId 简历ID
     * @return 面试记录列表
     */
    List<InterviewRecord> selectByResumeId(Long resumeId);

    /**
     * 根据招聘申请ID查询面试记录
     * @param recruitmentRequestId 招聘申请ID
     * @return 面试记录列表
     */
    List<InterviewRecord> selectByRecruitmentRequestId(Long recruitmentRequestId);

    /**
     * 根据面试环节查询面试记录
     * @param interviewRound 面试环节
     * @return 面试记录列表
     */
    List<InterviewRecord> selectByInterviewRound(String interviewRound);

    /**
     * 根据面试结果查询面试记录
     * @param interviewResult 面试结果
     * @return 面试记录列表
     */
    List<InterviewRecord> selectByInterviewResult(String interviewResult);
}
