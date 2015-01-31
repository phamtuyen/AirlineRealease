package com.mbv.ticketsystem.airline.vietjet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.mbv.ticketsystem.common.airline.AirExtraService;
import com.mbv.ticketsystem.common.airline.AirFareInfo;
import com.mbv.ticketsystem.common.airline.AirPassengerInfo;
import com.mbv.ticketsystem.common.airline.AirPassengerType;
import com.mbv.ticketsystem.common.airline.AirPassengerTypeQuantity;
import com.mbv.ticketsystem.common.airline.UpdateFarePriceCommand;
import com.mbv.ticketsystem.common.base.ContactInfo;


//@SuppressWarnings({ "serial", "unused" })
public class VietjetBook {
	private static DateFormat dateFormat_dd = new SimpleDateFormat("dd");
	private static DateFormat dateFormat_ddMMyyyy = new SimpleDateFormat("dd/MM/yyyy");
	private static DateFormat dateFormat_yyyyMM = new SimpleDateFormat("yyyy/MM");

	public Response postProcess(String url, HashMap<String, String> params, String sessId) throws Exception{
		try {
			return Jsoup.connect(url)
					.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0")
					.data(params)
					.cookie("ASP.NET_SessionId", sessId)
					.method(Method.POST)
					.timeout(300000)
					.execute();
		} catch (Exception ex) {
			throw new Exception("CONNECTION_ERROR");
		}
	}

	public Response getProcessLogin(String url) throws Exception{
		return Jsoup.connect(url)
				.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0")
				.method(Method.GET)
				.timeout(300000)
				.execute();
	}

	public Response getProcess(String url,String sessId) throws Exception{
		return Jsoup.connect(url)
				.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0")
				.cookie("ASP.NET_SessionId", sessId)
				.method(Method.GET)
				.timeout(300000)
				.execute();
	}

	public ArrayList<String > getDocumentJsoup(Response res){
		ArrayList<String > list = new ArrayList<String>();
		Document doc = Jsoup.parse(res.body());
		String viewstate = doc.select("input[name=__VIEWSTATE]").attr("value");
		list.add(viewstate);
		String sessId = res.cookies().get("ASP.NET_SessionId");
		list.add(sessId);
		return list;
	}

	private void createParams(HashMap<String, String> params,String button,final String viewstate){
		params.put("__VIEWSTATE", viewstate);
		params.put("button", button);
		params.put("DebugID", "22");
		params.put("SesID", "");
	}

	public HashMap<String, String> createPostMenuParams(final String viewstate) throws Exception {
		HashMap<String, String> params = new HashMap<String, String>();
		String button = "bookflight";
		createParams(params,button,viewstate);
		return params;
	}

	private void createSearchParams(HashMap<String, String> params,UpdateFarePriceCommand request,AirFareInfo fare1){
		params.put("lstDepDateRange", "0");
		params.put("lstRetDateRange", "0");
		params.put("departTime1", "0000");
		params.put("departTime2", "0000");
		params.put("lstLvlService", "1");
		params.put("lstResCurrency", "VND");

		params.put("txtNumAdults", request.getPassengerQuantity(AirPassengerType.ADT) + "");
		params.put("txtNumChildren", request.getPassengerQuantity(AirPassengerType.CHD) + "");
		params.put("txtNumInfants", request.getPassengerQuantity(AirPassengerType.INF) + "");

		params.put("lstOrigAP", fare1.getOriginCode());
		params.put("lstDestAP", fare1.getDestinationCode());
		params.put("dlstDepDate_Day", dateFormat_dd.format(fare1.getArrivalDate()));
		params.put("dlstDepDate_Month", dateFormat_yyyyMM.format(fare1.getArrivalDate()));
		params.put("departure1", dateFormat_ddMMyyyy.format(fare1.getArrivalDate()));
	}
	
	
	

	private void createSearchRoundTripParams(HashMap<String, String> params,AirFareInfo fare2){
		params.put("chkRoundTrip", "on");
		params.put("dlstRetDate_Day", dateFormat_dd.format(fare2.getArrivalDate()));
		params.put("dlstRetDate_Month", dateFormat_yyyyMM.format(fare2.getArrivalDate()));
		params.put("departure2", dateFormat_ddMMyyyy.format(fare2.getArrivalDate()));
	}

