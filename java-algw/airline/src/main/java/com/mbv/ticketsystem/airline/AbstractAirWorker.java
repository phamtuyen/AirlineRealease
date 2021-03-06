package com.mbv.ticketsystem.airline;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mbv.ticketsystem.airline.repository.AirItineraryRepository;
import com.mbv.ticketsystem.airline.rules.FarePriceRule;
import com.mbv.ticketsystem.airline.support.AirFarePriceCache;
import com.mbv.ticketsystem.airline.support.ItineraryFilter;
import com.mbv.ticketsystem.common.airline.AirExtraService;
import com.mbv.ticketsystem.common.airline.AirFareInfo;
import com.mbv.ticketsystem.common.airline.AirFarePriceInfos;
import com.mbv.ticketsystem.common.airline.AirFarePriceOption;
import com.mbv.ticketsystem.common.airline.AirItinerary;
import com.mbv.ticketsystem.common.airline.AirPassengerInfo;
import com.mbv.ticketsystem.common.airline.AirPassengerType;
import com.mbv.ticketsystem.common.airline.AirPassengerTypePrice;
import com.mbv.ticketsystem.common.airline.AirPassengerTypeQuantity;
import com.mbv.ticketsystem.common.airline.AirTicketingInfo;
import com.mbv.ticketsystem.common.airline.AirTicketingStatus;
import com.mbv.ticketsystem.common.airline.BookItineraryCommand;
import com.mbv.ticketsystem.common.airline.PayItineraryCommand;
import com.mbv.ticketsystem.common.airline.UpdateFarePriceCommand;

import akka.actor.UntypedActor;

public abstract class AbstractAirWorker extends UntypedActor {

	protected static final Logger logger = LoggerFactory.getLogger(AbstractAirWorker.class);

	private AirItineraryRepository itineraryRepository;

	public AirItineraryRepository getItineraryRepository() {
		return itineraryRepository;
	}

	public void setItineraryRepository(AirItineraryRepository itineraryRepository) {
		this.itineraryRepository = itineraryRepository;
	}

	private AirFarePriceCache farePriceCache;

	private List<FarePriceRule> fareRules;

	private WorkerAvailableMessage availableMessage;

	public WorkerAvailableMessage getAvailableMessage() {
		return availableMessage;
	}

	public void setAvailableMessage(WorkerAvailableMessage availableMessage) {
		this.availableMessage = availableMessage;
	}

	public AirFarePriceCache getFarePriceCache() {
		return farePriceCache;
	}

	public void setFarePriceCache(AirFarePriceCache farePriceCache) {
		this.farePriceCache = farePriceCache;
	}

	public List<FarePriceRule> getFareRules() {
		if (fareRules == null)
			fareRules = new ArrayList<FarePriceRule>();
		return fareRules;
	}

	public void setFareRules(List<FarePriceRule> fareRules) {
		this.fareRules = fareRules;
	}

	private ItineraryFilter filter;

	public void preStart() {
		logger.info("{} started", getSelf());
		this.filter = new ItineraryFilter(itineraryRepository);
		getContext().parent().tell(availableMessage, getSelf());
	}

	@Override
	public void onReceive(Object message) {
		logger.info("{} received {}", getSelf(), message);
		if (message instanceof UpdateFarePriceCommand) {
			updateFarePriceInfos((UpdateFarePriceCommand) message);
		} else if (message instanceof BookItineraryCommand) {
			
			AirItinerary itinerary = itineraryRepository.findById(((BookItineraryCommand) message).getId());
			if (itinerary != null) {
				AirTicketingInfo info = itinerary.getTicketingInfo();
				if (filter.mightDuplicated(itinerary)) {
					info.setStatus(AirTicketingStatus.BOOK_ERROR);
					info.setDescription("DUPLICATION_FOUND");
				} else {
					UpdateFarePriceCommand updatePriceCommand = UpdateFarePriceCommand.create(itinerary);					
					String hashString = updatePriceCommand.toHashString();                        
					AirFarePriceInfos result = farePriceCache.find(hashString);   
					AirFareInfo fareInfo0 = itinerary.getFareInfos().get(0);
					String reference0 = fareInfo0.getReference();					
					ArrayList<AirFareInfo> listAirFareInfo = (ArrayList<AirFareInfo>)itinerary.getFareInfos();
					List<AirPassengerInfo> listAirPassengerInfo = (List<AirPassengerInfo>)itinerary.getPassengerInfos();
					UpdateFarePriceCommand request = new UpdateFarePriceCommand();
					request.setOriginDestinationInfos(listAirFareInfo);
					request.setPassengerInfos(getAirPassengerTypeQuantity(listAirPassengerInfo));
					long sumPrices = sumPrices(result,reference0,request);		
					String vendor = fareInfo0.getVendor();
					List<AirExtraService> extraServices = itinerary.getExtraServices();{
						if(extraServices != null)
							for(int i=0;i<extraServices.size();i++){
								AirExtraService service = extraServices.get(i);
								if(vendor.equals("BL"))								
									sumPrices += jetstarLuggage(service);
							}
					}				
					AirFareInfo fareInfo1 =  null;
					String reference1 = "";
					if(itinerary.getFareInfos().size() == 2){
						fareInfo1 = itinerary.getFareInfos().get(1); 
						reference1 = fareInfo1.getReference();
						sumPrices += sumPrices(result,reference1,request);
					}   
					logger.info("call function doBook():" + itinerary.toString());
					doBook(itinerary, sumPrices);
					AirTicketingInfo ticketingInfo = itinerary.getTicketingInfo();
					if(ticketingInfo.getReservationCode().equals("1111111")){
						updateFarePriceInfos(updatePriceCommand); 
						info.setReservationCode("");
						info.setStatus(AirTicketingStatus.BOOK_ERROR);
						info.setDescription("FARE_PRICES_NOT_FOUND");
					}
				}
				try {
					logger.info("Result (data):" + itinerary.toString());
					itineraryRepository.update(itinerary);
				} catch (Exception ex) {
					logger.info(ex.getMessage());
				}
			}
		} else if (message instanceof PayItineraryCommand) {
			AirItinerary itinerary = itineraryRepository.findById(((PayItineraryCommand) message).getId());
			if (itinerary != null) {
				AirTicketingInfo info = itinerary.getTicketingInfo();
				logger.info("call function PayItineraryCommand:" + itinerary.toString());
				if (info.getStatus() == AirTicketingStatus.BUY_PENDING) {
					info.setStatus(AirTicketingStatus.BUY_PROCESSING);
					try {
						itineraryRepository.update(itinerary);
						doBuy(itinerary);
						itineraryRepository.update(itinerary);
					} catch (Exception ex) {
						logger.info(ex.getMessage());
					}
				}
			}
		}
		getContext().parent().tell(availableMessage, getSelf());
	}
	
