package com.sannsyn.dca.vaadin.login;

import com.sannsyn.dca.metadata.DCAMetadataConfig;
import com.sannsyn.dca.vaadin.component.custom.field.DCATextField;
import com.sannsyn.dca.vaadin.helper.OnEnterKeyHandler;
import com.sannsyn.dca.vaadin.servlet.DCAUserPreference;
import com.sannsyn.dca.vaadin.servlet.DCAUtils;
import com.sannsyn.dca.vaadin.ui.DCAUI;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.*;

import java.io.File;
import java.util.function.Supplier;


public class DCALoginViewImpl extends CustomComponent implements DCALoginView {
    private DCALoginViewHandler handler;
    private DCATextField txtUsername;
    private PasswordField txtPassword;
    private Supplier<String> defaultSectionSupplier;

    @Override
    public void setHandler(DCALoginViewHandler handler) {
        this.handler = handler;
    }

    @Override
    public void init(Supplier<String> defaultSectionSupplier) {
        this.defaultSectionSupplier = defaultSectionSupplier;
        CustomLayout content = createCustomLayout();
        addStyleName("login-custom-layout");
        setHeight(100, Unit.PERCENTAGE);

        String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
        FileResource bottomImageResource = new FileResource(new File(basepath + "/WEB-INF/images/logo.png"));
        Image logo = new Image("", bottomImageResource);
        content.addComponent(logo, "logo");

        txtUsername = new DCATextField(false);
        txtUsername.setInputPrompt("Username");
        content.addComponent(txtUsername, "username");

        txtPassword = new PasswordField("");
        txtPassword.setId("password-input");
        //todo Right now vaadin lacks a proper way to set placeholder attribute. So we are doing a js hack here.
        Page.getCurrent().getJavaScript().execute(
            "if(document.getElementById('password-input')) {\n" +
                "        document.getElementById('password-input').placeholder = 'Password';\n" +
                "    }");

        txtPassword.setImmediate(true);
        OnEnterKeyHandler enterHandler = new OnEnterKeyHandler() {
            @Override
            public void onEnterKeyPressed() {
                handler.login();
            }
        };
        enterHandler.installOn(txtPassword);

        content.addComponent(txtPassword, "password");

        Button loginButton = new Button("SIGN IN");
        loginButton.addStyleName("btn-primary-style");
        content.addComponent(loginButton, "signin");
        loginButton.addClickListener(clickEvent -> {
            handler.login();
        });

        setCompositionRoot(content);
    }

    private CustomLayout createCustomLayout() {
        return new CustomLayout("login-layout");
    }

    @Override
    public String getUsername() {
        return txtUsername.getValue();
    }

    @Override
    public String getPassword() {
        return txtPassword.getValue();
    }

    @Override
    public void afterSuccessfulLogin() {
        DCAUtils.removeTargetService();
        DCAUserPreference.initPreference();
        DCAMetadataConfig.initMetaDataSettingsMap();
        String viewName = defaultSectionSupplier.get();
        UI.getCurrent().getNavigator().navigateTo(viewName);
    }

    @Override
    public void failedLogin(String message) {
        Notification.show(message);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {

    }
}
