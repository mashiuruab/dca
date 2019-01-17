package com.sannsyn.dca.vaadin.widgets.operations.controller.model.summary;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by mashiur on 4/1/16.
 */
public class DCAControllerService {
    private String accountId;
    private String status;
    private String serviceName;
    private String serviceDescription;
    private String numEntities;
    private String runningSince;
    private String monitorType;
    private String lastUpdated;


    public String getAccountId() {
        return accountId;
    }

    public String getStatus() {
        return status;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServiceDescription() {
        return serviceDescription;
    }

    public String getNumEntities() {
        return numEntities;
    }

    public String getRunningSince() {
        return runningSince;
    }

    public String getMonitorType() {
        return monitorType;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    private String getDate(String dateStr) {
        String dateString = "";
        if (StringUtils.isNotEmpty(dateStr)) {
            Date date = new Date(Long.valueOf(dateStr));
            SimpleDateFormat  simpleDateFormat =  new SimpleDateFormat("dd.MM.yyyy HH:mm aaa");
            dateString = simpleDateFormat.format(date);
        }

        return dateString;
    }

    public Map<String, String> getServiceValues() {
        String runningSinceStr = getDate(getRunningSince());
        String lastUpdatedStr = getDate(getLastUpdated());

        Map<String, String> valueMap = new LinkedHashMap<String, String>();
        valueMap.put("Account:", getAccountId());
        valueMap.put("Name:", getServiceName());
        valueMap.put("Description:", getServiceDescription());
        valueMap.put("Status:", getStatus());
        valueMap.put("#entries:", getNumEntities());
        valueMap.put("Running since:", runningSinceStr);
        valueMap.put("Last updated:", lastUpdatedStr);
        valueMap.put("Monitor type:", getMonitorType());

        return valueMap;
    }
}
