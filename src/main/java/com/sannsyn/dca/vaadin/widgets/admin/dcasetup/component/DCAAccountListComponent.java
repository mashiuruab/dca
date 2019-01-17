package com.sannsyn.dca.vaadin.widgets.admin.dcasetup.component;

import com.google.gson.Gson;
import com.sannsyn.dca.presenter.DCAAdminPresenter;
import com.sannsyn.dca.vaadin.component.custom.field.DCATooltip;
import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.component.custom.field.DCATextField;
import com.sannsyn.dca.vaadin.component.custom.icon.DCAAddNewIcon;
import com.sannsyn.dca.vaadin.icons.SannsynIcons;
import com.sannsyn.dca.vaadin.ui.DCAUiHelper;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model.DCAAccount;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model.DCAAccountSearchEntity;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model.DCAAccountsContainer;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAPopupErrorComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.vaadin.event.LayoutEvents;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by mashiur on 5/6/16.
 */
public class DCAAccountListComponent extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAAccountListComponent.class);

    private static final List<String> HEADERS = Arrays.asList("Account name", "Description", "DCA Target");

    private CssLayout accountItemList = new CssLayout();
    private DCAAccountListComponent currentComponent;
    private DCAPopupErrorComponent popupErrorComponent;

    private DCAAccountsContainer accountsContainer;
    private String targetAccountId;
    private String permission;

    public DCAAccountListComponent(DCALayoutContainer layoutContainer, DCAAdminPresenter adminPresenter, String viewPermission) {
        setLayoutContainer(layoutContainer);
        setAdminPresenter(adminPresenter);
        this.permission = viewPermission;

        this.currentComponent = this;
        this.setStyleName("account-list-container");

        try {
            init();
        } catch (Exception e) {
            logger.error("Error : ", e);
            this.addComponent(new DCAError("Error Happened While Creating the Account List Component"));
        }
    }

    private void init() {
        DCALabel header = new DCALabel("Accounts", "header");

        this.addComponent(header);
        this.addComponent(createActionComponent());
    }

    private Component createActionComponent() {
        CssLayout actionWrapper = new CssLayout();
        actionWrapper.setStyleName("action-wrapper");

        DCATextField searchTextField = new DCATextField(SannsynIcons.SEARCH, false);
        CssLayout searchWrapper = DCAUiHelper.wrapWithCssLayout(searchTextField, "search-textfield");

        Button searchButton = new Button("Search");
        searchButton.setStyleName("btn-primary");

        searchButton.addClickListener((Button.ClickListener) event -> {
            DCAAccountSearchEntity searchEntity = new DCAAccountSearchEntity();
            searchEntity.setTextFields(String.format("%s%s", searchTextField.getValue(), "%"));

            try {
                String responseString = getAdminPresenter().postEntity(searchEntity, getLoggedInUser());

                Gson gson = new Gson();
                DCAAccountsContainer updateAccountsContainer = gson.fromJson(responseString, DCAAccountsContainer.class);

                if (updateAccountsContainer.getStatus().equals("ok")) {
                    accountsContainer = updateAccountsContainer;
                    populateAccounts();
                } else {
                    removePopupMessageComponent();
                    popupErrorComponent = new DCAPopupErrorComponent("Error:", responseString, currentComponent);
                    addComponentAsLast(popupErrorComponent, currentComponent);
                }
            } catch (Exception e) {
                logger.error("Error : ", e);
                removePopupMessageComponent();
                accountItemList.removeAllComponents();
                addComponentAsLast(new DCAError("Error Fetching the AccountList Search Result"), accountItemList);
            }
        });

        actionWrapper.addLayoutClickListener((LayoutEvents.LayoutClickListener) event -> {
            if (event.getChildComponent() != null && event.getChildComponent().getId() != null) {
                updateWidgetContainer(event.getChildComponent().getId());
            }
        });

        actionWrapper.addComponent(searchWrapper);
        actionWrapper.addComponent(searchButton);

        if (this.permission.contains("w")) {
            DCAAddNewIcon dcaAddNewIcon = new DCAAddNewIcon("add-new-account", "add-new-account-id");
            DCATooltip dcaTooltip = new DCATooltip("Add New Account", "");
            dcaAddNewIcon.addComponent(dcaTooltip);

            actionWrapper.addComponent(dcaAddNewIcon);
        }

        return actionWrapper;
    }

    private void removePopupMessageComponent() {
        if (popupErrorComponent != null) {
            currentComponent.removeComponent(popupErrorComponent);
        }
    }

    private void populateAccounts() {
        accountItemList.setStyleName("account-item-list");
        removePopupMessageComponent();
        accountItemList.removeAllComponents();

        DCAAccountItemComponent headerComponent = new DCAAccountItemComponent(HEADERS);
        accountItemList.addComponent(headerComponent);

        int counter = 1;
        for (DCAAccount accounts : this.accountsContainer.getAccounts()) {
            DCAAccountItemComponent itemComponent = new DCAAccountItemComponent(accounts,
                    getTargetAccountId().equals(accounts.getUuid()), getLayoutContainer());
            accountItemList.addComponent(itemComponent);
            if (counter++ % 2 == 0) {
                itemComponent.addStyleName("alternating-gray-color");
            }
        }

        accountItemList.addLayoutClickListener((LayoutEvents.LayoutClickListener) event -> {
            if (event.getChildComponent() != null && !"account-header-id".equals(event.getChildComponent().getId())) {
                updateWidgetContainer(event.getChildComponent().getId());
            }
        });

        addComponentAsLast(accountItemList, this);
    }

    public void onNext(DCAAccountsContainer accountsContainer) {
        try {
            this.accountsContainer = accountsContainer;
            populateAccounts();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void onError(Throwable throwable) {
        throwable.printStackTrace();
        logger.error("Error", throwable.getMessage());
        addComponentAsLast(new DCAError("Error Happened While Fetching AccountList"), this);
    }

    @Override
    public void updateWidgetContainer(String clickedComponentId) {
        Consumer<String> selfNavigation = s -> updateWidgetContainer(clickedComponentId);
        if ("add-new-account-id".equals(clickedComponentId)) {
            getLayoutContainer().getWidgetContainer().removeAllComponents();
            DCAAddEditAccountComponent dcaAddNewAccountComponent = new DCAAddEditAccountComponent(new DCAAccount() ,
                    getLayoutContainer(), getAdminPresenter(), this.permission, selfNavigation);
            getLayoutContainer().getWidgetContainer().addComponent(dcaAddNewAccountComponent);
        } else {
            rx.Observable<DCAAccount> accountObservable = getAdminPresenter().getAccount(clickedComponentId);

            accountObservable.subscribe(dcaAccount -> {
                getLayoutContainer().getWidgetContainer().removeAllComponents();
                DCAAddEditAccountComponent dcaEditAccountComponent = new DCAAddEditAccountComponent(dcaAccount,
                        getLayoutContainer(), getAdminPresenter(), this.permission, selfNavigation);
                getLayoutContainer().getWidgetContainer().addComponent(dcaEditAccountComponent);
            }, this::onError);
        }
    }

    public String getTargetAccountId() {
        return targetAccountId;
    }

    public void setTargetAccountId(String targetAccountId) {
        this.targetAccountId = targetAccountId;
    }
}
