package com.mbv.ticketsystem.webapp.web;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.Logger;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.mbv.ticketsystem.airline.repository.AirItineraryRepository;
import com.mbv.ticketsystem.airline.support.AirFarePriceCache;
import com.mbv.ticketsystem.common.airline.AirBookingInfo;
import com.mbv.ticketsystem.common.airline.AirFarePriceInfos;
import com.mbv.ticketsystem.common.airline.AirItinerary;
import com.mbv.ticketsystem.common.airline.AirTicketingInfo;
import com.mbv.ticketsystem.common.airline.AirTicketingStatus;
import com.mbv.ticketsystem.common.airline.BookItineraryCommand;
import com.mbv.ticketsystem.common.airline.PayItineraryCommand;
import com.mbv.ticketsystem.common.airline.RetrieveItineraryCommand;
import com.mbv.ticketsystem.common.airline.UpdateFarePriceCommand;

@Controller
@RequestMapping("/AirService")
public class AirlineController {
	private int timeCache;
	final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	final Logger logger = Logger.getLogger(AirlineController.class);
    private RabbitTemplate rabbitTemplate;
    private AirFarePriceCache farePriceCache;
    private AirItineraryRepository itineraryRepository;

    public int getTimeCache() {
		return timeCache;
	}

	public void setTimeCache(int timeCache) {
		this.timeCache = timeCache;
	}
	
