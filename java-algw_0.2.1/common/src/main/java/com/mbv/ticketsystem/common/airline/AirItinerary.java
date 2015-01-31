package com.mbv.ticketsystem.common.airline;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.mbv.ticketsystem.common.base.AgentInfo;
import com.mbv.ticketsystem.common.base.ContactInfo;
import com.mbv.ticketsystem.common.base.Gender;



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
    
    public static AirItinerary create(AirItinerary bookingInfo) {
        bookingInfo.generateId();   
//        bookingInfo.getTicketingInfo().setStatus(AirTicketingStatus.BOOK_PENDING);       
        return bookingInfo;
    }
    
    public static AirItinerary create(String bookingId,String verdor) throws ParseException {
    	 AirItinerary itinerary = new AirItinerary();
    	 AirBookingInfo bookingInfo = new AirBookingInfo(); 
    	 AgentInfo agentInfo = new AgentInfo();
    	 agentInfo.setAgentId("mca");
    	 agentInfo.setUserId("admin-mca");
    	 agentInfo.setAddress("104 Mai Thi Luu, Q1");
    	 agentInfo.setEmail("support@mobivi.com");
    	 agentInfo.setMobile("0965292512");
    	 agentInfo.setCity("TP Ho Chi Minh");
    	 
    	 bookingInfo.setAgentInfo(agentInfo);
    	 
    	 List<AirFareInfo> fareInfos = new ArrayList<AirFareInfo>();
    	 AirFareInfo fareInfo = new AirFareInfo();
    	 fareInfo.setVendor(verdor);
    	 fareInfo.setClassCode("");
    	 fareInfo.setReference("");
    	 fareInfo.setFlightCode("");
    	 fareInfo.setOriginCode("");
    	 fareInfo.setDestinationCode("");
    	 fareInfo.setDepartureDate(new Date());
    	 fareInfo.setArrivalDate(new Date());
    	 
    	 fareInfos.add(fareInfo);
    	 bookingInfo.setFareInfos(fareInfos);
    	 
    	 List<AirPassengerInfo> passengerInfos = new ArrayList<AirPassengerInfo>();
    	 AirPassengerInfo passengerInfo = new AirPassengerInfo();
    	 passengerInfo.setReference("");
    	 passengerInfo.setPassengerType(AirPassengerType.ADT);
    	 passengerInfo.setAccompaniedBy("");
    	 passengerInfo.setFirstName("Pham");
    	 passengerInfo.setLastName("Van Tuyen");
    	 passengerInfo.setGender(Gender.MALE);    
    	 passengerInfo.setBirthDate(dateFormat.parse("1990-01-01T00:00:00.000Z"));
    	 
    	 passengerInfos.add(passengerInfo);
    	 bookingInfo.setPassengerInfos(passengerInfos);
    	 
    	 ContactInfo contactInfo = new ContactInfo();
    	 contactInfo.setAddress("104 Mai Thi Luu, Q1");
    	 contactInfo.setCity("TP Ho Chi Minh");
    	 contactInfo.setMobile("01264143088");
    	 contactInfo.setEmail("support@mobivi.com");
    	 
    	 bookingInfo.setContactInfo(contactInfo);
         
         itinerary.setAgentInfo(bookingInfo.getAgentInfo());        
         itinerary.setFareInfos(bookingInfo.getFareInfos());        
         itinerary.setPassengerInfos(bookingInfo.getPassengerInfos());
         itinerary.setContactInfo(bookingInfo.getContactInfo());
         
         itinerary.generateId();   
         AirTicketingInfo ticketingInfo = new AirTicketingInfo();
         ticketingInfo.setCreatedDate(new Date());
         ticketingInfo.setUpdatedDate(new Date());
         ticketingInfo.setStatus(AirTicketingStatus.BOOK_PENDING);
         ticketingInfo.setReservationCode(bookingId);
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
