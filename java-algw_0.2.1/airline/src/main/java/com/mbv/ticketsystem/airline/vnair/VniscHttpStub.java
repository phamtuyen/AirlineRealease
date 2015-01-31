package com.mbv.ticketsystem.airline.vnair;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.mbv.ticketsystem.airline.httpsupport.Context;
import com.mbv.ticketsystem.airline.httpsupport.HttpHelper;
import com.mbv.ticketsystem.airline.httpsupport.Response;
import com.mbv.ticketsystem.common.airline.AirFareInfo;
import com.mbv.ticketsystem.common.airline.AirFarePriceInfo;
import com.mbv.ticketsystem.common.airline.AirFarePriceInfos;
import com.mbv.ticketsystem.common.airline.AirFarePriceOption;
import com.mbv.ticketsystem.common.airline.AirItinerary;
import com.mbv.ticketsystem.common.airline.AirPassengerInfo;
import com.mbv.ticketsystem.common.airline.AirPassengerType;
import com.mbv.ticketsystem.common.airline.AirPassengerTypePrice;
import com.mbv.ticketsystem.common.airline.UpdateFarePriceCommand;
import com.mbv.ticketsystem.common.base.ContactInfo;
import com.mbv.ticketsystem.common.base.Gender;

public class VniscHttpStub {
    private VniscConfig config;
    private static final String baseUrl = "http://booking.muadi.com.vn/";
    private String sessionId;
    private String _viewstate;
    private Context context;

    public VniscHttpStub(VniscConfig config) {
        this.config = config;
    }

    DateTimeFormatter formatter = DateTimeFormat.forPattern("HHmm dd/MM/yyyy");
    DateTimeFormatter formatter2 = DateTimeFormat.forPattern("ddMM");
    DateFormat birthdate = new SimpleDateFormat("dd/MM/yyyy");

    public AirFarePriceInfos search(UpdateFarePriceCommand request) throws Exception {
        this.context = HttpHelper.createContext();
        submitSearch(request);
        return getFarePrices(request);
    }

    public BookResult book(AirItinerary itinerary, AirFarePriceInfos priceInfos) throws Exception {
        SubmitFlights(itinerary, priceInfos);
        ConfirmFlights();
        SubmitPaxInfo(itinerary.getPassengerInfos(), itinerary.getContactInfo());

        BookResult result = new BookResult();
        result.setReservationCode(GetBookingCode());
        result.setOrderId(getOrderId(result.getReservationCode()));
        return result;
    }

    // http://ticket.mobivi.vn/index.php?route=service/airline/vnisc_payment?ID=xxxx
    // private static Pattern regex =
    // Pattern.compile("/vnisc_payment\\?ID=([0-9]+?)[^0-9]");

    private String getOrderId(String reservationCode) throws Exception {
        URI uri = new URI(baseUrl + "Misc.aspx?Do=CompleteBooking&" + sessionId + "&bookid=" + reservationCode);
        Response response = HttpHelper.POST(uri, context, null);
        Matcher m = config.getRedirectPattern().matcher(response.getBody());
        if (m.find())
            return m.group(1);
        return "";
    }

    private void submitSearch(UpdateFarePriceCommand request) throws Exception {
        URI uri = createSearchUri(request);

        Response response = HttpHelper.GET(uri, context);
        if (!response.getLocation().startsWith("/Booking_Redirect.aspx"))
            throw new Exception("BLABLA");

        String[] params = response.getLocation().split("[\\?=]");
        sessionId = null;
        for (int i = 0; i < params.length && sessionId == null; i++) {
            if ("sid".equals(params[i]) && i + 1 < params.length)
                sessionId = params[i + 1];
        }
        if (sessionId == null)
            throw new Exception("INVALID_SESSIONID");
        sessionId = "sid=" + sessionId;

        response = HttpHelper.GET(new URI(baseUrl + "Booking_Redirect.aspx?" + sessionId), context);
        if (response.getStatusCode() != 200)
            throw new Exception("BLABLA");
        Document document = Jsoup.parse(response.getBody());
        Element element = document.select("input[id=__VIEWSTATE]").first();
        if (element != null)
            _viewstate = element.attr("value");
    }

    private static ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private AirFarePriceInfos getFarePrices(UpdateFarePriceCommand request) throws Exception {
        URI uri = new URI(baseUrl + "Misc.aspx?Do=GetFlightData&" + sessionId);
        AirFarePriceInfos result = new AirFarePriceInfos();

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("type", "depart");

        for (int i = 0; i < request.getOriginDestinationInfos().size(); i++) {
            if (i == 1) {
                params.put("type", "return");
            }
            Response response = HttpHelper.POST(uri, context, params);
            SegmentPriceList prices = mapper.readValue(response.getBody(), SegmentPriceList.class);
            result.addAll(extractPrices(prices, request));
        }
        return result;
    }

