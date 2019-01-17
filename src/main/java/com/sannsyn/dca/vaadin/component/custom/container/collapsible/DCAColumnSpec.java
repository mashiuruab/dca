package com.sannsyn.dca.vaadin.component.custom.container.collapsible;

/**
 * Column specification for containers.
 * <p>
 * Created by jobaer on 3/6/17.
 */
public class DCAColumnSpec {

    private final String columnName;
    private final float width;
    private final String propertyName;

    public DCAColumnSpec(String columnName, float width, String propertyName) {
        this.columnName = columnName;
        this.width = width;
        this.propertyName = propertyName;
    }

    public String getColumnName() {
        return columnName;
    }

    public float getWidth() {
        return width;
    }

    public String getPropertyName() {
        return propertyName;
    }
}
