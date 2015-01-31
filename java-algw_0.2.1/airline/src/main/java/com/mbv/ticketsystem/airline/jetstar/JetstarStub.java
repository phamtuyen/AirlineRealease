package com.mbv.ticketsystem.airline.jetstar;

import com.mbv.ticketsystem.airline.AirErrorType;
import com.mbv.ticketsystem.common.airline.AirItinerary;

import org.joda.time.DateTime;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mbv.ticketsystem.airline.httpsupport.Context;
import com.mbv.ticketsystem.airline.httpsupport.HttpHelper;
import com.mbv.ticketsystem.airline.jetstar.JetstarPay.BuyResult;
import com.mbv.ticketsystem.airline.jetstar.JetstarWorker.BookResult;
import com.mbv.ticketsystem.common.airline.AirExtraService;
import com.mbv.ticketsystem.common.airline.AirFareInfo;
import com.mbv.ticketsystem.common.airline.AirFarePriceInfo;
import com.mbv.ticketsystem.common.airline.AirFarePriceInfos;
import com.mbv.ticketsystem.common.airline.AirFarePriceOption;
import com.mbv.ticketsystem.common.airline.AirPassengerInfo;
import com.mbv.ticketsystem.common.airline.AirPassengerType;
import com.mbv.ticketsystem.common.airline.AirPassengerTypePrice;
import com.mbv.ticketsystem.common.airline.AirTicketingInfo;
import com.mbv.ticketsystem.common.airline.UpdateFarePriceCommand;
import com.mbv.ticketsystem.common.base.AgentInfo;
import com.mbv.ticketsystem.common.base.ContactInfo;
import com.mbv.ticketsystem.common.base.Gender;
import com.mbv.ticketsystem.common.base.OriginDestinationInfo;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
//import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
//import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

@SuppressWarnings({ "serial", "unused" })
public class JetstarStub {
	private static final String baseUrl = "https://booknow.jetstar.com/";
	private String viewState;
	private String sessId; 
	private String totalPrice;
	private String user = "";
	private String password = "";
	private JetstarBook jetStarBook = null;
	private JetstarPay jetStarPay = null;
	private HashMap<String, String> loginForm;
	private DateFormat date_ddMMyyyy = new SimpleDateFormat("dd/MM/yyyy");
	final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");	

//	Logger logger = LoggerFactory.getLogger(JetstarStub.class);
	final static Logger logger = Logger.getLogger(JetstarStub.class);

	public JetstarStub(final JetstarAccount jetstarAccount) {
		loginForm = new HashMap<String, String>() {
			{
				put("__EVENTTARGET", "ControlGroupSelectView$LinkButtonSubmit");
				put("__EVENTARGUMENT", "");				
				put("pageToken", "");
				put("ControlGroupNewTradeLoginAgentView$AgentNewTradeLoginView$TextBoxUserID", jetstarAccount.getUsername());
				put("ControlGroupNewTradeLoginAgentView$AgentNewTradeLoginView$PasswordFieldPassword", jetstarAccount.getPassword());
				put("ControlGroupNewTradeLoginAgentView$AgentNewTradeLoginView$ButtonLogIn", "");
			}
		};	
		user = jetstarAccount.getUsername();
		password = jetstarAccount.getPassword();				
	}

