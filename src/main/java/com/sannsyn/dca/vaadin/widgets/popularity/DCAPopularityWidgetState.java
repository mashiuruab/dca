package com.sannsyn.dca.vaadin.widgets.popularity;

import com.vaadin.shared.ui.JavaScriptComponentState;

/**
 * This is the js state objects used in popularity widget
 *
 * Created by jobaer on 3/7/16.
 */
public class DCAPopularityWidgetState extends JavaScriptComponentState {
    public String value;

    private String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
