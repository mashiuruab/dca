package com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates;

import com.google.gson.JsonObject;
import com.sannsyn.dca.service.DCAPipesConfigParser;

import java.util.Map;

/**
 * Created by mashiur on 5/13/16.
 */
public class DCAElements {
    private Map<String, DCAClassWrapper> faucets;

    public Map<String, DCAClassWrapper> getFaucets() {
        return faucets;
    }

    private Map<String, JsonObject> sources;

    public Map<String, JsonObject> getSources() {
        return sources;
    }
}
