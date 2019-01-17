package com.sannsyn.dca.presenter;

import com.sannsyn.dca.model.config.DCAConfigEntity;
import com.sannsyn.dca.model.config.DCASelectedService;
import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model.DCAAccount;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model.DCAAccountsContainer;
import rx.Observable;

import javax.ws.rs.core.Response;

/**
 * Created by mashiur on 6/27/16.
 */
public class DCAAdminPresenter extends DCASectionPresenter {
    public Observable<DCAAccountsContainer> getAccounts() {
        return adminService.getAccounts();
    }

    public Observable<DCAAccount> getAccount(String uuid) {
        return adminService.getAccount(uuid);
    }

    public String postEntity(Object entity, DCAUser loggedInUser) {
        return adminService.postEntity(entity, loggedInUser);
    }

    public String putEntity(Object entity, DCAUser loggedInUser) {
        return adminService.putEntity(entity, loggedInUser);
    }

    public Observable<DCASelectedService> getService(String uuid) {
        return adminService.getService(uuid);
    }

    public DCAConfigEntity getLoggedInUserConfiguration(DCAUser loggedInUser) {
        return adminService.getLoggedInUserConfiguration(loggedInUser);
    }

    public Observable<Response> getServiceConfig(DCAUser loggedInUser) {
        return configService.getServiceConfigResponse(loggedInUser);
    }

    public Observable<Response> getEntity(DCAUser loggedInUser, String url) {
        return adminService.getEntity(loggedInUser, url);
    }

    public Response putEntity(String url, String jsonString, DCAUser loggedInUser) {
        return adminService.putEntity(url, jsonString, loggedInUser);
    }

    public Observable<Response> doHEADRequest(String endpoint, DCAUser loggedInUser) {
        return adminService.doHEADRequest(endpoint, loggedInUser);
    }

    public Observable<Response> deleteService(String UUID, DCAUser loggedInUser) {
        return adminService.deleteService(UUID, loggedInUser);
    }
}
