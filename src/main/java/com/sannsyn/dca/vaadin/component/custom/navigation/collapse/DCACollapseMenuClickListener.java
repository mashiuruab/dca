package com.sannsyn.dca.vaadin.component.custom.navigation.collapse;

import com.sannsyn.dca.vaadin.event.DCADashboardEvent;
import com.sannsyn.dca.vaadin.event.DCADashboardEventBus;
import com.vaadin.event.LayoutEvents;

/**
 * Created by mashiur on 2/29/16.
 */
public class DCACollapseMenuClickListener implements LayoutEvents.LayoutClickListener {
    private DCACollapseMenuObserver dcaCollapseMenuObserver;
    private DCACollapseMenuItem clickedItem;

    public DCACollapseMenuClickListener(DCACollapseMenuObserver dcaCollapseMenuObserver, DCACollapseMenuItem pClickedItem) {
        this.dcaCollapseMenuObserver = dcaCollapseMenuObserver;
        this.clickedItem = pClickedItem;
    }

    @Override
    public void layoutClick(final LayoutEvents.LayoutClickEvent event) {
        this.clickedItem.setCollapsed(!this.clickedItem.isCollapsed());

        dcaCollapseMenuObserver.notifyObservers(this.clickedItem.isCollapsed());

        if(!this.clickedItem.isCollapsed()) {
            DCADashboardEventBus.post(new DCADashboardEvent.MenuCollapseEvent());
        } else {
            DCADashboardEventBus.post(new DCADashboardEvent.MenuExpandEvent());
        }

    }
}
