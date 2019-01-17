package com.sannsyn.dca.vaadin.widgets.operations.controller.component.aggregates.form.create;

/**
 * Created by mashiur on 4/18/16.
 */
public class DCAAggregateCreateResponse {
    private String messageId;
    private String accountId;
    private String requestMessageId;
    private String okMessage;
    private Integer statusCode;
    private String Error;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getRequestMessageId() {
        return requestMessageId;
    }

    public void setRequestMessageId(String requestMessageId) {
        this.requestMessageId = requestMessageId;
    }

    public String getOkMessage() {
        return okMessage;
    }

    public void setOkMessage(String okMessage) {
        this.okMessage = okMessage;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getError() {
        return Error;
    }
}
