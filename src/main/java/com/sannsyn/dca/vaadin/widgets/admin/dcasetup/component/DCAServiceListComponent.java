package com.sannsyn.dca.vaadin.widgets.admin.dcasetup.component;

import com.sannsyn.dca.model.config.DCAConfigEntity;
import com.sannsyn.dca.model.config.DCAConfigWrapper;
import com.sannsyn.dca.model.config.DCASelectedService;
import com.sannsyn.dca.presenter.DCAAdminPresenter;
import com.sannsyn.dca.vaadin.component.custom.DCAWrapper;
import com.sannsyn.dca.vaadin.component.custom.field.DCATooltip;
import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.component.custom.icon.DCAAddNewIcon;
import com.sannsyn.dca.vaadin.component.custom.navigation.DCALeftPanelContainer;
import com.sannsyn.dca.vaadin.servlet.DCAUtils;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model.DCAAccount;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model.DCAAccountService;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAPopupErrorComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAPopupMessageComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.vaadin.event.LayoutEvents;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.DragAndDropWrapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;
import rx.Observable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mashiur on 5/9/16.
 */
public class DCAServiceListComponent extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAServiceListComponent.class);
    private List<String> headers = Arrays.asList("Account Name", "Service Name", "Description", "Status", "");

    private DCALabel emptyServiceMessage = new DCALabel("<span>TO SELECT NEW TARGET SERVICE FOR THE DCA, DROP IT HERE</span>", "target-service-empty-msg");
    private DCALabel replaceServiceMessage = new DCALabel("<span>TO REPLACE TARGET SERVICE DROP IT HERE</span>", "target-service-empty-msg");
    private CssLayout targetContainer = new CssLayout();

    private DCAAccountFormComponent  accountFormComponent;
    private DCAAccount account;
    private DCASelectedService targetService;
    private DCAServiceListComponent currentComponent;
    private String permission;

    public DCAServiceListComponent(DCAAccount account, DCAAdminPresenter adminPresenter,
                                   DCAAccountFormComponent accountFormComponent, DCALayoutContainer layoutContainer, String permission) {
        currentComponent = this;
        this.permission = permission;

        setLayoutContainer(layoutContainer);
        setAdminPresenter(adminPresenter);

        this.accountFormComponent = accountFormComponent;
        this.account = account;
        this.setStyleName("service-list-container");

        Observable<DCASelectedService> selectedServiceObservable = DCAUtils.getTargetService();
        selectedServiceObservable.subscribe(this::onNext, this::onError);

    }

    private void setTargetContainer(DCASelectedService selectedService) {
        this.targetService = selectedService;

        this.targetContainer.removeAllComponents();
        this.targetContainer.addComponent(new DCAServiceItemComponent(headers));
        this.targetContainer.addComponent(new DCAServiceItemComponent(selectedService, getAdminPresenter(),
                getLayoutContainer(), this.permission));
        this.targetContainer.addComponent(replaceServiceMessage);
    }

    private void onError(Throwable throwable) {
        throwable.printStackTrace();
        logger.error("Error : ", throwable);
        addComponentAsLast(new DCAError("Service List Generation Error"), currentComponent);
    }

    private void saveSelectedService(DCASelectedService droppedService) {
        if (droppedService == null) {
            throw new RuntimeException("Selected Service  could not be null");
        }

        Observable<DCAConfigEntity> configEntityObservable = getAdminPresenter().getUserConfiguration(getLoggedInUser());

        configEntityObservable.subscribe(dcaConfigEntity -> {
            DCAConfigWrapper configEntity = new DCAConfigWrapper();
            configEntity.setUuid(dcaConfigEntity.getPADCAConfiguration().getUuid());
            configEntity.setRoot(dcaConfigEntity.getPADCAConfiguration().getRoot());
            configEntity.getRoot().getSettings().setService(droppedService);

            try {
                String responseString =  getAdminPresenter().putEntity(configEntity, getLoggedInUser());

                if (logger.isDebugEnabled()) {
                    logger.debug(responseString);
                }

                DCAUtils.removeTargetService();
                Observable<DCASelectedService> selectedServiceObservable = DCAUtils.getTargetService();

                selectedServiceObservable.subscribe(selectedService -> {
                    this.targetService = selectedService;
                    updateWidgetContainer("success-id");
                });
            } catch (Exception e) {
                logger.error("Error : ", e);
                updateWidgetContainer("failure-id");
            }
        });
    }

    private void onNext(DCASelectedService selectedService) {
        List<Component> componentList = new ArrayList<>();

        DCALabel targetHeader = new DCALabel("DCA Target Service", "target-header dca-widget-title");
        this.targetContainer.setStyleName("drop-target");

        if (StringUtils.stripToEmpty(account.getUuid()).equals(selectedService.getAccount().getUuid())) {
            setTargetContainer(selectedService);
        } else {
            this.targetContainer.removeAllComponents();
            this.targetContainer.addComponent(emptyServiceMessage);
        }

        DragAndDropWrapper targetContainerWrapper = new DragAndDropWrapper(this.targetContainer);
        targetContainerWrapper.setStyleName("drop-target-wrapper");

        if (this.permission.contains("w")) {
            targetContainerWrapper.setDropHandler(new DropHandler() {
                @Override
                public void drop(DragAndDropEvent event) {
                    DragAndDropWrapper.WrapperTransferable transferable =
                            (DragAndDropWrapper.WrapperTransferable) event.getTransferable();

                    String uuid = StringUtils.stripToEmpty(transferable.getSourceComponent().getId());
                    DCASelectedService droppedService = getTargetService(uuid);
                    String dialogMessage = String.format("Are you sure you want to switch to %s service", droppedService.getName());

                    ConfirmDialog confirmDialog = ConfirmDialog.show(getUI(), dialogMessage, new ConfirmDialog.Listener() {
                        public void onClose(ConfirmDialog dialog) {
                            if (dialog.isConfirmed()) {
                                saveSelectedService(droppedService);
                            }
                        }
                    });
                    confirmDialog.getOkButton().setStyleName("btn-primary");
                    confirmDialog.getCancelButton().setStyleName("btn-primary");
                    confirmDialog.setCaption("");
                }

                @Override
                public AcceptCriterion getAcceptCriterion() {
                    return AcceptAll.get();
                }
            });
        }

        componentList.add(targetHeader);
        componentList.add(targetContainerWrapper);


        DCALabel headerLabel = new DCALabel("Available Services", "header dca-widget-title");

        List<Component> headerComponentList = new ArrayList<>();

        headerComponentList.add(headerLabel);

        if (this.permission.contains("w")) {
            DCAAddNewIcon addNewServiceComponent = new DCAAddNewIcon("add-new-service", "add-new-service-id");
            DCATooltip dcaTooltip = new DCATooltip("Add New Service", "add-new-service");
            addNewServiceComponent.addComponent(dcaTooltip);
            headerComponentList.add(addNewServiceComponent);
        }


        DCAWrapper headerContainer = new DCAWrapper(headerComponentList, "header-container");
        componentList.add(headerContainer);

        headerContainer.addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
            @Override
            public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                if (event.getChildComponent() != null && "add-new-service-id".equals(event.getChildComponent().getId())) {
                    updateWidgetContainer(event.getChildComponent().getId());
                }
            }
        });

        DCAServiceItemComponent listHeaderComponent = new DCAServiceItemComponent(headers);
        this.addComponent(listHeaderComponent);
        componentList.add(listHeaderComponent);

        if (this.account.getServices() != null) {
            int counter = 1;
            for (DCASelectedService dcaService : this.account.getServices()) {
                DCAServiceItemComponent itemComponent = new DCAServiceItemComponent(dcaService, getAdminPresenter(),
                        getLayoutContainer(), this.permission);
                DragAndDropWrapper itemWrapperComponent = new DragAndDropWrapper(itemComponent);
                itemWrapperComponent.setId(dcaService.getUuid());
                itemWrapperComponent.setDragStartMode(DragAndDropWrapper.DragStartMode.COMPONENT);
                componentList.add(itemWrapperComponent);

                if (counter++ % 2 == 0) {
                    itemComponent.addStyleName("alternating-gray-color");
                }
            }
        }

        addComponentAsLast(componentList, this);
    }

    private DCASelectedService getTargetService(String uuid) {
        DCASelectedService targetService = new DCASelectedService();
        if (account.getServices() != null) {
            for (DCASelectedService selectedService : account.getServices()) {
                if (uuid.equals(selectedService.getUuid())) {
                    targetService.setServiceId(selectedService.getUuid());
                    targetService.setName(selectedService.getName());
                    return targetService;
                }
            }
        }

        return targetService;
    }

    @Override
    public void updateWidgetContainer(String clickedComponentId) {
        getLayoutContainer().getWidgetContainer().removeAllComponents();
        if ("success-id".equals(clickedComponentId)) {
            DCAAddEditAccountComponent accountComponent = new DCAAddEditAccountComponent(account, getLayoutContainer(),
                    getAdminPresenter(), this.permission, s -> {
                System.out.println("DCAServiceListComponent.updateWidgetContainer");
            });
            String successMessage = String.format("%s service is added  as target service to account %s",
                    targetService.getName(), account.getName());
            DCAPopupMessageComponent popupMessageComponent = new DCAPopupMessageComponent("Done:", successMessage, accountComponent);
            accountComponent.addComponent(popupMessageComponent);
            addComponentAsLast(accountComponent, getLayoutContainer().getWidgetContainer());

            if (getLayoutContainer().getLeftPanelContainer() instanceof DCALeftPanelContainer) {
                DCALeftPanelContainer leftPanelContainer = (DCALeftPanelContainer) getLayoutContainer().getLeftPanelContainer();
                leftPanelContainer.loadCustomerLogoComponent(targetService);
                leftPanelContainer.loadSelectedServiceInfoComponent(targetService);
            }

        } else if ("failure-id".equals(clickedComponentId)) {
            DCAAddEditAccountComponent accountComponent = new DCAAddEditAccountComponent(account, getLayoutContainer(),
                    getAdminPresenter(), this.permission, s -> {
                System.out.println("DCAServiceListComponent.updateWidgetContainer 2");
            });
            String failureMessage = String.format("Failed in setting the target service %s", targetService.getName());
            DCAPopupErrorComponent errorComponent = new DCAPopupErrorComponent("Error:", failureMessage, accountComponent);
            accountComponent.addComponent(errorComponent);
            addComponentAsLast(accountComponent, getLayoutContainer().getWidgetContainer());
        } else if ("add-new-service-id".equals(clickedComponentId)) {
            DCAAccountService service = new DCAAccountService();
            service.setAccount(account);

            DCAAddEditServiceComponent serviceComponent = new DCAAddEditServiceComponent(service, getAdminPresenter(),
                    getLayoutContainer(), this.permission, StringUtils.isNotEmpty(service.getUuid()));
            getLayoutContainer().getWidgetContainer().addComponent(serviceComponent);
        }

    }
}
