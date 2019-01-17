package com.sannsyn.dca.model.config;

/**
 * Created by mashiur on 3/3/16.
 */
public class DCAConfigWrapper {
    private String uuid;
    private DCARootElements root;

    public DCARootElements getRoot() {
        return root;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setRoot(DCARootElements root) {
        this.root = root;
    }
}
