package com.sannsyn.dca.vaadin.view;

import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.vaadin.servlet.DCAUserPreference;
import com.vaadin.ui.CssLayout;

/**
 * Created by mashiur on 7/18/16.
 */
public class DCALayout extends CssLayout {
    public DCAUser getLoggedInUser() {
        return DCAUserPreference.getLoggedInUser();
    }
}
