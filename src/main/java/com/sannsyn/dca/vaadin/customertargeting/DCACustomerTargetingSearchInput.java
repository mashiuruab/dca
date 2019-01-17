package com.sannsyn.dca.vaadin.customertargeting;

import com.sannsyn.dca.vaadin.component.custom.field.DCATextField;
import com.sannsyn.dca.vaadin.helper.OnEnterKeyHandler;
import com.sannsyn.dca.vaadin.icons.SannsynIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Layout;

import java.util.function.Consumer;

import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;

/**
 * The search input component for customer targeting widget.
 * <p>
 * Created by jobaer on 8/22/16.
 */
class DCACustomerTargetingSearchInput extends CustomComponent {
    private Consumer<String> action;

    DCACustomerTargetingSearchInput(Consumer<String> action) {
        this.action = action;
        Layout searchInput = createSearchInput();
        setCompositionRoot(searchInput);
    }

    private Layout createSearchInput() {
        CssLayout cssLayout = new CssLayout();
        cssLayout.addStyleName("customer-targeting-search-input");

        CssLayout idWrapper = new CssLayout();
        idWrapper.setStyleName("input-id");
        final DCATextField inputField = new DCATextField(SannsynIcons.SEARCH, false);
        inputField.setInputPrompt("Items ID, title or similar");
        idWrapper.addComponent(inputField);

        inputField.installEnterKeyHandler(new OnEnterKeyHandler() {
            @Override
            public void onEnterKeyPressed() {
                String value = inputField.getValue();
                action.accept(value);
            }
        });

        CssLayout buttonWrapper = new CssLayout();
        buttonWrapper.setStyleName("input-button");
        Button searchButton = new Button("SEARCH");
        searchButton.setWidth(100, PERCENTAGE);

        searchButton.addClickListener(event -> action.accept(inputField.getValue()));

        searchButton.addStyleName("btn-primary-style");
        buttonWrapper.addComponent(searchButton);

        cssLayout.addComponent(idWrapper);
        cssLayout.addComponent(buttonWrapper);

        return cssLayout;
    }
}

