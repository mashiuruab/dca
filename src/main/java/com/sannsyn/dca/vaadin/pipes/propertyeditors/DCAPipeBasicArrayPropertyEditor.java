package com.sannsyn.dca.vaadin.pipes.propertyeditors;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sannsyn.dca.service.recommender.PipeProperty;
import com.sannsyn.dca.vaadin.widgets.controller.overview.model.aggregates.DCAPipe;
import com.typesafe.config.Config;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * A property editor for integer types.
 * <p>
 * Created by jobaer on 3/23/17.
 */
public class DCAPipeBasicArrayPropertyEditor extends DCAPipeStringPropertyEditor {
    private static final Logger logger = LoggerFactory.getLogger(DCAPipeBasicArrayPropertyEditor.class);

    public DCAPipeBasicArrayPropertyEditor(DCAPipe item, PipeProperty property) {
        super(item, property);
        setupExtractor();
    }

    public DCAPipeBasicArrayPropertyEditor(DCAPipe item, PipeProperty property, boolean hideLabel) {
        super(item, property, hideLabel);
        setupExtractor();
    }

    protected void initializeValue() {
        JsonObject serverSideJson = item.getServerSideJson();
        if (serverSideJson != null && serverSideJson.has(property.getPropertyName())) {
            JsonElement jsonElement = serverSideJson.get(property.getPropertyName());
            try {
                String commaSeparatedString = getCommaSeparatedString(jsonElement.getAsJsonArray());
                textField.setValue(commaSeparatedString);
            } catch (Exception e) {
                // don't do anything ... editor will be empty
                logger.error("Error : ", e);
            }
        } else if (property.getDefaultValueConfig() != null) {
            Gson gson = new Gson();
            JsonArray asJsArray = gson.fromJson(String.valueOf(property.getDefaultValueConfig()), JsonArray.class);
            String commanSeparatedString = getCommaSeparatedString(asJsArray);
            if (StringUtils.isNotEmpty(commanSeparatedString)) {
                textField.setInputPrompt(commanSeparatedString);
            }
        }
    }

    private String getCommaSeparatedString(JsonArray asJsonArray) {
        StringBuilder targetString = new StringBuilder();
        for(int i=0; i<asJsonArray.size(); i++) {
            targetString.append(asJsonArray.get(i).getAsString());
            if(i < asJsonArray.size() - 1) {
                targetString.append(", ");
            }
        }

        return targetString.toString();
    }

    @Override
    public void setValue(Object value) {
        if(value instanceof String) {
            textField.setValue((String) value);
        }
    }

    private void setupExtractor() {
        setExtractor(value -> {
            if (value != null) {
                String strValue = value.toString();
                if (StringUtils.isNotBlank(strValue)) {
                    String[] items = strValue.split(",");

                    if ("integer".equals(getItemsType())) {
                        Integer[] itemInts = new Integer[items.length];
                        for (int counter = 0; counter < items.length; counter++) {
                            itemInts[counter] = Integer.valueOf(items[counter].trim());
                        }
                        return Optional.of(itemInts);
                    }

                    return Optional.of(items);
                }
            }
            return Optional.empty();
        });
    }

    private String getItemsType() {
        String itemsType = "string";
        if (property.getSchema().hasPath("items.type")) {
            itemsType = property.getSchema().getString("items.type");
        } else if (property.getSchema().hasPath("items")) {
            itemsType = property.getSchema().getString("items");
        }

        return itemsType;
    }

    static boolean matches(Config config) {
        if (config != null && config.hasPath("type")) {
            String type = config.getString("type");
            if ("array".equals(type)) {
                return true;
            }
        }

        return false;
    }
}
