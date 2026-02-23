package com.hr.service.impl;

import com.hr.entity.OfferRecord;
import com.hr.entity.Resume;
import com.hr.entity.InterviewRecord;
import com.hr.entity.EmailTemplate;
import com.hr.mapper.OfferRecordMapper;
import com.hr.mapper.ResumeMapper;
import com.hr.mapper.InterviewRecordMapper;
import com.hr.service.OfferRecordService;
import com.hr.service.EmailTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 录用记录服务实现类
 * 实现录用管理的业务逻辑
 */
@Service
public class OfferRecordServiceImpl implements OfferRecordService {
    
    private static final Logger logger = LoggerFactory.getLogger(OfferRecordServiceImpl.class);
    
    @Autowired
    private OfferRecordMapper offerRecordMapper;
    
    @Autowired
    private ResumeMapper resumeMapper;
    
    @Autowired
    private InterviewRecordMapper interviewRecordMapper;
    
    @Autowired
    private EmailTemplateService emailTemplateService;
    
    @Autowired
    private com.hr.utils.EmailSender emailSender;

    @Override
    @Transactional
    public OfferRecord save(OfferRecord offerRecord) {
        logger.debug("保存录用记录: {}", offerRecord);
        
        Date now = new Date();
        if (offerRecord.getOfferRecordId() == null) {
            // 新增录用记录
            offerRecord.setCreateTime(now);
            offerRecord.setUpdateTime(now);
            offerRecord.setStatus("PENDING");
            offerRecord.setEmailStatus("NOT_SENT");
            offerRecordMapper.insert(offerRecord);
        } else {
            // 更新录用记录
            offerRecord.setUpdateTime(now);
            offerRecordMapper.updateByPrimaryKey(offerRecord);
        }
        
        return offerRecord;
    }

    @Override
    public OfferRecord getById(Long id) {
        logger.debug("根据ID查询录用记录: {}", id);
        return offerRecordMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<OfferRecord> getAll() {
        logger.debug("查询所有录用记录");
        return offerRecordMapper.selectAll();
    }

    @Override
    public OfferRecord getByResumeId(Long resumeId) {
        logger.debug("根据简历ID查询录用记录: {}", resumeId);
        return offerRecordMapper.selectByResumeId(resumeId);
    }

    @Override
    public List<OfferRecord> getByRecruitmentRequestId(Long recruitmentRequestId) {
        logger.debug("根据招聘申请ID查询录用记录: {}", recruitmentRequestId);
        return offerRecordMapper.selectByRecruitmentRequestId(recruitmentRequestId);
    }

    @Override
    public List<OfferRecord> getByStatus(String status) {
        logger.debug("根据状态查询录用记录: {}", status);
        return offerRecordMapper.selectByStatus(status);
    }

    @Override
    @Transactional
    public boolean sendOfferEmail(Long offerRecordId) {
        logger.debug("发送录用邮件: {}", offerRecordId);
        
        try {
            OfferRecord offerRecord = getById(offerRecordId);
            if (offerRecord == null) {
                logger.error("录用记录不存在: {}", offerRecordId);
                return false;
            }
            
            // 获取邮件模板
            EmailTemplate emailTemplate = emailTemplateService.getDefaultOfferTemplate();
            if (emailTemplate == null) {
                logger.error("邮件模板不存在");
                return false;
            }
            
            // 准备邮件参数
            String to = offerRecord.getEmail();
            String candidateName = offerRecord.getCandidateName();
            String position = offerRecord.getPosition();
            String entryTime = "";
            if (offerRecord.getEntryTime() != null) {
                entryTime = new java.text.SimpleDateFormat("yyyy年MM月dd日").format(offerRecord.getEntryTime());
            } else {
                entryTime = "待定";
            }
            String subject = emailTemplate.getSubject();
            String contentTemplate = emailTemplate.getContent();
            
            // 发送邮件
            boolean sent = emailSender.sendOfferEmail(to, candidateName, position, entryTime, subject, contentTemplate);
            
            // 更新邮件发送状态
            if (sent) {
                offerRecord.setEmailStatus("SENT");
                offerRecord.setStatus("APPROVED");
            } else {
                offerRecord.setEmailStatus("FAILED");
            }
            offerRecord.setUpdateTime(new Date());
            offerRecordMapper.updateByPrimaryKey(offerRecord);
            
            logger.info("录用邮件发送{}", sent ? "成功" : "失败" + ": {}", offerRecordId);
            return sent;
        } catch (Exception e) {
            logger.error("发送录用邮件失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public List<OfferRecord> getEligibleCandidates() {
        logger.debug("获取可录用的候选人");
        
        // 1. 获取所有简历
        List<Resume> allResumes = resumeMapper.selectAll();
        
        // 2. 筛选出三面通过的候选人
        List<OfferRecord> eligibleCandidates = allResumes.stream()
            .map(resume -> {
                // 查询该简历的所有面试记录
                List<InterviewRecord> interviewRecords = interviewRecordMapper.selectByResumeId(resume.getResumeId());
                
                // 检查是否有三面通过的记录
                boolean hasThirdRoundPassed = interviewRecords.stream()
                    .anyMatch(record -> "THIRD_ROUND".equals(record.getInterviewRound()) && "PASSED".equals(record.getInterviewResult()));
                
                if (hasThirdRoundPassed) {
                    // 检查是否已经有录用记录
                    OfferRecord existingOffer = offerRecordMapper.selectByResumeId(resume.getResumeId());
                    if (existingOffer == null) {
                        // 创建候选人信息
                        OfferRecord candidate = new OfferRecord();
                        candidate.setResumeId(resume.getResumeId());
                        candidate.setRecruitmentRequestId(resume.getRecruitmentRequestId());
                        candidate.setCandidateName(resume.getApplicantName());
                        candidate.setContactPhone(resume.getContactPhone());
                        candidate.setEmail(resume.getEmail());
                        candidate.setPosition(resume.getJobTitle());
                        return candidate;
                    }
                }
                return null;
            })
            .filter(candidate -> candidate != null)
            .collect(Collectors.toList());
        
        logger.debug("可录用的候选人数量: {}", eligibleCandidates.size());
        return eligibleCandidates;
    }
}
