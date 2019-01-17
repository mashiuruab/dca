package com.sannsyn.dca.vaadin.widgets.operations.dashboard.recommendation.model;

import java.util.Collections;
import java.util.List;

/**
 * Created by mashiur on 3/8/16.
 */
public class DCAHistogram {
    private List<Integer> histogram;
    private String average;
    private String denomination;

    public List<Integer> getHistogram() {
        Collections.reverse(histogram);
        return histogram;
    }

    public String getAverage() {
        return average;
    }

    public String getDenomination() {
        return denomination;
    }
}
