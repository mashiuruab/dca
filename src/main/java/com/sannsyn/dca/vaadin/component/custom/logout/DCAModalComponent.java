package com.sannsyn.dca.vaadin.component.custom.logout;

import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.vaadin.ui.CssLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mashiur on 11/28/16.
 */
public class DCAModalComponent extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAModalComponent.class);

    private DCALabel overLayComponent = new DCALabel("", "modal-overlay");

    public DCAModalComponent(CssLayout popupComponent) {
        this.setStyleName("modal-component-wrapper");

        try {
            addComponentAsLast(popupComponent, this);
            addComponentAsLast(overLayComponent, this);
        } catch (Exception e) {
            logger.error("Error : ", e);
            addComponentAsLast(new DCAError(e.getMessage()), this);
        }
    }

}
