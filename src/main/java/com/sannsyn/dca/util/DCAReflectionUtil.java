package com.sannsyn.dca.util;

import com.sannsyn.dca.metadata.DCAItem;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * An utility class for extracting property values from object using reflection api.
 * <p>
 * Created by jobaer on 2/13/17.
 */
public class DCAReflectionUtil {
    private static final Logger logger = LoggerFactory.getLogger(DCAReflectionUtil.class);

    public static String getItemPropertyAsString(DCAItem item, String propertyName) {
        String defaultValue = "";
        Class<? extends DCAItem> itemClass = item.getClass();
        String getterName = "get" + StringUtils.capitalize(propertyName);
        try {
            Method method = itemClass.getMethod(getterName, null);
            Object value = method.invoke(item);
            if (value != null) {
                return defaultValue + value.toString();
            }
        } catch (NoSuchMethodException e) {
            logger.debug("No property found on the object with name " + propertyName);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.debug(e.getMessage());
        }
        return defaultValue;
    }
}
