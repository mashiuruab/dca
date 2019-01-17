package com.sannsyn.dca.vaadin.pipes;

import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.service.DCAPipesConfigParser;
import com.sannsyn.dca.service.Status;
import com.sannsyn.dca.service.recommender.PipeProperty;
import com.sannsyn.dca.vaadin.pipes.propertyeditors.DCAPipePropertyEditor;
import com.sannsyn.dca.vaadin.pipes.propertyeditors.DCAPipePropertyEditorFactory;
import com.sannsyn.dca.vaadin.validators.NonEmptyValidator;
import com.sannsyn.dca.vaadin.widgets.controller.overview.model.aggregates.DCAPipe;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAPopupErrorComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAPopupMessageComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAPopupWarningComponent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

import static com.sannsyn.dca.service.Status.SUCCESS;
import static com.sannsyn.dca.service.Status.WARNING;
import static com.sannsyn.dca.vaadin.ui.DCAUiHelper.createSpacer;
import static com.sannsyn.dca.vaadin.widgets.controller.overview.model.aggregates.DCAPipeInstanceType.IMPLEMENTATION;
import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;

/**
 * Form for create/edit Pipes
 * <p>
 * Created by jobaer on 6/17/16.
 */
public class DCAPipeEditForm extends CustomComponent {
    public static final String SHOW_IN_MANAGE_FILTER_KEY = "showInManageFilter";
    public static final String KEY_COMPONENT_DESCRIPTION = "description";

    private TextField nameField;
    private TextField componentDescriptionField;
    private CssLayout itemDetails = new CssLayout();
    private CssLayout itemForm = new CssLayout();
    private List<DCAPipePropertyEditor> propertyEditors = new ArrayList<>();
    private final DCAPipeEditFormHandler handler;
    private DCAUser loggedInUser;

    private CheckBox showInManageFilterCheckbox;
    private Map<String, Object> pipeExternalData;

    DCAPipeEditForm(DCAUser loggedInUser, Runnable refreshAction) {
        Component component = createPipeDisplayComponent();
        setCompositionRoot(component);
        this.loggedInUser = loggedInUser;
        handler = new DCAPipeEditFormHandler(loggedInUser, refreshAction);
        itemForm.setWidth(100, PERCENTAGE);
    }

    public Map<String, Object> getPipeExternalData() {
        return pipeExternalData;
    }

    public void setPipeExternalData(Map<String, Object> pipeExternalData) {
        this.pipeExternalData = pipeExternalData;
    }

    private Component createPipeDisplayComponent() {
        itemDetails.setStyleName("create-recommender-component-show-item-wrapper");
        itemDetails.setWidth(100, PERCENTAGE);

        CssLayout layout = new CssLayout();
        layout.addStyleName("create-recommender-component-show-items-all");
        layout.setWidth(100, PERCENTAGE);

        layout.addComponent(itemDetails);
        layout.addComponent(itemForm);

        return layout;
    }

