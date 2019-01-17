package com.sannsyn.dca.vaadin.widgets.admin.dcasetup.component;

import com.sannsyn.dca.presenter.DCAAdminPresenter;
import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.DCASetupAccountComponent;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model.DCAAccount;
import com.sannsyn.dca.vaadin.widgets.common.DCABreadCrumb;
import com.sannsyn.dca.vaadin.widgets.common.DCABreadCrumbImpl;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Created by mashiur on 5/9/16.
 */
public class DCAAddEditAccountComponent extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAAddEditAccountComponent.class);

    private DCAAccount account;
    private DCAAccountFormComponent dcaAccountFormComponent;
    private DCAServiceListComponent dcaServiceListComponent;
    private String permissionMode;
    private Consumer<String> selfNavigation = s -> {};
    public DCAAddEditAccountComponent(DCAAccount account, DCALayoutContainer layoutContainer,
                                      DCAAdminPresenter adminPresenter, String permissionMode,
                                      Consumer<String> selfNavigation) {
        setLayoutContainer(layoutContainer);
        setAdminPresenter(adminPresenter);
        this.account = account;
        this.permissionMode = permissionMode;
        this.selfNavigation = selfNavigation;
        this.setStyleName("edit-account-container");

        try {
            init();
        } catch (Exception e) {
            logger.error("Error : ",e);
            this.addComponent(new DCAError("Account Page Creation Error"));
        }
    }

    private void init() {
        String bcLabel = StringUtils.isEmpty(this.account.getName()) ? "Account" : this.account.getName();
        DCABreadCrumb breadCrumb = new DCABreadCrumbImpl();
        breadCrumb.addAction("DCA Setup", s -> updateWidgetContainer("dca-setup-id"));
        breadCrumb.addAction(bcLabel, selfNavigation);
        getLayoutContainer().setBreadCrumb(breadCrumb);
        this.addComponent(breadCrumb.getView());

        this.dcaAccountFormComponent = new DCAAccountFormComponent(account, getAdminPresenter(),
                getLayoutContainer(), this.permissionMode, StringUtils.isNotEmpty(account.getUuid()));
        this.dcaServiceListComponent = new DCAServiceListComponent(account, getAdminPresenter(),
                dcaAccountFormComponent, getLayoutContainer(), this.permissionMode);

        this.addComponent(dcaAccountFormComponent);
        this.addComponent(dcaServiceListComponent);
    }

    @Override
    public void updateWidgetContainer(String clickedComponentId) {
        if ("dca-setup-id".equals(clickedComponentId)) {
            getLayoutContainer().getWidgetContainer().removeAllComponents();
            getLayoutContainer().getWidgetContainer().addComponent(new DCASetupAccountComponent(getLayoutContainer(), getAdminPresenter()));
        }
    }

    public DCAAccount getAccount() {
        return account;
    }

    public String getPermissionMode() {
        return permissionMode;
    }

    public Consumer<String> getSelfNavigation() {
        return selfNavigation;
    }
}
