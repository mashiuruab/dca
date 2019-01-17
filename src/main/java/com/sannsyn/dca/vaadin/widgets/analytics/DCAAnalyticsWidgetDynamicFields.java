package com.sannsyn.dca.vaadin.widgets.analytics;

import com.google.gson.JsonObject;
import com.vaadin.ui.Component;

/**
 * A dynamic form fields interface.
 * <p>
 * Created by jobaer on 5/10/17.
 */
public interface DCAAnalyticsWidgetDynamicFields extends Component {
    /**
     * Set the form fields value from the json object.
     *
     * @param data The json object containing the field values
     */
    void setData(JsonObject data);

    /**
     * Get the current representation of the values. Any values that are not part of the form may not be preserved.
     *
     * @return current values of the fields.
     */
    JsonObject getData();
}
