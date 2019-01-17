package com.sannsyn.dca.vaadin.component.custom.navigation;

import com.sannsyn.dca.model.config.DCAWidget;
import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.presenter.DCAAdminPresenter;
import com.sannsyn.dca.presenter.DCADashboardPresenter;
import com.sannsyn.dca.service.DCAConfigService;
import com.sannsyn.dca.vaadin.component.custom.DCAHelpContainerComponent;
import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.inspectaggregate.DCAInspectRecommendersAndAggregates;
import com.sannsyn.dca.vaadin.inspectassembly.DCAInspectAssemblyWidget;
import com.sannsyn.dca.vaadin.pipes.DCAPipeSearchAndEditComponent;
import com.sannsyn.dca.vaadin.servlet.DCAUserPreference;
import com.sannsyn.dca.vaadin.ui.DCAUI;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.DCAWidgetFactory;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.DCASetupAccountComponent;
import com.sannsyn.dca.vaadin.widgets.common.DCABreadCrumb;
import com.sannsyn.dca.vaadin.widgets.common.DCABreadCrumbImpl;
import com.sannsyn.dca.vaadin.widgets.operations.controller.DCAAggregatesWidget;
import com.sannsyn.dca.vaadin.widgets.operations.controller.DCAControllerOverviewComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.DCAPipeLineWidget;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.recommenders.DCARecommendersContainerComponent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.sannsyn.dca.vaadin.ui.DCAUiHelper.addComponentAsLast;
import static java.util.stream.Collectors.toList;

/**
 * Created by mashiur on 3/31/16.
 */
public class DCAWidgetGenerator {
    private static final Logger logger = LoggerFactory.getLogger(DCAWidgetGenerator.class);
    private static final Map<String, String> MENU_SECTION_MAP = new HashMap<String, String>() {{
        put("dashboard", DCAUI.DASHBOARD_VIEW);
        put("inspectItem", DCAUI.DASHBOARD_VIEW);
        put("controller", DCAUI.DASHBOARD_VIEW);
        put("controller-aggregate", DCAUI.DASHBOARD_VIEW);
        put("controller-pipeline", DCAUI.DASHBOARD_VIEW);
        put("controller-inspect", DCAUI.DASHBOARD_VIEW);
        put("controller-pipes", DCAUI.DASHBOARD_VIEW);
        put("controller-recommenders", DCAUI.DASHBOARD_VIEW);
        put("controller-inspect-assembly", DCAUI.DASHBOARD_VIEW);
        put("analytics", DCAUI.DASHBOARD_VIEW);
        put("Users", DCAUI.ADMIN_VIEW);
        put("dca-roles", DCAUI.ADMIN_VIEW);
        put("dca-setup", DCAUI.ADMIN_VIEW);
        put("Help", DCAUI.HELP_VIEW);
        put("ShopHelper", DCAUI.SHOPHELPER);
    }};

    private DCADashboardPresenter dcaDashboardPresenter;
    private DCAAdminPresenter adminPresenter;
    private DCAUser loggedInUser;
    private DCAConfigService configService = new DCAConfigService();

    public DCAWidgetGenerator(DCADashboardPresenter pDCADashboardPresenter,
                              DCAAdminPresenter adminPresenter, DCAUser loggedInUser) {
        this.dcaDashboardPresenter = pDCADashboardPresenter;
        this.adminPresenter = adminPresenter;
        this.loggedInUser = loggedInUser;
    }

    private void loadDashBoardWidgets(DCALayoutContainer layoutContainer) {
        loadDashboardWidgetsFromConfig(layoutContainer);
    }

    private void loadDashboardWidgetsFromConfig(DCALayoutContainer layoutContainer) {
        loadWidgetsFromConfig(layoutContainer, "operations", "dashboard", "", this::addDashboardBreadCrumb);
    }

    private void loadShopHelperWidget(DCALayoutContainer layoutContainer) {
        loadWidgetsFromConfig(layoutContainer, "shophelper", "ShopHelper", "", layout -> {
        });
    }

