package com.sannsyn.dca.vaadin.widgets.admin.dcasetup;

import com.sannsyn.dca.model.config.DCASelectedService;
import com.sannsyn.dca.model.config.DCAWidget;
import com.sannsyn.dca.presenter.DCAAdminPresenter;
import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.servlet.DCAUserPreference;
import com.sannsyn.dca.vaadin.servlet.DCAUtils;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.component.DCAAccountListComponent;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.component.DCAAddEditAccountComponent;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.component.DCATargetComponent;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model.DCAAccountsContainer;
import com.sannsyn.dca.vaadin.widgets.common.DCABreadCrumb;
import com.sannsyn.dca.vaadin.widgets.common.DCABreadCrumbImpl;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by mashiur on 5/6/16.
 */
public class DCASetupAccountComponent extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCASetupAccountComponent.class);

    private DCATargetComponent targetComponentContainer;
    private DCAAccountListComponent dcaAccountListComponent;
    private DCABreadCrumb breadCrumb = new DCABreadCrumbImpl();

    private Observable<DCAAccountsContainer> accountsContainerObservable;
    private Observable<DCASelectedService> selectedServiceObservable;
    private Map<String, DCAWidget> permissionMap = new HashMap<>();

    public DCASetupAccountComponent(DCALayoutContainer layoutContainer, DCAAdminPresenter adminPresenter) {
        setLayoutContainer(layoutContainer);
        setAdminPresenter(adminPresenter);
        this.setStyleName("dca-setup-account");

        Observable<List<DCAWidget>> dcaSetUpMenuConfigWidgetsObservable = getAdminPresenter().getWidget(getLoggedInUser(),
                DCAUserPreference.getViewState(), DCAUserPreference.getNavMenuState(), "");

        dcaSetUpMenuConfigWidgetsObservable.subscribe(this::onNext, this::onError);
    }

    private void onNext(List<DCAWidget> dcaSetupConfigWidgetList) {
        for (DCAWidget widget : dcaSetupConfigWidgetList) {
            permissionMap.put(widget.getName(), widget);
        }

        try {
            init();
        } catch (Exception e) {
            logger.error("Error : ", e);
            DCAError dcaError = new DCAError("Error Happened While generating DCA Setup Account page");
            this.addComponent(dcaError);
        }
    }

    private void onError(Throwable throwable) {
        logger.error("Error : ", throwable);
        addComponentAsLast(new DCAError("Admin Component creation error"), this);
    }

    private void init() {
        loadSetupPage();
    }

    private void loadSetupPage() {
        this.breadCrumb.addAction("DCA Setup", s -> updateWidgetContainer(s));
        this.accountsContainerObservable = getAdminPresenter().getAccounts();
        this.dcaAccountListComponent = new DCAAccountListComponent(getLayoutContainer(), getAdminPresenter(),
                getPermissionMode());
        this.targetComponentContainer = new DCATargetComponent(getAdminPresenter());

        this.selectedServiceObservable = DCAUtils.getTargetService();
        this.addComponent(breadCrumb.getView());
        this.addComponent(targetComponentContainer);

        if (getPermissionMode().contains("w")) {
            this.addComponent(dcaAccountListComponent);

            this.selectedServiceObservable.subscribe(targetComponentContainer::onNext, targetComponentContainer::onError,
                    this::onCompleteTargetComponent);
        } else {
            this.selectedServiceObservable.subscribe(targetComponentContainer::onNext, targetComponentContainer::onError);
        }
    }

    private String getPermissionMode() {
        /*TODO: It is kept as static for now, but need to find a good solution*/
        return StringUtils.stripToEmpty(permissionMap.get("dca-setup").getMode());
    }

    private void loadAccountPage() {
        this.selectedServiceObservable = DCAUtils.getTargetService();

        this.selectedServiceObservable.subscribe(selectedService -> {
            addAccountComponent(selectedService);
        }, throwable -> {
            throwable.printStackTrace();
            getLayoutContainer().getWidgetContainer().removeAllComponents();
            getLayoutContainer().getWidgetContainer().addComponent(new DCAError(throwable.getMessage()));
        });
    }

    private void addAccountComponent(DCASelectedService selectedService) {
        Consumer<String> selfNavigation = s -> {
            System.out.println("DCASetupAccountComponent.addAccountComponent");
            addAccountComponent(selectedService);
        };
        getLayoutContainer().getWidgetContainer().removeAllComponents();
        DCAAddEditAccountComponent accountComponent = new DCAAddEditAccountComponent(selectedService.getAccount(),
                getLayoutContainer(), getAdminPresenter(), getPermissionMode(), selfNavigation);
        addComponentAsLast(accountComponent, getLayoutContainer().getWidgetContainer());
    }

    private void onCompleteTargetComponent() {
        this.dcaAccountListComponent.setTargetAccountId(targetComponentContainer.getTargetAccountId());
        this.accountsContainerObservable.subscribe(this.dcaAccountListComponent::onNext, this.dcaAccountListComponent::onError);
    }

    @Override
    public void updateWidgetContainer(String clickedComponentId) {
        getLayoutContainer().getWidgetContainer().removeAllComponents();
        DCASetupAccountComponent dcaSetupAccountComponent = new DCASetupAccountComponent(getLayoutContainer(),
                getAdminPresenter());
        getLayoutContainer().getWidgetContainer().addComponent(dcaSetupAccountComponent);
    }
}