    private void showBasicInfo(DCAPipe item) {
        Layout wrapper = new CssLayout();
        wrapper.addStyleName("create-recommender-component-item-details-wrapper");
        wrapper.setWidth(100, PERCENTAGE);

        Label classLabel = new Label("Component class: ");
        classLabel.addStyleName("create-recommender-component-item-details-field");
        classLabel.setWidth(20, PERCENTAGE);
        wrapper.addComponent(classLabel);

        Label classValue = new Label(item.getClazz());
        classValue.addStyleName("create-recommender-component-item-details-field");
        classValue.setWidth(80, PERCENTAGE);
        wrapper.addComponent(classValue);

        Label descriptionLabel = new Label("Description: ");
        descriptionLabel.addStyleName("create-recommender-component-item-details-field");
        descriptionLabel.setWidth(20, PERCENTAGE);
        wrapper.addComponent(descriptionLabel);
        Label descValue = new Label(item.getDescription(), ContentMode.HTML);
        descValue.addStyleName("create-recommender-component-item-details-field");
        descValue.setWidth(80, PERCENTAGE);
        wrapper.addComponent(descValue);

        Label typeLabel = new Label("Type: ");
        typeLabel.addStyleName("create-recommender-component-item-details-field");
        typeLabel.setWidth(20, PERCENTAGE);
        wrapper.addComponent(typeLabel);

        String type = item.getType();
        if ("pipe".equalsIgnoreCase(type)) {
            type = "join";
        }
        Label typeValue = new Label(type);
        typeValue.addStyleName("create-recommender-component-item-details-field");
        typeValue.setWidth(80, PERCENTAGE);
        wrapper.addComponent(typeValue);

        Label nameLabel = new Label("Component name: ");
        nameLabel.addStyleName("create-recommender-component-item-details-field");
        nameLabel.setWidth(20, PERCENTAGE);
        wrapper.addComponent(nameLabel);

        nameField = new TextField();
        nameField.addValidator(new NonEmptyValidator("Name"));
        nameField.addStyleName("create-recommender-component-item-details-field");
        nameField.setValue(item.getName());
        nameField.setWidth(75, PERCENTAGE);
        wrapper.addComponent(nameField);

        Label componentDescriptionLabel = new Label("Component Description: ");
        componentDescriptionLabel.addStyleName("create-recommender-component-item-details-field");
        componentDescriptionLabel.setWidth(20, PERCENTAGE);
        wrapper.addComponent(componentDescriptionLabel);

        componentDescriptionField = new TextField(); //todo show component description
        componentDescriptionField.addStyleName("create-recommender-component-item-details-field");
        componentDescriptionField.setWidth(75, PERCENTAGE);
        componentDescriptionField.setValue(item.getComponentDescription());
        wrapper.addComponent(componentDescriptionField);

        itemDetails.addComponent(wrapper);
    }

    void showItemDetails(DCAPipe item) {
        itemDetails.removeAllComponents();
        showBasicInfo(item);
        showUpdatedForm(item);
    }

    private CssLayout getShowInDCA(DCAPipe item) {
        CssLayout cssLayout = new CssLayout();
        cssLayout.setStyleName("create-recommender-form-field");
        cssLayout.setWidth(100, PERCENTAGE);

        Label label = new Label("Visible in Manage Filters" + ": ");
        label.setWidth(30, PERCENTAGE);

        boolean showInManageFilter = getPipeExternalData().containsKey(item.getName()) &&
                ((Map<String, Object>) getPipeExternalData().get(item.getName())).containsKey(SHOW_IN_MANAGE_FILTER_KEY) ?
                Boolean.valueOf(((Map<String, Object>) getPipeExternalData().get(item.getName())).get(SHOW_IN_MANAGE_FILTER_KEY).toString()) : false;

        showInManageFilterCheckbox = new CheckBox("", showInManageFilter);
        showInManageFilterCheckbox.setWidth(69, PERCENTAGE);

        cssLayout.addComponent(label);
        cssLayout.addComponent(showInManageFilterCheckbox);

        return cssLayout;
    }

    private Component createForm(DCAPipe item) {
        Layout cssLayout = new CssLayout();
        cssLayout.setWidth(100, PERCENTAGE);
        cssLayout.addStyleName("create-recommender-component-item-form-wrapper");

        Label heading = new Label("Implementation specific values:");
        heading.setWidth(100, PERCENTAGE);
        heading.setStyleName("create-recommender-component-item-form-heading");
        cssLayout.addComponent(heading);

        DCAPipesConfigParser parser = new DCAPipesConfigParser();
        List<PipeProperty> allPropertiesFor = parser.getAllPropertiesFor(item.getClazz());

        Component formForProperties = createFormForProperties(item, allPropertiesFor);
        formForProperties.setWidth(100, PERCENTAGE);
        cssLayout.addComponent(formForProperties);

        CssLayout showInDCAComponent = getShowInDCA(item);
        cssLayout.addComponent(showInDCAComponent);

        Layout buttonLayout = new CssLayout();
        buttonLayout.addStyleName("create-recommender-component-update-button-layout");
        buttonLayout.setWidth(100, PERCENTAGE);
        buttonLayout.addComponent(createSpacer(70));

        String buttonLabel = "CREATE COMPONENT";
        if (item.getComponentType().equals(IMPLEMENTATION)) {
            buttonLabel = "UPDATE COMPONENT";
        }
        Button create = new Button(buttonLabel);
        create.addClickListener(event -> updateComponent(item));

        create.addStyleName("btn-primary-style");
        create.setWidth(30, PERCENTAGE);
        buttonLayout.addComponent(create);

        cssLayout.addComponent(buttonLayout);
        return cssLayout;
    }

