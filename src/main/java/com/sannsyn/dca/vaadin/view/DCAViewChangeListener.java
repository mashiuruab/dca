package com.sannsyn.dca.vaadin.view;

import com.sannsyn.dca.vaadin.servlet.DCAUserPreference;
import com.vaadin.navigator.ViewChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mashiur on 3/18/16.
 */
public class DCAViewChangeListener implements ViewChangeListener {
    private static final Logger logger = LoggerFactory.getLogger(DCAViewChangeListener.class);

    @Override
    public boolean beforeViewChange(final ViewChangeEvent event) {
        if (event.getOldView() instanceof DCAView) {
            if (!event.getNewView().equals(event.getOldView())) {
                DCAUserPreference.clearUIState();
            }

            DCAView oldView = (DCAView) event.getOldView();
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Clearing All widget of the View %s", oldView.getIdentifier()));
            }

            oldView.getLayoutContainer().getWidgetContainer().removeAllComponents();
        }
        return true;
    }

    @Override
    public void afterViewChange(final ViewChangeEvent event) {

    }
}
