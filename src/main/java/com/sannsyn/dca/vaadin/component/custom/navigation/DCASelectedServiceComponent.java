package com.sannsyn.dca.vaadin.component.custom.navigation;

import com.sannsyn.dca.model.config.DCASelectedService;
import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.vaadin.ui.CssLayout;

import java.util.Arrays;

/**
 * Created by mashiur on 8/4/16.
 */
public class DCASelectedServiceComponent extends CssLayout {
    public DCASelectedServiceComponent(DCASelectedService selectedService) {
        this.setStyleName("service-info-holder");


        String account = String.format("ACCOUNT: %s", selectedService.getAccount().getName());
        DCALabel accountItem = new DCALabel(account, "item");

        String service = String.format("SERVICE: %s", selectedService.getName());
        DCALabel serviceItem = new DCALabel(service, "item");

        this.addComponent(accountItem);
        this.addComponent(serviceItem);
    }
}
