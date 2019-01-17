package com.sannsyn.dca.vaadin.widgets.operations.controller.component.pipelines.pipeline;

import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAClassWrapper;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCATaskObject;
import com.vaadin.ui.CssLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by mashiur on 5/26/16.
 */
public class DCAFaucetContainerComponent extends CssLayout {
    private DCALabel labelName = new DCALabel("Faucet: ", "label-name");
    private DCADropDownComponent faucetDropDownComponent;

    public DCAFaucetContainerComponent(Map<String, DCAClassWrapper> faucets, DCATaskObject taskObject,
                                       DCALayoutContainer layoutContainer) {
        this.setStyleName("faucet-container");

        List<String> faucetList = new ArrayList<>();

        for (Map.Entry<String, DCAClassWrapper> entry : faucets.entrySet()) {
            faucetList.add(entry.getKey());
        }

        this.faucetDropDownComponent = new DCADropDownComponent(faucetList, layoutContainer);
        this.faucetDropDownComponent.setValue(taskObject.getOut());

        this.addComponent(this.labelName);
        this.addComponent(this.faucetDropDownComponent);
    }

    public DCADropDownComponent getFaucetDropDownComponent() {
        return faucetDropDownComponent;
    }
}
