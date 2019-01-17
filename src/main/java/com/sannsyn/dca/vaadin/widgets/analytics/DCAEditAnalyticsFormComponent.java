package com.sannsyn.dca.vaadin.widgets.analytics;

import com.google.gson.JsonObject;
import com.sannsyn.dca.vaadin.component.custom.field.DCAComboBox;
import com.sannsyn.dca.vaadin.component.custom.field.DCATextField;
import com.sannsyn.dca.vaadin.ui.DCAUiHelper;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

import static com.sannsyn.dca.vaadin.ui.DCAUiHelper.createSpacer;
import static com.sannsyn.dca.vaadin.ui.DCAUiHelper.groupComponentWithLabel;
import static com.sannsyn.dca.vaadin.ui.DCAUiHelper.wrapWithCssLayout;
import static com.sannsyn.dca.vaadin.widgets.analytics.DCAAnalyticsComponentFactory.SALES_BY_REC;
import static com.sannsyn.dca.vaadin.widgets.analytics.DCAAnalyticsDataParser.*;

/**
 * The create/edit form component for analytics
 * <p>
 * Created by jobaer on 4/26/17.
 */
public class DCAEditAnalyticsFormComponent extends CustomComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAEditAnalyticsFormComponent.class);

    private Consumer<JsonObject> previewAction = item -> {
    };
    private Consumer<JsonObject> saveAction = item -> {
    };
    private Runnable removeAction = () -> {
    };

    private DCATextField nameField = new DCATextField(false);
    private DCAComboBox typeField = new DCAComboBox();

    public DCAEditAnalyticsFormComponent(JsonObject item) {
        logger.debug("Got item = " + item);
        Component component = createFormUi(item);
        setCompositionRoot(component);
    }

    private Component createFormUi(JsonObject item) {
        CssLayout form = new CssLayout();
        form.addStyleName("analytics-edit-form-root");
        form.setWidth(100, Sizeable.Unit.PERCENTAGE);

        CssLayout headerWrapper = new CssLayout();
        headerWrapper.addStyleName("analytics-edit-form-header");
        headerWrapper.setWidth(100, Unit.PERCENTAGE);
        Label title = new Label("Create Dataset Presentation");
        title.setWidth(97, Unit.PERCENTAGE);
        headerWrapper.addComponent(title);

        CssLayout removeComponent = DCAUiHelper.createRemoveIcon(event -> removeModal());
        removeComponent.setWidth(3, Unit.PERCENTAGE);

        headerWrapper.addComponent(removeComponent);
        form.addComponent(headerWrapper);

        CssLayout formControls = new CssLayout();
        formControls.addStyleName("analytics-form-controls");
        formControls.setWidth(100, Unit.PERCENTAGE);

        Component nameFieldWrapper = prepareNameField(item);
        formControls.addComponent(nameFieldWrapper);

        Component typeFieldWrapper = prepareTypeField(item);
        formControls.addComponent(typeFieldWrapper);

        DCAAnalyticsWidgetDynamicFields salesByRecFields = DCAAnalyticsComponentFactory.createFieldsFor(SALES_BY_REC);
        salesByRecFields.setData(item);
        formControls.addComponent(salesByRecFields);

        Button discardButton = new Button("DISCARD");
        discardButton.setWidth(220, Unit.PIXELS);
        discardButton.addStyleName("btn-primary-style");

        Button previewButton = new Button("PREVIEW");
        previewButton.setWidth(220, Unit.PIXELS);
        previewButton.addStyleName("btn-primary-style");

        Button saveButton = new Button("SAVE");
        saveButton.setWidth(220, Unit.PIXELS);
        saveButton.addStyleName("btn-primary-style");

        CssLayout buttonRoot = new CssLayout();
        buttonRoot.addStyleName("button-root");
        buttonRoot.setWidth(100, Unit.PERCENTAGE);

        CssLayout buttonWrapper = new CssLayout();
        buttonWrapper.addStyleName("edit-analytics-button-wrapper");

        buttonWrapper.addComponent(wrapWithCssLayout(discardButton, "edit-analytics-button"));
        buttonWrapper.addComponent(wrapWithCssLayout(previewButton, "edit-analytics-button"));
        buttonWrapper.addComponent(wrapWithCssLayout(saveButton, "edit-analytics-button"));

        buttonRoot.addComponent(buttonWrapper);
        formControls.addComponent(buttonRoot);

        discardButton.addClickListener(event -> removeModal());
        previewButton.addClickListener(event -> {
            JsonObject jsonObject = salesByRecFields.getData();
            JsonObject previewJson = makePreviewJson(jsonObject);
            previewAction.accept(previewJson);
        });

        saveButton.addClickListener(event -> {
            String name = nameField.getValue();
            String type = getValue(typeField);

            String uuid = "";
            if (item.has(KEY_UUID)) {
                uuid = item.get(KEY_UUID).getAsString();
            }
            JsonObject data = salesByRecFields.getData();
            JsonObject jsonObject = mergeProperties(data, name, type, uuid);
            saveAction.accept(jsonObject);
        });


        form.addComponent(formControls);

        return form;
    }

    private String getValue(DCAComboBox comboBox) {
        Object value = comboBox.getValue();
        return value == null ? "" : value.toString();
    }

    private CssLayout prepareTypeField(JsonObject item) {
        typeField.addItem(SALES_BY_REC);
        if (isEditMode(item)) {
            typeField.setEnabled(false);
        }
        String typeValue = getPropertySafe(item, TYPE);
        if (StringUtils.isNotBlank(typeValue)) {
            typeField.select(typeValue);
        }

        CssLayout wrapper = new CssLayout();
        wrapper.addStyleName("analytics-edit-form-name-wrapper");
        wrapper.addStyleName("analytics-form-type-field");
        wrapper.setWidth(50, Unit.PERCENTAGE);

        CssLayout layout = groupComponentWithLabel("Type: ", typeField);
        wrapper.addComponent(layout);

        return wrapWithCssLayout(wrapper, "analytics-form-field", 100);
    }

    private CssLayout prepareNameField(JsonObject item) {
        String nameVal = getPropertySafe(item, NAME);
        nameField.setValue(nameVal);

        CssLayout wrapper = new CssLayout();
        wrapper.addStyleName("analytics-edit-form-name-wrapper");
        wrapper.setWidth(50, Unit.PERCENTAGE);

        CssLayout layout = groupComponentWithLabel("Name: ", nameField);
        wrapper.addComponent(layout);

        return wrapWithCssLayout(wrapper, "analytics-edit-form-name-root", 100);
    }

    private JsonObject mergeProperties(JsonObject data, String name, String type, String uuid) {
        data.addProperty(NAME, name);
        data.addProperty(TYPE, type);
        data.addProperty(KEY_UUID, uuid);
        return data;
    }

    private boolean isEditMode(JsonObject item) {
        if (item != null && item.has(KEY_UUID)) {
            String uuidStr = item.get(KEY_UUID).getAsString();
            if (StringUtils.isNotBlank(uuidStr)) {
                return true;
            }
        }
        return false;
    }

    private void removeModal() {
        removeAction.run();
    }

    void setRemoveAction(Runnable removeAction) {
        this.removeAction = removeAction;
    }

    void setPreviewAction(Consumer<JsonObject> previewAction) {
        this.previewAction = previewAction;
    }

    void setSaveAction(Consumer<JsonObject> saveAction) {
        this.saveAction = saveAction;
    }
}
