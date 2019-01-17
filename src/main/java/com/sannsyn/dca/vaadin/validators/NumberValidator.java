package com.sannsyn.dca.vaadin.validators;

import com.vaadin.data.Validator;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by jobaer on 5/31/16.
 */
public class NumberValidator implements Validator {
    @Override
    public void validate(Object value) throws InvalidValueException {
        if(value != null) {
            try {
                String stringVal = value.toString();
                if(StringUtils.isBlank(stringVal)) {
                    return;
                }

                String strVal = value.toString();
                Double.parseDouble(strVal.trim());
            } catch (NumberFormatException nfe) {
                throw new InvalidValueException("Please enter an number value");
            }
        }
    }
}
