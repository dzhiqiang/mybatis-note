package com.dzq.entity;

public class Transaction {

    private long tenantId;

    private String flowKey;

    private String nodeKey;

    private int deployVersion;

    public Transaction(long tenantId, String flowKey, String nodeKey, int deployVersion) {
        this.tenantId = tenantId;
        this.flowKey = flowKey;
        this.nodeKey = nodeKey;
        this.deployVersion = deployVersion;
    }

    public long getTenantId() {
        return tenantId;
    }

    public void setTenantId(long tenantId) {
        this.tenantId = tenantId;
    }

    public String getFlowKey() {
        return flowKey;
    }

    public void setFlowKey(String flowKey) {
        this.flowKey = flowKey;
    }

    public String getNodeKey() {
        return nodeKey;
    }

    public void setNodeKey(String nodeKey) {
        this.nodeKey = nodeKey;
    }

    public int getDeployVersion() {
        return deployVersion;
    }

    public void setDeployVersion(int deployVersion) {
        this.deployVersion = deployVersion;
    }
}
