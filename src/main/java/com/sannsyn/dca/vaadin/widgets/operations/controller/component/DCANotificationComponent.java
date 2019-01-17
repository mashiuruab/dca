package com.sannsyn.dca.vaadin.widgets.operations.controller.component;

import com.vaadin.ui.ComponentContainer;

/**
 * Created by mashiur on 7/3/17.
 */
public class DCANotificationComponent extends DCAPopupMessageComponent {
    public DCANotificationComponent(String titleMessage, String subTitleMessage, ComponentContainer parentContainer) {
        super(titleMessage, subTitleMessage, parentContainer);
        this.addStyleName("popup-notification");
    }
}
