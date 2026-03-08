package com.hr.mapper;

import com.hr.entity.ApprovalHistory;
import java.util.List;

public interface ApprovalHistoryMapper {
    int insert(ApprovalHistory record);
    List<ApprovalHistory> selectByRecruitmentRequestId(Long recruitmentRequestId);
    List<ApprovalHistory> selectByApproverId(String approverId);
}
