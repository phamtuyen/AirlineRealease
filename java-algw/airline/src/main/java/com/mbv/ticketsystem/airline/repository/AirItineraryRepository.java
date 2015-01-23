package com.mbv.ticketsystem.airline.repository;

import java.util.List;

import com.mbv.ticketsystem.common.airline.AirFareInfo;
import com.mbv.ticketsystem.common.airline.AirItinerary;

public interface AirItineraryRepository {

    public void add(AirItinerary itinerary) throws Exception;

    public AirItinerary findById(String id);

    public void update(AirItinerary itinerary) throws Exception;

    public List<AirItinerary> findByFare(AirFareInfo fareInfo);
}
