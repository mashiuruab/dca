package com.sannsyn.dca.vaadin.component.custom.container.collapsible;

import com.sannsyn.dca.vaadin.component.custom.icon.DCAIcon;
import com.sannsyn.dca.vaadin.ui.DCAUiHelper;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;

import static com.sannsyn.dca.vaadin.component.custom.navigation.item.DCALeftPanelItem.DOWN_ARROW_ICON;
import static com.sannsyn.dca.vaadin.component.custom.navigation.item.DCALeftPanelItem.RIGHT_ARROW_ICON;
import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;

/**
 * An implementation of collapsible layout.
 * <p>
 * Created by jobaer on 3/7/17.
 */
public class DCACollapsibleLayoutImpl extends CustomComponent implements DCACollapsibleLayout {
    private CssLayout expandedComponent;
    private CssLayout collapsedComponent;
    private CssLayout collapsedWrapper = new CssLayout();
    private boolean isCollapsed = true;
    private CssLayout iconLayout;
    private final DCAIcon rightIcon = new DCAIcon(RIGHT_ARROW_ICON);
    private final DCAIcon downIcon = new DCAIcon(DOWN_ARROW_ICON);

    DCACollapsibleLayoutImpl() {
        CssLayout rootLayout = new CssLayout();
        rootLayout.setWidth(100, PERCENTAGE);

        initCollapsedLayout(rootLayout);
        initExpandedLayout();
        initListener(rootLayout);

        setCompositionRoot(rootLayout);
    }

    private void initCollapsedLayout(CssLayout rootLayout) {
        collapsedWrapper.addStyleName("collapsed-component-wrapper");
        collapsedWrapper.setWidth(100, PERCENTAGE);

        collapsedComponent = new CssLayout();
        collapsedComponent.setWidth(90, PERCENTAGE);
        collapsedWrapper.addComponent(collapsedComponent);

        iconLayout = DCAUiHelper.wrapWithCssLayout(rightIcon, "", 10);
        collapsedWrapper.addComponent(iconLayout);

        rootLayout.addComponent(collapsedWrapper);
    }

    private void initExpandedLayout() {
        expandedComponent = new CssLayout();
        expandedComponent.setWidth(100, PERCENTAGE);
    }

    private void initListener(CssLayout rootLayout) {
        collapsedWrapper.addLayoutClickListener(event -> {
            if (isCollapsed) {
                expand(rootLayout);
            } else {
                collapse(rootLayout);
            }
        });
    }

    private void collapse(CssLayout rootLayout) {
        isCollapsed = true;
        updateIcon(rightIcon);
        rootLayout.removeComponent(expandedComponent);
    }

    private void expand(CssLayout rootLayout) {
        isCollapsed = false;
        updateIcon(downIcon);
        rootLayout.addComponent(expandedComponent);
    }

    private void updateIcon(DCAIcon downIcon) {
        iconLayout.removeAllComponents();
        iconLayout.addComponent(downIcon);
    }

    @Override
    public void setExpansionComponent(Component component) {
        expandedComponent.addComponent(component);
    }

    @Override
    public void setCollapseComponent(Component component) {
        collapsedComponent.addComponent(component);
    }
}