    private void SubmitFlights(AirItinerary itinerary, AirFarePriceInfos priceInfos) throws Exception {
        URI uri = new URI(baseUrl + "Booking_Redirect.aspx?" + sessionId);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("__VIEWSTATE", _viewstate);
        params.put("ListBooking$txtFlightBack", "");
        params.put("act", "confirm");
        params.put("ListBooking$btnSubmit", "Confirm Flight");

        boolean isTxtFlightGo = true;
        for (AirFareInfo fareInfo : itinerary.getFareInfos()) {
            AirFarePriceInfo priceInfo = priceInfos.get(fareInfo);
            if (priceInfo == null)
                throw new Exception("FARE_NOT_FOUND");
            AirFarePriceOption option = priceInfo.getAirFarePriceOption(fareInfo.getClassCode());
            if (option == null)
                throw new Exception("FARE_OPTION_NOT_FOUND");
            params.put(fareInfo.getOriginCode() + fareInfo.getDestinationCode(), option.getReference());
            if (isTxtFlightGo) {
                isTxtFlightGo = false;
                params.put("ListBooking$txtFlightGo", option.getReference());
                context.addCookie("booking.muadi.com.vn", "/", "vniscChooseFlight_Go_" + fareInfo.getOriginCode() + fareInfo.getDestinationCode(), priceInfo.getFareInfo().getReference());
            } else {
                params.put("ListBooking$txtFlightBack", option.getReference());
                context.addCookie("booking.muadi.com.vn", "/", "vniscChooseFlight_Back_" + fareInfo.getOriginCode() + fareInfo.getDestinationCode(), priceInfo.getFareInfo().getReference());
            }
        }
        if (!HttpHelper.POST(uri, context, params).getLocation().startsWith("/Booking_Redirect.aspx"))
            throw new Exception("SUBMIT_FLIGHTS_ERROR");
    }

    /*
     * GET /Booking_Redirect.aspx?step=2&sid=E417A6F4B0A55E5D02C5B78F71ED7F0A
     *
     * POST /Booking_Redirect.aspx?step=2&sid=E417A6F4B0A55E5D02C5B78F71ED7F0A
     * __VIEWSTATE /wEPDwUKMTE1Mzg4OTIxNWRk ctl01$btnSubmit Passenger
     * Information
     */
    private void ConfirmFlights() throws Exception {
        URI uri = new URI(baseUrl + "Booking_Redirect.aspx?step=2&" + sessionId);
        Response resp = HttpHelper.GET(uri, context);

        Document document = Jsoup.parse(resp.getBody());
        /*
		 * Element element =
		 * document.select("div[id=alltotal_section]").first(); if (element ==
		 * null) throw new Exception("TOTAL_PRICE_NOT_FOUND"); if
		 * (!element.text().replace(",", "").contains(" " +
		 * Long.toString(totalPrice) + " ")) throw new
		 * Exception("TOTAL_PRICE_NOT_MATCH");
		 */
        Element element = document.select("input[id=__VIEWSTATE]").first();
        if (element != null)
            _viewstate = element.attr("value");
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("__VIEWSTATE", _viewstate);
        params.put("ctl01$btnSubmit", "Passenger Information");
        resp = HttpHelper.POST(uri, context, params);
        if (!resp.getLocation().startsWith("/Booking_Redirect.aspx"))
            throw new Exception("CONFIRM_FLIGHTS_ERROR");
    }

