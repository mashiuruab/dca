package com.sannsyn.dca.vaadin.inspectaggregate;

import com.sannsyn.dca.service.AggregateQuery;

/**
 * This will handle the search input fields of inspect aggregate.
 * <p>
 * Created by jobaer on 6/7/16.
 */
interface DCAInspectAggregateSearchInputHandler {
    void search(AggregateQuery query);

    void switchToView(String viewName);
}
