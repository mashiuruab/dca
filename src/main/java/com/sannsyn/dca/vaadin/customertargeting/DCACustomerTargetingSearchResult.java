package com.sannsyn.dca.vaadin.customertargeting;

import com.sannsyn.dca.metadata.DCAItem;
import com.sannsyn.dca.model.config.DCAWidget;
import com.sannsyn.dca.vaadin.component.custom.container.AlternatingRowClass;
import com.sannsyn.dca.vaadin.component.custom.container.ItemContainer;
import com.sannsyn.dca.vaadin.component.custom.container.ItemPainter;
import com.sannsyn.dca.vaadin.component.custom.notification.DCANotification;
import com.sannsyn.dca.vaadin.event.DCAPopupNotificationEvent;
import com.sannsyn.dca.vaadin.event.DCAPopupNotificationEventBus;
import com.sannsyn.dca.vaadin.helper.DCAConfigurableColumnsBuilder;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCANotificationComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAPopupErrorComponent;
import com.vaadin.ui.*;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.sannsyn.dca.vaadin.component.custom.container.ItemContainer.DEFAULT_VIEW;
import static com.sannsyn.dca.vaadin.customertargeting.DCACustomerTargetingWidget.getServiceName;
import static com.sannsyn.dca.vaadin.ui.DCAUiHelper.*;
import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;

/**
 * Search result component for the customer targeting widget.
 * <p>
 * Created by jobaer on 8/22/16.
 */
class DCACustomerTargetingSearchResult extends CustomComponent {
    private Layout searchResult;
    private final UI mainUi;
    private final DCAWidget config;
    private CssLayout errorLayout;
    private ItemContainer<DCAItem> resultContainer = new ItemContainer<>(false);
    private Consumer<DCAItem> selectionAction;
    private Predicate<DCAItem> checkSelection;
    private BooleanSupplier checkMaxSelectedItem;

    DCACustomerTargetingSearchResult(UI mainUi, DCAWidget widgetConfig, Consumer<DCAItem> action,
                                     Predicate<DCAItem> check, BooleanSupplier checkMaxSelectedItem) {
        this.mainUi = mainUi;
        this.config = widgetConfig;
        this.selectionAction = action;
        this.checkSelection = check;
        this.checkMaxSelectedItem = checkMaxSelectedItem;
        this.searchResult = createSearchResult();
        setCompositionRoot(searchResult);
    }

    private Layout createSearchResult() {
        CssLayout cssLayout = new CssLayout();
        cssLayout.setWidth(100, PERCENTAGE);
        cssLayout.addStyleName("customer-targeting-search-result");

        AlternatingRowClass rowClass = new AlternatingRowClass("odd", "even");
        resultContainer.setResetAction(rowClass::reset);
        ItemPainter<DCAItem> painter = createPainter(rowClass);
        resultContainer.registerPainter(DEFAULT_VIEW, painter);

        Component columnHeaders = makeColumnHeaders();
        cssLayout.addComponent(columnHeaders);

        errorLayout = createErrorLayout(100);
        cssLayout.addComponent(errorLayout);

        Component component = resultContainer.getComponent();
        component.addStyleName("customer-targeting-search-result-items");

        cssLayout.addComponent(component);
        return cssLayout;
    }

    private ItemPainter<DCAItem> createPainter(final AlternatingRowClass rowClass) {
        String serviceName = getServiceName();
        return createPainter(serviceName, rowClass);
    }

