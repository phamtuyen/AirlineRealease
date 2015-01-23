package com.mbv.ticketsystem.airline;

import java.util.List;

import com.mbv.ticketsystem.airline.repository.AirItineraryRepository;
import com.mbv.ticketsystem.airline.rules.FarePriceRule;
import com.mbv.ticketsystem.airline.support.AirFarePriceCache;

import akka.actor.Actor;
import akka.actor.UntypedActorFactory;

@SuppressWarnings("serial")
public abstract class AbstractWorkerFactory implements UntypedActorFactory {

    private String workerName;
    private int numWorkers;
    private List<FarePriceRule> fareRules;
    private AirFarePriceCache farePriceCache;
    private AirItineraryRepository itineraryRepository;

    public AirItineraryRepository getItineraryRepository() {
        return itineraryRepository;
    }

    public void setItineraryRepository(AirItineraryRepository itineraryRepository) {
        this.itineraryRepository = itineraryRepository;
    }

    public int getNumWorkers() {
        return numWorkers;
    }

    public void setNumWorkers(int numWorkers) {
        this.numWorkers = numWorkers;
    }

    public List<FarePriceRule> getFareRules() {
        return fareRules;
    }

    public void setFareRules(List<FarePriceRule> fareRules) {
        this.fareRules = fareRules;
    }

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    public AirFarePriceCache getFarePriceCache() {
        return farePriceCache;
    }

    public void setFarePriceCache(AirFarePriceCache farePriceCache) {
        this.farePriceCache = farePriceCache;
    }

    public Actor create() throws Exception {
        AbstractAirWorker worker = doCreate();
        worker.setFareRules(getFareRules());
        worker.setFarePriceCache(getFarePriceCache());
        worker.setItineraryRepository(getItineraryRepository());
        worker.setAvailableMessage(new WorkerAvailableMessage(getWorkerName()));
        return (Actor) worker;
    }

    protected abstract AbstractAirWorker doCreate();
}
