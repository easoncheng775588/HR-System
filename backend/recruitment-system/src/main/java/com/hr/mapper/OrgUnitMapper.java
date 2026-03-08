package com.hr.mapper;

import com.hr.entity.OrgUnit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrgUnitMapper {

    List<OrgUnit> getActiveOrgUnits();

    Integer countByUnitName(@Param("unitName") String unitName);

    OrgUnit getByUnitName(@Param("unitName") String unitName);
}
