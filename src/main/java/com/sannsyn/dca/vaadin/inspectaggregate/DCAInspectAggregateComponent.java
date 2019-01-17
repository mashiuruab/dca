package com.sannsyn.dca.vaadin.inspectaggregate;

import com.sannsyn.dca.metadata.DCAItem;
import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.service.AggregateQuery;
import com.sannsyn.dca.vaadin.component.custom.container.ItemContainer;
import com.sannsyn.dca.vaadin.component.custom.container.ItemPainter;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main ui class to inspect aggregates. It contains the search input fields and also the result list.
 */
class DCAInspectAggregateComponent {
    private final UI mainUi;
    private ItemContainer<DCAItem> bookContainer = new ItemContainer<>(false);
    private ItemContainer<DCAItem> siblingsContainer = new ItemContainer<>(true);
    private DCAInspectAggregateHelper helper;
    private DCAInspectAggregateSearchInput inspectAggregateSearchInput;
    private static final Logger logger = LoggerFactory.getLogger(DCAInspectAggregateComponent.class);

    private DCAUser loggedInUser;

    DCAInspectAggregateComponent(UI ui, DCAUser loggedInUser) {
        this.mainUi = ui;
        this.loggedInUser = loggedInUser;

        createSearchInput(ui);
        createHelper();
    }

    private void createSearchInput(UI ui) {
        inspectAggregateSearchInput = new DCAInspectAggregateSearchInput(ui, loggedInUser);
        DCAInspectAggregateSearchInputHandler searchInputHandler = new DCAInspectAggregateSearchInputHandler() {
            @Override
            public void search(AggregateQuery query) {
                doSearch(query);
            }

            @Override
            public void switchToView(String viewName) {
                siblingsContainer.switchToView(viewName);
            }
        };
        inspectAggregateSearchInput.setHandler(searchInputHandler);
    }

    private void createHelper() {
        helper = new DCAInspectAggregateHelper(new DCAInspectAggregateUiHandler() {
            @Override
            public void error(String message) {
                logger.error(message);
                showErrorMessage(message);
            }

            @Override
            public void addItemToContainer(DCAItem item, ItemContainer<DCAItem> itemContainer) {
                mainUi.access(() -> {
                    itemContainer.addItem(item);
                    inspectAggregateSearchInput.enableSearch();
                });
            }

            @Override
            public void updateItemInContainer(DCAItem item, ItemContainer<DCAItem> itemContainer) {
                mainUi.access(() -> itemContainer.updateItem(item));
            }
        });
    }

    Component createUI() {
        Panel panel = new Panel();
        panel.setSizeFull();

        Layout cssLayout = new CssLayout();
        cssLayout.setStyleName("inspect-aggregate-container-left");
        Layout first = inspectAggregateSearchInput.getComponent();
        cssLayout.addComponent(first);
        cssLayout.addComponent(getResultComponent());

        panel.setContent(cssLayout);
        return panel;
    }

    private Layout getResultComponent() {
        Layout wrapper = new CssLayout();
        wrapper.setWidth(100, Sizeable.Unit.PERCENTAGE);

        ItemPainter<DCAItem> detailsPainter = new ItemPainterDetailsView(82, 18);
        ItemPainter<DCAItem> listPainter = new ItemPainterListView();

        bookContainer.registerPainter("default", detailsPainter);

        Layout siblings = new CssLayout();
        siblings.addStyleName("item-container-siblings");
        siblings.setWidth(100, Sizeable.Unit.PERCENTAGE);

        siblingsContainer.setTitle("");
        siblingsContainer.registerPainter("default", detailsPainter);
        siblingsContainer.registerPainter("listView", listPainter);
        siblings.addComponent(siblingsContainer.getComponent());

        wrapper.addComponent(bookContainer.getComponent());
        wrapper.addComponent(siblings);

        return wrapper;
    }

    private void doSearch(AggregateQuery query) {
        bookContainer.clear();
        siblingsContainer.clear();
        updateItemDetails(AggregateQuery.copyOf(query));
        helper.fetchResultAndUpdateUi(query, siblingsContainer);
    }

    private void showErrorMessage(String message) {
        mainUi.access(() -> {
            siblingsContainer.showErrorMessage(message);
            inspectAggregateSearchInput.enableSearch();
        });
    }

    private void updateItemDetails(AggregateQuery query) {
        DCAItem item = new DCAItem();
        item.setId(query.getId());
        bookContainer.addItem(item);
        siblingsContainer.setTitle("Siblings");
        helper.requestMetadataAndAggregateData(item, bookContainer, query.getName());
    }
}
