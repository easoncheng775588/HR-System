package com.hr.controller;

import com.hr.entity.OfferRecord;
import com.hr.service.OfferRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 录用记录控制器
 * 用于处理录用管理相关的HTTP请求
 */
@RestController
@RequestMapping("/api/offer")
@CrossOrigin(origins = "*")
public class OfferRecordController {
    
    private static final Logger logger = LoggerFactory.getLogger(OfferRecordController.class);
    
    @Autowired
    private OfferRecordService offerRecordService;

    /**
     * 保存录用记录
     * @param offerRecord 录用记录对象
     * @return 响应结果
     */
    @PostMapping("/save")
    public Map<String, Object> save(@RequestBody OfferRecord offerRecord) {
        Map<String, Object> result = new HashMap<>();
        logger.debug("保存录用记录请求: {}", offerRecord);
        
        try {
            OfferRecord savedOfferRecord = offerRecordService.save(offerRecord);
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", savedOfferRecord);
        } catch (Exception e) {
            logger.error("保存录用记录失败: {}", e.getMessage());
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "保存录用记录失败: " + e.getMessage());
            result.put("body", null);
        }
        
        return result;
    }

    /**
     * 根据ID查询录用记录
     * @param id 录用记录ID
     * @return 响应结果
     */
    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        logger.debug("根据ID查询录用记录请求: {}", id);
        
        try {
            OfferRecord offerRecord = offerRecordService.getById(id);
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", offerRecord);
        } catch (Exception e) {
            logger.error("查询录用记录失败: {}", e.getMessage());
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "查询录用记录失败: " + e.getMessage());
            result.put("body", null);
        }
        
        return result;
    }

    /**
     * 查询所有录用记录
     * @return 响应结果
     */
    @GetMapping("/list")
    public Map<String, Object> getAll() {
        Map<String, Object> result = new HashMap<>();
        logger.debug("查询所有录用记录请求");
        
        try {
            List<OfferRecord> offerRecords = offerRecordService.getAll();
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", offerRecords);
        } catch (Exception e) {
            logger.error("查询录用记录失败: {}", e.getMessage());
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "查询录用记录失败: " + e.getMessage());
            result.put("body", null);
        }
        
        return result;
    }

    /**
     * 根据简历ID查询录用记录
     * @param resumeId 简历ID
     * @return 响应结果
     */
    @GetMapping("/resume/{resumeId}")
    public Map<String, Object> getByResumeId(@PathVariable Long resumeId) {
        Map<String, Object> result = new HashMap<>();
        logger.debug("根据简历ID查询录用记录请求: {}", resumeId);
        
        try {
            OfferRecord offerRecord = offerRecordService.getByResumeId(resumeId);
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", offerRecord);
        } catch (Exception e) {
            logger.error("查询录用记录失败: {}", e.getMessage());
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "查询录用记录失败: " + e.getMessage());
            result.put("body", null);
        }
        
        return result;
    }

    /**
     * 根据招聘申请ID查询录用记录
     * @param recruitmentRequestId 招聘申请ID
     * @return 响应结果
     */
    @GetMapping("/recruitment-request/{recruitmentRequestId}")
    public Map<String, Object> getByRecruitmentRequestId(@PathVariable Long recruitmentRequestId) {
        Map<String, Object> result = new HashMap<>();
        logger.debug("根据招聘申请ID查询录用记录请求: {}", recruitmentRequestId);
        
        try {
            List<OfferRecord> offerRecords = offerRecordService.getByRecruitmentRequestId(recruitmentRequestId);
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", offerRecords);
        } catch (Exception e) {
            logger.error("查询录用记录失败: {}", e.getMessage());
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "查询录用记录失败: " + e.getMessage());
            result.put("body", null);
        }
        
        return result;
    }

    /**
     * 获取可录用的候选人
     * @return 响应结果
     */
    @GetMapping("/eligible-candidates")
    public Map<String, Object> getEligibleCandidates() {
        Map<String, Object> result = new HashMap<>();
        logger.debug("获取可录用的候选人请求");
        
        try {
            List<OfferRecord> eligibleCandidates = offerRecordService.getEligibleCandidates();
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", eligibleCandidates);
        } catch (Exception e) {
            logger.error("获取可录用的候选人失败: {}", e.getMessage());
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "获取可录用的候选人失败: " + e.getMessage());
            result.put("body", null);
        }
        
        return result;
    }

    /**
     * 发送录用邮件
     * @param offerRecordId 录用记录ID
     * @return 响应结果
     */
    @PostMapping("/{offerRecordId}/send-email")
    public Map<String, Object> sendOfferEmail(@PathVariable Long offerRecordId) {
        Map<String, Object> result = new HashMap<>();
        logger.debug("发送录用邮件请求: {}", offerRecordId);
        
        try {
            boolean sent = offerRecordService.sendOfferEmail(offerRecordId);
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", sent);
        } catch (Exception e) {
            logger.error("发送录用邮件失败: {}", e.getMessage());
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "发送录用邮件失败: " + e.getMessage());
            result.put("body", null);
        }
        
        return result;
    }

    /**
     * 更新录用状态
     * @param id 录用记录ID
     * @param status 新状态
     * @return 响应结果
     */
    @PutMapping("/{id}/status")
    public Map<String, Object> updateStatus(@PathVariable Long id, @RequestParam String status) {
        Map<String, Object> result = new HashMap<>();
        logger.debug("更新录用状态请求: id={}, status={}", id, status);
        
        try {
            OfferRecord offerRecord = offerRecordService.getById(id);
            if (offerRecord == null) {
                result.put("returnCode", "ERR0001");
                result.put("errorMsg", "录用记录不存在");
                result.put("body", null);
                return result;
            }
            
            offerRecord.setStatus(status);
            OfferRecord updatedOfferRecord = offerRecordService.save(offerRecord);
            result.put("returnCode", "SUC0000");
            result.put("errorMsg", "");
            result.put("body", updatedOfferRecord);
        } catch (Exception e) {
            logger.error("更新录用状态失败: {}", e.getMessage());
            result.put("returnCode", "ERR0001");
            result.put("errorMsg", "更新录用状态失败: " + e.getMessage());
            result.put("body", null);
        }
        
        return result;
    }
}