    /*
     * GET /Booking_Redirect.aspx?step=3&sid=E417A6F4B0A55E5D02C5B78F71ED7F0A
     *
     * POST /Booking_Redirect.aspx?step=3&sid=E417A6F4B0A55E5D02C5B78F71ED7F0A
     */
    private void SubmitPaxInfo(List<AirPassengerInfo> paxInfos, ContactInfo contactInfo) throws Exception {
        URI uri = new URI(baseUrl + "Booking_Redirect.aspx?step=3&" + sessionId);
        Response resp = HttpHelper.GET(uri, context);
        Document document = Jsoup.parse(resp.getBody());
        Element element = document.select("input[id=__VIEWSTATE]").first();
        if (element != null)
            _viewstate = element.attr("value");

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("__VIEWSTATE", _viewstate);
        params.put("ctl01$txtCustomerName", paxInfos.get(0).getFirstName() + " " + paxInfos.get(0).getLastName());
        params.put("ctl01$txtCustomerAddress", contactInfo.getAddress());
        params.put("ctl01$txtCustomerEmail", contactInfo.getEmail());
        params.put("ctl01$txtConfirmEmail", "");
        params.put("phoneprefix", "");
        params.put("ctl01$txtCustomerPhone", contactInfo.getMobile());
        params.put("ctl01$txtRemark", "");
        params.put("ctl01$btnConfirm", "Confirm Pax");

        int curADT = 0;
        int curCHD = 0;
        int curINF = 0;
        String postfix = null;
        for (AirPassengerInfo paxInfo : paxInfos) {
            switch (paxInfo.getPassengerType()) {
                case ADT:
                    curADT++;
                    postfix = "_adt_" + Integer.toString(curADT);
                    params.put("goldcard" + postfix, "");
                    break;
                case CHD:
                    curCHD++;
                    postfix = "_chd_" + Integer.toString(curCHD);
                    params.put("goldcard" + postfix, "");
                    break;
                case INF:
                    curINF++;
                    postfix = "_inf_" + Integer.toString(curINF);
                    params.put("birthday" + postfix, birthdate.format(paxInfo.getBirthDate()));
                    break;
            }
            params.put("title" + postfix, paxInfo.getGender() == Gender.MALE ? "mr" : "ms");
            params.put("firstname" + postfix, paxInfo.getLastName());
            params.put("lastname" + postfix, paxInfo.getFirstName());
        }
        if (!HttpHelper.POST(uri, context, params).getLocation().startsWith("/Booking_Redirect.aspx"))
            throw new Exception("SUBMIT_FLIGHTS_ERROR");
    }

    private String GetBookingCode() throws Exception {
		/*
		 * GET RESERVATION CODE GET
		 * /Booking_Redirect.aspx?step=4&sid=540F1578C91BEE488BAFFB7E944166F5
		 */
        URI uri = new URI(baseUrl + "Booking_Redirect.aspx?step=4&" + sessionId);
        Response resp = HttpHelper.GET(uri, context);
        Document document = Jsoup.parse(resp.getBody());
        Element element = document.select("div[id=div_pnrcode] font").first();
        if (element == null)
            throw new Exception("BOOKING_CODE_NOT_FOUND");
        return element.text();
    }

