package com.hr.service.impl;

import com.hr.entity.Resume;
import com.hr.mapper.ResumeMapper;
import com.hr.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ResumeServiceImpl implements ResumeService {

    @Autowired
    private ResumeMapper resumeMapper;

    @Override
    public Resume saveResume(Resume resume) {
        if (resume.getCandidateName() == null || resume.getCandidateName().trim().isEmpty()) {
            throw new IllegalArgumentException("候选人不能为空");
        }
        if (resume.getRelatedRequestIds() == null || resume.getRelatedRequestIds().trim().isEmpty()) {
            throw new IllegalArgumentException("关联需求不能为空");
        }

        if (resume.getStatus() == null || resume.getStatus().trim().isEmpty()) {
            resume.setStatus("PENDING_SCREENING");
        }
        if (resume.getCreateUserId() == null || resume.getCreateUserId().trim().isEmpty()) {
            resume.setCreateUserId("1001");
        }
        if (resume.getCreateUserName() == null || resume.getCreateUserName().trim().isEmpty()) {
            resume.setCreateUserName("系统用户");
        }
        if (resume.getUpdateUserId() == null || resume.getUpdateUserId().trim().isEmpty()) {
            resume.setUpdateUserId(resume.getCreateUserId());
        }
        if (resume.getUpdateUserName() == null || resume.getUpdateUserName().trim().isEmpty()) {
            resume.setUpdateUserName(resume.getCreateUserName());
        }

        if (resume.getApplicantName() == null || resume.getApplicantName().trim().isEmpty()) {
            resume.setApplicantName(resume.getCandidateName());
        }
        if (resume.getJobTitle() == null || resume.getJobTitle().trim().isEmpty()) {
            resume.setJobTitle(resume.getAppliedCategory());
        }
        if (resume.getEducation() == null || resume.getEducation().trim().isEmpty()) {
            resume.setEducation(resume.getHighestDegree());
        }

        if (resume.getSupplierName() == null || resume.getSupplierName().trim().isEmpty()) {
            String supplierName = resumeMapper.selectSupplierNameByUserId(resume.getCreateUserId());
            resume.setSupplierName(supplierName == null ? "" : supplierName);
        }
        if (resume.getSupplierRecommendDate() == null) {
            resume.setSupplierRecommendDate(new Date());
        }

        if (resume.getResumeId() == null) {
            resumeMapper.insert(resume);
        } else {
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

    @Override
    public int deleteById(Long id) {
        return resumeMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<Map<String, Object>> getRequirementOptions() {
        List<Map<String, Object>> dbOptions = resumeMapper.selectRequirementOptions();
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Boolean> labels = new LinkedHashMap<>();

        if (dbOptions != null) {
            for (Map<String, Object> item : dbOptions) {
                if (item == null) {
                    continue;
                }
                String label = String.valueOf(item.get("label"));
                labels.put(label, true);
                result.add(item);
            }
        }

        String[] defaults = new String[] {"系统研发岗", "测试", "项目助理", "行政", "人力"};
        for (int i = 0; i < defaults.length; i++) {
            String label = defaults[i];
            if (labels.containsKey(label)) {
                continue;
            }
            Map<String, Object> option = new LinkedHashMap<>();
            option.put("value", "INIT_" + (i + 1));
            option.put("label", label);
            result.add(option);
        }

        return result;
    }
}
