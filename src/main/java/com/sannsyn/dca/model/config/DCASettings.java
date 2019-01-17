package com.sannsyn.dca.model.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mashiur on 3/3/16.
 */
public class DCASettings {
    private Map<String, Object> uiState;
    private String account;
    private DCASelectedService service;
    private String language;
    private Map<String, Object> dca = new HashMap<>();

    public Map<String, Object> getUiState() {
        return uiState;
    }

    public void setUiState(Map<String, Object> uiState) {
        this.uiState = uiState;
    }

    public String getAccount() {
        return account;
    }

    public DCASelectedService getService() {
        return service;
    }

    public void setService(DCASelectedService service) {
        this.service = service;
    }

    public String getLanguage() {
        return language;
    }

    public Map<String, Object> getDca() {
        return dca;
    }
}
