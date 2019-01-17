package com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates;

import com.google.gson.annotations.SerializedName;

/**
 * Created by mashiur on 5/13/16.
 */
public class DCAClassWrapper {
    @SerializedName("class")
    private String className;

    public String getClassName() {
        return className;
    }
}
