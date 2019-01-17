package com.sannsyn.dca.vaadin.widgets.shophelper;

import com.google.gson.JsonObject;
import com.sannsyn.dca.metadata.DCAItem;
import com.sannsyn.dca.metadata.DCAMetadataResponse;
import com.sannsyn.dca.metadata.DCAMetadataServiceClientImpl;
import com.sannsyn.dca.model.config.DCAWidget;
import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.service.DCARecommenderService;
import com.sannsyn.dca.service.ark.DCAArkSolrClient;
import com.sannsyn.dca.vaadin.component.custom.container.DCATouchLayout;
import com.sannsyn.dca.vaadin.component.custom.field.DCATextField;
import com.sannsyn.dca.vaadin.helper.OnEnterKeyHandler;
import com.sannsyn.dca.vaadin.icons.SannsynIcons;
import com.sannsyn.dca.vaadin.servlet.DCAUserPreference;
import com.sannsyn.dca.vaadin.servlet.DCAUtils;
import com.sannsyn.dca.vaadin.ui.DCAUI;
import com.sannsyn.dca.vaadin.ui.DCAUiHelper;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.sannsyn.dca.vaadin.ui.DCAUiHelper.createSpacer;
import static com.sannsyn.dca.vaadin.ui.DCAUiHelper.wrapWithCssLayout;
import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;
import static com.vaadin.server.Sizeable.Unit.PIXELS;
import static java.lang.Character.isDigit;

/**
 * The ShopHelper widget
 * <p>
 * Created by jobaer on 5/17/17.
 */
