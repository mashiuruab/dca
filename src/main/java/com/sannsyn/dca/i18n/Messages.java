package com.sannsyn.dca.i18n;

import com.vaadin.ui.UI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Messages {

    private final ResourceBundle messages;
    private final Locale locale;

    private static final ConcurrentHashMap<Locale, Messages> MESSAGES_MAP = new ConcurrentHashMap<>();

    private Messages(Locale locale) {
        this.locale = Objects.requireNonNull(locale);
        messages = ResourceBundle.getBundle("messages", locale, new UnicodeResourceBundleControl());
    }

    private Locale getLocale() {
        return locale;
    }

    public String getMessage(String key, Object... arguments) {
        try {
            final String pattern = messages.getString(key);
            final MessageFormat format = new MessageFormat(pattern, getLocale());
            return format.format(arguments);
        } catch (MissingResourceException ex) {
            return "!" + key;
        }
    }

    public String getMessage(String key) {
        return getMessage(key, new String[]{});
    }


    private static Messages getInstance(Locale locale) {
        if (!MESSAGES_MAP.containsKey(locale)) {
            MESSAGES_MAP.putIfAbsent(locale, new Messages(locale));
        }
        return MESSAGES_MAP.get(locale);
    }

    public static Messages getInstance() {
        UI current = UI.getCurrent();
        if (current == null) {
            throw new IllegalStateException("No UI bound to current thread");
        }
        return getInstance(current.getLocale());
    }

    private class UnicodeResourceBundleControl extends ResourceBundle.Control {
        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader,
            boolean reload) throws IllegalAccessException, InstantiationException, IOException {

            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            final URL resourceURL = loader.getResource(resourceName);
            if (resourceURL == null)
                return null;

            BufferedReader in = new BufferedReader(
                new InputStreamReader(resourceURL.openStream(), StandardCharsets.UTF_8));

            try {
                return new PropertyResourceBundle(in);
            } finally {
                in.close();
            }
        }
    }
}
