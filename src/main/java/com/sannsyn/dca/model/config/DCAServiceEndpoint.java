package com.sannsyn.dca.model.config;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by mashiur on 6/29/16.
 */
public class DCAServiceEndpoint {
    private String uuid;
    private String serviceEndPointId;
    private String name;
    private String endpointAddress;

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return StringUtils.isEmpty(uuid) ? getServiceEndPointId() : uuid;
    }

    public String getName() {
        return name;
    }

    public String getEndpointAddress() {
        return endpointAddress;
    }

    public String getServiceEndPointId() {
        return serviceEndPointId;
    }

    public void setServiceEndPointId(String serviceEndPointId) {
        this.serviceEndPointId = serviceEndPointId;
    }
}
