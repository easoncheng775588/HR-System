package com.hr.service.impl;

import com.hr.entity.InterviewRecord;
import com.hr.entity.Resume;
import com.hr.mapper.InterviewRecordMapper;
import com.hr.mapper.ResumeMapper;
import com.hr.service.InterviewRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InterviewRecordServiceImpl implements InterviewRecordService {

    @Autowired
    private InterviewRecordMapper interviewRecordMapper;

    @Autowired
    private ResumeMapper resumeMapper;

    @Override
    public InterviewRecord saveInterviewRecord(InterviewRecord interviewRecord) {
        // 设置默认值
        if (interviewRecord.getCreateUserId() == null) {
            interviewRecord.setCreateUserId("1001");
            interviewRecord.setCreateUserName("系统用户");
        }
        if (interviewRecord.getUpdateUserId() == null) {
            interviewRecord.setUpdateUserId("1001");
            interviewRecord.setUpdateUserName("系统用户");
        }

        if (interviewRecord.getInterviewRecordId() == null) {
            // 新增：检查面试次数限制
            Long resumeId = interviewRecord.getResumeId();
            if (resumeId != null) {
                int interviewCount = interviewRecordMapper.countByResumeId(resumeId);
                if (interviewCount >= 3) {
                    throw new RuntimeException("候选人已经进行了3次面试，不建议再次安排面试");
                }
            }
            
            interviewRecordMapper.insert(interviewRecord);
            // 更新简历的面试状态
            updateResumeInterviewStatus(interviewRecord.getResumeId(), interviewRecord.getInterviewRound());
        } else {
            // 更新
            interviewRecordMapper.updateByPrimaryKey(interviewRecord);
            // 如果更新了面试结果，更新简历的面试状态
            if (interviewRecord.getInterviewResult() != null) {
                updateResumeInterviewStatusAfterResult(interviewRecord.getResumeId(), interviewRecord.getInterviewRound(), interviewRecord.getInterviewResult());
            }
        }
        return interviewRecord;
    }

    @Override
    public InterviewRecord getById(Long id) {
        return interviewRecordMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<InterviewRecord> getAll() {
        return interviewRecordMapper.selectAll();
    }

    @Override
    public List<InterviewRecord> getByResumeId(Long resumeId) {
        return interviewRecordMapper.selectByResumeId(resumeId);
    }

    @Override
    public List<InterviewRecord> getByRecruitmentRequestId(Long recruitmentRequestId) {
        return interviewRecordMapper.selectByRecruitmentRequestId(recruitmentRequestId);
    }

    @Override
    public InterviewRecord updateInterviewResult(Long id, String interviewResult, String interviewComment) {
        InterviewRecord interviewRecord = interviewRecordMapper.selectByPrimaryKey(id);
        if (interviewRecord == null) {
            throw new RuntimeException("面试记录不存在");
        }
        interviewRecord.setInterviewResult(interviewResult);
        interviewRecord.setInterviewComment(interviewComment);
        interviewRecord.setUpdateUserId("1001");
        interviewRecord.setUpdateUserName("系统用户");
        interviewRecordMapper.updateByPrimaryKey(interviewRecord);
        // 更新简历的面试状态
        updateResumeInterviewStatusAfterResult(interviewRecord.getResumeId(), interviewRecord.getInterviewRound(), interviewResult);
        return interviewRecord;
    }

    @Override
    public InterviewRecord getByResumeIdAndRound(Long resumeId, String interviewRound) {
        List<InterviewRecord> records = interviewRecordMapper.selectByResumeId(resumeId);
        for (InterviewRecord record : records) {
            if (record.getInterviewRound().equals(interviewRound)) {
                return record;
            }
        }
        return null;
    }

    @Override
    public int countByResumeId(Long resumeId) {
        return interviewRecordMapper.countByResumeId(resumeId);
    }

    /**
     * 更新简历的面试状态
     * @param resumeId 简历ID
     * @param interviewRound 面试环节
     */
    private void updateResumeInterviewStatus(Long resumeId, String interviewRound) {
        Resume resume = resumeMapper.selectByPrimaryKey(resumeId);
        if (resume != null) {
            resume.setInterviewStatus(interviewRound);
            resume.setUpdateUserId("1001");
            resume.setUpdateUserName("系统用户");
            resumeMapper.updateByPrimaryKey(resume);
        }
    }

    /**
     * 面试结果出来后更新简历的面试状态
     * @param resumeId 简历ID
     * @param interviewRound 面试环节
     * @param interviewResult 面试结果
     */
    private void updateResumeInterviewStatusAfterResult(Long resumeId, String interviewRound, String interviewResult) {
        Resume resume = resumeMapper.selectByPrimaryKey(resumeId);
        if (resume != null) {
            if ("FAILED".equals(interviewResult)) {
                // 任何一轮面试失败，简历状态变为未通过
                resume.setInterviewStatus("FAILED");
            } else if ("PASSED".equals(interviewResult)) {
                // 根据面试环节更新状态
                if ("FIRST_ROUND".equals(interviewRound)) {
                    resume.setInterviewStatus("SECOND_ROUND");
                } else if ("SECOND_ROUND".equals(interviewRound)) {
                    resume.setInterviewStatus("THIRD_ROUND");
                } else if ("THIRD_ROUND".equals(interviewRound)) {
                    resume.setInterviewStatus("PASSED");
                }
            }
            resume.setUpdateUserId("1001");
            resume.setUpdateUserName("系统用户");
            resumeMapper.updateByPrimaryKey(resume);
        }
    }

    @Override
    public void deleteById(Long id) {
        interviewRecordMapper.deleteByPrimaryKey(id);
    }
}
