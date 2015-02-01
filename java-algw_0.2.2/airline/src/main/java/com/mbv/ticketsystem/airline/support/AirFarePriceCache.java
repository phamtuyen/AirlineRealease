package com.mbv.ticketsystem.airline.support;
import java.util.Date;

import com.mbv.ticketsystem.common.airline.AirFarePriceInfos;


public interface AirFarePriceCache {
    public boolean hasResult(String id);

    public AirFarePriceInfos find(String id);
    
    public Date findByFare(String id);
    
    public void deleteId(String id);

    public void update(String id, AirFarePriceInfos result);
}
