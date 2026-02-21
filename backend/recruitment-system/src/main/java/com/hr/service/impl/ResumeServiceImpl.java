package com.hr.service.impl;

import com.hr.entity.Resume;
import com.hr.mapper.ResumeMapper;
import com.hr.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResumeServiceImpl implements ResumeService {

    @Autowired
    private ResumeMapper resumeMapper;

    @Override
    public Resume saveResume(Resume resume) {
        // 设置默认值
        if (resume.getStatus() == null) {
            resume.setStatus("PENDING_SCREENING");
        }
        if (resume.getCreateUserId() == null) {
            resume.setCreateUserId("1001");
            resume.setCreateUserName("系统用户");
        }
        if (resume.getUpdateUserId() == null) {
            resume.setUpdateUserId("1001");
            resume.setUpdateUserName("系统用户");
        }

        if (resume.getResumeId() == null) {
            // 新增
            resumeMapper.insert(resume);
        } else {
            // 更新
            resumeMapper.updateByPrimaryKey(resume);
        }
        return resume;
    }

    @Override
    public Resume getById(Long id) {
        return resumeMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<Resume> getAll() {
        return resumeMapper.selectAll();
    }

    @Override
    public List<Resume> getByRecruitmentRequestId(Long recruitmentRequestId) {
        return resumeMapper.selectByRecruitmentRequestId(recruitmentRequestId);
    }

    @Override
    public List<Resume> getByStatus(String status) {
        return resumeMapper.selectByStatus(status);
    }

    @Override
    public Resume updateStatus(Long id, String status) {
        Resume resume = resumeMapper.selectByPrimaryKey(id);
        if (resume == null) {
            throw new RuntimeException("简历不存在");
        }
        resume.setStatus(status);
        resume.setUpdateUserId("1001");
        resume.setUpdateUserName("系统用户");
        resumeMapper.updateByPrimaryKey(resume);
        return resume;
    }
}
