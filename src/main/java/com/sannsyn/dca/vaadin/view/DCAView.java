package com.sannsyn.dca.vaadin.view;

import com.sannsyn.dca.model.config.DCASelectedService;
import com.sannsyn.dca.presenter.DCAAdminPresenter;
import com.sannsyn.dca.presenter.DCADashboardPresenter;
import com.sannsyn.dca.vaadin.component.custom.DCATopPanel;
import com.sannsyn.dca.vaadin.component.custom.navigation.DCALeftPanelContainer;
import com.sannsyn.dca.vaadin.component.custom.navigation.DCAWidgetGenerator;
import com.sannsyn.dca.vaadin.servlet.DCAUserPreference;
import com.sannsyn.dca.vaadin.servlet.DCAUtils;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.CssLayout;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mashiur on 3/31/16.
 */
public class DCAView extends DCAWidgetContainerComponent implements View {
    private static final Logger logger = LoggerFactory.getLogger(DCAView.class);

    private static final Map<String, String> DEFAULT_VIEW_TO_MENU_MAP = new HashMap<String, String>(){{
        put("operations", "dashboard");
        put("admin", "dca-setup");
        put("help", "Help");
        put("shophelper", "ShopHelper");
    }};

    private CssLayout bodyContainer = new CssLayout();
    private CssLayout widgetContainer = new CssLayout();
    private DCALayoutContainer layoutContainer = new DCALayoutContainer();

    private String identifier;

    private DCATopPanel topPanel;
    private DCALeftPanelContainer dcaLeftPanelContainer;

    private DCADashboardPresenter dashboardPresenter;
    private DCAAdminPresenter adminPresenter;


    DCAView() {
        layoutContainer.setWidgetContainer(widgetContainer);
        layoutContainer.setBodyContainer(bodyContainer);
    }

    public DCALayoutContainer getLayoutContainer() {
        return layoutContainer;
    }

    public DCADashboardPresenter getDashboardPresenter() {
        return dashboardPresenter;
    }

    public void setDashboardPresenter(DCADashboardPresenter dashboardPresenter) {
        this.dashboardPresenter = dashboardPresenter;
    }

    public DCAAdminPresenter getAdminPresenter() {
        return adminPresenter;
    }

    public void setAdminPresenter(DCAAdminPresenter adminPresenter) {
        this.adminPresenter = adminPresenter;
    }

    public void init() {
        this.removeAllComponents();
        this.setStyleName("dca-dashboard main-wrapper");

        bodyContainer.setStyleName("dashboard-body-container module-wrapper");

        getLayoutContainer().getWidgetContainer().setStyleName("dca-dashboard-widget-container module-container-wrapper");

        this.dcaLeftPanelContainer = new DCALeftPanelContainer(getDashboardPresenter(), getIdentifier(),
                getLayoutContainer(), getAdminPresenter());

        bodyContainer.addComponent(dcaLeftPanelContainer);
        bodyContainer.addComponent(getLayoutContainer().getWidgetContainer());

        this.topPanel = new DCATopPanel(getDashboardPresenter(), getAdminPresenter(), getLayoutContainer(), getIdentifier());

        this.addComponent(this.topPanel);
        this.addComponent(bodyContainer);
    }

    @Override
    public void enter(final ViewChangeListener.ViewChangeEvent event) {
        logger.info(String.format("Entered the %s View", getIdentifier()));
        init();
        DCAWidgetGenerator widgetGenerator = new DCAWidgetGenerator(getDashboardPresenter(), getAdminPresenter() , getLoggedInUser());
        widgetGenerator.updateWidgetComponents(getLayoutContainer(), getMenuToNavigate());
        this.dcaLeftPanelContainer.getDcaLeftPanelItemObserver().notifyObservers(getMenuToNavigate());
        this.dcaLeftPanelContainer.getDcaLeftPanelSubMenuObserver().notifySubscribers(getMenuToNavigate());
    }

    public String getIdentifier() {
        return identifier;
    }

    void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    private String getMenuToNavigate() {
        String navToMenu = (String) DCAUserPreference.getPreference().get(DCAUserPreference.NAV_MENU_STATE);
        navToMenu = (navToMenu == null || StringUtils.isEmpty(navToMenu)) ? DEFAULT_VIEW_TO_MENU_MAP.get(getIdentifier()) : navToMenu;

        return navToMenu;
    }
}