    private void loadWidgetsFromConfig(DCALayoutContainer layoutContainer, String section, String menu, String subMenu, Consumer<DCALayoutContainer> breadCrumbAction) {
        final DCAWidgetFactory dcaWidgetFactory = new DCAWidgetFactory(dcaDashboardPresenter, loggedInUser);
        Observable<List<DCAWidget>> widgetConfigs = configService.getWidget(loggedInUser, section, menu, subMenu);
        Observable<List<Component>> widgets = widgetConfigs.map(configList ->
            configList.stream().map(dcaWidgetFactory::createWidget).collect(toList()));
        widgets.subscribe(widgetList -> {
            logger.debug("Widget list loaded.");
            UI.getCurrent().access(() -> {
                layoutContainer.getWidgetContainer().removeAllComponents();
                breadCrumbAction.accept(layoutContainer);
                for (Component dcaWidget : widgetList) {
                    layoutContainer.getWidgetContainer().addComponent(dcaWidget);
                }
            });
        }, e -> {
            logger.error("Error occurred while creating widget from configuration : ", e);
            addComponentAsLast(new DCAError("Error occurred while creating widget from configuration"), layoutContainer.getWidgetContainer());
        });
    }

    private void addDashboardBreadCrumb(DCALayoutContainer layoutContainer) {
        DCABreadCrumbImpl breadCrumb = new DCABreadCrumbImpl();
        breadCrumb.addAction("Dashboard", s -> loadDashBoardWidgets(layoutContainer));
        layoutContainer.getWidgetContainer().addComponent(breadCrumb.getView());
    }

    private void updateNavigationState(String navigationId) {
        DCAUserPreference.updateUiState(DCAUserPreference.VIEW_STATE, MENU_SECTION_MAP.get(navigationId));
        DCAUserPreference.updateUiState(DCAUserPreference.NAV_MENU_STATE, navigationId);
    }

    public void updateWidgetComponents(DCALayoutContainer layoutContainer, String itemId) {
        if ("dashboard".equals(itemId) || "floating-dashboard".equals(itemId)) {
            updateNavigationState("dashboard");
            loadDashBoardWidgets(layoutContainer);
        }

        if ("inspectItem".equals(itemId) || "floating-inspectItem".equals(itemId)) {
            updateNavigationState("inspectItem");
            layoutContainer.getWidgetContainer().removeAllComponents();
        }

        if ("controller".equals(itemId) || "floating-controller".equals(itemId)) {
            updateNavigationState("controller");

            layoutContainer.getWidgetContainer().removeAllComponents();
            DCAControllerOverviewComponent dcaControllerOverviewComponent = new DCAControllerOverviewComponent(dcaDashboardPresenter, layoutContainer);
            dcaControllerOverviewComponent.setNavigationHelper(componentId -> navigateToControllerOverview(componentId, layoutContainer));
            layoutContainer.getWidgetContainer().addComponent(dcaControllerOverviewComponent);
        }

        if ("controller-aggregate".equals(itemId) || "floating-controller-aggregate".equals(itemId)) {
            setUpAggregatesWidget(layoutContainer);
        }

        if ("controller-pipeline".equals(itemId) || "floating-controller-pipeline".equals(itemId)) {
            setUpPipeLineWidget(layoutContainer);
        }

        if ("controller-inspect".equals(itemId) || "floating-controller-inspect".equals(itemId)) {
            setUpInspectWidget(layoutContainer);
        }

        if ("controller-pipes".equals(itemId) || "floating-controller-pipes".equals(itemId)) {
            setUpPipesWidgt(layoutContainer);
        }

        if ("controller-recommenders".equals(itemId) || "floating-controller-recommenders".equals(itemId)) {
            setUpRecommendersWidget(layoutContainer);
        }

        if ("controller-inspect-assembly".equals(itemId) || "floating-controller-inspect-assembly".equals(itemId)) {
            setUpInspectAssemblyWidget(layoutContainer);
        }

        if ("analytics".equals(itemId) || "floating-analytics".equals(itemId)) {
            updateNavigationState("analytics");
            loadAnalyticsWidgets(layoutContainer);
        }

        if ("customer-targeting".equals(itemId) || "floating-customer-targeting".equals(itemId)) {
            loadCustomerTargetingWidgets(layoutContainer);
        }

        if ("dca-users".equals(itemId) || "floating-dca-users".equals(itemId)) {
            navigateToUsers(layoutContainer);
        }

        if ("dca-users-dca-roles".equals(itemId) || "floating-dca-users-dca-roles".equals(itemId)) {
            updateNavigationState("dca-users-dca-roles");
            loadDcaRolesWidget(layoutContainer);
        }

        if ("dca-setup".equals(itemId) || "floating-dca-setup".equals(itemId)) {
            updateNavigationState("dca-setup");
            layoutContainer.getWidgetContainer().removeAllComponents();
            DCASetupAccountComponent dcaSetupAccountComponent = new DCASetupAccountComponent(layoutContainer,
                this.adminPresenter);
            layoutContainer.getWidgetContainer().addComponent(dcaSetupAccountComponent);
        }

        if ("Help".equals(itemId) || "floating-Help".equals(itemId)) {
            updateNavigationState("Help");
            layoutContainer.getWidgetContainer().removeAllComponents();

            DCAHelpContainerComponent helpContainerComponent = new DCAHelpContainerComponent(layoutContainer.getWidgetContainer());
            layoutContainer.getWidgetContainer().addComponent(helpContainerComponent);
        }

        if ("ShopHelper".equals(itemId) || "floating-ShopHelper".equals(itemId)) {
            updateNavigationState("ShopHelper");
            loadShopHelperWidget(layoutContainer);
        }
    }

