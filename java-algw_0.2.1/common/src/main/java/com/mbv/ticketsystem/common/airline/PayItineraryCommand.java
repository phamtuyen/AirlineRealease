package com.mbv.ticketsystem.common.airline;

public class PayItineraryCommand {
    private String id;

    public PayItineraryCommand() {
    }

    public PayItineraryCommand(String Id) {
        this.id = Id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
