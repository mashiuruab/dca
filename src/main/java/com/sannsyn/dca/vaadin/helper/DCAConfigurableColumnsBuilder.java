package com.sannsyn.dca.vaadin.helper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sannsyn.dca.metadata.DCAItem;
import com.sannsyn.dca.model.config.DCAWidget;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static com.sannsyn.dca.util.DCAReflectionUtil.getItemPropertyAsString;

/**
 * A helper class for building tabular layout using a defined configuration format
 * <p>
 * Created by jobaer on 2/13/17.
 */
public class DCAConfigurableColumnsBuilder {
    public static void buildValuesFromConfig(String serviceName, DCAWidget config, DCAItem item,
                                             BiConsumer<String, Integer> labelPainter,
                                             BiConsumer<String, Integer> imagePainter,
                                             Runnable defaultRowPainter, String configName) {
        iterateJsonStructure(
            serviceName, config, item, DCAConfigurableColumnsBuilder::addValueFor, labelPainter,
            imagePainter, defaultRowPainter, configName);
    }

    public static void buildHeadersFromConfig(String serviceName, DCAWidget config, DCAItem item, BiConsumer<String, Integer> labelPainter,
                                              Runnable defaultHeaderPainter, String configName) {
        iterateJsonStructure(
            serviceName, config, item, DCAConfigurableColumnsBuilder::addLabelFor, labelPainter,
            (s, w) -> {
            }, defaultHeaderPainter, configName);
    }

    private static void iterateJsonStructure(String serviceName, DCAWidget config, DCAItem item,
                                             MultiConsumer<DCAItem, String, JsonObject, BiConsumer<String, Integer>, BiConsumer<String, Integer>> multiConsumer,
                                             BiConsumer<String, Integer> labelConsumer,
                                             BiConsumer<String, Integer> imageConsumer,
                                             Runnable defaultHandler, String configName) {
        if (config.getJsonConfig() == null || !config.getJsonConfig().has(configName)) {
            defaultHandler.run();
            return;
        }

        JsonObject popupConfig = config.getJsonConfig().get(configName).getAsJsonObject();
        if (popupConfig.has(serviceName)) {
            multiConsumer.accept(item, serviceName, popupConfig, labelConsumer, imageConsumer);
        } else if (popupConfig.has("default")) {
            multiConsumer.accept(item, "default", popupConfig, labelConsumer, imageConsumer);
        } else {
            defaultHandler.run();
        }
    }

    private static void addValueFor(DCAItem item, String serviceName, JsonObject popupConfig,
                                    BiConsumer<String, Integer> labelConsumer,
                                    BiConsumer<String, Integer> imageConsumer) {
        iterateItems(item, serviceName, popupConfig, DCAConfigurableColumnsBuilder::getPropertyValue, labelConsumer, imageConsumer);
    }

    private static void addLabelFor(DCAItem item, String serviceName, JsonObject popupConfig,
                                    BiConsumer<String, Integer> labelConsumer,
                                    BiConsumer<String, Integer> imageConsumer) {
        iterateItems(item, serviceName, popupConfig, DCAConfigurableColumnsBuilder::getLabel, labelConsumer, imageConsumer);
    }

    private static void iterateItems(DCAItem item, String serviceName, JsonObject popupConfig,
                                     BiFunction<JsonElement, DCAItem, String> valueFunction,
                                     BiConsumer<String, Integer> labelConsumer,
                                     BiConsumer<String, Integer> imageConsumer) {
        JsonArray asJsonArray = popupConfig.get(serviceName).getAsJsonArray();
        for (JsonElement jsonElement : asJsonArray) {
            String value = valueFunction.apply(jsonElement, item);
            int width = jsonElement.getAsJsonObject().get("width").getAsInt();
            if (jsonElement.getAsJsonObject().has("type")) {
                String type = jsonElement.getAsJsonObject().get("type").getAsString();
                if ("image".equals(type)) {
                    imageConsumer.accept(value, width);
                } else {
                    labelConsumer.accept(value, width);
                }
            } else {
                labelConsumer.accept(value, width);
            }
        }
    }

    private static String getPropertyValue(JsonElement jsonElement, DCAItem item) {
        String properName = jsonElement.getAsJsonObject().get("property").getAsString();
        String value = getValue(item, properName);

        if (jsonElement.getAsJsonObject().has("type")) {
            String type = jsonElement.getAsJsonObject().get("type").getAsString();
            if ("float".equals(type)) {
                return formatFloat(value);
            }
        }

        return value;
    }

    private static String formatFloat(String value) {
        try {
            Double aDouble = Double.valueOf(value);
            return String.format("%.2f", aDouble);
        } catch (NumberFormatException e) {
            return value;
        }
    }

    private static String getLabel(JsonElement jsonElement, DCAItem item) {
        return jsonElement.getAsJsonObject().get("label").getAsString();
    }

    private static String getValue(DCAItem item, String propertyName) {
        return item != null ? getItemPropertyAsString(item, propertyName) : "";
    }

    @FunctionalInterface
    private interface MultiConsumer<S, T, U, V, W> {
        void accept(S s, T t, U u, V v, W w);
    }
}
