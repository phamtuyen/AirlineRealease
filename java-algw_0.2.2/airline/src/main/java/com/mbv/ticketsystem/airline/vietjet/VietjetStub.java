package com.mbv.ticketsystem.airline.vietjet;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mbv.ticketsystem.airline.AirErrorType;
import com.mbv.ticketsystem.airline.vietjet.VietjetPay.BuyResult;
import com.mbv.ticketsystem.airline.vietjet.VietjetWorker.BookResult;
import com.mbv.ticketsystem.common.airline.AirExtraService;
import com.mbv.ticketsystem.common.airline.AirFareInfo;
import com.mbv.ticketsystem.common.airline.AirFarePriceInfo;
import com.mbv.ticketsystem.common.airline.AirFarePriceInfos;
import com.mbv.ticketsystem.common.airline.AirFarePriceOption;
import com.mbv.ticketsystem.common.airline.AirItinerary;
import com.mbv.ticketsystem.common.airline.AirPassengerInfo;
import com.mbv.ticketsystem.common.airline.AirPassengerType;
import com.mbv.ticketsystem.common.airline.AirPassengerTypePrice;
import com.mbv.ticketsystem.common.airline.AirPassengerTypeQuantity;
import com.mbv.ticketsystem.common.airline.AirTicketingInfo;
import com.mbv.ticketsystem.common.airline.AirTicketingStatus;
import com.mbv.ticketsystem.common.airline.UpdateFarePriceCommand;
import com.mbv.ticketsystem.common.base.AgentInfo;
import com.mbv.ticketsystem.common.base.ContactInfo;

@SuppressWarnings("serial")
public class VietjetStub {
	private String viewstate;
	private String sessId; 		
	private VietjetBook vjBook = null;   
	private VietjetPay vjPay = null; 

	private String urlLogin = "https://ameliaweb5.intelisys.ca/VietJet/sitelogin.aspx?lang=vi";
	private String urlMenu = "https://ameliaweb5.intelisys.ca/VietJet/AgentOptions.aspx?lang=vi&sesid=";
	private String urlSearch = "https://ameliaweb5.intelisys.ca/VietJet/ViewFlights.aspx?Lang=vi";
	private String urlTravel = "https://ameliaweb5.intelisys.ca/VietJet/TravelOptions.aspx?lang=vi&sesid=";
	private String urlDetail = "https://ameliaweb5.intelisys.ca/VietJet/Details.aspx?lang=vi&sesid=";
	private String urlAddOns = "https://ameliaweb5.intelisys.ca/VietJet/AddOns.aspx?lang=vi&sesid=";
	private String urlPayments = "https://ameliaweb5.intelisys.ca/VietJet/Payments.aspx?lang=vi&sesid=";
	private String urlConfirm = "https://ameliaweb5.intelisys.ca/VietJet/Confirm.aspx?lang=vi&sesid=";
	private String urlItinerary = "https://ameliaweb5.intelisys.ca/VietJet/Itinerary.aspx?lang=vi&sesid=";	
	
	private HashMap<String, String> loginParams;
	final static Logger logger = Logger.getLogger(VietjetStub.class);

	// Init function
	private static DateFormat dateFormat_dd = new SimpleDateFormat("dd");
	private static DateFormat dateFormat_ddMMyyyy = new SimpleDateFormat("dd/MM/yyyy");
	private static DateFormat dateFormat_yyyyMM = new SimpleDateFormat("yyyy/MM");

	public VietjetStub(final VietjetConfig config) {
		loginParams = new HashMap<String, String>() {
			{
				put("DebugID", "61");
				put("SesID", "");
				put("__VIEWSTATE", config.getLoginViewState());
				put("txtAgentID", config.getUsername());
				put("txtAgentPswd", config.getPassword());				
			}
		};	
	}

	private HashMap<String, String> createSearchParams(UpdateFarePriceCommand request,final String viewstate) throws Exception {
		try {

			HashMap<String, String> params = new HashMap<String, String>() {
				{
					// put("lstCompanyList", "722ÃƒÆ’Ã¢â‚¬ ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢CTY VIET NHANH");
					put("SesID", "");
					put("DebugID", "29");
					put("button", "vfto");
					put("lstDepDateRange", "0");
					put("lstRetDateRange", "0");
					put("departTime1", "0000");
					put("departTime2", "0000");
					put("lstLvlService", "1");
					put("lstResCurrency", "VND");
					put("__VIEWSTATE", viewstate);
				}
			};

			params.put("txtNumAdults", request.getPassengerQuantity(AirPassengerType.ADT) + "");
			params.put("txtNumChildren", request.getPassengerQuantity(AirPassengerType.CHD) + "");
			params.put("txtNumInfants", request.getPassengerQuantity(AirPassengerType.INF) + "");

			AirFareInfo fare1 = request.getOriginDestinationInfos().get(0);
			params.put("lstOrigAP", fare1.getOriginCode());
			params.put("lstDestAP", fare1.getDestinationCode());
			params.put("dlstDepDate_Day", dateFormat_dd.format(fare1.getDepartureDate()));
			params.put("dlstDepDate_Month", dateFormat_yyyyMM.format(fare1.getDepartureDate()));
			params.put("departure1", dateFormat_ddMMyyyy.format(fare1.getDepartureDate()));

			// Neu RoundTrip thi get thong tin diem can den
			if (request.getOriginDestinationInfos().size() == 2) {
				AirFareInfo fare2 = request.getOriginDestinationInfos().get(1);
				if (fare1.getOriginCode().equals(fare2.getDestinationCode()) && fare1.getDestinationCode().equals(fare2.getOriginCode())) {
					params.put("chkRoundTrip", "on");
					params.put("dlstRetDate_Day", dateFormat_dd.format(fare2.getDepartureDate()));
					params.put("dlstRetDate_Month", dateFormat_yyyyMM.format(fare2.getDepartureDate()));
					params.put("departure2", dateFormat_ddMMyyyy.format(fare2.getDepartureDate()));
				} else {
					throw new Exception("INVALID_REQUEST");
				}
			}
			return params;

		} catch (Exception ex) {
			throw new Exception("INVALID_REQUEST");
		}
	}

