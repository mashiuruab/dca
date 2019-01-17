package com.sannsyn.dca.vaadin.component.custom.field;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.CustomComponent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A custom checkbox that can be collapsedIcon. When collapsedIcon it's value will be shown at the right side.
 * Supports multiple values.
 * <p>
 * Created by jobaer on 4/19/17.
 */
public class DCACollapsibleMultiCheckbox extends CustomComponent {
    private final String label;
    private Map<CheckBox, String> checkboxes = new LinkedHashMap<>();
    private DCACollapsibleFieldHelper collapsibleFieldHelper;

    public DCACollapsibleMultiCheckbox(String label) {
        this.label = label;
        paintComponents(label);
    }

    private void paintComponents(String label) {
        collapsibleFieldHelper = new DCACollapsibleFieldHelper(label, layout -> {
            for (CheckBox checkBox : checkboxes.keySet()) {
                layout.addComponent(checkBox);
            }
        });

        setCompositionRoot(collapsibleFieldHelper);
    }

    /**
     * Add a checkbox item
     *
     * @param label The label that will be shown
     * @param value The value that will be shown or returned
     */
    public void addItem(String label, String value) {
        CheckBox box = createCheckbox(label);
        checkboxes.put(box, value);
        paintComponents(this.label);
    }

    /**
     * Sets the values of the checkboxes. A list of string should be supplied. For each item in the list
     * if it is a valid value in the options then the checkbox will be selected.
     *
     * @param checkedList a list of string.
     */
    public void setValue(List<String> checkedList) {
        for (Map.Entry<CheckBox, String> entry : checkboxes.entrySet()) {
            String value = entry.getValue();
            if(checkedList.contains(value)) {
                CheckBox checkBox = entry.getKey();
                checkBox.setValue(true);
            }
        }
    }

    private void updateValueLabel() {
        String values = getValue();
        collapsibleFieldHelper.setValueLabel(values);
    }



    /**
     * Returns the value represented by this field
     *
     * @return a string concatenating the checked items' values
     */
    public String getValue() {
        StringBuilder value = new StringBuilder();
        for (Map.Entry<CheckBox, String> checkBoxStringEntry : checkboxes.entrySet()) {
            CheckBox checkBox = checkBoxStringEntry.getKey();
            String val = checkBoxStringEntry.getValue();
            if (checkBox.getValue()) {
                value.append(val).append(", ");
            }
        }
        return value.toString();
    }

    private CheckBox createCheckbox(String label) {
        CheckBox box = new CheckBox(label);
        box.setWidth(100, Unit.PERCENTAGE);

        box.addValueChangeListener(event -> updateValueLabel());

        return box;
    }
}
