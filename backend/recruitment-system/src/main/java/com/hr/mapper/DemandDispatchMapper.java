package com.hr.mapper;

import com.hr.entity.DemandDispatch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DemandDispatchMapper {
    int insert(DemandDispatch demandDispatch);

    int deleteByDemandIdAndSupplierId(@Param("demandId") Long demandId, @Param("supplierId") Long supplierId);

    int updateReceiveStatusByDemandIdAndHrUserId(@Param("demandId") Long demandId,
                                                 @Param("hrUserId") String hrUserId,
                                                 @Param("receiveStatus") String receiveStatus,
                                                 @Param("operatorUserId") String operatorUserId,
                                                 @Param("operatorUserName") String operatorUserName);

    List<DemandDispatch> selectByDemandId(@Param("demandId") Long demandId);

    List<DemandDispatch> selectByDemandIdAndHrUserId(@Param("demandId") Long demandId, @Param("hrUserId") String hrUserId);
}
