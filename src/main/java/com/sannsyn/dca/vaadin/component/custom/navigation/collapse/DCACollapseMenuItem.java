package com.sannsyn.dca.vaadin.component.custom.navigation.collapse;

import com.sannsyn.dca.vaadin.component.custom.icon.DCAIcon;
import com.sannsyn.dca.vaadin.component.custom.navigation.collapse.DCACollapseMenuClickListener;
import com.sannsyn.dca.vaadin.component.custom.navigation.collapse.DCACollapseMenuObserver;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;

/**
 * Created by mashiur on 2/29/16.
 */
public class DCACollapseMenuItem extends CssLayout {
    private Label collapseInIcon = new DCAIcon("icon-collapse-in", "collapse-in");
    private Label collapseOutIcon = new DCAIcon("icon-collapse-out", "collapse-out");
    private Link label = new Link();

    private boolean isCollapsed = true;
    private DCACollapseMenuObserver dcaCollapseMenuObserver;

    public DCACollapseMenuItem(String labelCaption, DCACollapseMenuObserver dcaCollapseMenuObserver) {
        this.label.setCaption(labelCaption);
        init();
        this.dcaCollapseMenuObserver = dcaCollapseMenuObserver;
        DCACollapseMenuClickListener dcaCollapseMenuListener = new DCACollapseMenuClickListener(dcaCollapseMenuObserver, this);
        this.addLayoutClickListener(dcaCollapseMenuListener);
    }

    public boolean isCollapsed() {
        return isCollapsed;
    }

    public void setCollapsed(final boolean pIsCollapsed) {
        isCollapsed = pIsCollapsed;
    }

    private void init() {
        this.label.setStyleName("menu-item-label");
        this.setStyleName("collapse-menu-item-wrapper");

        this.addComponent(this.collapseInIcon);
        this.addComponent(this.collapseOutIcon);
        this.addComponent(this.label);

    }
}
