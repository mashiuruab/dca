package com.sannsyn.dca.vaadin.component.custom.container.simple;

import com.google.gson.JsonObject;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;

import java.util.function.Consumer;

/**
 * A container ui which will show row/column type data.
 * <p>
 * Created by jobaer on 3/6/17.
 */
public class DCAClickableItemContainerImpl extends DCASimpleItemContainerImpl implements DCAClickableItemContainer {
    private Consumer<JsonObject> callback;

    @Override
    public void registerClickHandler(Consumer<JsonObject> callback) {
        this.callback = callback;
    }

    @Override
    protected Component paintItem(JsonObject item) {
        CssLayout collapsibleLayout = new CssLayout();
        collapsibleLayout.addStyleName("item-wrapper");
        collapsibleLayout.setWidth(100, Unit.PERCENTAGE);

        CssLayout collapsedItem = createCollapsedComponent(item);
        collapsibleLayout.addComponent(collapsedItem);
        collapsibleLayout.addLayoutClickListener(event -> callback.accept(item));

        return collapsibleLayout;
    }
}
