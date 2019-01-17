package com.sannsyn.dca.vaadin.login;

import com.sannsyn.dca.model.user.DCAUserException;
import com.sannsyn.dca.service.DCAUserService;

/**
 * Created by jobaer on 1/24/2016.
 */
public class DCALoginPresenter implements DCALoginViewHandler {
    private DCALoginView loginView;
    private DCAUserService userService;

    public DCALoginPresenter(DCALoginView loginView, DCAUserService userService) {
        this.loginView = loginView;
        this.userService = userService;
    }

    @Override
    public void login() {
        String username = loginView.getUsername();
        String password = loginView.getPassword();

        try {
            userService.login(username, password);
            loginView.afterSuccessfulLogin();
        } catch (DCAUserException exp) {
            loginView.failedLogin(exp.getMessage());
        }
    }
}