	public HashMap<String, String> createSearchParams(UpdateFarePriceCommand request,final String viewstate) throws  VietJetBookException {
		try {			
			HashMap<String, String> params = new HashMap<String, String>();		
			String button = "vfto";
			createParams(params,button,viewstate);
			// place
			AirFareInfo fare1 = request.getOriginDestinationInfos().get(0);
			createSearchParams(params,request,fare1);
			// RoundTrip
			if (request.getOriginDestinationInfos().size() == 2) {
				AirFareInfo fare2 = request.getOriginDestinationInfos().get(1);
				if (fare1.getOriginCode().equals(fare2.getDestinationCode()) && fare1.getDestinationCode().equals(fare2.getOriginCode())) {
					createSearchRoundTripParams(params,fare2);
				} else {
					throw new Exception("INVALID_REQUEST");
				}
			}			
			return params;
		} catch (Exception ex) {
			throw new VietJetBookException("INVALID_REQUEST");
		}
	}

	public HashMap<String, String> createPostTravelParams(final String reference1,final String reference2,final String viewstate) throws  VietJetBookException {
		try {			
			HashMap<String, String> params = new HashMap<String, String>();
			String button = "continue";
			createParams(params,button,viewstate);		
			params.put("PN", "");
			params.put("gridTravelOptDep",reference1);
			if(reference2 != "")
				params.put("gridTravelOptRet",reference2);
			return params;
		} catch (Exception ex) {
			throw new VietJetBookException("INVALID_REQUEST");
		}
	}

	private void infoFormDetail(HashMap<String, String> params,AirPassengerInfo airPassengerInfo,int adt){
		params.put("txtPax"+adt+"_LName", airPassengerInfo.getLastName());
		params.put("txtPax"+adt+"_FName", airPassengerInfo.getFirstName());
		params.put("txtPax"+adt+"_Phone1","");
		params.put("txtPax"+adt+"_DOB_Day", "");
		params.put("txtPax"+adt+"_DOB_Month", "");
		params.put("txtPax"+adt+"_DOB_Year", "");

		params.put("txtPax"+adt+"_Passport", "");
		params.put("dlstPax"+adt+"_PassportExpiry_Day", "");
		params.put("dlstPax"+adt+"_PassportExpiry_Month", "");
		params.put("lstPax"+adt+"_PassportCtry", "");
		params.put("txtPax"+adt+"_Nationality","");
		params.put("hidPax"+adt+"_Search", "-1");
	}

	private void convertDetailParams(HashMap<String, String> params,AirPassengerInfo airPassengerInfo,int adt,int curINF,int countINF,ContactInfo contactInfo){
		String gender = airPassengerInfo.getGender().toString(); // MALE, FEMALE
		if(gender.equals("MALE"))
			gender = "M";
		else
			gender = "F";						
		params.put("txtPax"+adt+"_Gender", gender);	
		if(curINF != 0 && curINF >=  countINF)
		{
			params.put("txtPax"+adt+"_Infant_DOB_Day", "");
			params.put("txtPax"+adt+"_Infant_DOB_Month", "");
			params.put("txtPax"+adt+"_Infant_DOB_Year", "");
		}	
		infoFormDetail(params,airPassengerInfo,adt);
		
		if(adt == 1){
			params.put("txtPax1_Addr1", contactInfo.getAddress());
			params.put("txtPax1_City", contactInfo.getCity());		
			params.put("txtPax1_Ctry", "234");
			params.put("txtPax1_Prov","-1");
			params.put("txtPax1_EMail", contactInfo.getEmail());			
			params.put("txtPax1_Phone2", contactInfo.getMobile());
		}
	}

