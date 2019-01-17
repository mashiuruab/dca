package com.sannsyn.dca.model.config;

/**
 * Created by mashiur on 7/14/16.
 */
public class DCAConfigEntity {
    private String status;
    private String uuid;
    private DCAConfigWrapper PADCAConfiguration;

    public String getStatus() {
        return status;
    }

    public DCAConfigWrapper getPADCAConfiguration() {
        return PADCAConfiguration;
    }
}
