package com.mbv.ticketsystem.common.airline;

import java.util.ArrayList;
import java.util.List;

public class AirFarePriceInfo {
    private AirFareInfo fareInfo;
    private List<AirFarePriceOption> priceOptions;

    public AirFareInfo getFareInfo() {
        return fareInfo;
    }

    public void setFareInfo(AirFareInfo fareInfo) {
        this.fareInfo = fareInfo;
    }

    public List<AirFarePriceOption> getPriceOptions() {
        return priceOptions;
    }

    public void setPriceOptions(List<AirFarePriceOption> priceOptions) {
        this.priceOptions = priceOptions;
    }

    public void add(AirFarePriceOption priceOption) {
        if (this.priceOptions == null) this.priceOptions = new ArrayList<AirFarePriceOption>();
        this.priceOptions.add(priceOption);
    }

    public AirFarePriceOption getAirFarePriceOption(String code) {
        for (AirFarePriceOption option : priceOptions) {
            if (code.equals(option.getClassCode()))
                return option;
        }
        return null;
    }
}
