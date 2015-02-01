package com.mbv.ticketsystem.common.airline;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class AirFarePriceInfos extends ArrayList<AirFarePriceInfo> {

    public AirFarePriceInfo get(AirFareInfo fareInfo) {
        for (AirFarePriceInfo priceInfo : this) {
            AirFareInfo tmp = priceInfo.getFareInfo();
            if (!fareInfo.getFlightCode().equals(tmp.getFlightCode()))
                continue;
            if (!fareInfo.getOriginCode().equals(tmp.getOriginCode()))
                continue;
            if (!fareInfo.getDestinationCode().equals(tmp.getDestinationCode()))
                continue;
            if (fareInfo.getDepartureDate().compareTo(tmp.getDepartureDate()) != 0)
                continue;
            return priceInfo;
        }
        return null;
    }

}
