package com.hr.mapper;

import com.hr.entity.DemandRequirement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DemandRequirementMapper {
    int insert(DemandRequirement demandRequirement);

    int updateByPrimaryKey(DemandRequirement demandRequirement);

    DemandRequirement selectByPrimaryKey(@Param("demandId") Long demandId);

    DemandRequirement selectBySourceRecruitmentRequestId(@Param("sourceRecruitmentRequestId") Long sourceRecruitmentRequestId);

    List<DemandRequirement> selectAll();

    List<DemandRequirement> selectByHrUserId(@Param("hrUserId") String hrUserId);
}
