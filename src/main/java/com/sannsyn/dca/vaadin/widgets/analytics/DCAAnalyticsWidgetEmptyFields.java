package com.sannsyn.dca.vaadin.widgets.analytics;

import com.google.gson.JsonObject;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;

/**
 * Empty form fields. Does nothing. Adds nothing.
 *
 * Created by jobaer on 5/10/17.
 */
public class DCAAnalyticsWidgetEmptyFields extends CustomComponent implements DCAAnalyticsWidgetDynamicFields {
    private JsonObject item;

    DCAAnalyticsWidgetEmptyFields() {
        CssLayout root = new CssLayout();
        root.setWidth(100, Unit.PERCENTAGE);
        root.addStyleName("analytics-widget-empty-fields");
        setCompositionRoot(root);
    }
    @Override
    public void setData(JsonObject data) {
        this.item = data;
    }

    @Override
    public JsonObject getData() {
        return item;
    }
}
