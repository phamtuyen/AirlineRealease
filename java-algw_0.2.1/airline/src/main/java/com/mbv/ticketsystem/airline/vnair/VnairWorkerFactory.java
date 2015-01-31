package com.mbv.ticketsystem.airline.vnair;

import com.mbv.ticketsystem.airline.AbstractAirWorker;
import com.mbv.ticketsystem.airline.AbstractWorkerFactory;

@SuppressWarnings("serial")
public class VnairWorkerFactory extends AbstractWorkerFactory {
    private VniscConfig config;

    public VniscConfig getConfig() {
        return config;
    }

    public void setConfig(VniscConfig config) {
        this.config = config;
    }

    @Override
    protected AbstractAirWorker doCreate() {
        return new VnairWorker(new VniscHttpStub(config), new VniscSoapStub(config));
    }
}
