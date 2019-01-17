package com.sannsyn.dca.vaadin.widgets.common;

import com.vaadin.ui.Component;

import java.util.function.Consumer;

/**
 * A generic bread-crumb component
 *
 * Created by jobaer on 10/5/16.
 */
public interface DCABreadCrumb {
    /**
     * Get the view component of this breadcrumb. You will add this component in your layout.
     * @return the view component.
     */
    Component getView();

    /**
     * Add a breadcrumb action. The name will be shown and when clicked on the name, the action will be performed.
     * @param actionName Name of the action, will be shown in the view
     * @param action And the corresponding action object
     */
    void addAction(String actionName, Consumer<String> action);

    /**
     * Remove the action previously registered by this name.
     * @param actionName Name of the action
     */
    void removeAction(String actionName);

    /**
     * The action specified by the key will be performed.
     * @param actionName The name of the action to be performed.
     */
    void navigateTo(String actionName);
}
