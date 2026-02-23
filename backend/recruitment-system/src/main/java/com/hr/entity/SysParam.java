package com.hr.entity;

import java.util.Date;

/**
 * 系统参数实体类
 */
public class SysParam {
    /**
     * 参数ID
     */
    private String paramId;
    
    /**
     * 参数编码
     */
    private String paramCode;
    
    /**
     * 参数名称
     */
    private String paramName;
    
    /**
     * 参数值
     */
    private String paramValue;
    
    /**
     * 参数类型
     */
    private String paramType;
    
    /**
     * 状态（ACTIVE：启用，INACTIVE：禁用）
     */
    private String status;
    
    /**
     * 排序顺序
     */
    private Integer sortOrder;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 创建人ID
     */
    private String createUserId;
    
    /**
     * 创建人姓名
     */
    private String createUserName;
    
    /**
     * 更新时间
     */
    private Date updateTime;
    
    /**
     * 更新人ID
     */
    private String updateUserId;
    
    /**
     * 更新人姓名
     */
    private String updateUserName;

    // Getters and Setters
    public String getParamId() {
        return paramId;
    }

    public void setParamId(String paramId) {
        this.paramId = paramId;
    }

    public String getParamCode() {
        return paramCode;
    }

    public void setParamCode(String paramCode) {
        this.paramCode = paramCode;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    public String getParamType() {
        return paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
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
