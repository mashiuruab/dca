package com.sannsyn.dca.vaadin.widgets.admin.dcasetup.component;

import com.sannsyn.dca.model.config.DCASelectedService;
import com.sannsyn.dca.presenter.DCAAdminPresenter;
import com.sannsyn.dca.util.DCAConfigProperties;
import com.sannsyn.dca.vaadin.component.custom.DCAWrapper;
import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.component.custom.navigation.DCALeftPanelContainer;
import com.sannsyn.dca.vaadin.servlet.DCAUtils;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.DCASetupAccountComponent;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model.DCAAccount;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model.DCAAccountService;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model.DCAServiceEntity;
import com.sannsyn.dca.vaadin.widgets.common.DCABreadCrumb;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAPopupErrorComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAPopupMessageComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.TextField;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;
import rx.Observable;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by mashiur on 6/29/16.
 */
public class DCAAddEditServiceComponent extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAAddEditServiceComponent.class);

    private DCAAddEditServiceComponent currentComponent;
    private DCAAccountService accountService;
    private TextField serviceIdentifier;
    private TextField name;
    private TextField serviceEndpoint;
    private TextField metaDataEndpoint;
    private TextField analyticsEndpoint;
    private String permission;
    private boolean readOnlyMode;

    public DCAAddEditServiceComponent(DCAAccountService accountService, DCAAdminPresenter adminPresenter,
                                      DCALayoutContainer layoutContainer, String permission, boolean readOnlyMode) {
        currentComponent = this;
        setAdminPresenter(adminPresenter);
        setLayoutContainer(layoutContainer);
        this.permission = permission;
        setReadOnlyMode(readOnlyMode);

        this.accountService = accountService;
        this.setStyleName("account-service-container");


        renderForm();
    }

    public boolean isReadOnlyMode() {
        return readOnlyMode;
    }

    public void setReadOnlyMode(boolean readOnlyMode) {
        this.readOnlyMode = readOnlyMode;
    }

    private boolean isValidForm() {
        if (!name.isValid()) {
            name.setRequiredError("Service Name Required");
            return false;
        }

        if (!serviceEndpoint.isValid()) {
            serviceEndpoint.setRequiredError("Service Endpoint Address Required");
            return false;
        }

        return true;
    }

    public void renderForm() {
        this.removeAllComponents();
        try {
            init();
        } catch (Exception e) {
            logger.error("Error : ", e);
            this.addComponent(new DCAError("Service Form Creation Error"));
        }
    }

    private void init() {
        String serviceLabel = StringUtils.isEmpty(this.accountService.getName()) ? "Service" : this.accountService.getName();
        DCABreadCrumb breadCrumb = getLayoutContainer().getBreadCrumb();
        if (!"Service".equals(serviceLabel)) {
            breadCrumb.addAction(serviceLabel, s -> {
                updateWidgetContainer("reload-service");
            });
        }
        addComponentAsLast(breadCrumb.getView(), this);

        CssLayout formContainer = new CssLayout();
        formContainer.setStyleName("form-container");

        serviceIdentifier = new TextField("Service Identifier: ", accountService.getServiceIdentifier());
        serviceIdentifier.setReadOnly(isReadOnlyMode());
        serviceIdentifier.setRequired(true);
        DCAWrapper serviceIdItem = new DCAWrapper(Collections.singletonList(serviceIdentifier), "service-item");

        name = new TextField("Name: ", accountService.getName());
        name.setReadOnly(isReadOnlyMode());
        DCAWrapper nameItemComponent = new DCAWrapper(Collections.singletonList(name), "service-item");

        serviceEndpoint = new TextField("Service Endpoint: ", accountService.getServiceEndpoint());
        serviceEndpoint.setReadOnly(isReadOnlyMode());
        serviceEndpoint.setRequired(true);
        DCAWrapper serviceUrlItemComponent = new DCAWrapper(Collections.singletonList(serviceEndpoint), "service-item");


        String metaDataServiceUrlValue = "";

        if (isReadOnlyMode()) {
            metaDataServiceUrlValue = StringUtils.isEmpty(accountService.getMetaDataServerUrl()) ?
                    DCAConfigProperties.getMetaDataServerUrl().get() : accountService.getMetaDataServerUrl();
        } else {
            metaDataServiceUrlValue = accountService.getMetaDataServerUrl();
        }

        metaDataEndpoint = new TextField("MetaData Endpoint: ", metaDataServiceUrlValue);
        metaDataEndpoint.setReadOnly(isReadOnlyMode());
        DCAWrapper metaDataEndpointComponent = new DCAWrapper(Collections.singletonList(metaDataEndpoint), "service-item meta-data-url");

        if (StringUtils.isEmpty(accountService.getMetaDataServerUrl())) {
            metaDataEndpoint.setInputPrompt(DCAConfigProperties.getMetaDataServerUrl().get());
        }

        String analyticsServerUrl = accountService.getAnalyticsServerUrl();
        if(StringUtils.isBlank(analyticsServerUrl)) {
            analyticsServerUrl = "";
        }
        analyticsEndpoint = new TextField("Analytics Endpoint: ", analyticsServerUrl);
        analyticsEndpoint.setReadOnly(isReadOnlyMode());
        DCAWrapper analyticsEndpointComponent = new DCAWrapper(Collections.singletonList(analyticsEndpoint), "service-item");


        formContainer.addComponent(serviceIdItem);
        formContainer.addComponent(nameItemComponent);
        formContainer.addComponent(serviceUrlItemComponent);
        formContainer.addComponent(metaDataEndpointComponent);
        formContainer.addComponent(analyticsEndpointComponent);

        if (this.permission.contains("w") && !isReadOnlyMode()) {
            Button saveButton = getSaveButton();
            Button deleteButton = getDeleteButton();

            formContainer.addComponent(deleteButton);
            formContainer.addComponent(saveButton);
        } else if (this.permission.contains("w")) {
            Button editButton = getEditButton();
            formContainer.addComponent(editButton);
        }

        addComponentAsLast(formContainer, this);
    }

    private Button getDeleteButton() {
        Button deleteButton = new Button("Delete");
        deleteButton.setStyleName("btn-primary");
        deleteButton.addClickListener(event -> {
            String serviceUUID = getSubmittedService().getUuid();

            if (StringUtils.isEmpty(serviceUUID)) {
                return;
            }

            String dialogMessage = String.format("Do you really want to delete the service %s",
                    getSubmittedService().getName());

            ConfirmDialog confirmDialog = ConfirmDialog.show(getUI(), dialogMessage, new ConfirmDialog.Listener() {
                @Override
                public void onClose(ConfirmDialog dialog) {
                    if (dialog.isConfirmed()) {
                        Observable<Response> responseObservable = getAdminPresenter().deleteService(serviceUUID, getLoggedInUser());

                        responseObservable.subscribe(response -> {
                            String responseMessage = response.readEntity(String.class);
                            DCAPopupMessageComponent successMessageComponent = new DCAPopupMessageComponent("SUCCESS: ",
                                    responseMessage, getLayoutContainer().getWidgetContainer());

                            getLayoutContainer().getWidgetContainer().removeAllComponents();
                            DCASetupAccountComponent setupAccountComponent = new DCASetupAccountComponent(getLayoutContainer(),
                                    getAdminPresenter());

                            List<Component> componentList = new ArrayList<>();
                            componentList.add(setupAccountComponent);
                            componentList.add(successMessageComponent);
                            addComponentAsLast(componentList, getLayoutContainer().getWidgetContainer());

                        }, throwable -> {
                            onError(throwable);
                        });
                    }
                }
            });

            confirmDialog.getOkButton().setStyleName("btn-primary");
            confirmDialog.getCancelButton().setStyleName("btn-primary");
            confirmDialog.setCaption("");

        });

        return deleteButton;
    }

    private Button getEditButton() {
        Button editButton = new Button("Edit");
        editButton.setStyleName("btn-primary");
        editButton.addClickListener(event -> {
            currentComponent.setReadOnlyMode(false);
            currentComponent.renderForm();
        });

        return editButton;
    }

    private Button getSaveButton() {
        Button saveButton = new Button("Save");
        saveButton.setStyleName("btn-primary save");
        saveButton.setDisableOnClick(true);
        saveButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                logger.info(String.format("%s-%s", name.getValue(), serviceEndpoint.getValue()));

                if (!isValidForm()) {
                    return;
                }

                DCAAccountService submittedService = getSubmittedService();
                DCAServiceEntity entity = new DCAServiceEntity();
                entity.setService(submittedService);

                try {
                    validateEndPoints();
                    String responseString;
                    if (StringUtils.isNotEmpty(submittedService.getUuid())) {
                        responseString = getAdminPresenter().putEntity(entity, getLoggedInUser());
                    } else {
                        responseString = getAdminPresenter().postEntity(entity, getLoggedInUser());
                    }
                    logger.info(responseString);
                    updateWidgetContainer("accountService-success-id");
                } catch (Exception e) {
                    onError(e);
                } finally {
                    saveButton.setEnabled(true);
                }
            }
        });

        return saveButton;
    }

    private void validateEndPoints() {
        try {
            handleHEADRequest(getSubmittedService().getServiceEndpoint());
        } catch (Exception e) {
            throw e;
        }
    }

    private void handleHEADRequest(String endPoint) {
        if (StringUtils.isEmpty(endPoint)) {
            return;
        }

        try {
            Response response = getAdminPresenter().doHEADRequest(endPoint, getLoggedInUser()).toBlocking().first();

            if (response.getStatus() != 200) {
                throw new RuntimeException(response.readEntity(String.class));
            }
        } catch (Exception e) {
            String customMessage = String.format("EndPoint Url : \"%s\" not reachable.<br/>", endPoint);
            throw new RuntimeException(String.format("%s%s", customMessage, e.getMessage()));
        }

    }

    private void onError(Throwable throwable) {
        logger.error("Error : ", throwable);
        DCAPopupErrorComponent errorComponent = new DCAPopupErrorComponent("ERROR: ", throwable.getMessage(),
                getLayoutContainer().getWidgetContainer());
        addComponentAsLast(errorComponent, getLayoutContainer().getWidgetContainer());
    }

    private DCAAccountService getSubmittedService() {
        String accountId = accountService.getAccount().getUuid();

        DCAAccountService accountService = this.accountService;
        accountService.setServiceIdentifier(serviceIdentifier.getValue());
        accountService.setName(name.getValue());
        accountService.setServiceEndpoint(serviceEndpoint.getValue());
        accountService.setMetaDataServerUrl(metaDataEndpoint.getValue());
        accountService.setAnalyticsServerUrl(analyticsEndpoint.getValue());
        accountService.setAccount(new DCAAccount());
        accountService.getAccount().setUuid(accountId);

        return accountService;
    }

    @Override
    public void updateWidgetContainer(String clickedComponentId) {
        if ("dca-setup-id".equals(clickedComponentId)) {
            getLayoutContainer().getWidgetContainer().removeAllComponents();
            getLayoutContainer().getWidgetContainer().addComponent(
                new DCASetupAccountComponent(getLayoutContainer(), getAdminPresenter()));
        } else if ("accountService-success-id".equals(clickedComponentId)) {
            getLayoutContainer().getWidgetContainer().removeAllComponents();
            DCAAccountService service = getSubmittedService();
            DCAAddEditServiceComponent serviceComponent = new DCAAddEditServiceComponent(service, getAdminPresenter(),
                getLayoutContainer(), this.permission, isReadOnlyMode());
            String successMessage = String.format("SuccessFully Stored the Service %s", service.getName());
            DCAPopupMessageComponent successMessageComponent = new DCAPopupMessageComponent("Done:", successMessage, serviceComponent);
            serviceComponent.addComponent(successMessageComponent);
            addComponentAsLast(serviceComponent, getLayoutContainer().getWidgetContainer());

            DCAUtils.removeCurrentTargetServiceForAll();
            Observable<DCASelectedService> selectedServiceObservable = DCAUtils.getTargetService();

            selectedServiceObservable.subscribe(selectedService -> {
                if (getLayoutContainer().getLeftPanelContainer() instanceof DCALeftPanelContainer) {
                    DCALeftPanelContainer leftPanelContainer = (DCALeftPanelContainer) getLayoutContainer().getLeftPanelContainer();
                    leftPanelContainer.loadSelectedServiceInfoComponent(selectedService);
                }
            }, this::onError);

        } else if ("reload-service".equals(clickedComponentId)) {
            if (StringUtils.isEmpty(accountService.getUuid())) {
                return;
            }
            getLayoutContainer().getWidgetContainer().removeAllComponents();

            Observable<DCASelectedService> selectedServiceObservable = getAdminPresenter().getService(accountService.getUuid());

            selectedServiceObservable.subscribe(dcaSelectedService -> {
                DCAAccountService latestAccountService = accountService;
                latestAccountService.setName(dcaSelectedService.getName());
                latestAccountService.setDescription(dcaSelectedService.getDescription());
                latestAccountService.setServiceEndpoint(dcaSelectedService.getServiceEndpoint().getEndpointAddress());
                latestAccountService.setServiceIdentifier(dcaSelectedService.getServiceIdentifier());
                latestAccountService.setMetaDataServerUrl(dcaSelectedService.getMetaDataServerUrl());
                latestAccountService.setAnalyticsServerUrl(dcaSelectedService.getAnalyticsServerUrl());
                latestAccountService.setStatus(dcaSelectedService.getStatus());

                DCAAddEditServiceComponent latestServiceComponent = new DCAAddEditServiceComponent(latestAccountService,
                        getAdminPresenter(), getLayoutContainer(), permission, isReadOnlyMode());
                addComponentAsLast(latestServiceComponent, getLayoutContainer().getWidgetContainer());

            }, throwable -> {
                logger.error("Error : ", throwable);
                DCAError errorComponent = new DCAError(String.format("Error Happened While reloading the service %s, the error is  %s",
                        accountService.getName(), throwable.getMessage()));
                addComponentAsLast(errorComponent, getLayoutContainer().getWidgetContainer());
            });
        }
    }
}
