package com.mbv.ticketsystem.airline.jetstar;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.mbv.ticketsystem.common.airline.AirExtraService;
import com.mbv.ticketsystem.common.airline.AirFareInfo;
import com.mbv.ticketsystem.common.airline.AirItinerary;
import com.mbv.ticketsystem.common.airline.AirPassengerInfo;
import com.mbv.ticketsystem.common.airline.AirPassengerType;
import com.mbv.ticketsystem.common.airline.AirTicketingStatus;

public class JetstarSearchInfo {
	final static Logger logger = Logger.getLogger(JetstarSearchInfo.class);
	public AirItinerary swapItinerary(AirItinerary itineraryOld,AirItinerary itineraryNew ){	
		itineraryOld.getTicketingInfo().setAmount(itineraryNew.getTicketingInfo().getAmount());
		itineraryOld.getTicketingInfo().setStatus(AirTicketingStatus.BOOK_SUCCESS);
		itineraryOld.getTicketingInfo().setCreatedDate(new Date());
		itineraryOld.getTicketingInfo().setUpdatedDate(itineraryNew.getTicketingInfo().getUpdatedDate());		
		itineraryOld.setFareInfos(itineraryNew.getFareInfos());
		itineraryOld.getFareInfos().get(0).setVendor("BL");
		if(itineraryNew.getFareInfos().size() == 2)
			itineraryOld.getFareInfos().get(1).setVendor("BL");
		itineraryOld.setPassengerInfos(itineraryNew.getPassengerInfos());
		itineraryOld.setExtraServices(itineraryNew.getExtraServices());
		itineraryOld.setContactInfo(itineraryNew.getContactInfo());
		return itineraryOld;
	}
	
	@SuppressWarnings({ "rawtypes"})
	public List<AirExtraService> getExtraService(JSONArray jsonArray,String haveReturn){
		List<AirExtraService> extraServices = new ArrayList<AirExtraService>();	
		Iterator index = jsonArray.iterator();
		while (index.hasNext()) {		
			JSONObject slide = (JSONObject) index.next();
			String flightCode = ((String)slide.get("flightCode")).replaceAll("\\s","");
			String bags = ((String) slide.get("bags"));
			if(!bags.equals("")){		
				String luggages = ((String) slide.get("bags")).split(":")[1].replaceAll("\\s","");				
				String []partLuggage = luggages.split(",");
				for(int i=0;i<partLuggage.length;i++){
					AirExtraService extraService = new AirExtraService();
					extraService.setServiceCode(getInfoFlight(partLuggage[i],flightLuggage));
					extraService.setHaveReturn(haveReturn);
					extraService.setTypeFlightCode(flightCode);	
					extraServices.add(extraService);
				}
			}					
		}
		return extraServices;
	}
	
	public int countCHD(JSONObject objJson){	
		String chd = "";
		try {
			JSONObject paxCount = (JSONObject) objJson.get("paxCount");		
			chd = paxCount.get("CHD").toString();
		} catch (Exception ex) {
			logger.error("Retry bookID: " + ex.getMessage()); 
		}
		if(chd.equals(""))
			return 0;
		return Integer.parseInt(chd);
	}
	
	public List<AirExtraService> addExtraService(List<AirExtraService> serviceDepart,List<AirExtraService> serviceReturn){		
		for(int i=0;i<serviceReturn.size();i++)
			serviceDepart.add(serviceReturn.get(i));
		return serviceDepart;
	}
	
	public List<AirPassengerInfo> setPassengerCHD(List<AirPassengerInfo> passengerInfos,int chd){
		for(int i = passengerInfos.size() -1 ;i > 0 && chd > 0;i--){
			passengerInfos.get(i).setPassengerType(AirPassengerType.CHD);
			chd--;
		}
		swapCHD(passengerInfos,"INF","ADT",0);
		return swapCHD(passengerInfos,"INF","CHD",1);
	}
	
