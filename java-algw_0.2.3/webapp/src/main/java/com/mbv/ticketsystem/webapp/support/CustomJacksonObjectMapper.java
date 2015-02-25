package com.mbv.ticketsystem.webapp.support;

import java.text.SimpleDateFormat;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreType;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import com.mbv.ticketsystem.common.airline.AirFareInfo;
import com.mbv.ticketsystem.common.base.OriginDestinationInfo;

public class CustomJacksonObjectMapper extends ObjectMapper {
    public CustomJacksonObjectMapper() {
        super();
        setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"));
        setSerializationInclusion(Inclusion.NON_NULL);
        //getDeserializationConfig().addMixInAnnotations(AirSearchRequest.class, AirSearchRequestMixIn.class);
        //getSerializationConfig().addMixInAnnotations(AirSearchResponse.class, AirSearchResponseMixIn.class);
        //getSerializationConfig().addMixInAnnotations(AirFareInfo.class, AirFareInfoMixIn.class);

    }

/*	//@JsonIgnoreType
    abstract class AirSearchRequestMixIn {
		@JsonProperty("originDestinationInfos")
		List<OriginDestinationInfo> originDestinationInfos;

		@JsonProperty("passengerInfos")
		AirPassengerInfos passengerInfos;

		@JsonProperty("travelPreferences")
		AirTravelPreferences travelPreferences;
		
		@JsonIgnore() String id;
	}
	
	abstract class AirSearchResponseMixIn{
		@JsonIgnore() String searchString;
	}
	
	abstract class AirFareInfoMixIn{
		@JsonIgnore() String reference;
	}*/
}
