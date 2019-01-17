package com.sannsyn.dca.vaadin.widgets.users.roles;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.sannsyn.dca.service.DCAAdminService;
import com.sannsyn.dca.service.Status;
import com.sannsyn.dca.vaadin.component.custom.logout.DCAModalComponent;
import com.sannsyn.dca.vaadin.component.custom.notification.DCANotification;
import com.sannsyn.dca.vaadin.ui.DCAUiHelper;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.*;

import static com.sannsyn.dca.vaadin.ui.DCAUiHelper.addComponentAsLast;
import static com.sannsyn.dca.vaadin.ui.DCAUiHelper.wrapWithCssLayout;
import static com.vaadin.server.Sizeable.Unit.PIXELS;

/**
 * The edit role dialog component
 * <p>
 * Created by jobaer on 5/9/17.
 */
class DCAEditRoleDialog {
    private final UI currentUi;
    private Runnable refreshAction;
    private DCAAdminService adminService;

    private static final String UUID_KEY = "uuid";
    private static final String ROOT_KEY = "root";

    DCAEditRoleDialog(UI currentUi,
                      CssLayout rootLayout, JsonObject item, Runnable refreshAction, DCAAdminService adminService) {
        this.currentUi = currentUi;
        this.refreshAction = refreshAction;
        this.adminService = adminService;

        createEditDialog(rootLayout, item);
    }

    private void createEditDialog(CssLayout rootLayout, JsonObject item) {
        CssLayout newLayout = new CssLayout();
        newLayout.addStyleName("edit-conf-modal-wrapper");
        newLayout.addStyleName("edit-role-dialog");

        CssLayout inner = new CssLayout();
        inner.setWidth(100, Sizeable.Unit.PERCENTAGE);

        DCAModalComponent modal = new DCAModalComponent(newLayout);
        modal.addStyleName("edit-conf-modal");

        CssLayout headerWrapper = new CssLayout();
        headerWrapper.addStyleName("analytics-edit-form-header");
        headerWrapper.setWidth(100, Sizeable.Unit.PERCENTAGE);
        Label title = new Label("Edit role json");
        title.setWidth(97, Sizeable.Unit.PERCENTAGE);
        headerWrapper.addComponent(title);

        CssLayout removeComponent = DCAUiHelper.createRemoveIcon(event -> removeComponent(modal, rootLayout));
        removeComponent.setWidth(3, Sizeable.Unit.PERCENTAGE);

        headerWrapper.addComponent(removeComponent);
        inner.addComponent(headerWrapper);

        TextArea jsonEditor = new TextArea();
        jsonEditor.addStyleName("role-json-editor");
        CssLayout editorWrapper = wrapWithCssLayout(jsonEditor, "editor-wrapper", 100);

        setEditorValue(item, jsonEditor);

        inner.addComponent(editorWrapper);

        Button saveButton = new Button("SAVE");
        saveButton.setWidth(150, PIXELS);
        saveButton.addStyleName("btn-primary-style");
        saveButton.addStyleName("pull-right");
        saveButton.addStyleName("left-margin-10px");
        CssLayout buttonWrapper = wrapWithCssLayout(saveButton, "buttons-wrapper", 100);

        Button discardButton = new Button("DISCARD");
        discardButton.setWidth(150, PIXELS);
        discardButton.addStyleName("btn-primary-style");
        discardButton.addStyleName("pull-right");
        buttonWrapper.addComponent(discardButton);

        inner.addComponent(buttonWrapper);
        newLayout.addComponent(inner);

        Runnable removeAction = () -> removeComponent(modal, rootLayout);
        discardButton.addClickListener(event -> removeAction.run());
        saveButton.addClickListener(event -> updateRole(item, jsonEditor.getValue(), removeAction));

        addComponentAsLast(modal, rootLayout);
    }

    private void updateRole(JsonObject initialVersion, String updatedValue, Runnable removeAction) {
        if (!initialVersion.has(UUID_KEY)) {
            DCANotification.ERROR.show("Error occurred while updating the role. No UUID found.");
            return;
        }
        String uuid = initialVersion.get(UUID_KEY).getAsString();
        Status status = adminService.updateRole(updatedValue, uuid);
        if (Status.SUCCESS.equals(status)) {
            DCANotification.SUCCESS.show("The role was updated successfully.");
            removeAction.run();
            refreshAction.run();
        } else {
            DCANotification.ERROR.show("Error occurred while updating the role.");
        }
    }

    private void setEditorValue(JsonObject item, TextArea jsonEditor) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String value = "{\"status\": \"Error parsing role json\"}";
        if (item.has(ROOT_KEY)) {
            JsonObject root = item.get(ROOT_KEY).getAsJsonObject();
            value = gson.toJson(root);
        }
        jsonEditor.setValue(value);
    }

    private void removeComponent(Component component, CssLayout layout) {
        currentUi.access(() -> layout.removeComponent(component));
    }

}