	public AirFarePriceInfos search(UpdateFarePriceCommand request) throws Exception {
		logger.info("VietjetSeach:function Login");
		loginBookVietJet();
		logger.info("VietjetSeach:function menu");
		menuBookVietJet();
		logger.info("VietjetSeach:function search");
		return extractAirFareInfo(searchViewFlight(request),request);
	}

	private void loginBookVietJet() throws Exception{
		if (vjBook == null)
			vjBook = new VietjetBook();	
		Connection.Response resGetLogin = vjBook.getProcessLogin(urlLogin);
		URL urlResult = resGetLogin.url();
		if (!urlResult.toString().equals(urlLogin))
			throw new Exception("Get_Login Exception");
		ArrayList<String > list = vjBook.getDocumentJsoup((Connection.Response) resGetLogin);
		viewstate = list.get(0);
		sessId = list.get(1);
		Connection.Response resPostLogin = vjBook.postProcess(urlLogin,loginParams,sessId);   
		urlResult = resPostLogin.url();
		if (!urlResult.toString().contains("AgentOptions.aspx")){
			logger.error("VietjetBook: Post_Login Exception");
			throw new Exception("Post_Login Exception"); 
		}

	}

	private void menuBookVietJet() throws Exception{
		Connection.Response resGetMenu = vjBook.getProcess(urlMenu,sessId);
		URL urlResult = resGetMenu.url();
		if (!urlResult.toString().equals(urlMenu))
			throw new Exception("Get_Menu Exception");

		ArrayList<String > list = vjBook.getDocumentJsoup((Connection.Response) resGetMenu);
		viewstate = list.get(0);			
		Connection.Response resPostMenu = vjBook.postProcess(urlMenu,vjBook.createPostMenuParams(viewstate),sessId);
		urlResult = resPostMenu.url();
		if (!urlResult.toString().contains("ViewFlights.aspx"))		{
			logger.error("VietjetBook: Post_Menu Exception");
			throw new Exception("Post_Menu Exception");
		}

	}

	private void searchBookVietJet(AirItinerary itinerary) throws Exception{
		Connection.Response resGetSearch = vjBook.getProcess(urlSearch,sessId);
		URL urlResult = resGetSearch.url();
		if (!urlResult.toString().equals(urlSearch))
			throw new Exception("Get_Search Exception");

		ArrayList<String > list  = vjBook.getDocumentJsoup((Connection.Response) resGetSearch);
		viewstate = list.get(0);
		ArrayList<AirFareInfo> listAirFareInfo = (ArrayList<AirFareInfo>)itinerary.getFareInfos();
		List<AirPassengerInfo> listAirPassengerInfo = (List<AirPassengerInfo>)itinerary.getPassengerInfos();
		UpdateFarePriceCommand request = new UpdateFarePriceCommand();
		request.setOriginDestinationInfos(listAirFareInfo);
		request.setPassengerInfos(vjBook.getAirPassengerTypeQuantity(listAirPassengerInfo));			
		Connection.Response resPostSearch = vjBook.postProcess(urlSearch,vjBook.createSearchParams(request,viewstate),sessId);	
		urlResult = resPostSearch.url();	
		if (!urlResult.toString().contains("TravelOptions.aspx"))	{
			logger.error("VietjetBook: Post_Search Exception");
			throw new Exception("Post_Search Exception");
		}

	}
	
	private String searchViewFlight(UpdateFarePriceCommand request) throws Exception{
		Connection.Response resGetSearch = vjBook.getProcess(urlSearch,sessId);
		URL urlResult = resGetSearch.url();
		if (!urlResult.toString().equals(urlSearch))
			throw new Exception("Get_Search Exception");

		ArrayList<String > list  = vjBook.getDocumentJsoup((Connection.Response) resGetSearch);
		viewstate = list.get(0);		
		
		Connection.Response resPostSearch = vjBook.postProcess(urlSearch,createSearchParams(request,viewstate),sessId);	
		urlResult = resPostSearch.url();	
		if (!urlResult.toString().contains("TravelOptions.aspx"))	{
			logger.error("VietjetBook: Post_Search Exception");
			throw new Exception("Post_Search Exception");
		}
		return resPostSearch.body();
	}

