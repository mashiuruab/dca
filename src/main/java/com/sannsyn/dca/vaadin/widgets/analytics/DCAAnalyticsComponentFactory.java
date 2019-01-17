package com.sannsyn.dca.vaadin.widgets.analytics;

/**
 * Factory class for creating dynamic forms for the analytics widget.
 * <p>
 * Created by jobaer on 5/10/17.
 */
class DCAAnalyticsComponentFactory {
    static final String SALES_BY_REC = "SalesByRecommendation";

    static DCAAnalyticsWidgetDynamicFields createFieldsFor(String type) {
        if (SALES_BY_REC.equals(type)) {
            return new DCASalesByRecommendationFields();
        } else {
            return new DCAAnalyticsWidgetEmptyFields();
        }
    }
}
