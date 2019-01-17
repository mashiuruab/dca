package com.sannsyn.dca.vaadin.widgets.operations.controller;

import com.sannsyn.dca.model.config.DCAWidget;
import com.sannsyn.dca.presenter.DCADashboardPresenter;
import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.servlet.DCAUserPreference;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.common.DCABreadCrumb;
import com.sannsyn.dca.vaadin.widgets.common.DCABreadCrumbImpl;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.pipelines.DCAPipeLineContainerComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAServiceConfigWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The Recommenders widget.
 *
 * Created by jobaer on 10/5/16.
 */
public class DCAPipeLineWidget extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAPipeLineWidget.class);

    public static final String PIPELINE_CONFIG_KEY = "pipeline";
    private Observable<DCAServiceConfigWrapper> dcaControllerConfigWrapper;
    private DCABreadCrumb breadCrumb = new DCABreadCrumbImpl();

    private Consumer<String> navigationHelper;
    private Consumer<String> navigateToSelf;

    public DCAPipeLineWidget(DCADashboardPresenter pDCADashboardPresenter, DCALayoutContainer layoutContainer) {
        setLayoutContainer(layoutContainer);
        setDashboardPresenter(pDCADashboardPresenter);
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
        Observable<List<DCAWidget>> pipeLineConfigWidgetObservable = getDashboardPresenter().getWidget(getLoggedInUser(),
            DCAUserPreference.getViewState(), DCAUserPreference.getNavMenuState(), PIPELINE_CONFIG_KEY);

        pipeLineConfigWidgetObservable.subscribe(this::onNext, this::onError);
    }

    private void onNext(List<DCAWidget> pipeLineConfigWidgetList) {
        Map<String, DCAWidget> permissionMap = new HashMap<>();

        for(DCAWidget dcaWidget : pipeLineConfigWidgetList) {
            permissionMap.put(dcaWidget.getName(), dcaWidget);
        }

        prepareBreadCrumb();

        DCAPipeLineContainerComponent dcaPipeLineContainerComponent = new
                DCAPipeLineContainerComponent(permissionMap, getDashboardPresenter(), getLayoutContainer());
        this.addComponent(dcaPipeLineContainerComponent);
        this.dcaControllerConfigWrapper.subscribe(dcaPipeLineContainerComponent::onNext, dcaPipeLineContainerComponent::onError);
    }

    private void prepareBreadCrumb() {
        breadCrumb.addAction("Controller", navigationHelper);
        breadCrumb.addAction("Pipelines", navigateToSelf);
        this.addComponent(breadCrumb.getView());
        getLayoutContainer().setBreadCrumb(breadCrumb);
    }

    private void onError(Throwable throwable) {
        logger.error("Error : ", throwable);
        addComponentAsLast(new DCAError("Error happened in Controller/Pipelines page"), this);
    }

    public void setNavigationHelper(Consumer<String> navigationHelper) {
        this.navigationHelper = navigationHelper;
    }

    public void setSelfNavigation(Consumer<String> navigationHelper) {
        this.navigateToSelf = navigationHelper;
    }

}
