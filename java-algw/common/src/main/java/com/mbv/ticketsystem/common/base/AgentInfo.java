package com.mbv.ticketsystem.common.base;

public class AgentInfo extends ContactInfo {
    private String agentId;
    private String userId;

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
