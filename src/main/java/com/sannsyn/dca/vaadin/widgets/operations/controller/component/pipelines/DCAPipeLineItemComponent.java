package com.sannsyn.dca.vaadin.widgets.operations.controller.component.pipelines;

import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCATaskObject;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;

import java.util.List;

/**
 * Created by mashiur on 4/4/16.
 */
public class DCAPipeLineItemComponent extends CssLayout{
    public DCAPipeLineItemComponent(List<String> values) {
        this.setStyleName("recommender-item-header");
        for (String value : values) {
            this.addComponent(createLabel("recommender-item", value));
        }
    }

    public DCAPipeLineItemComponent(String name, DCATaskObject pDCATaskObject) {
        this.setStyleName("item-row");
        this.setId(name);

        Label nameComponent = createLabel("recommender-item", name);
        Label output = createLabel("recommender-item", pDCATaskObject.getOut());
        Label description = createLabel("recommender-item", pDCATaskObject.getDescription());

        this.addComponent(nameComponent);
        this.addComponent(output);
        this.addComponent(description);
    }

    private Label createLabel(String styleName, String value) {
        Label label = new Label(value, ContentMode.HTML);
        label.setStyleName(styleName);
        label.setWidthUndefined();
        return label;
    }
}
