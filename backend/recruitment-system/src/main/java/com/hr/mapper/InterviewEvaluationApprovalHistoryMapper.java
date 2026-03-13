package com.hr.mapper;

import com.hr.entity.InterviewEvaluationApprovalHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InterviewEvaluationApprovalHistoryMapper {
    int insert(InterviewEvaluationApprovalHistory history);

    List<InterviewEvaluationApprovalHistory> selectByEvaluationId(@Param("evaluationId") Long evaluationId);

    List<InterviewEvaluationApprovalHistory> selectByApproverId(@Param("approverId") String approverId);
}
