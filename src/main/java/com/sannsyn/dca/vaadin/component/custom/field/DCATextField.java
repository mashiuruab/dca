package com.sannsyn.dca.vaadin.component.custom.field;

import com.sannsyn.dca.vaadin.helper.OnEnterKeyHandler;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.event.FieldEvents;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;

/**
 * A custom textfield with proper styling for DCA. There are three types of field that can be created.
 * - with caption/label
 * - without caption or label
 * - with an icon inside the input prompt (for example search icon).
 * <p>
 * Created by mashiur on 4/13/16.
 */
public class DCATextField extends CustomComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCATextField.class);

    private TextField textField = new TextField();
    private boolean isRequired;
    private CssLayout currentLayout;

    public DCATextField(String labelName, String additionalStyleName, boolean isRequired) {
        this(additionalStyleName, isRequired);
        setCaption(labelName);
    }

    public DCATextField(boolean isRequired) {
        this("primary", isRequired);
    }

    public DCATextField(Resource icon, boolean isRequired) {
        this("primary with-icon", isRequired);
        textField.setIcon(icon);
        textField.setRequired(isRequired);

        textField.addFocusListener(event -> {
            currentLayout.addStyleName("focused");
        });

        textField.addBlurListener(event -> {
            currentLayout.removeStyleName("focused");
        });
    }


    public DCATextField(String additionalStyleName, boolean isRequired) {
        this.isRequired = isRequired;
        currentLayout = new CssLayout();
        textField.setStyleName("form-control");
        textField.setRequired(isRequired);

        textField.addValidator(o -> {
            currentLayout.removeStyleName("has-success");
            currentLayout.removeStyleName("has-error");
            if (!String.valueOf(o).isEmpty()) {
                currentLayout.addStyleName("has-success");
            } else {
                currentLayout.removeStyleName("has-success");
                if (isRequired()) {
                    currentLayout.addStyleName("has-error");
                }
            }
        });

        currentLayout.setStyleName("form-group");
        currentLayout.addStyleName(additionalStyleName);
        currentLayout.addComponent(textField);

        setCompositionRoot(currentLayout);
    }

    public void installEnterKeyHandler(@NotNull OnEnterKeyHandler handler) {
        handler.installOn(textField);
    }

    public TextField getTextField() {
        return textField;
    }

    public void setCaption(String labelName) {
        textField.setCaption(labelName);
    }

    public boolean isRequired() {
        return isRequired;
    }

    public String getValue() {
        return textField.getValue() == null ? "" : textField.getValue().trim();
    }

    public void setInputPrompt(String inputPrompt) {
        textField.setInputPrompt(inputPrompt);
    }

    public void clear() {
        textField.clear();
    }

    public void focus() {
        textField.focus();
    }

    public void setValue(String value) {
        textField.setValue(value);
    }

    void addTextChangeListener(FieldEvents.TextChangeListener listener) {
        textField.addTextChangeListener(listener);
    }

    void setValidationVisible(boolean visible) {
        textField.setValidationVisible(visible);
    }

    void addValidator(Validator validator) {
        textField.addValidator(validator);
    }

    void validate() throws Validator.InvalidValueException {
        textField.validate();
    }

    public void addFocusListener(FieldEvents.FocusListener listener) {
        textField.addFocusListener(listener);
    }

    public void addValueChangeListener (Property.ValueChangeListener listener) {
        textField.addValueChangeListener(listener);
    }

    public void addBlurListener(FieldEvents.BlurListener listener) {
        textField.addBlurListener(listener);
    }
}
