package com.mbv.ticketsystem.airline.jetstar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;

import com.mbv.ticketsystem.airline.AirErrorType;
import com.mbv.ticketsystem.airline.vietjet.VietjetBook.VietJetBookException;
import com.mbv.ticketsystem.common.airline.AirExtraService;
import com.mbv.ticketsystem.common.airline.AirFareInfo;
import com.mbv.ticketsystem.common.airline.AirPassengerInfo;
import com.mbv.ticketsystem.common.airline.AirPassengerType;
import com.mbv.ticketsystem.common.airline.AirPassengerTypeQuantity;
import com.mbv.ticketsystem.common.airline.UpdateFarePriceCommand;
import com.mbv.ticketsystem.common.base.AgentInfo;
import com.mbv.ticketsystem.common.base.ContactInfo;
import com.mbv.ticketsystem.common.base.Gender;
import com.mbv.ticketsystem.common.base.OriginDestinationInfo;

@SuppressWarnings({ "serial", "unused" })
public class JetstarBook { 
	
	private DateFormat dateFormat_dd = new SimpleDateFormat("dd");
	private DateFormat dateFormat_ddMMyyyy = new SimpleDateFormat("dd/MM/yyyy");
	private DateFormat dateFormat_yyyyMM = new SimpleDateFormat("yyyy/MM");
	private DateFormat dateddMMyyyy = new SimpleDateFormat("dd/MM/yyyy");
	
	private static final String loginParams =  "ControlGroupNewTradeLoginAgentView$AgentNewTradeLoginView$";
	private static final String searchParams = "ControlGroupTradeSalesHomeView$AvailabilitySearchInputTradeSalesHomeView$";
	private static final String selectParams = "AgentAdditionalBaggagePassengerView$AdditionalBaggageDropDownList";
	private static final String payParams = "ControlGroupAgentPayView$PaymentSectionAgentPayView$UpdatePanelAgentPayView$PaymentInputAgentPayView$";
	private static final String agentContactParams = "AgentControlGroupPassengerView$AgentContactInputViewPassengerView$";
	private static final String agentPassengerParams = "AgentControlGroupPassengerView$AgentPassengerInputViewPassengerView$";

	public Response postProcess(String url, HashMap<String, String> params, String sessId) throws Exception{
		try {
			return Jsoup.connect(url)
					.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0")
					.data(params)
					.cookie("ASP.NET_SessionId", sessId)
					.method(Method.POST)
					.timeout(3000000)
					.execute();
		} catch (Exception ex) {
			throw new Exception("CONNECTION_ERROR");
		}
	}

	public Response getProcessLogin(String url) throws Exception{
		return Jsoup.connect(url)
				.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0")
				.method(Method.GET)
				.timeout(30000)
				.execute();
	}

	public Response getProcess(String url,String sessId) throws Exception{
		return Jsoup.connect(url)
				.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0")
				.cookie("ASP.NET_SessionId", sessId)
				.method(Method.GET)
				.timeout(30000)
				.execute();
	}

	public ArrayList<String > getDocumentJsoupGenerator(Response res){
		ArrayList<String > list = new ArrayList<String>();
		Document doc = Jsoup.parse(res.body());
		String viewstate = doc.select("input[name=viewState]").attr("value");		
		list.add(viewstate);
		String sessId = res.cookies().get("ASP.NET_SessionId");
		list.add(sessId);
		String viewstateEgenerator = doc.select("input[name=__VIEWSTATEGENERATOR]").attr("value");
		list.add(viewstateEgenerator);
		return list;
	}

	public ArrayList<String > getDocumentJsoup(Response res){
		ArrayList<String > list = new ArrayList<String>();
		Document doc = Jsoup.parse(res.body());
		String viewstate = doc.select("input[name=viewState]").attr("value");		
		list.add(viewstate);
		String sessId = res.cookies().get("ASP.NET_SessionId");
		list.add(sessId);
		return list;
	}

	private void convertParams(HashMap<String, String> params,String eventtarget,final String viewstate){	
		params.put("__EVENTTARGET", eventtarget);
		params.put("__EVENTARGUMENT", "");
		params.put("__VIEWSTATE", viewstate);		
		params.put("pageToken", "");
	}
	
