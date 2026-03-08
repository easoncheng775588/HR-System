package com.hr.controller;

import com.hr.entity.InterviewRecord;
import com.hr.service.InterviewRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interview")
@CrossOrigin(origins = "*")
public class InterviewRecordController {

    @Autowired
    private InterviewRecordService interviewRecordService;

    /**
     * 保存面试记录
     * @param interviewRecord 面试记录对象
     * @return 响应结果
     */
    @PostMapping("/save")
    public Map<String, Object> saveInterviewRecord(@RequestBody InterviewRecord interviewRecord) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 设置默认的用户信息（实际项目中应该从登录信息中获取）
            interviewRecord.setCreateUserId("1001");
            interviewRecord.setCreateUserName("系统用户");
            interviewRecord.setUpdateUserId("1001");
            interviewRecord.setUpdateUserName("系统用户");

            InterviewRecord savedRecord = interviewRecordService.saveInterviewRecord(interviewRecord);
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", savedRecord);
        } catch (RuntimeException e) {
            response.put("returnCode", "ERR0001");
            response.put("errorMsg", e.getMessage());
            response.put("body", null);
        } catch (Exception e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "保存面试记录失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }

    /**
     * 查询所有面试记录
     * @return 响应结果
     */
    @GetMapping("/list")
    public Map<String, Object> getAllInterviewRecords() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", interviewRecordService.getAll());
        } catch (Exception e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "查询面试记录失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }

    /**
     * 根据ID查询面试记录
     * @param id 面试记录ID
     * @return 响应结果
     */
    @GetMapping("/{id}")
    public Map<String, Object> getInterviewRecordById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            InterviewRecord record = interviewRecordService.getById(id);
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", record);
        } catch (Exception e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "查询面试记录失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }

    /**
     * 根据简历ID查询面试记录
     * @param resumeId 简历ID
     * @return 响应结果
     */
    @GetMapping("/resume/{resumeId}")
    public Map<String, Object> getInterviewRecordsByResumeId(@PathVariable Long resumeId) {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", interviewRecordService.getByResumeId(resumeId));
        } catch (Exception e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "查询面试记录失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }

    /**
     * 根据招聘申请ID查询面试记录
     * @param recruitmentRequestId 招聘申请ID
     * @return 响应结果
     */
    @GetMapping("/recruitment-request/{recruitmentRequestId}")
    public Map<String, Object> getInterviewRecordsByRecruitmentRequestId(@PathVariable Long recruitmentRequestId) {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", interviewRecordService.getByRecruitmentRequestId(recruitmentRequestId));
        } catch (Exception e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "查询面试记录失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }

    /**
     * 更新面试结果
     * @param id 面试记录ID
     * @param params 包含面试结果和评语的参数
     * @return 响应结果
     */
    @PutMapping("/{id}/result")
    public Map<String, Object> updateInterviewResult(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Map<String, Object> response = new HashMap<>();
        try {
            String interviewResult = (String) params.get("interviewResult");
            String interviewComment = (String) params.get("interviewComment");
            InterviewRecord updatedRecord = interviewRecordService.updateInterviewResult(id, interviewResult, interviewComment);
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", updatedRecord);
        } catch (Exception e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "更新面试结果失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }

    /**
     * 获取待面试的数量
     * @return 响应结果
     */
    @GetMapping("/pending/count")
    public Map<String, Object> getPendingInterviewCount() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<InterviewRecord> allRecords = interviewRecordService.getAll();
            // 统计未录入结果的面试记录数量
            int pendingCount = 0;
            for (InterviewRecord record : allRecords) {
                if (record.getInterviewResult() == null) {
                    pendingCount++;
                }
            }
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", pendingCount);
        } catch (Exception e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "获取待面试数量失败：" + e.getMessage());
            response.put("body", 0);
        }
        return response;
    }

    /**
     * 根据ID删除面试记录
     * @param id 面试记录ID
     * @return 响应结果
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteInterviewRecord(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            interviewRecordService.deleteById(id);
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", null);
        } catch (Exception e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "删除面试记录失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }
}
