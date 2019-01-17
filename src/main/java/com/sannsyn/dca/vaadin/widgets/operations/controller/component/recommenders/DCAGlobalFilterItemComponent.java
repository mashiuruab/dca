package com.sannsyn.dca.vaadin.widgets.operations.controller.component.recommenders;

import com.sannsyn.dca.presenter.DCADashboardPresenter;
import com.sannsyn.dca.vaadin.component.custom.logout.DCAModalComponent;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAPopupErrorComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.vaadin.ui.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mashiur on 4/7/17.
 */
public class DCAGlobalFilterItemComponent extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAGlobalFilterItemComponent.class);

    public DCAGlobalFilterItemComponent(DCALayoutContainer layoutContainer, DCADashboardPresenter dashboardPresenter) {
        setLayoutContainer(layoutContainer);
        setDashboardPresenter(dashboardPresenter);
        setCurrentComponent(this);

        this.setWidth(100, Unit.PERCENTAGE);
        this.setStyleName("global-filter-item-component");
        this.addStyleName("expand-container");

        Button manageFilterButton = new Button("Manage Filters");
        manageFilterButton.setStyleName("btn-manage-filter");

        manageFilterButton.addClickListener(event -> {
            DCAModalComponent globalFilterModalComponent = null;
            try {
                DCAGlobalFilterContainer globalFilterContainer = new DCAGlobalFilterContainer(getDashboardPresenter(),
                        getLayoutContainer());
                globalFilterModalComponent = new DCAModalComponent(globalFilterContainer);
                addComponentAsLast(globalFilterModalComponent, getLayoutContainer().getWidgetContainer());
            } catch (Exception e) {
                logger.error("Error : ", e);
                removeComponent(globalFilterModalComponent, getLayoutContainer().getWidgetContainer());
                DCAPopupErrorComponent errorComponent = new DCAPopupErrorComponent("ERROR: ", e.getMessage(),
                        getCurrentComponent());
                addComponentAsLast(errorComponent, getCurrentComponent());
            }
        });

        this.addComponent(manageFilterButton);
    }
}
