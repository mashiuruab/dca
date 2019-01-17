package com.sannsyn.dca.vaadin.validators;

import com.vaadin.data.Validator;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by jobaer on 5/30/16.
 */
public class NonEmptyValidator implements Validator {
    private final String fieldName;
    private final String msg;

    public NonEmptyValidator(String fieldName) {
        this.fieldName = fieldName;
        this.msg = fieldName + " shouldn't be empty";
    }

    @Override
    public void validate(Object value) throws InvalidValueException {
        if (value == null) throw new InvalidValueException(msg);
        String val = (String) value;
        if (StringUtils.isBlank(val)) {
            throw new InvalidValueException(msg);
        }
    }
}
