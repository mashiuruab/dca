package com.sannsyn.dca.vaadin.ui;

import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.ErrorEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mashiur on 7/13/16.
 */
public class DCACustomErrorHandler extends DefaultErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(DCACustomErrorHandler.class);
    @Override
    public void error(ErrorEvent event) {
        event.getThrowable().printStackTrace();
        logger.error("ERROR:: ", event.getThrowable());
    }
}
