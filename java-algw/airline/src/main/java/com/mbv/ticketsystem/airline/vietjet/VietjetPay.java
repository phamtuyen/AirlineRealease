package com.mbv.ticketsystem.airline.vietjet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mbv.ticketsystem.common.base.AgentInfo;
import com.mbv.ticketsystem.common.base.ContactInfo;

@SuppressWarnings("serial")
public class VietjetPay {

	public Response postProcess(String url, HashMap<String, String> params, String sessId) throws Exception{
		try {
			return Jsoup.connect(url)					
					.userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36")
					.data(params)
					.cookie("ASP.NET_SessionId", sessId)
					.method(Method.POST)
					.timeout(30000)
					.execute();
		} catch (Exception ex) {
			throw new Exception("CONNECTION_ERROR");
		}
	}

	public Response getProcessLogin(String url) throws Exception{
		return Jsoup.connect(url)
				.userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36")
				.method(Method.GET)
				.timeout(30000)
				.execute();
	}

	public Response getProcess(String url,String sessId) throws Exception{
		return Jsoup.connect(url)
				.userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36")
				.cookie("ASP.NET_SessionId", sessId)
				.method(Method.GET)
				.timeout(30000)
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
        String button = "editres";
        createParams(params,button,viewstate);
        return params;
    }
	
	private void convertSesrchParams(HashMap<String, String> params,final String ReservationCode,int flag){
		params.put("txtSearchResNum", ReservationCode);
		params.put("txtSearchPhoneNum", "");
		
		params.put("txtSearchResLName", "");
		params.put("txtSearchResFName", "");
		params.put("lstSearchResDepDate_Day", "");

		params.put("lstSearchResDepDate_Month", "");
		params.put("lstSearchResDepAp", "-1");
		params.put("lstSearchResBookFrDate_Day", "");
		params.put("lstSearchResBookFrDate_Month", "");
		params.put("lstSearchResBookToDate_Day", "");
		params.put("lstSearchResBookToDate_Month", "");
		if(flag == 1)
			params.put("gridResSearch", ReservationCode);	
	}
	
	public HashMap<String, String> createSearchResCodeParams(final String reservationCode,final String viewstate) throws  VietjetPayException {
		try {
			HashMap<String, String> params = new HashMap<String, String>();							
			String button = "search";
	        createParams(params,button,viewstate);		
	        int flag = 0;
	        convertSesrchParams(params,reservationCode,flag);			
			return params;
		} catch (Exception ex) {
			throw new VietjetPayException("INVALID_REQUEST");
		}
	}

	public HashMap<String, String> createSearchResContinueParams(final String reservationCode,final String viewstate) throws  VietjetPayException {
		try {
			HashMap<String, String> params = new HashMap<String, String>();			
			String button = "continue";
	        createParams(params,button,viewstate);
	        int flag = 1;
	        convertSesrchParams(params,reservationCode,flag);		
			return params;
		} catch (Exception ex) {
			throw new VietjetPayException("INVALID_REQUEST");
		}
	}

	public HashMap<String, String> createEditRecindFunctionParams(final String viewstate) throws  VietjetPayException {
		try {
			HashMap<String, String> params = new HashMap<String, String>() {
				{
					put("__VIEWSTATE", viewstate);
					put("SesID", "");
					put("DebugID", "25");						
				}
			};						
			return params;
		} catch (Exception ex) {
			throw new VietjetPayException("INVALID_REQUEST");
		}
	}

	private void convertEditResParams(HashMap<String, String> params,final Document document){
		Elements elements = document.getElementsByClass("hdrTable900");		
		for (Element element : elements) {			
			Elements gridPaxEven = element.getElementsByClass("GridPaxEven"); 	
			String chkPax1 = gridPaxEven.get(0).select("input[id=chkPax1]").attr("value");				
			params.put("chkPax1", chkPax1);		
			
			int even = 1;
			for (Element PaxEven : gridPaxEven){						
				String hdnInputPax = PaxEven.select("input[id=hdnInputPax"+even+"]").attr("value");											
				params.put("hdnInputPax"+even+"", hdnInputPax);
				even = even + 2;
			}
							
			Elements gridPaxOdd = element.getElementsByClass("GridPaxOdd"); 	
			int oDD =2;
			for (Element PaxOdd : gridPaxOdd){					
				String hdnInputPax = PaxOdd.select("input[id=hdnInputPax"+oDD+"]").attr("value");	
				params.put("hdnInputPax"+oDD+"", hdnInputPax);
				oDD = oDD + 2;
			}
		}
		params.put("gridEditSegments", "1");
	}
	
	public HashMap<String, String> createEditResParams(final Document document,final String viewstate) throws  VietjetPayException {
		try {
			HashMap<String, String> params = new HashMap<String, String>();		
			String button = "addpayment";
			createParams(params,button,viewstate);
			convertEditResParams(params,document);
			return params;
		} catch (Exception ex) {
			throw new VietjetPayException("INVALID_REQUEST");
		}
	}	
	
	
	private void convertPaymentParams(HashMap<String, String> params,AgentInfo agentInfo,ContactInfo contactInfo){
		params.put("lstPmtType", "4,AG,0,V,0,0,0");
		params.put("txtCardNo", "");
		
		params.put("dlstExpiry", "2016/12/31");     
		params.put("txtCVC", "");
		
		params.put("txtCardholder", agentInfo.getUserId());
		params.put("txtAddr1", "384");
		
		params.put("txtCity", "HCM");
		
		params.put("txtPCode", "");
		params.put("lstProv", "10230");
		params.put("lstCtry", "234");
		params.put("txtPhone", contactInfo.getMobile());
		params.put("txtPaymentEmail", contactInfo.getEmail());
		params.put("lstCompList", "722ƒCTY VIET NHANH");
		params.put("txtPONumber", "");
	}
	
	public HashMap<String, String> createAddPaymentParams(AgentInfo agentInfo,ContactInfo contactInfo,final String viewstate) throws  VietjetPayException {
		try {
			HashMap<String, String> params = new HashMap<String, String>();				
			String button = "account";
			createParams(params,button,viewstate);
			convertPaymentParams(params,agentInfo,contactInfo);				
			return params;
		} catch (Exception ex) {
			throw new VietjetPayException("INVALID_REQUEST");
		}
	}
	
	public HashMap<String, String> createProcessingParams(final String viewstate) throws  VietjetPayException {
		try {
			HashMap<String, String> params = new HashMap<String, String>() {
				{
					put("__VIEWSTATE", viewstate);
					put("SesID", "");
					put("DebugID", "25");						
				}
			};						
			return params;
		} catch (Exception ex) {
			throw new VietjetPayException("INVALID_REQUEST");
		}
	}

	public class VietjetPayException extends Exception {

		private static final long serialVersionUID = 1L;

		public VietjetPayException(String message) {
			super(message);
		}
	}

	public static class BuyResult {
		private String description;
		private List<String> ticketNumbers;

		public BuyResult(String description) {
			this.description = description;
		}

		public BuyResult(String[] ticketNumbers){
			this.ticketNumbers = Arrays.asList(ticketNumbers);
		}

		public String getDescription() {
			return description;
		}

		public boolean isError() {
			return description != null;
		}

		public List<String> getTicketNumbers() {
			return ticketNumbers;
		}
	}
}