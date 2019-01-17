package com.sannsyn.dca.vaadin.widgets.operations.controller.component;

import com.vaadin.ui.ComponentContainer;

/**
 * Created by mashiur on 6/21/16.
 */
public class DCAPopupWarningComponent extends DCAPopupMessageComponent {
    public DCAPopupWarningComponent(String titleMessage, String subTitleMessage, ComponentContainer parentContainer) {
        super(titleMessage, subTitleMessage, parentContainer);
        this.addStyleName("popup-warning");
    }
}
