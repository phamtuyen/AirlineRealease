package com.mbv.ticketsystem.airline.check;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import com.mbv.ticketsystem.airline.jetstar.JetstarAccount;
import com.mbv.ticketsystem.airline.jetstar.JetstarStub;
import com.mbv.ticketsystem.airline.vietjet.VietjetConfig;
import com.mbv.ticketsystem.airline.vietjet.VietjetStub;
import com.mbv.ticketsystem.common.airline.AirExtraService;
import com.mbv.ticketsystem.common.airline.AirFareInfo;
import com.mbv.ticketsystem.common.airline.AirFarePriceInfo;
import com.mbv.ticketsystem.common.airline.AirFarePriceInfos;
import com.mbv.ticketsystem.common.airline.AirItinerary;
import com.mbv.ticketsystem.common.airline.AirPassengerInfo;
import com.mbv.ticketsystem.common.airline.AirPassengerType;
import com.mbv.ticketsystem.common.airline.AirPassengerTypeQuantity;
import com.mbv.ticketsystem.common.airline.UpdateFarePriceCommand;
import com.mbv.ticketsystem.common.base.AgentInfo;
import com.mbv.ticketsystem.common.base.ContactInfo;
import com.mbv.ticketsystem.common.base.Gender;


public class checkService {
	
	final static Logger logger = Logger.getLogger(checkService.class);
	final static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	public static void main(String[] args) throws Exception,InterruptedException {	
		PropertyConfigurator.configure("log4j_config.xml");
		checkVietjet();
		checkJetstar();		
	}

	private static void checkVietjet() throws Exception{
		String vendor = "VJ"; 
		UpdateFarePriceCommand command = createUpdateFarePrice(vendor);
		VietjetConfig vietjetConfig = new VietjetConfig("AG38197106","CMVJPvf8I4xa");
		VietjetStub vietjetStup = new VietjetStub(vietjetConfig);
		AirFarePriceInfos result = null;
		for (int retry = 0; retry <= 1 && result == null; retry++) {
			try {
				result = vietjetStup.search(command);
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
				logger.error(ex.getMessage());
			}
		} 

		if(result != null){ 							
			logger.info("VietjetSearch:true"); 
			AirFarePriceInfo priceInfo = findRevertionCode(result);		
			String classCode = priceInfo.getPriceOptions().get(0).getClassCode();
			String flightCode = priceInfo.getFareInfo().getFlightCode();					
			String reference = priceInfo.getPriceOptions().get(0).getReference();	
			AirItinerary itinerary = createItinerary(vendor, classCode, flightCode, reference);
			boolean ItineraryResult = vietjetStup.verifyBook(itinerary);
			if(ItineraryResult == true){
				logger.info("VietjetBook:true");					
			}				
			else{
				logger.info("VietjetBook:false");					
			}
		}			
		else{
			logger.info("VietjetSearch: false");					
		}

	}

	private static void checkJetstar() throws Exception{		
		UpdateFarePriceCommand command = createUpdateFarePrice("BL");
		JetstarAccount jetstarAccount = new JetstarAccount("ngahuynh","MTLEDC14","");
		JetstarStub jetstar = new JetstarStub(jetstarAccount);		
		AirFarePriceInfos result = null;
		for (int retry = 0; retry <= 1 && result == null; retry++) {
			try {
				result = jetstar.search(command);
			} catch (Exception ex) {					
				logger.error("JetstarSearch: "+ ex.getMessage()); 
			}
		} 

		if(result != null){
			System.out.println("JetstarSearch: true");
			AirFarePriceInfo priceInfo = findRevertionCode(result);		
			String classCode = priceInfo.getPriceOptions().get(0).getClassCode();
			String flightCode = priceInfo.getFareInfo().getFlightCode();					
			String reference = priceInfo.getPriceOptions().get(0).getReference();	
			AirItinerary itinerary = createItinerary("BL", classCode, flightCode, reference);
			boolean itineraryResult = jetstar.verifyBook(itinerary);
			if(itineraryResult == true){
				logger.info("JetstarBook: true");
			}				
			else{
				logger.info("JetstarBook: false");
			}
		}
		else{
			logger.info("JetstarSearch: false");					
		}				
	}

	public static UpdateFarePriceCommand createUpdateFarePrice(String vendor) throws ParseException{
		// fareInfo
		AirFareInfo fareInfo = new AirFareInfo();
		fareInfo.setVendor(vendor);
		fareInfo.setOriginCode("SGN");
		fareInfo.setDestinationCode("HAN");
		String yyyymmddDeparture  =  createDate();
		fareInfo.setDepartureDate(dateFormat.parse(yyyymmddDeparture + "-27"));
		fareInfo.setArrivalDate(dateFormat.parse(yyyymmddDeparture + "-27"));
		List<AirFareInfo> fareInfoList = new ArrayList<AirFareInfo>();	
		fareInfoList.add(fareInfo);

		// Passenger
		List<AirPassengerTypeQuantity> passList = new ArrayList<AirPassengerTypeQuantity>();
		passList.add(new AirPassengerTypeQuantity(AirPassengerType.ADT, 1));
		passList.add(new AirPassengerTypeQuantity(AirPassengerType.CHD, 0));
		passList.add(new AirPassengerTypeQuantity(AirPassengerType.INF, 0));

		UpdateFarePriceCommand command = new UpdateFarePriceCommand();
		command.setOriginDestinationInfos(fareInfoList);
		command.setPassengerInfos(passList);

		return command;
	}

