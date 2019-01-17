package com.sannsyn.dca.vaadin.event;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import com.sannsyn.dca.vaadin.ui.DCAUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple wrapper for Guava event bus. Defines static convenience methods for
 * relevant actions.
 */
public class DCADashboardEventBus implements SubscriberExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(DCADashboardEventBus.class);

    private final EventBus eventBus = new EventBus(this);

    public static void post(final Object event) {
        DCAUI.getDashboardEventbus().eventBus.post(event);
    }

    public static void register(final Object object) {
        DCAUI.getDashboardEventbus().eventBus.register(object);
    }

    public static void unregister(final Object object) {
        DCAUI.getDashboardEventbus().eventBus.unregister(object);
    }

    @Override
    public final void handleException(final Throwable exception,
                                      final SubscriberExceptionContext context) {
        logger.error("Exception occurred in event handling", exception);
    }
}
