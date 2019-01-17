package com.sannsyn.dca.vaadin.component.custom.logout;

import com.sannsyn.dca.presenter.DCAAdminPresenter;
import com.sannsyn.dca.vaadin.servlet.DCAUserPreference;
import com.sannsyn.dca.vaadin.servlet.DCAUtils;
import com.sannsyn.dca.vaadin.ui.DCAUI;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.vaadin.event.LayoutEvents;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Link;
import com.vaadin.ui.UI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mashiur on 3/24/16.
 */
public class DCALogOut extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCALogOut.class);

    private Link logoutIcon;
    private Link changePasswordLink;
    private Link userLogoutLink;
    private CssLayout currentComponent;

    public DCALogOut(DCAAdminPresenter adminPresenter, DCALayoutContainer layoutContainer) {
        currentComponent = this;
        setAdminPresenter(adminPresenter);
        setLayoutContainer(layoutContainer);

        setLogoutIcon();
        setChangePasswordLink();
        setUserLogoutLink();

        init();

        DCALogoutLayoutClickListener dcaLogoutLayoutClickListener = new DCALogoutLayoutClickListener();
        dcaLogoutLayoutClickListener.setDcaLogout(this);
        addLayoutClickListener(dcaLogoutLayoutClickListener);
    }

    private void setLogoutIcon() {
        this.logoutIcon = new Link();
        this.logoutIcon.setId("dca-logout-avater");
        this.logoutIcon.setStyleName("dropdown-toggle user-profle");

        Resource imageResource = new ExternalResource("static/img/user-avater.jpg");
        this.logoutIcon.setIcon(imageResource);

    }

    private void setChangePasswordLink() {
        this.changePasswordLink = new Link();
        this.changePasswordLink.setId("dca-change-password-id");
        this.changePasswordLink.setCaption("Change Password");
        this.changePasswordLink.setStyleName("dropdown-menu-item");
    }

    private void setUserLogoutLink() {
        this.userLogoutLink = new Link();
        this.userLogoutLink.setId("dca-logout");
        this.userLogoutLink.setCaption("Logout");
        this.userLogoutLink.setStyleName("dropdown-menu-item");
    }

    private void init() {
        this.setId("dca-logout-container-id");
        this.setStyleName("global-header-logout");
        this.addComponent(this.logoutIcon);

        final CssLayout floatLayout = new CssLayout();
        floatLayout.setStyleName("dropdown-menu");
        floatLayout.addComponent(this.changePasswordLink);
        floatLayout.addComponent(this.userLogoutLink);

        floatLayout.addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
            @Override
            public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                if(event.getChildComponent() == null) {
                    return;
                }

                if ("dca-logout".equals(event.getChildComponent().getId())) {
                    DCAUtils.removeTargetService();
                    DCAUserPreference.removeLoggedInUser();
                    UI.getCurrent().getNavigator().navigateTo(DCAUI.START_VIEW);
                } else if ("dca-change-password-id".equals(event.getChildComponent().getId())) {
                    DCAChangePasswordComponent changePasswordComponent =
                            new DCAChangePasswordComponent(getAdminPresenter(), getLayoutContainer());
                    DCAModalComponent changePWPopup = new DCAModalComponent(changePasswordComponent);
                    addComponentAsLast(changePWPopup, getLayoutContainer().getWidgetContainer());
                }
            }
        });

        this.addComponent(floatLayout);

        getLayoutContainer().getWidgetContainer().addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
            @Override
            public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                currentComponent.removeStyleName("open");
            }
        });

        getLayoutContainer().getLeftPanelContainer().addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
            @Override
            public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                currentComponent.removeStyleName("open");
            }
        });

        getLayoutContainer().getTopPanelContainer().addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
            @Override
            public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                if (event.getClickedComponent() == null) {
                    currentComponent.removeStyleName("open");
                }

                if (event.getClickedComponent() != null && !"dca-logout-avater".equals(event.getClickedComponent().getId())) {
                    currentComponent.removeStyleName("open");
                }
            }
        });
    }
}
