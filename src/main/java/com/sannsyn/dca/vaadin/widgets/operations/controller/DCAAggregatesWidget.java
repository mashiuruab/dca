package com.sannsyn.dca.vaadin.widgets.operations.controller;

import com.sannsyn.dca.model.config.DCAWidget;
import com.sannsyn.dca.presenter.DCADashboardPresenter;
import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.servlet.DCAUserPreference;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.common.DCABreadCrumb;
import com.sannsyn.dca.vaadin.widgets.common.DCABreadCrumbImpl;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.aggregates.DCAAggregateContainerComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAServiceConfigWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The Aggregates widget.
 *
 * Created by jobaer on 10/5/16.
 */
public class DCAAggregatesWidget extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAControllerOverviewComponent.class);

    public static final String AGGREGATES_CONFIG_KEY = "aggregate";
    public static final String CREATE_NEW_LABEL = "Create new aggregate";
    public static final String AGGREGATES_LABEL = "Aggregates";

    private Observable<DCAServiceConfigWrapper> dcaControllerConfigWrapper;

    private DCABreadCrumb breadCrumb;
    private Consumer<String> navigationHelper;
    private Consumer<String> navigateToSelf;

    public DCAAggregatesWidget(DCADashboardPresenter pDCADashboardPresenter, DCALayoutContainer layoutContainer) {
        setLayoutContainer(layoutContainer);
        setDashboardPresenter(pDCADashboardPresenter);
        breadCrumb  = new DCABreadCrumbImpl();
        try {
            this.dcaControllerConfigWrapper = getDashboardPresenter().getServiceConfig(getLoggedInUser());
            init();
        } catch (Exception e) {
            this.addComponent(new DCAError("Controller Summary Component Loading Error"));
            logger.error("Error : ", e);
        }
    }

    private void init() {
        this.setStyleName("controller-overview-container");
        Observable<List<DCAWidget>> aggregateConfigWidgetObservable = getDashboardPresenter().getWidget(getLoggedInUser(),
            DCAUserPreference.getViewState(), DCAUserPreference.getNavMenuState(), AGGREGATES_CONFIG_KEY);
        aggregateConfigWidgetObservable.subscribe(this::onNext, this::onError);
    }

    private void onNext(List<DCAWidget> aggregateConfigWidgetList) {
        Map<String, DCAWidget> permissionMap = new HashMap<>();

        for (DCAWidget dcaWidget : aggregateConfigWidgetList) {
            permissionMap.put(dcaWidget.getName(), dcaWidget);
        }
        DCAAggregateContainerComponent dcaAggregateContainerComponent = new DCAAggregateContainerComponent(permissionMap, getDashboardPresenter(), getLayoutContainer());
        prepareBreadCrumb();
        this.addComponent(dcaAggregateContainerComponent);

        this.dcaControllerConfigWrapper.subscribe(dcaAggregateContainerComponent::onNext, dcaAggregateContainerComponent::onError);
    }

    private void prepareBreadCrumb() {
        breadCrumb.addAction("Controller", navigationHelper);
        breadCrumb.addAction(AGGREGATES_LABEL, navigateToSelf);
        this.addComponent(breadCrumb.getView());
        getLayoutContainer().setBreadCrumb(breadCrumb);
    }

    private void onError(Throwable throwable) {
        logger.error("Error : ", throwable);
        addComponentAsLast(new DCAError("Error happened in Controller/Aggregates page"), this);
    }

    // todo remove these two methods and use addNavigationAction instead
    public void setNavigationHelper(Consumer<String> navigationHelper) {
        this.navigationHelper = navigationHelper;
    }

    public void setSelfNavigation(Consumer<String> navigationHelper) {
        this.navigateToSelf = navigationHelper;
    }
}
