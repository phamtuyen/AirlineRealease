package com.mbv.ticketsystem.check;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Main {

	public static void main(String[] args) throws IOException {
		
		// login
		Connection.Response resGetLogin = Jsoup.connect("https://ameliaweb5.intelisys.ca/VietJet/sitelogin.aspx")
				.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0")
				.method(Method.GET)
				.timeout(300000)
				.execute();
		
		Document doc = Jsoup.parse(resGetLogin.body());
		String viewstate = doc.select("input[name=__VIEWSTATE]").attr("value");		
		String sessId = resGetLogin.cookies().get("ASP.NET_SessionId");
		String viewstategen = doc.select("input[name=__VIEWSTATEGENERATOR]").attr("value");
		
		HashMap<String, String> paramsLogin = new HashMap<String, String>();
		paramsLogin.put("DebugID", "61");
		paramsLogin.put("SesID", "");
		paramsLogin.put("__VIEWSTATE", viewstate);
		paramsLogin.put("__VIEWSTATEGENERATOR", viewstategen);
		paramsLogin.put("txtAgentID", "AG38197121");
		paramsLogin.put("txtAgentPswd", "Mobivi123456");
		
		Connection.Response resPostLogin = Jsoup.connect("https://ameliaweb5.intelisys.ca/VietJet/sitelogin.aspx")
				.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0")
				.data(paramsLogin)
				.cookie("ASP.NET_SessionId", sessId)
				.method(Method.POST)
				.timeout(300000)
				.execute();
		System.out.println(resPostLogin.url());
		
		// AgentOptions
		Connection.Response resGetOption= Jsoup.connect("https://ameliaweb5.intelisys.ca/VietJet/AgentOptions.aspx?lang=en&st=sl&sesid=")
				.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0")
				.cookie("ASP.NET_SessionId", sessId)
				.method(Method.GET)
				.timeout(300000)
				.execute();
		
		doc = Jsoup.parse(resGetOption.body());
		viewstate = doc.select("input[name=__VIEWSTATE]").attr("value");		
		viewstategen = doc.select("input[name=__VIEWSTATEGENERATOR]").attr("value");
		
		HashMap<String, String> paramsOption = new HashMap<String, String>();
		paramsOption.put("__VIEWSTATE", viewstate);
		paramsOption.put("__VIEWSTATEGENERATOR", viewstategen);
		paramsOption.put("SesID", "");
		paramsOption.put("DebugID", "77");
		paramsOption.put("button", "bookflight");
		
		Connection.Response resPostOption= Jsoup.connect("https://ameliaweb5.intelisys.ca/VietJet/AgentOptions.aspx?lang=en&st=sl&sesid=")
				.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0")
				.data(paramsOption)
				.cookie("ASP.NET_SessionId", sessId)
				.method(Method.POST)
				.timeout(300000)
				.execute();
		System.out.println(resPostOption.url());
		
		// viewflight
		Connection.Response resGetViewflight= Jsoup.connect("https://ameliaweb5.intelisys.ca/VietJet/ViewFlights.aspx?lang=vi")
				.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0")
				.cookie("ASP.NET_SessionId", sessId)
				.method(Method.GET)
				.timeout(300000)
				.execute();
		
		doc = Jsoup.parse(resGetViewflight.body());
		viewstate = doc.select("input[name=__VIEWSTATE]").attr("value");		
		viewstategen = doc.select("input[name=__VIEWSTATEGENERATOR]").attr("value");
		
		HashMap<String, String> paramsViewflight = new HashMap<String, String>();
		paramsViewflight.put("__VIEWSTATE", viewstate);
		paramsViewflight.put("__VIEWSTATEGENERATOR", viewstategen);
		paramsViewflight.put("SesID", "");
		paramsViewflight.put("DebugID", "25");
		paramsViewflight.put("button", "vfto");
		paramsViewflight.put("dlstDepDate_Day", "01");
		paramsViewflight.put("dlstDepDate_Month", "2015/08");
		paramsViewflight.put("dlstRetDate_Day", "01");
		paramsViewflight.put("dlstRetDate_Month", "2015/08");
		paramsViewflight.put("lstDepDateRange", "0");
		paramsViewflight.put("lstRetDateRange", "0");
		paramsViewflight.put("chkRoundTrip", "");
		paramsViewflight.put("lstOrigAP", "HAN");
		paramsViewflight.put("lstDestAP", "SGN");
		paramsViewflight.put("departure1", "01/08/2015");
		paramsViewflight.put("departTime1", "0000");
		paramsViewflight.put("departure2", "01/08/2015");
		paramsViewflight.put("departTime2", "0000");
		paramsViewflight.put("lstLvlService", "1");
		paramsViewflight.put("lstResCurrency", "VND");
		paramsViewflight.put("txtNumAdults", "1");
		paramsViewflight.put("txtNumChildren", "0");
		paramsViewflight.put("txtNumInfants", "0");
		paramsViewflight.put("lstCompanyList", "722ƒCTY VIET NHANH");
		paramsViewflight.put("txtPONumber", "");
		
		Connection.Response resPostViewfight= Jsoup.connect("https://ameliaweb5.intelisys.ca/VietJet/ViewFlights.aspx?lang=vi")
				.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0")
				.data(paramsViewflight)
				.cookie("ASP.NET_SessionId", sessId)
				.method(Method.POST)
				.timeout(300000)
				.execute();
		System.out.println(resPostViewfight.url());
		
		// TravelOptions
		Connection.Response resGetTravelOptions= Jsoup.connect("https://ameliaweb5.intelisys.ca/VietJet/TravelOptions.aspx?lang=vi&sesid=")
				.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0")
				.cookie("ASP.NET_SessionId", sessId)
				.method(Method.GET)
				.timeout(300000)
				.execute();
		
		doc = Jsoup.parse(resGetTravelOptions.body());
		viewstate = doc.select("input[name=__VIEWSTATE]").attr("value");		
		viewstategen = doc.select("input[name=__VIEWSTATEGENERATOR]").attr("value");
		
		HashMap<String, String> paramsTravelOptions = new HashMap<String, String>();
		paramsTravelOptions.put("__VIEWSTATE", viewstate);
		paramsTravelOptions.put("__VIEWSTATEGENERATOR", viewstategen);
		paramsTravelOptions.put("button", "continue");
		paramsTravelOptions.put("SesID", "");
		paramsTravelOptions.put("DebugID", "25");
		paramsTravelOptions.put("PN", "");
		paramsTravelOptions.put("gridTravelOptDep", "1,J_Eco,O");
		
		
		Connection.Response resPostTravelOptions= Jsoup.connect("https://ameliaweb5.intelisys.ca/VietJet/TravelOptions.aspx?lang=vi&sesid=")
				.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0")
				.data(paramsTravelOptions)
				.cookie("ASP.NET_SessionId", sessId)
				.method(Method.POST)
				.timeout(300000)
				.execute();
		System.out.println(resPostTravelOptions.url());
		
		// Details
		// https://ameliaweb5.intelisys.ca/VietJet/Details.aspx?lang=vi&sesid=
		Connection.Response resGetDetail= Jsoup.connect("https://ameliaweb5.intelisys.ca/VietJet/Details.aspx?lang=vi&sesid=")
				.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0")
				.cookie("ASP.NET_SessionId", sessId)
				.method(Method.GET)
				.timeout(300000)
				.execute();
		
		doc = Jsoup.parse(resGetDetail.body());
		viewstate = doc.select("input[name=__VIEWSTATE]").attr("value");		
		viewstategen = doc.select("input[name=__VIEWSTATEGENERATOR]").attr("value");
		
		HashMap<String, String> paramsDetail = new HashMap<String, String>();
		paramsDetail.put("__VIEWSTATE", viewstate);
		paramsDetail.put("__VIEWSTATEGENERATOR", viewstategen);
		paramsDetail.put("SesID", "");
		paramsDetail.put("DebugID", "25");
		paramsDetail.put("button", "continue");
		paramsDetail.put("txtPax1_Gender", "M");
		paramsDetail.put("txtPax1_LName", "Nguyen");
		paramsDetail.put("txtPax1_FName", "Hai Ha");
		paramsDetail.put("txtPax1_Addr1", "cau giay");
		paramsDetail.put("txtPax1_City", "Ha Noi");
		paramsDetail.put("txtPax1_Ctry", "234");
		paramsDetail.put("txtPax1_Prov", "-1");
		paramsDetail.put("txtPax1_EMail", "nguyenhaiha.tt@gmail.com");
		paramsDetail.put("txtPax1_DOB_Day", "");
		paramsDetail.put("txtPax1_DOB_Month", "");
		paramsDetail.put("txtPax1_DOB_Year", "");
		paramsDetail.put("txtPax1_Phone2", "0965292512");
		paramsDetail.put("txtPax1_Phone1", "");
		paramsDetail.put("txtPax1_Passport", "");
		paramsDetail.put("dlstPax1_PassportExpiry_Day", "");
		paramsDetail.put("dlstPax1_PassportExpiry_Month", "");
		paramsDetail.put("lstPax1_PassportCtry", "");
		paramsDetail.put("txtPax1_Nationality", "");
		paramsDetail.put("hidPax1_Search", "-1");
		
		Connection.Response resPostDetail= Jsoup.connect("https://ameliaweb5.intelisys.ca/VietJet/Details.aspx?lang=vi&sesid=")
				.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0")
				.data(paramsDetail)
				.cookie("ASP.NET_SessionId", sessId)
				.method(Method.POST)
				.timeout(300000)
				.execute();
		System.out.println(resPostDetail.url());
		
		// 
		// Addonds
		Connection.Response resGetAddonds= Jsoup.connect("https://ameliaweb5.intelisys.ca/VietJet/AddOns.aspx?lang=vi&sesid=")
				.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0")
				.cookie("ASP.NET_SessionId", sessId)
				.method(Method.GET)
				.timeout(300000)
				.execute();
		
		doc = Jsoup.parse(resGetAddonds.body());
		viewstate = doc.select("input[name=__VIEWSTATE]").attr("value");		
		viewstategen = doc.select("input[name=__VIEWSTATEGENERATOR]").attr("value");
		
		HashMap<String, String> paramsAddonds = new HashMap<String, String>();
		paramsAddonds.put("__VIEWSTATE", viewstate);
		paramsAddonds.put("__VIEWSTATEGENERATOR", viewstategen);
		paramsAddonds.put("SesID", "");
		paramsAddonds.put("DebugID", "25");
		paramsAddonds.put("button", "continue");
		paramsAddonds.put("m1th", "2");
		paramsAddonds.put("m1p", "1");
		paramsAddonds.put("m1p1", "");
		paramsAddonds.put("m1p1rpg", "");
		paramsAddonds.put("ctrSeatAssM", "1");
		paramsAddonds.put("ctrSeatAssP", "1");
		paramsAddonds.put("lstPaxItem:-1:1:8:", "-1");
		paramsAddonds.put("-1", "-1");
		
		Connection.Response resPostAddonds= Jsoup.connect("https://ameliaweb5.intelisys.ca/VietJet/AddOns.aspx?lang=vi&sesid=")
				.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0")
				.data(paramsAddonds)
				.cookie("ASP.NET_SessionId", sessId)
				.method(Method.POST)
				.timeout(300000)
				.execute();
		System.out.println(resPostAddonds.url());
		
		// payment
		// https://ameliaweb5.intelisys.ca/VietJet/Payments.aspx?lang=vi&sesid=
		Connection.Response resGetPayment= Jsoup.connect("https://ameliaweb5.intelisys.ca/VietJet/Payments.aspx?lang=vi&sesid=")
				.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0")
				.cookie("ASP.NET_SessionId", sessId)
				.method(Method.GET)
				.timeout(300000)
				.execute();
		
		doc = Jsoup.parse(resGetPayment.body());
		viewstate = doc.select("input[name=__VIEWSTATE]").attr("value");		
		viewstategen = doc.select("input[name=__VIEWSTATEGENERATOR]").attr("value");
		
		HashMap<String, String> paramsPayment = new HashMap<String, String>();
		paramsPayment.put("__VIEWSTATE", viewstate);
		paramsPayment.put("__VIEWSTATEGENERATOR", viewstategen);
		paramsPayment.put("button", "3rd");
		paramsPayment.put("SesID", "");
		paramsPayment.put("DebugID", "25");
		paramsPayment.put("lstPmtType", "5,PL,0,V,0,0,0");
		paramsPayment.put("txtCardNo", "");
		paramsPayment.put("dlstExpiry", "2015/02/28");
		paramsPayment.put("txtCVC", "");
		paramsPayment.put("txtCardholder", "");
		paramsPayment.put("txtAddr1", "");
		paramsPayment.put("txtCity", "");
		paramsPayment.put("txtPCode", "");
		paramsPayment.put("lstCtry", "-1");
		paramsPayment.put("lstProv", "-1");
		paramsPayment.put("txtPhone", "");
		
		Connection.Response resPostPayment= Jsoup.connect("https://ameliaweb5.intelisys.ca/VietJet/Payments.aspx?lang=vi&sesid=")
				.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0")
				.data(paramsPayment)
				.cookie("ASP.NET_SessionId", sessId)
				.method(Method.POST)
				.timeout(300000)
				.execute();
		System.out.println(resPostPayment.url());
		
		// https://ameliaweb5.intelisys.ca/VietJet/Confirm.aspx?lang=vi&sesid=
		// confirm
		Connection.Response resGetConfirm= Jsoup.connect("https://ameliaweb5.intelisys.ca/VietJet/Confirm.aspx?lang=vi&sesid=")
				.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0")
				.cookie("ASP.NET_SessionId", sessId)
				.method(Method.GET)
				.timeout(300000)
				.execute();
		
		doc = Jsoup.parse(resGetConfirm.body());
		viewstate = doc.select("input[name=__VIEWSTATE]").attr("value");		
		viewstategen = doc.select("input[name=__VIEWSTATEGENERATOR]").attr("value");
		
		HashMap<String, String> paramsConfirm = new HashMap<String, String>();
		paramsConfirm.put("__VIEWSTATE", viewstate);
		paramsConfirm.put("__VIEWSTATEGENERATOR", viewstategen);
		paramsConfirm.put("SesID", "");
		paramsConfirm.put("DebugID", "25");
		paramsConfirm.put("button", "continue");
		
		paramsConfirm.put("txtPax1_Gender", "M");
		paramsConfirm.put("txtPax1_LName", "Nguyen");
		paramsConfirm.put("txtPax1_FName", "Hai Ha");
		paramsConfirm.put("ZeroOptions", "");
		
		paramsConfirm.put("chkIAgree", "on");
		
		
		Connection.Response resPostConfirm= Jsoup.connect("https://ameliaweb5.intelisys.ca/VietJet/Confirm.aspx?lang=vi&sesid=")
				.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0")
				.data(paramsConfirm)
				.cookie("ASP.NET_SessionId", sessId)
				.method(Method.POST)
				.timeout(300000)
				.execute();
		System.out.println(resPostConfirm.url());
		
		
		// itinerary
		Connection.Response resGetItinerary = Jsoup.connect("https://ameliaweb5.intelisys.ca/VietJet/Itinerary.aspx?lang=vi&sesid=")
				.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0")
				.cookie("ASP.NET_SessionId", sessId)
				.method(Method.GET)
				.timeout(300000)
				.execute();
		
		doc = Jsoup.parse(resGetItinerary.body());
		String content = doc.html();			
		File file = new File("/home/phamtuyen/parse.txt");
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(content);
		bw.close();	
					
	}

}
























