package com.mbv.ticketsystem.airline.rules;

import java.util.HashMap;

import com.mbv.ticketsystem.common.airline.AirFarePriceInfo;
import com.mbv.ticketsystem.common.airline.AirFarePriceInfos;
import com.mbv.ticketsystem.common.airline.AirFarePriceOption;
import com.mbv.ticketsystem.common.airline.AirPassengerType;
import com.mbv.ticketsystem.common.airline.AirPassengerTypePrice;

@SuppressWarnings("serial")
public class AirportTaxRule extends FarePriceRule {
    private HashMap<String, Long> taxes = new HashMap<String, Long>() {
        {
            put("BMV", 60000L);
            put("CAH", 50000L);
            put("VCS", 50000L);
            put("VCA", 60000L);
            put("HUI", 60000L);
            put("HAN", 60000L);
            put("HPH", 60000L);
            put("NHA", 60000L);
            put("PQC", 60000L);
            put("PXU", 50000L);
            put("UIH", 50000L);
            put("VKG", 50000L);
            put("VCL", 50000L);
            put("SGN", 60000L);
            put("VII", 50000L);
            put("DIN", 50000L);
            put("DLI", 60000L);
            put("DAD", 60000L);
            put("VDH", 50000L);
            put("THD", 50000L);
        }
    };

    public HashMap<String, Long> getTaxes() {
        return taxes;
    }

    public void setTaxes(HashMap<String, Long> taxes) {
        this.taxes = taxes;
    }

    public void apply(Object object) {
        if (object instanceof AirFarePriceInfos) {
            doAppply((AirFarePriceInfos) object);
        } else if (object instanceof AirFarePriceInfo) {
            doAppply((AirFarePriceInfo) object);
        }
    }

    private void doAppply(AirFarePriceInfos result) {
        for (AirFarePriceInfo item : result) {
            doAppply(item);
        }
    }

    private void doAppply(AirFarePriceInfo farePriceInfo) {
        String airportCode = farePriceInfo.getFareInfo().getOriginCode();
        if (!taxes.containsKey(airportCode))
            return;
        Long tax = taxes.get(airportCode);
        for (AirFarePriceOption option : farePriceInfo.getPriceOptions()) {
            for (AirPassengerTypePrice price : option.getPriceDetail()) {
                long newPrice = price.getPrice();
                switch (price.getPassengerType()) {
                    case ADT:
                        newPrice += tax;
                        break;
                    case CHD:
                        newPrice += tax / 2;
                        break;
                    case INF:
                        break;
                }
                price.setPrice(newPrice);
            }
        }
    }
}
