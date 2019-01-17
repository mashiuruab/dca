package com.sannsyn.dca.vaadin.login;

import com.vaadin.navigator.View;

import java.util.function.Supplier;

/**
 * Created by jobaer on 1/24/2016.
 */
public interface DCALoginView extends View {
    void setHandler(DCALoginViewHandler handler);
    void init(Supplier<String> defaultSectionSupplier);

    String getUsername();
    String getPassword();

    void afterSuccessfulLogin();
    void failedLogin(String message);
}