	public HashMap<String, String> createPostDetailParams(List<AirPassengerInfo> passengerInfos,ContactInfo contactInfo,final String viewstate) throws  VietJetBookException {
		try {
			HashMap<String, String> params = new HashMap<String, String>();		
			String button = "continue";
			createParams(params,button,viewstate);
			ArrayList<AirPassengerTypeQuantity> list = getAirPassengerTypeQuantity(passengerInfos);
			int curADT = list.get(0).getQuantity();
			int curINF = list.get(2).getQuantity();
			int countINF = 0;
			AirPassengerInfo airPassengerInfo = passengerInfos.get(0);
			if(airPassengerInfo.getPassengerType().toString().equals("ADT")){	
				int adt = 1;
				countINF++; // count = 1
				convertDetailParams(params,airPassengerInfo,adt,curINF,countINF,contactInfo);
			}
			if(passengerInfos.size() >= 2){
				// ADT, INF
				int adt = 2;
				for(int i = 1;i < passengerInfos.size();i++){
					airPassengerInfo = passengerInfos.get(i);
					
					if(airPassengerInfo.getPassengerType().toString().equals("ADT")){
						countINF++; 
						convertDetailParams(params,airPassengerInfo,adt,curINF,countINF,contactInfo);
						adt++;					
					}
				}
				// CHD
				curADT++;
				for(int i = 1;i < passengerInfos.size();i++){
					airPassengerInfo = passengerInfos.get(i);
					if(airPassengerInfo.getPassengerType().toString().equals("CHD")){
						params.put("txtPax"+curADT+"_Gender","C");					
						infoFormDetail(params,airPassengerInfo,curADT);
						curADT++;
					}
				}
			}
			params.put("txtResNotes", "");
			return params;
		} catch (Exception ex) {
			throw new VietJetBookException("INVALID_REQUEST");
		}
	}

	
	private int findIndexPass(String pasKg){
		int indexPass = 2;
		if(pasKg.equals("BG20"))
			indexPass = 3;
		if(pasKg.equals("BG25"))
			indexPass = 4;
		if(pasKg.equals("BG30"))
			indexPass = 5;
		if(pasKg.equals("BG35"))
			indexPass = 52;
		if(pasKg.equals("BG40"))
			indexPass = 53;
		return indexPass;
	}
	
	
	
	private String splitItemPass(String hidPaxItem){
		String hidPaxItemTemp = hidPaxItem.replace("|", "-");
		String[] parts = hidPaxItemTemp.split("-");
		String numberPas = parts[3];
		return numberPas;
	}
	
	private void createMealParams(HashMap<String, String> params, int index){
		params.put("shpsel", "");
		params.put("hidPaxItem:-"+index+":17:1", "1ƒNAƒ17ƒ444233ƒ40000ƒFalseƒ3ƒBun XaoƒVJ151 - Ha NoiƒNAƒ444233ƒ1ƒBun Xao Singaporeƒ40,000ƒ40000ƒ4000.00ƒ0ƒ0");						
		params.put("hidPaxItem:-"+index+":24:1", "1ƒNAƒ24ƒ361560ƒ40000ƒFalseƒ3ƒMi YƒVJ151 - Ha NoiƒNAƒ361560ƒ1ƒMi Yƒ40,000ƒ40000ƒ4000.00ƒ0ƒ0");						
		params.put("hidPaxItem:-"+index+":51:1", "1ƒNAƒ51ƒ701848ƒ55000ƒFalseƒ3ƒCombo My YƒVJ151 - Ha NoiƒNAƒ701848ƒ1ƒCombo My Yƒ55,000ƒ55000ƒ5500.00ƒ0ƒ0");	
		params.put("hidPaxItem:-"+index+":64:1", "1ƒNAƒ64ƒ701667ƒ55000ƒFalseƒ3ƒCombo Bun XaoƒVJ151 - Ha NoiƒNAƒ701667ƒ1ƒCombo Bun Xaoƒ55,000ƒ55000ƒ5500.00ƒ0ƒ0");
	}
	
	private void convertAddOnsParams(HashMap<String, String> params,final int RoundTrip, final int ADT_CHD){
		params.put("m1th", "2");
		params.put("m1p", "1");
		params.put("ctrSeatAssM", ""+RoundTrip);
		params.put("ctrSeatAssP", ""+ADT_CHD);
	}
		
