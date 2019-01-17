package com.sannsyn.dca.model.config;

import com.sannsyn.dca.util.DCAConfigProperties;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model.DCAAccount;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by mashiur on 6/29/16.
 */
public class DCASelectedService {
    private String uuid;
    private String serviceId;
    private String name;
    private String serviceIdentifier;
    private String metaDataServerUrl;
    private String analyticsServerUrl;
    private String description;
    private String status;
    private DCAServiceEndpoint serviceEndpoint;
    private DCAAccount account;

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return StringUtils.isEmpty(uuid) ? getServiceId() : uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServiceIdentifier() {
        /*TODO : This is done to initially skip the serviceIdentifier field which might not be present*/
        return (StringUtils.isNotEmpty(serviceIdentifier)) ? serviceIdentifier : getName();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public DCAServiceEndpoint getServiceEndpoint() {
        return serviceEndpoint;
    }

    public void setServiceEndpoint(DCAServiceEndpoint serviceEndpoint) {
        this.serviceEndpoint = serviceEndpoint;
    }

    public DCAAccount getAccount() {
        return account;
    }

    public void setAccount(DCAAccount account) {
        this.account = account;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getAnalyticsServerUrl() {
        return analyticsServerUrl;
    }

    public void setAnalyticsServerUrl(String analyticsServerUrl) {
        this.analyticsServerUrl = analyticsServerUrl;
    }
}
