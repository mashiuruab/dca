package com.sannsyn.dca.vaadin.widgets.operations.controller.component.recommenders;

import com.vaadin.ui.CssLayout;

/**
 * Created by mashiur on 2/24/17.
 */
public class DCAVerticalDotComponent extends CssLayout {

    public DCAVerticalDotComponent() {
        this.setStyleName("vertical-dot-container");

        CssLayout firstDot = new CssLayout();
        firstDot.setStyleName("dot");

        CssLayout secondDot = new CssLayout();
        secondDot.setStyleName("dot");

        CssLayout thirdDot = new CssLayout();
        thirdDot.setStyleName("dot");

        this.addComponent(firstDot);
        this.addComponent(secondDot);
        this.addComponent(thirdDot);
    }
}
