package com.mbv.ticketsystem.airline;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.ActiveObjectCounter;
import org.springframework.amqp.rabbit.listener.BlockingQueueConsumer;
import org.springframework.amqp.rabbit.support.DefaultMessagePropertiesConverter;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;

//Actor
public class RabbitMqAirServiceMaster extends AirServiceMaster {
    private static Logger logger = LoggerFactory.getLogger(RabbitMqAirServiceMaster.class);

    private RabbitTemplate template;
    private HashMap<String, ActorRef> listeners;

    public RabbitMqAirServiceMaster(List<AbstractWorkerFactory> workerFactories, RabbitTemplate template) {
        super(workerFactories);
        this.template = template;
        this.listeners = new HashMap<String, ActorRef>();
    }

    public void preStart() {
        super.preStart();
        System.out.println("pre started");
    }

    @SuppressWarnings("serial")
    @Override
    protected void dispatchWorker(WorkerAvailableMessage message, ActorRef worker) {
        final String vendor = message.getVendor();
        ActorRef listener = listeners.get(vendor);
        if (listener == null) {
            listener = getContext().actorOf(new Props(new UntypedActorFactory() {
                public Actor create() throws Exception {
                    return new RabbitMqListenerActor(vendor, template);
                }
            }));
            listeners.put(vendor, listener);
        }
        listener.tell(getSender(), null);
    }

    private static class RabbitMqListenerActor extends UntypedActor {
        private RabbitTemplate template;
        private BlockingQueueConsumer consumer;

        public RabbitMqListenerActor(String queue, RabbitTemplate template) {
            this.template = template;
            consumer = new BlockingQueueConsumer(template.getConnectionFactory(), new DefaultMessagePropertiesConverter(), new ActiveObjectCounter<BlockingQueueConsumer>(), AcknowledgeMode.AUTO, false, 1, queue);
            consumer.start();
        }

        @Override
        public void onReceive(Object object) throws Exception {
            if (object instanceof ActorRef) {
                ActorRef worker = (ActorRef) object;
                Message message = consumer.nextMessage(1000L);
                if (message == null) {
                    getSelf().tell(worker, null);
                } else {
                    consumer.commitIfNecessary(false);
                    logger.info("dispatchWorker: " + worker.path());
                    worker.tell(template.getMessageConverter().fromMessage(message), null);
                }
            }
        }

        public void postStop() {
            consumer.stop();
        }
    }
}
