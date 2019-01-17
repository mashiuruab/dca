package com.sannsyn.dca.vaadin.widgets.operations.controller.component.aggregates;

import com.sannsyn.dca.presenter.DCADashboardPresenter;
import com.sannsyn.dca.vaadin.component.custom.field.DCATooltip;
import com.sannsyn.dca.vaadin.component.custom.icon.DCAAddNewIcon;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.common.DCABreadCrumb;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.aggregates.form.create.DCAAggregateCreateUpdateComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAAggregateItem;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAServiceConfig;
import com.vaadin.event.LayoutEvents;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.sannsyn.dca.vaadin.widgets.operations.controller.DCAAggregatesWidget.CREATE_NEW_LABEL;

/**
 * Created by mashiur on 4/4/16.
 */
class DCAAggregatesComponent extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAAggregatesComponent.class);

    private List<String> headers = Arrays.asList("Name", "Type", "Tags", "Taxons", "Size", "Description");
    private Label headerLabel = new Label("Aggregates", ContentMode.HTML);

    private DCAServiceConfig mDCAServiceConfig;
    private String permission;

    DCAAggregatesComponent(DCAServiceConfig pDCAServiceConfig, String permission,
                           DCADashboardPresenter dcaDashboardPresenter, DCALayoutContainer layoutContainer) {
        this.mDCAServiceConfig = pDCAServiceConfig;
        this.permission = permission;
        setDashboardPresenter(dcaDashboardPresenter);
        setLayoutContainer(layoutContainer);

        init();
    }

    private void init() {
        this.setId("aggregate-wrapper-id");
        this.setStyleName("aggregate-wrapper");
        this.removeStyleName("show");
        this.addStyleName("show");

        CssLayout aggregateHeaderContainer = new CssLayout();
        aggregateHeaderContainer.setStyleName("aggregate-header-container");
        aggregateHeaderContainer.setId("aggregate-toggle-id");

        headerLabel.setStyleName("aggregate-header dca-widget-title");
        headerLabel.setWidthUndefined();

        CssLayout addNewItemComponent = new DCAAddNewIcon("", "aggregate-new-item");
        DCATooltip tooltipComponent = new DCATooltip("Add New Aggregate", "add-new-aggregate");
        addNewItemComponent.addComponent(tooltipComponent);

        aggregateHeaderContainer.addComponent(this.headerLabel);

        if (permission.contains("w")) {
            addNewItemComponent.addLayoutClickListener((LayoutEvents.LayoutClickListener) event -> updateWidgetContainer(event.getComponent().getId()));

            aggregateHeaderContainer.addComponent(addNewItemComponent);
        }

        DCAAggregatesItemComponent headerLabelComponent = new DCAAggregatesItemComponent(headers);

        CssLayout aggregateWrapper = new CssLayout();
        aggregateWrapper.setStyleName("item-wrapper");
        int counter = 0;
        for (Map.Entry<String, DCAAggregateItem> entry : this.mDCAServiceConfig.getAggregates().entrySet()) {
            DCAAggregatesItemComponent itemComponents = new DCAAggregatesItemComponent(entry.getKey(), entry.getValue(),
                    getLayoutContainer(), this.mDCAServiceConfig.getAggregateDefaults().getPopularity(), getDashboardPresenter());
            if (counter % 2 == 0) {
                itemComponents.addStyleName("alternating-gray-color");
            }
            aggregateWrapper.addComponent(itemComponents);
            counter++;
        }

        this.addComponent(aggregateHeaderContainer);
        this.addComponent(headerLabelComponent);
        this.addComponent(aggregateWrapper);
    }

    public void updateWidgetContainer(String clickedComponentId) {
        getLayoutContainer().getWidgetContainer().removeAllComponents();
        DCABreadCrumb breadCrumb = getLayoutContainer().getBreadCrumb();
        breadCrumb.addAction(CREATE_NEW_LABEL, s -> {});
        getLayoutContainer().getWidgetContainer().addComponent(breadCrumb.getView());
        getLayoutContainer().getWidgetContainer().addComponent(new DCAAggregateCreateUpdateComponent(this.mDCAServiceConfig, getDashboardPresenter(), getLayoutContainer()));
    }
}
