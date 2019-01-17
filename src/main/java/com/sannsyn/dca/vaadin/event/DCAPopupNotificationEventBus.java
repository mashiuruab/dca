package com.sannsyn.dca.vaadin.event;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import com.sannsyn.dca.vaadin.ui.DCAUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mashiur on 7/3/17.
 */
public class DCAPopupNotificationEventBus implements SubscriberExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(DCAPopupNotificationEventBus.class);

    private final EventBus eventBus = new EventBus(this);

    public static void post(final Object event) {
        DCAUI.getPopupNotificationEventBus().eventBus.post(event);
    }

    public static void register(final Object object) {
        DCAUI.getPopupNotificationEventBus().eventBus.register(object);
    }

    public static void unregister(final Object object) {
        DCAUI.getPopupNotificationEventBus().eventBus.unregister(object);
    }

    @Override
    public void handleException(Throwable exception, SubscriberExceptionContext context) {
        logger.error("Exception occured in PopUp Notification Event Bus ", exception);
    }
}
