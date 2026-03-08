package com.hr.entity;

import java.util.Date;

public class Staffing {
    private Long staffingId;
    private String orgUnitName;
    private Integer totalHeadcount;
    private Integer vacancyHeadcount;
    private Integer outsourcingHeadcount;
    private Integer employeeHeadcount;
    private Date createTime;
    private String createUserId;
    private String createUserName;
    private Date updateTime;
    private String updateUserId;
    private String updateUserName;

    public Long getStaffingId() {
        return staffingId;
    }

    public void setStaffingId(Long staffingId) {
        this.staffingId = staffingId;
    }

    public String getOrgUnitName() {
        return orgUnitName;
    }

    public void setOrgUnitName(String orgUnitName) {
        this.orgUnitName = orgUnitName;
    }

    public Integer getTotalHeadcount() {
        return totalHeadcount;
    }

    public void setTotalHeadcount(Integer totalHeadcount) {
        this.totalHeadcount = totalHeadcount;
    }

    public Integer getVacancyHeadcount() {
        return vacancyHeadcount;
    }

    public void setVacancyHeadcount(Integer vacancyHeadcount) {
        this.vacancyHeadcount = vacancyHeadcount;
    }

    public Integer getOutsourcingHeadcount() {
        return outsourcingHeadcount;
    }

    public void setOutsourcingHeadcount(Integer outsourcingHeadcount) {
        this.outsourcingHeadcount = outsourcingHeadcount;
    }

    public Integer getEmployeeHeadcount() {
        return employeeHeadcount;
    }

    public void setEmployeeHeadcount(Integer employeeHeadcount) {
        this.employeeHeadcount = employeeHeadcount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(String createUserId) {
        this.createUserId = createUserId;
    }

    public String getCreateUserName() {
        return createUserName;
    }

    public void setCreateUserName(String createUserName) {
        this.createUserName = createUserName;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getUpdateUserId() {
        return updateUserId;
    }

    public void setUpdateUserId(String updateUserId) {
        this.updateUserId = updateUserId;
    }

    public String getUpdateUserName() {
        return updateUserName;
    }

    public void setUpdateUserName(String updateUserName) {
        this.updateUserName = updateUserName;
    }
}