	private static String createDate() throws ParseException{	
		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());    
		int day = calendar.get(Calendar.DATE);
		int month = calendar.get(Calendar.MONTH) + 1;
		int year = calendar.get(Calendar.YEAR);	
		if(day > 22)
			month += 1;
		if(month == 1 || month == 2 || month == 3 || month > 11){
			month = 4;
			if(month > 11)
				year += 1;
		}
		String yyyymm =  year +"-"+ month;
		return yyyymm;		
	}

	private static int createYearBirthday(){ 
		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());  
		int year = calendar.get(Calendar.YEAR);	
		year--;
		return year;
	}

	private static AirFarePriceInfo findRevertionCode(AirFarePriceInfos result){
		return result.get(0);
	}

	private static AirItinerary createItinerary(final String vendor,final String classCode,final String flightCode,final String reference) throws ParseException, JsonGenerationException, JsonMappingException, IOException{
		final List<AirFareInfo> fareInfos = new ArrayList<AirFareInfo>();
		// fareInfos
		fareInfos.add(new AirFareInfo() {
			{			
				setVendor(vendor);
				setClassCode(classCode); 
				setFlightCode(flightCode);
				setOriginCode("SGN");
				setDestinationCode("HAN");
				setDepartureDate(dateFormat.parse(createDate() +"-27"));						
				setArrivalDate(dateFormat.parse(createDate() +"-27"));
				setReference(reference); 
			}
		});	
		// passengerInfos
		List<AirPassengerInfo> passengerInfos = new ArrayList<AirPassengerInfo>();
		passengerInfos.add(new AirPassengerInfo() {
			{
				setPassengerType(AirPassengerType.ADT);
				setFirstName("Pham");
				setLastName("Van Tuyen");
				setGender(Gender.MALE);
				setReference("reference");
				setBirthDate(dateFormat.parse("1990-03-03T00:00:00.000Z"));
			}
		});			
		passengerInfos.add(new AirPassengerInfo() {
			{
				setPassengerType(AirPassengerType.ADT);
				setFirstName("Truong");
				setLastName("Viet Cuong");
				setGender(Gender.MALE);
				setReference("reference");
				setBirthDate(dateFormat.parse("1984-03-03T00:00:00.000Z"));
			}
		});	

		passengerInfos.add(new AirPassengerInfo() {
			{
				setPassengerType(AirPassengerType.CHD);
				setFirstName("Vo");
				setLastName("Thi Hang");
				setGender(Gender.FEMALE);
				setReference("reference");
				setBirthDate(dateFormat.parse(createYearBirthday()-4 + "-03-21"));
			}
		});	

		passengerInfos.add(new AirPassengerInfo() {
			{
				setPassengerType(AirPassengerType.INF);
				setFirstName("Vu");
				setLastName("Thi Phuong");
				setGender(Gender.FEMALE);
				setReference("reference");
				setBirthDate(dateFormat.parse(createYearBirthday() + "-07-04"));
			}
		});	

		// agentInfo
		AgentInfo agentInfo = new AgentInfo(){
			{
				setAgentId("AgentId001");
				setUserId("Pham Van Tuyen");
			}
		};

		// contactInfo
		ContactInfo contactInfo = new ContactInfo(){
			{
				setAddress("Tran Van Dang");
				setCity("Ho Chi Minh");
				setEmail("vantuyen.pham@mobivi.com");
				setMobile("0965292512");
			}
		};

		// extraServices
		List<AirExtraService> extraServices = new ArrayList<AirExtraService>();
		AirExtraService extraService = new AirExtraService();
		extraService.setPassengerCode("");
		extraService.setServiceCode("BG15");
		extraService.setTypeFlightCode("");
		extraService.setHaveReturn("0");
		extraServices.add(extraService);
		extraService = new AirExtraService();
		extraService.setPassengerCode("");
		extraService.setServiceCode("BG25");
		extraService.setTypeFlightCode("");
		extraService.setHaveReturn("0");
		extraServices.add(extraService);

		// itinerary
		AirItinerary itinerary = new AirItinerary();
		itinerary.setContactInfo(contactInfo);
		itinerary.setFareInfos(fareInfos);
		itinerary.setPassengerInfos(passengerInfos);
		itinerary.setAgentInfo(agentInfo);
		itinerary.setExtraServices(extraServices);	

		return itinerary;		
	}
}
