package com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by mashiur on 1/3/17.
 */
public class DCAExternal {
    private Map<String, Map<String, Object>> dca = new HashMap<>();

    public Optional<Map<String, Map<String, Object>>> getDca() {
        return Optional.ofNullable(dca);
    }
}
