package com.sannsyn.dca.vaadin.pipes.propertyeditors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sannsyn.dca.service.DCAPipesConfigParser;
import com.sannsyn.dca.service.recommender.PipeProperty;
import com.sannsyn.dca.vaadin.widgets.controller.overview.model.aggregates.DCAPipe;
import com.typesafe.config.Config;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.OptionGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;

/**
 * Created by jobaer on 3/23/17.
 */
public class DCAPipeOneOfPropertyEditor implements DCAPipePropertyEditor {
    private static final Logger logger = LoggerFactory.getLogger(DCAPipeOneOfPropertyEditor.class);

    private final List<DCAPipePropertyEditor> options = new ArrayList<>();
    private final DCAPipe pipe;
    private final PipeProperty property;
    private DCAPropertyExtractor extractor;
    private DCAPipePropertyEditor currentEditor;
    private DCAPipePropertyEditor nullEditor;
    private DCAPipePropertyEditor valueEditor;
    private OptionGroup optionGroup = new OptionGroup();

    public DCAPipeOneOfPropertyEditor(DCAPipe pipe, PipeProperty property) {
        this.pipe = pipe;
        this.property = property;

        setupOptions();
    }

    protected void initializeValue() {
        JsonObject serverSideJson = pipe.getServerSideJson();
        if (serverSideJson != null && serverSideJson.has(property.getPropertyName())) {
            JsonElement jsonElement = serverSideJson.get(property.getPropertyName());
            try {
                String value = "";
                if(jsonElement.isJsonArray()) {
                    StringBuilder sb = new StringBuilder();
                    JsonArray asJsonArray = jsonElement.getAsJsonArray();
                    for(int i=0; i<asJsonArray.size(); i++) {
                        sb.append(asJsonArray.get(i).getAsString());
                        if(i < asJsonArray.size() - 1) {
                            sb.append(", ");
                        }
                    }
                    value = sb.toString();
                } else {
                    value = jsonElement.getAsString();
                }

                valueEditor.setValue(value);
                optionGroup.setValue(valueEditor.getName());
            } catch (Exception e) {
                // don't do anything ... editor will be empty
                logger.error("Error : ", e);
            }
        } else {
            optionGroup.setValue(nullEditor.getName());
        }
    }

    private void setupOptions() {
        Config schema = property.getSchema();
        if (!matches(schema)) return;

        List<PipeProperty> properties = DCAPipesConfigParser.parseOneOfProperties(schema);
        for (PipeProperty pipeProperty : properties) {
            DCAPipePropertyEditor propertyEditor = DCAPipePropertyEditorFactory.createPropertyEditor(pipe, pipeProperty);
            if (propertyEditor instanceof DCAPipeNullPropertyEditor) {
                nullEditor = propertyEditor;
            } else {
                valueEditor = propertyEditor;
            }
            options.add(propertyEditor);
        }
    }

    @Override
    public Component getComponent() {
        CssLayout wrapper = new CssLayout();
        wrapper.addStyleName("create-recommender-form-field");
        wrapper.setWidth(100, PERCENTAGE);

        CssLayout selected = new CssLayout();
        selected.setWidth(100, PERCENTAGE);

        HashMap<String, DCAPipePropertyEditor> editorMap = new HashMap<>();
        optionGroup.addStyleName("horizontal");

        for (DCAPipePropertyEditor option : options) {
            editorMap.put(option.getName(), option);
            optionGroup.addItem(option.getName());
        }

        optionGroup.addValueChangeListener(event -> {
            Object value = event.getProperty().getValue();
            if (value != null) {
                DCAPipePropertyEditor effectiveEditor = editorMap.get(value.toString());
                currentEditor = effectiveEditor;
                selected.removeAllComponents();
                selected.addComponent(effectiveEditor.getComponent());
            }
        });
        initializeValue();

        Label label = new Label(property.getPropertyName());
        label.setWidth(30, PERCENTAGE);
        wrapper.addComponent(label);

        optionGroup.setWidth(69, PERCENTAGE);

        wrapper.addComponent(optionGroup);
        wrapper.addComponent(selected);

        return wrapper;
    }

    @Override
    public Optional<Object> getValue() {
        if (currentEditor != null) {
            return currentEditor.getValue();
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String getName() {
        return property.getPropertyName();
    }

    @Override
    public void setValue(Object value) {

    }

    @Override
    public void setExtractor(DCAPropertyExtractor extractor) {
        this.extractor = extractor;
    }

    static boolean matches(Config config) {
        return config != null && config.hasPath("oneOf");
    }
}