	private void convertLoginParams(HashMap<String, String> params,String user,String password,String viewstateEgenerator){
		params.put(loginParams + "TextBoxUserID", user);
		params.put(loginParams + "PasswordFieldPassword", password);	
		params.put(loginParams + "ButtonLogIn", "");
		params.put("__VIEWSTATEGENERATOR", viewstateEgenerator);
	}
	
	public HashMap<String, String> createLoginFormParams(String user,String password,String viewstateEgenerator,final String viewstate){
		HashMap<String, String> params = new HashMap<String, String>();
		String eventtarget = "";
		convertParams(params,eventtarget,viewstate);
		convertLoginParams(params,user,password,viewstateEgenerator);
		return params;
	}

	private void convertSearchParams(HashMap<String, String> params, UpdateFarePriceCommand airSearchRequest,String travelType,int adt,int chd,int inf,OriginDestinationInfo tmpODI){
		
		params.put("total_price", "");
		params.put(searchParams + "RadioButtonSearchBy", "");
		params.put(searchParams + "numberTrips", "1");
		params.put(searchParams + "ButtonSubmit", "");
		
		params.put(searchParams + "RadioButtonMarketStructure", travelType);
		params.put(searchParams + "DropDownListPassengerType_ADT", adt + "");
		params.put(searchParams + "DropDownListPassengerType_CHD", chd + "");
		params.put(searchParams + "DropDownListPassengerType_INFANT", inf + "");
		
		params.put(searchParams + "TextBoxMarketOrigin1", tmpODI.getOriginCode());
		params.put(searchParams + "TextBoxMarketDestination1", tmpODI.getDestinationCode());
		params.put(searchParams + "TextboxDepartureDate1", dateddMMyyyy.format(tmpODI.getArrivalDate()));
		
	}
	
	private void convertSearchRoundTripParams(HashMap<String, String> params,OriginDestinationInfo tmpODI,OriginDestinationInfo tmpODI2){
		params.put(searchParams + "TextboxDestinationDate1", dateddMMyyyy.format(tmpODI2.getArrivalDate()));
		params.put(searchParams + "TextBoxMarketOrigin2", tmpODI2.getOriginCode());
		params.put(searchParams + "TextBoxMarketDestination2", tmpODI2.getDestinationCode());
		params.put(searchParams + "TextboxDepartureDate2", dateddMMyyyy.format(tmpODI2.getArrivalDate()));
		params.put(searchParams + "TextboxDestinationDate2", "");

		params.put(searchParams + "TextBoxMultiCityOrigin1", tmpODI.getOriginCode());
		params.put(searchParams + "TextBoxMultiCityDestination1", tmpODI.getDestinationCode());
		params.put(searchParams + "TextboxDepartureMultiDate1", dateddMMyyyy.format(tmpODI.getArrivalDate()));

		params.put(searchParams + "TextBoxMultiCityOrigin2", tmpODI2.getOriginCode());
		params.put(searchParams + "TextBoxMultiCityDestination2", tmpODI2.getDestinationCode());
		params.put(searchParams + "TextboxDepartureMultiDate2", dateddMMyyyy.format(tmpODI2.getArrivalDate()));
	}
	
