package com.sannsyn.dca.vaadin.widgets.operations.controller;

import com.sannsyn.dca.metadata.DCAMetadataServiceClient;
import com.sannsyn.dca.metadata.DCAMetadataServiceClientImpl;
import com.sannsyn.dca.model.config.DCAWidget;
import com.sannsyn.dca.presenter.DCADashboardPresenter;
import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.servlet.DCAUserPreference;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.common.DCABreadCrumb;
import com.sannsyn.dca.vaadin.widgets.common.DCABreadCrumbImpl;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.summary.DCAControllerTitleComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.summary.DCAMetadataSummaryContainer;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.summary.DCASummaryContainer;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.summary.DCAControllerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The Controller overview widget
 * Created by mashiur on 4/1/16.
 */
public class DCAControllerOverviewComponent extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAControllerOverviewComponent.class);

    private Observable<DCAControllerService> dcaControllerService;
    private DCAMetadataServiceClient metadataServiceClient = new DCAMetadataServiceClientImpl();

    private DCAControllerTitleComponent dcaControllerTitleComponent = new DCAControllerTitleComponent();
    private DCASummaryContainer dcaSummaryContainer = new DCASummaryContainer();
    private DCAMetadataSummaryContainer dcaMetadataSummaryContainer = new DCAMetadataSummaryContainer();
    private Consumer<String> navigationHelper;

    public DCAControllerOverviewComponent(DCADashboardPresenter pDCADashboardPresenter, DCALayoutContainer layoutContainer) {
        setLayoutContainer(layoutContainer);
        setDashboardPresenter(pDCADashboardPresenter);

        try {
            this.dcaControllerService = getDashboardPresenter().getServiceStatus(getLoggedInUser());
            init();
        } catch (Exception e) {
            this.addComponent(new DCAError("Controller Summary Component Loading Error"));
            logger.error("Error : ", e);
        }
    }

    private void init() {
        this.setStyleName("controller-overview-container");
        Observable<List<DCAWidget>> controllerWidgetConfigObservable = getDashboardPresenter().getWidget(getLoggedInUser(),
            DCAUserPreference.getViewState(), DCAUserPreference.getNavMenuState(), "");

        controllerWidgetConfigObservable.subscribe(this::onNext, this::onError);
    }

    private void onNext(List<DCAWidget> controllerConfigWidgetList) {
        Map<String, DCAWidget> permissionMap = new HashMap<>();

        for (DCAWidget dcaWidget : controllerConfigWidgetList) {
            permissionMap.put(dcaWidget.getName(), dcaWidget);
        }

        DCABreadCrumb breadCrumb = new DCABreadCrumbImpl();
        breadCrumb.addAction("Controller", navigationHelper);

        this.addComponent(breadCrumb.getView());
        this.addComponent(dcaControllerTitleComponent);
        this.addComponent(dcaSummaryContainer);
        this.addComponent(dcaMetadataSummaryContainer);

        this.dcaControllerService.subscribe(dcaControllerTitleComponent::onNext, dcaControllerTitleComponent::onError);
        this.dcaControllerService.subscribe(dcaSummaryContainer::onNext, dcaSummaryContainer::onError);
        metadataServiceClient.getScrapingMetrics().subscribe(
            dcaMetadataSummaryContainer::onNext, dcaMetadataSummaryContainer::onError);

    }

    private void onError(Throwable throwable) {
        logger.error("Error : ", throwable);
        addComponentAsLast(new DCAError("Error happened in Controller overview page"), this);
    }

    public void setNavigationHelper(Consumer<String> navigationHelper) {
        this.navigationHelper = navigationHelper;
    }
}
