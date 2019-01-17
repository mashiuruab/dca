package com.sannsyn.dca.vaadin.component.custom;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;

import java.util.List;

/**
 * Created by mashiur on 4/15/16.
 */
public class DCAWrapper extends CssLayout {
    public DCAWrapper(List<Component> componentList, String primaryStyleName) {
        this.setStyleName(primaryStyleName);
        componentList.forEach(this::addComponent);
    }
}
