package com.sannsyn.dca.vaadin.customertargeting;

import com.sannsyn.dca.metadata.DCAItem;
import com.sannsyn.dca.model.config.DCAWidget;
import com.sannsyn.dca.vaadin.component.custom.container.AlternatingRowClass;
import com.sannsyn.dca.vaadin.component.custom.container.ItemContainer;
import com.sannsyn.dca.vaadin.component.custom.container.ItemPainter;
import com.sannsyn.dca.vaadin.ui.DCAUiHelper;
import com.vaadin.ui.*;

import java.util.function.Consumer;

import static com.sannsyn.dca.vaadin.component.custom.container.ItemContainer.DEFAULT_VIEW;
import static com.sannsyn.dca.vaadin.customertargeting.DCACustomerTargetingWidget.getServiceName;
import static com.sannsyn.dca.vaadin.helper.DCAConfigurableColumnsBuilder.buildHeadersFromConfig;
import static com.sannsyn.dca.vaadin.helper.DCAConfigurableColumnsBuilder.buildValuesFromConfig;
import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;
import static com.vaadin.server.Sizeable.Unit.PIXELS;

/**
 * The popup component. It provides two services - adding an item. And listening for remove events.
 * <p>
 * Created by jobaer on 8/25/16.
 */
class DCACustomerTargetingPopup extends CustomComponent {
    private final DCAWidget config;
    private ItemContainer<DCAItem> selectedResultContainer = new ItemContainer<>(false);
    private Consumer<DCAItem> removeAction;

    DCACustomerTargetingPopup(DCAWidget config, Consumer<DCAItem> removeAction) {
        this.removeAction = removeAction;
        this.config = config;
        Component component = createSelectedItemsPopup();
        setCompositionRoot(component);
    }

    void addItem(DCAItem b) {
        selectedResultContainer.addItem(b);
    }

    private Component createSelectedItemsPopup() {
        Layout cssLayout = new CssLayout();
        cssLayout.setWidth(600, PIXELS);
        Layout popSearchResult = createPopSearchResult();
        cssLayout.addComponent(popSearchResult);

        return cssLayout;
    }

    private Layout createPopSearchResult() {
        CssLayout cssLayout = new CssLayout();
        cssLayout.setWidth(100, PERCENTAGE);
        cssLayout.addStyleName("customer-targeting-search-result");

        Component columnHeaders = makeColumnHeaders();

        AlternatingRowClass rowClass = new AlternatingRowClass("odd", "even");
        ItemPainter<DCAItem> painter = new ItemPainter<DCAItem>() {
            @Override
            public ComponentContainer draw(DCAItem item) {
                CssLayout layout = new CssLayout();
                layout.setWidth(100, PERCENTAGE);
                drawItem(layout, item);
                return layout;
            }

            private void drawItem(ComponentContainer layout, DCAItem item) {
                createColumnValuesFromConfig(layout, item, rowClass);
            }

            @Override
            public void redraw(ComponentContainer comp, DCAItem item) {
                comp.removeAllComponents();
                drawItem(comp, item);
            }
        };
        selectedResultContainer.registerPainter(DEFAULT_VIEW, painter);
        Component component = selectedResultContainer.getComponent();
        component.addStyleName("customer-targeting-search-result-items");

        cssLayout.addComponent(columnHeaders);
        cssLayout.addComponent(component);

        return cssLayout;
    }

    private Component makeColumnHeaders() {
        return createColumnHeadersFromConfig(config);
    }

    private Component createColumnHeadersFromConfig(DCAWidget config) {
        CssLayout layout = prepareFirstColumn();
        String serviceName = getServiceName();
        buildHeadersFromConfig(serviceName, config, null, (label, width) -> {
                Label header = createColumnHeader(label, width);
                layout.addComponent(header);
            }, () -> {
                addDefaultColumnHeaders(layout);
            }
            , "popupConfig");
        return layout;
    }

    private void createColumnValuesFromConfig(ComponentContainer layout, DCAItem item, AlternatingRowClass rowClass) {
        CssLayout cssLayout = new CssLayout();
        cssLayout.setWidth(100, PERCENTAGE);
        cssLayout.addStyleName("result-item");
        cssLayout.addStyleName(rowClass.alt());

        CssLayout plusIcon = createPlusIcon(item);
        cssLayout.addComponent(plusIcon);

        String serviceName = getServiceName();
        buildValuesFromConfig(serviceName, config, item, (value, width) -> {
                Label valueLabel = createValueLabel(value, width);
                cssLayout.addComponent(valueLabel);
            }, (val, width) -> {
                Component imageLabel = DCAUiHelper.createImageLabel(val, width);
                cssLayout.addComponent(imageLabel);
            },
            () -> addDefaultValues(item, cssLayout), "popupConfig");

        layout.addComponent(cssLayout);
    }

    private void addDefaultColumnHeaders(CssLayout layout) {
        Label title = createLabel("Title", 65);
        Label isbn = createLabel("ID", 25);

        layout.addComponent(title);
        layout.addComponent(isbn);
    }

    private void addDefaultValues(DCAItem item, CssLayout layout) {
        Label titleLabel = createValueLabel(item.getTitle(), 65);
        Label idLabel = createValueLabel(item.getId(), 25);
        layout.addComponent(titleLabel);
        layout.addComponent(idLabel);
    }

    private CssLayout prepareFirstColumn() {
        CssLayout headerWrapper = new CssLayout();
        headerWrapper.setWidth(100, PERCENTAGE);
        headerWrapper.addStyleName("customer-targeting-result-colheader-wrapper");

        Label add = createLabel("", 10);
        headerWrapper.addComponent(add);
        return headerWrapper;
    }

    private Label createValueLabel(String value, int widthPercentage) {
        Label label = new Label(value);
        label.setWidth(widthPercentage, PERCENTAGE);
        return label;
    }

    private Label createColumnHeader(String columnName, int widthPercentage) {
        return createLabel(columnName, widthPercentage);
    }

    private Label createLabel(String labelName, int widthPercentage) {
        Label add = new Label(labelName);
        add.setStyleName("recommender-component-search-result-colheader");
        add.setWidth(widthPercentage, PERCENTAGE);
        return add;
    }

    private CssLayout createPlusIcon(DCAItem item) {
        CssLayout buttonWrapper = new CssLayout();
        buttonWrapper.addStyleName("remove-icon-wrapper");
        buttonWrapper.setWidth(10, PERCENTAGE);

        Button removeButton = new Button("");
        removeButton.addClickListener(click -> {
            selectedResultContainer.removeItem(item);
            removeAction.accept(item);
        });

        buttonWrapper.addComponent(removeButton);
        return buttonWrapper;
    }
}
