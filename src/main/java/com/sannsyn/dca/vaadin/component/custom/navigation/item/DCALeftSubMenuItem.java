package com.sannsyn.dca.vaadin.component.custom.navigation.item;

import com.sannsyn.dca.vaadin.component.custom.navigation.DCAWidgetGenerator;
import com.sannsyn.dca.vaadin.view.DCALayout;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.vaadin.event.LayoutEvents;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Link;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mashiur on 2/29/16.
 */
public class DCALeftSubMenuItem extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCALeftSubMenuItem.class);

    private Link item = new Link();
    private DCAWidgetGenerator dcaWidgetGenerator;
    private DCALeftPanelSubMenuObserver dcaLeftPanelSubMenuObserver;

    public Link getItem() {
        return item;
    }

    public DCALeftSubMenuItem(String itemId, String labelName, DCAWidgetGenerator pDCAWidgetGenerator,
                              DCALayoutContainer layoutContainer, DCALeftPanelSubMenuObserver dcaLeftPanelSubMenuObserver,
                              DCALeftPanelItemObserver dcaLeftPanelItemObserver) {
        this.item.setId(itemId);
        setLayoutContainer(layoutContainer);
        this.item.setCaption(labelName);
        this.dcaWidgetGenerator = pDCAWidgetGenerator;
        this.dcaLeftPanelSubMenuObserver = dcaLeftPanelSubMenuObserver;

        init();

        addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
            @Override
            public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                String clickedItemId = StringUtils.stripToEmpty(event.getChildComponent().getId());
                if (itemId.equals(clickedItemId)) {
                    dcaLeftPanelSubMenuObserver.notifySubscribers(clickedItemId);
                    updateWidgetContainer(clickedItemId);
                }
                dcaLeftPanelItemObserver.resetSubscribers();
            }
        });
    }

    private void init() {
        this.item.setStyleName("menu-item-label");
        this.setStyleName("sub-menu-item-wrapper");
        this.addComponent(item);
    }

    @Override
    public void updateWidgetContainer(String clickedComponentId) {
        this.dcaWidgetGenerator.updateWidgetComponents(getLayoutContainer(), clickedComponentId);
    }
}
