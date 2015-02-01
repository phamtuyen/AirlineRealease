package com.mbv.ticketsystem.airline.vietjet;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mbv.ticketsystem.common.airline.AirPassengerInfo;
import com.mbv.ticketsystem.common.airline.AirPassengerType;
import com.mbv.ticketsystem.common.base.Gender;

public class VietjetSearchInfo {
	public List<AirPassengerInfo> getPassengerInfo(Elements elements){
		List<AirPassengerInfo> passengerInfos = new ArrayList<AirPassengerInfo>();
		for(Element element : elements){
			AirPassengerInfo passengerInfo = new AirPassengerInfo();
			String []fullName = element.select("td").get(1).text().split(",");
			passengerInfo.setFirstName(fullName[0]);
			passengerInfo.setLastName(fullName[1]);
			if(element.select("td").get(2).text().equals("M") || (element.select("td").get(2).text().equals("F"))){
				passengerInfo.setGender(Gender.MALE);
				passengerInfo.setPassengerType(AirPassengerType.ADT);
				if((element.select("td").get(2).text().equals("F")))
					passengerInfo.setGender(Gender.FEMALE);
			}				
			if(element.select("td").get(2).text().equals("C"))
				passengerInfo.setPassengerType(AirPassengerType.CHD);
			passengerInfos.add(passengerInfo);	
		}
		return passengerInfos;
	}
	
	public int countInf(Elements elements){
		int count = 0;
		for(Element element : elements){
			if(element.select("td").get(3).text().contains("1"))
				count++;
		}
		return count;
	}
	
	public long addAmount(Elements elements){
		long amount = 0;
		for(Element element : elements)
			amount +=Long.valueOf(element.select("td").get(4).text().replace(",", "")).longValue();
		return amount;
	}
	
	public List<AirPassengerInfo> addPassengerInfo(List<AirPassengerInfo> passengerInfosA,List<AirPassengerInfo> passengerInfosB){
		for(AirPassengerInfo passengerInfo : passengerInfosB)
			passengerInfosA.add(passengerInfo);
		return passengerInfosA;
	}
}



