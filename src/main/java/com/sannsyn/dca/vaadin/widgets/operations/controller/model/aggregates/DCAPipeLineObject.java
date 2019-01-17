package com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates;

import com.google.gson.annotations.SerializedName;

/**
 * Created by mashiur on 5/20/16.
 */
public class DCAPipeLineObject {
    private String className;
    private String type;

    public String getClassName() {
        return className;
    }


    public void setClassName(String className) {
        this.className = className;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return String.format("className = %s, type = %s", getClassName(), getType());
    }
}
