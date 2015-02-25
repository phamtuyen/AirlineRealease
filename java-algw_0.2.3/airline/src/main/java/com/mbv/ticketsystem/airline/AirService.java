package com.mbv.ticketsystem.airline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;

public class AirService {
    protected final Logger logger = LoggerFactory.getLogger(AirService.class);

    protected ActorSystem actorSystem;
    private ActorRef master;
    private Props masterProps;

    public Props getMasterProps() {
        return masterProps;
    }

    public void setMasterProps(Props masterProps) {
        this.masterProps = masterProps;
    }

    public void start() {
        if (actorSystem == null) {
            actorSystem = ActorSystem.create("AirService");
        }
        master = actorSystem.actorOf(masterProps, "Master");
    }

    public void stop() {
        master.tell(PoisonPill.getInstance(), null);
    }
}
