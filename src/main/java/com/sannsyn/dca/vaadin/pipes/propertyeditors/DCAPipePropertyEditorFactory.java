package com.sannsyn.dca.vaadin.pipes.propertyeditors;

import com.sannsyn.dca.service.recommender.PipeProperty;
import com.sannsyn.dca.vaadin.widgets.controller.overview.model.aggregates.DCAPipe;
import com.typesafe.config.Config;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * A factory class for creating different type of property editors
 * <p>
 * Created by jobaer on 5/26/16.
 */
public class DCAPipePropertyEditorFactory {
    private static final List<Matcher> matchers = new ArrayList<>();

    static {
        matchers.add(matcher(DCAPipeBooleanPropertyEditor::matches, DCAPipePropertyEditorFactory::createBooleanPropertyEditor));
        matchers.add(matcher(DCAPipeIntegerPropertyEditor::matches, DCAPipePropertyEditorFactory::createIntegerPropertyEditor));
        matchers.add(matcher(DCAPipeFloatPropertyEditor::matches, DCAPipePropertyEditorFactory::createFloatPropertyEditor));
        matchers.add(matcher(DCAPipeBasicArrayPropertyEditor::matches, DCAPipePropertyEditorFactory::createStringArrayPropertyEditor));
        matchers.add(matcher(DCAPipeEnumPropertyEditor::matches, DCAPipePropertyEditorFactory::createEnumPropertyEditor));
        matchers.add(matcher(DCAPipeObjectPropertyEditor::matches, DCAPipePropertyEditorFactory::createObjectPropertyEditor));
        matchers.add(matcher(DCAPipeOneOfPropertyEditor::matches, DCAPipePropertyEditorFactory::createOneOfPropertyEditor));
        matchers.add(matcher(DCAPipeNullPropertyEditor::matches, DCAPipePropertyEditorFactory::createNullPropertyEditor));
        matchers.add(matcher(DCAPipeStringPropertyEditor::matches, DCAPipePropertyEditorFactory::createStringPropertyEditor));
    }

    static DCAPropertyExtractor integerExtractor = value -> {
        if (value == null || !(value instanceof String)) {
            return Optional.empty();
        } else {
            String intStr = (String) value;
            if(StringUtils.isBlank(intStr)) return Optional.empty();
            Integer intVal = Integer.valueOf(intStr);
            return Optional.of(intVal);
        }
    };

    static DCAPropertyExtractor floatPropertyExtractor = value -> {
        if (value == null || !(value instanceof String)) {
            return Optional.empty();
        } else {
            String floatStr = (String) value;
            if(StringUtils.isBlank(floatStr)) return Optional.empty();
            Float floatVal = Float.valueOf(floatStr);
            return Optional.of(floatVal);
        }
    };

    public static DCAPipePropertyEditor createPropertyEditor(DCAPipe item, PipeProperty property) {
        DCAPipePropertyEditor defaultEditor = createStringPropertyEditor(item, property);

        Optional<DCAPipePropertyEditor> matchingEditor = createMatchingEditor(item, property);
        return matchingEditor.orElse(defaultEditor);
    }

    private static Optional<DCAPipePropertyEditor> createMatchingEditor(DCAPipe item, PipeProperty property) {
        for (Matcher matcher : matchers) {
            if (matcher.condition.test(property.getSchema())) {
                return Optional.of(matcher.constructor.apply(item, property));
            }
        }
        return Optional.empty();
    }

    private static DCAPipePropertyEditor createStringPropertyEditor(DCAPipe item, PipeProperty property) {
        return new DCAPipeStringPropertyEditor(item, property);
    }

    private static DCAPipePropertyEditor createBooleanPropertyEditor(DCAPipe item, PipeProperty property) {
        return new DCAPipeBooleanPropertyEditor(item, property);
    }

    private static DCAPipePropertyEditor createObjectPropertyEditor(DCAPipe item, PipeProperty property) {
        return new DCAPipeObjectPropertyEditor(item, property);
    }

    private static DCAPipePropertyEditor createStringArrayPropertyEditor(DCAPipe item, PipeProperty property) {
        return new DCAPipeBasicArrayPropertyEditor(item, property);
    }

    private static DCAPipePropertyEditor createIntegerPropertyEditor(DCAPipe item, PipeProperty property) {
        return new DCAPipeIntegerPropertyEditor(item, property);
    }

    private static DCAPipePropertyEditor createFloatPropertyEditor(DCAPipe item, PipeProperty property) {
        return new DCAPipeFloatPropertyEditor(item, property);
    }

    private static DCAPipePropertyEditor createEnumPropertyEditor(DCAPipe item, PipeProperty property) {
        return new DCAPipeEnumPropertyEditor(item, property);
    }

    private static DCAPipePropertyEditor createEnumIntPropertyEditor(DCAPipe item, PipeProperty property, List<String> options) {
        DCAPipeEnumPropertyEditor dcaPipeEnumPropertyEditor = new DCAPipeEnumPropertyEditor(item, property);
        dcaPipeEnumPropertyEditor.setExtractor(integerExtractor);
        return dcaPipeEnumPropertyEditor;
    }

    private static DCAPipePropertyEditor createEnumFloatPropertyEditor(DCAPipe item, PipeProperty property, List<String> options) {
        DCAPipeEnumPropertyEditor dcaPipeEnumPropertyEditor = new DCAPipeEnumPropertyEditor(item, property);
        dcaPipeEnumPropertyEditor.setExtractor(floatPropertyExtractor);
        return dcaPipeEnumPropertyEditor;
    }

    private static DCAPipePropertyEditor createNumberPropertyEditor(DCAPipe item, PipeProperty property) {
        return new DCAPipeFloatPropertyEditor(item, property);
    }

    private static DCAPipePropertyEditor createNullPropertyEditor(DCAPipe item, PipeProperty property) {
        return new DCAPipeNullPropertyEditor(item, property);
    }

    private static DCAPipePropertyEditor createOneOfPropertyEditor(DCAPipe item, PipeProperty property) {
        return new DCAPipeOneOfPropertyEditor(item, property);
    }

    private static Matcher matcher(Predicate<Config> condition, BiFunction<DCAPipe, PipeProperty, DCAPipePropertyEditor> constructor) {
        return new Matcher(condition, constructor);
    }
}

class Matcher {
    final Predicate<Config> condition;
    final BiFunction<DCAPipe, PipeProperty, DCAPipePropertyEditor> constructor;

    Matcher(Predicate<Config> condition, BiFunction<DCAPipe, PipeProperty, DCAPipePropertyEditor> constructor) {
        this.condition = condition;
        this.constructor = constructor;
    }
}
