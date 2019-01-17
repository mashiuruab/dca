package com.sannsyn.dca.vaadin.pipes.propertyeditors;

import com.google.gson.JsonNull;
import com.sannsyn.dca.service.recommender.PipeProperty;
import com.sannsyn.dca.vaadin.widgets.controller.overview.model.aggregates.DCAPipe;
import com.typesafe.config.Config;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;

import java.util.Optional;

/**
 * A property editor for string types.
 * <p>
 * Created by jobaer on 3/23/17.
 */
public class DCAPipeNullPropertyEditor implements DCAPipePropertyEditor {
    private final DCAPipe item;
    private final PipeProperty property;

    public DCAPipeNullPropertyEditor(DCAPipe item, PipeProperty property) {
        if (item == null || property == null) {
            throw new IllegalArgumentException("Could not initialize property with null values");
        }
        this.item = item;
        this.property = property;
    }

    @Override
    public Component getComponent() {
        CssLayout empty = new CssLayout();
        empty.setWidth(100, Sizeable.Unit.PERCENTAGE);
        return empty;
    }

    @Override
    public Optional<Object> getValue() {
        return Optional.of(JsonNull.INSTANCE);
    }

    @Override
    public String getName() {
        return property.getPropertyName();
    }

    @Override
    public void setValue(Object value) {
        // do nothing
    }

    @Override
    public void setExtractor(DCAPropertyExtractor extractor) {
        // do nothing
    }

    static boolean matches(Config config) {
        if (config != null && config.hasPath("type")) {
            String type = config.getString("type");
            if ("null".equals(type)) {
                return true;
            }
        }

        return false;
    }
}
