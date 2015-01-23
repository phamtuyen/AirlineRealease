package com.mbv.ticketsystem.common.airline;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;



public class AirItinerary extends AirBookingInfo {
    private String id;
    private AirTicketingInfo ticketingInfo;   
    final static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");	
	
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private void generateId() {
        this.setId(UUID.randomUUID().toString().replace("-", ""));
    }

    public static AirItinerary create(AirBookingInfo bookingInfo) {
        AirItinerary itinerary = new AirItinerary();
        itinerary.setAgentInfo(bookingInfo.getAgentInfo());        
        itinerary.setFareInfos(bookingInfo.getFareInfos());        
        itinerary.setPassengerInfos(bookingInfo.getPassengerInfos());
        itinerary.setContactInfo(bookingInfo.getContactInfo());
        itinerary.setExtraServices(bookingInfo.getExtraServices());        
        itinerary.generateId();
        AirTicketingInfo ticketingInfo = new AirTicketingInfo();
        ticketingInfo.setCreatedDate(new Date());
        ticketingInfo.setUpdatedDate(new Date());
        ticketingInfo.setStatus(AirTicketingStatus.BOOK_PENDING);
        itinerary.setTicketingInfo(ticketingInfo);
        return itinerary;
    }

    public AirTicketingInfo getTicketingInfo() {
        return ticketingInfo;
    }

    public void setTicketingInfo(AirTicketingInfo ticketingInfo) {
        this.ticketingInfo = ticketingInfo;
    }

	@Override
	public String toString() {
		return "AirItinerary [id=" + id + ", ticketingInfo=" + ticketingInfo
				+ "]";
	}
    
}
