package airline;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mbv.ticketsystem.airline.jetstar.JetstarAccount;
import com.mbv.ticketsystem.airline.jetstar.JetstarStub;
import com.mbv.ticketsystem.common.airline.AirFareInfo;
import com.mbv.ticketsystem.common.airline.AirFarePriceInfos;
import com.mbv.ticketsystem.common.airline.AirPassengerType;
import com.mbv.ticketsystem.common.airline.AirPassengerTypeQuantity;
import com.mbv.ticketsystem.common.airline.UpdateFarePriceCommand;

public class demo {
	private static final Logger logger = LoggerFactory.getLogger(demo.class);
	public static void main(String[] args) throws Exception {
//		final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		AirFarePriceInfos result = vietJetSearch();
		if(result != null){
			logger.info("1");
		}			
		else
			logger.info("0");

	}
	
	public static AirFarePriceInfos vietJetSearch(){
		AirFareInfo fareInfo = new AirFareInfo();
		fareInfo.setVendor("BL");
		fareInfo.setOriginCode("SGN");
		fareInfo.setDestinationCode("HAN");
		fareInfo.setDepartureDate(new Date());
		fareInfo.setArrivalDate(new Date());

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
		
		JetstarAccount jetstarAccount = new JetstarAccount("MBVtest1","P@ssword123");
		JetstarStub jetstar = new JetstarStub(jetstarAccount);		
		AirFarePriceInfos result = null;
        for (int retry = 0; retry <= 1 && result == null; retry++) {
            try {
                result = jetstar.search(command);
            } catch (Exception ex) {
            	System.out.println(ex.getMessage());
            }
        }      
        return result;
	}

}
