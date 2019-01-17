package com.sannsyn.dca.vaadin.widgets.operations.controller.component.pipelines;

import com.sannsyn.dca.presenter.DCADashboardPresenter;
import com.sannsyn.dca.vaadin.component.custom.field.DCATooltip;
import com.sannsyn.dca.vaadin.component.custom.icon.DCAAddNewIcon;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.pipelines.pipeline.DCAPipelineViewComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAServiceConfig;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCATaskObject;
import com.vaadin.event.LayoutEvents;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by mashiur on 4/4/16.
 */
class DCAPipeLineLayoutComponent extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAPipeLineLayoutComponent.class);

    private DCAServiceConfig serviceConfig;
    private String permission;

    DCAPipeLineLayoutComponent(DCAServiceConfig serviceConfig, String permission,
                               DCADashboardPresenter dcaDashboardPresenter, DCALayoutContainer layoutContainer) {
        this.permission = permission;
        setLayoutContainer(layoutContainer);
        setDashboardPresenter(dcaDashboardPresenter);

        this.serviceConfig = serviceConfig;
        Map<String, DCATaskObject> pipeItems = serviceConfig.getEnsembles().getTasks();

        this.setId("recommender-wrapper-id");
        this.setStyleName("recommender-wrapper");
        this.removeStyleName("show");
        this.addStyleName("show");

        Label headerLabel = new Label("Pipelines", ContentMode.HTML);
        headerLabel.setWidthUndefined();
        headerLabel.setStyleName("recommender-header dca-widget-title");

        CssLayout headerContainer = new CssLayout();
        headerContainer.setStyleName("recommender-header-container");
        headerContainer.setId("pipe-toggle-id");

        CssLayout addNewPipeIcon = new DCAAddNewIcon("", "pipe-new-item");
        DCATooltip tooltipComponent = new DCATooltip("Add New PipeLine", "add-new-pipeline");
        addNewPipeIcon.addComponent(tooltipComponent);

        headerContainer.addComponent(headerLabel);

        if (this.permission.contains("w")) {
            headerContainer.addComponent(addNewPipeIcon);
        }

        headerContainer.addLayoutClickListener((LayoutEvents.LayoutClickListener) event -> {
            if (event.getChildComponent() != null && "pipe-new-item".equals(event.getChildComponent().getId())) {
                updateWidgetContainer(event.getChildComponent().getId());
            }
        });

        this.addComponent(headerContainer);

        List<String> headers = Arrays.asList("Name", "Faucet", "Description");
        DCAPipeLineItemComponent headerItems = new DCAPipeLineItemComponent(headers);
        this.addComponent(headerItems);

        CssLayout recommenderWrapper = new CssLayout();
        recommenderWrapper.setStyleName("item-wrapper");
        int counter = 0;
        for (Map.Entry<String, DCATaskObject> entry : pipeItems.entrySet()) {
            DCAPipeLineItemComponent recommenderItem = new DCAPipeLineItemComponent(entry.getKey(), entry.getValue());
            if (counter % 2 == 0) {
                recommenderItem.addStyleName("alternating-gray-color");
            }
            recommenderWrapper.addComponent(recommenderItem);
            counter++;
        }

        recommenderWrapper.addLayoutClickListener((LayoutEvents.LayoutClickListener) event -> {
            if (event.getChildComponent() != null && event.getChildComponent().getId() != null) {
                updateWidgetContainer(event.getChildComponent().getId());
            }
        });

        this.addComponent(recommenderWrapper);
    }

    @Override
    public void updateWidgetContainer(String clickedComponentId) {
        if ("pipe-new-item".equals(clickedComponentId)) {
            getLayoutContainer().getWidgetContainer().removeAllComponents();
            DCAPipelineViewComponent dcaPipelineViewComponent = new DCAPipelineViewComponent("", serviceConfig,
                    getDashboardPresenter(), getLayoutContainer(), "", this.permission);
            getLayoutContainer().getWidgetContainer().addComponent(dcaPipelineViewComponent);
        } else {
            getLayoutContainer().getWidgetContainer().removeAllComponents();
            DCAPipelineViewComponent dcaPipelineViewComponent = new DCAPipelineViewComponent(clickedComponentId, serviceConfig,
                    getDashboardPresenter(), getLayoutContainer(), "", this.permission);
            getLayoutContainer().getWidgetContainer().addComponent(dcaPipelineViewComponent);
        }
    }
}
