package com.hr.mapper;

import com.hr.entity.DemandOperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DemandOperationLogMapper {
    int insert(DemandOperationLog demandOperationLog);
}
