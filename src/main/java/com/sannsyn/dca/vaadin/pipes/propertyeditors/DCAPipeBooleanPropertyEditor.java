package com.sannsyn.dca.vaadin.pipes.propertyeditors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sannsyn.dca.service.recommender.PipeProperty;
import com.sannsyn.dca.vaadin.ui.DCAUiHelper;
import com.sannsyn.dca.vaadin.widgets.controller.overview.model.aggregates.DCAPipe;
import com.typesafe.config.Config;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Created by jobaer on 3/24/17.
 */
public class DCAPipeBooleanPropertyEditor implements DCAPipePropertyEditor {
    private static final Logger logger = LoggerFactory.getLogger(DCAPipeBooleanPropertyEditor.class);

    private final DCAPipe item;
    private final PipeProperty property;
    private CheckBox checkBox;
    private DCAPropertyExtractor extractor;
    private boolean hideLabel = false;

    public DCAPipeBooleanPropertyEditor(DCAPipe item, PipeProperty property) {
        if (item == null || property == null) {
            throw new IllegalArgumentException("Could not initialize property with null values");
        }
        this.item = item;
        this.property = property;
        checkBox = new CheckBox();
        initializeValue();
    }

    public DCAPipeBooleanPropertyEditor(DCAPipe item, PipeProperty property, boolean hideLabel) {
        this(item, property);
        this.hideLabel = hideLabel;
    }

    protected void initializeValue() {
        JsonObject serverSideJson = item.getServerSideJson();
        if (serverSideJson != null && serverSideJson.has(property.getPropertyName())) {
            JsonElement jsonElement = serverSideJson.get(property.getPropertyName());
            try {
                Boolean asString = jsonElement.getAsBoolean();
                checkBox.setValue(asString);
            } catch (Exception e) {
                // don't do anything ... editor will be empty
                logger.error("Error : ", e);
            }
        } else if (property.getDefaultValueConfig() != null) {
            checkBox.setValue(Boolean.valueOf(StringUtils.stripToEmpty(String.valueOf(property.getDefaultValueConfig()))));
        }
    }

    @Override
    public Component getComponent() {
        if (StringUtils.isNotEmpty(property.getDescription())) {
            checkBox.setDescription(property.getDescription());
        }

        if (hideLabel) {
            return DCAUiHelper.wrapWithEmptyLabel(checkBox, property);
        } else {
            return DCAUiHelper.wrapWithLabelPipe(checkBox, property);
        }
    }

    @Override
    public Optional<Object> getValue() {
        return Optional.of(checkBox.getValue());
    }

    @Override
    public String getName() {
        return property.getPropertyName();
    }

    @Override
    public void setValue(Object value) {
        boolean isChecked = "true".equals(String.valueOf(value));
        checkBox.setValue(isChecked);
    }

    static boolean matches(Config config) {
        /*Null Check to avoid error in class type com.sannsyn.aggregator.computation.ensemble.impl.transforms.FrequencyExponentialAdjust*/
        if (config!=  null && config.hasPath("type")) {
            String type = config.getString("type");
            if ("boolean".equals(type)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void setExtractor(DCAPropertyExtractor extractor) {
        this.extractor = extractor;
    }
}
