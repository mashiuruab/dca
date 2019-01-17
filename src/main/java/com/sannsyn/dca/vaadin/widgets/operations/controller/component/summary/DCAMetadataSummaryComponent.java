package com.sannsyn.dca.vaadin.widgets.operations.controller.component.summary;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import org.apache.commons.lang3.StringUtils;

/**
 * Metadata summary compoennt
 * <p>
 * Created by jobaer on 22/2/17.
 */
class DCAMetadataSummaryComponent extends CssLayout {

    DCAMetadataSummaryComponent(JsonObject response) {
        this.setStyleName("controller-summary-wrapper");
        Label headerLabel = new Label("Metadata Summary", ContentMode.HTML);
        headerLabel.setWidthUndefined();
        headerLabel.setStyleName("summary-header dca-widget-title-container");
        this.addComponent(headerLabel);

        addLabelFor(response, "unfinished");
        addLabelFor(response, "successful");
        addLabelFor(response, "unsuccessful");
    }

    private void addLabelFor(JsonObject response, String propertyName) {
        if (response.has(propertyName)) {
            JsonElement unfinished = response.get(propertyName);
            String value = unfinished.toString();
            String name = StringUtils.capitalize(propertyName) + ": ";
            DCASummaryItem dcaSummaryItem = new DCASummaryItem(name, value);
            this.addComponent(dcaSummaryItem);
        }
    }
}