	public long jetstarLuggage(AirExtraService service){
		long sum = 0;
		String pasKg = service.getServiceCode();
		if(pasKg.equals("BG15"))
			sum += 143000;
		if(pasKg.equals("BG20"))
			sum += 165000;
		if(pasKg.equals("BG25"))
			sum += 220000;
		if(pasKg.equals("BG30"))
			sum += 270000;
		if(pasKg.equals("BG35"))
			sum += 320000;
		if(pasKg.equals("BG40"))
			sum += 370000;
		return sum;
	}
	
	private long sumPrices(AirFarePriceInfos result,String reference,UpdateFarePriceCommand request){
		long sum = 0;
		int numADT = request.getPassengerQuantity(AirPassengerType.ADT);
		int numCHD = request.getPassengerQuantity(AirPassengerType.CHD);
		int numINF = request.getPassengerQuantity(AirPassengerType.INF);
		List<AirFarePriceOption> priceOptions = null;
		for(int i = 0;i < result.size();i++){
			priceOptions = result.get(i).getPriceOptions();
			for(int j = 0;j < priceOptions.size();j++){ 
				AirFarePriceOption priceOption = priceOptions.get(j);                    		
				if(priceOption.getReference().equals(reference)){                    		
					List<AirPassengerTypePrice> priceDetail = priceOption.getPriceDetail();
					for(int k = 0;k < priceDetail.size();k++){
						if(k==0)
							sum += priceDetail.get(k).getPrice()*numADT;
						else if(k==1)
							sum += priceDetail.get(k).getPrice()*numCHD;
						else if(k==2)
							sum += priceDetail.get(k).getPrice()*numINF;
					}
				}
			}                    	
		}    	      
		return sum;
	}
	
	public ArrayList<AirPassengerTypeQuantity> getAirPassengerTypeQuantity(List<AirPassengerInfo> listAirPassengerInfo){
		ArrayList<AirPassengerTypeQuantity> list = new ArrayList<AirPassengerTypeQuantity>();
		int curADT = 0;
		int curCHD = 0;
		int curINF = 0;
		for (AirPassengerInfo paxInfo : listAirPassengerInfo) {
			switch (paxInfo.getPassengerType()) {
			case ADT:
				curADT++;
				break;
			case CHD:
				curCHD++;
				break;
			case INF:
				curINF++;
				break;
			}
		}
		AirPassengerTypeQuantity aptqadt = new AirPassengerTypeQuantity(AirPassengerType.ADT,curADT);
		list.add(aptqadt);
		AirPassengerTypeQuantity aptqchd = new AirPassengerTypeQuantity(AirPassengerType.CHD,curCHD);
		list.add(aptqchd);
		AirPassengerTypeQuantity aptqinf = new AirPassengerTypeQuantity(AirPassengerType.INF,curINF);
		list.add(aptqinf);

		return list;
	}

	private AirFarePriceInfos updateFarePriceInfos(UpdateFarePriceCommand command) {
		AirFarePriceInfos result = doSearch(command);
		if (result != null) {
			try {
				String hashString = command.toHashString();
				farePriceCache.update(hashString, result);
			} catch (Exception ex) {
				logger.info(ex.getMessage());
				result = null;
			}
			logger.info("{} finished: result    {}", getSelf(), result.toString());
		}
		return result;    	
	}

	protected abstract AirFarePriceInfos doSearch(UpdateFarePriceCommand command);

	//    protected abstract void doBook(AirItinerary itinerary, AirFarePriceInfos prices);
	protected abstract void doBook(AirItinerary itinerary, long sumPrices);

	protected abstract void doBuy(AirItinerary itinerary);
}
