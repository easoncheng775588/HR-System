package com.hr.service;

import com.hr.entity.ConfirmInterviewTimeRequest;
import com.hr.entity.PendingInterviewResumeVO;

import java.util.List;

public interface InterviewArrangementService {
    List<PendingInterviewResumeVO> getPendingList(String viewerId, String viewerRole);

    void confirmInterviewTime(Long resumeId, ConfirmInterviewTimeRequest request);
}
