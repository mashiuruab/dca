package com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates;

import com.google.gson.annotations.SerializedName;

/**
 * Created by mashiur on 4/7/16.
 */
public class DCAAggregatePopularity {
    private String maxCacheAge;
    @SerializedName("long")
    private DCAAggregatePopularityTerm longTerm;
    @SerializedName("short")
    private DCAAggregatePopularityTerm shortTerm;

    public String getMaxCacheAge() {
        return maxCacheAge;
    }

    public DCAAggregatePopularityTerm getLongTerm() {
        return longTerm;
    }

    public DCAAggregatePopularityTerm getShortTerm() {
        return shortTerm;
    }

    public void setMaxCacheAge(final String pMaxCacheAge) {
        maxCacheAge = pMaxCacheAge;
    }

    public void setLongTerm(final DCAAggregatePopularityTerm pLongTerm) {
        longTerm = pLongTerm;
    }

    public void setShortTerm(final DCAAggregatePopularityTerm pShortTerm) {
        shortTerm = pShortTerm;
    }
}
