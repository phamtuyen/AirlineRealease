package com.mbv.ticketsystem.common.airline;

import com.mbv.ticketsystem.common.base.PersonInfo;


public class AirPassengerInfo extends PersonInfo {
	private String reference;
    private AirPassengerType passengerType;
    private String accompaniedBy;

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public AirPassengerType getPassengerType() {
        return passengerType;
    }

    public void setPassengerType(AirPassengerType passengerType) {
        this.passengerType = passengerType;
    }

    public String getAccompaniedBy() {
        return accompaniedBy;
    }

    public void setAccompaniedBy(String accompaniedBy) {
        this.accompaniedBy = accompaniedBy;
    }

	@Override
	public String toString() {
		return "AirPassengerInfo [reference=" + reference + ", passengerType="
				+ passengerType + ", accompaniedBy=" + accompaniedBy + "]";
	}
    
}
