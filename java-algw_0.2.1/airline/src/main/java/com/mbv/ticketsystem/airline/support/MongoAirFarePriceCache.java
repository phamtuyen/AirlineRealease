package com.mbv.ticketsystem.airline.support;

import java.util.Date;
import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.mbv.ticketsystem.common.airline.AirFareInfo;
import com.mbv.ticketsystem.common.airline.AirFarePriceInfos;
import com.mbv.ticketsystem.common.airline.AirItinerary;

public class MongoAirFarePriceCache implements AirFarePriceCache {
    private final String COLLECTION_NAME = "AirFarePriceCache";
    private MongoTemplate template;

    public MongoAirFarePriceCache(MongoTemplate template) {
        this.template = template;
    }

    public boolean hasResult(String id) {
        return template.count(new Query(Criteria.where("id").is(id)), COLLECTION_NAME) != 0;
    }

    public AirFarePriceInfos find(String id) {
        CacheItem item = template.findOne(new Query(Criteria.where("id").is(id)), CacheItem.class, COLLECTION_NAME);
        if (item == null) 
        	return null;
        return item.getPriceInfos();
    }
    public Date findByFare(String id){    	
    	CacheItem item = template.findOne(new Query(Criteria.where("id").is(id)), CacheItem.class, COLLECTION_NAME);
    	return item.getCreated();
    }
    
    public void deleteId(String id){
    	template.remove(new Query(Criteria.where("id").is(id)), COLLECTION_NAME);
    }

    public void update(String id, AirFarePriceInfos result) {
        CacheItem item = new CacheItem(id, result);
        template.save(item, COLLECTION_NAME);
    }
    
    
//    public List<AirFarePriceInfos> findByFare(AirFareInfo fareInfo){
//    	Query query = new Query();
//    	Criteria fareCriteria = Criteria.where("vendor").is(fareInfo.getVendor())
//                //.and("classCode").is(fareInfo.getClassCode())
//                .and("flightCode").is(fareInfo.getFlightCode())
//                .and("departureDate").is(fareInfo.getDepartureDate());
//        query.addCriteria(Criteria.where("fareInfos").elemMatch(fareCriteria));   
//        return template.find(query, AirFarePriceInfos.class, COLLECTION_NAME);
//    }

    private static class CacheItem {
        private String id;
        private AirFarePriceInfos priceInfos;
        private Date created;

        public CacheItem() {
        }

        public CacheItem(String id, AirFarePriceInfos priceInfos) {
            this.id = id;
            this.priceInfos = priceInfos;
            this.created = new Date();
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public AirFarePriceInfos getPriceInfos() {
            return priceInfos;
        }

        public void setPriceInfos(AirFarePriceInfos priceInfos) {
            this.priceInfos = priceInfos;
        }

        public Date getCreated() {
            return created;
        }

        public void setCreated(Date created) {
            this.created = created;
        }
    }
}
