package com.sannsyn.dca.vaadin.component.custom.field;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;

/**
 * Created by mashiur on 4/28/16.
 */
public class DCAError extends Label {
    public DCAError(String message) {
        this.setStyleName("dca-error-message");
        this.setValue(message);
        this.setContentMode(ContentMode.HTML);
        this.setWidthUndefined();
    }
}