	private HashMap<String, String> CreateSearchForm(UpdateFarePriceCommand airSearchRequest,final String viewstate) throws Exception {
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

			HashMap<String, String> params = new HashMap<String, String>() {
				{
					put("__EVENTTARGET", "");
					put("__EVENTARGUMENT", "");
					put("__VIEWSTATE", viewstate);
					put("pageToken", "");
					put("total_price", "");
					//                    put("ControlGroupTradeSalesHomeView$AvailabilitySearchInputTradeSalesHomeView$DropDownListCurrency", "VND");
					put("ControlGroupTradeSalesHomeView$AvailabilitySearchInputTradeSalesHomeView$RadioButtonSearchBy", "");
					put("ControlGroupTradeSalesHomeView$AvailabilitySearchInputTradeSalesHomeView$numberTrips", "1");
					put("ControlGroupTradeSalesHomeView$AvailabilitySearchInputTradeSalesHomeView$ButtonSubmit", "");
				}
			};

			params.put("ControlGroupTradeSalesHomeView$AvailabilitySearchInputTradeSalesHomeView$RadioButtonMarketStructure", travelType);
			params.put("ControlGroupTradeSalesHomeView$AvailabilitySearchInputTradeSalesHomeView$DropDownListPassengerType_ADT", adt + "");
			params.put("ControlGroupTradeSalesHomeView$AvailabilitySearchInputTradeSalesHomeView$DropDownListPassengerType_CHD", chd + "");
			params.put("ControlGroupTradeSalesHomeView$AvailabilitySearchInputTradeSalesHomeView$DropDownListPassengerType_INFANT", inf + "");			

			OriginDestinationInfo tmpODI = airSearchRequest.getOriginDestinationInfos().get(0);
			params.put("ControlGroupTradeSalesHomeView$AvailabilitySearchInputTradeSalesHomeView$TextBoxMarketOrigin1", tmpODI.getOriginCode());
			params.put("ControlGroupTradeSalesHomeView$AvailabilitySearchInputTradeSalesHomeView$TextBoxMarketDestination1", tmpODI.getDestinationCode());
			params.put("ControlGroupTradeSalesHomeView$AvailabilitySearchInputTradeSalesHomeView$TextboxDepartureDate1", date_ddMMyyyy.format(tmpODI.getDepartureDate()));

			if (travelType.equals("RoundTrip") || travelType.equals("OpenJaw")) {
				OriginDestinationInfo tmpODI2 = airSearchRequest.getOriginDestinationInfos().get(1);
				params.put("ControlGroupTradeSalesHomeView$AvailabilitySearchInputTradeSalesHomeView$TextboxDestinationDate1", date_ddMMyyyy.format(tmpODI2.getDepartureDate()));
				params.put("ControlGroupTradeSalesHomeView$AvailabilitySearchInputTradeSalesHomeView$TextBoxMarketOrigin2", tmpODI2.getOriginCode());
				params.put("ControlGroupTradeSalesHomeView$AvailabilitySearchInputTradeSalesHomeView$TextBoxMarketDestination2", tmpODI2.getDestinationCode());
				params.put("ControlGroupTradeSalesHomeView$AvailabilitySearchInputTradeSalesHomeView$TextboxDepartureDate2", date_ddMMyyyy.format(tmpODI2.getDepartureDate()));
				params.put("ControlGroupTradeSalesHomeView$AvailabilitySearchInputTradeSalesHomeView$TextboxDestinationDate2", "");

				params.put("ControlGroupTradeSalesHomeView$AvailabilitySearchInputTradeSalesHomeView$TextBoxMultiCityOrigin1", tmpODI.getOriginCode());
				params.put("ControlGroupTradeSalesHomeView$AvailabilitySearchInputTradeSalesHomeView$TextBoxMultiCityDestination1", tmpODI.getDestinationCode());
				params.put("ControlGroupTradeSalesHomeView$AvailabilitySearchInputTradeSalesHomeView$TextboxDepartureMultiDate1", date_ddMMyyyy.format(tmpODI.getDepartureDate()));

				params.put("ControlGroupTradeSalesHomeView$AvailabilitySearchInputTradeSalesHomeView$TextBoxMultiCityOrigin2", tmpODI2.getOriginCode());
				params.put("ControlGroupTradeSalesHomeView$AvailabilitySearchInputTradeSalesHomeView$TextBoxMultiCityDestination2", tmpODI2.getDestinationCode());
				params.put("ControlGroupTradeSalesHomeView$AvailabilitySearchInputTradeSalesHomeView$TextboxDepartureMultiDate2", date_ddMMyyyy.format(tmpODI2.getDepartureDate()));
			}
			return params;
		} catch (Exception e) {
			throw new Exception(AirErrorType.INVALID_REQUEST + " CreateSearchForm:        " + e.getMessage());
		}
	}

	public AirFarePriceInfos search(UpdateFarePriceCommand request) throws Exception {
		logger.info("jetstarSearch: function login");
		jetStarBookLogin();
		logger.info("jetstarSearch: function search");
		String html = jetStarSearch(request);
		return extractAirFareInfo(html);
	}
	
	private String jetStarSearch(UpdateFarePriceCommand request) throws Exception{
		String urlSearch = baseUrl + "TradeSalesHome.aspx";
		Connection.Response resGetSearch = jetStarBook.getProcess(urlSearch,sessId);		
		URL urlResult = resGetSearch.url();
		if (!urlResult.toString().equals(urlSearch))
			throw new Exception("Get_BookSearch Exception");

		ArrayList<String > list = jetStarBook.getDocumentJsoup((Connection.Response) resGetSearch);
		viewState = list.get(0);		
		Connection.Response resPostSearch = jetStarBook.postProcess(urlSearch,CreateSearchForm(request,viewState),sessId);		
		urlResult = resPostSearch.url();
		if (!urlResult.toString().equals(baseUrl + "AgentSelect.aspx"))		{
			logger.info("jetstarBook: " + "Post_BookSearch Exception");
			throw new Exception("Post_BookSearch Exception"); 
		}
		return resPostSearch.body();
			
	}
	
	private AirFarePriceInfos extractAirFareInfo(String html) throws Exception {
		if (html == null) {
			throw new Exception(AirErrorType.INVALID_RESPONSE.toString());
		}
		Document document = Jsoup.parse(html);
		AirFarePriceInfos result = new AirFarePriceInfos();
		DateFormat dateFormatFull = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

		// Validate
		Element elementReturn = document.select("div[id=book-header]").first();
		if (elementReturn == null) {
			throw new Exception(AirErrorType.INVALID_RESPONSE.toString());
		}
		String href = elementReturn.getElementsByTag("a").first().attr("href");
		if (!"TradeSalesHome.aspx".equalsIgnoreCase(href)) {
			throw new Exception(AirErrorType.INVALID_RESPONSE.toString());
		}

		int quantityCHD, quantityINF;
		try {
			Element person = document.select("div[id=current-search] dd[data-children]").first();
			quantityCHD = Integer.parseInt(person.attr("data-children"));
			quantityINF = Integer.parseInt(person.attr("data-infants"));
		} catch (Exception ex) {
			throw new Exception(AirErrorType.INVALID_RESPONSE.toString());
		}

		// Elements elements = document.select("table[class=full-matrix]");
		Elements elements = document.getElementsByClass("full-matrix");
		for (Element element : elements) {
			try {
				Elements flightInfos = element.select("tr[flight-info-json]");
				for (Element flightInfo : flightInfos) {
					//Element flightcode = element.select("td[class=flight-code]").first();
					Element flightcode = flightInfo.select("td.flight-code").first();
					if (flightcode.text().replace(" ", "").length() > 5)
						continue;
					//String abc  = flightcode.toString();
					Element origincode = flightInfo.select("dt").first();
					Element destinationcode = flightInfo.select("dt").get(1);
					Element flightDetails = flightInfo.select("dd[class=details flight-summary-details]").first();
					Element datetime = flightDetails.getElementsByClass("date-time").first();

					AirFarePriceInfo priceInfo = new AirFarePriceInfo();
					// FARE INFO
					//0~H~HLECOH~5000~~Both~X|BL~ 791~ ~~HAN~05/19/2013 08:35~SGN~05/19/2013 10:40
					AirFareInfo fareinfo = new AirFareInfo();
					fareinfo.setVendor("BL");
					//                    StringBuffer sb = new StringBuffer(flightcode.text().replace(" ", ""));
					//                    if(sb.length()>5)
					//                    	sb.insert(5,"|");
					fareinfo.setFlightCode(flightcode.text().replace(" ", ""));
					fareinfo.setOriginCode(origincode.text());
					fareinfo.setDestinationCode(destinationcode.text());
					fareinfo.setDepartureDate(dateFormatFull.parse(datetime.attr("data-date-dept")));
					fareinfo.setArrivalDate(dateFormatFull.parse(datetime.attr("data-date-arrv")));
					priceInfo.setFareInfo(fareinfo);

					// PRICES OPTIONS
					AirFarePriceOption priceOption = null;
					//prices starter
					Element starter = flightInfo.select("td[class=selection starter]").first();
					if (starter != null) {
						try {
							Element priceDetails = starter.select("input[class=radio]").first();
							//                            logger.info("____" + priceDetails.html());
							// PRICES option info
							//0~E1~~ELECOE1~5000~~1~X|BL~ 369~ ~~CXR~11/22/2014 16:10~SGN~11/22/2014 17:10~
							String value = priceDetails.attr("value");
							String[] splitValues = value.split("[~\\|]");

							priceOption = new AirFarePriceOption();
							priceOption.setClassCode(splitValues[1]);
							priceOption.setClassName(starter.attr("bundletype"));
							priceOption.setReference(value);

							//price details
							// ADT
							long price = (long) Double.parseDouble(priceDetails.attr("bundle-fare-adt"));
							long priceADTFees = (long) Double.parseDouble(priceDetails.attr("data-discfees-adt"));
							priceOption.add(new AirPassengerTypePrice(AirPassengerType.ADT, price + priceADTFees));
							// CHD
							if (quantityCHD > 0) {
								price = (long) Double.parseDouble(priceDetails.attr("bundle-fare-chd"));
								long priceCHDFees = (long) Double.parseDouble(priceDetails.attr("data-discfees-chd"));
								priceOption.add(new AirPassengerTypePrice(AirPassengerType.CHD, price + priceCHDFees));
							}
							// INF
							if (quantityINF > 0) {
								priceOption.add(new AirPassengerTypePrice(AirPassengerType.INF, 0));
							}

							priceInfo.add(priceOption);
						} catch (Exception ex) {
							ex.printStackTrace();
							continue;
						}
					}

					// prices PLUS
					Element plus = flightInfo.select("td[class=selection starter-plus]").first();
					//                    logger.info(plus.html());
					if (plus != null) {
						try {
							Element priceDetails = plus.select("input[class=radio]").first();
							// PRICES option info
							//0~HP~HLECOHP~5001~~Both~X|BL~ 791~ ~~HAN~05/19/2013 08:35~SGN~05/19/2013 10:40
							String value = priceDetails.attr("value");
							String[] splitValues = value.split("[~\\|]");
							priceOption = new AirFarePriceOption();
							priceOption.setClassCode(splitValues[1]);
							priceOption.setClassName(plus.attr("bundletype"));
							priceOption.setReference(value);

							//price details
							// ADT
							long price = (long) Double.parseDouble(priceDetails.attr("bundle-fare-adt"));
							long priceADTFees = (long) Double.parseDouble(priceDetails.attr("data-discfees-adt"));
							priceOption.add(new AirPassengerTypePrice(AirPassengerType.ADT, price + priceADTFees));
							// CHD
							if (quantityCHD > 0) {
								price = (long) Double.parseDouble(priceDetails.attr("bundle-fare-chd"));
								long priceCHDFees = (long) Double.parseDouble(priceDetails.attr("data-discfees-chd"));
								priceOption.add(new AirPassengerTypePrice(AirPassengerType.CHD, price + priceCHDFees));
							}
							// INF
							if (quantityINF > 0) {
								priceOption.add(new AirPassengerTypePrice(AirPassengerType.INF, 0));
							}
							priceInfo.add(priceOption);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}

					// prices MAX
					Element max = flightInfo.select("td[class=selection starter-max]").first();
					if (max != null) {
						try {
							Element priceDetails = max.select("input[class=radio]").first();
							///0~RY~RLECORY~5002~~Both~X|BL~ 791~ ~~HAN~05/19/2013 08:35~SGN~05/19/2013 10:40
							String value = priceDetails.attr("value");
							String[] splitValues = value.split("[~\\|]");
							priceOption = new AirFarePriceOption();
							priceOption.setClassCode(splitValues[1]);
							priceOption.setClassName(max.attr("bundletype"));
							priceOption.setReference(value);

							// ADT
							long price = (long) Double.parseDouble(priceDetails.attr("bundle-fare-adt"));
							long priceADTFees = (long) Double.parseDouble(priceDetails.attr("data-discfees-adt"));
							priceOption.add(new AirPassengerTypePrice(AirPassengerType.ADT, price + priceADTFees));
							// CHD
							if (quantityCHD > 0) {
								price = (long) Double.parseDouble(priceDetails.attr("bundle-fare-chd"));
								long priceCHDFees = (long) Double.parseDouble(priceDetails.attr("data-discfees-chd"));
								priceOption.add(new AirPassengerTypePrice(AirPassengerType.CHD, price + priceCHDFees));
							}
							// INF
							if (quantityINF > 0) {
								priceOption.add(new AirPassengerTypePrice(AirPassengerType.INF, 0));
							}
							priceInfo.add(priceOption);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					result.add(priceInfo);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}		
		return result;
	}

	private void jetStarBookLogin() throws Exception{
		if (jetStarBook == null)
			jetStarBook = new JetstarBook();			
		String urlLogin = baseUrl + "TradeLoginAgent.aspx?culture=vi-VN";
		Connection.Response resGetLogin = jetStarBook.getProcessLogin(urlLogin);
		URL urlResult = resGetLogin.url();
		if (!urlResult.toString().equals(urlLogin))
			throw new Exception("Get_BookLogin Exception");
		
		ArrayList<String > list = jetStarBook.getDocumentJsoupGenerator((Connection.Response) resGetLogin);
		viewState = list.get(0);
		sessId = list.get(1);

		String viewstateEgenerator = list.get(2);	
		Connection.Response resPostLogin = jetStarBook.postProcess(urlLogin,jetStarBook.createLoginFormParams(user, password, viewstateEgenerator, viewState),sessId);		
		urlResult = resPostLogin.url();								
		if (!urlResult.toString().equals(baseUrl + "TradeSalesHome.aspx"))	{
			logger.info("jetstarBook: " + "Post_BookLogin Exception");
			throw new Exception("Post_BookLogin Exception");   
		}
	}

	private void jetStarBookSearch(AirItinerary itinerary) throws Exception{
		String urlSearch = baseUrl + "TradeSalesHome.aspx";
		Connection.Response resGetSearch = jetStarBook.getProcess(urlSearch,sessId);		
		URL urlResult = resGetSearch.url();
		if (!urlResult.toString().equals(urlSearch))
			throw new Exception("Get_BookSearch Exception");

		ArrayList<String > list = jetStarBook.getDocumentJsoup((Connection.Response) resGetSearch);
		viewState = list.get(0);		
		UpdateFarePriceCommand request = new UpdateFarePriceCommand();
		ArrayList<AirFareInfo> listAirFareInfo = (ArrayList<AirFareInfo>)itinerary.getFareInfos();
		List<AirPassengerInfo> listAirPassengerInfo = (List<AirPassengerInfo>)itinerary.getPassengerInfos();
		request.setOriginDestinationInfos(listAirFareInfo);
		request.setPassengerInfos(jetStarBook.getAirPassengerTypeQuantity(listAirPassengerInfo));
		Connection.Response resPostSearch = jetStarBook.postProcess(urlSearch,jetStarBook.createSearchFormParams(request,viewState),sessId);		
		urlResult = resPostSearch.url();
		if (!urlResult.toString().equals(baseUrl + "AgentSelect.aspx"))		{
			logger.info("jetstarBook: " + "Post_BookSearch Exception");
			throw new Exception("Post_BookSearch Exception"); 
		}
			
	}

	private void jetStarBookAgentSelect(AirItinerary itinerary) throws Exception{
		String urlAgentSelect = baseUrl + "AgentSelect.aspx";
		Connection.Response resurlGetAgentSelect = jetStarBook.getProcess(urlAgentSelect,sessId);
		URL urlResult = resurlGetAgentSelect.url();
		if (!urlResult.toString().equals(urlAgentSelect))
			throw new Exception("Get_BookAgentSelect Exception");

		ArrayList<String > list = jetStarBook.getDocumentJsoup((Connection.Response) resurlGetAgentSelect);
		viewState = list.get(0);	
		
		//String referenceGo = itinerary.getFareInfos().get(0).getFlightReference();
		String referenceGo = itinerary.getFareInfos().get(0).getReference();
		String referenceBack = "";
		if(itinerary.getFareInfos().size()==2)
			referenceBack = itinerary.getFareInfos().get(1).getReference();	
		List<AirFareInfo> fareInfos = itinerary.getFareInfos();				
		final int customers = itinerary.getPassengerInfos().size();		
		int roundTrip = 1;
		ArrayList<AirFareInfo> listAirFareInfo = (ArrayList<AirFareInfo>)itinerary.getFareInfos();
		List<AirPassengerInfo> listAirPassengerInfo = (List<AirPassengerInfo>)itinerary.getPassengerInfos();
		UpdateFarePriceCommand request = new UpdateFarePriceCommand();
		request.setOriginDestinationInfos(listAirFareInfo);
		request.setPassengerInfos(jetStarBook.getAirPassengerTypeQuantity(listAirPassengerInfo));
		if (request.getOriginDestinationInfos().size() == 2)
			roundTrip = 2;		
		List<AirExtraService> extraServices = itinerary.getExtraServices();				
		Connection.Response resPostAgentSelect = jetStarBook.postProcess(urlAgentSelect,jetStarBook.createAgentSelectParams(referenceGo,referenceBack,fareInfos,customers,roundTrip,extraServices,viewState),sessId);		 
		urlResult = resPostAgentSelect.url();
		if (!urlResult.toString().equals(baseUrl + "AgentPassenger.aspx"))	{
			logger.info("jetstarBook: " + "Post_BookAgentSelect Exception");
			throw new Exception("Post_BookAgentSelect Exception");
		}
	}
	
	private boolean jetStarBookAgentPassenger(AirItinerary itinerary,long sumPrices) throws Exception{		
		String urlAgentPassenger = baseUrl + "AgentPassenger.aspx";
		Connection.Response resurlGetAgentPassenger = jetStarBook.getProcess(urlAgentPassenger,sessId);
		URL urlResult = resurlGetAgentPassenger.url();
		if (!urlResult.toString().equals(urlAgentPassenger))
			throw new Exception("Get_BookAgentPassenger Exception");	
		// Compare prices
		Document document = Jsoup.parse(resurlGetAgentPassenger.body());	
		Elements elements = document.getElementsByClass("cash");
		long sumAmount = 0;
		for (Element element : elements) {
			String amount = element.getElementsByTag("span").text().replaceAll("[^\\d]","");		
			sumAmount += Long.valueOf(amount).longValue();
		}					
		if(sumAmount != sumPrices){
			return false;
		}								
		ArrayList<String >  list = jetStarBook.getDocumentJsoup((Connection.Response) resurlGetAgentPassenger);
		viewState = list.get(0);	
		document = Jsoup.parse(resurlGetAgentPassenger.body());			
		totalPrice = document.getElementById("total_price").val();		
		ContactInfo contactInfo = itinerary.getContactInfo();	
		AgentInfo agentInfo = itinerary.getAgentInfo();			
		List<AirPassengerInfo> passengerInfos = itinerary.getPassengerInfos();	
		
		UpdateFarePriceCommand request = new UpdateFarePriceCommand();
		ArrayList<AirFareInfo> listAirFareInfo = (ArrayList<AirFareInfo>)itinerary.getFareInfos();
		List<AirPassengerInfo> listAirPassengerInfo = (List<AirPassengerInfo>)itinerary.getPassengerInfos();
		request.setOriginDestinationInfos(listAirFareInfo);
		request.setPassengerInfos(jetStarBook.getAirPassengerTypeQuantity(listAirPassengerInfo));
		int roundTrip = 1;
		if (request.getOriginDestinationInfos().size() == 2)
			roundTrip = 2;
		Connection.Response resPostAgentPassenger = jetStarBook.postProcess(urlAgentPassenger,jetStarBook.createAgentPassengerParams(request,roundTrip,agentInfo,contactInfo,passengerInfos,totalPrice,viewState),sessId);
		urlResult = resPostAgentPassenger.url();		
		if (!urlResult.toString().equals(baseUrl + "AgentPay.aspx"))	{
			logger.info("jetstarBook: " + "Post_BookAgentPassenger Exception");
			throw new Exception("Post_BookAgentPassenger Exception");
		}		
		return true;
	}

	private void jetStarBookAgentPay() throws Exception{
		String urlAgentPay = baseUrl + "AgentPay.aspx";
		Connection.Response resurlGetAgentPay = jetStarBook.getProcess(urlAgentPay,sessId);				
		URL urlResult = resurlGetAgentPay.url();
		if (!urlResult.toString().equals(urlAgentPay))
			throw new Exception("Get_BookAgentPay Exception");
		ArrayList<String >  list = jetStarBook.getDocumentJsoup((Connection.Response) resurlGetAgentPay);
		viewState = list.get(0);	
		
		Connection.Response resPostAgentPay  = jetStarBook.postProcess(urlAgentPay,jetStarBook.createAgentPayParams(totalPrice, viewState),sessId);		
		urlResult = resPostAgentPay.url();		
		if (!urlResult.toString().equals(baseUrl + "Wait.aspx"))	{
			logger.info("jetstarBook: " + "Post_BookAgentPay Exception");
			throw new Exception("Post_BookAgentPay Exception");  
		}			
	}
				
	private BookResult jetStarBookItinerary() throws Exception{		
		String urlWait = baseUrl + "Wait.aspx";
		Connection.Response resurlGetWait = jetStarBook.getProcess(urlWait,sessId);			
		URL urlResult = resurlGetWait.url();	
		if (!urlResult.toString().equals(baseUrl + "htl2-Itinerary.aspx")){
			logger.info("jetstarBook: " + "Get_BookAgentWait Exception");
			throw new Exception("Get_BookAgentWait Exception");
		}
											
		String urlItinerary = baseUrl + "htl2-Itinerary.aspx";
		Connection.Response resurlGetItinerary = jetStarBook.getProcess(urlItinerary,sessId);	
		urlResult = resurlGetItinerary.url();	
		if (!urlResult.toString().equals(baseUrl + "htl2-Itinerary.aspx"))
			throw new Exception("Get_BookAgentItinerar Exception");
		
		Document document = Jsoup.parse(resurlGetItinerary.body());							
		String bookingId = document.select("div#datalayer-itinerary-data").attr("data-transactionpnr").toString();
		String amounts = document.select("div#datalayer-itinerary-data").attr("data-bookingtotalprice").toString().replace(".", "-"); 			
		String []parts = amounts.split("-");
		String amount= parts[0];		
		long sumAmount = Long.parseLong(amount);		
		BookResult result = null;
		if(bookingId != ""){
			result = new BookResult();
			result.setReservationCode(bookingId);
			result.setAmount(sumAmount);				
		}
		return result;	
	}

	public BookResult bookJetStar(AirItinerary itinerary,long sumPrices) throws Exception {	
		logger.info("jetstarBook: function login");
		jetStarBookLogin();
		logger.info("jetstarBook: function search");
		jetStarBookSearch(itinerary);
		logger.info("jetstarBook: function AgentSelect");
		jetStarBookAgentSelect(itinerary);
		logger.info("jetstarBook: function AgentPassenger");
		boolean comparePrice = jetStarBookAgentPassenger(itinerary,sumPrices);
		if(comparePrice == false){
			BookResult result = new BookResult();				
			result.setReservationCode("1111111"); 
			result.setAmount(0000000);
			return result;
		}
		logger.info("jetstarBook: function AgentPay");
		jetStarBookAgentPay();   
		logger.info("jetstarBook: function Itinerary");
		return jetStarBookItinerary();		
	}
	
	public BookResult jetstarSearchBookId(AirItinerary itinerary) throws Exception{
		BookResult result = null;
		AirItinerary itinerarySearch = new AirItinerary();
		logger.info("jetstarSearchBookingId: function Login");
		jetStarPayLogin();
		logger.info("jetstarSearchBookingId: function searchBookingList");
		String html = jetStarSearchBookingList(itinerary);
		itinerarySearch = extractAirItinerary(html);
		logger.info("jetstarSearchBookingId: finish extractInfo");
		if(itinerarySearch.getTicketingInfo().getAmount() !=0){
			result = new BookResult();
			result.setAmount(10000000);			
			JetstarSearchInfo searchInfo = new JetstarSearchInfo();
			searchInfo.swapItinerary(itinerary,itinerarySearch);
		}		
		return result;
	}
	
	private String jetStarSearchBookingList(AirItinerary itinerary) throws Exception{
		String urlSearch = baseUrl + "TradeSalesHome.aspx";
		Connection.Response resGetSearch = jetStarPay.getProcess(urlSearch,sessId);		
		URL urlResult = resGetSearch.url();
		if (!urlResult.toString().equals(urlSearch))
			throw new Exception("Get_PaySearch Exception");
	
		String urlBookingList= baseUrl + "AgentBookingList.aspx#paymentDues";
		Connection.Response urlGetBookingList = jetStarPay.getProcess(urlBookingList,sessId);		
		urlResult = urlGetBookingList.url();
		if (!urlResult.toString().equals(urlBookingList))
			throw new Exception("Get_PayBookingList Exception");	
		AirTicketingInfo ticketInfo = itinerary.getTicketingInfo();			
		String reservationCode = ticketInfo.getReservationCode();			
		String dateCurrent = date_ddMMyyyy.format(new Date());
		String dateNew = createDate(dateCurrent);
		// process datetime
		String urlBookingDue = baseUrl + "AgentBookingDetails.aspx?RecordLocator="+reservationCode+"&status=status-hold&expirationDate="+dateNew+"&currentTab=#paymentDues";		
		Connection.Response urlGetBookingDue= jetStarPay.getProcess(urlBookingDue,sessId);	
		urlResult = urlGetBookingDue.url();				
		if (!urlResult.toString().equals(urlBookingDue))
			throw new Exception("Get_PayBookingDue Exception");			
		return urlGetBookingDue.body();
	}
	
	@SuppressWarnings({ "rawtypes", "unused" })
	private AirItinerary extractAirItinerary(String htmlContent) throws Exception{
		logger.info("jetstarSearchBookingId: begin extract data");
		JetstarSearchInfo searchInfo = new JetstarSearchInfo();
		AirItinerary itinerary = new AirItinerary();
		DateFormat dateFormatFull = new SimpleDateFormat("HH:mm'T'dd/MM/yyyy", Locale.US);
		DateFormat dateFormatExpert = new SimpleDateFormat("dd-MM-yyyy");	
		try {
			Document doc = Jsoup.parse(htmlContent, "UTF-8");					
			Elements divdetails = doc.select("div[id=booking-summary]");		
			// PassengerInfos
			List<AirPassengerInfo> passengerInfos = new ArrayList<AirPassengerInfo>();		
			Elements divcontainer = divdetails.select("div[id=data-container]");
			Elements infos = divcontainer.select("dl > ul > li");
			AirPassengerInfo passengerInfo = null;											
			for(Element info:infos){
				passengerInfo = new AirPassengerInfo();
				String [] infoPart= info.text().substring(2).replace(".", "-").split("-");				
				passengerInfo.setPassengerType(AirPassengerType.ADT);
				passengerInfo.setGender(Gender.FEMALE);
				if(infoPart[0].replaceAll("\\s","").equals("Mr"))
					passengerInfo.setGender(Gender.MALE);				
				passengerInfo.setLastName(searchInfo.getLastName(infoPart[1]));				
				String []partInf = searchInfo.getFirstName(infoPart[1]).split(",");				
				passengerInfo.setFirstName(partInf[0]);
				passengerInfos.add(passengerInfo);
				// process inf
				if(partInf.length == 2){
					passengerInfo = new AirPassengerInfo();
					passengerInfo.setPassengerType(AirPassengerType.INF);
					String []processInf = partInf[1].replace(":", "-").split("-");
					passengerInfo.setLastName(searchInfo.getLastName(processInf[1]).replaceAll("\\s",""));	
					passengerInfo.setFirstName(searchInfo.getFirstName(processInf[1]).replaceAll("\\s",""));
					passengerInfos.add(passengerInfo);
				}			
			}						
			// TicketInfo
			Elements divConfirmation = divdetails.select("div[id=confirmation]");
			Elements divPrice = divConfirmation.select("dl[class=total-price]");
			String amount = divPrice.select("dd > span").text().replaceAll("[^\\d]","");	
			String dayExpert = divdetails.select("div[id=booking-on-hold] > div > p > strong").text().split(" ")[0].replaceAll("\\s","");		
			DateTime datetime = new DateTime();					
			String datetiemExpert = ""+(Integer.parseInt(dayExpert)-1) +"-" +datetime.getMonthOfYear() + "-" +datetime.getYear();
			AirTicketingInfo ticketingInfo = new AirTicketingInfo();
			ticketingInfo.setAmount(Long.valueOf(amount).longValue());	
			ticketingInfo.setUpdatedDate(dateFormatExpert.parse(datetiemExpert));
			itinerary.setTicketingInfo(ticketingInfo);			
			// Contact
			ContactInfo contactInfo = new ContactInfo();
			Elements divContact = divConfirmation.select("dl[class=contact-details]");
			String mobileContact  =  divContact.select("dd > dl > dd.summary-contact-mobile").text();
			contactInfo.setMobile(mobileContact);
			String emailContact = divContact.select("dd > dl > dd.summary-contact-pc-email").text();
			contactInfo.setEmail(emailContact);
			itinerary.setContactInfo(contactInfo);
			// FareInfos	
			List<AirFareInfo> fareInfos = new ArrayList<AirFareInfo>();				
			Elements scriptElements = doc.getElementsByTag("script");
			Elements element = scriptElements.tagName("bookingJson");	
			String getElementJson = element.get(1).toString();
			String []partJson = getElementJson.split("var bookingJson =");
			String []getDataJson = partJson[1].split(";");		
			String data = getDataJson[0].replaceAll("(\\r|\\n)", "").replaceAll("\"", "'").replaceAll("'", "\"");
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(data);
			// get an array from the JSON object
			JSONArray slideContent = (JSONArray) jsonObject.get("journeys");
			JSONObject payment = null;
			JSONArray legsDeparture = null;
			JSONArray legsReturn = null;
			Iterator i = slideContent.iterator();
			int indexSlideContent = 0;
			while (i.hasNext()) {
				JSONObject slide = (JSONObject) i.next();
				payment = (JSONObject) slide.get("payment");
				if(indexSlideContent ==0)
					legsDeparture = (JSONArray) slide.get("legs");
				else
					legsReturn = (JSONArray) slide.get("legs");
				indexSlideContent++;
			}		
			int chd = searchInfo.countCHD(payment);
			AirFareInfo fareInfo = searchInfo.getFareInfo(legsDeparture);
			String haveReturn = "0";
			List<AirExtraService> extraServicesDepart = searchInfo.getExtraService(legsDeparture,haveReturn);
			fareInfos.add(fareInfo);
			if(slideContent.size() == 2){
				fareInfo = searchInfo.getFareInfo(legsReturn);
				haveReturn = "1";
				List<AirExtraService> extraServicesReturn = searchInfo.getExtraService(legsReturn,haveReturn);
				extraServicesDepart = searchInfo.addExtraService(extraServicesDepart, extraServicesReturn);
				fareInfos.add(fareInfo);
			}		
			
			itinerary.setExtraServices(extraServicesDepart);
			itinerary.setFareInfos(fareInfos);
			itinerary.setPassengerInfos(searchInfo.setPassengerCHD(passengerInfos, chd));
		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw new Exception(AirErrorType.PARSE_ERROR + ":        " + e);
		}
		return itinerary;
	}
	
	private void jetStarPayLogin() throws Exception{
		if (jetStarPay == null)
			jetStarPay = new JetstarPay();			
		String urlLogin = baseUrl + "TradeLoginAgent.aspx?culture=vi-VN";
		Connection.Response resGetLogin = jetStarPay.getProcessLogin(urlLogin);
		URL urlResult = resGetLogin.url();
		if (!urlResult.toString().equals(urlLogin))
			throw new Exception("Get_PayLogin Exception");

		ArrayList<String > list = jetStarPay.getDocumentJsoupEgenerator((Connection.Response) resGetLogin);
		viewState = list.get(0);
		sessId = list.get(1);
		String viewstateEgenerator = list.get(2);	
		Connection.Response resPostLogin = jetStarPay.postProcess(urlLogin,jetStarPay.createLoginFormParams(user, password, viewstateEgenerator, viewState),sessId);		
		urlResult = resPostLogin.url();
		if (!urlResult.toString().equals(baseUrl + "TradeSalesHome.aspx"))			
			throw new Exception("Post_PayLogin Exception"); 
	}
	private void jetStarBookingList(AirItinerary itinerary) throws Exception{		
		String urlSearch = baseUrl + "TradeSalesHome.aspx";
		Connection.Response resGetSearch = jetStarPay.getProcess(urlSearch,sessId);		
		URL urlResult = resGetSearch.url();
		if (!urlResult.toString().equals(urlSearch))
			throw new Exception("Get_PaySearch Exception");
	
		String urlBookingList= baseUrl + "AgentBookingList.aspx#paymentDues";
		Connection.Response urlGetBookingList = jetStarPay.getProcess(urlBookingList,sessId);		
		urlResult = urlGetBookingList.url();
		if (!urlResult.toString().equals(urlBookingList))
			throw new Exception("Get_PayBookingList Exception");			
		AirTicketingInfo ticketInfo = itinerary.getTicketingInfo();		
		
//		ticketInfo.setUpdatedDate(dateFormat.parse("2014-12-22T08:01:30.311Z"));
//		ticketInfo.setReservationCode("L6LHPY");	
		
		String reservationCode = ticketInfo.getReservationCode();			
		String dateCurrent = date_ddMMyyyy.format(ticketInfo.getUpdatedDate());
		String dateNew = createDate(dateCurrent);
	
		String urlBookingDue = baseUrl + "AgentBookingDetails.aspx?RecordLocator="+reservationCode+"&status=status-hold&expirationDate="+dateNew+"&currentTab=#paymentDues";
		Connection.Response urlGetBookingDue= jetStarPay.getProcess(urlBookingDue,sessId);		
		urlResult = urlGetBookingDue.url();		
		if (!urlResult.toString().equals(urlBookingDue))
			throw new Exception("Get_PayBookingDue Exception");			
	}
	
	private BuyResult jetStarC3PaymentTEST(AirItinerary itinerary) throws Exception{	
		BuyResult result = new BuyResult("");
		String urlPayment= "https://booknow.jetstar.com/C3Payment.aspx";	
		Connection.Response urlGetPayment = jetStarPay.getProcess(urlPayment,sessId);		
		URL urlResult = urlGetPayment.url();		
		if (!urlResult.toString().equals(baseUrl + "C3Payment.aspx")){
			String[] ticketNumbers = new String[10];					
			String ticketNumber = "0000000";  
			ticketNumbers[0] = 	ticketNumber;
			result = new BuyResult(ticketNumbers);				
		}				
		else{				
			String[] ticketNumbers = new String[10];					
			String ticketNumber = "1234567";  
			ticketNumbers[0] = 	ticketNumber;
			result = new BuyResult(ticketNumbers);			
		}					
		return result;	
	}
	private String createDate(String dateOld){
		String dateNew = "";
		String tempDate = dateOld.replace("/", "-");
		String[] parts = tempDate.split("-");
		int temp = Integer.parseInt(parts[0]);
		temp++;
		dateNew = temp +"/" + parts[1] + "/" + parts[2];
		return dateNew;
	}	
	private void jetStarC3Payment() throws Exception{
		String urlPayment= baseUrl + "C3Payment.aspx";	
		Connection.Response urlGetPayment = jetStarPay.getProcess(urlPayment,sessId);		
		URL urlResult = urlGetPayment.url();
		if (!urlResult.toString().equals(baseUrl + "C3Payment.aspx"))
			throw new Exception("Get_Payment Exception");
				
		ArrayList<String >  list = jetStarPay.getDocumentJsoup((Connection.Response) urlGetPayment);
		viewState = list.get(0);			
		Connection.Response resPostPayment  = jetStarPay.postProcess(urlPayment,jetStarPay.createPaymentFormParams(viewState),sessId);		
		urlResult = resPostPayment.url();	
		// 
//		if (!urlResult.toString().equals(baseUrl + "C3Wait.aspx"))			
//			throw new Exception("Post_BookAgentPay Exception"); 			
	}
	
	private void jetStarC3Wait() throws Exception{
		String urlWaitRef = baseUrl + "C3Wait.aspx";	
		Connection.Response urlGetWaitRef = jetStarPay.getProcess(urlWaitRef,sessId);		
		URL urlResult = urlGetWaitRef.url();
		// 
//		if (!urlResult.toString().equals(baseUrl + "C3Itinerary.aspx"))
//			throw new Exception("Get_Wait Exception");
		
		String urlWait =  "https://booknow.jetstar.com/C3Wait.aspx";
		Connection.Response urlGetWait = jetStarPay.getProcess(urlWait,sessId);
		urlResult = urlGetWait.url();
		// 
		
	}
	
	private BuyResult jetStarC3Itinerary() throws Exception{
		BuyResult result = null;
		String urlItinerary = baseUrl + "C3Itinerary.aspx";	
		Connection.Response urlGetItinerary = jetStarPay.getProcess(urlItinerary,sessId);		
		URL urlResult = urlGetItinerary.url();
		// Begin lock // https://booknow.jetstar.com/C3Itinerary.aspx		
		String urlItineraryRef =  "https://booknow.jetstar.com/C3Itinerary.aspx";	
		Connection.Response urlGetItineraryRef = jetStarPay.getProcess(urlItineraryRef,sessId);	
		urlResult = urlGetItineraryRef.url();
		// 	
		if (!urlResult.toString().equals(baseUrl + "C3Itinerary.aspx"))
			return result;
		else{
			result = new BuyResult("");
			//Document document = Jsoup.parse(urlGetItinerary.body());		
			String[] ticketNumbers = new String[5];
			// get Revertioncode
			//String ticketNumber = document.select("").text();  		
			ticketNumbers[0] = 	"123456789";		
			result = new BuyResult(ticketNumbers);	
			return result;
		}
	}
	
	
	public BuyResult jetStarBuy(AirItinerary itinerary) throws Exception {
		try { 					
			jetStarPayLogin();
			jetStarBookingList(itinerary);	
			// Begin lock
			jetStarC3Payment();
			jetStarC3Wait();
			return jetStarC3Itinerary();			
			// End lock
			
//			return jetStarC3PaymentTEST(itinerary);
			
		} catch (Exception ex) {
			throw ex;
		}
	}
}