	public HashMap<String, String> createSearchFormParams(UpdateFarePriceCommand airSearchRequest,final String searchViewState) throws Exception {
		try {
			String travelType;
			if (airSearchRequest.getOriginDestinationInfos().size() == 1) {
				travelType = "OneWay";
			} else {
				AirFareInfo fare1 = airSearchRequest.getOriginDestinationInfos().get(0);
				AirFareInfo fare2 = airSearchRequest.getOriginDestinationInfos().get(1);
				if (fare1.getOriginCode().equals(fare2.getDestinationCode()) && fare1.getDestinationCode().equals(fare2.getOriginCode())) {
					travelType = "RoundTrip";
				} else {
					travelType = "OpenJaw";
				}
			}
			int adt = airSearchRequest.getPassengerQuantity(AirPassengerType.ADT);
			int chd = airSearchRequest.getPassengerQuantity(AirPassengerType.CHD);
			int inf = airSearchRequest.getPassengerQuantity(AirPassengerType.INF);

			HashMap<String, String> params = new HashMap<String, String>();
			
			String eventtarget = "";
			convertParams(params,eventtarget,searchViewState);
			
			OriginDestinationInfo tmpODI = airSearchRequest.getOriginDestinationInfos().get(0);
			convertSearchParams(params,airSearchRequest,travelType,adt,chd,inf,tmpODI);
			
			if (travelType.equals("RoundTrip") || travelType.equals("OpenJaw")) {
				OriginDestinationInfo tmpODI2 = airSearchRequest.getOriginDestinationInfos().get(1);
				convertSearchRoundTripParams(params,tmpODI,tmpODI2);									
			}
			return params;
		} catch (Exception e) {
			throw new Exception(AirErrorType.INVALID_REQUEST + " CreateSearchForm:" + e.getMessage());
		}
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

	private void convertSelectParams(HashMap<String, String> params,String referenceGo){
		params.put("total_price", "");
		params.put("ControlGroupAgentSelectView$AvailabilityInputAgentSelectView$HiddenFieldTabIndex1", "4");		
		params.put("bannerFileName", "");
				
		params.put("ControlGroupAgentSelectView$AvailabilityInputAgentSelectView$market1",referenceGo);			
		params.put("baggage-selection-toggler", "on");
	}
		
	private String getServiceCode(List<AirExtraService> extraServices,int indexMinSV){	
		String classCode  = "none";					
		if(extraServices.get(indexMinSV).getServiceCode().equals("BG25"))
			classCode = "BG05";
		if(extraServices.get(indexMinSV).getServiceCode().equals("BG30"))
			classCode = "BG10";
		if(extraServices.get(indexMinSV).getServiceCode().equals("BG35"))
			classCode = "BG15";
		if(extraServices.get(indexMinSV).getServiceCode().equals("BG40"))
			classCode = "BG20";
		return classCode;
	}
	
	public HashMap<String, String> createAgentSelectParams(String referenceGo,String referenceBack,List<AirFareInfo> fareInfos,final int customers,final int roundTrip,List<AirExtraService> extraServices,final String viewstate) throws jetStarBookException {
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			
			String eventtarget = "ControlGroupAgentSelectView$ButtonSubmit";
			convertParams(params,eventtarget,viewstate);		
			convertSelectParams(params,referenceGo);
			if(extraServices == null || extraServices.size() == 0)
				params.put(selectParams + "Journey0", "none");
			else
			{		
				AirFareInfo fareInfo = fareInfos.get(0);
				int goBG =  searchService(extraServices).get(0);
				if(goBG == customers){					
					ArrayList<Integer> services = convertStringToInterger(extraServices,goBG,0);
					int indexMinSV  = minValue(services);				
					if(fareInfo.getClassCode().equals("RY")){
						String classCode  = getServiceCode(extraServices,indexMinSV);								
						params.put(selectParams + "Journey0", classCode);
					}						
					else
						params.put(selectParams + "Journey0", extraServices.get(indexMinSV).getServiceCode());
				}
				else{
					params.put(selectParams + "Journey0", "none");
				}				
			}

			// Process Max
			for(int i = 0;i < customers; i++){
				 if(extraServices == null || extraServices.size() == 0)
					params.put(selectParams + "Journey0Pax"+i+"", "none");
				else{
					AirFareInfo fareInfo = fareInfos.get(0);
					int goSVCount =  searchService(extraServices).get(0);
					if(i < goSVCount){
						// if value = max
						if(fareInfo.getClassCode().equals("RY")){		
							String classCode  = getServiceCode(extraServices,i);
							params.put(selectParams + "Journey0Pax"+i+"", classCode);
						}		
						else
							params.put(selectParams + "Journey0Pax"+i+"", extraServices.get(i).getServiceCode());
					}
					else{
						params.put(selectParams + "Journey0Pax"+i+"", "none");
					}
				}				
			}

			params.put("marketstructure", "OneWay");
			
			if(roundTrip == 2){
				params.put("ControlGroupAgentSelectView$AvailabilityInputAgentSelectView$HiddenFieldTabIndex2", "4");
				params.put("ControlGroupAgentSelectView$AvailabilityInputAgentSelectView$market2",referenceBack);
				params.put("marketstructure", "RoundTrip");
				// 		
				if(extraServices == null || extraServices.size() == 0)
					params.put(selectParams + "Journey1", "none");
				else
				{
					AirFareInfo fareInfo = fareInfos.get(1);
					int backBG =  searchService(extraServices).get(1);
					if(backBG == customers){
						ArrayList<Integer> services = convertStringToInterger(extraServices,backBG,1);
						int indexMinSV  = minValue(services);	
						if(fareInfo.getClassCode().equals("RY")){
							String classCode  = getServiceCode(extraServices,indexMinSV);
							params.put(selectParams + "Journey1", classCode);
						}						
						else
							params.put(selectParams + "Journey1", extraServices.get(indexMinSV + backBG).getServiceCode());
					}
					else{
						params.put(selectParams + "Journey1", "none");
					}	
				}

				// Process Max
				int backSVCount = 0;
				int indexSVBack = 0;
				if(extraServices != null){
					backSVCount =  searchService(extraServices).get(0);
					indexSVBack = searchService(extraServices).get(1);
				}
					
				for(int i = 0;i < customers; i++){
					 if(extraServices == null || extraServices.size() == 0)
						params.put(selectParams + "Journey1Pax"+i+"", "none");
					else{
						AirFareInfo fareInfo = fareInfos.get(1);
//						int backSVCount =  searchService(extraServices).get(1);
						if(i+1 <= indexSVBack){
							// if value = max
							if(fareInfo.getClassCode().equals("RY")){
								String classCode  = getServiceCode(extraServices,backSVCount);
								params.put(selectParams + "Journey1Pax"+i+"", classCode);
							}		
							else
								params.put(selectParams + "Journey1Pax"+i+"", extraServices.get(backSVCount).getServiceCode());
							backSVCount++;
						}
						else{
							params.put(selectParams + "Journey1Pax"+i+"", "none");
						}
					}				
				}
			}
			return params;
		} catch (Exception ex) {
			throw new jetStarBookException("INVALID_REQUEST");
		}
	}

