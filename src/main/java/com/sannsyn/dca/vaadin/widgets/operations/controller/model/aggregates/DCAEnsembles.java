package com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;

/**
 * Created by mashiur on 4/4/16.
 */
public class DCAEnsembles {
    private Map<String, DCATaskObject> tasks;
    private DCAElements elements;
    private Map<String, JsonElement> assembles;
    private DCAGlobalFilter globalFilters;

    public DCAElements getElements() {
        return elements;
    }

    public Map<String, DCATaskObject> getTasks() {
        return tasks;
    }

    public Map<String, JsonElement> getAssembles() {
        return assembles;
    }

    public DCAGlobalFilter getGlobalFilters() {
        return globalFilters;
    }
}
