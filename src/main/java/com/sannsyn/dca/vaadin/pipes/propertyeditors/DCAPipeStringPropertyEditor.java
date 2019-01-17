package com.sannsyn.dca.vaadin.pipes.propertyeditors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sannsyn.dca.service.recommender.PipeProperty;
import com.sannsyn.dca.vaadin.ui.DCAUiHelper;
import com.sannsyn.dca.vaadin.widgets.controller.overview.model.aggregates.DCAPipe;
import com.typesafe.config.Config;
import com.vaadin.data.Validator;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * A property editor for string types.
 * <p>
 * Created by jobaer on 3/23/17.
 */
public class DCAPipeStringPropertyEditor implements DCAPipePropertyEditor {
    private static final Logger logger = LoggerFactory.getLogger(DCAPipeStringPropertyEditor.class);

    protected final DCAPipe item;
    protected final PipeProperty property;
    protected TextField textField;
    protected DCAPropertyExtractor extractor;
    private boolean hideLabel = false;

    public DCAPipeStringPropertyEditor(DCAPipe item, PipeProperty property) {
        if (item == null || property == null) {
            throw new IllegalArgumentException("Could not initialize property with null values");
        }
        this.item = item;
        this.property = property;
        textField = new TextField();
        //default extractor
        extractor = Optional::of;

        initializeValue();

    }

    protected void initializeValue() {
        JsonObject serverSideJson = item.getServerSideJson();
        if (serverSideJson != null && serverSideJson.has(property.getPropertyName())
                && serverSideJson.get(property.getPropertyName()).isJsonPrimitive()) {

            JsonElement jsonElement = serverSideJson.get(property.getPropertyName());
            try {
                String asString = jsonElement.getAsString();
                textField.setValue(asString);
            } catch (Exception e) {
                // don't do anything ... editor will be empty
                logger.error(String.format("Error Happened for property key = %s json Element %s",
                        property.getPropertyName(), jsonElement));
                logger.error("Error : ", e);
            }
        } else if (property.getDefaultValueConfig() != null
                && StringUtils.isNotEmpty(String.valueOf(property.getDefaultValueConfig()))) {
            textField.setInputPrompt(String.valueOf(property.getDefaultValueConfig()));
        }
    }

    public DCAPipeStringPropertyEditor(DCAPipe item, PipeProperty property, boolean hideLabel) {
        this(item, property);
        this.hideLabel = hideLabel;
    }

    @Override
    public Component getComponent() {
        if (StringUtils.isNotEmpty(property.getDescription())) {
            textField.setDescription(property.getDescription());
        }

        if (hideLabel) {
            return DCAUiHelper.wrapWithEmptyLabel(textField, property);
        } else {
            return DCAUiHelper.wrapWithLabelPipe(textField, property);
        }
    }

    @Override
    public Optional<Object> getValue() {
        if (textField.getValue() == null || extractor == null) {
            return Optional.empty();
        } else {
            return extractor.extract(textField.getValue());
        }
    }

    @Override
    public String getName() {
        return property.getPropertyName();
    }

    @Override
    public void setValue(Object value) {
        if (extractor != null) {
            Optional<Object> extract = extractor.extract(value);
            if (extract.isPresent()) {
                textField.setValue(extract.get().toString());
            } else {
                textField.setValue(value.toString());
            }
        }
    }

    @Override
    public void addValidator(Validator validator) {
        if (validator != null) {
            textField.addValidator(validator);
        }
    }

    @Override
    public void setExtractor(DCAPropertyExtractor extractor) {
        this.extractor = extractor;
    }

    static boolean matches(Config config) {
        if (config != null && config.hasPath("type")) {
            String type = config.getString("type");
            if ("string".equals(type)) {
                return true;
            }
        }

        return false;
    }
}
