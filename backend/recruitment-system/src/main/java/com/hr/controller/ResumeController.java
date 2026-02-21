package com.hr.controller;

import com.hr.entity.Resume;
import com.hr.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/resume")
@CrossOrigin(origins = "*")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    /**
     * 提交简历
     * @param resume 简历对象
     * @return 响应结果
     */
    @PostMapping("/submit")
    public Map<String, Object> submitResume(@RequestBody Resume resume) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 设置默认状态为待筛选
            resume.setStatus("PENDING_SCREENING");
            // 设置默认的用户信息（实际项目中应该从登录信息中获取）
            resume.setCreateUserId("1001");
            resume.setCreateUserName("系统用户");
            resume.setUpdateUserId("1001");
            resume.setUpdateUserName("系统用户");

            Resume savedResume = resumeService.saveResume(resume);
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", savedResume);
        } catch (Exception e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "提交简历失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }

    /**
     * 查询所有简历
     * @return 响应结果
     */
    @GetMapping("/list")
    public Map<String, Object> getAllResumes() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", resumeService.getAll());
        } catch (Exception e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "查询简历失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }

    /**
     * 根据ID查询简历
     * @param id 简历ID
     * @return 响应结果
     */
    @GetMapping("/{id}")
    public Map<String, Object> getResumeById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Resume resume = resumeService.getById(id);
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", resume);
        } catch (Exception e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "查询简历失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }

    /**
     * 根据招聘申请ID查询简历
     * @param recruitmentRequestId 招聘申请ID
     * @return 响应结果
     */
    @GetMapping("/recruitment-request/{recruitmentRequestId}")
    public Map<String, Object> getResumesByRecruitmentRequestId(@PathVariable Long recruitmentRequestId) {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", resumeService.getByRecruitmentRequestId(recruitmentRequestId));
        } catch (Exception e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "查询简历失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }

    /**
     * 根据状态查询简历
     * @param status 简历状态
     * @return 响应结果
     */
    @GetMapping("/status/{status}")
    public Map<String, Object> getResumesByStatus(@PathVariable String status) {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", resumeService.getByStatus(status));
        } catch (Exception e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "查询简历失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }

    /**
     * 更新简历状态
     * @param id 简历ID
     * @param params 包含状态的参数
     * @return 响应结果
     */
    @PutMapping("/{id}/status")
    public Map<String, Object> updateResumeStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Map<String, Object> response = new HashMap<>();
        try {
            String status = (String) params.get("status");
            Resume updatedResume = resumeService.updateStatus(id, status);
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", updatedResume);
        } catch (Exception e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "更新简历状态失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }
}
