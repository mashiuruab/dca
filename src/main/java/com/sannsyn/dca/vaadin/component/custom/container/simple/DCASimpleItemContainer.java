package com.sannsyn.dca.vaadin.component.custom.container.simple;

import com.google.gson.JsonObject;
import com.sannsyn.dca.vaadin.component.custom.container.collapsible.DCAColumnSpec;
import com.vaadin.ui.Component;

import java.util.List;

/**
 * Interface for a container ui which will show row/column type json data. The column names are configurable.
 * <p>
 * <p>
 * Created by jobaer on 3/6/17.
 */
public interface DCASimpleItemContainer extends Component {
    /**
     * Set the column specifications. The header and the rows will be drawn according to this spec.
     *
     * @param columnSpecs the column specs
     */
    void setColumnSpecs(List<DCAColumnSpec> columnSpecs);

    /**
     * Add a list of items to this container. This function should be called from inside UI thread.
     *
     * @param items the list of items to be added
     */
    void addItems(List<JsonObject> items);

    /**
     * Clear the already added items.
     */
    void clear();
}
