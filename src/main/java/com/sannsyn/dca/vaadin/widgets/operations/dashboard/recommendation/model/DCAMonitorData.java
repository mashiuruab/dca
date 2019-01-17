package com.sannsyn.dca.vaadin.widgets.operations.dashboard.recommendation.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mashiur on 3/8/16.
 */
public class DCAMonitorData {
    private List<DCAContext> contexts = new ArrayList<>();
    private String type;

    public List<DCAContext> getContexts() {
        return contexts;
    }

    public String getType() {
        return type;
    }
}
