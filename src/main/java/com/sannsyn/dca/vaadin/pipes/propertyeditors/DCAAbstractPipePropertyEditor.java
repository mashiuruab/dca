package com.sannsyn.dca.vaadin.pipes.propertyeditors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sannsyn.dca.service.recommender.PipeProperty;
import com.sannsyn.dca.vaadin.widgets.controller.overview.model.aggregates.DCAPipe;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;

/**
 * An abstract class for the property editors
 * <p>
 * Created by jobaer on 5/26/16.
 */
abstract class DCAAbstractPipePropertyEditor implements DCAPipePropertyEditor {
    protected final DCAPipe item;
    protected final PipeProperty property;
    private static final Logger logger = LoggerFactory.getLogger(DCAAbstractPipePropertyEditor.class);

    DCAAbstractPipePropertyEditor(DCAPipe item, PipeProperty property) {
        if (item == null || property == null) {
            throw new IllegalArgumentException("Could not initialize property with null values");
        }
        this.item = item;
        this.property = property;
    }

    Optional<String> getServerSideValue() {
        logger.debug(property.getPropertyName());
        if (true) {
            logger.debug("Object type property. Returning empty value.");
            return Optional.empty();
        } else {
            logger.debug("Basic type property. Will return value.");
            Optional<String> valueFromPipe = getValueFromPipe();
            logger.debug(valueFromPipe.toString());
            return valueFromPipe;
        }
    }

    private Optional<String> getValueFromPipe() {
        JsonObject serverSideJson = item.getServerSideJson();
        if (serverSideJson == null) {
            return Optional.empty();
        }

        if (property.hasParent()) {
            logger.debug("Will get value from parent object");
            logger.debug("Parent is " + property.getParent().getPropertyName());
            JsonElement parentJson = serverSideJson.get(property.getParent().getPropertyName());
            if (parentJson != null) {
                return extractValue(parentJson.getAsJsonObject(), property.getPropertyName());
            }
        } else {
            return extractValue(serverSideJson, property.getPropertyName());
        }

        return Optional.empty();
    }

    private Optional<String> extractValue(JsonObject serverSideJson, String propertyName) {
        JsonElement jsonElement = serverSideJson.get(propertyName);
        if (jsonElement == null) return Optional.empty();

        if (jsonElement.isJsonPrimitive()) {
            return Optional.of(jsonElement.getAsString());
        } else if (jsonElement.isJsonArray()) {
            return handleArrayValues(jsonElement);
        } else {
            return Optional.empty();
        }
    }

    private Optional<String> handleArrayValues(JsonElement jsonElement) {
        StringBuilder result = new StringBuilder();
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        int len = jsonArray.size();
        for (int i = 0; i < len; i++) {
            if (i == len - 1) {
                result.append(jsonArray.get(i).getAsString());
            } else {
                result.append(jsonArray.get(i).getAsString());
                result.append(", ");
            }
        }

        return Optional.of(result.toString());
    }

    @Override
    public String getName() {
        return property.getPropertyName();
    }
}
