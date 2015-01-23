package com.mbv.ticketsystem.airline.vietjet;

import com.mbv.ticketsystem.airline.AbstractAirWorker;
import com.mbv.ticketsystem.airline.AbstractWorkerFactory;

@SuppressWarnings("serial")
public class VietjetWorkerFactory extends AbstractWorkerFactory {
    private VietjetConfig config;

    public VietjetConfig getConfig() {
        return config;
    }

    public void setConfig(VietjetConfig config) {
        this.config = config;
    }

    @Override
    protected AbstractAirWorker doCreate() {
        return new VietjetWorker(config, getFareRules());
    }
}
