package com.sannsyn.dca.vaadin.widgets.operations.controller.component.aggregates.tags;

import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;

/**
 * Created by mashiur on 4/21/16.
 */
public class DCATagsLabel extends CssLayout {
    private DCALabel label = new DCALabel("", "");
    private DCALabel removeIcon = new DCALabel();

    public DCATagsLabel(String labelName) {

        this.setStyleName("tag-value-item");

        this.label.setStyleName("tag-value");
        this.label.setValue(labelName);

        this.removeIcon.setIcon(FontAwesome.REMOVE);
        this.removeIcon.setId("tag-remove-icon-id");
        this.removeIcon.setStyleName("tag-remove-icon");

        this.addComponent(label);
        this.addComponent(removeIcon);

    }

    public Label getLabel() {
        return label;
    }
}
