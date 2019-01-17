package com.sannsyn.dca.vaadin.component.custom.logout;

import com.vaadin.event.LayoutEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mashiur on 3/22/16.
 */
public class DCALogoutLayoutClickListener implements LayoutEvents.LayoutClickListener {
    private static final Logger logger = LoggerFactory.getLogger(DCALogoutLayoutClickListener.class);

    private DCALogOut dcaLogout;

    public DCALogOut getDcaLogout() {
        return dcaLogout;
    }

    public void setDcaLogout(final DCALogOut pDcaLogout) {
        dcaLogout = pDcaLogout;
    }

    @Override
    public void layoutClick(final LayoutEvents.LayoutClickEvent event) {
        if (event.getChildComponent() != null && "dca-logout-avater".equals(event.getChildComponent().getId())) {
            if (dcaLogout.getStyleName().contains("open")) {
                dcaLogout.removeStyleName("open");
            } else {
                dcaLogout.addStyleName("open");
            }
        }
    }
}