public class DCAShopHelperWidget extends CustomComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAShopHelperWidget.class);
    private static final String SHOPPERHELPER_BY_CUSTOMER_AND_ITEMS = "ShopperhelperByCustomerAndItems";
    private static final String RECOMMENDER_NAMES = "recommenderNames";
    private static final String CUSTOMER_ITEM = "customerItem";
    private static final String SHOPPERHELPER_BY_ITEMS = "ShopperhelperByItems";
    private static final String ITEM = "item";

    private final DCAWidget config;
    private final UI currentUi;
    private CssLayout searchResultLayout;
    private Set<DCAItem> activeItems = new LinkedHashSet<>();
    private Set<DCAItem> wishListItems = new LinkedHashSet<>();
    private CssLayout activeItemsLayout;
    private CssLayout searchResult = new CssLayout();
    private CssLayout customerNumberlayout = new CssLayout();
    private CssLayout recommendedItemsLayout = new CssLayout();
    private CssLayout wishListItemsLayout = new CssLayout();
    private String activeCustomer = null;
    private DCAUser loggedInUser;
    private DCAArkSolrClient arkSolrClient;

    public DCAShopHelperWidget(DCAWidget widgetConfig, UI current) {
        this.config = widgetConfig;
        this.currentUi = current;

        loggedInUser = DCAUserPreference.getLoggedInUser();
        initializeSolrClient();

        CssLayout root = new CssLayout();
        populateWidget(root);
        setCompositionRoot(root);
    }

    private void initializeSolrClient() {
        JsonObject jsonConfig = config.getJsonConfig();
        if (jsonConfig != null && jsonConfig.has("solrConfig")) {
            JsonObject solrConfig = jsonConfig.get("solrConfig").getAsJsonObject();
            arkSolrClient = new DCAArkSolrClient(solrConfig);
        } else {
            throw new IllegalStateException("No solrConfig found. Unable to create solr client");
        }
    }

    private void populateWidget(CssLayout root) {
        root.addStyleName("shop-helper-widget");

        setupActiveArea(root);
        addCustomerNumberLayout(root);
        setupInputArea(root);
        setupResultArea(root);
        setupSelectionArea(root);
        addLogoutLink(root);
    }

    private void addLogoutLink(CssLayout root) {
        CssLayout layout = new CssLayout();
        layout.addStyleName("shop-helper-logout-wrapper");
        Link logout = new Link();
        logout.setId("shop-helper-logout");
        logout.setCaption("Logout");

        layout.addComponent(logout);
        layout.addLayoutClickListener(event -> logout());

        root.addComponent(layout);
    }

    private void setupActiveArea(CssLayout root) {
        CssLayout layout = new CssLayout();
        layout.setWidth(100, PERCENTAGE);
        layout.addStyleName("shop-helper-active-layout");
        Label title = new Label("Active items");
        CssLayout titleWrap = wrapWithCssLayout(title, "shop-helper-active-layout-title", 100);
        layout.addComponent(titleWrap);

        activeItemsLayout = new CssLayout();
        activeItemsLayout.addStyleName("shope-helper-active-items");
        activeItemsLayout.setWidth(100, PERCENTAGE);
        Label label = new Label("No active items");
        activeItemsLayout.addComponent(label);

        layout.addComponent(activeItemsLayout);
        root.addComponent(layout);
    }

    private void setupInputArea(CssLayout root) {
        CssLayout layout = new CssLayout();
        layout.setWidth(100, PERCENTAGE);
        layout.addStyleName("shop-helper-input-layout");


        DCATextField input = new DCATextField("shop-helper-input-field", false);
        input.setWidth(94, PERCENTAGE);
        CssLayout wrapper = wrapWithCssLayout(input, "shop-helper-input-field-wrapper", 100);

        Label xLabel = new Label();
        xLabel.addStyleName("shop-helper-input-field-cross");
        xLabel.setIcon(SannsynIcons.CLOSE);
        CssLayout crossWrapper = wrapWithCssLayout(xLabel, "shop-helper-input-field-cross-wrapper");

        crossWrapper.setWidth(6, PERCENTAGE);
        crossWrapper.addLayoutClickListener(event -> {
            input.clear();
            input.focus();
        });
        wrapper.addComponent(crossWrapper);

        layout.addComponent(wrapper);

        OnEnterKeyHandler enterHandler = new OnEnterKeyHandler() {
            @Override
            public void onEnterKeyPressed() {
                String value = input.getValue();
                doSearch(value);
            }
        };
        input.installEnterKeyHandler(enterHandler);

        searchResultLayout = createSearchResultLayout();
        searchResultLayout.setVisible(false);
        layout.addComponent(searchResultLayout);

        root.addComponent(layout);
    }

    private void addCustomerNumberLayout(CssLayout root) {
        customerNumberlayout.setSizeFull();
        customerNumberlayout.addStyleName("shop-helper-customer-number-wrapper");

        root.addComponent(customerNumberlayout);
    }

    private Layout createCustomerNumber(String customerId) {
        Label iconLabel = new Label("");
        iconLabel.setWidthUndefined();
        iconLabel.setIcon(FontAwesome.USER);

        DCATouchLayout wrapper = new DCATouchLayout("active-customer-id" + customerId);

        CssLayout layout = wrapWithCssLayout(iconLabel, "shop-helper-customer-number", 100);

        Label label = new Label("Customer Id: " + customerId);
        label.setWidthUndefined();

        Label spacer = new Label("");
        spacer.setWidth(5, PIXELS);
        layout.addComponent(spacer);
        layout.addComponent(label);

        wrapper.addSwipeLeftListener(this::clearActiveCustomer);

        wrapper.addComponent(layout);
        return wrapper;
    }

    private void clearActiveCustomer() {
        customerNumberlayout.removeAllComponents();
        activeCustomer = null;
        updateRecommendations();
    }

    private CssLayout createSearchResultLayout() {
        searchResult.addStyleName("shop-helper-search-result");
        return searchResult;
    }

    private CssLayout createSearchResultMessage(String message) {
        CssLayout layout = new CssLayout();
        layout.addStyleName("shop-helper-search-result-item");
        layout.setSizeFull();

        CssLayout info = new CssLayout();
        info.setWidth(100, PERCENTAGE);
        Label c = new Label(message);
        info.addComponent(c);
        layout.addComponent(info);

        layout.addLayoutClickListener(event -> searchResultLayout.setVisible(false));

        return layout;
    }


    private CssLayout createSearchResultItem(DCAItem item) {
        CssLayout layout = new CssLayout();
        layout.addStyleName("shop-helper-search-result-item");
        layout.setWidth(100, PERCENTAGE);

        CssLayout info = new CssLayout();
        info.setWidth(100, PERCENTAGE);
        Label c = new Label(item.getTitle());
        info.addComponent(c);
        Label authorLabel = createAuthorLabel(item);
        info.addComponent(authorLabel);

        layout.addComponent(info);

        layout.addLayoutClickListener(event -> {
            addItemToActiveArea(item);
            searchResultLayout.setVisible(false);
        });

        return layout;
    }

    private void addItemToActiveArea(DCAItem string) {
        activeItems.add(string);
        repaintActiveItems();
        updateRecommendations();
    }

    private void repaintActiveItems() {
        activeItemsLayout.removeAllComponents();
        for (DCAItem activeItem : activeItems) {
            CssLayout active = createActiveItem(activeItem);
            activeItemsLayout.addComponent(active);
        }
    }

    private CssLayout createActiveItem(DCAItem activeItem) {
        DCATouchLayout layout = new DCATouchLayout("activeItem" + activeItem.getId());
        layout.addStyleName("shop-helper-active-item");
        layout.setWidth(100, PERCENTAGE);

        CssLayout info = new CssLayout();
        info.setWidth(100, PERCENTAGE);
        Label c = new Label(activeItem.getTitle());
        info.addComponent(c);
        Label authorLabel = createAuthorLabel(activeItem);
        info.addComponent(authorLabel);

        layout.addComponent(info);

        layout.addSwipeLeftListener(() -> {
            activeItems.remove(activeItem);
            repaintActiveItems();
            updateRecommendations();
        });

        return layout;
    }

    private void doSearch(String value) {
        if (isCustomerId(value)) {
            searchCustomerAndUpdateUi(value);
        } else {
            searchItemsAndUpdateUi(value);
        }
    }

    private void updateRecommendations() {
        logger.debug("Updating recommendations .... ");
        clearExistingRecommendations();
        requestRecommendationUpdate();
    }

    private void requestRecommendationUpdate() {
        if (activeCustomer != null) {
            List<String> activeIds = getActiveIds(activeItems);
            Observable<DCAItem> recommendations = getCustomerItemRecommendations(activeCustomer, activeIds);
            recommendations.subscribe(result -> {
                Layout itemView = createRecommendedItem(result);
                currentUi.access(() -> recommendedItemsLayout.addComponent(itemView));
            }, this::handleError);
            Observable<Integer> resultCount = recommendations.count();
            resultCount.subscribe(count -> {
                if (count <= 0) {
                    currentUi.access(() -> recommendedItemsLayout.addComponent(new Label("No recommendations found.")));
                }
            }, e -> logger.error(e.getMessage()));
        } else {
            List<String> activeIds = getActiveIds(activeItems);
            Observable<DCAItem> recommendations = getItemRecommendations(activeIds);
            recommendations.subscribe(result -> {
                Layout itemView = createRecommendedItem(result);
                currentUi.access(() -> recommendedItemsLayout.addComponent(itemView));
            }, this::handleError);
            Observable<Integer> resultCount = recommendations.count();
            resultCount.subscribe(count -> {
                if (count <= 0) {
                    currentUi.access(() -> recommendedItemsLayout.addComponent(new Label("No recommendations found.")));
                }
            }, e -> logger.error(e.getMessage()));
        }
    }

    private void handleError(Throwable e) {
        logger.error("Error occurred while fetching recommendations. ", e);
        CssLayout nameLabel = createSearchResultMessage("Error occurred while fetching recommendations.");
        currentUi.access(() -> {
            searchResult.addComponent(nameLabel);
            // then update the result area
            if (searchResultLayout != null) {
                searchResultLayout.setVisible(true);
            }
        });
    }

    private List<String> getActiveIds(Set<DCAItem> activeItems) {
        return activeItems.stream().map(DCAItem::getId).collect(Collectors.toList());
    }

    private void clearExistingRecommendations() {
        recommendedItemsLayout.removeAllComponents();
    }

    private Observable<DCAItem> getItemRecommendations(List<String> idList) {
        DCARecommenderService recommenderService = new DCARecommenderService(loggedInUser);
        String recommenderName = getItemRecommenderName();
        logger.debug("Item recommender name: " + recommenderName);
        Observable<List<DCAItem>> resultObservable =
            recommenderService.getShopHelperRecommendations(recommenderName, idList);

        return transformResult(resultObservable);
    }

    private Observable<DCAItem> transformResult(Observable<List<DCAItem>> resultObservable) {
        DCAMetadataServiceClientImpl metadataServiceClient = new DCAMetadataServiceClientImpl();

        return resultObservable
            .flatMap(items -> {
                List<String> listOfIds = items.stream().map(DCAItem::getId).collect(Collectors.toList());
                return metadataServiceClient.getAllMetadataItems(listOfIds);
            })
            .filter(dcaMetadataResponse -> "success".equals(dcaMetadataResponse.getStatus()))
            .map(DCAItem::fromMetadata);
    }

    private String getItemRecommenderName() {
        String defaultName = SHOPPERHELPER_BY_ITEMS;
        if (config == null) return defaultName;
        JsonObject jsonConfig = config.getJsonConfig();
        if (jsonConfig != null && jsonConfig.has(RECOMMENDER_NAMES)) {
            JsonObject recommenderNames = jsonConfig.get(RECOMMENDER_NAMES).getAsJsonObject();
            return recommenderNames.get(ITEM).getAsString();
        } else {
            return defaultName;
        }
    }

    private String getCustomerAndItemRecommenderName() {
        String defaultName = SHOPPERHELPER_BY_CUSTOMER_AND_ITEMS;
        if (config == null) return defaultName;
        JsonObject jsonConfig = config.getJsonConfig();
        if (jsonConfig != null && jsonConfig.has(RECOMMENDER_NAMES)) {
            JsonObject recommenderNames = jsonConfig.get(RECOMMENDER_NAMES).getAsJsonObject();
            return recommenderNames.get(CUSTOMER_ITEM).getAsString();
        } else {
            return defaultName;
        }
    }

    private Observable<DCAItem> getCustomerItemRecommendations(String customerId, List<String> activeIds) {
        DCARecommenderService recommenderService = new DCARecommenderService(loggedInUser);

        List<String> idList = new ArrayList<>();
        idList.add(customerId);
        idList.addAll(activeIds);

        String recommenderName = getCustomerAndItemRecommenderName();
        logger.debug("Customer and item recommender name: " + recommenderName);
        Observable<List<DCAItem>> resultObservable =
            recommenderService.getShopHelperRecommendations(recommenderName, idList);
        return transformResult(resultObservable);
    }

    private Boolean startsWithNumber(String str) {
        if (str != null) str = str.trim();
        return !StringUtils.isBlank(str) && isDigit(str.charAt(0));
    }

    private Boolean isCustomerId(String value) {
        return startsWithNumber(value) && value.length() < 13;
    }

    private void searchCustomerAndUpdateUi(String value) {
        if (isCustomerId(value)) {
            activeCustomer = value;
            customerNumberlayout.removeAllComponents();
            Layout customerNumber = createCustomerNumber(value);
            customerNumberlayout.addComponent(customerNumber);
            updateRecommendations();
        }
    }

    private void searchItemsAndUpdateUi(String value) {
        searchResult.removeAllComponents();
        if (StringUtils.isBlank(value)) return;

        // do a solr search in ark

        Observable<DCAMetadataResponse> result = arkSolrClient.searchProducts(value);

        Observable<List<DCAMetadataResponse>> resultList = result.toList();
        resultList.subscribe(resp -> {
            if (resp.isEmpty()) {
                CssLayout nameLabel = createSearchResultMessage("No result found for the query");
                currentUi.access(() -> {
                    searchResult.addComponent(nameLabel);
                    // then update the result area
                    if (searchResultLayout != null) {
                        searchResultLayout.setVisible(true);
                    }
                });
            } else {
                List<CssLayout> items = new ArrayList<>();
                for (DCAMetadataResponse dcaMetadataResponse : resp) {
                    logger.debug("resp.getAsJsonObject() = " + dcaMetadataResponse.getAsJsonObject());
                    DCAItem dcaItem = DCAItem.fromMetadata(dcaMetadataResponse);
                    CssLayout nameLabel = createSearchResultItem(dcaItem);
                    items.add(nameLabel);

                }
                currentUi.access(() -> {
                    for (CssLayout item : items) {
                        searchResult.addComponent(item);
                    }
                    // then update the result area
                    if (searchResultLayout != null) {
                        searchResultLayout.setVisible(true);
                    }
                });
            }
        }, e -> logger.error("Error occurred while metadata search.", e));
    }

    private void setupResultArea(CssLayout root) {
        CssLayout layout = new CssLayout();
        layout.setWidth(100, PERCENTAGE);
        layout.addStyleName("shop-helper-result-layout");

        Label title = new Label("Recommended items");
        CssLayout titleWrap = wrapWithCssLayout(title, "shop-helper-active-layout-title", 100);
        layout.addComponent(titleWrap);

        recommendedItemsLayout.addStyleName("shop-helper-recommended-items-layout");
        recommendedItemsLayout.setWidth(100, PERCENTAGE);
        layout.addComponent(recommendedItemsLayout);
        recommendedItemsLayout.addComponent(new Label("No recommended items"));

        root.addComponent(layout);
    }

    private void setupSelectionArea(CssLayout root) {
        CssLayout layout = new CssLayout();
        layout.setWidth(100, PERCENTAGE);
        layout.addStyleName("shop-helper-selection-layout");

        Label title = new Label("Wish list");
        CssLayout titleWrap = wrapWithCssLayout(title, "shop-helper-active-layout-title", 100);
        layout.addComponent(titleWrap);

        wishListItemsLayout.addStyleName("shop-helper-wishlist-items-layout");
        wishListItemsLayout.setWidth(100, PERCENTAGE);
        layout.addComponent(wishListItemsLayout);
        updateWishListItems();

        root.addComponent(layout);
    }

    private void updateWishListItems() {
        wishListItemsLayout.removeAllComponents();
        if (wishListItems.isEmpty()) {
            wishListItemsLayout.addComponent(new Label("No items yet"));
            return;
        }

        for (DCAItem wishlistItem : wishListItems) {
            Layout itemView = createWishItem(wishlistItem);
            wishListItemsLayout.addComponent(itemView);
        }
    }

    private Layout createRecommendedItem(DCAItem item) {
        CssLayout layout = createItemView(item);
        DCATouchLayout touchLayout = new DCATouchLayout("recommendedItem-" + item.getId());
        touchLayout.setWidth(100, PERCENTAGE);

        touchLayout.addSwipeRightListener(() -> addItemToActiveArea(item));

        touchLayout.addDoubletapListener(() -> {
            recommendedItemsLayout.removeComponent(touchLayout);
            wishListItems.add(item);
            updateWishListItems();
        });

        touchLayout.addComponent(layout);

        return touchLayout;
    }

    private Layout createWishItem(DCAItem item) {
        CssLayout itemView = createItemView(item);
        DCATouchLayout touchLayout = new DCATouchLayout("wishItem-" + item.getId());
        touchLayout.setWidth(100, PERCENTAGE);
        touchLayout.addComponent(itemView);
        touchLayout.addSwipeLeftListener(() -> {
            wishListItems.remove(item);
            updateWishListItems();
        });
        return touchLayout;
    }

    private CssLayout createItemView(DCAItem item) {
        CssLayout layout = new CssLayout();
        layout.addStyleName("shop-helper-item-wrapper");
        Component imageLabel = DCAUiHelper.createImageLabel(item.getThumbnail(), 15, PERCENTAGE);

        CssLayout infoLayout = new CssLayout();
        infoLayout.setWidth(80, PERCENTAGE);
        Label titleLabel = new Label(item.getTitle());
        infoLayout.addComponent(titleLabel);
        Label authorLabel = createAuthorLabel(item);
        infoLayout.addComponent(authorLabel);

        Label spacer = createSpacer(3);

        layout.addComponent(imageLabel);
        layout.addComponent(spacer);
        layout.addComponent(infoLayout);

        return layout;
    }

    private Label createAuthorLabel(DCAItem item) {
        Label author = new Label();
        if (StringUtils.isNotBlank(item.getAuthor())) author.setValue(item.getAuthor());
        return author;
    }

    private void logout() {
        DCAUtils.removeTargetService();
        DCAUserPreference.removeLoggedInUser();
        UI.getCurrent().getNavigator().navigateTo(DCAUI.START_VIEW);
    }
}
