package com.sannsyn.dca.vaadin.component.custom;

import com.sannsyn.dca.vaadin.widgets.common.DCABreadCrumb;
import com.sannsyn.dca.vaadin.widgets.common.DCABreadCrumbImpl;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Embedded;

/**
 * Created by mashiur on 9/26/16.
 */
public class DCAHelpContainerComponent extends DCAWidgetContainerComponent {

    private static final String STATIC_HELP_PAGE_URI = "/static/html/help.html";

    public DCAHelpContainerComponent(CssLayout widgetContainer) {
        this.setStyleName("help-container");
        Embedded browser = new Embedded();
        browser.setSource(new ExternalResource(STATIC_HELP_PAGE_URI));
        browser.setType(Embedded.TYPE_BROWSER);
        browser.setStyleName("help-iframe-container");

        DCABreadCrumb breadCrumb = new DCABreadCrumbImpl();
        breadCrumb.addAction("Help", s -> navigateToSelf(widgetContainer));
        this.addComponent(breadCrumb.getView());
        this.addComponent(browser);
    }

    private void navigateToSelf(CssLayout widgetContainer) {
        widgetContainer.removeAllComponents();
        DCAHelpContainerComponent helpContainerComponent = new DCAHelpContainerComponent(widgetContainer);
        addComponentAsLast(helpContainerComponent, widgetContainer);
    }
}
