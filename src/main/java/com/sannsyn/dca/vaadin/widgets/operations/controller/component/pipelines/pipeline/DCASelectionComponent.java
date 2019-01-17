package com.sannsyn.dca.vaadin.widgets.operations.controller.component.pipelines.pipeline;

import com.sannsyn.dca.vaadin.component.custom.DCAWrapper;
import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.CssLayout;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by mashiur on 5/19/16.
 */
public class DCASelectionComponent extends CssLayout {

    private DCAWrapper removeIconComponent;
    private DCADropDownComponent selectedComponent;

    public DCASelectionComponent(String value, Map<String, List<String>> pipesDropDownItems,
                                 DCALayoutContainer layoutContainer) {
        this.setStyleName("selection-wrapper");

        DCALabel iconComponent = new DCALabel();
        iconComponent.setIcon(FontAwesome.REMOVE);
        removeIconComponent = new DCAWrapper(Arrays.asList(iconComponent), "remove-icon");


        selectedComponent = new DCADropDownComponent(layoutContainer, pipesDropDownItems);
        setValue(value);

        selectedComponent.addComponentAsFirst(removeIconComponent);
        this.addComponent(selectedComponent);
    }

    public DCAWrapper getRemoveIconComponent() {
        return removeIconComponent;
    }

    public void setValue(String value) {
        selectedComponent.setValue(value);
    }
    public String getValue() {
        return selectedComponent.getValue();
    }

    public DCADropDownComponent getSelectedComponent() {
        return selectedComponent;
    }
}
