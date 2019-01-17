package com.sannsyn.dca.vaadin.ui;

import com.sannsyn.dca.model.config.DCAConfigEntity;
import com.sannsyn.dca.model.config.DCASection;
import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.presenter.DCAAdminPresenter;
import com.sannsyn.dca.presenter.DCADashboardPresenter;
import com.sannsyn.dca.service.DCAUserLoginService;
import com.sannsyn.dca.vaadin.event.DCADashboardEventBus;
import com.sannsyn.dca.vaadin.event.DCAPopupNotificationEventBus;
import com.sannsyn.dca.vaadin.login.DCALoginPresenter;
import com.sannsyn.dca.vaadin.login.DCALoginView;
import com.sannsyn.dca.vaadin.login.DCALoginViewImpl;
import com.sannsyn.dca.vaadin.servlet.DCAUserPreference;
import com.sannsyn.dca.vaadin.view.*;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.Page;
import com.vaadin.server.Responsive;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.ui.UI;
import elemental.json.JsonArray;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by mashiur on 2/23/16.
 */
@Push
@Theme("dcatheme")
@Widgetset("com.sannsyn.dca.DCAAppWidgetset")
@com.vaadin.annotations.JavaScript({"vaadin://js/jquery.min.js", "vaadin://js/sannsyn.js", "vaadin://js/hammer.js"})
public class DCAUI extends UI {
    private static final Logger logger = LoggerFactory.getLogger(DCAUI.class.getName());

    public static final String START_VIEW = "";
    public static final String DASHBOARD_VIEW = "operations";
    public static final String ADMIN_VIEW = "admin";
    public static final String HELP_VIEW = "help";
    public static final String SHOPHELPER = "shophelper";

    private Navigator navigator;

    private DCADashboardPresenter dashboardPresenter;
    private DCAAdminPresenter adminPresenter;
    private DCAOperationsView dashboardView;

    private DCAAdminView dcaAdminView;

    private final DCADashboardEventBus dashboardEventbus = new DCADashboardEventBus();
    private final DCAPopupNotificationEventBus popupNotificationEventBus = new DCAPopupNotificationEventBus();

    @Override
    public void detach() {
        super.detach();
    }

    private void handleBrowserCloseEvent() {
        JavaScript.getCurrent().addFunction("closeDCAService", new JavaScriptFunction() {
            @Override
            public void call(final JsonArray arguments) {
                detach();
            }
        });

        Page.getCurrent().getJavaScript().execute("window.onbeforeunload = function (e) { var e = e || window.event; closeDCAService(); return; };");
    }

    @Override
    protected void init(final VaadinRequest request) {
        VaadinSession.getCurrent().setErrorHandler(new DCACustomErrorHandler());
        handleBrowserCloseEvent();

        if (getPage().getWebBrowser().getLocale() != null) {
            setLocale(getPage().getWebBrowser().getLocale());
        }

        DCALoginView loginView = new DCALoginViewImpl();
        loginView.init(this::getDefaultViewName);
        DCALoginPresenter loginPresenter = new DCALoginPresenter(loginView, new DCAUserLoginService());
        loginView.setHandler(loginPresenter);

        Responsive.makeResponsive(this);

        dashboardView = new DCAOperationsView(DASHBOARD_VIEW);
        dashboardPresenter = new DCADashboardPresenter();
        adminPresenter = new DCAAdminPresenter();

        dashboardView.setDashboardPresenter(dashboardPresenter);
        dashboardView.setAdminPresenter(adminPresenter);

        dcaAdminView = new DCAAdminView(ADMIN_VIEW);

        dcaAdminView.setDashboardPresenter(dashboardPresenter);
        dcaAdminView.setAdminPresenter(adminPresenter);

        DCAHelpView dcaHelpView = new DCAHelpView(HELP_VIEW);
        dcaHelpView.setDashboardPresenter(dashboardPresenter);
        dcaHelpView.setAdminPresenter(adminPresenter);

        DCAShopHelperView shopView = new DCAShopHelperView(SHOPHELPER);
        shopView.setDashboardPresenter(dashboardPresenter);
        shopView.setAdminPresenter(adminPresenter);

        navigator = new Navigator(this, this);
        navigator.addView(START_VIEW, loginView);
        navigator.addView(DASHBOARD_VIEW, dashboardView);
        navigator.addView(ADMIN_VIEW, dcaAdminView);
        navigator.addView(HELP_VIEW, dcaHelpView);
        navigator.addView(SHOPHELPER, shopView);

        DCAViewChangeListener dcaViewChangeListener = new DCAViewChangeListener();
        navigator.addViewChangeListener(dcaViewChangeListener);

        DCAUser loggedInUser = DCAUserPreference.getLoggedInUser();

        if (loggedInUser == null) {
            navigator.navigateTo(START_VIEW);
        } else {
            String lastViewState = (String) DCAUserPreference.getPreference().get(DCAUserPreference.VIEW_STATE);
            String viewName = getDefaultViewName();
            lastViewState = (lastViewState == null || StringUtils.isEmpty(lastViewState)) ? viewName : lastViewState;
            navigator.navigateTo(lastViewState);
        }
    }

    private String getDefaultViewName() {
        DCAUser loggedInUser = DCAUserPreference.getLoggedInUser();
        // Get the first section name as default view
        // If nothing present then return DASHBOARD_VIEW
        DCAConfigEntity userConfiguration = adminPresenter.getUserConfiguration(loggedInUser).toBlocking().first();

        List<DCASection> sections = userConfiguration.getPADCAConfiguration().getRoot().getSections();
        if (sections != null && sections.size() > 0) {
            DCASection dcaSection = sections.get(0);
            return dcaSection.getName();
        } else {
            return DASHBOARD_VIEW;
        }
    }

    public static DCADashboardEventBus getDashboardEventbus() {
        return ((DCAUI) getCurrent()).dashboardEventbus;
    }

    public static DCAPopupNotificationEventBus getPopupNotificationEventBus() {
        return ((DCAUI) getCurrent()).popupNotificationEventBus;
    }

}
