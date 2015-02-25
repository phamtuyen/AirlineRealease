package com.mbv.ticketsystem.airline.jetstar;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;

public class JetstarPay {
	private static final String loginParams =  "ControlGroupNewTradeLoginAgentView$AgentNewTradeLoginView$";
	private static final String paymentParams = "ControlGroupPaymentView%24PaymentSectionPaymentView%24UpdatePanelPaymentView%24PaymentInputPaymentView%24";
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
				.timeout(3000000)
				.execute();
	}

	public Response getProcess(String url,String sessId) throws Exception{
		return Jsoup.connect(url)
				.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0")
				.cookie("ASP.NET_SessionId", sessId)
				.method(Method.GET)
				.timeout(3000000)
				.execute();
	}

	public ArrayList<String > getDocumentJsoupEgenerator(Response res){
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

	
	private void createParams(HashMap<String, String> params,final String viewstate){		
		params.put("__EVENTTARGET", "");
		params.put("__EVENTARGUMENT", "");
		params.put("__VIEWSTATE", viewstate);		
		params.put("pageToken", "");
	}
	
	
	private void createLoginParams(HashMap<String, String> params,String user,String password,String viewstateEgenerator){
		params.put(loginParams + "TextBoxUserID", user);
		params.put(loginParams + "PasswordFieldPassword", password);	
		params.put(loginParams + "ButtonLogIn", "");
		params.put("__VIEWSTATEGENERATOR", viewstateEgenerator);
	}
	
	public HashMap<String, String> createLoginFormParams(String user,String password,String viewstateEgenerator,final String viewstate){
		HashMap<String, String> params = new HashMap<String, String>();				
		createParams(params,viewstate);
		createLoginParams(params,user,password,viewstateEgenerator);					
		return params;
	}
	
	
	private void createPaymentParams(HashMap<String, String> params){	
				
		params.put(paymentParams + "PaymentMethodDropDown", "AgencyAccount-AG");
		params.put("card_number1", "");
		params.put("card_number2", "");
		params.put("card_number3", "");
		params.put("card_number4", "");
		
		params.put(paymentParams + "TextBoxCC__AccountHolderName", "");
		params.put(paymentParams + "DropDownListEXPDAT_Month", "12");
		params.put(paymentParams + "DropDownListEXPDAT_Year", "2016");
		
		params.put(paymentParams + "TextBoxCC__VerificationCode", "");
		params.put(paymentParams + "TextBoxACCTNO", "");
		params.put("inlineDCCAjaxSucceeded", "false");
		params.put(paymentParams + "TextBoxVoucherAccount_VO_ACCTNO", "");
				
		// 			ControlGroupPaymentView%24AgreementInputPaymentView%24CheckBoxAgreement
		params.put("ControlGroupPaymentView$AgreementInputPaymentView$CheckBoxAgreement", "on");
		// 			ControlGroupPaymentView%24ButtonSubmit
		params.put("ControlGroupPaymentView$ButtonSubmit", "");
	}
	
	public HashMap<String, String> createPaymentFormParams(final String viewstate){
		HashMap<String, String> params = new HashMap<String, String>();		
		createParams(params,viewstate);
		createPaymentParams(params);			
		return params;
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
