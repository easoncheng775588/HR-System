package com.hr.mapper;

import com.hr.entity.Staffing;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StaffingMapper {

    List<Staffing> getAllStaffings();

    Staffing getStaffingById(@Param("staffingId") Long staffingId);

    Staffing getStaffingByOrgUnitName(@Param("orgUnitName") String orgUnitName);

    int insertStaffing(Staffing staffing);

    int updateStaffing(Staffing staffing);

    int deleteStaffing(@Param("staffingId") Long staffingId);

    Integer countByOrgUnitName(@Param("orgUnitName") String orgUnitName, @Param("excludeStaffingId") Long excludeStaffingId);
}

