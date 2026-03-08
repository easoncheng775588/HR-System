package com.hr.entity;

import java.util.Date;

public class WorkflowNodeConfig {
    private Long nodeConfigId;
    private String processCode;
    private Integer nodeOrder;
    private String nodeName;
    private String approverRole;
    private Integer nextNodeOrder;
    private Integer finalNode;
    private String status;
    private Date createTime;
    private Date updateTime;

    public Long getNodeConfigId() {
        return nodeConfigId;
    }

    public void setNodeConfigId(Long nodeConfigId) {
        this.nodeConfigId = nodeConfigId;
    }

    public String getProcessCode() {
        return processCode;
    }

    public void setProcessCode(String processCode) {
        this.processCode = processCode;
    }

    public Integer getNodeOrder() {
        return nodeOrder;
    }

    public void setNodeOrder(Integer nodeOrder) {
        this.nodeOrder = nodeOrder;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getApproverRole() {
        return approverRole;
    }

    public void setApproverRole(String approverRole) {
        this.approverRole = approverRole;
    }

    public Integer getNextNodeOrder() {
        return nextNodeOrder;
    }

    public void setNextNodeOrder(Integer nextNodeOrder) {
        this.nextNodeOrder = nextNodeOrder;
    }

    public Integer getFinalNode() {
        return finalNode;
    }

    public void setFinalNode(Integer finalNode) {
        this.finalNode = finalNode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
