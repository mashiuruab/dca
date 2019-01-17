package com.sannsyn.dca.vaadin.inspectaggregate;

import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.vaadin.widgets.common.DCABreadCrumb;
import com.sannsyn.dca.vaadin.widgets.common.DCABreadCrumbImpl;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.UI;

import java.util.function.Consumer;

/**
 * This class merges the inspect aggregate and inspect recommender ui using a split panel.
 */
public class DCAInspectRecommendersAndAggregates {
    private final UI ui;
    private DCAUser loggedInUser;
    private Consumer<String> navigationHelper = s -> {
    };

    public DCAInspectRecommendersAndAggregates(UI ui, DCAUser loggedInUser) {
        this.ui = ui;
        this.loggedInUser = loggedInUser;
    }

    public Component createInspector() {
        CssLayout wrapper = new CssLayout();
        wrapper.setStyleName("inspect-root-layout");
        wrapper.setWidth(100, Sizeable.Unit.PERCENTAGE);
        wrapper.setHeight(100, Sizeable.Unit.PERCENTAGE);

        Component breadcrumb = setupBreadcrumb();
        wrapper.addComponent(breadcrumb);

        Component cssLayout = getFirstComponent();
        Component secondUI = getSecondComponent();

        HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
        splitPanel.setMaxSplitPosition(982, Sizeable.Unit.PIXELS);
        splitPanel.setFirstComponent(cssLayout);
        splitPanel.setSecondComponent(secondUI);
        wrapper.addComponent(splitPanel);
        return wrapper;
    }

    private Component setupBreadcrumb() {
        DCABreadCrumb breadCrumb = new DCABreadCrumbImpl();
        breadCrumb.addAction("Controller", navigationHelper);
        breadCrumb.addAction("Inspect", s -> {
        });
        return breadCrumb.getView();
    }

    private Component getFirstComponent() {
        DCAInspectAggregateComponent inspectAggregateComponent = new DCAInspectAggregateComponent(ui, this.loggedInUser);
        return inspectAggregateComponent.createUI();
    }

    private Component getSecondComponent() {
        DCAInspectRecommenderComponent recommenderComponent = new DCAInspectRecommenderComponent(ui, loggedInUser);
        return recommenderComponent.createUI();
    }

    public void setNavigationHelper(Consumer<String> navigationHelper) {
        this.navigationHelper = navigationHelper;
    }
}
