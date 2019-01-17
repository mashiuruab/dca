package com.sannsyn.dca.vaadin.pipes;

import com.sannsyn.dca.vaadin.component.custom.container.ItemContainer;
import com.sannsyn.dca.vaadin.component.custom.container.ItemPainter;
import com.sannsyn.dca.vaadin.widgets.controller.overview.model.aggregates.DCAPipe;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;

import java.util.List;

import static com.sannsyn.dca.vaadin.ui.DCAUiHelper.createSpacer;
import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;

/**
 * Search result component for the pipes
 * <p>
 * Created by jobaer on 6/17/16.
 */
class DCAPipeSearchResult extends CustomComponent {
    private final DCAPipeSearchResultHandler handler;
    private ItemContainer<DCAPipe> resultList;

    DCAPipeSearchResult(DCAPipeSearchResultHandler handler) {
        this.handler = handler;
        init();
    }

    private void init() {
        CssLayout layout = new CssLayout();
        layout.setStyleName("create-recommender-component-search-result");
        layout.setWidth(100, PERCENTAGE);

        resultList = new ItemContainer<>(false);
        ItemPainter<DCAPipe> simplePainter = new DCAPipeItemPainter(handler::onClick);
        resultList.registerPainter("default", simplePainter);

        Component columnHeaders = makeColumnHeaders();
        layout.addComponent(columnHeaders);
        CssLayout resultLayout = new CssLayout();
        resultLayout.addStyleName("pipes-search-result-resultList");
        resultLayout.setWidth(100, PERCENTAGE);
        resultLayout.addComponent(resultList.getComponent());
        layout.addComponent(resultLayout);

        setCompositionRoot(layout);
    }

    private Component makeColumnHeaders() {
        CssLayout headerWrapper = new CssLayout();
        headerWrapper.setWidth(100, PERCENTAGE);
        headerWrapper.addStyleName("recommender-component-search-result-colheader-wrapper");

        Label spacer = createSpacer(7);
        Label name = new Label("Name");
        name.setStyleName("recommender-component-search-result-colheader");
        name.setWidth(25, PERCENTAGE);

        Label type = new Label("Type");
        type.setStyleName("recommender-component-search-result-colheader");
        type.setWidth(12, PERCENTAGE);

        Label description = new Label("Description");
        description.setStyleName("recommender-component-search-result-colheader");
        description.setWidth(40, PERCENTAGE);

        headerWrapper.addComponent(spacer);
        headerWrapper.addComponent(name);
        headerWrapper.addComponent(type);
        headerWrapper.addComponent(description);

        return headerWrapper;
    }

    void clearResult() {
        resultList.clearForce();
    }

    void updateSearchResult(List<DCAPipe> result) {
        if (result.isEmpty()) resultList.showErrorMessage("No result found...");
        result.forEach(resultList::addItem);
    }
}

interface DCAPipeSearchResultHandler {
    void onClick(DCAPipe item);
}
