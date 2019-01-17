package com.sannsyn.dca.vaadin.component.custom.icon;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;

/**
 * Simple icon
 * <p>
 * Created by jobaer on 8/5/17.
 */
public class DCASimpleIcon extends Label {
    public DCASimpleIcon(String iconClassName) {
        super(String.format("<span class='%s'></span>", iconClassName), ContentMode.HTML);
        setWidthUndefined();
    }

    public DCASimpleIcon(String iconClass, String styleName) {
        this(iconClass);
        addStyleName(styleName);
    }

    public void updateValue(String newStringValue) {
        setValue(String.format("<span class='%s'></span>", newStringValue));
    }
}
