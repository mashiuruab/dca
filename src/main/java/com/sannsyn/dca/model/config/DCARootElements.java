package com.sannsyn.dca.model.config;

import java.util.List;

/**
 * Created by mashiur on 3/3/16.
 */
public class DCARootElements {
    private String uuid;
    private String accountIcon;
    private List<DCASection> sections;
    private DCASettings settings;

    public String getUuid() {
        return uuid;
    }

    public String getAccountIcon() {
        return accountIcon;
    }

    public List<DCASection> getSections() {
        return sections;
    }

    public DCASettings getSettings() {
        return settings;
    }
}
