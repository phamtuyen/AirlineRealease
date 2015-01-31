package com.mbv.ticketsystem.common.airline;

public class AirPassengerTypePrice {
    private AirPassengerType passengerType;
    private long price;

    public AirPassengerTypePrice() {

    }

    public AirPassengerTypePrice(AirPassengerType passengerType, long price) {
        this.passengerType = passengerType;
        this.price = price;
    }

    public AirPassengerType getPassengerType() {
        return passengerType;
    }

    public void setPassengerType(AirPassengerType passengerType) {
        this.passengerType = passengerType;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }
}
