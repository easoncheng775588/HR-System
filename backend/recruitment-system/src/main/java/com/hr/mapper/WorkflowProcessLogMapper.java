package com.hr.mapper;

import com.hr.entity.WorkflowProcessLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WorkflowProcessLogMapper {
    int insert(WorkflowProcessLog log);

    List<WorkflowProcessLog> selectByBusinessId(@Param("processCode") String processCode, @Param("businessId") Long businessId);
}
