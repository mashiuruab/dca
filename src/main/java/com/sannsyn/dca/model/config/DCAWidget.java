package com.sannsyn.dca.model.config;

import com.google.gson.JsonObject;

import java.util.List;

/**
 * Created by mashiur on 3/3/16.
 */
public class DCAWidget {
    private String name;
    private String mode;
    private Integer widthPercentage;
    private List<DCAWidget> children;
    private List<String> availableServices;
    private JsonObject jsonConfig;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Integer getWidthPercentage() {
        return widthPercentage;
    }


    public List<DCAWidget> getChildren() {
        return children;
    }

    public List<String> getAvailableServices() {
        return availableServices;
    }

    public JsonObject getJsonConfig() {
        return jsonConfig;
    }

    public void setJsonConfig(JsonObject jsonConfig) {
        this.jsonConfig = jsonConfig;
    }
}
