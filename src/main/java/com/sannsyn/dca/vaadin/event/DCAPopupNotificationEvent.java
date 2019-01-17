package com.sannsyn.dca.vaadin.event;

/**
 * Created by mashiur on 7/3/17.
 */
public class DCAPopupNotificationEvent {
    public static class SuccessEvent {
        private String message;
        public SuccessEvent(String message) {
            this.message = message;
        }

        public String getMessage() {
            return this.message;
        }
    }

    public static class ErrorEvent {
        private String message;

        public ErrorEvent(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class WarningEvent {
        private String message;

        public WarningEvent(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class NotificationEvent {
        private String message;

        public NotificationEvent(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