	private void travelBookVietJet(AirItinerary itinerary) throws Exception{
		Connection.Response resGetTravel = vjBook.getProcess(urlTravel,sessId);
		URL urlResult = resGetTravel.url();
		if (!urlResult.toString().equals(urlTravel))
			throw new Exception("Get_Travel Exception");

		ArrayList<String > list = vjBook.getDocumentJsoup((Connection.Response) resGetTravel);
		viewstate = list.get(0);			
		String reference1 = itinerary.getFareInfos().get(0).getReference();
		String reference2 = "";
		if(itinerary.getFareInfos().size()==2)
			reference2 = itinerary.getFareInfos().get(1).getReference();			
		Connection.Response resPostTravel = vjBook.postProcess(urlTravel,vjBook.createPostTravelParams(reference1,reference2,viewstate),sessId);
		urlResult = resPostTravel.url();
		if (!urlResult.toString().contains("Details.aspx"))		{
			logger.error("VietjetBook: Post_Travel Exception");
			throw new Exception("Post_Travel Exception");
		}
	}
	private boolean detailBookVietJet(AirItinerary itinerary,long sumPrices) throws Exception{
		Connection.Response resGetDetail = vjBook.getProcess(urlDetail,sessId);
		URL urlResult = resGetDetail.url();
		if (!urlResult.toString().equals(urlDetail))
			throw new Exception("Get_Detail Exception");
		Document document = Jsoup.parse(resGetDetail.body());	
		String sumAmount1 = document.getElementById("Leg1BSTotalFare").text().replace(",", "");  
		long amountPrice = Long.valueOf(sumAmount1).longValue();
		String sumAmount2 = "";		
		if(itinerary.getFareInfos().size() == 2){
			sumAmount2 = document.getElementById("Leg2BSTotalFare").text().replace(",", "");
			amountPrice += Long.valueOf(sumAmount2).longValue();
		}				
		if(sumPrices != amountPrice){
			logger.info("vietjetDetail Compare prices: Result false" );
			return false;
		}		
		ArrayList<String> list = vjBook.getDocumentJsoup((Connection.Response) resGetDetail);
		viewstate = list.get(0);			
		List<AirPassengerInfo> passengerInfos = itinerary.getPassengerInfos();
		ContactInfo contactInfo = itinerary.getContactInfo();
		Connection.Response resPostDetail = vjBook.postProcess(urlDetail,vjBook.createPostDetailParams(passengerInfos,contactInfo,viewstate),sessId);
		urlResult = resPostDetail.url();
		if (!urlResult.toString().contains("AddOns.aspx"))		{
			logger.error("VietjetBook: Post_Detail Exception");
			throw new Exception("Post_Detail Exception");
		}		
		return true;
	}

	private void addonsBookVietJet(AirItinerary itinerary) throws Exception{
		Connection.Response resGetAddons = vjBook.getProcess(urlAddOns,sessId);	
		URL urlResult = resGetAddons.url();
		if (!urlResult.toString().equals(urlAddOns))
			throw new Exception("Get_AddOns Exception");

		ArrayList<String > list = vjBook.getDocumentJsoup((Connection.Response) resGetAddons);
		viewstate = list.get(0);
		ArrayList<AirExtraService> extraServices = (ArrayList<AirExtraService>) itinerary.getExtraServices();
		//---- PaxItems ------
		Document document = Jsoup.parse(resGetAddons.body());				
		int glagMeal = 1;		
		String flagMeal  = document.select("#shopPaxOmega > tbody > tr:nth-child(1) > td > div > table > tbody > tr:nth-child(4) > td > table > tbody > tr:nth-child(10) > td > span").text();
		if(flagMeal.isEmpty() || flagMeal.equals(""))
			glagMeal = 0;

		Elements elements = document.getElementsByClass("shoppax_item");	      
		// VJ330
		Element flightcode = elements.get(0).select("td").get(2).tagName("option");      	 
		Elements flightInfos = flightcode.getElementsByTag("option");   

		// GO
		int index = 0;
		List<String> listAttrPasKg = new ArrayList<String>();
		for (Element flightInfo : flightInfos) {
			if(index >=2 && index <= 7){	        	
				listAttrPasKg.add(flightInfo.attr("hidpaxvalue"));
			}	        	
			index++;
		}       				     

		int roundTrip = 1;
		List<String> listAttrPasKgBack = null;
		ArrayList<AirFareInfo> listAirFareInfo = (ArrayList<AirFareInfo>)itinerary.getFareInfos();
		List<AirPassengerInfo> listAirPassengerInfo = (List<AirPassengerInfo>)itinerary.getPassengerInfos();
		UpdateFarePriceCommand request = new UpdateFarePriceCommand();
		request.setOriginDestinationInfos(listAirFareInfo);
		request.setPassengerInfos(vjBook.getAirPassengerTypeQuantity(listAirPassengerInfo));
		if (request.getOriginDestinationInfos().size() == 2){
			roundTrip = 2;
			// BACK
			// VJ331
			Element flightcodeBack = elements.get(1).select("td").get(2).tagName("option");      	 
			Elements flightInfosBack = flightcodeBack.getElementsByTag("option");  
			index = 0;	        
			listAttrPasKgBack = new ArrayList<String>();
			for (Element flightInfo : flightInfosBack) {
				if(index >=2 && index <= 7){	        	
					listAttrPasKgBack.add(flightInfo.attr("hidpaxvalue"));
				}	        	
				index++;
			} 
		}

		ArrayList<AirPassengerTypeQuantity> passengerInfosList = vjBook.getAirPassengerTypeQuantity(listAirPassengerInfo);
		int adt_chd = passengerInfosList.get(0).getQuantity() + passengerInfosList.get(1).getQuantity();
		Connection.Response resPostAddOns =  vjBook.postProcess(urlAddOns,vjBook.createPostAddOnsParams(roundTrip,adt_chd,extraServices,listAttrPasKg,listAttrPasKgBack,glagMeal,viewstate),sessId);
		// 
		urlResult = resPostAddOns.url();
		if (!urlResult.toString().contains("Payments.aspx"))	{
			logger.error("VietjetBook: Post_AddOns Exception");
			throw new Exception("Post_AddOns Exception");
		}		
	}

