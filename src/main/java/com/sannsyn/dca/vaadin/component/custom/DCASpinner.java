package com.sannsyn.dca.vaadin.component.custom;

import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.ui.Image;

/**
 * Created by mashiur on 3/23/17.
 */
public class DCASpinner extends DCAWidgetContainerComponent {
    private static final String SPINNER_IMG_URI = "static/img/spinner_animation02.gif";

    public DCASpinner() {
        this.setStyleName("spinner-container");

        Resource imageResource = new ExternalResource(SPINNER_IMG_URI);
        Image image = new Image("", imageResource);

        this.addComponent(image);
    }
}
