package com.sannsyn.dca.vaadin.widgets.controller.overview.model.aggregates;

import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;

/**
 * A representation of pipe type component
 * Created by jobaer on 5/5/16.
 */
public class DCAPipe {
    private String name;
    private String type;
    /*Class Description*/
    private String description;
    /*Instance Description*/
    private String componentDescription;
    private String clazz;
    private DCAPipeInstanceType componentType;
    private JsonObject serverSideJson;

    /*Manage Filter Widget*/
    private String typeOfFilter;

    public DCAPipe() {
//        no-arg constructor
    }

    public DCAPipe(String name, String type, String description) {
        this.name = name;
        this.type = type;
        this.description = description;
    }

    public String getName() {
        return StringUtils.stripToEmpty(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return StringUtils.stripToEmpty(description);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getComponentDescription() {
        return StringUtils.stripToEmpty(componentDescription).isEmpty() ? description : componentDescription;
    }

    public void setComponentDescription(String componentDescription) {
        this.componentDescription = componentDescription;
    }

    public void setClazz(String clazz) {

        this.clazz = clazz;
    }

    public boolean matchesNameOrClass(String query) {
        if (StringUtils.isBlank(query)) return true;
        query = query.toLowerCase();
        return name != null && name.toLowerCase().contains(query) ||
            clazz != null && clazz.toLowerCase().contains(query);
    }

    public boolean matchesType(String type) {
        if (StringUtils.isBlank(type)) return true;
        type = type.toLowerCase();
        return this.type != null && this.type.toLowerCase().equals(type);
    }

    public DCAPipeInstanceType getComponentType() {
        return componentType;
    }

    public void setComponentType(DCAPipeInstanceType componentType) {
        this.componentType = componentType;
    }

    public String getClazz() {
        return clazz;
    }

    public String getUnqualifiedClassName() {
        if (StringUtils.isBlank(clazz)) return clazz;

        int i = clazz.lastIndexOf('.');
        return clazz.substring(i + 1, clazz.length());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DCAPipe) {
            DCAPipe other = (DCAPipe) obj;
            if (clazz != null) {
                return name.equals(other.getName()) && clazz.equals(other.getClazz());
            } else {
                return name.equals(other.getName());
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (clazz != null) {
            return (name.hashCode() * 41 + clazz.hashCode());
        } else {
            return name.hashCode();
        }
    }

    @Override
    public String toString() {
        return clazz != null ? name + "[" + clazz + "]" : name;
    }

    public JsonObject getServerSideJson() {
        return serverSideJson;
    }

    public void setServerSideJson(JsonObject serverSideJson) {
        this.serverSideJson = serverSideJson;
    }

    public String getTypeOfFilter() {
        return StringUtils.stripToEmpty(typeOfFilter);
    }

    public void setTypeOfFilter(String typeOfFilter) {
        this.typeOfFilter = typeOfFilter;
    }
}