	private void paymentsBookVietJet(AirItinerary itinerary) throws Exception{
		Connection.Response resGetPayments = vjBook.getProcess(urlPayments,sessId);
		URL urlResult = resGetPayments.url();
		if (!urlResult.toString().equals(urlPayments))
			throw new Exception("Get_Payments Exception");
		ArrayList<String > list = vjBook.getDocumentJsoup((Connection.Response) resGetPayments);
		viewstate = list.get(0);			
		Connection.Response resPostPayments = vjBook.postProcess(urlPayments,vjBook.createPostPaymentsParams(viewstate),sessId);		
		urlResult = resPostPayments.url();
		if (!urlResult.toString().contains("Confirm.aspx"))		{
			logger.error("VietjetBook: Post_Payments Exception");
			throw new Exception("Post_Payments Exception");
		}

	}

	private void confirmBookVietJet() throws Exception{
		Connection.Response resGetConfirm = vjBook.getProcess(urlConfirm,sessId);
		URL urlResult = resGetConfirm.url();
		if (!urlResult.toString().equals(urlConfirm))
			throw new Exception("Get_Confirm Exception");
		
		Document document = Jsoup.parse(resGetConfirm.body());		
		String content = document.html();			
		File file = new File("/home/phamtuyen/parse.txt");
		// if file doesnt exists, then create it
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(content);
		bw.close();	
		
		ArrayList<String > list = vjBook.getDocumentJsoup((Connection.Response) resGetConfirm);
		viewstate = list.get(0);			
		Connection.Response resPostConfirm = vjBook.postProcess(urlConfirm,vjBook.createPostConfirmParams(viewstate),sessId);	
		urlResult = resPostConfirm.url();									
		if (!urlResult.toString().contains("Confirm.aspx")){
			logger.error("VietjetBook: Post_Confirm Exception");
			throw new Exception("Post_Confirm Exception");
		}

	}

	private BookResult itineraryBookVietJet() throws Exception{
		Connection.Response resGetItinerary = vjBook.getProcess(urlItinerary,sessId);    
		URL urlResult = resGetItinerary.url();
		if (!urlResult.toString().equals(urlItinerary)){
			logger.info("VietjetBook: Get_Itinerary Exception");
			throw new Exception("Get_Itinerary Exception");	
		}

		ArrayList<String > list = vjBook.getDocumentJsoup((Connection.Response) resGetItinerary);
		viewstate = list.get(0);
		Document document = Jsoup.parse(resGetItinerary.body());
		String bookingId = document.select("#itin > table > tbody > tr > td:nth-child(1) > span.ResNumber").text();
		String amountBook = document.select("#rescharges > tbody > tr.ChargesTotal > td:nth-child(2) > strong").text();
		long sumAmount = Long.valueOf(amountBook.replace(",", "")).longValue();            
		BookResult result = null;
		if(bookingId != ""){
			result = new BookResult();
			result.setReservationCode(bookingId);
			result.setAmount(sumAmount);
		}
		return result;
	}

	public BookResult bookVietJet(AirItinerary itinerary,long sumPrices) throws Exception {
		try {					
			logger.info("VietjetBook:function Login");
			loginBookVietJet();	
			logger.info("VietjetBook:function menu");
			menuBookVietJet();
			logger.info("VietjetBook:function search");
			searchBookVietJet(itinerary);	
			logger.info("VietjetBook:function travel");
			travelBookVietJet(itinerary);
			logger.info("VietjetBook:function detail");
			boolean comparePrice = detailBookVietJet(itinerary,sumPrices);	
			if(comparePrice == false){
				BookResult result = new BookResult();				
				result.setReservationCode("1111111"); 
				result.setAmount(0000000);
				return result;
			}
			logger.info("VietjetBook:function addons");
			addonsBookVietJet(itinerary);	
			logger.info("VietjetBook:function payment");			
			paymentsBookVietJet(itinerary);
			logger.info("VietjetBook:function confirm");
			confirmBookVietJet();
			logger.info("VietjetBook:function itinerary");
			return itineraryBookVietJet();		
		} catch (Exception ex) {
			logger.error("VietjetBook: " + ex.toString()); 
			throw ex;
		}
	}

