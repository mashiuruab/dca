package com.sannsyn.dca.vaadin.widgets.operations.controller.component.summary;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;

import java.util.Map;

/**
 * Created by mashiur on 4/4/16.
 */
public class DCASummaryComponent extends CssLayout {
    private Label headerLabel = new Label("Service Summary", ContentMode.HTML);

    public DCASummaryComponent(Map<String, String> mapItems) {
        this.setId("controller-summary-wrapper-id");
        this.setStyleName("controller-summary-wrapper");
        this.headerLabel.setWidthUndefined();
        this.headerLabel.setStyleName("summary-header dca-widget-title-container");
        this.addComponent(headerLabel);

        for(Map.Entry<String, String> entry : mapItems.entrySet()) {
            DCASummaryItem dcaSummaryItem = new DCASummaryItem(entry.getKey(), entry.getValue());
            this.addComponent(dcaSummaryItem);
        }
    }
}
