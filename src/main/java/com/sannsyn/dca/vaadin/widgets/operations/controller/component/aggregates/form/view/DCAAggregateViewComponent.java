package com.sannsyn.dca.vaadin.widgets.operations.controller.component.aggregates.form.view;

import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.vaadin.ui.CssLayout;

/**
 * Created by mashiur on 4/29/16.
 */
public class DCAAggregateViewComponent extends DCAWidgetContainerComponent {
    public CssLayout createViewItem(String primaryStyleName, String name, String value) {
        CssLayout item = new CssLayout();
        item.setStyleName(primaryStyleName);

        DCALabel nameLabel = new DCALabel(name, "item-label");
        DCALabel valueLabel = new DCALabel(value, "item-value");

        item.addComponent(nameLabel);
        item.addComponent(valueLabel);

        return item;
    }

    @Override
    public void updateWidgetContainer(String clickedComponentId) {

    }
}