	private AirItinerary vietjetSearchBookIdEditRes() throws Exception{
		String urlEditRes = "https://ameliaweb5.intelisys.ca/VietJet/EditRes.aspx?lang=vi&st=sl&sesid=";
		Connection.Response resGetEditRes = vjPay.getProcess(urlEditRes,sessId);		
		URL urlResult = resGetEditRes.url();
		if (!urlResult.toString().contains("EditRes.aspx"))
			throw new Exception("Get_EditRes Exception");
		String html = resGetEditRes.body();
		return extractAirItinerary(html);
	}
	
	public BookResult vietjetSearchBookId(AirItinerary itinerary) throws Exception {
		BookResult result = null;
		logger.info("VietjetSearchBookId:function login");
		loginBuyVietJet();
		logger.info("VietjetSearchBookId:function menu");
		menuBuyVietJet();
		logger.info("VietjetSearchBookId:function searchResOne");
		searchResOneBuyVietJet(itinerary);
		logger.info("VietjetSearchBookId:function searchResTwo");
		searchResTwoBuyVietJet(itinerary);
		logger.info("VietjetSearchBookId:function recindFucnc");
		recindFunctionBuyVietJet();
		logger.info("VietjetSearchBookId:function editRes");
		AirItinerary itinerarySearch = vietjetSearchBookIdEditRes();
		if(itinerarySearch.getTicketingInfo().getAmount() != 0){
			result = new BookResult();
			result.setAmount(10000);					
			itinerary.getTicketingInfo().setAmount(itinerarySearch.getTicketingInfo().getAmount());
			itinerary.getTicketingInfo().setStatus(AirTicketingStatus.BOOK_SUCCESS);
			itinerary.getTicketingInfo().setCreatedDate(new Date());
			itinerary.getTicketingInfo().setUpdatedDate(new Date());
			itinerary.setPassengerInfos(itinerarySearch.getPassengerInfos());
			itinerary.setFareInfos(itinerarySearch.getFareInfos());
			List<AirExtraService> extraServices = new ArrayList<AirExtraService>();
			itinerary.setExtraServices(extraServices);
		}
		logger.info("VietjetSearchBookId:finish extractInfo");
		return result;
	}

	private AirItinerary extractAirItinerary(String htmlContent) throws Exception {
 		AirItinerary itinerary = new AirItinerary();
 		VietjetSearchInfo vietjetSearchInfo = new VietjetSearchInfo();
		DateFormat dateFormatFull = new SimpleDateFormat("dd/MM/yyyy EEE HH:mm", Locale.US);
		try {
			Document doc = Jsoup.parse(htmlContent, "UTF-8");	
			Elements passengerTable = doc.select("table[class=hdrTable900]");
			Elements paxEvenTable = passengerTable.select("tr.GridPaxEven");
			List<AirPassengerInfo> passengerInfos = vietjetSearchInfo.getPassengerInfo(paxEvenTable);
			long sumAmount = vietjetSearchInfo.addAmount(paxEvenTable); 
			int countInf = vietjetSearchInfo.countInf(paxEvenTable);			
			Elements paxOddTable = passengerTable.select("tr.GridPaxOdd");			
			sumAmount += vietjetSearchInfo.addAmount(paxOddTable);
			countInf += vietjetSearchInfo.countInf(paxOddTable);
			passengerInfos = vietjetSearchInfo.addPassengerInfo(passengerInfos, vietjetSearchInfo.getPassengerInfo(paxOddTable));			
			for(int i = 0;i < countInf;i++){
				AirPassengerInfo passengerInfo = new AirPassengerInfo();
				passengerInfo.setFirstName("Em");
				passengerInfo.setLastName("Be");
				passengerInfo.setPassengerType(AirPassengerType.INF);
				passengerInfos.add(passengerInfo);	
			}
			Elements dataTables = doc.select("table[id=grdLegOmegaEdit]");
			Elements grdLegGridEdit = dataTables.select("table[id=grdLegGridEdit]");
			Elements grdLegGridEditMstrs = grdLegGridEdit.select("table[id=grdLegGridEditMstr]");
			List<AirFareInfo> fareInfos = new ArrayList<AirFareInfo>();	    	
			for(Element grdLegGridEditMstr:grdLegGridEditMstrs){
				AirFareInfo fareInfo = new AirFareInfo();
				String siteDate = grdLegGridEditMstr.select("td").first().text();
				String departureDate = grdLegGridEditMstr.select("td").get(2).text().substring(0, 5);			
				fareInfo.setDepartureDate(dateFormatFull.parse(siteDate + " " + departureDate.substring(0, 5)));
				String arrivalDate = grdLegGridEditMstr.select("td").get(3).text().substring(0, 5);
				fareInfo.setArrivalDate(dateFormatFull.parse(siteDate + " " + arrivalDate.substring(0, 5)));			
				fareInfo.setFlightCode(grdLegGridEditMstr.select("td").get(1).text());			
				fareInfo.setOriginCode(grdLegGridEditMstr.select("td").get(2).text().substring(6, 9));
				fareInfo.setDestinationCode(grdLegGridEditMstr.select("td").get(3).text().substring(6, 9));		
				fareInfos.add(fareInfo);
			}
			Elements hdrTableMidLeg = grdLegGridEdit.select("table[class=hdrTableMidLeg]");
			Element grdPaxFareDetails = hdrTableMidLeg.select("tr[id=grdPaxFareDetails]").get(0);
			String classCode = grdPaxFareDetails.select("td").get(1).text().substring(0, 1);
			fareInfos.get(0).setClassCode(classCode);		
			
			ContactInfo contactInfo = new ContactInfo();
			contactInfo.setAddress("104 Mai Thi Luu, Q1");
			contactInfo.setCity("TP Ho Chi Minh");
			contactInfo.setMobile("01264143088");
			contactInfo.setEmail("support@mobivi.com");
			
			AirTicketingInfo ticketingInfo = new AirTicketingInfo();
			ticketingInfo.setCreatedDate(new Date());
	        ticketingInfo.setUpdatedDate(new Date());
	        ticketingInfo.setStatus(AirTicketingStatus.BOOK_SUCCESS);
			ticketingInfo.setAmount(sumAmount);
			itinerary.setTicketingInfo(ticketingInfo);
			itinerary.setPassengerInfos(passengerInfos);
			itinerary.setFareInfos(fareInfos);
			itinerary.setContactInfo(contactInfo);
		} catch (Exception e) {
			throw new Exception(AirErrorType.PARSE_ERROR + ":        " + e);
		}
		return itinerary;
	}


