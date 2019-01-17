package com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mashiur on 5/13/16.
 */
public class DCAFallbackAggregateWrapper {
    private List<Object> aggregate;

    public List<Object> getAggregate() {
        List<Object> aggregateList = new ArrayList<>();

        for (Object object : aggregate) {
            if (object instanceof String) {
                aggregateList.add(object);
            } else if (object instanceof List) {
                aggregateList.add(object);
            }
            /*Skipped all the objects other than STRING and ARRAY*/
        }
        return aggregateList;
    }
}
