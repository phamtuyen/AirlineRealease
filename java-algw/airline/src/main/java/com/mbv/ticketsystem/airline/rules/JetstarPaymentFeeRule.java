package com.mbv.ticketsystem.airline.rules;

import java.util.HashMap;

import com.mbv.ticketsystem.common.airline.AirFarePriceInfo;
import com.mbv.ticketsystem.common.airline.AirFarePriceInfos;
import com.mbv.ticketsystem.common.airline.AirFarePriceOption;
import com.mbv.ticketsystem.common.airline.AirPassengerType;
import com.mbv.ticketsystem.common.airline.AirPassengerTypePrice;

public class JetstarPaymentFeeRule extends FarePriceRule {

    private HashMap<AirPassengerType, Long> fees = new HashMap<AirPassengerType, Long>() {
        {
            put(AirPassengerType.ADT, 223000L);
            put(AirPassengerType.CHD, 223000L);
        }
    };

    public HashMap<AirPassengerType, Long> getFees() {
        return fees;
    }

    public void setFees(HashMap<AirPassengerType, Long> fees) {
        this.fees = fees;
    }

    private long getFee(AirPassengerType passengerType) {
        if (fees == null || !fees.containsKey(passengerType))
            return 0;
        return fees.get(passengerType);
    }

    public void apply(Object object) {
        if (object instanceof AirFarePriceInfos) {
            doApply((AirFarePriceInfos) object);
        } else if (object instanceof AirFarePriceInfo) {
            doApply((AirFarePriceInfo) object);
        } else if (object instanceof AirFarePriceOption) {
            doApply((AirFarePriceOption) object);
        }

    }

    private void doApply(AirFarePriceInfos result) {
        for (AirFarePriceInfo item : result) {
            doApply(item);
        }
    }

    private void doApply(AirFarePriceInfo farePrice) {
        for (AirFarePriceOption option : farePrice.getPriceOptions()) {
            doApply(option);
        }
    }

    private void doApply(AirFarePriceOption option) {
        for (AirPassengerTypePrice price : option.getPriceDetail()) {
            long newPrice = price.getPrice() + getFee(price.getPassengerType());
            price.setPrice(newPrice);
        }
    }
}
