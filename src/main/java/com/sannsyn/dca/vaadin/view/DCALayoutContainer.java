package com.sannsyn.dca.vaadin.view;

import com.google.common.eventbus.Subscribe;
import com.sannsyn.dca.vaadin.event.DCAPopupNotificationEvent;
import com.sannsyn.dca.vaadin.event.DCAPopupNotificationEventBus;
import com.sannsyn.dca.vaadin.widgets.common.DCABreadCrumb;
import com.sannsyn.dca.vaadin.widgets.common.DCABreadCrumbImpl;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCANotificationComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAPopupErrorComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAPopupMessageComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAPopupWarningComponent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.UI;

/**
 * Created by mashiur on 8/4/16.
 */
public class DCALayoutContainer {
    private CssLayout topPanelContainer;
    private CssLayout leftPanelContainer;
    private CssLayout widgetContainer;
    private CssLayout bodyContainer;

    private DCABreadCrumb breadCrumb = new DCABreadCrumbImpl();

    public CssLayout getTopPanelContainer() {
        return topPanelContainer;
    }

    public void setTopPanelContainer(CssLayout topPanelContainer) {
        this.topPanelContainer = topPanelContainer;
    }

    public CssLayout getLeftPanelContainer() {
        return leftPanelContainer;
    }

    public void setLeftPanelContainer(CssLayout leftPanelContainer) {
        this.leftPanelContainer = leftPanelContainer;
    }

    public CssLayout getWidgetContainer() {
        return widgetContainer;
    }

    public CssLayout getBodyContainer() {
        return bodyContainer;
    }

    public void setBodyContainer(CssLayout bodyContainer) {
        this.bodyContainer = bodyContainer;
    }

    public void setWidgetContainer(CssLayout widgetContainer) {
        this.widgetContainer = widgetContainer;
        DCAPopupNotificationEventBus.register(this);
    }

    public void setBreadCrumb(DCABreadCrumb breadCrumb) {
        this.breadCrumb = breadCrumb;
    }

    public DCABreadCrumb getBreadCrumb() {
        return this.breadCrumb;
    }

    private void addComponentToWidgetContainer(Component component) {
        UI.getCurrent().access(() -> {
           getWidgetContainer().addComponent(component);
        });
    }

    @Subscribe
    public void showSuccessNotification(DCAPopupNotificationEvent.SuccessEvent event) {
        DCAPopupMessageComponent successComponent =
                new DCAPopupMessageComponent("SUCCESS: ", event.getMessage(), getWidgetContainer());
        addComponentToWidgetContainer(successComponent);
    }

    @Subscribe
    public void showErrorNotification(DCAPopupNotificationEvent.ErrorEvent event) {
        DCAPopupErrorComponent errorComponent =
                new DCAPopupErrorComponent("ERROR: ", event.getMessage(), getWidgetContainer());
        addComponentToWidgetContainer(errorComponent);
    }

    @Subscribe
    public void showWarningNotification(DCAPopupNotificationEvent.WarningEvent event) {
        DCAPopupWarningComponent warningComponent =
                new DCAPopupWarningComponent("WARNING: ", event.getMessage(), getWidgetContainer());
        addComponentToWidgetContainer(warningComponent);
    }

    @Subscribe
    public void showNotification(DCAPopupNotificationEvent.NotificationEvent event) {
        DCANotificationComponent notificationComponent =
                new DCANotificationComponent("NOTIFICATION: ", event.getMessage(), getBodyContainer());
        UI.getCurrent().access(() -> {
            bodyContainer.addComponent(notificationComponent);
        });
    }
}