	public HashMap<String, String> createPostAddOnsParams(final int RoundTrip, final int ADT_CHD,ArrayList<AirExtraService> extraServices,List<String> listAttrPasKg,List<String> listAttrPasKgBack,final int glagMeal,final String viewstate) throws  VietJetBookException {
		try {
			HashMap<String, String> params = new HashMap<String, String>();			
			String button = "continue";
			createParams(params,button,viewstate);
			convertAddOnsParams(params,RoundTrip,ADT_CHD);
			if(RoundTrip == 1){
				int what = 8;
				for(int i = 1; i <= ADT_CHD; i++){
					params.put("m1p" + i + "", "");
					params.put("m1p" + i + "rpg", "");
					if(extraServices == null || extraServices.size() == 0)
					{
						params.put("lstPaxItem:-"+i+":1:"+what+"", "-1");
						params.put("-1", "-1");
					}
					else
					{
						if(i <= extraServices.size()){
							String pasKg = extraServices.get(i-1).getServiceCode();
							int indexPass = findIndexPass(pasKg);
							String hidPaxItem = findPassKG(pasKg,listAttrPasKg);  	
							String numberPas = splitItemPass(hidPaxItem);
							
							params.put("lstPaxItem:-"+i+":1:"+what+"", ""+indexPass+":-"+i+":"+numberPas+":1");
							params.put("hidPaxItem:-"+i+":"+indexPass+":1", hidPaxItem);
						}
						else{
							params.put("lstPaxItem:-"+i+":1:"+what+"", "-1");
							params.put("-1", "-1");
						}                	
					}   

					if(glagMeal == 1){					
						createMealParams(params,i);
					}
					what += 14;
				}
			}			
			if(RoundTrip == 2){

				params.put("m2th", "2");
				params.put("m2p", "1");

				int what = 8;
				for(int i = 1; i <= ADT_CHD; i++){
					// one way
					params.put("m1p" + i + "", "");
					params.put("m1p" + i + "rpg", "");
					// return
					params.put("m2p" + i + "", "");
					params.put("m2p" + i + "rpg", "");

					if(extraServices == null || extraServices.size() == 0)
					{						
						params.put("lstPaxItem:-"+i+":1:"+what+"", "-1");
						params.put("-1", "-1");										
					}
					else
					{						
						// int count GO
						List<Integer> list = searchService(extraServices);
						int go = list.get(0);
						if(i <= go){
							String pasKg = extraServices.get(i-1).getServiceCode();  // point get getPassengerRefenerence();							
							int indexPass = findIndexPass(pasKg);
							String hidPaxItem = findPassKG(pasKg,listAttrPasKg);  	
							String numberPas = splitItemPass(hidPaxItem);
							// One way							
							params.put("lstPaxItem:-"+i+":1:"+what+"", ""+indexPass+":-"+i+":"+numberPas+":1");						
							params.put("hidPaxItem:-"+i+":"+indexPass+":1", hidPaxItem);

						}
						else{
							params.put("lstPaxItem:-"+i+":1:"+what+"", "-1");
							params.put("-1", "-1");
						} 
					}

					if(glagMeal == 1){
						// one way
						createMealParams(params,i);
					}					
					what += 28;
				}
				// BACK
				what = 22;
				for(int i = 1; i <= ADT_CHD; i++){					
					if(extraServices == null || extraServices.size() == 0)
					{										
						params.put("lstPaxItem:-"+i+":2:"+what+"", "-1");
						params.put("-1", "-1");
					}
					else
					{						
						// int count GO
						List<Integer> list = searchService(extraServices);
						int go = list.get(0);
						int back = list.get(1);
						if(i <= back){
							String pasKg = extraServices.get(go).getServiceCode();
							go++;						
							int indexPass = findIndexPass(pasKg);
							String hidPaxItem = findPassKG(pasKg,listAttrPasKgBack);  	
							String numberPas = splitItemPass(hidPaxItem);
							// return
							params.put("lstPaxItem:-"+i+":2:"+what+"", ""+indexPass+":-"+i+":"+numberPas+":2");						
							params.put("hidPaxItem:-"+i+":"+indexPass+":2", hidPaxItem);
						}
						else{
							params.put("lstPaxItem:-"+i+":2:"+what+"", "-1");
							params.put("-1", "-1");
						} 
					}
					if(glagMeal == 1){
						// return
						createMealRoundTripParams(params,i);
					}
					what += 28;
				}			
			}
			return params;
		} catch (Exception ex) {
			throw new VietJetBookException("INVALID_REQUEST");
		}
	}