	private void loginBuyVietJet() throws Exception{
		if (vjPay == null)
			vjPay = new VietjetPay();	
		Connection.Response resGetLogin = vjPay.getProcessLogin(urlLogin);
		URL urlResult = resGetLogin.url();
		if (!urlResult.toString().equals(urlLogin))
			throw new Exception("Get_Login Exception");
		ArrayList<String > list = vjPay.getDocumentJsoup((Connection.Response) resGetLogin);
		viewstate = list.get(0);
		sessId = list.get(1);			
		Connection.Response resPostLogin = vjPay.postProcess(urlLogin,loginParams,sessId);
		urlResult = resPostLogin.url();		
		if (!urlResult.toString().contains("AgentOptions.aspx")){
			logger.error("VietjetPay: Post_Login Exception");
			throw new Exception("Post_Login Exception"); 
		}			
	}

	private void menuBuyVietJet() throws Exception{
		Connection.Response resGetMenu = vjPay.getProcess(urlMenu,sessId);
		URL urlResult = resGetMenu.url();
		if (!urlResult.toString().equals(urlMenu))
			throw new Exception("Get_Menu Exception");

		ArrayList<String > list = vjPay.getDocumentJsoup((Connection.Response) resGetMenu);
		viewstate = list.get(0);			
		Connection.Response resPostMenu = vjPay.postProcess(urlMenu,vjPay.createPostMenuParams(viewstate),sessId);
		urlResult = resPostMenu.url();
		if (!urlResult.toString().contains("SearchRes.aspx"))	{
			logger.error("VietjetPay: Post_Menu Exception");
			throw new Exception("Post_Menu Exception"); 
		}

	}

	private void searchResOneBuyVietJet(AirItinerary itinerary) throws Exception{
		String urlSearchRes = "https://ameliaweb5.intelisys.ca/VietJet/SearchRes.aspx?lang=vi&st=sl&sesid=";
		Connection.Response resGetSearchResOne = vjPay.getProcess(urlSearchRes,sessId); 
		URL urlResult = resGetSearchResOne.url();
		if (!urlResult.toString().equals(urlSearchRes))
			throw new Exception("Get_SearchResOne Exception");

		ArrayList<String > list = vjPay.getDocumentJsoup((Connection.Response) resGetSearchResOne);
		viewstate = list.get(0);		
		AirTicketingInfo ticketInfo = itinerary.getTicketingInfo();
		final String reservationCode = ticketInfo.getReservationCode();
		Connection.Response resPostSearchResOne = vjPay.postProcess(urlSearchRes,vjPay.createSearchResCodeParams(reservationCode,viewstate),sessId);		
		urlResult = resPostSearchResOne.url();
		if (!urlResult.toString().contains("SearchRes.aspx")){
			logger.error("VietjetPay: Post_SearchResOne Exception");
			throw new Exception("Post_SearchResOne Exception"); 
		}

	}

	private void searchResTwoBuyVietJet(AirItinerary itinerary) throws Exception{
		// 
		String urlSearchRes = "https://ameliaweb5.intelisys.ca/VietJet/SearchRes.aspx?lang=vi&st=sl&sesid=";
		Connection.Response resGetSearchResTwo = vjPay.getProcess(urlSearchRes,sessId);
		URL urlResult = resGetSearchResTwo.url();
		if (!urlResult.toString().equals(urlSearchRes))
			throw new Exception("Get_SearchResTwo Exception");

		ArrayList<String > list = vjPay.getDocumentJsoup((Connection.Response) resGetSearchResTwo);
		viewstate = list.get(0);	
		AirTicketingInfo ticketInfo = itinerary.getTicketingInfo();
		final String reservationCode = ticketInfo.getReservationCode();	
		Connection.Response resPostSearchResTwo = vjPay.postProcess(urlSearchRes,vjPay.createSearchResContinueParams(reservationCode,viewstate),sessId);
		urlResult = resPostSearchResTwo.url();			
		if (!urlResult.toString().contains("EditRecindFunction.aspx"))	{
			logger.error("VietjetPay: Post_SearchResTwo Exception");
			throw new Exception("Post_SearchResTwo Exception"); 
		}

	}

