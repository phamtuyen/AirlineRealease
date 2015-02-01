package com.mbv.ticketsystem.common.airline;

import java.util.ArrayList;
import java.util.List;

public class AirFarePriceOption {
    private String reference;
    private String classCode;
    private String className;
    private List<AirPassengerTypePrice> priceDetail;

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

    public List<AirPassengerTypePrice> getPriceDetail() {
        return priceDetail;
    }

    public void setPriceDetail(List<AirPassengerTypePrice> priceDetail) {
        this.priceDetail = priceDetail;
    }

    ;

    public void add(AirPassengerTypePrice price) {
        if (this.priceDetail == null) this.priceDetail = new ArrayList<AirPassengerTypePrice>();
        this.priceDetail.add(price);
    }

    public AirPassengerTypePrice getPrice(AirPassengerType type) {
        if (this.priceDetail == null) return null;
        for (AirPassengerTypePrice item : priceDetail) {
            if (item.getPassengerType() == type) return item;
        }
        return null;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

	@Override
	public String toString() {
		return "AirFarePriceOption [reference=" + reference + ", classCode="
				+ classCode + ", className=" + className + ", priceDetail="
				+ priceDetail + "]";
	}
    
    
}
