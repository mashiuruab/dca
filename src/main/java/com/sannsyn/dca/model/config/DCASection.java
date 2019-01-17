package com.sannsyn.dca.model.config;

import java.util.List;

/**
 * Created by mashiur on 3/3/16.
 */
public class DCASection {
    private String name;
    private String uuid;
    private List<DCAContainers> containers;

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }

    public List<DCAContainers> getContainers() {
        return containers;
    }
}
