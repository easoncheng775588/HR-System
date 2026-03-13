package com.hr.service;

import com.hr.entity.InterviewEvaluation;
import com.hr.entity.InterviewEvaluationDetailVO;
import com.hr.entity.SubmitInterviewEvaluationRequest;
import com.hr.entity.WorkflowApproveRequest;

import java.util.List;

public interface InterviewEvaluationService {
    InterviewEvaluation submit(SubmitInterviewEvaluationRequest request);

    InterviewEvaluationDetailVO getDetail(Long evaluationId);

    void approve(Long evaluationId, WorkflowApproveRequest request);

    List<InterviewEvaluation> getAll();

    List<InterviewEvaluation> getByCreator(String userId);

    List<InterviewEvaluation> getByApprover(String userId);

    InterviewEvaluation getById(Long evaluationId);
}