    // prices are round up to 1000
    private AirFarePriceInfos extractPrices(SegmentPriceList priceList, UpdateFarePriceCommand request) {

        AirFarePriceInfos result = new AirFarePriceInfos();

        int numADT = request.getPassengerQuantity(AirPassengerType.ADT);
        int numCHD = request.getPassengerQuantity(AirPassengerType.CHD);
        int numINF = request.getPassengerQuantity(AirPassengerType.INF);
        int numSeats = numADT + numCHD;
        int curYear = (new DateTime()).getYear();

        for (SegmentPriceInfo spi : priceList) {
            AirFareInfo fareInfo = new AirFareInfo();
            fareInfo.setVendor("VN");
            fareInfo.setOriginCode(spi.getFrom());
            fareInfo.setDestinationCode(spi.getTo());
            fareInfo.setFlightCode("VN" + spi.getFlightCode().trim());
            DateTime departureDate = formatter.parseDateTime(spi.getTimeFrom() + " " + spi.getDayFrom() + "/" + spi.getMonthFrom() + "/" + Integer.toString(curYear));
            fareInfo.setDepartureDate(departureDate.toDate());
            DateTime arrivalDate = formatter.parseDateTime(spi.getTimeTo() + " " + spi.getDayTo() + "/" + spi.getMonthTo() + "/" + Integer.toString(curYear));
            if (arrivalDate.isBefore(departureDate)) {
                arrivalDate = formatter.parseDateTime(spi.getTimeTo() + " " + spi.getDayTo() + "/" + spi.getMonthTo() + "/" + Integer.toString(curYear + 1));
            }
            fareInfo.setArrivalDate(arrivalDate.toDate());
            fareInfo.setReference(spi.getFrom() + spi.getTo() + spi.getTimeFrom() + spi.getTimeTo() + spi.getSeg());// HANSGN060008001
            String reference1 = spi.getAirlines() + spi.getFrom() + spi.getTo() + spi.getTimeFrom() + spi.getTimeTo();
            String reference2 = formatter2.print(departureDate) + spi.getFlightCode() + spi.getSeg();

            HashMap<String, AirFarePriceOption> options = new HashMap<String, AirFarePriceOption>();

            for (ClassPriceInfo classPrice : spi.getPriceInfo()) {
                if (classPrice.getSeat() < numSeats && classPrice.getSeat() < 7)
                    continue;
                String className = config.getBookingClassMap().get(classPrice.getSeatClass());
                if (className == null)
                    continue;

                AirFarePriceOption curOption = options.get(className);
                long basePriceADT = (long) (classPrice.getPrice());
                long baseTaxADT = basePriceADT / 10;
                baseTaxADT = (baseTaxADT % 1000 == 0) ? baseTaxADT : baseTaxADT - baseTaxADT % 1000 + 1000;
                long newTotalADT = basePriceADT + baseTaxADT;
                if (curOption == null || curOption.getPrice(AirPassengerType.ADT).getPrice() > newTotalADT) {
                    AirFarePriceOption newOption = new AirFarePriceOption();
                    newOption.setClassCode(classPrice.getSeatClass());
                    newOption.setClassName(className);
                    newOption.add(new AirPassengerTypePrice(AirPassengerType.ADT, newTotalADT));
                    if (numCHD > 0) {
                        long tmpPrice = basePriceADT / 100 * 75;
                        tmpPrice = (tmpPrice % 1000 == 0) ? tmpPrice : tmpPrice - tmpPrice % 1000 + 1000;
                        long tmpTax = tmpPrice / 10;
                        tmpTax = (tmpTax % 1000 == 0) ? tmpTax : tmpTax - tmpTax % 1000 + 1000;
                        newOption.add(new AirPassengerTypePrice(AirPassengerType.CHD, tmpPrice + tmpTax));
                    }
                    if (numINF > 0) {
                        long tmpPrice = basePriceADT / 10;
                        tmpPrice = (tmpPrice % 1000 == 0) ? tmpPrice : tmpPrice - tmpPrice % 1000 + 1000;
                        long tmpTax = tmpPrice / 10;
                        tmpTax = (tmpTax % 1000 == 0) ? tmpTax : tmpTax - tmpTax % 1000 + 1000;
                        newOption.add(new AirPassengerTypePrice(AirPassengerType.INF, tmpPrice + tmpTax));
                    }
                    String ref = reference1 + classPrice.getSeatClass() + reference2;
                    newOption.setReference(ref);
                    options.put(newOption.getClassName(), newOption);
                }
            }
            AirFarePriceInfo priceInfo = new AirFarePriceInfo();
            priceInfo.setFareInfo(fareInfo);
            priceInfo.setPriceOptions(new ArrayList<AirFarePriceOption>());
            priceInfo.getPriceOptions().addAll(options.values());
            result.add(priceInfo);
        }
        return result;
    }

    private static HashFunction hashFunc = Hashing.md5();

    private URI createSearchUri(UpdateFarePriceCommand request) throws Exception {
        HashMap<String, String> queryParams = new HashMap<String, String>();
        StringBuilder hashBuilder = new StringBuilder();

        String tmpStr = config.getAgentCode();
        queryParams.put("Agent", tmpStr);
        hashBuilder.append(tmpStr);

        AirFareInfo fare0 = request.getOriginDestinationInfos().get(0);
        tmpStr = fare0.getOriginCode();
        queryParams.put("From", tmpStr);
        hashBuilder.append(tmpStr);

        tmpStr = fare0.getDestinationCode();
        queryParams.put("To", tmpStr);
        hashBuilder.append(tmpStr);

        DateTime tmpDate = new DateTime(fare0.getDepartureDate());
        tmpStr = Integer.toString(tmpDate.getDayOfMonth());
        queryParams.put("DayDepart", tmpStr);
        hashBuilder.append(tmpStr);

        tmpStr = Integer.toString(tmpDate.getMonthOfYear());
        queryParams.put("MonthDepart", tmpStr);
        hashBuilder.append(tmpStr);

        tmpStr = Integer.toString(tmpDate.getYear());
        queryParams.put("YearDepart", tmpStr);
        hashBuilder.append(tmpStr);

        String type;
        if (request.getOriginDestinationInfos().size() == 1) {
            tmpDate = new DateTime();
            type = "oneway";
        } else {
            tmpDate = new DateTime(request.getOriginDestinationInfos().get(1).getDepartureDate());
            type = "roundway";
        }

        tmpStr = Integer.toString(tmpDate.getDayOfMonth());
        queryParams.put("DayReturn", tmpStr);
        hashBuilder.append(tmpStr);

        tmpStr = Integer.toString(tmpDate.getMonthOfYear());
        queryParams.put("MonthReturn", tmpStr);
        hashBuilder.append(tmpStr);

        tmpStr = Integer.toString(tmpDate.getYear());
        queryParams.put("YearReturn", tmpStr);
        hashBuilder.append(tmpStr);

        queryParams.put("Type", type);
        hashBuilder.append(type);

        tmpStr = Integer.toString(request.getPassengerQuantity(AirPassengerType.ADT));
        queryParams.put("ADT", tmpStr);
        hashBuilder.append(tmpStr);

        tmpStr = Integer.toString(request.getPassengerQuantity(AirPassengerType.CHD));
        queryParams.put("CHD", tmpStr);
        hashBuilder.append(tmpStr);

        tmpStr = Integer.toString(request.getPassengerQuantity(AirPassengerType.INF));
        queryParams.put("INF", tmpStr);

        hashBuilder.append(config.getSecurityCode());

        HashCode hc = hashFunc.newHasher().putBytes(hashBuilder.toString().getBytes()).hash();
        queryParams.put("Hash", hc.toString().toUpperCase());

        queryParams.put("vHash", "2");
        return HttpHelper.createURI(baseUrl + "Booking_Redirect.aspx", queryParams);
    }

