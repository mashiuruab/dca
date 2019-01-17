package com.sannsyn.dca.vaadin.pipes.propertyeditors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sannsyn.dca.service.recommender.PipeProperty;
import com.sannsyn.dca.vaadin.ui.DCAUiHelper;
import com.sannsyn.dca.vaadin.widgets.controller.overview.model.aggregates.DCAPipe;
import com.typesafe.config.Config;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A property editor for string types.
 * <p>
 * Created by jobaer on 3/23/17.
 */
public class DCAPipeEnumPropertyEditor implements DCAPipePropertyEditor {
    private static final Logger logger = LoggerFactory.getLogger(DCAPipeEnumPropertyEditor.class);

    private final DCAPipe item;
    private final PipeProperty property;
    private ComboBox comboBox;
    private DCAPropertyExtractor extractor;
    private boolean hideLabel = false;

    public DCAPipeEnumPropertyEditor(DCAPipe item, PipeProperty property) {
        if (item == null || property == null) {
            throw new IllegalArgumentException("Could not initialize property with null values");
        }
        this.item = item;
        this.property = property;
        comboBox = new ComboBox();

        List<String> options = setupOptions(property);
        for (String option : options) {
            comboBox.addItem(option);
        }

        initializeValue();
    }

    private void initializeValue() {
        JsonObject serverSideJson = item.getServerSideJson();
        if (serverSideJson != null && serverSideJson.has(property.getPropertyName())) {
            JsonElement jsonElement = serverSideJson.get(property.getPropertyName());
            try {
                String asString = jsonElement.getAsString();
                comboBox.setValue(asString);
            } catch (Exception e) {
                // don't do anything ... editor will be empty
                logger.error("Error : ", e);
            }
        } else if (property.getDefaultValueConfig() != null && StringUtils.isNotEmpty(String.valueOf(property.getDefaultValueConfig()))) {
            comboBox.setInputPrompt(String.valueOf(property.getDefaultValueConfig()));
        }
    }

    private List<String> setupOptions(PipeProperty property) {
        List<String> options = new ArrayList<>();
        Config schema = property.getSchema();
        if (schema.hasPath("enum")) {
            List<String> anEnum = schema.getStringList("enum");
            for (String s : anEnum) {
                options.add(s);
            }
        }
        return options;
    }

    public DCAPipeEnumPropertyEditor(DCAPipe item, PipeProperty property, List<String> options, boolean hideLabel) {
        this(item, property);
        this.hideLabel = hideLabel;
    }

    @Override
    public Component getComponent() {
        if (StringUtils.isNotEmpty(property.getDescription())) {
            comboBox.setDescription(property.getDescription());
        }

        if (hideLabel) {
            return DCAUiHelper.wrapWithEmptyLabel(comboBox, property);
        } else {
            return DCAUiHelper.wrapWithLabelPipe(comboBox, property);
        }
    }

    @Override
    public Optional<Object> getValue() {
        if (comboBox.getValue() == null) {
            return Optional.empty();
        } else if (extractor != null) {
            return extractor.extract(comboBox.getValue());
        } else {
            return Optional.of(comboBox.getValue());
        }
    }

    @Override
    public String getName() {
        return property.getPropertyName();
    }

    @Override
    public void setValue(Object value) {
        if (value != null) {
            comboBox.setValue(value);
        }
    }

    @Override
    public void setExtractor(DCAPropertyExtractor extractor) {
        this.extractor = extractor;
    }

    static boolean matches(Config config) {
        return config != null && config.hasPath("enum");
    }
}