	private ArrayList<Integer> convertStringToInterger(List<AirExtraService> extraServices, int temp,int flag){		
		ArrayList<Integer> services =  new ArrayList<Integer>();	
		if(flag == 0){
			for(int i = 0; i< temp; i++){
				String passBG = extraServices.get(i).getServiceCode();
				String[] parts = passBG.split("G");		
				services.add(Integer.parseInt(parts[1]));
			}	
		}
		else{
			for(int i = temp; i < extraServices.size(); i++){
				String passBG = extraServices.get(i).getServiceCode();
				String[] parts = passBG.split("G");		
				services.add(Integer.parseInt(parts[1]));
			}	
		}

		return services;
	}
	
	private void convertPayParams(HashMap<String, String> params,final String totalPrice){
		params.put("total_price", totalPrice);
		params.put(payParams + "PaymentMethodDropDown", "ExternalAccount-HOLD");
		params.put("card_number1", "");
		params.put("card_number2", "");
		params.put("card_number3", "");
		params.put("card_number4", "");
		params.put(payParams + "TextBoxCC__AccountHolderName", "");

		params.put(payParams + "DropDownListEXPDAT_Month", "12");
		params.put(payParams + "DropDownListEXPDAT_Year", "2016");
		params.put(payParams + "TextBoxCC__VerificationCode", "");
		params.put(payParams + "TextBoxACCTNO", "");
		params.put("inlineDCCAjaxSucceeded", "false");
		params.put(payParams + "TextBoxVoucherAccount_VO_ACCTNO", "");
		
		params.put("ControlGroupAgentPayView$AgreementInputAgentPayView$CheckBoxAgreement", "on");
		params.put("summary-amount-total", "NaN");
		params.put("ControlGroupAgentPayView$ButtonSubmit", "");
	}
	
	private int minValue(ArrayList<Integer> services){
		int index = 0;
		int value = services.get(0);
		for(int i = 1;i < services.size();i++){
			if(services.get(i) < value){
				value = services.get(i);
				index = i;
			}
		}
		return index;
	}

	private ArrayList<Integer> searchService(List<AirExtraService> extraServices){
		ArrayList<Integer>  list = new ArrayList<Integer>();
		int go = 0;	
		for(int i=0;i < extraServices.size();i++){			
			//if(extraServices.get(i).getHaveReturn() == false)
			if(extraServices.get(i).getHaveReturn().equals("0"))
				go++;
		}		
		list.add(go);
		list.add(extraServices.size() - go);		
		return list;
	}
	
