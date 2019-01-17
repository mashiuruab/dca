package com.sannsyn.dca.vaadin.widgets.operations.controller.component.pipelines.pipeline;

import com.sannsyn.dca.vaadin.view.DCALayout;
import com.vaadin.ui.Button;

/**
 * Created by mashiur on 11/14/16.
 */
public class DCARemoveButtonComponent extends DCALayout {
    private Button button = new Button();

    public DCARemoveButtonComponent() {
        this.setStyleName("remove-icon-wrapper");
        this.addComponent(button);
    }

    public Button getButton() {
        return button;
    }
}
