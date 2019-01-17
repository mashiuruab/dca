package com.sannsyn.dca.vaadin.component.custom.container.collapsible;

import com.google.gson.JsonObject;
import com.sannsyn.dca.vaadin.component.custom.container.simple.DCASimpleItemContainerImpl;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;

import java.util.function.Function;

/**
 * A container ui which will show row/column type data. And will give the ability to expand/collapse rows.
 * <p>
 * Created by jobaer on 3/6/17.
 */
public class DCACollapsibleItemContainerImpl extends DCASimpleItemContainerImpl implements DCACollapsibleItemContainer {
    private Function<JsonObject, Component> expandHandler = (JsonObject o) -> new CssLayout();

    @Override
    public void registerExpandHandler(Function<JsonObject, Component> callback) {
        this.expandHandler = callback;
    }

    @Override
    protected float getHeaderWidth() {
        //Width is less due to the expand/collapse icon
        return 90;
    }

    @Override
    protected Component paintItem(JsonObject item) {
        DCACollapsibleLayout collapsibleLayout = new DCACollapsibleLayoutImpl();
        collapsibleLayout.addStyleName("item-wrapper");
        collapsibleLayout.setWidth(100, Unit.PERCENTAGE);

        CssLayout collapsedItem = createCollapsedComponent(item);
        collapsibleLayout.setCollapseComponent(collapsedItem);

        CssLayout expandedItem = createExpandedComponent(item);
        collapsibleLayout.setExpansionComponent(expandedItem);

        return collapsibleLayout;
    }

    private CssLayout createExpandedComponent(JsonObject item) {
        CssLayout layout = new CssLayout();
        layout.setWidth(100, Unit.PERCENTAGE);

        Component expanded = expandHandler.apply(item);
        layout.addComponent(expanded);

        return layout;
    }
}