	private void converAgentPassengerParams(HashMap<String, String> params, int index, String flagGender,String gender,AirPassengerInfo airPassInfo){
		params.put(agentPassengerParams + "DropDownListTitle_"+index+"", gender);
		params.put(agentPassengerParams + "TextBoxFirstName_"+index+"", airPassInfo.getLastName());
		params.put("default-value-firstname-"+index+"", "Tên");
		params.put(agentPassengerParams + "TextBoxLastName_"+index+"", airPassInfo.getFirstName());
		params.put("default-value-lastname-"+index+"", "Họ và tên đệm");
		params.put(agentPassengerParams + "DropDownListBirthDateDay_"+index+"", "");				
		params.put(agentPassengerParams + "DropDownListBirthDateMonth_"+index+"", "");
		params.put(agentPassengerParams + "DropDownListBirthDateYear_"+index+"", "");
		params.put(agentPassengerParams + "DropDownListGender_"+index+"", flagGender); 
		params.put(agentPassengerParams + "TextBoxProgramNumber_"+index+"", "");
		params.put(agentPassengerParams + "DropDownListProgram_"+index+"", "QF");	
	}
	
	private List<String> createGender(AirPassengerInfo airPassInfo,int index,int adt){
		List<String> list = new ArrayList<String>();
		String gender = "MR";
		String flagGender = "1";				
		if(index <= adt){
			if(airPassInfo.getGender().name().equals("FEMALE")){
				gender = "MRS";
				flagGender = "2";
			}
		}
		else{
			if(airPassInfo.getGender().name().equals("MALE")){						
				gender = "MSTR"; 						
			}
			else{					
				gender = "MISS";
				flagGender = "2";
			}
		}	
		list.add(flagGender);
		list.add(gender);	
		return list;
	}
	
	public HashMap<String, String> createAgentPassengerParams(UpdateFarePriceCommand airSearchRequest, int roundTrip,AgentInfo agentInfo,ContactInfo contactInfo,List<AirPassengerInfo> passengerInfos,String totalPrice,final String viewstate) throws jetStarBookException {
		try {
			HashMap<String, String> params = new HashMap<String, String>();		
			String eventtarget = "AgentControlGroupPassengerView$ButtonSubmit";
			convertParams(params,eventtarget,viewstate);		
			convertContactParams(params,totalPrice,agentInfo,contactInfo);		
			// review
			int adt = airSearchRequest.getPassengerQuantity(AirPassengerType.ADT);
			int chd = airSearchRequest.getPassengerQuantity(AirPassengerType.CHD);
			int inf = airSearchRequest.getPassengerQuantity(AirPassengerType.INF);		
			// if inf == 0
			if(inf == 0){
				for(int i = 1; i <= passengerInfos.size(); i++){
					AirPassengerInfo airPassInfo =  passengerInfos.get(i-1);						
					List<String> listGender = createGender(airPassInfo,i,adt);						
					converAgentPassengerParams(params,i,listGender.get(0),listGender.get(1),airPassInfo);								
				}
			}
			else{
				int indexInf = 2;		
				int tempPass = 1;
				int countInf = 0;
				for(int i = 1; i <= (passengerInfos.size() - inf); i++){ // 2
					AirPassengerInfo airPassInfo =  passengerInfos.get(i-1);	// 0
					List<String> listGender = createGender(airPassInfo,i,adt);					
					if(countInf <= inf && countInf != 0){	// if(countInf <= inf && countInf != 0){					
						tempPass += 2;
					}									
					if(countInf > inf)
						tempPass++;
					converAgentPassengerParams(params,tempPass,listGender.get(0),listGender.get(1),airPassInfo);					
					countInf++;
				}
				// inf
				indexInf = 2;				
				int k = adt + chd;
				while(k < passengerInfos.size()){
					AirPassengerInfo airPassInfo =  passengerInfos.get(k);					
					String flagGender = "1";
					if(airPassInfo.getGender().name().equals("FEMALE"))
						flagGender = "2";
					int tempName = k+1;
					convertPassengerInf(params,indexInf,tempName,airPassInfo,flagGender);									
					indexInf +=2;					
					k++;
				}			
			}	
			// 
			for(int i =1 ;i<=passengerInfos.size();i++){
				params.put("default-value-firstname-"+i+"", "Tên");
				params.put("default-value-lastname-"+i+"", "Họ và tên đệm");
			}
			int adt_chd = adt+chd;
			convertMapSeatPassenger(params,adt_chd,roundTrip);		
			return params;
		} catch (Exception ex) {
			throw new jetStarBookException("INVALID_REQUEST");
		}
	}	
	
