package com.sannsyn.dca.vaadin.component.custom.field;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TextField;

/**
 * A custom textfield that can be collapsed. When collapsed it's value will be shown at the right side.
 * <p>
 * <p>
 * Created by jobaer on 4/19/17.
 */
public class DCACollapsibleTextField extends CustomComponent {
    private DCATextField textField = new DCATextField(false);

    public DCACollapsibleTextField(String label) {
        textField.setWidth(49, Unit.PERCENTAGE);
        DCACollapsibleFieldHelper collapsedText = new DCACollapsibleFieldHelper(label, layout -> layout.addComponent(textField));
        textField.addValueChangeListener(event -> {
            String updatedVal = textField.getValue();
            collapsedText.setValueLabel(updatedVal);
        });

        setCompositionRoot(collapsedText);
    }

    public void setValue(String val) {
        textField.setValue(val);
    }

    public String getValue() {
        return textField.getValue();
    }
}
