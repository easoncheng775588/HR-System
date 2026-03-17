package com.hr.mapper;

import com.hr.entity.ArrivalConfirmationApprovalHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArrivalConfirmationApprovalHistoryMapper {
    int insert(ArrivalConfirmationApprovalHistory history);

    List<ArrivalConfirmationApprovalHistory> selectByArrivalConfirmationId(@Param("arrivalConfirmationId") Long arrivalConfirmationId);

    List<ArrivalConfirmationApprovalHistory> selectByApproverId(@Param("approverId") String approverId);
}
