package com.hr.controller;

import com.hr.entity.Resume;
import com.hr.entity.InterviewerChoiceRequest;
import com.hr.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resume")
@CrossOrigin(origins = "*")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @PostMapping("/submit")
    public Map<String, Object> submitResume(@RequestBody Resume resume) {
        Map<String, Object> response = new HashMap<>();
        try {
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

    @PutMapping("/{id}")
    public Map<String, Object> updateResume(@PathVariable Long id, @RequestBody Resume resume) {
        Map<String, Object> response = new HashMap<>();
        try {
            resume.setResumeId(id);
            Resume savedResume = resumeService.saveResume(resume);
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", savedResume);
        } catch (Exception e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "更新简历失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteResume(@PathVariable Long id,
                                            @RequestParam(value = "operatorUserId", required = false) String operatorUserId,
                                            @RequestParam(value = "operatorRole", required = false) String operatorRole) {
        Map<String, Object> response = new HashMap<>();
        try {
            int affected = resumeService.deleteById(id, operatorUserId, operatorRole);
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", affected > 0);
        } catch (Exception e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "删除简历失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }

    @PostMapping("/{id}/screen")
    public Map<String, Object> screenResume(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            resumeService.screenResume(
                id,
                stringValue(request.get("action")),
                stringValue(request.get("operatorUserId")),
                stringValue(request.get("operatorUserName")),
                stringValue(request.get("operatorRole"))
            );
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", true);
        } catch (Exception e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "简历初筛失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }

    @GetMapping("/dispatch-demand-options")
    public Map<String, Object> getDispatchDemandOptions(@RequestParam("operatorUserId") String operatorUserId,
                                                        @RequestParam(value = "operatorRole", required = false) String operatorRole) {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", resumeService.getDispatchDemandOptions(operatorUserId, operatorRole));
        } catch (Exception e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "查询分发需求失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }

    @PostMapping("/{id}/dispatch")
    public Map<String, Object> dispatchResume(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Long> demandIds = toLongList(request.get("demandIds"));
            resumeService.dispatchResume(
                id,
                demandIds,
                stringValue(request.get("operatorUserId")),
                stringValue(request.get("operatorUserName")),
                stringValue(request.get("operatorRole"))
            );
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", true);
        } catch (Exception e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "分发简历失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }

    @PostMapping("/{id}/interviewer-choice")
    public Map<String, Object> interviewerChoice(@PathVariable Long id, @RequestBody InterviewerChoiceRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            resumeService.interviewerChoice(id, request);
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", true);
        } catch (Exception e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "面试官操作失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }

    @GetMapping("/requirement-options")
    public Map<String, Object> getRequirementOptions() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", resumeService.getRequirementOptions());
        } catch (Exception e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "查询关联需求选项失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }

    @GetMapping("/list")
    public Map<String, Object> getAllResumes(@RequestParam(value = "viewerId", required = false) String viewerId,
                                            @RequestParam(value = "viewerRole", required = false) String viewerRole) {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("returnCode", "SUC0000");
            response.put("errorMsg", "");
            response.put("body", resumeService.getAll(viewerId, viewerRole));
        } catch (Exception e) {
            response.put("returnCode", "ERR0000");
            response.put("errorMsg", "查询简历失败：" + e.getMessage());
            response.put("body", null);
        }
        return response;
    }

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

    @PutMapping("/{id}/status")
    public Map<String, Object> updateResumeStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Map<String, Object> response = new HashMap<>();
        try {
            String status = stringValue(params.get("status"));
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

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private List<Long> toLongList(Object value) {
        java.util.ArrayList<Long> result = new java.util.ArrayList<>();
        if (!(value instanceof List<?>)) {
            return result;
        }
        for (Object item : (List<?>) value) {
            if (item == null) {
                continue;
            }
            try {
                result.add(Long.parseLong(String.valueOf(item)));
            } catch (NumberFormatException ignore) {
                // skip invalid id
            }
        }
        return result;
    }
}
