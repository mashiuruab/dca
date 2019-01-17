package com.sannsyn.dca.vaadin.widgets;

import com.sannsyn.dca.model.config.DCAWidget;
import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.presenter.DCADashboardPresenter;
import com.sannsyn.dca.service.DCAPopularityService;
import com.sannsyn.dca.service.DCAPopularityServiceImpl;
import com.sannsyn.dca.vaadin.component.custom.DCAWrapper;
import com.sannsyn.dca.vaadin.customertargeting.DCACustomerTargetingWidget;
import com.sannsyn.dca.vaadin.widgets.analytics.DCAAnalyticsWidget;
import com.sannsyn.dca.vaadin.widgets.keyfigures.DCAKeyFiguresWidget;
import com.sannsyn.dca.vaadin.widgets.operations.dashboard.recommendation.DCANumberOfRecommendationChart;
import com.sannsyn.dca.vaadin.widgets.operations.dashboard.recommendation.DCAWidgetLiveUpdateComponent;
import com.sannsyn.dca.vaadin.widgets.operations.dashboard.salesbyrec.DCASalesByRecommendationWidget;
import com.sannsyn.dca.vaadin.widgets.popularity.DCAPopularityWidgetPresenter;
import com.sannsyn.dca.vaadin.widgets.popularity.DCAPopularityWidgetPresenterImpl;
import com.sannsyn.dca.vaadin.widgets.popularity.DCAPopularityWidgetView;
import com.sannsyn.dca.vaadin.widgets.popularity.DCAPopularityWidgetViewImpl;
import com.sannsyn.dca.vaadin.widgets.shophelper.DCAShopHelperWidget;
import com.sannsyn.dca.vaadin.widgets.users.roles.DCARolesWidget;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.UI;

import java.util.Collections;
import java.util.List;

import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;
import static com.vaadin.server.Sizeable.Unit.PIXELS;

/**
 * A simple factory for creating widgets
 * <p>
 * Created by jobaer on 12/2/16.
 */
public class DCAWidgetFactory {
    private final DCADashboardPresenter dashboardPresenter;
    private final DCAUser loggedInUser;

    public DCAWidgetFactory(DCADashboardPresenter dcaDashboardPresenter, DCAUser loggedInUser) {
        this.dashboardPresenter = dcaDashboardPresenter;
        this.loggedInUser = loggedInUser;
    }

    private Component createNumberOfRecommendationWidget(DCAWidget widgetConfig) {
        DCAWidgetLiveUpdateComponent numOfRecommendationContainer = new DCANumberOfRecommendationChart(this.dashboardPresenter);
        return wrap(numOfRecommendationContainer, widgetConfig);
    }

    private Component createSalesByRecWidget(DCAWidget widgetConfig) {
        DCASalesByRecommendationWidget salesByRecommendationWidget = new DCASalesByRecommendationWidget(UI.getCurrent());
        return wrap(salesByRecommendationWidget, widgetConfig);
    }

    private Component createPopularityWidget(DCAWidget widgetConfig) {
        Component popularityViewComponent = createPopularityComponent();
        return wrap(popularityViewComponent, widgetConfig);
    }

    private DCAWrapper wrap(Component popularityViewComponent, DCAWidget widgetConfig) {
        Integer widthPercentage = widgetConfig.getWidthPercentage();
        String styleName = widthPercentage != null && widthPercentage < 100 ? "dca-column" : "dca-row";
        return new DCAWrapper(Collections.singletonList(popularityViewComponent), styleName);
    }

    private Component createPopularityComponent() {
        DCAPopularityWidgetView popularityView = new DCAPopularityWidgetViewImpl(UI.getCurrent());
        popularityView.init();
        DCAPopularityService service = new DCAPopularityServiceImpl(loggedInUser);
        DCAPopularityWidgetPresenter presenter = new DCAPopularityWidgetPresenterImpl(service, popularityView, loggedInUser);
        popularityView.setHandler(presenter);
        return popularityView.getComponent();
    }

    private Component createKeyFiguresWidget(DCAWidget widgetConfig) {
        return new DCAKeyFiguresWidget(widgetConfig, UI.getCurrent());
    }

    private Component createShopHelperWidget(DCAWidget widgetConfig) {
        return new DCAShopHelperWidget(widgetConfig, UI.getCurrent());
    }

    private CssLayout createWidgetSet(DCAWidget widgetConfig) {
        CssLayout root = new CssLayout();
        root.setStyleName("widget-set-wrapper");
        List<DCAWidget> children = widgetConfig.getChildren();
        for (int i = 0; i < children.size(); i++) {
            Component widget = createWidget(children.get(i));
            root.addComponent(widget);
            if (i < children.size() - 1) {
                root.addComponent(createSpacer());
            }
        }

        return wrap(root, widgetConfig);
    }

    private CssLayout createSpacer() {
        CssLayout spacer = new CssLayout();
        spacer.setStyleName("spacer");
        spacer.setHeight(360, PIXELS);
        spacer.setWidth(2, PERCENTAGE);
        return spacer;
    }

    public Component createWidget(DCAWidget widgetConfig) {
        String widgetName = widgetConfig.getName();
        switch (widgetName) {
            case "mostPopularItems":
                return createPopularityWidget(widgetConfig);
            case "keyFiguresWidget":
                return createKeyFiguresWidget(widgetConfig);
            case "widgetSet":
                return createWidgetSet(widgetConfig);
            case "numberOfRecommendations":
                return createNumberOfRecommendationWidget(widgetConfig);
            case "salesByRecommendations":
                return createSalesByRecWidget(widgetConfig);
            case "customer-targeting":
                return createCustomerTargetingWidget(widgetConfig);
            case "analytics":
                return createAnalyticsWidget(widgetConfig);
            case "ShopHelper":
                return createShopHelperWidget(widgetConfig);
            case "dca-roles":
                return createDcaRolesWidget();
            default:
                // just return an empty component
                return new CssLayout();
        }
    }

    private Component createDcaRolesWidget() {
        return new DCARolesWidget(UI.getCurrent());
    }

    private Component createCustomerTargetingWidget(DCAWidget widgetConfig) {
        return new DCACustomerTargetingWidget(UI.getCurrent(), this.loggedInUser, widgetConfig);
    }

    private Component createAnalyticsWidget(DCAWidget widgetConfig) {
        return new DCAAnalyticsWidget(UI.getCurrent(), widgetConfig);
    }

}
