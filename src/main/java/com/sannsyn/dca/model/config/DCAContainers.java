package com.sannsyn.dca.model.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mashiur on 3/3/16.
 */
public class DCAContainers {
    private String name;
    private List<DCAWidget> widgets = new ArrayList<>();
    private List<DCAContainers> submenu = new ArrayList<>();

    public String getName() {
        return name;
    }

    public List<DCAWidget> getWidgets() {
        return widgets;
    }

    public List<DCAContainers> getSubmenu() {
        return submenu;
    }
}
