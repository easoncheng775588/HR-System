package com.hr.mapper;

import com.hr.entity.InterviewEvaluation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InterviewEvaluationMapper {
    int insert(InterviewEvaluation interviewEvaluation);

    int updateByPrimaryKey(InterviewEvaluation interviewEvaluation);

    InterviewEvaluation selectByPrimaryKey(@Param("evaluationId") Long evaluationId);

    InterviewEvaluation selectLatestByResumeId(@Param("resumeId") Long resumeId);

    List<InterviewEvaluation> selectAll();

    List<InterviewEvaluation> selectByCreateUserId(@Param("createUserId") String createUserId);

    List<InterviewEvaluation> selectByApproverId(@Param("approverId") String approverId);
}
