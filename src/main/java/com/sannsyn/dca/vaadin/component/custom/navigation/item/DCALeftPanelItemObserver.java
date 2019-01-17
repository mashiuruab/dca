package com.sannsyn.dca.vaadin.component.custom.navigation.item;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mashiur on 2/26/16.
 */
public class DCALeftPanelItemObserver {
    private static final String SELECTED_MENU_ITEM_CLS_NAME = "selected-menu-item";

    private List<DCALeftPanelItem> leftPanelItems = new ArrayList<DCALeftPanelItem>();

    public DCALeftPanelItemObserver() {
    }

    public void attach(final DCALeftPanelItem leftPanelItem) {
        this.leftPanelItems.add(leftPanelItem);
    }

    public void notifyObservers(final String mainMenuId) {
        for (DCALeftPanelItem leftPanelItem : leftPanelItems) {
            leftPanelItem.getMainMenuItem().removeStyleName(SELECTED_MENU_ITEM_CLS_NAME);
            leftPanelItem.getFloatingMainMenuItemWrapper().removeStyleName(SELECTED_MENU_ITEM_CLS_NAME);

            if (leftPanelItem.getMainMenuItem().getId().equals(mainMenuId) ||
                    leftPanelItem.getFloatingMainMenuItemWrapper().getId().equals(mainMenuId)) {
                leftPanelItem.getMainMenuItem().addStyleName(SELECTED_MENU_ITEM_CLS_NAME);
                leftPanelItem.getFloatingMainMenuItemWrapper().addStyleName(SELECTED_MENU_ITEM_CLS_NAME);
            }
        }
    }

    public void resetSubscribers() {
        for (DCALeftPanelItem dcaLeftPanelItem : leftPanelItems) {
            dcaLeftPanelItem.getMainMenuItem().removeStyleName(SELECTED_MENU_ITEM_CLS_NAME);
            dcaLeftPanelItem.getFloatingMainMenuItemWrapper().removeStyleName(SELECTED_MENU_ITEM_CLS_NAME);
        }
    }
}
