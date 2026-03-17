package com.hr.mapper;

import com.hr.entity.ArrivalConfirmation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArrivalConfirmationMapper {
    int insert(ArrivalConfirmation arrivalConfirmation);

    int updateByPrimaryKey(ArrivalConfirmation arrivalConfirmation);

    int deleteByPrimaryKey(@Param("arrivalConfirmationId") Long arrivalConfirmationId);

    ArrivalConfirmation selectByPrimaryKey(@Param("arrivalConfirmationId") Long arrivalConfirmationId);

    ArrivalConfirmation selectLatestByEntryRecordId(@Param("entryRecordId") Long entryRecordId);

    List<ArrivalConfirmation> selectAll();

    List<ArrivalConfirmation> selectByCreateUserId(@Param("createUserId") String createUserId);

    List<ArrivalConfirmation> selectByApproverId(@Param("approverId") String approverId);
}
