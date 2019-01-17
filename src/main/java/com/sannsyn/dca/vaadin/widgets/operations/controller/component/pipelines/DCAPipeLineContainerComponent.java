package com.sannsyn.dca.vaadin.widgets.operations.controller.component.pipelines;

import com.sannsyn.dca.model.config.DCAWidget;
import com.sannsyn.dca.presenter.DCADashboardPresenter;
import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAServiceConfigWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by mashiur on 5/2/16.
 */
public class DCAPipeLineContainerComponent extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAPipeLineContainerComponent.class);

    private String permission;

    public DCAPipeLineContainerComponent(Map<String, DCAWidget> permissionMap, DCADashboardPresenter dcaDashboardPresenter,
                                         DCALayoutContainer layoutContainer) {
        this.permission = permissionMap.get("pipeline").getMode();
        setLayoutContainer(layoutContainer);
        setDashboardPresenter(dcaDashboardPresenter);
        this.setStyleName("recommender-container");
    }

    public void onNext(DCAServiceConfigWrapper dcaServiceConfigWrapper) {
        try {
            DCAPipeLineLayoutComponent dcaPipeLineLayoutComponent = new DCAPipeLineLayoutComponent(
                    dcaServiceConfigWrapper.getService(), permission, getDashboardPresenter(),
                    getLayoutContainer());
            addComponentAsLast(dcaPipeLineLayoutComponent, this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void onError(Throwable throwable) {
        throwable.printStackTrace();
        logger.error(throwable.getMessage());
        addComponentAsLast(new DCAError("Error Happened While Fetching Pipelines"), this);
    }

    @Override
    public void updateWidgetContainer(String clickedComponentId) {

    }
}
