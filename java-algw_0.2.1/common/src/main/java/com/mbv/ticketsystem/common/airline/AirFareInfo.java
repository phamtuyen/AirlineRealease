package com.mbv.ticketsystem.common.airline;

import com.google.common.base.Objects;
import com.mbv.ticketsystem.common.base.OriginDestinationInfo;

public class AirFareInfo extends OriginDestinationInfo {
    private String vendor;
    private String classCode;
    private String reference;
    private String flightCode;   
    
    public String getVendor() {
        return vendor;
    }

    public void setVendor(String verdor) {
        this.vendor = verdor;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public String getFlightCode() {
        return flightCode;
    }

    public void setFlightCode(String flightCode) {
        this.flightCode = flightCode;
    }

	@Override
	public String toString() {
		return "AirFareInfo [vendor=" + vendor + ", classCode=" + classCode
				+ ", reference=" + reference + ", flightCode=" + flightCode
				+ "]";
	}

//    public String toString() {
//        return Objects.toStringHelper(this).omitNullValues()
//                .add("Vendor", getVendor())
//                .add("OriginCode", getOriginCode())
//                .add("DestinationCode", getDestinationCode())
//                .add("DepartureDate", getDepartureDate())
//                .add("ArrivalDate", getArrivalDate())
//                .add("FlightCode", getFlightCode())
//                .add("ClassCode", getClassCode())
//                .add("Reference", getReference())
//                .toString();
//        // return getVendor() + "" + getOriginCode() + getDestinationCode() +
//        // getDepartureDate().toString() + getFlightCode() + getClassCode();
//    }
    
    
}
