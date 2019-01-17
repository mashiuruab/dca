package com.sannsyn.dca.vaadin.widgets.admin.dcasetup.component;

import com.google.gson.Gson;
import com.sannsyn.dca.model.config.DCASelectedService;
import com.sannsyn.dca.presenter.DCAAdminPresenter;
import com.sannsyn.dca.vaadin.component.custom.DCAWrapper;
import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.component.custom.navigation.DCALeftPanelContainer;
import com.sannsyn.dca.vaadin.servlet.DCAUtils;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.component.upload.DCAUploadComponent;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model.DCAAccount;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model.DCAAccountEntity;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model.DCAResponseEntity;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAPopupErrorComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAPopupMessageComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.TextField;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

/**
 * Created by mashiur on 5/9/16.
 */
public class DCAAccountFormComponent extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAAccountFormComponent.class);

    private DCAAccountFormComponent currentComponent;
    private DCAAccount account;
    private TextField name;
    private TextField description;
    private CheckBox activeValue;
    private DCAUploadComponent binaryLogoComponent;
    private TextField logoUrl;
    private TextField mostPopularRecommender;

    private DCAAccount submitAccount;
    private DCAResponseEntity responseEntity;
    private TextField searchResultRecommender;
    private TextField popularityCountAggregate;
    private TextField presenceCheckAggregate;
    private String permission;
    private boolean readOnlyMode;


    public DCAAccountFormComponent(DCAAccount account, DCAAdminPresenter adminPresenter,
                                   DCALayoutContainer layoutContainer, String permission, boolean readOnlyMode) {
        currentComponent = this;
        setLayoutContainer(layoutContainer);
        setAdminPresenter(adminPresenter);
        this.permission = permission;
        setReadOnlyMode(readOnlyMode);

        this.setStyleName("account-form");
        this.account = account;

        renderAccountForm();
    }

    public boolean isReadOnlyMode() {
        return readOnlyMode;
    }

    public void setReadOnlyMode(boolean readOnlyMode) {
        this.readOnlyMode = readOnlyMode;
    }

    public void renderAccountForm() {
        this.removeAllComponents();
        try {
            init();
        } catch (Exception e) {
            logger.error("Error : ", e);
            this.addComponent(new DCAError("Account Form Generation Error"));
        }
    }

    private void init() {
        DCALabel accountIdLabel = new DCALabel("Account Id:", "itemLabel");
        DCALabel accountIdValue = new DCALabel(account.getUuid(), "itemValue");

        DCAWrapper accountIdItem = new DCAWrapper(Arrays.asList(accountIdLabel, accountIdValue), "item");

        name = new TextField("Name:", account.getName());
        name.setRequired(true);
        name.setReadOnly(isReadOnlyMode());
        DCAWrapper nameItem = new DCAWrapper(Arrays.asList(name), "item");

        description = new TextField("Description:", account.getDescription());
        description.setReadOnly(isReadOnlyMode());
        DCAWrapper descriptionItem = new DCAWrapper(Arrays.asList(description), "item");

        DCALabel activeLabel = new DCALabel("Active: ", "itemLabel");
        activeValue = new CheckBox("", account.getActive());
        activeValue.setStyleName("itemValue checkbox-value");
        activeValue.setReadOnly(isReadOnlyMode());
        DCAWrapper activeItem = new DCAWrapper(Arrays.asList(activeLabel, activeValue), "item");

        DCALabel logoLabel = new DCALabel("Logo:", "itemLabel", "logoLabel");
        binaryLogoComponent = new DCAUploadComponent("uploadItem", true);
        logoUrl = new TextField();
        logoUrl.setStyleName("logo-url");
        logoUrl.setValue(account.getLogoUrl());
        logoUrl.setReadOnly(isReadOnlyMode());


        DCAWrapper logoItem;

        if (isReadOnlyMode()) {
            logoItem = new DCAWrapper(Arrays.asList(logoLabel, logoUrl), "item");
        } else {
            logoItem = new DCAWrapper(Arrays.asList(logoLabel, binaryLogoComponent, logoUrl), "item");
        }

        mostPopularRecommender = new TextField("Most Popular Recommender: ", account.getMostPopularRecommender());
        mostPopularRecommender.setReadOnly(isReadOnlyMode());
        DCAWrapper popularRecommenderItem = new DCAWrapper(Collections.singletonList(mostPopularRecommender), "item");

        popularityCountAggregate = new TextField("Popularity aggregate: ", account.getPopularityCountAggregate());
        popularityCountAggregate.setReadOnly(isReadOnlyMode());
        DCAWrapper popularityCountAggregateItem = new DCAWrapper(Collections.singletonList(popularityCountAggregate), "item");

        presenceCheckAggregate = new TextField("Presence checker aggregate: ", account.getPresenceCheckAggregate());
        presenceCheckAggregate.setReadOnly(isReadOnlyMode());
        DCAWrapper presenceCheckAggregateItem = new DCAWrapper(Collections.singletonList(presenceCheckAggregate), "item");

        searchResultRecommender = new TextField("Search result recommender: ", account.getSearchResultRecommender());
        searchResultRecommender.setReadOnly(isReadOnlyMode());
        DCAWrapper searchResultRecommenderItem = new DCAWrapper(Collections.singletonList(searchResultRecommender), "item");


        if (StringUtils.isNotEmpty(account.getUuid())) {
            addComponentAsLast(accountIdItem, this);
        }
        addComponentAsLast(nameItem, this);
        addComponentAsLast(descriptionItem, this);
        addComponentAsLast(logoItem, this);
        addComponentAsLast(activeItem, this);
        addComponentAsLast(popularRecommenderItem, this);
        addComponentAsLast(popularityCountAggregateItem, this);
        addComponentAsLast(presenceCheckAggregateItem, this);
        addComponentAsLast(searchResultRecommenderItem, this);

        if (this.permission.contains("w") && !readOnlyMode) {
            Button saveButton = getSaveButton();
            addComponentAsLast(saveButton, this);
        } else if (this.permission.contains("w") && readOnlyMode) {
            Button editButton = getEditButton();
            addComponentAsLast(editButton, this);
        }
    }

    private Button getEditButton() {
        Button editButton = new Button("Edit");
        editButton.setStyleName("btn-primary");

        editButton.addClickListener(event -> {
            currentComponent.setReadOnlyMode(false);
            currentComponent.renderAccountForm();
        });
        return editButton;
    }

    private Button getSaveButton() {
        Button saveButton = new Button("Save");
        saveButton.setStyleName("btn-primary");
        saveButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (!name.isValid()) {
                    name.setRequiredError("Account Name Required");
                    return;
                }
                DCAAccountEntity accountEntity = new DCAAccountEntity();
                accountEntity.setAccount(getSubmitAccount());

                String responseString = null;

                try {
                    if (StringUtils.isEmpty(accountEntity.getAccount().getUuid())) {
                        responseString = getAdminPresenter().postEntity(accountEntity, getLoggedInUser());
                    } else {
                        responseString = getAdminPresenter().putEntity(accountEntity, getLoggedInUser());
                    }

                    if (logger.isDebugEnabled()) {
                        logger.debug(responseString);
                    }

                    Gson gson = new Gson();
                    responseEntity = gson.fromJson(responseString, DCAResponseEntity.class);
                    updateWidgetContainer("success-id");
                } catch (Exception e) {
                    logger.error("Error : ", e);
                    onFailure(getSubmitAccount());
                }
            }
        });

        return saveButton;
    }

    private DCAAccount getSubmitAccount() {
        this.submitAccount = new DCAAccount();

        if (StringUtils.isNotEmpty(account.getUuid())) {
            submitAccount.setUuid(account.getUuid());
        }

        submitAccount.setName(name.getValue());
        submitAccount.setDescription(description.getValue());
        submitAccount.setActive(activeValue.getValue());
        submitAccount.setSelectedService(account.getSelectedService());
        submitAccount.setServices(account.getServices());
        submitAccount.setLogoUrl(logoUrl.getValue());
        submitAccount.setMostPopularRecommender(mostPopularRecommender.getValue());
        submitAccount.setPopularityCountAggregate(popularityCountAggregate.getValue());
        submitAccount.setPresenceCheckAggregate(presenceCheckAggregate.getValue());
        submitAccount.setSearchResultRecommender(searchResultRecommender.getValue());
        submitAccount.setLogo(binaryLogoComponent.getBinaryImage());

        return submitAccount;
    }

    @Override
    public void updateWidgetContainer(String clickedComponentId) {
        if ("success-id".equals(clickedComponentId)) {
            String uuid = account.getUuid() == null ? responseEntity.getUuid() : account.getUuid();
            Observable<DCAAccount> accountObservable = getAdminPresenter().getAccount(uuid);
            accountObservable.subscribe(dcaAccount -> {
                onSuccess(dcaAccount, responseEntity.getStatus());
            }, this::onError);
        }
    }

    private void onSuccess(DCAAccount account, String successMessage) {
        Consumer<String> selfNav = s -> {
            System.out.println("DCAAccountFormComponent.onSuccess");
            onSuccess(account, "");
        };
        getLayoutContainer().getWidgetContainer().removeAllComponents();

        DCAAddEditAccountComponent accountComponent = new DCAAddEditAccountComponent(account, getLayoutContainer(),
                getAdminPresenter(), this.permission, selfNav);

        if (StringUtils.isNotEmpty(successMessage)) {
            successMessage = String.format("%s Successfully Stored", account.getName());
            DCAPopupMessageComponent popupMessageComponent = new DCAPopupMessageComponent("Done:", successMessage,
                    accountComponent);
            accountComponent.addComponent(popupMessageComponent);
        }

        addComponentAsLast(accountComponent, getLayoutContainer().getWidgetContainer());

        DCAUtils.removeCurrentTargetServiceForAll();
        Observable<DCASelectedService> selectedServiceObservable = DCAUtils.getTargetService();

        selectedServiceObservable.subscribe(targetService -> {
            if (getLayoutContainer().getLeftPanelContainer() instanceof DCALeftPanelContainer) {
                DCALeftPanelContainer leftPanelContainer = (DCALeftPanelContainer) getLayoutContainer().getLeftPanelContainer();
                leftPanelContainer.loadCustomerLogoComponent(targetService);
                leftPanelContainer.loadSelectedServiceInfoComponent(targetService);
            }
        }, throwable -> {
            throwable.printStackTrace();
            logger.error("Error : ", throwable);
        });
    }

    private void onFailure(DCAAccount account) {
        Consumer<String> selfNavigation = s -> {
            System.out.println("DCAAccountFormComponent.onFailure");
            onFailure(account);
        };
        getLayoutContainer().getWidgetContainer().removeAllComponents();
        DCAAddEditAccountComponent accountComponent = new DCAAddEditAccountComponent(account, getLayoutContainer(),
                getAdminPresenter(), this.permission, selfNavigation);
        String errorMessage = String.format("Failure on storing account %s", account.getName());
        DCAPopupErrorComponent errorComponent = new DCAPopupErrorComponent("Error:", errorMessage, accountComponent);
        accountComponent.addComponent(errorComponent);

        addComponentAsLast(accountComponent, getLayoutContainer().getWidgetContainer());
    }

    private void onError(Throwable throwable) {
        throwable.printStackTrace();
        logger.error("Error : ", throwable.getMessage());
        getLayoutContainer().getWidgetContainer().removeAllComponents();
        addComponentAsLast(new DCAError(String.format("Error Happened : %s", throwable.getMessage())), getLayoutContainer().getWidgetContainer());
    }
}
