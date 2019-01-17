package com.sannsyn.dca.vaadin.customertargeting;

import com.sannsyn.dca.metadata.DCAItem;
import com.sannsyn.dca.model.config.DCAWidget;
import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.service.DCACustomerTargetingService;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.UI;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.sannsyn.dca.vaadin.ui.DCAUiHelper.addSeparatorWithPadding;
import static com.sannsyn.dca.vaadin.ui.DCAUiHelper.createCssLayout;

/**
 * The search component (search input and search result) for customer targeting.
 * <p>
 * Created by jobaer on 8/22/16.
 */
class DCACustomerTargetingSearchComponent {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(DCACustomerTargetingSearchComponent.class);
    private final Consumer<Boolean> labelDisplay;

    private DCACustomerTargetingService targetingService;
    private DCACustomerTargetingSearchResult searchResult;
    private DCACustomerTargetingSearchInput searchInput;
    private CssLayout searchResultContainer;
    private UI mainUi;

    DCACustomerTargetingSearchComponent(UI mainUi, DCAUser loggedInUser, DCAWidget widgetConfig,
                                        Consumer<DCAItem> selectionAction,
                                        Predicate<DCAItem> checkSelection, Consumer<Boolean> labelDisplay, BooleanSupplier checkMaxSelectedItem) {
        this.mainUi = mainUi;
        searchInput = new DCACustomerTargetingSearchInput(this::doSearch);
        searchResult = new DCACustomerTargetingSearchResult(mainUi, widgetConfig, selectionAction, checkSelection, checkMaxSelectedItem);
        targetingService = new DCACustomerTargetingService(loggedInUser);
        this.labelDisplay = labelDisplay;
    }

    Component getSearchInput() {
        return searchInput;
    }

    Component getSearchResult() {
        searchResultContainer = createCssLayout("customer-targeting-search-result-container", 100);
        addSeparatorWithPadding(searchResultContainer);
        searchResultContainer.addComponent(searchResult);
        searchResultContainer.setVisible(false);
        return searchResultContainer;
    }

    void updateResultList() {
        searchResult.updateResultList();
    }

    private void doSearch(String input) {
        String value = StringUtils.stripToEmpty(input);
        if (StringUtils.isBlank(value)) {
            searchResultContainer.setVisible(false);
            labelDisplay.accept(false);
            return;
        }

        mainUi.access(() -> {
            searchResult.clear();
            searchResultContainer.setVisible(true);
            labelDisplay.accept(true);
        });

        targetingService
            .search(value)
            .switchIfEmpty(
                Observable.create(subscriber -> searchResult.showErrorMessage()))
            .subscribe(
                book -> mainUi.access(() -> searchResult.addItemToResult(book)),
                error -> {
                    logger.warn("Error occurred while searching in the metadata service", error);
                    searchResult.showErrorMessage();
                });
    }
}
