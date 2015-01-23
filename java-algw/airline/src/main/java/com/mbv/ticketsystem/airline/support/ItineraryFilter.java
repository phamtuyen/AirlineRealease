package com.mbv.ticketsystem.airline.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;
import com.mbv.ticketsystem.airline.repository.AirItineraryRepository;
import com.mbv.ticketsystem.common.airline.AirItinerary;
import com.mbv.ticketsystem.common.airline.AirPassengerInfo;
import com.mbv.ticketsystem.common.airline.AirTicketingStatus;

public class ItineraryFilter {

    private AirItineraryRepository itineraryRepository;

    public ItineraryFilter(AirItineraryRepository itineraryRepository) {
        this.itineraryRepository = itineraryRepository;
    }

    /*
     * "contactInfo": { "address": "H3 Hoang dieu, q4", "city":
     * "TP Ho Chi Minh", "mobile": "0902882076", "email": "support@mobivi.com"
     * }, "passengerInfos": [ { "reference": "1", "passengerType": "ADT",
     * "accompaniedBy": "", "firstName": "Viet Ha", "lastName": "Nguyen",
     * "gender": "MALE" } ],
     */
    @SuppressWarnings("serial")
    private static class ItineraryFunnel implements Funnel<AirItinerary> {
        public void funnel(AirItinerary itinerary, PrimitiveSink sink) {
            // mob;email;pax1;pax2;pax3
            String tmpStr = itinerary.getContactInfo().getMobile().replace(" ", "").toLowerCase();
            sink.putString(tmpStr + ";");
            tmpStr = itinerary.getContactInfo().getEmail().replace(" ", "").toLowerCase();
            sink.putString(tmpStr + ";");

            List<String> paxString = new ArrayList<String>();
            for (AirPassengerInfo paxInfo : itinerary.getPassengerInfos()) {
                tmpStr = paxInfo.getFirstName() + paxInfo.getLastName() + ";";
                paxString.add(tmpStr.replace(" ", "").toLowerCase());
            }
            Collections.sort(paxString);
            for (String pax : paxString)
                sink.putString(pax);
        }
    }

    public boolean mightDuplicated(AirItinerary itinerary) {
        List<AirItinerary> itineraries = itineraryRepository.findByFare(itinerary.getFareInfos().get(0));
        if (itineraries == null || itineraries.isEmpty())
            return false;
        BloomFilter<AirItinerary> filter = BloomFilter.create(new ItineraryFunnel(), itineraries.size(), 0.01);
        for (AirItinerary item : itineraries) {
            if (item.getTicketingInfo().getStatus() == AirTicketingStatus.BOOK_ERROR)
                continue;
            if (!itinerary.getAgentInfo().getAgentId().equals(item.getAgentInfo().getAgentId()))
                continue;
            if (itinerary.getId().equals(item.getId()))
                continue;
            filter.put(item);
        }
        return filter.mightContain(itinerary);
    }
}
