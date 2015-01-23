package com.mbv.ticketsystem.launcher;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericXmlApplicationContext;

public class Console implements Daemon {
    GenericXmlApplicationContext ctx = null;

    @Override
    public void destroy() {
        ctx = null;

    }

    @Override
    public void init(DaemonContext arg0) throws DaemonInitException, Exception {

    }

    @Override
    public void start() throws Exception {
        ctx = new GenericXmlApplicationContext();
        ctx.load("classpath:airservice-context.xml");
        ctx.registerShutdownHook();
        ctx.refresh();
    }

    @Override
    public void stop() throws Exception {
        ctx.close();
    }

    public static void main(String[] args) {
        final Logger logger = LoggerFactory.getLogger(Console.class);
        try {
            Console console = new Console();
            console.start();
        } catch (Exception e) {
            logger.info("Error starting application", e);
            System.exit(1);
        }
    }

//    public static void main(String[] args) {
//		final Logger logger = LoggerFactory.getLogger(Console.class);
//		try {
//			GenericXmlApplicationContext applicationContext = new GenericXmlApplicationContext(args);
//			applicationContext.registerShutdownHook();
//		} catch (Exception e) {
//			logger.error("Error starting application", e);
//			System.exit(1);
//		}
//	}
}
