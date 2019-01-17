package com.sannsyn.dca.vaadin.component.custom.notification;

import com.sannsyn.dca.vaadin.event.DCAPopupNotificationEvent;
import com.sannsyn.dca.vaadin.event.DCAPopupNotificationEventBus;

/**
 * Created by mashiur on 7/3/17.
 */
public enum  DCANotification {
    SUCCESS,
    ERROR,
    WARNING,
    NOTIFICATION;

    public void show(String message) {
        switch (this) {
            case SUCCESS:
                DCAPopupNotificationEventBus.post(new DCAPopupNotificationEvent.SuccessEvent(message));
                break;
            case ERROR:
                DCAPopupNotificationEventBus.post(new DCAPopupNotificationEvent.ErrorEvent(message));
                break;
            case WARNING:
                DCAPopupNotificationEventBus.post(new DCAPopupNotificationEvent.WarningEvent(message));
                break;
            case NOTIFICATION:
                DCAPopupNotificationEventBus.post(new DCAPopupNotificationEvent.NotificationEvent(message));
                break;
            default:
                break;
        }
    }
}
