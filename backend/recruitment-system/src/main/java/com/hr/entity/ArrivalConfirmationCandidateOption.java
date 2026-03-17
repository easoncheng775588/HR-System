package com.hr.entity;

public class ArrivalConfirmationCandidateOption {
    private Long entryRecordId;
    private Long resumeId;
    private Long sourceRecruitmentRequestId;
    private String candidateName;
    private Long supplierId;
    private String supplierName;
    private String supplierHrUserId;
    private String supplierHrUserName;
    private String targetOrgUnitName;
    private String recommendedRoomManagerUserId;
    private String recommendedRoomManagerUserName;

    public Long getEntryRecordId() { return entryRecordId; }
    public void setEntryRecordId(Long entryRecordId) { this.entryRecordId = entryRecordId; }
    public Long getResumeId() { return resumeId; }
    public void setResumeId(Long resumeId) { this.resumeId = resumeId; }
    public Long getSourceRecruitmentRequestId() { return sourceRecruitmentRequestId; }
    public void setSourceRecruitmentRequestId(Long sourceRecruitmentRequestId) { this.sourceRecruitmentRequestId = sourceRecruitmentRequestId; }
    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getSupplierHrUserId() { return supplierHrUserId; }
    public void setSupplierHrUserId(String supplierHrUserId) { this.supplierHrUserId = supplierHrUserId; }
    public String getSupplierHrUserName() { return supplierHrUserName; }
    public void setSupplierHrUserName(String supplierHrUserName) { this.supplierHrUserName = supplierHrUserName; }
    public String getTargetOrgUnitName() { return targetOrgUnitName; }
    public void setTargetOrgUnitName(String targetOrgUnitName) { this.targetOrgUnitName = targetOrgUnitName; }
    public String getRecommendedRoomManagerUserId() { return recommendedRoomManagerUserId; }
    public void setRecommendedRoomManagerUserId(String recommendedRoomManagerUserId) { this.recommendedRoomManagerUserId = recommendedRoomManagerUserId; }
    public String getRecommendedRoomManagerUserName() { return recommendedRoomManagerUserName; }
    public void setRecommendedRoomManagerUserName(String recommendedRoomManagerUserName) { this.recommendedRoomManagerUserName = recommendedRoomManagerUserName; }
}
