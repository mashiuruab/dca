package com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model;

import com.sannsyn.dca.model.config.DCASelectedService;

import java.util.List;

/**
 * Created by mashiur on 7/13/16.
 */
public class DCAResponseEntity {
    private String status;
    private String uuid;
    private String logoUrl;
    private DCASelectedService service;
    private List<DCAUserEntity> configurations;

    public String getStatus() {
        return status;
    }

    public String getUuid() {
        return uuid;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public DCASelectedService getService() {
        return service;
    }

    public List<DCAUserEntity> getConfigurations() {
        return configurations;
    }
}
