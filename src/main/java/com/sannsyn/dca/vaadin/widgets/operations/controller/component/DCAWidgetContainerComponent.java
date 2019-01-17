package com.sannsyn.dca.vaadin.widgets.operations.controller.component;

import com.sannsyn.dca.presenter.DCAAdminPresenter;
import com.sannsyn.dca.presenter.DCADashboardPresenter;
import com.sannsyn.dca.vaadin.ui.DCAUiHelper;
import com.sannsyn.dca.vaadin.view.DCALayout;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.UI;

import java.util.List;

/**
 * Created by mashiur on 4/8/16.
 */
public class DCAWidgetContainerComponent extends DCALayout {
    private DCALayoutContainer layoutContainer;
    private DCADashboardPresenter dcaDashboardPresenter;
    private DCAAdminPresenter adminPresenter;
    private CssLayout currentComponent;


    public DCALayoutContainer getLayoutContainer() {
        return layoutContainer;
    }

    public void setLayoutContainer(DCALayoutContainer layoutContainer) {
        this.layoutContainer = layoutContainer;
        if (layoutContainer.getWidgetContainer() == null) {
            return;
        }

        layoutContainer.getWidgetContainer().addLayoutClickListener(event -> {
            for (int counter = 0; counter < layoutContainer.getWidgetContainer().getComponentCount(); counter++) {
                if (layoutContainer.getWidgetContainer().getComponent(counter) instanceof DCAPopupMessageComponent) {
                    removeComponent(layoutContainer.getWidgetContainer().getComponent(counter),
                            layoutContainer.getWidgetContainer());
                }
            }
        });

    }

    public DCADashboardPresenter getDashboardPresenter() {
        return dcaDashboardPresenter;
    }

    public void setDashboardPresenter(DCADashboardPresenter dcaDashboardPresenter) {
        this.dcaDashboardPresenter = dcaDashboardPresenter;
    }

    public DCAAdminPresenter getAdminPresenter() {
        return adminPresenter;
    }

    public void setAdminPresenter(DCAAdminPresenter adminPresenter) {
        this.adminPresenter = adminPresenter;
    }

    public void updateWidgetContainer(String clickedComponentId){
//        by default empty
    };

    public void addComponentAsLast(Component pComponent, CssLayout layout) {
        DCAUiHelper.addComponentAsLast(pComponent, layout);
    }

    public void addComponentAsFirst(Component component, CssLayout layout) {
        UI.getCurrent().access(() -> {
           layout.addComponentAsFirst(component);
        });
    }

    public void addComponentAsLast(List<Component> childComponentList, CssLayout layout) {
        UI.getCurrent().access(() -> {
            childComponentList.forEach(layout::addComponent);
        });
    }

    public void removeComponent(Component component, CssLayout layout) {
        UI.getCurrent().access(() -> {
            layout.removeComponent(component);
        });
    }

    public CssLayout getCurrentComponent() {
        return currentComponent;
    }

    public void setCurrentComponent(CssLayout currentComponent) {
        this.currentComponent = currentComponent;

        this.currentComponent.addLayoutClickListener(event -> {
            for (int counter = 0; counter < currentComponent.getComponentCount(); counter++) {
                if (currentComponent.getComponent(counter) instanceof DCAPopupMessageComponent) {
                    removeComponent(currentComponent.getComponent(counter), currentComponent);
                }
            }
        });
    }

    protected void showErrorNotification(Throwable throwable) {
        DCAPopupErrorComponent errorComponent = new DCAPopupErrorComponent("ERROR:", throwable.getMessage(),
                getCurrentComponent());
        addComponentAsLast(errorComponent, getCurrentComponent());
    }

    protected void showSuccessNotification(String message) {
        DCAPopupMessageComponent popupMessageComponent = new DCAPopupMessageComponent("SUCCESS:", message,
                getCurrentComponent());
        addComponentAsLast(popupMessageComponent, getCurrentComponent());
    }

    protected void showWarningNotification(String message) {
        DCAPopupWarningComponent warningMessageComponent = new DCAPopupWarningComponent("WARNING:", message,
                getCurrentComponent());
        addComponentAsLast(warningMessageComponent, getCurrentComponent());
    }
}
