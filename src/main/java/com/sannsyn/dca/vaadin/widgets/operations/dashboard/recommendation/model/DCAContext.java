package com.sannsyn.dca.vaadin.widgets.operations.dashboard.recommendation.model;

import java.util.List;

/**
 * Created by mashiur on 3/8/16.
 */
public class DCAContext {
    private String average;
    private List<DCAHistogram> histograms;
    private String name;
    private Long timestamp;

    public String getAverage() {
        return average;
    }

    public List<DCAHistogram> getHistograms() {
        return histograms;
    }

    public String getName() {
        return name;
    }

    public Long getTimestamp() {
        return timestamp;
    }
}
