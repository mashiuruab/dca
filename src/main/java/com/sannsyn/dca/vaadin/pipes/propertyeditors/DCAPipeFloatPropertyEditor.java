package com.sannsyn.dca.vaadin.pipes.propertyeditors;

import com.sannsyn.dca.service.recommender.PipeProperty;
import com.sannsyn.dca.vaadin.validators.NumberValidator;
import com.sannsyn.dca.vaadin.widgets.controller.overview.model.aggregates.DCAPipe;
import com.typesafe.config.Config;

import java.util.Optional;

import static com.sannsyn.dca.vaadin.pipes.propertyeditors.DCAPipePropertyEditorFactory.floatPropertyExtractor;

/**
 * A property editor for integer types.
 * <p>
 * Created by jobaer on 3/23/17.
 */
public class DCAPipeFloatPropertyEditor extends DCAPipeStringPropertyEditor {
    public DCAPipeFloatPropertyEditor(DCAPipe item, PipeProperty property) {
        super(item, property);
        setupFloatProperty();
    }

    public DCAPipeFloatPropertyEditor(DCAPipe item, PipeProperty property, boolean hideLabel) {
        super(item, property, hideLabel);
        setupFloatProperty();
    }

    private void setupFloatProperty() {
        addValidator(new NumberValidator());
        setExtractor(floatPropertyExtractor);
    }

    static boolean matches(Config config) {
        if (config != null && config.hasPath("type")) {
            String type = config.getString("type");
            if ("float".equals(type) || "number".equals(type)) {
                return true;
            }
        }

        return false;
    }
}
