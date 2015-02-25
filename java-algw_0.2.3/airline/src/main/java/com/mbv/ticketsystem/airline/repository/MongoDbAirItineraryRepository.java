package com.mbv.ticketsystem.airline.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.mbv.ticketsystem.common.airline.AirExtraService;
import com.mbv.ticketsystem.common.airline.AirFareInfo;
import com.mbv.ticketsystem.common.airline.AirItinerary;
import com.mbv.ticketsystem.common.airline.AirPassengerInfo;
import com.mbv.ticketsystem.common.airline.AirTicketingInfo;
import com.mbv.ticketsystem.common.base.AgentInfo;
import com.mbv.ticketsystem.common.base.ContactInfo;

public class MongoDbAirItineraryRepository implements AirItineraryRepository {
    private static final String COLLECTION_NAME = "AirItinerary";
    private MongoTemplate mongoTemplate;

    public MongoDbAirItineraryRepository(MongoTemplate template) {
        this.mongoTemplate = template;
    }

    public void add(AirItinerary itinerary) throws Exception {
        try {
            mongoTemplate.insert(itinerary, COLLECTION_NAME);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception("SYS_INTERNAL_ERROR");
        }
    }

    public AirItinerary findById(String id) {
        try {
            return mongoTemplate.findById(id, AirItinerary.class, COLLECTION_NAME);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    //optimistic locking by updatedDate
    public void update(AirItinerary itinerary) throws Exception {
        AirTicketingInfo ticketingInfo = itinerary.getTicketingInfo();
        Date oldDate = ticketingInfo.getUpdatedDate();
        ticketingInfo.setUpdatedDate(new Date());
        Criteria criteria = Criteria.where("id").is(itinerary.getId())
                .and("ticketingInfo.updatedDate").is(oldDate);
        Query query = new Query(criteria);
        Update update = new Update().set("ticketingInfo", ticketingInfo);
        if (mongoTemplate.updateFirst(query, update, COLLECTION_NAME).getN() != 1)
            throw new Exception("UPDATE_FAILED");
    }
    
//    public void updateBookingInfo(AirItinerary itinerary) throws Exception{
//    	AirTicketingInfo ticketingInfo = itinerary.getTicketingInfo();
//    	 List<AirPassengerInfo> passengerInfos = itinerary.getPassengerInfos();
//    	 List<AirFareInfo> fareInfos = itinerary.getFareInfos();
//        Date oldDate = ticketingInfo.getUpdatedDate();
//        ticketingInfo.setUpdatedDate(new Date());
//        Criteria criteria = Criteria.where("id").is(itinerary.getId())
//                .and("ticketingInfo.updatedDate").is(oldDate);              
//    	 Query query = new Query(criteria);
//         Update update = new Update().set("passengerInfos", passengerInfos).set("fareInfos", fareInfos).set("ticketingInfo", ticketingInfo);        
//         if (mongoTemplate.updateFirst(query, update, COLLECTION_NAME).getN() != 1)
//             throw new Exception("UPDATE_FAILED");
//    }
    
    public void updateBookingInfo(AirItinerary itinerary) throws Exception {
        AgentInfo agentInfo = itinerary.getAgentInfo();
        ContactInfo contactInfo = itinerary.getContactInfo();
        AirTicketingInfo ticketingInfo = itinerary.getTicketingInfo();
        List<AirPassengerInfo> passengerInfos = itinerary.getPassengerInfos();
        List<AirFareInfo> fareInfos = itinerary.getFareInfos();
        List<AirExtraService> extraServices = itinerary.getExtraServices();

        Criteria criteria = Criteria.where("id").is(itinerary.getId());
        Query query = new Query(criteria);
        Update update = new Update()
                .set("agentInfo", agentInfo)
                .set("ticketingInfo", ticketingInfo)
                .set("fareInfos", fareInfos)
                .set("passengerInfos", passengerInfos)
                .set("contactInfo", contactInfo);
        if(extraServices.size() !=0)
        	update.set("extraServices", extraServices);
        if (mongoTemplate.updateFirst(query, update, COLLECTION_NAME).getN() != 1)
            throw new Exception("UPDATE_FAILED");
    }


    public List<AirItinerary> findByFare(AirFareInfo fareInfo) {
        //mongoTemplate.find(query, entityClass, collectionName)
        //vendor
        //originCode
        //destinationCode
        //departureDate
        //flightCode
        Query query = new Query();
        Criteria fareCriteria = Criteria.where("vendor").is(fareInfo.getVendor())
                .and("classCode").is(fareInfo.getClassCode())
                .and("flightCode").is(fareInfo.getFlightCode())
                .and("departureDate").is(fareInfo.getDepartureDate());
        query.addCriteria(Criteria.where("fareInfos").elemMatch(fareCriteria));
        //query.addCriteria(Criteria.where("fareInfos").elemMatch(Criteria.where("vendor").is("VN")));
        //query.addCriteria(Criteria.where("fareInfos").elemMatch(Criteria.where("flightCode").is("221")));
        //query.addCriteria(Criteria.where("ticketingInfo.status").is(AirTicketingStatus.BOOKED));

        //query.addCriteria(Criteria.where("fareInfos").elemMatch(Criteria.where("departureDate").is(fareInfo.getDepartureDate())));
        return mongoTemplate.find(query, AirItinerary.class, COLLECTION_NAME);
        //return null;
    }
    
    public AirItinerary findByReservationCode(AirTicketingInfo ticketingInfo){
    	 Query query = new Query();
    	 query.addCriteria(Criteria.where("ticketingInfo.reservationCode").is(ticketingInfo.getReservationCode()));
    	 query.addCriteria(Criteria.where("ticketingInfo.status").is("BOOK_SUCCESS"));
         return mongoTemplate.findOne(query, AirItinerary.class, COLLECTION_NAME);
         
    }
}
