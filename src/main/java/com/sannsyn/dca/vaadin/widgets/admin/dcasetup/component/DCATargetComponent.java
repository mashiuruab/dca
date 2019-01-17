package com.sannsyn.dca.vaadin.widgets.admin.dcasetup.component;

import com.sannsyn.dca.model.config.DCASelectedService;
import com.sannsyn.dca.presenter.DCAAdminPresenter;
import com.sannsyn.dca.vaadin.component.custom.DCAWrapper;
import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model.DCAAccount;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.summary.DCAControllerService;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by mashiur on 5/6/16.
 */
public class DCATargetComponent extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCATargetComponent.class);

    private Map<String, String> targetInfoMap = new LinkedHashMap<String, String>();
    private DCAAccount targetAccount;
    private CssLayout accountInfoContainer = new CssLayout();

    public DCATargetComponent(DCAAdminPresenter adminPresenter) {
        setAdminPresenter(adminPresenter);
        try {
            init();
        } catch (Exception e) {
            logger.error("Error : ", e);
            this.addComponent(new DCAError("Error Loading Target Account Component"));
        }
    }

    private Component createTargetItem(String name, String value) {
        DCALabel labelName = new DCALabel(name, "name");
        DCALabel labelValue = new DCALabel(value, "value");
        return new DCAWrapper(Arrays.asList(labelName, labelValue), "item");
    }

    private void init() {
        this.setStyleName("dca-target");
        DCALabel header = new DCALabel("DCA Target", "header dca-widget-title");
        accountInfoContainer.setStyleName("account-info-container");
        this.addComponent(header);
        this.addComponent(accountInfoContainer);
    }

    public void onNext(DCASelectedService selectedService) {
        this.targetAccount = selectedService.getAccount();

        this.targetInfoMap.put("Account name :", targetAccount.getName());
        this.targetInfoMap.put("Account description :", targetAccount.getDescription());
        this.targetInfoMap.put("Service name :", selectedService.getName());

        Observable<DCAControllerService> controllerServiceObservable = getAdminPresenter().getServiceStatus(getLoggedInUser());
        controllerServiceObservable.subscribe(this::loadServiceInfo, this::onError);

    }

    private void loadServiceInfo(DCAControllerService controllerService) {
        this.targetInfoMap.put("Service description :", controllerService.getServiceDescription());
        this.targetInfoMap.put("Service status :", StringUtils.isEmpty(controllerService.getRunningSince()) ? "Stopped" : "Running");

        int counter = 1;
        for (Map.Entry<String, String> entry : targetInfoMap.entrySet()) {
            Component itemComponent = createTargetItem(entry.getKey(), entry.getValue());
            addComponentAsLast(itemComponent, accountInfoContainer);
            if ((counter % 4 == 0) || (counter % 4 == 3)) {
                itemComponent.addStyleName("alternating-gray-color");
            }
            counter++;
        }
    }

    public void onError(Throwable throwable) {
        throwable.printStackTrace();
        logger.error(throwable.getMessage());
        addComponentAsLast(new DCAError(throwable.getMessage()), this);
    }

    public String getTargetAccountId() {
        return targetAccount.getUuid();
    }

    @Override
    public void updateWidgetContainer(String clickedComponentId) {

    }
}
