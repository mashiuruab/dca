package com.sannsyn.dca.vaadin.component.custom.logout.model;

/**
 * Created by mashiur on 11/24/16.
 */
public class DCAChangePassword {
    private String username;
    private String currentpassword;
    private String newpassword;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCurrentpassword() {
        return currentpassword;
    }

    public void setCurrentpassword(String currentpassword) {
        this.currentpassword = currentpassword;
    }

    public String getNewpassword() {
        return newpassword;
    }

    public void setNewpassword(String newpassword) {
        this.newpassword = newpassword;
    }
}