	private void recindFunctionBuyVietJet() throws Exception{									
		String urlEditRecindFunction = "https://ameliaweb5.intelisys.ca/VietJet/EditRecindFunction.aspx?lang=vi&st=sl&sesid=";
		Connection.Response resGetEditRecindFunction = vjPay.getProcess(urlEditRecindFunction,sessId);
		URL urlResult = resGetEditRecindFunction.url();
		if (!urlResult.toString().equals(urlEditRecindFunction))
			throw new Exception("Get_EditRecindFunction Exception");

		ArrayList<String> list = vjPay.getDocumentJsoup((Connection.Response) resGetEditRecindFunction);
		viewstate = list.get(0);
		Connection.Response resPostEditRecindFunction = vjPay.postProcess(urlEditRecindFunction,vjPay.createEditRecindFunctionParams(viewstate),sessId);
		urlResult = resPostEditRecindFunction.url();		
		if (!urlResult.toString().contains("EditRes.aspx")){
			logger.error("VietjetPay: Post_EditRecindFunction Exception");
			throw new Exception("Post_EditRecindFunction Exception"); 
		}

	}

	private void editResBuyVietJet() throws Exception{
		String urlEditRes = "https://ameliaweb5.intelisys.ca/VietJet/EditRes.aspx?lang=vi&st=sl&sesid=";
		Connection.Response resGetEditRes = vjPay.getProcess(urlEditRes,sessId);		
		URL urlResult = resGetEditRes.url();
		if (!urlResult.toString().contains("EditRes.aspx"))
			throw new Exception("Get_EditRes Exception");

		ArrayList<String > list = vjPay.getDocumentJsoup((Connection.Response) resGetEditRes);
		viewstate = list.get(0);
		Document document = Jsoup.parse(resGetEditRes.body());
		Connection.Response resPostEditRes = vjPay.postProcess(urlEditRes,vjPay.createEditResParams(document,viewstate),sessId);
		urlResult = resPostEditRes.url();		
		if (!urlResult.toString().contains("AddPayment.aspx"))	{
			logger.error("VietjetPay: Post_EditRes Exception");
			throw new Exception("Post_EditRes Exception"); 
		}

	}

	private void addPaymentBuyVietJet(AirItinerary itinerary) throws Exception{
		String urlAddPayment = "https://ameliaweb5.intelisys.ca/VietJet/AddPayment.aspx?lang=vi&st=sl&sesid=";
		Connection.Response resGetAddPayment = vjPay.getProcess(urlAddPayment,sessId);
		URL urlResult = resGetAddPayment.url();
		if (!urlResult.toString().contains("AddPayment.aspx"))
			throw new Exception("Get_AddPayment Exception");			

		ArrayList<String > list = vjPay.getDocumentJsoup((Connection.Response) resGetAddPayment);
		viewstate = list.get(0);
		ContactInfo contactInfo = itinerary.getContactInfo();
		AgentInfo agentInfo = itinerary.getAgentInfo();				
		Connection.Response resPostAddPayment = vjPay.postProcess(urlAddPayment,vjPay.createAddPaymentParams(agentInfo,contactInfo,viewstate),sessId);	
		urlResult = resPostAddPayment.url();			
		if (!urlResult.toString().contains("Processing.aspx")){
			logger.error("VietjetPay: Post_AddPayment Exception");
			throw new Exception("Post_AddPayment Exception");
		}

	}

	private BuyResult processingBuyVietJet() throws Exception{
		BuyResult result = null;
		String urlProcessing = "https://ameliaweb5.intelisys.ca/VietJet/Processing.aspx?lang=vi&st=sl&sesid=";
		Connection.Response resGetProcessing = vjPay.getProcess(urlProcessing,sessId);	
		URL urlResult = resGetProcessing.url();		
		if (!urlResult.toString().contains("Processing.aspx")){
			logger.error("VietjetPay: Get_Processing Exception");
			throw new Exception("Get_Processing Exception");
		}

		ArrayList<String > list = vjPay.getDocumentJsoup((Connection.Response) resGetProcessing);
		viewstate = list.get(0);	
		Connection.Response resPostProcessing = vjPay.postProcess(urlProcessing,vjPay.createProcessingParams(viewstate),sessId);
		urlResult = resPostProcessing.url();			
		if (!urlResult.toString().contains("EditResResults.aspx"))	
			return result; 
		else{
			result = new BuyResult("");
			String[] ticketNumbers = new String[10];						
			ticketNumbers[0] = 	"123456789";
			result = new BuyResult(ticketNumbers);
			return result;
		}
	}