    private ItemPainter<DCAItem> createPainter(String serviceName, final AlternatingRowClass rowClass) {
        return new ItemPainter<DCAItem>() {
            @Override
            public ComponentContainer draw(DCAItem item) {
                CssLayout layout = new CssLayout();
                layout.setWidth(100, PERCENTAGE);
                drawItem(layout, item);
                return layout;
            }

            private void drawItem(ComponentContainer layout, DCAItem item) {
                CssLayout cssLayout = new CssLayout();
                cssLayout.setWidth(100, PERCENTAGE);
                cssLayout.addStyleName("result-item");

                if (checkSelection.test(item)) {
                    cssLayout.addStyleName("selected");
                }
                cssLayout.addStyleName(rowClass.alt());
                CssLayout plusWidth = createPlusIcon(item, cssLayout);
                cssLayout.addComponent(plusWidth);

                DCAConfigurableColumnsBuilder.buildValuesFromConfig(serviceName, config, item, (val, width) -> {
                        Label label = createValueLabel(val, width);
                        cssLayout.addComponent(label);
                    }, (url, width) -> cssLayout.addComponent(createImageLabel(url, width)),
                    () -> createDefaultValues(cssLayout, item), "columnConfig");

                layout.addComponent(cssLayout);
            }

            @Override
            public void redraw(ComponentContainer comp, DCAItem item) {
                comp.removeAllComponents();
                drawItem(comp, item);
            }
        };
    }

    private Label createValueLabel(String value, Integer width) {
        Label title = new Label(value);
        title.setWidth(width, PERCENTAGE);
        return title;
    }

    private CssLayout createPlusIcon(DCAItem item, Layout cssLayout) {
        Button plusIcon = new Button("");

        plusIcon.addClickListener(click -> {
            if (!checkMaxSelectedItem.getAsBoolean()) {
                DCANotification.NOTIFICATION.show("You can not Select more than 20 items");
                return;
            }
            selectionAction.accept(item);
            cssLayout.addStyleName("selected");
        });

        CssLayout layout1 = wrapWithCssLayout(plusIcon, "plus-icon-wrapper");
        layout1.setWidth(10, PERCENTAGE);
        return layout1;
    }

    private Component makeColumnHeaders() {
        CssLayout headerWrapper = new CssLayout();
        headerWrapper.setWidth(100, PERCENTAGE);
        headerWrapper.addStyleName("customer-targeting-result-colheader-wrapper");

        Label add = createColumnLabel("Add", 10);
        headerWrapper.addComponent(add);

        String serviceName = getServiceName();

        DCAConfigurableColumnsBuilder.buildHeadersFromConfig(serviceName, config, null,
            (name, width) -> {
                Label columnLabel = createColumnLabel(name, width);
                headerWrapper.addComponent(columnLabel);
            }, () -> createDefaultColumnNames(headerWrapper), "columnConfig");

        return headerWrapper;
    }

    private void createDefaultColumnNames(CssLayout layout) {
        Label isbn = createColumnLabel("ID", 15);
        Label title = createColumnLabel("Name", 45);
        Label popularity = createColumnLabel("Popularity", 30);

        layout.addComponent(isbn);
        layout.addComponent(title);
        layout.addComponent(popularity);
    }

    private void createDefaultValues(CssLayout layout, DCAItem item) {
        Label id = createValueLabel(item.getId(), 15);
        Label title = createValueLabel(item.getTitle(), 45);

        String popularityVal = "";
        if(item.getPopularity() != null) {
            popularityVal = String.format("%.2f", item.getPopularity());
        }
        Label popularity = createValueLabel(popularityVal, 30);

        layout.addComponent(id);
        layout.addComponent(title);
        layout.addComponent(popularity);
    }

    private Label createColumnLabel(String labelName, int widthPercentage) {
        Label add = new Label(labelName);
        add.setStyleName("recommender-component-search-result-colheader");
        add.setWidth(widthPercentage, PERCENTAGE);
        return add;
    }

    void showErrorMessage() {
        mainUi.access(() -> {
            resultContainer.clear();
            errorLayout.addStyleName("visible");
        });
    }

    void updateResultList() {
        errorLayout.removeStyleName("visible");
        resultContainer.redraw();
    }

    void addItemToResult(DCAItem item) {
        errorLayout.removeStyleName("visible");
        resultContainer.addItem(item);
    }

    public void clear() {
        resultContainer.clear();
    }
}
