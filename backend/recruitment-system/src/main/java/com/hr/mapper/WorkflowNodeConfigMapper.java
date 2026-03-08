package com.hr.mapper;

import com.hr.entity.WorkflowNodeConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WorkflowNodeConfigMapper {
    List<WorkflowNodeConfig> selectByProcessCode(@Param("processCode") String processCode);
}
