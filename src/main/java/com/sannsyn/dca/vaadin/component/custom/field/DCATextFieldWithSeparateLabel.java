package com.sannsyn.dca.vaadin.component.custom.field;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;

import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;

/**
 * A custom input field with a label at the left.
 *
 * Created by jobaer on 1/16/17.
 */
public class DCATextFieldWithSeparateLabel extends CustomComponent {
    private final String label;
    private DCASuggestionTextField inputId = new DCASuggestionTextField();

    public String getValue() {
        return inputId.getValue();
    }

    public DCATextFieldWithSeparateLabel(String label) {
        this.label = label;
        Component rootComponent = createRootComponent();
        setCompositionRoot(rootComponent);
    }

    private Component createRootComponent() {
        CssLayout layout = new CssLayout();

        layout.setStyleName("dca-textfield-with-label");
        layout.setWidth(100, PERCENTAGE);

        DCALabel inputLabel = new DCALabel(label, "dca-textfield-input-label");

        inputId.setSizeUndefined();
        inputId.addStyleName("dca-textfield-with-label-input");
        inputId.setInputPrompt("");
        layout.addComponent(inputId);
        layout.addComponent(inputLabel);

        return layout;
    }
}
