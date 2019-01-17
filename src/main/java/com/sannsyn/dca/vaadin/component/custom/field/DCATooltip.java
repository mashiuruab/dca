package com.sannsyn.dca.vaadin.component.custom.field;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;

/**
 * Created by mashiur on 5/3/16.
 */
public class DCATooltip extends Label {
    public DCATooltip(String toolTipText, String secondaryStyleName) {
        this.setContentMode(ContentMode.HTML);
        this.setValue(toolTipText);
        this.setWidthUndefined();
        this.setStyleName("add-new-plus");
        this.addStyleName(secondaryStyleName);
    }
}
