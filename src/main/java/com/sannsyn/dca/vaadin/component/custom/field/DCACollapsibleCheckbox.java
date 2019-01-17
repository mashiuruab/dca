package com.sannsyn.dca.vaadin.component.custom.field;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.CustomComponent;

/**
 * A custom checkbox that can be collapsed. When collapsed it's value will be shown at the right side.
 * <p>
 * <p>
 * Created by jobaer on 4/19/17.
 */
public class DCACollapsibleCheckbox extends CustomComponent {
    private CheckBox checkBox = new CheckBox();
    private DCACollapsibleFieldHelper collapsibleFieldHelper;

    public DCACollapsibleCheckbox(String label) {
        checkBox.setWidth(100, Unit.PERCENTAGE);
        collapsibleFieldHelper = new DCACollapsibleFieldHelper(label, layout -> layout.addComponent(checkBox));
        checkBox.addValueChangeListener(event -> updateValueLabel());

        setCompositionRoot(collapsibleFieldHelper);
    }

    public void setValue(Boolean value) {
        checkBox.setValue(value);
    }

    private void updateValueLabel() {
        Boolean value = checkBox.getValue();
        collapsibleFieldHelper.setValueLabel(value.toString());
    }

    /**
     * Returns the value represented by this field
     *
     * @return boolean representing the checkbox state
     */
    public Boolean getValue() {
        return checkBox.getValue();
    }
}
