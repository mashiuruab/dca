package com.sannsyn.dca.vaadin.component.custom.container.simple;

import com.google.gson.JsonObject;
import com.sannsyn.dca.vaadin.component.custom.container.collapsible.DCAColumnSpec;
import com.sannsyn.dca.vaadin.ui.DCAUiHelper;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;

import java.util.ArrayList;
import java.util.List;

import static com.sannsyn.dca.vaadin.ui.DCAUiHelper.runInUiThread;

/**
 * A container ui which will show row/column type configurable json data.
 * <p>
 * Created by jobaer on 3/6/17.
 */
public class DCASimpleItemContainerImpl extends CustomComponent implements DCASimpleItemContainer {
    private List<JsonObject> storage = new ArrayList<>();
    private List<DCAColumnSpec> columnSpecs = new ArrayList<>();

    private CssLayout header = new CssLayout();
    private CssLayout resultContainer = new CssLayout();

    public DCASimpleItemContainerImpl() {
        CssLayout rootComponent = new CssLayout();
        rootComponent.addStyleName("item-container");
        rootComponent.setWidth(100, Unit.PERCENTAGE);

        header.setWidth(getHeaderWidth(), Unit.PERCENTAGE);
        header.addStyleName("item-header");

        resultContainer.setWidth(100, Unit.PERCENTAGE);
        resultContainer.addStyleName("result-container");

        rootComponent.addComponent(header);
        rootComponent.addComponent(resultContainer);
        setCompositionRoot(rootComponent);
    }

    protected float getHeaderWidth() {
        return 100;
    }

    @Override
    public void setColumnSpecs(List<DCAColumnSpec> columnSpecs) {
        this.columnSpecs.clear();
        this.columnSpecs.addAll(columnSpecs);

        CssLayout headerWrapper = new CssLayout();
        headerWrapper.setWidth(100, Unit.PERCENTAGE);

        for (DCAColumnSpec columnSpec : this.columnSpecs) {
            Component columnHeading = createColumnHeading(columnSpec);
            headerWrapper.addComponent(columnHeading);
        }
        header.addComponent(headerWrapper);
    }

    @Override
    public void addItems(List<JsonObject> items) {
        storage.addAll(items);
        rebuildUi();
    }

    @Override
    public void clear() {
        storage.clear();
        resultContainer.removeAllComponents();
    }

    private Component createColumnHeading(DCAColumnSpec columnSpec) {
        CssLayout heading = new CssLayout();
        heading.setWidth(columnSpec.getWidth(), Unit.PERCENTAGE);
        heading.addComponent(new Label(columnSpec.getColumnName()));
        return heading;
    }

    private void rebuildUi() {
        List<Component> children = createChildren();
        runInUiThread(() -> {
            resultContainer.removeAllComponents();
            if (children.isEmpty()) {
                CssLayout errorLayout = DCAUiHelper.createErrorLayout(100);
                resultContainer.addComponent(errorLayout);
                errorLayout.addStyleName("visible");
            } else {
                children.forEach(child -> resultContainer.addComponent(child));
            }
        });
    }

    private List<Component> createChildren() {
        List<Component> children = new ArrayList<>();
        for (JsonObject jsonObject : storage) {
            Component component = paintItem(jsonObject);
            children.add(component);
        }
        return children;
    }

    protected Component paintItem(JsonObject item) {
        CssLayout collapsibleLayout = new CssLayout();
        collapsibleLayout.addStyleName("item-wrapper");
        collapsibleLayout.setWidth(100, Unit.PERCENTAGE);

        CssLayout collapsedItem = createCollapsedComponent(item);
        collapsibleLayout.addComponent(collapsedItem);

        return collapsibleLayout;
    }

    protected CssLayout createCollapsedComponent(JsonObject item) {
        return createLayoutWithIcon(item);
    }

    private CssLayout createLayoutWithIcon(JsonObject item) {
        CssLayout layout = new CssLayout();
        layout.setWidth(100, Unit.PERCENTAGE);
        layout.setStyleName("main-item-container");

        CssLayout collapsedItem = createCollapsedItem(item);
        layout.addComponent(collapsedItem);

        return layout;
    }

    private CssLayout createCollapsedItem(JsonObject item) {
        CssLayout itemWrapper = new CssLayout();
        itemWrapper.setWidth(100, Unit.PERCENTAGE);

        for (DCAColumnSpec columnSpec : columnSpecs) {
            Component column = createItemColumn(columnSpec, item);
            itemWrapper.addComponent(column);
        }

        return itemWrapper;
    }

    private Component createItemColumn(DCAColumnSpec columnSpec, JsonObject item) {
        CssLayout column = new CssLayout();
        column.setWidth(columnSpec.getWidth(), Unit.PERCENTAGE);

        String value = "";
        if(item.has(columnSpec.getPropertyName())) {
            value = item.get(columnSpec.getPropertyName()).getAsString();
        }

        Label label = new Label(value);
        column.addComponent(label);
        return column;
    }
}
