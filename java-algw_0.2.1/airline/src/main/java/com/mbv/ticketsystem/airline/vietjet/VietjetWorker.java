package com.mbv.ticketsystem.airline.vietjet;

import java.util.List;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.apache.log4j.Logger;
import com.mbv.ticketsystem.airline.AbstractAirWorker;
import com.mbv.ticketsystem.airline.WorkerAvailableMessage;
import com.mbv.ticketsystem.airline.rules.FarePriceRule;
import com.mbv.ticketsystem.airline.vietjet.VietjetPay.BuyResult;
import com.mbv.ticketsystem.common.airline.AirFarePriceInfos;
import com.mbv.ticketsystem.common.airline.AirItinerary;
import com.mbv.ticketsystem.common.airline.AirTicketingInfo;
import com.mbv.ticketsystem.common.airline.AirTicketingStatus;
import com.mbv.ticketsystem.common.airline.UpdateFarePriceCommand;

public class VietjetWorker extends AbstractAirWorker {

//    private static final Logger logger = LoggerFactory.getLogger(VietjetWorker.class);
	final static Logger logger = Logger.getLogger(VietjetWorker.class);
    private VietjetStub stub;

    public VietjetWorker(VietjetConfig account, List<FarePriceRule> fareRules) {
        this.stub = new VietjetStub(account);
        setAvailableMessage(new WorkerAvailableMessage("Vietjet"));
    }

    @Override
    protected AirFarePriceInfos doSearch(UpdateFarePriceCommand request) {
    	logger.info("VietjetSearch:" + request.toString());
        AirFarePriceInfos result = null;
        for (int retry = 0; retry <= 1 && result == null; retry++) {
            try {
                result = stub.search(request);
            } catch (Exception e) {
                logger.info(e.getMessage());
            }
        }
        return result;
    }

    @Override
//    protected void doBook(AirItinerary itinerary, AirFarePriceInfos prices) {
    protected void doBook(AirItinerary itinerary,long sumPrices) {
    	logger.info("VietjetWorkerBook:" + itinerary.toString());
    	AirTicketingInfo info = itinerary.getTicketingInfo();
    	BookResult result = null;
    	for(int i = 0; i <=1 && result == null; i++){
    		try {               		
    			result= bookAirItinerary(itinerary,sumPrices);
    			if(result != null){
    				if(result.getReservationCode() != "1111111"){					
    					info.setReservationCode(result.getReservationCode());
    					info.setAmount(result.getAmount());
    					info.setStatus(AirTicketingStatus.BOOK_SUCCESS);
    				}
    				else{									
    					info.setAmount(0000000);
    					info.setStatus(AirTicketingStatus.BOOK_ERROR);
    					logger.info("VietjetBook: Prices difference");
    				}
    			}
    			else{					
    				info.setReservationCode("");
    				info.setAmount(0000000);
    				info.setStatus(AirTicketingStatus.BOOK_ERROR);
    				logger.info("VietjetBook: RevertionCode null");					
    			}      	        	       
    		}catch (Exception ex) {
    			info.setStatus(AirTicketingStatus.BOOK_ERROR);
    			info.setDescription(ex.getMessage());				
    		}
    	}
	}

    @Override
    protected void doBuy(AirItinerary itinerary) {
    	logger.info("VietjetPay:" + itinerary.toString());
    	AirTicketingInfo info = itinerary.getTicketingInfo();
		BuyResult result = null;
		for(int i = 0; i <=1 && result == null; i++){
			try {						
				result = buyAirItinerary(itinerary);
				if(result.getTicketNumbers() != null){
					info.setDescription("SUCCESS");
					info.setTicketNumbers(result.getTicketNumbers());
					info.setStatus(AirTicketingStatus.BUY_SUCCESS);					
				}
				else{			
					info.setDescription("ERROR");
					info.setTicketNumbers(result.getTicketNumbers());
					info.setStatus(AirTicketingStatus.BUY_ERROR);					
				}
	
			} catch (Exception ex) {
				info.setStatus(AirTicketingStatus.UNKNOWN);
				info.setDescription(ex.getMessage());
			}
		}
    }
    
    @Override
    protected void doSearchBookingId(AirItinerary itinerary) {
    	logger.info("VietjetWorkerBook:" + itinerary.toString());
    	AirTicketingInfo info = itinerary.getTicketingInfo();
		BookResult result = null;
		for(int i = 0; i <=1 && result == null; i++){
			try {               		
				result= bookAirItinerary(itinerary);  	       
			}catch (Exception ex) {
				info.setStatus(AirTicketingStatus.BOOK_ERROR);
				info.setDescription(ex.getMessage());				
			}
		}
    }
    
    private BookResult bookAirItinerary(AirItinerary itinerary) throws Exception {        	        	                   	
		return stub.vietjetSearchBookId(itinerary);
	}
    
    private BookResult bookAirItinerary(AirItinerary itinerary, long sumPrices) throws Exception {        	        	                   	
		return stub.bookVietJet(itinerary,sumPrices);
	}

	private BuyResult buyAirItinerary(AirItinerary itinerary) throws Exception{
		return stub.buyVietJet(itinerary);
	}
    
    public static class BookResult {
		private String reservationCode;
		//private String orderId;
		private long amount;

		public long getAmount() {
			return amount;
		}

		public void setAmount(long amount) {
			this.amount = amount;
		}

		public String getReservationCode() {
			return reservationCode;
		}

		public void setReservationCode(String reservationCode) {
			this.reservationCode = reservationCode;
		}

//		public String getOrderId() {
//			return orderId;
//		}
//
//		public void setOrderId(String orderId) {
//			this.orderId = orderId;
//		}
	}

}