    public void setRabbitTemplate(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public AirFarePriceCache getFarePriceCache() {
        return farePriceCache;
    }

    public void setFarePriceCache(AirFarePriceCache farePriceCache) {
        this.farePriceCache = farePriceCache;
    }

    public AirItineraryRepository getItineraryRepository() {
        return itineraryRepository;
    }

    public void setItineraryRepository(AirItineraryRepository itineraryRepository) {
        this.itineraryRepository = itineraryRepository;
    }

    @RequestMapping(value = "test", method = RequestMethod.GET)
    @ResponseBody
    public String Test() throws Exception {
        return "Maka Faka";
    }

    @RequestMapping(value = "fare/{Id}", method = RequestMethod.GET)
    @ResponseBody
    public AirFarePriceInfos GetFares(@PathVariable String Id) throws FareNotFoundException {
        AirFarePriceInfos result = farePriceCache.find(Id);
        if (result == null)
            throw new FareNotFoundException();
        return result;
    }

    @RequestMapping(value = "fare", method = RequestMethod.POST)
    public String SearchFares(@RequestBody UpdateFarePriceCommand request) throws InterruptedException {    
        String id = request.toHashString();
        String routingKey = request.getOriginDestinationInfos().get(0).getVendor();   
        AirFarePriceInfos result = farePriceCache.find(id);
        if(result != null){            	
        	boolean resultCompareDate = compareDate(id,timeCache);
        	if(resultCompareDate){
        		farePriceCache.deleteId(id);
        		result = null;
        	}        	
        }       
        if (routingKey == "BL" || routingKey == "VJ" || result == null) {
            rabbitTemplate.convertAndSend("AirService", routingKey, request, new MessagePostProcessor() {
                public Message postProcessMessage(Message message) throws AmqpException {
                    logger.info("RabbitMQ send success search request: " + message.toString());
                    return message;
                }
            });
        }       
        logger.info("AirlineController: Data Received(Search): " + request.toString());
        return "redirect:/AirService/fare/" + id;
    }

    @RequestMapping(value = "itinerary/{Id}", method = RequestMethod.GET)
    @ResponseBody
    public AirItinerary GetItinerary(@PathVariable("Id") String Id) {
    	return itineraryRepository.findById(Id);
    }

    @RequestMapping(value = "itinerary/{Id}/status", method = RequestMethod.GET)
    @ResponseBody
    public AirTicketingInfo GetTicketingInfo(@PathVariable("Id") String Id) {
        AirItinerary itinerary = itineraryRepository.findById(Id);
        if (itinerary != null)
            return itinerary.getTicketingInfo();
        return null;
    }

    @RequestMapping(value = "itinerary", method = RequestMethod.POST)
    public String Book(@RequestBody AirBookingInfo bookingInfo) throws Exception {    	    	   
        AirItinerary itinerary = AirItinerary.create(bookingInfo);        
        itineraryRepository.add(itinerary);       
        String routingKey = bookingInfo.getFareInfos().get(0).getVendor();
        BookItineraryCommand command = new BookItineraryCommand();
        command.setId(itinerary.getId());
        rabbitTemplate.convertAndSend("AirService", routingKey, command, new MessagePostProcessor() {
            public Message postProcessMessage(Message message) throws AmqpException {
                logger.info("RabbitMQ send success booking request: " + message.toString());
                return message;
            }
        });
        logger.info("AirlineController: Data Recieved(Book): " + itinerary.toString());
        return "redirect:/AirService/itinerary/" + itinerary.getId();
    }

    @RequestMapping(value = "itinerary/{Id}", method = RequestMethod.POST)
    public ResponseEntity<String> UpdateItinerary(@PathVariable("Id") String Id, @RequestBody ItineraryUpdateCommand command) throws Exception {
        AirItinerary itinerary = itineraryRepository.findById(Id);
        if (itinerary == null)
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);       
        AirTicketingInfo info = itinerary.getTicketingInfo();
        if ("BUY".equals(command.getCommand())) {
            if (info.getStatus() == AirTicketingStatus.BOOK_SUCCESS || info.getStatus() == AirTicketingStatus.BUY_ONLY) {
                info.setStatus(AirTicketingStatus.BUY_PENDING);
                itineraryRepository.update(itinerary);
                String routingKey = itinerary.getFareInfos().get(0).getVendor();
                rabbitTemplate.convertAndSend("AirService", routingKey, new PayItineraryCommand(Id));
                logger.info("RabbitMQ send success payment request: " + command.toString());
            }
        } else if ("CANCEL".equals(command.getCommand())) {
            if (info.getStatus() == AirTicketingStatus.BOOK_SUCCESS) {
                info.setStatus(AirTicketingStatus.BOOK_CANCELED);
                itineraryRepository.update(itinerary);
            }
        }
        logger.info("AirlineController: Date Recieved(Pay): " + itinerary.toString());
        return new ResponseEntity<String>(HttpStatus.ACCEPTED);
    }
    
    
    @RequestMapping(value = "getItinerary", method = RequestMethod.POST)
    public String getBookId(@RequestBody AirItinerary bookingInfo) throws Exception {
        AirItinerary itinerary = itineraryRepository.findByReservationCode(bookingInfo.getTicketingInfo());
        if(itinerary == null) {
            itinerary = AirItinerary.create(bookingInfo);
            itineraryRepository.add(itinerary);
        }
        String routingKey = itinerary.getFareInfos().get(0).getVendor();
        RetrieveItineraryCommand command = new RetrieveItineraryCommand();
        command.setId(itinerary.getId());
        rabbitTemplate.convertAndSend("AirService", routingKey, command, new MessagePostProcessor() {
            public Message postProcessMessage(Message message) throws AmqpException {
                logger.info("RabbitMQ send success booking request: " + message.toString());
                return message;
            }
        });
        logger.info("AirlineController: Search BookID Recieved: " + itinerary.toString());
        return "redirect:/AirService/itinerary/" + itinerary.getId();
    }
               
    public static class ItineraryGetBookingId{
    	private String reservationCode;;
    	private String vendor;
		public String getReservationCode() {
			return reservationCode;
		}
		public void setReservationCode(String reservationCode) {
			this.reservationCode = reservationCode;
		}
		public String getVendor() {
			return vendor;
		}
		public void setVendor(String vendor) {
			this.vendor = vendor;
		}
		
    }
    
    public static class ItineraryUpdateCommand {
        private String command;

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }
    }
    
    private boolean compareDate(String id,int timeTemp){
    	Date dateMongodb = farePriceCache.findByFare(id); 
    	long ldateMongodb = dateMongodb.getTime();
    	long lcurrentDate =  new Date().getTime();        	
    	long datebetween = lcurrentDate -ldateMongodb;
    	if(datebetween > timeTemp*60000)
    		return true;
    	return false;    		
    }

    @SuppressWarnings("serial")
    private static class FareNotFoundException extends Exception {
    }

    @ExceptionHandler(FareNotFoundException.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void FareNotFound(FareNotFoundException ex) {
    }

    @SuppressWarnings("serial")
    private static class ItineraryNotFoundException extends Exception {
    }

    @ExceptionHandler(ItineraryNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void FareNotFound(ItineraryNotFoundException ex) {
    }

	
}
