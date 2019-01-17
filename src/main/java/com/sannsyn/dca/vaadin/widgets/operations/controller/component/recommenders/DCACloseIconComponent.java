package com.sannsyn.dca.vaadin.widgets.operations.controller.component.recommenders;

import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.icons.SannsynIcons;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.CssLayout;

/**
 * Created by mashiur on 2/24/17.
 */
public class DCACloseIconComponent extends CssLayout {

    public DCACloseIconComponent() {
        this.setStyleName("remove-icon-wrapper");

        DCALabel removeIcon = new DCALabel("", "remove-icon");
        removeIcon.setIcon(SannsynIcons.CLOSE_LINE);

        this.addComponent(removeIcon);
    }
}