    private void navigateToUsers(DCALayoutContainer layoutContainer) {
        updateNavigationState("dca-users");
        layoutContainer.getWidgetContainer().removeAllComponents();
        updateLeftMenuState(layoutContainer);
    }

    private void loadAnalyticsWidgets(DCALayoutContainer layoutContainer) {
        loadWidgetsFromConfig(layoutContainer, "operations", "analytics", "", this::addAnalyticsBreadcrumb);
    }

    private void loadCustomerTargetingWidgets(DCALayoutContainer layoutContainer) {
        loadWidgetsFromConfig(layoutContainer, "operations", "customer-targeting", "", this::addCustomerTargetingBreadcrumb);
    }

    private void loadDcaRolesWidget(DCALayoutContainer layoutContainer) {
        loadWidgetsFromConfig(layoutContainer, "admin", "dca-users", "dca-roles", this::addRolesBreadcrumb);
    }

    private void addRolesBreadcrumb(DCALayoutContainer layoutContainer) {
        DCABreadCrumb breadCrumb = new DCABreadCrumbImpl();
        breadCrumb.addAction("Users", s -> {
            navigateToUsers(layoutContainer);
        });

        breadCrumb.addAction("Roles", s -> { });
        Component view = breadCrumb.getView();
        layoutContainer.getWidgetContainer().addComponent(view);
    }

    private void addCustomerTargetingBreadcrumb(DCALayoutContainer layoutContainer) {
        Label dashboardHeader = new Label("<span style='color:#40b08e;'>Customer targeting</span>", ContentMode.HTML);
        dashboardHeader.setStyleName("component-header");
        dashboardHeader.setWidthUndefined();
        layoutContainer.getWidgetContainer().addComponent(dashboardHeader);
    }

    private void addAnalyticsBreadcrumb(DCALayoutContainer layoutContainer) {
        Label dashboardHeader = new Label("<span style='color:#40b08e;'>Analytics</span>", ContentMode.HTML);
        dashboardHeader.setStyleName("component-header");
        dashboardHeader.setWidthUndefined();
        layoutContainer.getWidgetContainer().addComponent(dashboardHeader);
    }

    private void setUpPipesWidgt(DCALayoutContainer layoutContainer) {
        updateNavigationState("controller");
        layoutContainer.getWidgetContainer().removeAllComponents();
        layoutContainer.getWidgetContainer().addComponent(createRecommenderComponent(layoutContainer));
    }

    private void setUpInspectWidget(DCALayoutContainer layoutContainer) {
        updateNavigationState("controller");
        layoutContainer.getWidgetContainer().removeAllComponents();
        Component inspector = createInspector(layoutContainer);
        layoutContainer.getWidgetContainer().addComponent(inspector);
    }

