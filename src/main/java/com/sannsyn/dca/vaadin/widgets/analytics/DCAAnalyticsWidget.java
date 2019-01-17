package com.sannsyn.dca.vaadin.widgets.analytics;

import com.sannsyn.dca.model.config.DCAWidget;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAPopupErrorComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAPopupMessageComponent;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.UI;

import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;

/**
 * The analytics widget.
 * <p>
 * Created by jobaer on 3/8/17.
 */
public class DCAAnalyticsWidget extends CustomComponent {
    private final DCAWidget config;
    private CssLayout layout = new CssLayout();

    public DCAAnalyticsWidget(UI current, DCAWidget widgetConfig) {
        layout.setWidth(100, PERCENTAGE);
        layout.addStyleName("analytics-widget");
        this.config = widgetConfig;

        boolean isReadOnly = !isEditEnabled();
        DCAAnalyticsListComponent listComponent = new DCAAnalyticsListComponent(current, this::showSuccessMessage,
            this::showErrorMessage, isReadOnly);
        DCAAnalyticsWidgetHeader header = new DCAAnalyticsWidgetHeader(current, layout,
            listComponent::refresh, this::showSuccessMessage, this::showErrorMessage, isReadOnly);

        layout.addComponent(header);
        layout.addComponent(listComponent);

        setCompositionRoot(layout);
    }

    private void showSuccessMessage() {
        DCAPopupMessageComponent successMessageComponent =
            new DCAPopupMessageComponent("Success:", "Save successful", layout);
        layout.addComponent(successMessageComponent);
    }

    private void showErrorMessage() {
        DCAPopupErrorComponent successMessageComponent =
            new DCAPopupErrorComponent("Failure:", "Save failed", layout);
        layout.addComponent(successMessageComponent);
    }

    private boolean isEditEnabled() {
        return "rw".equals(config.getMode());
    }
}
