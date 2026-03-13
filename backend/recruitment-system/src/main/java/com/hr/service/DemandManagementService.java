package com.hr.service;

import com.hr.entity.RecruitmentRequest;

import java.util.List;
import java.util.Map;

public interface DemandManagementService {
    void createFromApprovedRecruitment(RecruitmentRequest request);

    List<Map<String, Object>> getDemandList(String viewerId, String viewerRole);

    List<Map<String, Object>> getSupplierOptions(String viewerId, String viewerRole);

    void dispatchToSuppliers(Long demandId, List<Long> supplierIds, String operatorUserId, String operatorUserName);

    void confirmReceive(Long demandId, String operatorUserId, String operatorUserName);
}
