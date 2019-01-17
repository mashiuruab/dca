package com.sannsyn.dca.vaadin.pipes.propertyeditors;

import com.vaadin.data.Validator;
import com.vaadin.ui.Component;

import java.util.Optional;

/**
 * A single Property Editor for a recommender component
 * Created by jobaer on 5/26/16.
 */
public interface DCAPipePropertyEditor {
    /**
     * Return the ui object representing this property editor.
     *
     * @return the ui object for this property
     */
    Component getComponent();

    /**
     * Returns the value (before or after edited by user) for this property.
     *
     * @return An object representing the value of the property editor
     */
    Optional<Object> getValue();

    String getName();

    void setValue(Object value);

    void setExtractor(DCAPropertyExtractor extractor);

    default void addValidator(Validator validator) {};

    default boolean isValid() {
        return true;
    };
}
