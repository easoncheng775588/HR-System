package com.hr.entity;

import java.util.List;

public class ArrivalConfirmationFormOptionsVO {
    private List<ArrivalConfirmationCandidateOption> candidates;
    private List<ArrivalConfirmationSupplierOption> suppliers;
    private List<ArrivalConfirmationUserOption> supplierHrs;
    private List<ArrivalConfirmationUserOption> roomManagers;
    private List<ArrivalConfirmationOrgUnitOption> orgUnits;

    public List<ArrivalConfirmationCandidateOption> getCandidates() { return candidates; }
    public void setCandidates(List<ArrivalConfirmationCandidateOption> candidates) { this.candidates = candidates; }
    public List<ArrivalConfirmationSupplierOption> getSuppliers() { return suppliers; }
    public void setSuppliers(List<ArrivalConfirmationSupplierOption> suppliers) { this.suppliers = suppliers; }
    public List<ArrivalConfirmationUserOption> getSupplierHrs() { return supplierHrs; }
    public void setSupplierHrs(List<ArrivalConfirmationUserOption> supplierHrs) { this.supplierHrs = supplierHrs; }
    public List<ArrivalConfirmationUserOption> getRoomManagers() { return roomManagers; }
    public void setRoomManagers(List<ArrivalConfirmationUserOption> roomManagers) { this.roomManagers = roomManagers; }
    public List<ArrivalConfirmationOrgUnitOption> getOrgUnits() { return orgUnits; }
    public void setOrgUnits(List<ArrivalConfirmationOrgUnitOption> orgUnits) { this.orgUnits = orgUnits; }
}
