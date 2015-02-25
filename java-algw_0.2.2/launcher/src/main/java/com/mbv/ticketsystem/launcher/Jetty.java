package com.mbv.ticketsystem.launcher;

import java.io.IOException;
import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.GzipFilter;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;

@Configuration
public class Jetty implements ApplicationContextAware, Daemon {
    private ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server jettyServer() throws IOException {
        Server server = new Server();
        server.setHandler(servletContext());
        server.setConnectors(jettyConnectors());
        QueuedThreadPool threadpool = new QueuedThreadPool();
        threadpool.setMinThreads(10);
        threadpool.setMaxThreads(20);
        server.setThreadPool(threadpool);
        return server;
    }

    //Handler
    @Bean
    public ServletContextHandler servletContext() throws IOException {
        ServletContextHandler handler = new ServletContextHandler();
        handler.setContextPath("/");
        handler.addServlet(dispatcherServlet(), "/");

        FilterHolder filter = handler.addFilter(GzipFilter.class, "/*", EnumSet.of(DispatcherType.ASYNC));
        filter.setInitParameter("mimeTypes", "application/json");
        return handler;
    }

    @Bean
    public ServletHolder dispatcherServlet() {
        XmlWebApplicationContext ctx = new XmlWebApplicationContext();
        ctx.setParent(applicationContext);
        ctx.setConfigLocation("classpath:dispatcher-servlet.xml");
        DispatcherServlet servlet = new DispatcherServlet(ctx);
        ServletHolder holder = new ServletHolder("dispatcher-servlet", servlet);
        holder.setInitOrder(1);
        return holder;
    }

    //Connector
    @Bean
    public Connector[] jettyConnectors() {
        SelectChannelConnector connector = new SelectChannelConnector();
        String webPort = System.getenv("PORT");
        if (webPort == null || webPort.isEmpty()) {
            webPort = "8181";
        }
        connector.setPort(Integer.parseInt(webPort));
        return new Connector[]{connector};
    }

    private static boolean webApplicationContextInitialized = false;

    AnnotationConfigApplicationContext ctx = null;

    @Override
    public void destroy() {
        ctx = null;

    }

    @Override
    public void init(DaemonContext arg0) throws DaemonInitException, Exception {

    }

    @Override
    public void start() throws Exception {
        ctx = new AnnotationConfigApplicationContext();
        ctx.addApplicationListener(new ApplicationListener<ContextRefreshedEvent>() {
            public void onApplicationEvent(ContextRefreshedEvent event) {
                ApplicationContext ctx = event.getApplicationContext();
                if (ctx instanceof WebApplicationContext)
                    webApplicationContextInitialized = true;
            }
        });
        ctx.registerShutdownHook();
        ctx.register(Jetty.class);
        ctx.refresh();

        if (!webApplicationContextInitialized)
            throw new Exception("Web application context not initialized. Exiting.");
    }

    @Override
    public void stop() throws Exception {
        ctx.close();
    }

    public static void main(String[] args) throws Exception {
//        final Logger logger = LoggerFactory.getLogger(Jetty.class);
    	final Logger logger = Logger.getLogger(Jetty.class);
        try {
        	logger.info("Jetty started port: 8181"); 
            Jetty jetty = new Jetty();
            jetty.start();
        } catch (Exception e) {
            logger.info("Error starting application", e);
            System.exit(1);
        }
    }

//	public static void main(String[] args) throws Exception {
//		final Logger logger = LoggerFactory.getLogger(Jetty.class);
//		try {
//			AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
//			applicationContext.addApplicationListener(new ApplicationListener<ContextRefreshedEvent>() {
//				public void onApplicationEvent(ContextRefreshedEvent event) {
//					ApplicationContext ctx = event.getApplicationContext();
//					if (ctx instanceof WebApplicationContext)
//						webApplicationContextInitialized = true;
//				}
//			});
//			applicationContext.registerShutdownHook();
//			applicationContext.register(Jetty.class);
//			applicationContext.refresh();
//
//			if (!webApplicationContextInitialized)
//				throw new Exception("Web application context not initialized. Exiting.");
//		} catch (Exception e) {
//			logger.error("Error starting application", e);
//			System.exit(1);
//		}
//	}
}
