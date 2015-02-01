package com.mbv.ticketsystem.airline;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import akka.actor.Actor;
import akka.actor.Props;
import akka.actor.UntypedActorFactory;

public class AirServiceMasterPropsFactory {
    @SuppressWarnings("serial")
    public static Props create(final List<AbstractWorkerFactory> factories, final RabbitTemplate rabbitTemplate) {
        Props props = new Props(new UntypedActorFactory() {
            public Actor create() throws Exception {
                return new RabbitMqAirServiceMaster(factories, rabbitTemplate);
            }
        });
        return props;
    }
}
