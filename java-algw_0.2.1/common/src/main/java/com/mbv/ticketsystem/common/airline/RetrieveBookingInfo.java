package com.mbv.ticketsystem.common.airline;

import com.mbv.ticketsystem.common.base.AgentInfo;

import java.util.UUID;

public class RetrieveBookingInfo {
    private String id;
    private String vendor;
    private String reservationCode;
    private AgentInfo agentInfo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void generateId() {
        this.setId(UUID.randomUUID().toString().replace("-", ""));
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getReservationCode() {
        return reservationCode;
    }

    public void setReservationCode(String reservationCode) {
        this.reservationCode = reservationCode;
    }
    public AgentInfo getAgentInfo() {
        return agentInfo;
    }

    public void setAgentInfo(AgentInfo agentInfo) {
        this.agentInfo = agentInfo;
    }
}
