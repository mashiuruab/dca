package com.sannsyn.dca.vaadin.widgets.admin.dcasetup.component;

import com.sannsyn.dca.vaadin.component.custom.DCAWrapper;
import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model.DCAAccount;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.CssLayout;

import java.util.Arrays;
import java.util.List;

/**
 * Created by mashiur on 5/6/16.
 */
public class DCAAccountItemComponent extends DCAWidgetContainerComponent {

    public DCAAccountItemComponent(DCAAccount account, boolean isTarget, DCALayoutContainer layoutContainer) {
        setLayoutContainer(layoutContainer);
        this.setStyleName("item");
        this.setId(account.getUuid());

        this.addComponent(new DCALabel(account.getName(), "col-value"));
        this.addComponent(new DCALabel(account.getDescription(), "col-value"));
        DCALabel target = new DCALabel("", "");
        if (isTarget) {
            target.setIcon(FontAwesome.CHECK);
        }
        this.addComponent(new DCAWrapper(Arrays.asList(target), "col-value target"));
    }

    public DCAAccountItemComponent(List<String> headerNames) {
        this.setStyleName("header-item");
        this.setId("account-header-id");

        int counter = 0;

        for(String header : headerNames) {
            String styleName = "col-value";
            counter++;
            if (counter == headerNames.size()) {
                styleName = String.format("%s %s", styleName, "target-header");
            }
            this.addComponent(new DCALabel(header, styleName));
        }
    }

    @Override
    public void updateWidgetContainer(String clickedComponentId) {

    }
}
