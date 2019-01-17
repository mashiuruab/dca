package com.sannsyn.dca.vaadin.pipes.propertyeditors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sannsyn.dca.service.DCAPipesConfigParser;
import com.sannsyn.dca.service.recommender.PipeProperty;
import com.sannsyn.dca.vaadin.widgets.controller.overview.model.aggregates.DCAPipe;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;
import com.vaadin.data.Validator;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;

/**
 * A property editor for string types.
 * <p>
 * Created by jobaer on 3/23/17.
 */
public class DCAPipeObjectPropertyEditor implements DCAPipePropertyEditor {
    private static final Logger logger = LoggerFactory.getLogger(DCAPipeObjectPropertyEditor.class);

    private final DCAPipe item;
    private final PipeProperty property;
    private DCAPropertyExtractor extractor;
    private boolean hideLabel = false;
    private List<DCAPipePropertyEditor> childProperties = new ArrayList<>();
    private DCAPipesConfigParser pipesConfigParser = new DCAPipesConfigParser();

    public DCAPipeObjectPropertyEditor(DCAPipe item, PipeProperty property) {
        if (item == null || property == null) {
            throw new IllegalArgumentException("Could not initialize property with null values");
        }
        this.item = item;
        this.property = property;
        setupChildProperties();
    }

    private void setupChildProperties() {
        Config schema = property.getSchema();
        if (!matches(schema)) return;

        Optional<Config> defaultConfigOptional = pipesConfigParser.getDefaultConfig(item.getClazz());
        Config defaultConfig = null;

        if (defaultConfigOptional.isPresent()) {
            defaultConfig = defaultConfigOptional.get();
        }

        List<PipeProperty> pipeProperties = DCAPipesConfigParser.parseProperties(schema);
        for (PipeProperty pipeProperty : pipeProperties) {
            setDefaultConfig(pipeProperty, defaultConfig);
            DCAPipePropertyEditor propertyEditor = DCAPipePropertyEditorFactory.createPropertyEditor(item, pipeProperty);
            childProperties.add(propertyEditor);
        }

        initializeValue();
    }

    private void setDefaultConfig(PipeProperty childProperty, Config defaultConfig) {
        if (defaultConfig == null) {
            return;
        }

        String nestedPropertyKey = String.format("%s.%s", property.getPropertyName(), childProperty.getPropertyName());
        Object targetConfig = getConfig(nestedPropertyKey, defaultConfig);

        childProperty.setDefaultValueConfig(targetConfig);
    }

    private Object getConfig(String key, Config config) {

        for (Map.Entry<String, ConfigValue> entry : config.entrySet()) {
            if (entry.getKey().contains(key)) {
                return entry.getValue().unwrapped();
            }
        }

        return null;
    }

    protected void initializeValue() {
        JsonObject serverSideJson = item.getServerSideJson();
        if (serverSideJson != null && serverSideJson.has(property.getPropertyName())) {
            JsonElement jsonElement = serverSideJson.get(property.getPropertyName());
            try {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                for (DCAPipePropertyEditor childProperty : childProperties) {
                    String name = childProperty.getName();
                    if(jsonObject.has(name)) {
                        JsonElement element = jsonObject.get(name);
                        String string = element.getAsString();
                        System.out.println("string = " + string);
                        childProperty.setValue(string);
                    }
                }

            } catch (Exception e) {
                // don't do anything ... editor will be empty
                logger.error("Error : ", e);
            }
        }
    }

    @Override
    public Component getComponent() {
        Layout cssLayout = new CssLayout();
        cssLayout.setStyleName("object-property-editor");
        cssLayout.setWidth(100, PERCENTAGE);

        Label objectLabel = new Label(property.getPropertyName());
        objectLabel.setStyleName("object-property-editor-label");
        cssLayout.addComponent(objectLabel);

        for (DCAPipePropertyEditor childProperty : childProperties) {
            cssLayout.addComponent(childProperty.getComponent());
        }

        return cssLayout;
    }

    @Override
    public Optional<Object> getValue() {
        Map<String, Object> values = new HashMap<>();
        childProperties.forEach(editor -> {
            if (editor != null && editor.getValue().isPresent()) values.put(editor.getName(), editor.getValue().get());
        });

        return Optional.of(values);
    }

    @Override
    public String getName() {
        return property.getPropertyName();
    }

    static boolean matches(Config config) {
        if (config != null && config.hasPath("type")) {
            String type = config.getString("type");
            if ("object".equals(type)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void setValue(Object value) {
        throw new UnsupportedOperationException("Setting up object property directly is not supported yet.");
    }

    @Override
    public void addValidator(Validator validator) {
        throw new UnsupportedOperationException("Setting up object property validator is not supported yet.");
    }

    @Override
    public void setExtractor(DCAPropertyExtractor extractor) {
        throw new UnsupportedOperationException("Setting up object vale extractoris not supported yet.");
    }
}
