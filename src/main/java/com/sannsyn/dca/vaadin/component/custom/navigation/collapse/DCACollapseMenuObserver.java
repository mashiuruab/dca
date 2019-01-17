package com.sannsyn.dca.vaadin.component.custom.navigation.collapse;

import com.sannsyn.dca.vaadin.component.custom.navigation.DCALeftPanelContainer;
import com.vaadin.server.Page;
import com.vaadin.ui.CssLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mashiur on 2/29/16.
 */
public class DCACollapseMenuObserver {
    private DCALeftPanelContainer leftPanelLayoutContainer;

    public void attach(DCALeftPanelContainer pLeftPanelLayoutContainer) {
        this.leftPanelLayoutContainer = pLeftPanelLayoutContainer;
    }

    public void notifyObservers(final boolean isCollapsed) {
        Page.getCurrent().getJavaScript().execute("$.event.trigger({type:'menuChanged'});");
        if (isCollapsed) {
            leftPanelLayoutContainer.removeStyleName("not-collapsed");
        } else {
            leftPanelLayoutContainer.addStyleName("not-collapsed");
        }
    }
}