	private void convertMapSeatPassenger(HashMap<String, String> params,int adt_chd,int roundTrip){
		params.put("AgentControlGroupPassengerView$AgentItineraryDistributionInputPassengerView$Distribution", "2");
		for(int i = 0;i< adt_chd; i++){
			params.put("AgentControlGroupPassengerView$AgentUnitMapSeatsView$HiddenEquipmentConfiguration_0_PassengerNumber_"+i+"", "");
			if(roundTrip == 2) 
				params.put("AgentControlGroupPassengerView$AgentUnitMapSeatsView$HiddenEquipmentConfiguration_1_PassengerNumber_"+i+"", "");			
		}
	}
	
	private void convertPassengerInf(HashMap<String, String> params,int indexInf,int tempName,AirPassengerInfo airPassInfo,String flagGender){
		params.put(agentPassengerParams + "TextBoxFirstName_"+indexInf+"", airPassInfo.getLastName());
//		params.put("default-value-firstname-"+tempName+"", "Tên");
		params.put(agentPassengerParams + "TextBoxLastName_"+indexInf+"", airPassInfo.getFirstName());
		
//		params.put("default-value-lastname-"+tempName+"", "Họ và tên đệm");
		params.put(agentPassengerParams + "DropDownListAssign_"+indexInf+"", Integer.toString(indexInf-1)); 
		
		String birthDate = dateddMMyyyy.format(airPassInfo.getBirthDate());				
		String [] birthTemp = birthDate.split("/");									
		params.put(agentPassengerParams + "DropDownListBirthDateDay_"+indexInf+"", birthTemp[0]);				
		params.put(agentPassengerParams + "DropDownListBirthDateMonth_"+indexInf+"", birthTemp[1]);
		params.put(agentPassengerParams + "DropDownListBirthDateYear_"+indexInf+"", birthTemp[2]);				
		params.put(agentPassengerParams + "DropDownListGender_"+indexInf+"", flagGender);	
	}
	
	private void convertContactParams(HashMap<String, String> params,String totalPrice,AgentInfo agentInfo,ContactInfo contactInfo){
		params.put("total_price", totalPrice);
		params.put(agentContactParams + "DropDownListTitle", "MR");
		params.put(agentContactParams + "TextBoxFirstName", agentInfo.getAgentId());
		params.put(agentContactParams + "TextBoxLastName", agentInfo.getUserId());
		params.put(agentContactParams + "DropDownListWorkPhoneCountryCode", "VN");
		params.put(agentContactParams + "TextBoxWorkPhone", contactInfo.getMobile());	
		
		params.put(agentContactParams + "TextBoxJahEmailAddress", "");
		params.put(agentContactParams + "TextBoxEmailAddress", contactInfo.getEmail());
		params.put(agentContactParams + "TextBoxPostalCode", "084");
		params.put(agentContactParams + "TextBoxCity", "4"); 
		params.put(agentContactParams + "TextBoxAddressLine1", "Suite 3.1, H3 Tower, 384 Hoang Dieu, Ward 6, Distric"); // default
		
		params.put(agentContactParams + "TextBoxAddressLine2", "");
		params.put(agentContactParams + "TextBoxCompanyName", "Viet Fast Services JST Company");
		params.put(agentContactParams + "DropDownListCountry", "VN");
		params.put(agentContactParams + "DropDownListCountryCode", "VN");
	}

	public HashMap<String, String> createAgentPayParams(final String totalPrice,final String viewstate) throws jetStarBookException {
		try {
			HashMap<String, String> params = new HashMap<String, String>();			
			String eventtarget = "";
			convertParams(params,eventtarget,viewstate);
			convertPayParams(params,totalPrice);			
			return params;
		} catch (Exception ex) {
			throw new jetStarBookException("INVALID_REQUEST");
		}
	}
	
	public HashMap<String, String> createAgentWaitParams(final String viewstate) throws jetStarBookException {
		try {
			HashMap<String, String> params = new HashMap<String, String>() {
				{
					put("__EVENTTARGET", "");
					put("__EVENTARGUMENT", "");
					put("__VIEWSTATE", viewstate);		
				}
			};		
			return params;
		} catch (Exception ex) {
			throw new jetStarBookException("INVALID_REQUEST");
		}
	}

	public class jetStarBookException extends Exception {

		private static final long serialVersionUID = 1L;

		public jetStarBookException(String message) {
			super(message);
		}
	}
}
