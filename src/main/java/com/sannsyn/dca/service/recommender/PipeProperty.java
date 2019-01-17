package com.sannsyn.dca.service.recommender;

import com.typesafe.config.Config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a specific property of a recommender component.
 * <p>
 * Created by jobaer on 5/11/16.
 */
public class PipeProperty {
    private String propertyName;
    private String description;
    private PipeProperty parent;
    private Object defaultValueConfig;
    private List<String> enumValues = new ArrayList<>();
    private List<PipeProperty> nestedProperties = new ArrayList<>();
    private Config schema;

    public List<String> getEnumValues() {
        return Collections.unmodifiableList(enumValues);
    }

    public void addEnumValues(List<String> enumValues) {
        this.enumValues.addAll(enumValues);
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addNestedProperties(List<PipeProperty> properties) {
        properties.forEach(property -> property.setParent(this));
        nestedProperties.addAll(properties);
    }

    public List<PipeProperty> getNestedProperties() {
        return Collections.unmodifiableList(nestedProperties);
    }

    public PipeProperty getParent() {
        return parent;
    }

    public boolean hasParent() {
        return parent != null;
    }

    public void setParent(PipeProperty parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        return "PipeProperty{" +
            "propertyName='" + propertyName + '\'' +
            ", schema =" + schema +
            ", defaultValueConfig = " + defaultValueConfig +
            '}';
    }

    public Config getSchema() {
        return schema;
    }

    public void setSchema(Config schema) {
        this.schema = schema;
    }

    public Object getDefaultValueConfig() {
        return defaultValueConfig;
    }

    public void setDefaultValueConfig(Object defaultValueConfig) {
        this.defaultValueConfig = defaultValueConfig;
    }
}
