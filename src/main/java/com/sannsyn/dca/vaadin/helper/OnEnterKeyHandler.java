package com.sannsyn.dca.vaadin.helper;

import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.AbstractTextField;

/**
 * Custom enter key handler.
 * Reference - http://ramontalaverasuarez.blogspot.co.at/2014/06/vaadin-7-detect-enter-key-in-textfield.html
 */
public abstract class OnEnterKeyHandler {
    final ShortcutListener enterShortCut = new ShortcutListener(
        "EnterOnTextAreaShorcut", ShortcutAction.KeyCode.ENTER, null) {
        @Override
        public void handleAction(Object sender, Object target) {
            onEnterKeyPressed();
        }
    };

    public void installOn(final AbstractTextField component) {
        component.addFocusListener(
            event -> component.addShortcutListener(enterShortCut)
        );

        component.addBlurListener(
            event -> component.removeShortcutListener(enterShortCut)
        );
    }

    public abstract void onEnterKeyPressed();
}