	private String findPassKG(String pasKg,List<String> listAttrPasKg){
		String hidPaxItem = listAttrPasKg.get(0);  	
		if(pasKg.equals("BG20"))		
			hidPaxItem = listAttrPasKg.get(1);
		if(pasKg.equals("BG25"))		
			hidPaxItem = listAttrPasKg.get(2);
		if(pasKg.equals("BG30"))		
			hidPaxItem = listAttrPasKg.get(3);
		if(pasKg.equals("BG35"))		
			hidPaxItem = listAttrPasKg.get(4);
		if(pasKg.equals("BG40"))		
			hidPaxItem = listAttrPasKg.get(5);
		return hidPaxItem;
	}
	
	private void createMealRoundTripParams(HashMap<String, String> params, int index){
		params.put("shpsel", "");                
		params.put("hidPaxItem:-"+index+":17:2", "2ƒNAƒ17ƒ768585ƒ40000ƒFalseƒ2ƒBun XaoƒVJ198 - Ho Chi MinhƒNAƒ768585ƒ1ƒBun Xao Singaporeƒ40,000ƒ40000ƒ4000.00ƒ0ƒ0");						
		params.put("hidPaxItem:-"+index+":24:2", "2ƒNAƒ24ƒ769035ƒ40000ƒFalseƒ2ƒMi YƒVJ198 - Ho Chi MinhƒNAƒ769035ƒ1ƒMi Yƒ40,000ƒ40000ƒ4000.00ƒ0ƒ0");						
		params.put("hidPaxItem:-"+index+":51:2", "2ƒNAƒ51ƒ768885ƒ55000ƒFalseƒ2ƒCombo My YƒVJ198 - Ho Chi MinhƒNAƒ768885ƒ1ƒCombo My Yƒ55,000ƒ55000ƒ5500.00ƒ0ƒ0");						
		params.put("hidPaxItem:-"+index+":64:2", "2ƒNAƒ64ƒ768735ƒ55000ƒFalseƒ2ƒCombo Bun XaoƒVJ198 - Ho Chi MinhƒNAƒ768735ƒ1ƒCombo Bun Xaoƒ55,000ƒ55000ƒ5500.00ƒ0ƒ0");												
	}

	private List<Integer> searchService(List<AirExtraService> extraServices){
		List<Integer>  list = new ArrayList<Integer>();
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

	private void convertPaymentParams(HashMap<String, String> params){
		params.put("lstPmtType", "5,PL,0,V,0,0,0");
		params.put("txtCardNo", "");
		params.put("dlstExpiry", "2020/12/31");
		params.put("txtCVC", "");
		params.put("txtCardholder", "");

		params.put("txtAddr1", "");
		params.put("txtCity", "");
		params.put("txtPCode", "");
		params.put("lstCtry", "-1");
		params.put("lstProv", "-1");
		params.put("txtPhone", "");
	}

	public HashMap<String, String> createPostPaymentsParams(final String viewstate) throws  VietJetBookException {
		try {
			HashMap<String, String> params = new HashMap<String, String>();		
			String button = "3rd";
			createParams(params,button,viewstate);
			convertPaymentParams(params);		
			return params;
		} catch (Exception ex) {
			throw new VietJetBookException("INVALID_REQUEST");
		}
	}

	public HashMap<String, String> createPostConfirmParams(final String viewstate) throws  VietJetBookException {
		try {
			HashMap<String, String> params = new HashMap<String, String>();		
			String button = "continue";
			createParams(params,button,viewstate);			
			return params;
		} catch (Exception ex) {
			throw new VietJetBookException("INVALID_REQUEST");
		}
	}

	public class VietJetBookException extends Exception {

		private static final long serialVersionUID = 1L;

		public VietJetBookException(String message) {
			super(message);
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

}
