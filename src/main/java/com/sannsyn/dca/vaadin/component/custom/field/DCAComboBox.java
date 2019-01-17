package com.sannsyn.dca.vaadin.component.custom.field;

import com.sannsyn.dca.vaadin.ui.DCAUiHelper;
import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.FieldEvents;
import com.vaadin.ui.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;

/**
 * A ComboBox object styled according to DCA theme.
 *
 * Created by mashiur on 4/18/16.
 */
public class DCAComboBox extends CustomComponent {
    private static String defaultWrapperClassName = "dca-combo-box";
    private ComboBox comboBox = new ComboBox();
    private IndexedContainer indexedContainer = new IndexedContainer();
    private Layout root = new CssLayout();
    private CssLayout errorLayout = new CssLayout();
    private CssLayout comboboxWrapper;

    public DCAComboBox(String caption , Set<String> items) {
        this(caption, defaultWrapperClassName, items);
    }

    public DCAComboBox(){
        comboBox.setWidth(100, PERCENTAGE);
        root.setStyleName("dca-combo-box primary");
        comboboxWrapper = DCAUiHelper.wrapWithCssLayout(comboBox, "dca-combo-box-wrapper");
        comboboxWrapper.setWidth(100, PERCENTAGE);
        root.addComponent(comboboxWrapper);
        errorLayout.setWidth(100, PERCENTAGE);
        errorLayout.addStyleName("dca-combo-box-error");
        errorLayout.setVisible(false);
        comboBox.addFocusListener((FieldEvents.FocusListener) event -> clearErrorMessage());
        root.addComponent(errorLayout);
        setCompositionRoot(root);
    }

    public DCAComboBox(String caption, String wrapperStyleName, Set<String> items) {
        comboBox.setCaption(caption);

        items.stream().filter(StringUtils::isNotEmpty).forEach(indexedContainer::addItem);
        comboBox.setContainerDataSource(indexedContainer);

        root.setStyleName(wrapperStyleName);
        root.addComponent(comboBox);

        setCompositionRoot(root);
    }

    public void showErrorMessage(String msg) {
        errorLayout.addComponent(new Label(msg));
        comboboxWrapper.addStyleName("dca-combo-box-invalid");
        errorLayout.setVisible(true);
    }

    public void clearErrorMessage() {
        errorLayout.removeAllComponents();
        comboboxWrapper.removeStyleName("dca-combo-box-invalid");
        errorLayout.setVisible(false);
    }

    public ComboBox getComboBox() {
        return comboBox;
    }

    public void setNullSelectionAllowed(boolean b) {
        comboBox.setNullSelectionAllowed(b);
    }

    public void addItem(String item) {
        comboBox.addItem(item);
    }

    public Object getValue() {
        return comboBox.getValue();
    }

    public void addItem(Object item) {
        comboBox.addItem(item);
    }

    public void select(Object item) {
        comboBox.select(item);
    }

    public void setInputPrompt(String s) {
        comboBox.setInputPrompt(s);
    }

    public void addItems(Object... items) {
        comboBox.addItems(items);
    }

    public void removeAllItems() {
        comboBox.removeAllItems();
    }

    public void addValueChangeListener(Property.ValueChangeListener listener) {
        comboBox.addValueChangeListener(listener);
    }
}
