package com.mbv.ticketsystem.airline.vnair;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.mbv.ticketsystem.airline.vnair.vniscsoapservice.BookingProcessService;
import com.mbv.ticketsystem.airline.vnair.vniscsoapservice.BookingProcessServiceSoap;
import com.mbv.ticketsystem.common.airline.AirItinerary;

//Not sure Vnisc supports concurrency, use lock for good;
public class VniscSoapStub {
    private VniscConfig config;
    private BookingProcessServiceSoap servicePort;

    private static HashFunction md5 = Hashing.md5();
    private static DocumentBuilder builder;

    public VniscSoapStub(VniscConfig config) {
        this.config = config;
        servicePort = (new BookingProcessService()).getBookingProcessServiceSoap();
    }

    private static String calculateHash(String source) {
        return md5.newHasher().putBytes(source.getBytes()).hash().toString().toUpperCase();
    }

    private static Document parseXml(String content) throws Exception {
        if (builder == null)
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(content));
        return builder.parse(is);
    }

    private String GetTokenKey() throws Exception {
        try {
            String tokenKey = servicePort.agentLogin(config.getAgentCode(), calculateHash(config.getAgentCode() + config.getSecurityCode()));
            if (tokenKey != null && tokenKey.length() > 0)
                return tokenKey;
        } catch (Exception ex) {
        }
        throw new Exception("LOGIN_ERROR");
    }

    // Exceptions:
    // LOGIN_FAILED

    public long checkAndGetAmount(AirItinerary itinerary, String orderId) throws Exception {
        String token = GetTokenKey();
        try {
            String tmp = servicePort.bookingInfo(Long.parseLong(orderId), token, calculateHash(orderId + config.getSecurityCode()));
            Document doc = parseXml(tmp);
            NodeList nodes = doc.getElementsByTagName("Amount");
            return Long.parseLong(nodes.item(0).getTextContent());
        } catch (Exception ex) {
        }
        throw new Exception("");
    }

    public void CancelBooking(AirItinerary itinerary) throws Exception {
        String token = GetTokenKey();
        String tmp = itinerary.getTicketingInfo().getReservationCode();
        tmp = servicePort.cancelBooking(tmp, token, calculateHash(tmp + config.getSecurityCode()));
    }

    public BuyResult payItinerary(AirItinerary itinerary) throws Exception {
        String token;
        try {
            token = GetTokenKey();
        } catch (Exception ex) {
            return new BuyResult("LOGIN_ERROR");
        }

        String tmp = itinerary.getTicketingInfo().getReservationCode();
        try {
            tmp = servicePort.paymentBooking(tmp, token, calculateHash(tmp + config.getSecurityCode()));
        } catch (Exception ex) {
            throw new Exception("SERVICE_CALL_ERROR");
        }

        try {
            Document doc = parseXml(tmp);
            tmp = doc.getElementsByTagName("ErrorCode").item(0).getTextContent().trim();
            if (!"0".equals(tmp))
                return new BuyResult("Error: " + tmp);
            tmp = doc.getElementsByTagName("Data").item(0).getTextContent();
            return new BuyResult(tmp.split(","));
        } catch (Exception ex) {
            throw new Exception("RESPONSE_PARSE_ERROR");
        }

    }

    public static class BuyResult {
        private String description;
        private List<String> ticketNumbers;

        public BuyResult(String description) {
            this.description = description;
        }

        public BuyResult(String[] ticketNumbers) {
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
