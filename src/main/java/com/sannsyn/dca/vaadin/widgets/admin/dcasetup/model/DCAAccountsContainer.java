package com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model;

import java.util.List;

/**
 * Created by mashiur on 6/27/16.
 */
public class DCAAccountsContainer {
    private String status;
    private String count;
    private List<DCAAccount> accounts;

    public String getStatus() {
        return status;
    }

    public String getCount() {
        return count;
    }

    public List<DCAAccount> getAccounts() {
        return accounts;
    }
}
