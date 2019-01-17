package com.sannsyn.dca.vaadin.component.custom.icon;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;

/**
 * Created by mashiur on 5/4/16.
 */
public class DCAAddNewIcon extends CssLayout {
    public DCAAddNewIcon(String secondaryStyleName) {
        Label plusLabel = new Label();
        plusLabel.setStyleName("plus-icon");
        plusLabel.setWidthUndefined();
        plusLabel.setIcon(FontAwesome.PLUS);

        this.setStyleName("add-item-icon");
        this.addStyleName(secondaryStyleName);
        this.addComponent(plusLabel);
    }

    public DCAAddNewIcon(String secondaryStyleName, String styleId) {
        this(secondaryStyleName);
        this.setId(styleId);
    }
}
