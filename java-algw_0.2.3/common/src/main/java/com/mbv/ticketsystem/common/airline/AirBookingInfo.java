package com.mbv.ticketsystem.common.airline;

import java.util.List;

import com.mbv.ticketsystem.common.base.AgentInfo;
import com.mbv.ticketsystem.common.base.ContactInfo;

public class AirBookingInfo { 
	private AgentInfo agentInfo;
    private List<AirFareInfo> fareInfos;
    private List<AirPassengerInfo> passengerInfos;
    private ContactInfo contactInfo;
    private List<AirExtraService> extraServices;


    public List<AirExtraService> getExtraServices() {
        return extraServices;
    }

    public void setExtraServices(List<AirExtraService> extraServices) {
        this.extraServices = extraServices;
    }

    public List<AirFareInfo> getFareInfos() {
        return fareInfos;
    }

    public void setFareInfos(List<AirFareInfo> fareInfos) {
        this.fareInfos = fareInfos;
    }

    public List<AirPassengerInfo> getPassengerInfos() {
        return passengerInfos;
    }

    public void setPassengerInfos(List<AirPassengerInfo> passengerInfos) {
        this.passengerInfos = passengerInfos;
    }

    public AgentInfo getAgentInfo() {
        return agentInfo;
    }

    public void setAgentInfo(AgentInfo agentInfo) {
        this.agentInfo = agentInfo;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }
    
    @Override
	public String toString() {
		return "AirBookingInfo [agentInfo=" + agentInfo + ", fareInfos="
				+ fareInfos + ", passengerInfos=" + passengerInfos
				+ ", contactInfo=" + contactInfo + ", extraServices="
				+ extraServices + "]";
	}
}
