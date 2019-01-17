package com.sannsyn.dca.vaadin.widgets.operations.controller.component.aggregates;

import com.sannsyn.dca.model.config.DCAWidget;
import com.sannsyn.dca.presenter.DCADashboardPresenter;
import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.operations.controller.DCAAggregatesWidget;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAServiceConfigWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by mashiur on 5/2/16.
 */
public class DCAAggregateContainerComponent extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAAggregateContainerComponent.class);

    private String permission;

    public DCAAggregateContainerComponent(Map<String, DCAWidget> permissionMap, DCADashboardPresenter dcaDashboardPresenter, DCALayoutContainer layoutContainer) {
        this.permission = permissionMap.get(DCAAggregatesWidget.AGGREGATES_CONFIG_KEY).getMode();
        setDashboardPresenter(dcaDashboardPresenter);
        setLayoutContainer(layoutContainer);

        this.setStyleName("aggregate-container");
    }

    public void onNext(DCAServiceConfigWrapper pDCAServiceConfigWrapper) {
        try {
            DCAAggregatesComponent dcaAggregatesComponent = new DCAAggregatesComponent
                    (pDCAServiceConfigWrapper.getService(), permission, getDashboardPresenter(), getLayoutContainer());
            addComponentAsLast(dcaAggregatesComponent, this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void onError(Throwable throwable) {
        throwable.printStackTrace();
        logger.error(throwable.getMessage());
        addComponentAsLast(new DCAError("Error Happened While Fetching Aggregates"), this);
    }

    @Override
    public void updateWidgetContainer(String clickedComponentId) {

    }
}
