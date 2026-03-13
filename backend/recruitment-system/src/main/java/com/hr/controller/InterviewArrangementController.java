package com.hr.controller;

import com.hr.common.Response;
import com.hr.entity.ConfirmInterviewTimeRequest;
import com.hr.service.InterviewArrangementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interview-arrangement")
@CrossOrigin(origins = "*")
public class InterviewArrangementController {

    @Autowired
    private InterviewArrangementService interviewArrangementService;

    @GetMapping("/pending")
    public Response<?> getPending(@RequestParam("viewerId") String viewerId,
                                  @RequestParam(value = "viewerRole", required = false) String viewerRole) {
        try {
            return Response.success(interviewArrangementService.getPendingList(viewerId, viewerRole));
        } catch (Exception e) {
            return Response.fail("获取待安排面试列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/{resumeId}/confirm-time")
    public Response<?> confirmTime(@PathVariable Long resumeId, @RequestBody ConfirmInterviewTimeRequest request) {
        try {
            interviewArrangementService.confirmInterviewTime(resumeId, request);
            return Response.success(true);
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }
}
