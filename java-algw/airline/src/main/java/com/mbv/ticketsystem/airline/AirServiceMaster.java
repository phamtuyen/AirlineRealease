package com.mbv.ticketsystem.airline;

import java.util.List;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;

public abstract class AirServiceMaster extends UntypedActor {

    private List<AbstractWorkerFactory> workerFactories;

    public AirServiceMaster(List<AbstractWorkerFactory> workerFactories) {
        this.workerFactories = workerFactories;
    }

    public void preStart() {
        for (AbstractWorkerFactory factory : workerFactories) {
            Props props = new Props(factory);
            for (int num = 0; num < factory.getNumWorkers(); num++) {
                getContext().actorOf(props, factory.getWorkerName() + num);
            }
        }
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof WorkerAvailableMessage) {
            dispatchWorker((WorkerAvailableMessage) message, getSender());
        }
    }

    protected abstract void dispatchWorker(WorkerAvailableMessage message, ActorRef worker);
}