    public static class BookResult {
        private String reservationCode;
        private String orderId;

        public String getReservationCode() {
            return reservationCode;
        }

        public void setReservationCode(String reservationCode) {
            this.reservationCode = reservationCode;
        }

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }
    }

    private static class ClassPriceInfo {
        private String seatClass;
        private int seat;
        private double price;
    
        public int getSeat() {
            return seat;
        }

        @JsonProperty("seat")
        public void setSeat(int seat) {
            this.seat = seat;
        }

        public double getPrice() {
            return price;
        }

        @JsonProperty("price")
        public void setPrice(double price) {
        	this.price = price;
        }



        public String getSeatClass() {
            return seatClass;
        }

        @JsonProperty("Class")
        public void setSeatClass(String seatClass) {
            this.seatClass = seatClass;
        }
    }

    @SuppressWarnings("serial")
    private static class SegmentPriceList extends ArrayList<SegmentPriceInfo> {
    }

    private static class SegmentPriceInfo {
        private int seg;
        private String airlines;
        private String flightCode;
        private String from;
        private String to;
        private int dayFrom;
        private int monthFrom;
        private int dayTo;
        private int monthTo;
        private String timeFrom;
        private String timeTo;
       
        private List<ClassPriceInfo> priceInfo;

        public int getSeg() {
            return seg;
        }

        @JsonProperty("seg")
        public void setSeg(int seg) {
            this.seg = seg;
        }

        public String getAirlines() {
            return airlines;
        }

        @JsonProperty("airlines")
        public void setAirlines(String airlines) {
        	this.airlines = airlines;
        }

        public String getFlightCode() {
            return flightCode;
        }

        @JsonProperty("flightCode")
        public void setFlightCode(String flightCode) {
        	this.flightCode = flightCode;
        }

        public String getFrom() {
            return from;
        }

        @JsonProperty("from")
        public void setFrom(String from) {
        	this.from = from;
        }

        public String getTo() {
            return to;
        }

        @JsonProperty("to")
        public void setTo(String to) {
        	this.to = to;
        }

        public int getDayFrom() {
            return dayFrom;
        }

        @JsonProperty("dayFrom")
        public void setDayFrom(int dayFrom) {
        	this.dayFrom = dayFrom;
        }

        public int getMonthFrom() {
            return monthFrom;
        }

        @JsonProperty("monthFrom")
        public void setMonthFrom(int monthFrom) {
        	this.monthFrom = monthFrom;
        }

        public int getDayTo() {
            return dayTo;
        }

        @JsonProperty("dayTo")
        public void setDayTo(int dayTo) {
        	this.dayTo = dayTo;
        }

        public int getMonthTo() {
            return monthTo;
        }

        @JsonProperty("monthTo")
        public void setMonthTo(int monthTo) {
        	this.monthTo = monthTo;
        }

        public String getTimeFrom() {
            return timeFrom;
        }

        @JsonProperty("timeFrom")
        public void setTimeFrom(String timeFrom) {
        	this.timeFrom = timeFrom;
        }

        public String getTimeTo() {
            return timeTo;
        }

        @JsonProperty("timeTo")
        public void setTimeTo(String timeTo) {
        	this.timeTo = timeTo;
        }

        
        public List<ClassPriceInfo> getPriceInfo() {
            return priceInfo;
        }

        @JsonProperty("priceInfo")
        public void setPriceInfo(List<ClassPriceInfo> priceInfo) {
        	this.priceInfo = priceInfo;
        }
    }
}
