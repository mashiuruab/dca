package com.sannsyn.dca.vaadin.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.Enumeration;

/**
 * Created by mashiur on 3/25/16.
 */
public class DCAWebAppContextListener implements ServletContextListener, HttpSessionListener {
    private static final Logger logger = LoggerFactory.getLogger(DCAWebAppContextListener.class);
    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        logger.info("DCA Web Application Context Initialized");
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
        logger.info("DCA Web Application Context Destroyed");
        DCASchedulerUtil.getChartUpdateExecutorService().shutdownNow();
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {

    }

    @Override
    public void sessionDestroyed(HttpSessionEvent sessionEvent) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Destroying session %s", sessionEvent.getSession().getId()));

            Enumeration<String> elements = sessionEvent.getSession().getAttributeNames();
            while (elements.hasMoreElements()) {
                logger.debug(String.format("%s", elements.nextElement()));
            }
        }
        sessionEvent.getSession().removeAttribute(DCAUserPreference.LOGGEDIN_USER_SESSION_KEY);
    }
}
