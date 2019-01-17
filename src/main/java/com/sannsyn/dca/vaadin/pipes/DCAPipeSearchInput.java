package com.sannsyn.dca.vaadin.pipes;

import com.sannsyn.dca.vaadin.component.custom.field.DCAComboBox;
import com.sannsyn.dca.vaadin.component.custom.field.DCATextField;
import com.sannsyn.dca.vaadin.helper.OnEnterKeyHandler;
import com.sannsyn.dca.vaadin.icons.SannsynIcons;
import com.vaadin.ui.*;

import static com.sannsyn.dca.vaadin.ui.DCAUiHelper.createSpacer;
import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;

/**
 * Form component for searching pipes.
 * <p>
 * Created by jobaer on 6/17/16.
 */
class DCAPipeSearchInput extends CustomComponent {
    private final DCAPipeSearchInputHandler handler;
    private DCATextField searchInput;
    private DCAComboBox typeComboBox;

    DCAPipeSearchInput(DCAPipeSearchInputHandler handler) {
        this.handler = handler;
        init();
    }

    private void init() {
        CssLayout outer = new CssLayout();
        outer.setStyleName("create-recommender-component-search-input");
        outer.setWidth(100, PERCENTAGE);

        CssLayout layout = new CssLayout();
        layout.setWidth(100, PERCENTAGE);

        searchInput = new DCATextField(SannsynIcons.SEARCH, false);
        searchInput.setInputPrompt("Search for components or component classes");
        searchInput.setWidth(50, PERCENTAGE);
        layout.addComponent(searchInput);

        layout.addComponent(createSpacer(3));

        typeComboBox = new DCAComboBox();
        typeComboBox.setNullSelectionAllowed(true);
        typeComboBox.setWidth(22, PERCENTAGE);
        typeComboBox.addItem("Transform");
        typeComboBox.addItem("Filter");
        typeComboBox.addItem("Producer");
        typeComboBox.addItem("Join");
        layout.addComponent(typeComboBox);

        layout.addComponent(createSpacer(3));

        Button searchButton = new Button("Search");
        searchButton.addClickListener(event -> {
            doSearch();
        });
        searchButton.addStyleName("btn-primary-style");
        searchButton.setWidth(22, PERCENTAGE);
        layout.addComponent(searchButton);
        outer.addComponent(layout);

        searchInput.installEnterKeyHandler(new OnEnterKeyHandler() {
            @Override
            public void onEnterKeyPressed() {
                doSearch();
            }
        });

        Label c = new Label();
        c.addStyleName("pipes-search-input-border");
        outer.addComponent(c);

        setCompositionRoot(outer);
    }

    public void refreshSearchResult() {
        doSearch();
    }

    private void doSearch() {
        String query = searchInput.getValue();
        String type = "";
        Object value = typeComboBox.getValue();
        if (value != null) {
            type = value.toString();
        }

        if("Join".equalsIgnoreCase(type)) {
            type = "pipe";
        }

        handler.search(query, type);
    }
}

interface DCAPipeSearchInputHandler {
    void search(String query, String type);
}
