package com.sannsyn.dca.vaadin.component.custom.logout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sannsyn.dca.presenter.DCAAdminPresenter;
import com.sannsyn.dca.vaadin.component.custom.DCAWrapper;
import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.component.custom.logout.model.DCAChangePassword;
import com.sannsyn.dca.vaadin.component.custom.logout.model.DCAChangePasswordWrapper;
import com.sannsyn.dca.vaadin.helper.OnEnterKeyHandler;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAPopupMessageComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.recommenders.DCACloseIconComponent;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.PasswordField;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

/**
 * Created by mashiur on 11/22/16.
 */
public class DCAChangePasswordComponent extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAChangePasswordComponent.class);

    private DCAChangePasswordComponent currentComponent;
    private CssLayout messageContainer = new CssLayout(){
        @Override
        public void addComponent(Component c) {
            super.addComponent(c);
            addStyleName("show");
        }

        @Override
        public void removeAllComponents() {
            super.removeAllComponents();
            removeStyleName("show");
        }
    };

    private PasswordField currentPasswordField = new PasswordField();
    private PasswordField newPasswordField = new PasswordField();
    private PasswordField retypeNewPasswordField = new PasswordField();


    public DCAChangePasswordComponent(DCAAdminPresenter adminPresenter, DCALayoutContainer layoutContainer) {
        setAdminPresenter(adminPresenter);
        setLayoutContainer(layoutContainer);
        currentComponent = this;

        try {
            init();
        } catch (Exception e) {
            logger.error("Error : ", e);
            addComponentAsLast(new DCAError(String.format("Error Happened While creating the component %s", e.getMessage())), this);
        }
    }

    private void init() {
        this.setStyleName("dca-change-password-component");

        DCALabel headerLabel = new DCALabel("Change password", "label");

        String randomId = UUID.randomUUID().toString();
        DCACloseIconComponent removeComponent = new DCACloseIconComponent();
        removeComponent.setId(randomId);


        DCAWrapper headerWrapper = new DCAWrapper(Arrays.asList(headerLabel, removeComponent), "header-wrapper");

        headerWrapper.addLayoutClickListener(event -> {
            if (event.getChildComponent() == null) {
                return;
            }

            if (randomId.equals(event.getChildComponent().getId())) {
                removePasswordComponent();
            }
        });

        String textDescription = "Please fill out the following information to change your password!";
        DCALabel descriptionComponent = new DCALabel(textDescription, "description");

        this.addComponent(headerWrapper);
        this.addComponent(descriptionComponent);

        currentPasswordField.setStyleName("current-password");
        currentPasswordField.setImmediate(true);

        newPasswordField.setStyleName("new-password");
        newPasswordField.setImmediate(true);

        retypeNewPasswordField.setStyleName("retype-new-password");
        retypeNewPasswordField.setImmediate(true);

        OnEnterKeyHandler passwordEnterKeyHandler = new OnEnterKeyHandler() {
            @Override
            public void onEnterKeyPressed() {
                handleChangePassword();
            }
        };

        passwordEnterKeyHandler.installOn(currentPasswordField);
        passwordEnterKeyHandler.installOn(newPasswordField);
        passwordEnterKeyHandler.installOn(retypeNewPasswordField);

        this.addComponent(currentPasswordField);
        this.addComponent(newPasswordField);
        this.addComponent(retypeNewPasswordField);


        messageContainer.setStyleName("password-message-container");

        this.addComponent(messageContainer);

        Button doneButton = new Button("DONE");
        doneButton.setStyleName("btn-primary");

        doneButton.addClickListener(event -> {
            handleChangePassword();
        });

        this.addComponent(doneButton);

        Page.getCurrent().getJavaScript().execute("handleChangePasswordPlaceHolder()");


        ShortcutListener removeModalComponentKeyListener = new ShortcutListener("Esc Key", ShortcutAction.KeyCode.ESCAPE, null) {

            @Override
            public void handleAction(Object sender, Object target) {
                removePasswordComponent();
            }
        };

        this.addShortcutListener(removeModalComponentKeyListener);
    }


    private void removePasswordComponent() {
        if (currentComponent.getParent() instanceof DCAModalComponent) {
            DCAModalComponent popupComponent = (DCAModalComponent) currentComponent.getParent();
            removeComponent(popupComponent, getLayoutContainer().getWidgetContainer());
        }
    }

    private void handleChangePassword() {
        String currentPasswordValue = currentPasswordField.getValue();
        String newPasswordValue = newPasswordField.getValue();
        String retypesPasswordValue = retypeNewPasswordField.getValue();

        messageContainer.removeAllComponents();


        if (StringUtils.isEmpty(currentPasswordValue) || StringUtils.isEmpty(newPasswordValue)
                || StringUtils.isEmpty(retypesPasswordValue)) {
            addComponentAsLast(new DCAError("All the Fields are Required"), messageContainer);
            return;
        }


        if (!newPasswordValue.equals(retypesPasswordValue)) {
            addComponentAsLast(new DCAError("New Password did not match"), messageContainer);
            return;
        }

        DCAChangePasswordWrapper changePasswordWrapper =
                createJson(getLoggedInUser().getUsername(), currentPasswordValue, newPasswordValue);

        try {
            String responseString = getAdminPresenter().postEntity(changePasswordWrapper, getLoggedInUser());
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            Map<String, String> responseStatusMap = gson.fromJson(responseString, type);


            if ("ok".equals(responseStatusMap.get("status"))) {
                String successMessage = "Password  Updated Successfully";
                removePasswordComponent();
                DCAPopupMessageComponent successMessageComponent =
                        new DCAPopupMessageComponent("DONE:", successMessage, getLayoutContainer().getWidgetContainer());
                addComponentAsLast(successMessageComponent, getLayoutContainer().getWidgetContainer());
            } else {
                addComponentAsLast(new DCAError(responseStatusMap.get("msg")), messageContainer);
            }
        } catch (Exception e) {
            logger.error("Error :", e);
            addComponentAsLast(new DCAError(String.format("Error : %s", e.getMessage())), messageContainer);
        }
    }

    private DCAChangePasswordWrapper createJson(String username, String currentPassword, String newPassword) {
        DCAChangePassword changePassword = new DCAChangePassword();
        changePassword.setUsername(username);
        changePassword.setCurrentpassword(currentPassword);
        changePassword.setNewpassword(newPassword);

        DCAChangePasswordWrapper changePasswordWrapper = new DCAChangePasswordWrapper();
        changePasswordWrapper.setUser(changePassword);

        return changePasswordWrapper;
    }
}
