package com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.info;

import com.google.gson.annotations.SerializedName;

/**
 * Created by mashiur on 4/27/16.
 */
public class DCAAggregateInfoEntity {
    @SerializedName("pop")
    private String popularity;
    private String size;
    private String count;
    private String externalId;
    private String id;

    public String getPopularity() {
        return popularity;
    }

    public String getSize() {
        return size;
    }

    public String getCount() {
        return count;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getId() {
        return id;
    }
}