	public List<AirPassengerInfo> swapCHD (List<AirPassengerInfo> passengerInfos,String typeA,String typeB,int flag){
		int begin = 0;
		int end = passengerInfos.size() -1;
		while(begin < end){
			AirPassengerInfo passengerInfoADT = passengerInfos.get(begin);
			AirPassengerInfo passengerInfoINF = passengerInfos.get(end);
			if(passengerInfoADT.getPassengerType().toString().equals(typeA) && passengerInfoINF.getPassengerType().toString().equals(typeB)){
				passengerInfos.set(begin, passengerInfoINF);
				passengerInfos.set(end, passengerInfoADT);
				begin++;
				if(flag == 1)
					end--;
			}
			if(passengerInfoADT.getPassengerType().toString().equals("ADT"))
				begin++;
			if(flag == 0)
				end--;
		}
		return 	passengerInfos;
	}
	
	@SuppressWarnings("rawtypes")
	public AirFareInfo getFareInfo(JSONArray jsonArray) throws ParseException{
		 AirFareInfo fareInfo = new AirFareInfo();
		 DateFormat dateFormatFull = new SimpleDateFormat("HH:mm'T'dd/MM/yyyy", Locale.US);
		 Iterator index = jsonArray.iterator();
			while (index.hasNext()) {								
				JSONObject slide = (JSONObject) index.next();
				String flightCode = ((String)slide.get("flightCode")).replaceAll("\\s","");
				fareInfo.setFlightCode(flightCode);									
				String deptTime = (String) slide.get("deptTime");
				fareInfo.setDepartureDate(dateFormatFull.parse(deptTime.replaceAll(",", "T").replaceAll("\\s","")));
				String arrvTime = (String) slide.get("arrvTime");
				fareInfo.setArrivalDate(dateFormatFull.parse(arrvTime.replaceAll(",", "T").replaceAll("\\s","")));		
				fareInfo.setDestinationCode(getInfoFlight((String) slide.get("arrvAirport"),flightLocation));
				fareInfo.setOriginCode(getInfoFlight((String) slide.get("deptAirport"),flightLocation));
			}
		 return fareInfo;
	}
	
	public String getLastName(String fullName){
		String []part = fullName.split(" ");
		return part[1];
	}
	
	public String getFirstName(String fullName){
		String []part = fullName.split(" ");
		String firstName = "";
		for(int i=2;i<part.length;i++){
			if(i < part.length-1)
			 firstName +=part[i] + " ";
			else
				firstName +=part[i];
		}
		return firstName;
	}
	
	@SuppressWarnings("serial")
	public HashMap<String, String> flightLuggage = new HashMap<String, String>() {
		{
			put("15kg", "BG15");
			put("20kg", "BG20");
			put("25kg", "BG25");
			put("30kg", "BG30");
			put("35kg", "BG35");
			put("40kg", "BG40");
		}
	};		
	@SuppressWarnings("serial")
	public HashMap<String, String> flightLocation = new HashMap<String, String>() {
		{		
			put("Buôn Ma Thuột", "BMV");
			put("Đà Nẵng", "DAD");
			put("Đồng Hới", "VDH");
			put("Hà Nội", "HAN");
			put("Hải Phòng", "HPH");
			put("Huế", "HUI");
			put("Nha Trang", "CXR");
			put("Phú Quốc", "PQC");
			put("Quy Nhơn", "UIH");
			put("Thanh Hóa", "THD");
			put("Thành phố Hồ Chí Minh", "SGN");
			put("Tuy Hoa", "TBB");
			put("Vinh", "VII");						
		}
	};
	
	public  String getInfoFlight(String keySearch,HashMap<String, String> flightInfo){
		String value = "";
		Iterator<String> keySetIterator = flightInfo.keySet().iterator();
		while(keySetIterator.hasNext()){
			String key = keySetIterator.next();
			if(key.contains(keySearch))
				return flightInfo.get(key);		
		}
		return value;
	}
}
