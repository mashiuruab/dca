package com.sannsyn.dca.vaadin.component.custom.navigation.item;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mashiur on 5/4/16.
 */
public class DCALeftPanelSubMenuObserver {
    private static final Logger logger = LoggerFactory.getLogger(DCALeftPanelSubMenuObserver.class);

    private static final String SELECTED_MENU_ITEM_CLS_NAME = "selected-menu-item";
    private List<DCALeftSubMenuItem> subscribersList = new ArrayList<>();

    public DCALeftPanelSubMenuObserver() {

    }

    public void attach(DCALeftSubMenuItem dcaLeftSubMenuItem) {
        this.subscribersList.add(dcaLeftSubMenuItem);
    }

    public void notifySubscribers(String clickedSubMenuItemId) {
        for (DCALeftSubMenuItem dcaLeftSubMenuItem : subscribersList) {
            dcaLeftSubMenuItem.removeStyleName(SELECTED_MENU_ITEM_CLS_NAME);

            String targetSubMenuId = dcaLeftSubMenuItem.getItem().getId();
            String subMenuId = clickedSubMenuItemId.replace("floating-", "");
            String floatingSubMenuId = clickedSubMenuItemId.contains("floating-") ? clickedSubMenuItemId : String.format("floating-%s", clickedSubMenuItemId);
            if (targetSubMenuId.equals(subMenuId) || targetSubMenuId.equals(floatingSubMenuId)) {
                dcaLeftSubMenuItem.addStyleName(SELECTED_MENU_ITEM_CLS_NAME);
            }
        }
    }

    public void resetSubscribers() {
        for (DCALeftSubMenuItem dcaLeftSubMenuItem : subscribersList) {
            dcaLeftSubMenuItem.removeStyleName(SELECTED_MENU_ITEM_CLS_NAME);
        }
    }
}
