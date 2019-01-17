package com.sannsyn.dca.vaadin.component.custom.field;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;

/**
 * Created by mashiur on 4/28/16.
 */
public class DCALabel extends Label {

    public DCALabel() {
        this.setWidthUndefined();
    }
    public DCALabel(String htmlContent, String primaryStyleName) {
        this.setValue(htmlContent);
        this.setContentMode(ContentMode.HTML);
        this.setStyleName(primaryStyleName);
        this.setWidthUndefined();
    }

    public DCALabel(String htmlContent, String primaryStyleName, String secondaryStyleName) {
        this(htmlContent, primaryStyleName);
        this.addStyleName(secondaryStyleName);
    }
}
