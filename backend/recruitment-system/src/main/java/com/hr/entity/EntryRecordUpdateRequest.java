package com.hr.entity;

public class EntryRecordUpdateRequest {
    private String entryStatus;
    private String plannedEntryDate;
    private String actualEntryDate;
    private String arrivalStatus;
    private String operatorUserId;
    private String operatorUserName;
    private String operatorRole;

    public String getEntryStatus() {
        return entryStatus;
    }

    public void setEntryStatus(String entryStatus) {
        this.entryStatus = entryStatus;
    }

    public String getPlannedEntryDate() {
        return plannedEntryDate;
    }

    public void setPlannedEntryDate(String plannedEntryDate) {
        this.plannedEntryDate = plannedEntryDate;
    }

    public String getActualEntryDate() {
        return actualEntryDate;
    }

    public void setActualEntryDate(String actualEntryDate) {
        this.actualEntryDate = actualEntryDate;
    }

    public String getArrivalStatus() {
        return arrivalStatus;
    }

    public void setArrivalStatus(String arrivalStatus) {
        this.arrivalStatus = arrivalStatus;
    }

    public String getOperatorUserId() {
        return operatorUserId;
    }

    public void setOperatorUserId(String operatorUserId) {
        this.operatorUserId = operatorUserId;
    }

    public String getOperatorUserName() {
        return operatorUserName;
    }

    public void setOperatorUserName(String operatorUserName) {
        this.operatorUserName = operatorUserName;
    }

    public String getOperatorRole() {
        return operatorRole;
    }

    public void setOperatorRole(String operatorRole) {
        this.operatorRole = operatorRole;
    }
}
