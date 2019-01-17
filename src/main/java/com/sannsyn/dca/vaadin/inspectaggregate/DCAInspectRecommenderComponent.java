package com.sannsyn.dca.vaadin.inspectaggregate;

import com.google.gwt.thirdparty.guava.common.collect.EvictingQueue;
import com.sannsyn.dca.metadata.DCAItem;
import com.sannsyn.dca.metadata.DCAMetadataResponse;
import com.sannsyn.dca.metadata.DCAMetadataServiceClient;
import com.sannsyn.dca.metadata.DCAMetadataServiceClientImpl;
import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.vaadin.component.custom.container.ItemContainer;
import com.sannsyn.dca.vaadin.component.custom.container.ItemPainter;
import com.sannsyn.dca.vaadin.component.custom.field.DCAComboBox;
import com.sannsyn.dca.vaadin.component.custom.field.DCASuggestionTextField;
import com.sannsyn.dca.vaadin.icons.SannsynIcons;
import com.sannsyn.dca.vaadin.validators.NonEmptyValidator;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Sizeable;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import static com.vaadin.server.Sizeable.Unit.PIXELS;

/**
 * Main ui class for inspecting recommenders. Contains the search fields and also the result list.
 */
class DCAInspectRecommenderComponent {
    private final UI mainUi;
    private DCAMetadataServiceClient metadataServiceClient = new DCAMetadataServiceClientImpl();
    private ItemContainer<DCAItem> bookContainer = new ItemContainer<>(false);
    private DCARecommenderContainer recsContainer;
    private DCASuggestionTextField id = new DCASuggestionTextField(SannsynIcons.SEARCH);
    private Button search = new Button("");
    private DCAComboBox items = new DCAComboBox();
    private static final Logger logger = LoggerFactory.getLogger(DCAInspectRecommenderComponent.class);

    DCAInspectRecommenderComponent(UI ui, DCAUser loggedInUser) {
        this.mainUi = ui;
        this.recsContainer = new DCARecommenderContainer(loggedInUser);

        recsContainer.setUi(this.mainUi);
        recsContainer.setHandler(new DCARecommenderItemsHandler() {
            @Override
            public String getId() {
                return id.getValue();
            }

            @Override
            public String getCount() {
                return items.getValue() == null ? "10" : items.getValue().toString();
            }
        });
    }


    Component createUI() {
        Panel panel = new Panel();
        panel.setSizeFull();
        Layout cssLayout = new CssLayout();
        cssLayout.addStyleName("inspect-recommender-container");
        Layout searchInput = getSearchInput();
        cssLayout.addComponent(searchInput);
        cssLayout.addComponent(getResult());
        panel.setContent(cssLayout);
        return panel;
    }

    private Layout getSearchInput() {
        Layout wrapper = new CssLayout();
        Layout searchInput = createSearchInput();
        wrapper.addComponent(searchInput);
        Layout mainItemUi = createMainItemUi();
        wrapper.addComponent(mainItemUi);
        return wrapper;
    }

    private Layout getResult() {
        Layout wrapper = new CssLayout();
        wrapper.setWidth(100, Sizeable.Unit.PERCENTAGE);
        wrapper.addComponent(recsContainer.getComponent());

        return wrapper;
    }

    private Layout createMainItemUi() {
        ItemPainter<DCAItem> detailsPainter = new ItemPainterDetailsView(84, 16) {
            @Override
            protected float getWidthInPercentage() {
                return 100;
            }
        };

        bookContainer.setTitle("");
        bookContainer.registerPainter("default", detailsPainter);
        Layout cssLayout = new CssLayout();
        cssLayout.setStyleName("inspect-rec-main-item");
        cssLayout.addComponent(bookContainer.getComponent());
        return cssLayout;
    }

    private Layout createSearchInput() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setStyleName("inspect-rec-input");
        layout.setSpacing(true);

        prepareIdField(layout);

        items.setWidth(130, PIXELS);
        items.addItems("10", "25", "50", "100", "1000", "Unlimited");
        items.select("10");
        items.setNullSelectionAllowed(false);
        items.setInputPrompt("No. of items");
        layout.addComponent(items);

