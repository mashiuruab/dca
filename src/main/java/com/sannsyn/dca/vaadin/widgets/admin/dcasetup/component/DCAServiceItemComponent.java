package com.sannsyn.dca.vaadin.widgets.admin.dcasetup.component;

import com.sannsyn.dca.model.config.DCASelectedService;
import com.sannsyn.dca.presenter.DCAAdminPresenter;
import com.sannsyn.dca.vaadin.component.custom.DCAWrapper;
import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.component.custom.navigation.DCAExternalImageComponent;
import com.sannsyn.dca.vaadin.servlet.DCAUtils;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model.DCAAccount;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model.DCAAccountService;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.vaadin.event.LayoutEvents;
import com.vaadin.ui.Component;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by mashiur on 5/9/16.
 */
public class DCAServiceItemComponent extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAServiceItemComponent.class);

    private DCASelectedService selectedService;
    private String permission;

    public DCAServiceItemComponent(List<String> headers) {
        this.setStyleName("header-item");

        for(String header : headers) {
            DCALabel columnValue = new DCALabel(header, "col-value");
            this.addComponent(columnValue);
        }
    }

    public DCAServiceItemComponent(DCASelectedService service, DCAAdminPresenter adminPresenter,
                                   DCALayoutContainer layoutContainer, String permission) {
        this.selectedService = service;
        this.permission = permission;
        setLayoutContainer(layoutContainer);
        setAdminPresenter(adminPresenter);

        this.setStyleName("item");
        this.setId(service.getUuid());

        if (selectedService.getAccount() != null) {
            Observable<DCAAccount> accountObservable = getAdminPresenter().getAccount(selectedService.getAccount().getUuid());
            accountObservable.subscribe(this::onNext, this::onError);
        }

        this.addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
            @Override
            public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                if (event.getChildComponent() != null && StringUtils.isEmpty(event.getChildComponent().getId())) {
                    Observable<DCASelectedService> selectedServiceObservable = getAdminPresenter().getService(selectedService.getUuid());
                    selectedServiceObservable.subscribe(selectedService -> {
                        DCAAccountService accountService = new DCAAccountService();
                        accountService.setAccount(selectedService.getAccount());
                        accountService.setName(selectedService.getName());
                        accountService.setServiceIdentifier(selectedService.getServiceIdentifier());
                        accountService.setMetaDataServerUrl(selectedService.getMetaDataServerUrl());
                        accountService.setAnalyticsServerUrl(selectedService.getAnalyticsServerUrl());
                        accountService.setDescription(selectedService.getDescription());
                        accountService.setServiceEndpoint(selectedService.getServiceEndpoint().getEndpointAddress());
                        accountService.setStatus(selectedService.getStatus());
                        accountService.setUuid(selectedService.getUuid());

                        getLayoutContainer().getWidgetContainer().removeAllComponents();
                        DCAAddEditServiceComponent serviceComponent = new DCAAddEditServiceComponent(accountService,
                                getAdminPresenter(), getLayoutContainer(), permission, StringUtils.isNotEmpty(accountService.getUuid()));
                        addComponentAsLast(serviceComponent, getLayoutContainer().getWidgetContainer());
                    }, throwable -> {
                        onError(throwable);
                    });
                } else if (event.getChildComponent() != null && StringUtils.isNotEmpty(event.getChildComponent().getId())) {
                    DCAServiceWidget serviceWidget = new DCAServiceWidget(selectedService, getLayoutContainer(), getAdminPresenter());
                    getLayoutContainer().getWidgetContainer().removeAllComponents();
                    getLayoutContainer().getWidgetContainer().addComponent(serviceWidget);
                }
            }
        });
    }

    private void onNext(DCAAccount account) {
        try {
            DCALabel accountName = new DCALabel(account.getName(), "col-value");
            DCALabel serviceName = new DCALabel(selectedService.getName(), "col-value");
            DCALabel descriptionItem = new DCALabel(selectedService.getDescription(), "col-value");
            DCALabel statusItem = new DCALabel(selectedService.getStatus(), "col-value");

            DCALabel restrictedImage = new DCALabel("", "v-image");
            DCAWrapper restrictedImageWrapper = new DCAWrapper(Collections.singletonList(restrictedImage), "col-value restricted-img");
            restrictedImageWrapper.setId(UUID.randomUUID().toString());

            List<Component> componentList = new ArrayList<>();

            componentList.add(accountName);
            componentList.add(serviceName);
            componentList.add(descriptionItem);
            componentList.add(statusItem);
            componentList.add(restrictedImageWrapper);

            addComponentAsLast(componentList, this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void onError(Throwable throwable) {
        throwable.printStackTrace();
        logger.error(throwable.getMessage());
        addComponentAsLast(new DCAError(throwable.getMessage()), this);
    }

    @Override
    public void updateWidgetContainer(String clickedComponentId) {
    }
}
