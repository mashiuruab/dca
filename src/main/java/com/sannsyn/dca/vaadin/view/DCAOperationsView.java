package com.sannsyn.dca.vaadin.view;

import com.sannsyn.dca.presenter.DCAAdminPresenter;
import com.sannsyn.dca.presenter.DCADashboardPresenter;
import com.sannsyn.dca.vaadin.component.custom.DCATopPanel;
import com.sannsyn.dca.vaadin.component.custom.navigation.DCALeftPanelContainer;
import com.sannsyn.dca.vaadin.component.custom.navigation.DCAWidgetGenerator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.CssLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mashiur on 2/24/16.
 */
public class DCAOperationsView extends DCAView {
    private static final Logger logger = LoggerFactory.getLogger(DCAOperationsView.class);

    public DCAOperationsView(String pIdentifier) {
        super();
        setIdentifier(pIdentifier);
    }
}
