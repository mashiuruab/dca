package com.sannsyn.dca.vaadin.component.custom.container.collapsible;

import com.google.gson.JsonObject;
import com.sannsyn.dca.vaadin.component.custom.container.simple.DCASimpleItemContainer;
import com.vaadin.ui.Component;

import java.util.function.Function;

/**
 * Interface for a container ui which will show row/column type data. And will give the ability to expand/collapse rows.
 * <p>
 * Created by jobaer on 3/6/17.
 */
public interface DCACollapsibleItemContainer extends DCASimpleItemContainer {
    /**
     * A register a callback that will be used to create the component when the row is expanded.
     *
     * @param callback the callback function
     */
    void registerExpandHandler(Function<JsonObject, Component> callback);
}
