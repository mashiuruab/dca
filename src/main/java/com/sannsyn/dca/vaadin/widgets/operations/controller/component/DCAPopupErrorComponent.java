package com.sannsyn.dca.vaadin.widgets.operations.controller.component;

import com.vaadin.ui.ComponentContainer;

/**
 * Created by mashiur on 6/21/16.
 */
public class DCAPopupErrorComponent extends DCAPopupMessageComponent {
    public DCAPopupErrorComponent(String titleMessage, String subTitleMessage, ComponentContainer parentContainer) {
        super(titleMessage, subTitleMessage, parentContainer);
        this.addStyleName("popup-error");
    }
}
