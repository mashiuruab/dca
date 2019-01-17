package com.sannsyn.dca.vaadin.component.custom.container;

/**
 * An alternating row class name provider
 *
 * Created by jobaer on 6/9/16.
 */
public class AlternatingRowClass {
    private final String first;
    private final String second;
    private String current;

    public AlternatingRowClass(String first, String second) {
        this.first = first;
        this.second = second;
        current = first;
    }

    public String alt() {
        current = first.equals(current) ? second : first;
        return current;
    }

    public void reset() {
        current = first;
    }
}
