package com.mbv.ticketsystem.common.airline;

public class AirExtraService {
	public String passengerCode;
	public String serviceCode;
	public String typeFlightCode;
	public String haveReturn;
	
	public String getPassengerCode() {
		return passengerCode;
	}
	public void setPassengerCode(String passengerCode) {
		this.passengerCode = passengerCode;
	}
	public String getServiceCode() {
		return serviceCode;
	}
	public void setServiceCode(String serviceCode) {
		this.serviceCode = serviceCode;
	}
	public String getTypeFlightCode() {
		return typeFlightCode;
	}
	public void setTypeFlightCode(String typeFlightCode) {
		this.typeFlightCode = typeFlightCode;
	}
	public String getHaveReturn() {
		return haveReturn;
	}
	public void setHaveReturn(String haveReturn) {
		this.haveReturn = haveReturn;
	}
	@Override
	public String toString() {
		return "AirExtraService [passengerCode=" + passengerCode
				+ ", serviceCode=" + serviceCode + ", typeFlightCode="
				+ typeFlightCode + ", haveReturn=" + haveReturn + "]";
	}
	
	
}
