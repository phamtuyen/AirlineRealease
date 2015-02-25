package com.mbv.ticketsystem.airline.vnair;

import java.util.Random;

import com.mbv.ticketsystem.airline.AbstractAirWorker;
import com.mbv.ticketsystem.airline.vnair.VniscHttpStub.BookResult;
import com.mbv.ticketsystem.airline.vnair.VniscSoapStub.BuyResult;
import com.mbv.ticketsystem.common.airline.AirFarePriceInfos;
import com.mbv.ticketsystem.common.airline.AirItinerary;
import com.mbv.ticketsystem.common.airline.AirTicketingInfo;
import com.mbv.ticketsystem.common.airline.AirTicketingStatus;
import com.mbv.ticketsystem.common.airline.UpdateFarePriceCommand;

public class VnairWorker extends AbstractAirWorker {
    private VniscHttpStub httpStub;
    private VniscSoapStub soapStub;

    public VnairWorker(VniscHttpStub httpStub, VniscSoapStub soapStub) {
        this.httpStub = httpStub;
        this.soapStub = soapStub;
    }

    @Override
    protected AirFarePriceInfos doSearch(UpdateFarePriceCommand request) {
        AirFarePriceInfos result = null;
        for (int retry = 0; retry <= 1; retry++) {
            try {
                result = httpStub.search(request);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    protected void doBook(AirItinerary itinerary, long prices) {
//        AirTicketingInfo info = itinerary.getTicketingInfo();
//        try {
//            BookResult result = httpStub.book(itinerary, prices);
//            info.setReservationCode(result.getReservationCode());
//            info.setRefContent(result.getOrderId());
//            long amount = soapStub.checkAndGetAmount(itinerary, result.getOrderId());
//            info.setAmount(amount);
//            info.setStatus(AirTicketingStatus.BOOK_SUCCESS);
//        } catch (Exception ex) {
//            info.setStatus(AirTicketingStatus.BOOK_ERROR);
//            info.setDescription(ex.getMessage());
//        }
    }

    private static Random rnd = new Random();

    @Override
    protected void doBuy(AirItinerary itinerary) {
        AirTicketingInfo info = itinerary.getTicketingInfo();
        try {
            // BuyResult result = soapStub.PayItinerary(itinerary);
            BuyResult result;
            switch (rnd.nextInt() % 5) {
                case 0:
                case 1:
                case 2:
                    String[] numbers = new String[itinerary.getPassengerInfos().size()];
                    for (int i = 0; i < numbers.length; i++)
                        numbers[i] = "TEST" + Integer.toString(Math.abs(rnd.nextInt()));
                    result = new BuyResult(numbers);
                    break;
                case 3:
                    result = new BuyResult("Sample_Error");
                    break;
                default:
                    throw new Exception("Sample_Exception");
            }

            if (result.isError()) {
                info.setStatus(AirTicketingStatus.BUY_ERROR);
                info.setDescription(result.getDescription());
            } else {
                info.setTicketNumbers(result.getTicketNumbers());
                info.setStatus(AirTicketingStatus.BUY_SUCCESS);
            }
        } catch (Exception ex) {
            info.setStatus(AirTicketingStatus.UNKNOWN);
            info.setDescription(ex.getMessage());
        }
    }
    
    @Override
    protected void doSearchBookingId(AirItinerary itinerary) {
    	System.out.println("456");
    }
}
