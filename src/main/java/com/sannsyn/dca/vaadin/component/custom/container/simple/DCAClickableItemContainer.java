package com.sannsyn.dca.vaadin.component.custom.container.simple;

import com.google.gson.JsonObject;

import java.util.function.Consumer;

/**
 * Interface for a container ui which will show row/column type json data. The column names are configurable.
 * <p>
 * <p>
 * Created by jobaer on 3/6/17.
 */
public interface DCAClickableItemContainer extends DCASimpleItemContainer {
    /**
     * The callback function that will be called when the rows are clicked.
     *
     * @param callback the callback function
     */
    void registerClickHandler(Consumer<JsonObject> callback);
}
