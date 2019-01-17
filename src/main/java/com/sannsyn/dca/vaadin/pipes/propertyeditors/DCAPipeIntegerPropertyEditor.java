package com.sannsyn.dca.vaadin.pipes.propertyeditors;

import com.sannsyn.dca.service.recommender.PipeProperty;
import com.sannsyn.dca.vaadin.validators.IntegerValidator;
import com.sannsyn.dca.vaadin.widgets.controller.overview.model.aggregates.DCAPipe;
import com.typesafe.config.Config;

import java.util.Optional;

import static com.sannsyn.dca.vaadin.pipes.propertyeditors.DCAPipePropertyEditorFactory.integerExtractor;

/**
 * A property editor for integer types.
 * <p>
 * Created by jobaer on 3/23/17.
 */
public class DCAPipeIntegerPropertyEditor extends DCAPipeStringPropertyEditor {
    public DCAPipeIntegerPropertyEditor(DCAPipe item, PipeProperty property) {
        super(item, property);
        setUpIntegerProperty();
    }

    public DCAPipeIntegerPropertyEditor(DCAPipe item, PipeProperty property, boolean hideLabel) {
        super(item, property, hideLabel);
        setUpIntegerProperty();
    }

    private void setUpIntegerProperty() {
        addValidator(new IntegerValidator());
        setExtractor(integerExtractor);
    }

    static boolean matches(Config config) {
        if (config != null && config.hasPath("type")) {
            String type = config.getString("type");
            if ("integer".equals(type)) {
                return true;
            }
        }

        return false;
    }
}
