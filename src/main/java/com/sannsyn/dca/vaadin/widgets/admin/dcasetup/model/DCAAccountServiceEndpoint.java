package com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model;

/**
 * Created by mashiur on 6/27/16.
 */
public class DCAAccountServiceEndpoint {
    private String uuid;
    private String name;
    private String type;
    private String protocol;
    private String host;
    private String endpointAddress;

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getHost() {
        return host;
    }

    public String getEndpointAddress() {
        return endpointAddress;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setEndpointAddress(String endpointAddress) {
        this.endpointAddress = endpointAddress;
    }
}
