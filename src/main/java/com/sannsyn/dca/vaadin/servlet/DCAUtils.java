package com.sannsyn.dca.vaadin.servlet;

import com.sannsyn.dca.model.config.DCAConfigEntity;
import com.sannsyn.dca.model.config.DCASelectedService;
import com.sannsyn.dca.model.config.DCASettings;
import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.presenter.DCAAdminPresenter;
import com.vaadin.server.VaadinSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by mashiur on 12/7/16.
 */
public class DCAUtils {
    private static final Logger logger = LoggerFactory.getLogger(DCAUtils.class);
    private static Map<String, DCASelectedService> targetServiceMap = new ConcurrentHashMap<>();
    private static DCAAdminPresenter adminPresenter = new DCAAdminPresenter();

    private DCAUtils() {

    }

    public static Observable<DCASelectedService> getTargetService() {
        if (DCAUserPreference.getLoggedInUser() == null) {
            logger.error(String.format("No Logged In User Found in the Session %s", VaadinSession.getCurrent().getSession()));
            return Observable.empty();
        }

        DCASelectedService targetService = targetServiceMap.get(DCAUserPreference.getLoggedInUser().getUsername());

        if (targetService == null) {
            Observable<DCASelectedService> selectedServiceObservable = adminPresenter.getTargetService(
                    DCAUserPreference.getLoggedInUser());
            return selectedServiceObservable.flatMap(dcaSelectedService -> {
                setTargetService(dcaSelectedService);
                return getTargetService();
            });
        } else {
            return Observable.just(targetService);
        }
    }

    public static void setTargetService(DCASelectedService pTargetService) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Setting the target service %s for user : %s",
                    pTargetService.getName(), DCAUserPreference.getLoggedInUser().getUsername()));
        }
        targetServiceMap.put(DCAUserPreference.getLoggedInUser().getUsername(), pTargetService);
    }

    public static void removeTargetService() {
        if (targetServiceMap.containsKey(DCAUserPreference.getLoggedInUser().getUsername())) {
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Removing Target Service Cache for UserName %s",
                        DCAUserPreference.getLoggedInUser().getUsername()));
            }
            targetServiceMap.remove(DCAUserPreference.getLoggedInUser().getUsername());
        }
    }

    public static void removeCurrentTargetServiceForAll() {
        /*This is to make sure no cached data served like metadata url, analytics url if the service entity is updated*/
        DCASelectedService targetService = targetServiceMap.get(DCAUserPreference.getLoggedInUser().getUsername());

        if (targetService == null) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Invalidating Cached service with UUID = %s for all the Users",
                    targetService.getUuid()));
        }

        List<String> usersToRemove = new ArrayList<>();
        for (Map.Entry<String, DCASelectedService> entry : targetServiceMap.entrySet()) {
            DCASelectedService selectedService = entry.getValue();
            if (selectedService.getUuid().equals(targetService.getUuid())) {
                usersToRemove.add(entry.getKey());
            }
        }

        for (String userName : usersToRemove) {
            targetServiceMap.remove(userName);
        }
    }

    public static DCASettings getConfigSettings() {
        DCAConfigEntity dcaConfigEntity = adminPresenter.getLoggedInUserConfiguration(DCAUserPreference.getLoggedInUser());

        if ("ok".equals(dcaConfigEntity.getStatus())) {
            return dcaConfigEntity.getPADCAConfiguration().getRoot().getSettings();
        } else {
            logger.error(String.format("Config Settings Not found for the User %s", DCAUserPreference.getLoggedInUser()));
            return new DCASettings();
        }
    }
}
