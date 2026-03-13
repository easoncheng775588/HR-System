package com.hr.controller;

import com.hr.common.Response;
import com.hr.entity.SubmitInterviewEvaluationRequest;
import com.hr.service.InterviewEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interview-evaluation")
@CrossOrigin(origins = "*")
public class InterviewEvaluationController {

    @Autowired
    private InterviewEvaluationService interviewEvaluationService;

    @PostMapping("/submit")
    public Response<?> submit(@RequestBody SubmitInterviewEvaluationRequest request) {
        try {
            return Response.success(interviewEvaluationService.submit(request));
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }

    @GetMapping("/{evaluationId}")
    public Response<?> getDetail(@PathVariable Long evaluationId) {
        try {
            return Response.success(interviewEvaluationService.getDetail(evaluationId));
        } catch (Exception e) {
            return Response.fail("获取面试评价详情失败: " + e.getMessage());
        }
    }
}
