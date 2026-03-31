package com.hr.service;

import com.hr.entity.Staffing;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface StaffingService {

    List<Staffing> getAllStaffings(String viewerId, String viewerRole);

    List<Staffing> getResponsibleStaffings(String userId);

    Staffing getStaffingById(Long staffingId);

    Staffing getStaffingByOrgUnitName(String orgUnitName);

    int createStaffing(Staffing staffing);

    int updateStaffing(Long staffingId, Staffing staffing);

    int deleteStaffing(Long staffingId);

    byte[] generateTemplate();

    Map<String, Object> importFromTemplate(MultipartFile file);
}
