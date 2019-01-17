package com.sannsyn.dca.presenter;

import com.sannsyn.dca.model.config.*;
import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.service.DCAAdminService;
import com.sannsyn.dca.service.DCAConfigService;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.summary.DCAControllerService;
import com.sannsyn.dca.vaadin.widgets.operations.dashboard.recommendation.model.DCAContext;
import rx.Observable;

import java.util.List;

/**
 * Created by mashiur on 6/27/16.
 */
public class DCASectionPresenter {
    DCAConfigService configService = new DCAConfigService();
    DCAAdminService adminService = new DCAAdminService();

    public List<DCASection> getSections(DCAUser loggedInUser) {
        return configService.getSectionList(loggedInUser);
    }

    public List<DCAContainers> getLeftMenuContainers(String sectionName, DCAUser loggedInUser) {
        return configService.getLeftMenuContainers(sectionName, loggedInUser);
    }

    public DCAContext getEnsembleData(DCASelectedService targerService) {
        return configService.getEnsembleData(targerService);
    }

    public Observable<DCAControllerService> getServiceStatus(DCAUser loggedInUser) {
        return configService.getServiceStatus(loggedInUser);
    }

    public Observable<DCAConfigEntity> getUserConfiguration(DCAUser loggedInUser) {
        return configService.getUserConfiguration(loggedInUser);
    }

    public Observable<List<DCAWidget>> getWidget(DCAUser loggedInUser, String section, String menu, String subMenu) {
        return configService.getWidget(loggedInUser, section, menu, subMenu);
    }

    public Observable<DCASelectedService> getTargetService(DCAUser loggedInUser) {
        return adminService.getSelectedService(loggedInUser);
    }
}
