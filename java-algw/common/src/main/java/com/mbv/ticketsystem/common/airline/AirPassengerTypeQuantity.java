package com.mbv.ticketsystem.common.airline;

public class AirPassengerTypeQuantity {
    private AirPassengerType passengerType;
    private int quantity;

    public AirPassengerTypeQuantity() {

    }

    public AirPassengerTypeQuantity(AirPassengerType type, int quantity) {
        this.passengerType = type;
        this.quantity = quantity;
    }

    public AirPassengerType getPassengerType() {
        return passengerType;
    }

    public void setPassengerType(AirPassengerType passengerType) {
        this.passengerType = passengerType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
