package com.sannsyn.dca.vaadin.component.custom.field;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;

/**
 * A custom combobox that can be collapsed. When collapsed it's value will be shown at the right side.
 * <p>
 * <p>
 * Created by jobaer on 08-may-2017
 */
public class DCACollapsibleComboBox extends CustomComponent {
    private ComboBox comboBox = new ComboBox();
    private DCACollapsibleFieldHelper collapsibleFieldHelper;

    public DCACollapsibleComboBox(String label) {
        comboBox.setWidth(95, Unit.PERCENTAGE);
        paintComponents(label);
    }

    private void paintComponents(String label) {
        collapsibleFieldHelper = new DCACollapsibleFieldHelper(label, layout -> layout.addComponent(comboBox));
        comboBox.addValueChangeListener(event -> updateValueLabel());
        setCompositionRoot(collapsibleFieldHelper);
    }

    /**
     * Add an item to the combo box.
     *
     * @param value The item to be added as a value
     */
    public void addItem(String value) {
        comboBox.addItem(value);
    }

    private void updateValueLabel() {
        String values = getValue();
        collapsibleFieldHelper.setValueLabel(values);
    }

    public void setValue(String value) {
        comboBox.setValue(value);
    }

    /**
     * Returns the value represented by this field
     *
     * @return a string value
     */
    public String getValue() {
        Object value = comboBox.getValue();
        return value == null ? "" : value.toString();
    }

    @Override
    public void setEnabled(boolean enabled) {
        comboBox.setEnabled(enabled);
    }
}
