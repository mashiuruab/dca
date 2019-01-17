package com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates;

/**
 * Created by mashiur on 4/7/16.
 */
public class DCAAggregatePopularityTerm {
    private String halftime;
    private String weight;

    public String getHalftime() {
        return halftime;
    }

    public String getWeight() {
        return weight;
    }

    public void setHalftime(final String pHalftime) {
        halftime = pHalftime;
    }

    public void setWeight(final String pWeight) {
        weight = pWeight;
    }
}
