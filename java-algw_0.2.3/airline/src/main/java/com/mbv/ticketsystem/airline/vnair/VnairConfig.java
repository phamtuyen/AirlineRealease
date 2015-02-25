package com.mbv.ticketsystem.airline.vnair;

import java.util.HashMap;

public class VnairConfig {
    @SuppressWarnings("serial")
    private HashMap<String, String> bookingClassMap = new HashMap<String, String>() {
        {
            put("C", "Business Flex");
            put("K", "Eco Flex");
            put("M", "Saver Flex");
            put("L", "Saver Flex");
            put("Q", "Saver");
            put("R", "Saver");
            put("O", "Saver");
            put("P", "Super Saver");
            put("E", "Super Saver");
        }
    };

    public HashMap<String, String> getBookingClassMap() {
        return bookingClassMap;
    }

    public void setBookingClassMap(HashMap<String, String> bookingClassMap) {
        this.bookingClassMap = bookingClassMap;
    }


}