    private void showUpdatedForm(DCAPipe item) {
        itemForm.removeAllComponents();
        Component form = createForm(item);
        itemForm.addComponent(form);
    }

    private void showNotification(Pair<Status, String> statusStringPair) {
        if (SUCCESS.equals(statusStringPair.getLeft())) {
            showSuccessMessage(statusStringPair.getRight());
        } else if (WARNING.equals(statusStringPair.getLeft())) {
            showWarningMessage(statusStringPair.getRight());
        } else {
            showErrorMessage(statusStringPair.getRight());
        }
    }

    private void showSuccessMessage(String message) {
        DCAPopupMessageComponent successMessageComponent = new DCAPopupMessageComponent("Success:", message, itemForm);
        itemForm.addComponent(successMessageComponent);
    }

    private void showErrorMessage(String message) {
        DCAPopupErrorComponent successMessageComponent = new DCAPopupErrorComponent("Failure:", message, itemForm);
        itemForm.addComponent(successMessageComponent);
    }

    private void showWarningMessage(String message) {
        DCAPopupWarningComponent messageComponent = new DCAPopupWarningComponent("Warning:", message, itemForm);
        itemForm.addComponent(messageComponent);
    }

    private void updateComponent(DCAPipe item) {
        boolean isValid = isFormValid();
        if (!isValid) {
            showErrorMessage("Please make sure the field values are valid.");
            return;
        }

        // Get all form values and create a map
        Map<String, Object> formValues = prepareFormValues(item);

        if (!getPipeExternalData().containsKey(item.getName())) {
            getPipeExternalData().put(item.getName(), new HashMap<>());
        }

        ((Map<String, Object>) getPipeExternalData().get(item.getName())).put(SHOW_IN_MANAGE_FILTER_KEY, showInManageFilterCheckbox.getValue());

        Pair<Status, String> statusStringPair = handler.createOrUpdatePipe(formValues, item.getClazz(), getPipeExternalData());
        showNotification(statusStringPair);
    }

    private Map<String, Object> prepareFormValues(DCAPipe item) {
        String name = nameField.getValue();
        String componentDescription = StringUtils.stripToEmpty(componentDescriptionField.getValue());
        Map<String, Object> formValues = new HashMap<>();
        formValues.put("name", name);
        formValues.put("class", item.getClazz());
        formValues.put(KEY_COMPONENT_DESCRIPTION, componentDescription);

        propertyEditors.forEach(editor -> {
            if (editor.getValue() != null && !KEY_COMPONENT_DESCRIPTION.equals(editor.getName())) {
                Optional<Object> editorValue = editor.getValue();
                editorValue.ifPresent(o -> formValues.put(editor.getName(), o));
            }
        });
        return formValues;
    }

    private boolean isFormValid() {
        if (!nameField.isValid()) {
            return false;
        }

        for (DCAPipePropertyEditor propertyEditor : propertyEditors) {
            if (!propertyEditor.isValid()) {
                return false;
            }
        }
        return true;
    }

    private Component createFormForProperties(DCAPipe item, List<PipeProperty> allPropertiesFor) {
        Layout cssLayout = new CssLayout();

        propertyEditors.clear();
        for (PipeProperty property : allPropertiesFor) {
            if (KEY_COMPONENT_DESCRIPTION.equals(property.getPropertyName())) {
                continue;
            }
            Component field = createInputField(item, property);
            cssLayout.addComponent(field);
        }

        return cssLayout;
    }

    private Component createInputField(DCAPipe item, PipeProperty property) {
        DCAPipePropertyEditor propertyEditor =
            DCAPipePropertyEditorFactory.createPropertyEditor(item, property);
        propertyEditors.add(propertyEditor);
        return propertyEditor.getComponent();
    }
}