	public BuyResult buyVietJet(AirItinerary itinerary) throws Exception {	
		try {		
			logger.info("VietjetPayMent:function login");
			loginBuyVietJet();
			logger.info("VietjetPayMent:function menu");
			menuBuyVietJet();
			logger.info("VietjetPayMent:function searchResOne");
			searchResOneBuyVietJet(itinerary);
			logger.info("VietjetPayMent:function searchResTwo");
			searchResTwoBuyVietJet(itinerary);
			logger.info("VietjetPayMent:function recindFucnc");
			recindFunctionBuyVietJet();
			logger.info("VietjetPayMent:function editRes");
			editResBuyVietJet();			
			logger.info("VietjetPayMent:function addPayment");
			addPaymentBuyVietJet(itinerary);
			logger.info("VietjetPayMent:function processingBuy");
			return processingBuyVietJet();		
		} catch (Exception ex) {
			throw ex;
		}
	}

	private AirFarePriceInfos extractAirFareInfo(String htmlContent, UpdateFarePriceCommand request) throws Exception {
		AirFarePriceInfos result = new AirFarePriceInfos();
		try {
			Document doc = Jsoup.parse(htmlContent, "UTF-8");
			Check_Login(doc);
			Elements dataTable = doc.select("#contentwsb");// #contentwsb
			Elements flightTable = dataTable.select("table.TrvOptGrid");// #travOpsMain>
			DateFormat dateFormatFull = new SimpleDateFormat("dd/MM/yyyy EEE HH:mm", Locale.US);
			for (Element incFlightTable : flightTable) {
				String siteDate = incFlightTable.select("b").get(0).html();
				Elements exTickets = incFlightTable.select("tr[class^=gridFlight][id^=gridTravelOpt]");
				for (Element exTicket : exTickets) {
					try {
						AirFareInfo fareInfo = new AirFareInfo();
						fareInfo.setVendor("VJ");
						Elements partElements = exTicket.select("td.SegInfo");
						// Thoi gian xuat phat (khoi~ hanh)
						Element segment = partElements.get(0);
						String data = segment.text();
						fareInfo.setDepartureDate(dateFormatFull.parse(siteDate + " " + data.substring(0, 5)));
//						System.out.println(siteDate + " " + data.substring(0, 5));
						fareInfo.setOriginCode(data.substring(6, 9));
						segment = partElements.get(1);
						data = segment.text();
						fareInfo.setArrivalDate(dateFormatFull.parse(siteDate + " " + data.substring(0, 5)));
						fareInfo.setDestinationCode(data.substring(6, 9));
						fareInfo.setFlightCode(partElements.get(2).text().substring(0, 5));
						// Initialize PriceOption List
						List<AirFarePriceOption> priceOptionList = new ArrayList<AirFarePriceOption>();
						Elements priceOptions = exTicket.select("table.FaresGrid");
						for (Element option : priceOptions) {
							Elements prices = option.select("td[id^=gridTravelOpt]");
							for (Element price : prices) {
								AirFarePriceOption priceOption = new AirFarePriceOption();
								priceOption.setReference(price.select("input[id^=gridTravelOpt]").val());
								priceOption.setClassCode(priceOption.getReference().split("[,_]")[1]);
								priceOption.setClassName(priceOption.getReference().split("[,_]")[2]);

								long fare = Long.parseLong(price.select("#fare").first().val().replace(",", ""));
								long fare_taxes = Long.parseLong(price.select("#fare_taxes").first().val().replace(",", ""));
								long charges = Long.parseLong(price.select("#charges").first().val().replace(",", ""));
								int numADT = request.getPassengerQuantity(AirPassengerType.ADT);
								int numCHD = request.getPassengerQuantity(AirPassengerType.CHD);
								int numINF = request.getPassengerQuantity(AirPassengerType.INF);
								long zWhat = 10 * fare_taxes - fare;
								long axWhat = 2 * (charges - (numADT + numCHD) * zWhat) / (2 * numADT + numCHD);

								priceOption.add(new AirPassengerTypePrice(AirPassengerType.ADT, fare + fare_taxes + axWhat + zWhat));
								if (numCHD > 0) {
									priceOption.add(new AirPassengerTypePrice(AirPassengerType.CHD, fare + fare_taxes + axWhat / 2 + zWhat));
								}
								if (numINF > 0) {
									priceOption.add(new AirPassengerTypePrice(AirPassengerType.INF, 0));
								}
								priceOptionList.add(priceOption);
							}
						}

						AirFarePriceInfo farePriceInfo = new AirFarePriceInfo();
						farePriceInfo.setFareInfo(fareInfo);
						farePriceInfo.setPriceOptions(priceOptionList);
						result.add(farePriceInfo);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		} catch (Exception ex) {
			logger.error(ex.toString());
			throw new Exception(AirErrorType.PARSE_ERROR + ":        " + ex);
		}		
		return result;
	}

	private void Check_Login(Document doc) throws Exception {
		Elements loginSignal = doc.select("img[src=images/key.gif]");// ("#indAgencyLogin[src]");
		if (loginSignal.size() <= 0) {
			throw new Exception("session");
		}
	}

	// Others
	public class VietJetException extends Exception {

		private static final long serialVersionUID = 1L;

		public VietJetException(String message) {
			super(message);
		}
	}
}