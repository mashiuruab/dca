package com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mashiur on 4/4/16.
 */
public class DCAAggregateItem {
    private String entityTaxon = "";
    private String clusterTaxon = "";
    private String description = "";
    private String type = "";
    private List<String> tags = new ArrayList<>();
    private String size = "";
    private DCAAggregatePopularity popularity;
    private Boolean isOverriden;

    @SerializedName("static")
    private boolean isStatic = false;

    public String getEntityTaxon() {
        return entityTaxon;
    }

    public String getClusterTaxon() {
        return clusterTaxon;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getSize() {
        return size;
    }

    public String getTagsAsString() {
        String tagString = "";
        String seperator = ",";
        int counter = 1;
        for (String string : getTags()) {
            if (counter++ == getTags().size()) {
                seperator = "";
            }
            tagString += String.format("%s%s", string, seperator);
        }
        return tagString;
    }

    public DCAAggregatePopularity getPopularity() {
        return popularity;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public void setStatic(final boolean pIsStatic) {
        isStatic = pIsStatic;
    }

    public void setDescription(final String pDescription) {
        description = pDescription;
    }

    public void setPopularity(final DCAAggregatePopularity pPopularity) {
        popularity = pPopularity;
    }

    public void setTags(final List<String> pTags) {
        tags = pTags;
    }

    public void setType(final String pType) {
        type = pType;
    }

    public void setEntityTaxon(String entityTaxon) {
        this.entityTaxon = entityTaxon;
    }

    public void setClusterTaxon(String clusterTaxon) {
        this.clusterTaxon = clusterTaxon;
    }

    public Boolean getIsOverriden() {
        return isOverriden;
    }

    public void setIsOverriden(Boolean isOverriden) {
        this.isOverriden = isOverriden;
    }
}
