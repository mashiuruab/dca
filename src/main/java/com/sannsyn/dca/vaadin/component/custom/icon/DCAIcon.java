package com.sannsyn.dca.vaadin.component.custom.icon;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;

/**
 * Created by mashiur on 3/29/16.
 */
public class DCAIcon extends Label{
    public DCAIcon(String iconClassName) {
        super(String.format("<span class='%s'></span>", iconClassName), ContentMode.HTML);
        setWidthUndefined();
        setStyleName("menu-item-icon");
    }

    public DCAIcon(String iconClass, String styleName) {
        this(iconClass);
        addStyleName(styleName);
    }

    public DCAIcon(String iconClassName, String mainClassName, String extraStyleName) {
        this(iconClassName, extraStyleName);
        setWidthUndefined();
        setStyleName(mainClassName);
    }

    public void updateValue(String newStringValue) {
        setValue(String.format("<span class='%s'></span>", newStringValue));
    }
}