        Object storedNumOfItems = VaadinSession.getCurrent().getAttribute("ir-numOfItems");
        if (storedNumOfItems != null) {
            items.select(storedNumOfItems);
        }

        CssLayout group = new CssLayout();
        group.addStyleName("v-component-group");
        layout.addComponent(group);

        Button listButton = new Button("");
        listButton.setDescription("List view");
        listButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        listButton.addStyleName("flat-button");
        listButton.setIcon(FontAwesome.NAVICON);
        group.addComponent(listButton);

        Button imageButton = new Button("");
        imageButton.setDescription("Detail view");
        imageButton.setIcon(FontAwesome.IMAGE);
        imageButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        imageButton.addStyleName("flat-button");

        group.addComponent(imageButton);

        String activeStyleName = "btn-active-style";
        // By default the image style is active
        imageButton.addStyleName(activeStyleName);
        listButton.addClickListener(event -> {
            recsContainer.switchAllViews("listView");
            imageButton.removeStyleName(activeStyleName);
            listButton.addStyleName(activeStyleName);
        });
        imageButton.addClickListener(event -> {
            recsContainer.switchAllViews("default");
            listButton.removeStyleName(activeStyleName);
            imageButton.addStyleName(activeStyleName);
        });

        search.setIcon(SannsynIcons.SEARCH);
        search.addStyleName("btn-primary-style");
        search.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        search.addClickListener(event -> {
            search.setEnabled(false);
            bookContainer.clear();
            prepareQueryAndGetResponse(id, items);
        });
        layout.addComponent(search);

        return layout;
    }

    private void prepareIdField(HorizontalLayout layout) {
        id.setWidth(420, PIXELS);
        id.setInputPrompt("Find customer or item");

        Object storedIds = VaadinSession.getCurrent().getAttribute("ir-pastItemIds");
        if (storedIds != null && storedIds instanceof EvictingQueue) {
            EvictingQueue<String> queue = (EvictingQueue) storedIds;
            for (String s : queue) {
                id.addItem(s);
            }
        }
        // Add non-empty validator
        id.addValidator(new NonEmptyValidator("* Id"));


        layout.addComponent(id);
    }

    private void prepareQueryAndGetResponse(DCASuggestionTextField id, DCAComboBox items) {
        id.validate();
        String itemId = id.getValue();
        if (StringUtils.isEmpty(itemId)) {
            search.setEnabled(true);
            return;
        }

        String firstId = itemId;
        if (itemId.contains(";")) {
            String[] split = StringUtils.split(itemId, ';');
            firstId = split[0];
        }

        String numOfItems = items.getValue().toString();
        preserveState(itemId, numOfItems);

        updateItemDetails(firstId);

        recsContainer.preserveState();
        recsContainer.searchAll();
    }

    private void preserveState(String itemId, String numOfItems) {
        id.addItem(itemId);
        EvictingQueue<String> queue;
        Object storedIds = VaadinSession.getCurrent().getAttribute("ir-pastItemIds");
        if (storedIds != null && storedIds instanceof EvictingQueue) {
            queue = (EvictingQueue) storedIds;
        } else {
            queue = EvictingQueue.create(10);
        }
        queue.add(itemId);

        VaadinSession.getCurrent().setAttribute("ir-pastItemIds", queue);
        VaadinSession.getCurrent().setAttribute("ir-numOfItems", numOfItems);
    }

    private void updateItemDetails(String itemId) {
        DCAItem item = new DCAItem();
        item.setId(itemId);
        bookContainer.addItem(item);

        requestForMetadataUpdate(itemId, bookContainer);
    }

    private void requestForMetadataUpdate(String itemId, ItemContainer<DCAItem> bookContainer) {
        Observable<DCAMetadataResponse> metadataItem = metadataServiceClient.getMetadataItem(itemId);
        metadataItem.subscribe(resp -> {
            DCAItem item = createBookFromMetadataResponse(resp);
            mainUi.access(() -> bookContainer.updateItem(item));
            search.setEnabled(true);
        }, e -> {
            logger.error("Error while updating inspect recommender main item ", e);
            search.setEnabled(true);
        });
    }

    private DCAItem createBookFromMetadataResponse(DCAMetadataResponse resp) {
        return DCAItem.fromMetadata(resp);
    }
}
