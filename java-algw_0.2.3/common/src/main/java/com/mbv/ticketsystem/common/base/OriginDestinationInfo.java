package com.mbv.ticketsystem.common.base;

public class OriginDestinationInfo extends TravelDateInfo {
    @Override
	public String toString() {
		return "OriginDestinationInfo [originCode=" + originCode
				+ ", destinationCode=" + destinationCode + "]";
	}

	private String originCode;
    private String destinationCode;

    public String getOriginCode() {
        return originCode;
    }

    public void setOriginCode(String originCode) {
        this.originCode = originCode;
    }

    public String getDestinationCode() {
        return destinationCode;
    }

    public void setDestinationCode(String destinationCode) {
        this.destinationCode = destinationCode;
    }

}
