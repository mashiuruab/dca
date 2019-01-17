package com.sannsyn.dca.vaadin.widgets.operations.controller.component.summary;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;

/**
 * Created by mashiur on 4/1/16.
 */
public class DCASummaryItem extends CssLayout {
    private Label name = new Label("", ContentMode.HTML);
    private Label value = new Label("", ContentMode.HTML);

    public DCASummaryItem(String name, String value) {
        this.name.setStyleName("item-label");
        this.name.setValue(name);
        this.name.setWidthUndefined();

        this.value.setStyleName("item-value");
        this.value.setValue(value);
        this.value.setWidthUndefined();

        this.addComponent(this.name);
        this.addComponent(this.value);
        this.setStyleName("controller-summary-item");
    }
}
