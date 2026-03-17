package com.hr.entity;

public class SubmitArrivalConfirmationRequest {
    private Long entryRecordId;
    private Long supplierId;
    private String supplierHrUserId;
    private String targetOrgUnitName;
    private String roomManagerUserId;
    private String entryDate;
    private String positionLevel;
    private String operatorUserId;
    private String operatorUserName;
    private String operatorUserRole;

    public Long getEntryRecordId() { return entryRecordId; }
    public void setEntryRecordId(Long entryRecordId) { this.entryRecordId = entryRecordId; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierHrUserId() { return supplierHrUserId; }
    public void setSupplierHrUserId(String supplierHrUserId) { this.supplierHrUserId = supplierHrUserId; }
    public String getTargetOrgUnitName() { return targetOrgUnitName; }
    public void setTargetOrgUnitName(String targetOrgUnitName) { this.targetOrgUnitName = targetOrgUnitName; }
    public String getRoomManagerUserId() { return roomManagerUserId; }
    public void setRoomManagerUserId(String roomManagerUserId) { this.roomManagerUserId = roomManagerUserId; }
    public String getEntryDate() { return entryDate; }
    public void setEntryDate(String entryDate) { this.entryDate = entryDate; }
    public String getPositionLevel() { return positionLevel; }
    public void setPositionLevel(String positionLevel) { this.positionLevel = positionLevel; }
    public String getOperatorUserId() { return operatorUserId; }
    public void setOperatorUserId(String operatorUserId) { this.operatorUserId = operatorUserId; }
    public String getOperatorUserName() { return operatorUserName; }
    public void setOperatorUserName(String operatorUserName) { this.operatorUserName = operatorUserName; }
    public String getOperatorUserRole() { return operatorUserRole; }
    public void setOperatorUserRole(String operatorUserRole) { this.operatorUserRole = operatorUserRole; }
}
