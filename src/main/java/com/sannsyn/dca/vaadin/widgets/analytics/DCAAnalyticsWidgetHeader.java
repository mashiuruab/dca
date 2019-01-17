package com.sannsyn.dca.vaadin.widgets.analytics;

import com.google.gson.JsonObject;
import com.sannsyn.dca.vaadin.component.custom.field.DCATooltip;
import com.sannsyn.dca.vaadin.component.custom.icon.DCAAddNewIcon;
import com.sannsyn.dca.vaadin.ui.DCAUiHelper;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;

/**
 * The header component for analytics widget.
 * <p>
 * Created by jobaer on 5/9/17.
 */
class DCAAnalyticsWidgetHeader extends CustomComponent {

    DCAAnalyticsWidgetHeader(UI currentUi,
                             CssLayout layout, Runnable refreshAction,
                             Runnable successMsgAction,
                             Runnable errorMsgAction, boolean isReadOnly) {
        CssLayout headerWrapper = new CssLayout();
        headerWrapper.addStyleName("analytics-widget-header");
        headerWrapper.setWidth(100, PERCENTAGE);
        Label heading = new Label("Overview of available statistics");
        heading.setWidth(95, PERCENTAGE);
        headerWrapper.addComponent(heading);

        attachAddIcon(currentUi, layout, refreshAction, successMsgAction, errorMsgAction, headerWrapper, isReadOnly);
        heading.addStyleName("dca-widget-title-container");
        setCompositionRoot(headerWrapper);
    }

    private void attachAddIcon(UI currentUi, CssLayout layout, Runnable refreshAction, Runnable successMsgAction,
                               Runnable errorMsgAction, CssLayout headerWrapper, boolean isReadOnly) {
        if (isReadOnly) return;

        DCAAddNewIcon addIcon = new DCAAddNewIcon("plus-icon-without-margin add-new-analytics-button");
        DCATooltip tooltipComponent = new DCATooltip("Add New Analytics", "add-new-analytics");
        addIcon.addComponent(tooltipComponent);

        addIcon.addLayoutClickListener(event ->
            new DCAEditAnalyticsDialog(currentUi, layout, new JsonObject(), refreshAction, successMsgAction, errorMsgAction));

        CssLayout wrapper = DCAUiHelper.wrapWithCssLayout(addIcon, "add-new-analytics-wrapper", 5);
        headerWrapper.addComponent(wrapper);
    }

}