    private void setUpInspectAssemblyWidget(DCALayoutContainer layoutContainer) {
        updateNavigationState("controller");
        layoutContainer.getWidgetContainer().removeAllComponents();
        Component inspector = createInspectAssembly(layoutContainer);
        layoutContainer.getWidgetContainer().addComponent(inspector);
    }

    private void setUpAggregatesWidget(DCALayoutContainer layoutContainer) {
        updateNavigationState("controller");

        layoutContainer.getWidgetContainer().removeAllComponents();
        DCAAggregatesWidget dcaAggregatesWidget = new DCAAggregatesWidget(dcaDashboardPresenter, layoutContainer);
        dcaAggregatesWidget.setNavigationHelper(componentId -> navigateToControllerOverview(componentId, layoutContainer));
        dcaAggregatesWidget.setSelfNavigation(s -> setUpAggregatesWidget(layoutContainer));

        layoutContainer.getWidgetContainer().addComponent(dcaAggregatesWidget);
    }

    private void setUpPipeLineWidget(DCALayoutContainer layoutContainer) {
        updateNavigationState("controller");

        layoutContainer.getWidgetContainer().removeAllComponents();
        DCAPipeLineWidget dcaPipeLineWidget = new DCAPipeLineWidget(dcaDashboardPresenter, layoutContainer);
        dcaPipeLineWidget.setNavigationHelper(componentId -> navigateToControllerOverview(componentId, layoutContainer));
        dcaPipeLineWidget.setSelfNavigation(s -> setUpPipeLineWidget(layoutContainer));

        layoutContainer.getWidgetContainer().addComponent(dcaPipeLineWidget);
    }

    private void setUpRecommendersWidget(DCALayoutContainer layoutContainer) {
        updateNavigationState("controller");

        layoutContainer.getWidgetContainer().removeAllComponents();
        DCARecommendersContainerComponent recommendersContainerComponent = new DCARecommendersContainerComponent(
            dcaDashboardPresenter, layoutContainer);
        recommendersContainerComponent.setNavigationHelper(
            componentId -> navigateToControllerOverview(componentId, layoutContainer));
        recommendersContainerComponent.setNavigateToSelf(s -> setUpRecommendersWidget(layoutContainer));

        layoutContainer.getWidgetContainer().addComponent(recommendersContainerComponent);
    }

    private void updateLeftMenuState(DCALayoutContainer layoutContainer) {
        if (layoutContainer.getLeftPanelContainer() instanceof DCALeftPanelContainer) {
            DCALeftPanelContainer dcaLeftPanel = (DCALeftPanelContainer) layoutContainer.getLeftPanelContainer();
            dcaLeftPanel.getDcaLeftPanelItemObserver().notifyObservers("controller");
            dcaLeftPanel.getDcaLeftPanelSubMenuObserver().notifySubscribers("controller");
        }
    }

    private Component createInspector(DCALayoutContainer layoutContainer) {
        DCAInspectRecommendersAndAggregates recommendersAndAggregates =
            new DCAInspectRecommendersAndAggregates(UI.getCurrent(), loggedInUser);
        recommendersAndAggregates.setNavigationHelper(componentId -> navigateToControllerOverview(componentId, layoutContainer));
        return recommendersAndAggregates.createInspector();
    }

    private Component createInspectAssembly(DCALayoutContainer layoutContainer) {
        Consumer<String> navigationHelper = componentId -> navigateToControllerOverview(componentId, layoutContainer);
        return new DCAInspectAssemblyWidget(navigationHelper);
    }

    private Component createRecommenderComponent(DCALayoutContainer layoutContainer) {
        Consumer<String> navHelper = componentId -> navigateToControllerOverview(componentId, layoutContainer);
        return new DCAPipeSearchAndEditComponent(UI.getCurrent(), loggedInUser, navHelper);
    }

    private void navigateToControllerOverview(String clickedComponentId, DCALayoutContainer layoutContainer) {
        layoutContainer.getWidgetContainer().removeAllComponents();
        DCAControllerOverviewComponent dcaControllerOverviewComponent =
            new DCAControllerOverviewComponent(dcaDashboardPresenter, layoutContainer);
        layoutContainer.getWidgetContainer().addComponent(dcaControllerOverviewComponent);

        updateLeftMenuState(layoutContainer);
    }
}
