package com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by mashiur on 6/27/16.
 */
public class DCAAccountService {
    private String uuid;
    private String name;
    private String serviceIdentifier;
    private String metaDataServerUrl;
    private String analyticsServerUrl;
    private String description;
    private String status;
    private String serviceEndpoint;
    private DCAAccount account;

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return StringUtils.stripToEmpty(name);
    }

    public String getDescription() {
        return StringUtils.stripToEmpty(description);
    }

    public String getStatus() {
        return StringUtils.stripToEmpty(status);
    }

    public String getServiceEndpoint() {
        return StringUtils.stripToEmpty(serviceEndpoint);
    }

    public DCAAccount getAccount() {
        return account;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setServiceEndpoint(String serviceEndpoint) {
        this.serviceEndpoint = serviceEndpoint;
    }

    public void setAccount(DCAAccount account) {
        this.account = account;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getServiceIdentifier() {
        return StringUtils.stripToEmpty(serviceIdentifier);
    }

    public void setServiceIdentifier(String serviceIdentifier) {
        this.serviceIdentifier = serviceIdentifier;
    }

    public String getMetaDataServerUrl() {
        return StringUtils.stripToEmpty(metaDataServerUrl);
    }

    public void setMetaDataServerUrl(String metaDataServerUrl) {
        this.metaDataServerUrl = metaDataServerUrl;
    }

    public String getAnalyticsServerUrl() {
        return analyticsServerUrl;
    }

    public void setAnalyticsServerUrl(String analyticsServerUrl) {
        this.analyticsServerUrl = analyticsServerUrl;
    }
}
