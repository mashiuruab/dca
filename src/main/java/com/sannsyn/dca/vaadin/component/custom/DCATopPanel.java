package com.sannsyn.dca.vaadin.component.custom;

import com.google.common.eventbus.Subscribe;
import com.sannsyn.dca.i18n.Messages;
import com.sannsyn.dca.model.config.DCASection;
import com.sannsyn.dca.presenter.DCAAdminPresenter;
import com.sannsyn.dca.presenter.DCADashboardPresenter;
import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.component.custom.logout.DCALogOut;
import com.sannsyn.dca.vaadin.event.DCADashboardEvent;
import com.sannsyn.dca.vaadin.event.DCADashboardEventBus;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mashiur on 2/25/16.
 */
public class DCATopPanel extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCATopPanel.class);
    private static final Map<String, String> i18nKeyMap = new HashMap<String, String>() {{
        put("operations", "top.panel.menu.operations");
        put("admin", "top.panel.menu.admin");
        put("help", "top.panel.menu.help");
        put("shophelper", "top.panel.menu.shophelper");
    }};

    private List<DCASection> dcaSectionList;
    private String identifier;
    private CssLayout thinBorder = new CssLayout();

    public DCATopPanel(DCADashboardPresenter pPDCADashboardPresenter, DCAAdminPresenter adminPresenter,
                       DCALayoutContainer layoutContainer, String identifier) {
        setDashboardPresenter(pPDCADashboardPresenter);
        setAdminPresenter(adminPresenter);
        setLayoutContainer(layoutContainer);
        this.identifier = identifier;
        getLayoutContainer().setTopPanelContainer(this);

        try {
            this.dcaSectionList = getDashboardPresenter().getSections(getLoggedInUser());
            init();
            DCADashboardEventBus.register(this);
        } catch (Exception e) {
            logger.info("Error : ", e);
            this.addComponent(new DCAError("Error Happend in the DCATopPanel Component"));
        }
    }

    private void init() {
        this.setStyleName("top-panel-container global-header");

        CssLayout navContainer = new CssLayout();
        navContainer.setStyleName("nav pull-right");

        int count = 0;
        String wrapperStyleName = "top-panel-link";

        for (DCASection dcaSection : this.dcaSectionList) {
            if (++count == this.dcaSectionList.size()) {
                wrapperStyleName = "top-panel-link-last";
            }
            String itemKey = i18nKeyMap.get(dcaSection.getName());
            String itemLabel = Messages.getInstance().getMessage(itemKey);
            final Link topMenuLink = new Link(itemLabel, new ExternalResource("#!" + dcaSection.getName()));
            topMenuLink.setStyleName(wrapperStyleName);
            if (this.identifier.equals(dcaSection.getName())) {
                topMenuLink.addStyleName("selected-section");
            }
            navContainer.addComponent(topMenuLink);
        }

        DCALogOut dcaLogout = new DCALogOut(getAdminPresenter(), getLayoutContainer());

        navContainer.addComponent(dcaLogout);

        this.addComponent(navContainer);

        thinBorder.addStyleName("top-thin-border");
        this.addComponent(thinBorder);
    }

    @Subscribe
    public void handleExpandMenu(final DCADashboardEvent.MenuExpandEvent event) {
        logger.debug("Handling menu expansion from DCATopPanel.");
        thinBorder.removeStyleName("collapsed");
    }

    @Subscribe
    public void handleCollapsedMenu(final DCADashboardEvent.MenuCollapseEvent event) {
        logger.debug("Handling menu collapse from DCATopPanel.");
        thinBorder.addStyleName("collapsed");
    }
}
