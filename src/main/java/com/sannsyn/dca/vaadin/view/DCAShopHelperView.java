package com.sannsyn.dca.vaadin.view;

import com.sannsyn.dca.vaadin.component.custom.navigation.DCAWidgetGenerator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Responsive;
import com.vaadin.ui.CssLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jobaer on may 15, 2017
 */
public class DCAShopHelperView extends DCAView {
    private static final Logger logger = LoggerFactory.getLogger(DCAShopHelperView.class);

    public DCAShopHelperView(String identifier) {
        super();
        setIdentifier(identifier);
    }

    @Override
    public void enter(final ViewChangeListener.ViewChangeEvent event) {
        logger.info(String.format("Entered the %s View", getIdentifier()));

        DCAWidgetGenerator widgetGenerator = new DCAWidgetGenerator(getDashboardPresenter(), getAdminPresenter(), getLoggedInUser());
        widgetGenerator.updateWidgetComponents(getLayoutContainer(), "ShopHelper");

        init();
    }

    @Override
    public void init() {
        this.removeAllComponents();
        this.setStyleName("dca-dashboard");
        this.addStyleName("shop-helper");
        this.setSizeFull();
        Responsive.makeResponsive(this);

        final CssLayout bodyContainer = new CssLayout();
        bodyContainer.setStyleName("dashboard-body-container module-wrapper");

        bodyContainer.addComponent(getLayoutContainer().getWidgetContainer());
        this.addComponent(bodyContainer);
    }
}
