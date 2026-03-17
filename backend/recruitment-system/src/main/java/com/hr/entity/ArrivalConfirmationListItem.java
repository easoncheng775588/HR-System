package com.hr.entity;

import java.util.Date;

public class ArrivalConfirmationListItem {
    private Long arrivalConfirmationId;
    private String candidateName;
    private String supplierName;
    private String supplierHrUserName;
    private String targetOrgUnitName;
    private String roomManagerUserName;
    private Date entryDate;
    private String positionLevel;
    private String createUserName;
    private Date createTime;
    private String currentApprovalNode;
    private String approvalStatus;
    private Boolean canDelete;

    public Long getArrivalConfirmationId() { return arrivalConfirmationId; }
    public void setArrivalConfirmationId(Long arrivalConfirmationId) { this.arrivalConfirmationId = arrivalConfirmationId; }
    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getSupplierHrUserName() { return supplierHrUserName; }
    public void setSupplierHrUserName(String supplierHrUserName) { this.supplierHrUserName = supplierHrUserName; }
    public String getTargetOrgUnitName() { return targetOrgUnitName; }
    public void setTargetOrgUnitName(String targetOrgUnitName) { this.targetOrgUnitName = targetOrgUnitName; }
    public String getRoomManagerUserName() { return roomManagerUserName; }
    public void setRoomManagerUserName(String roomManagerUserName) { this.roomManagerUserName = roomManagerUserName; }
    public Date getEntryDate() { return entryDate; }
    public void setEntryDate(Date entryDate) { this.entryDate = entryDate; }
    public String getPositionLevel() { return positionLevel; }
    public void setPositionLevel(String positionLevel) { this.positionLevel = positionLevel; }
    public String getCreateUserName() { return createUserName; }
    public void setCreateUserName(String createUserName) { this.createUserName = createUserName; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public String getCurrentApprovalNode() { return currentApprovalNode; }
    public void setCurrentApprovalNode(String currentApprovalNode) { this.currentApprovalNode = currentApprovalNode; }
    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }
    public Boolean getCanDelete() { return canDelete; }
    public void setCanDelete(Boolean canDelete) { this.canDelete = canDelete; }
}
