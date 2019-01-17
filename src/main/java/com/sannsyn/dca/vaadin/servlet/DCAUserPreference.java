package com.sannsyn.dca.vaadin.servlet;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sannsyn.dca.model.config.DCAConfigEntity;
import com.sannsyn.dca.model.config.DCAConfigWrapper;
import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.presenter.DCAAdminPresenter;
import com.sannsyn.dca.vaadin.component.custom.notification.DCANotification;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mashiur on 7/21/16.
 */
public class DCAUserPreference {
    private static final Logger logger = LoggerFactory.getLogger(DCAUserPreference.class);

    public static final String LOGGEDIN_USER_SESSION_KEY = "dcaLoggedInUser";
    private static final String PREFERENCE_KEY = "preference";
    public static final String VIEW_STATE = "view-state";
    public static final String NAV_MENU_STATE = "nav-menu-state";

    private static DCAAdminPresenter adminPresenter = new DCAAdminPresenter();
    private static Gson gson = new Gson();

    private DCAUserPreference() {}

    public static void updateUiState(String key, Object value) {
        Map<String, Object> updateStateMap = getPreference();
        updateStateMap.put(key, value);
        DCAUserPreference.setPreference(updateStateMap);
    }

    public static void setLoggedInUserSessionKey(DCAUser loggedInUser) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Setting session attribute dcaLoggedInUser %s", loggedInUser.toString()));
        }

        if (DCAUser.logins.containsKey(loggedInUser.getUsername()) && DCAUser.logins.get(loggedInUser.getUsername()) >= 1) {
            String errorMessage = String.format("User %s already logged in", loggedInUser.getUsername());
            logger.error(errorMessage);

            DCANotification.NOTIFICATION.show(errorMessage);
        }

        VaadinSession.getCurrent().getSession().setAttribute(LOGGEDIN_USER_SESSION_KEY, loggedInUser);
    }

    public static void removeLoggedInUser() {
        if (getLoggedInUser() == null) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Removing session attribute %s from session %s",
                    LOGGEDIN_USER_SESSION_KEY, VaadinSession.getCurrent().getSession().getId()));
        }

        persistUIState();

        VaadinSession.getCurrent().getSession().removeAttribute(LOGGEDIN_USER_SESSION_KEY);
    }

    public static void clearUIState() {
        getPreference().remove(VIEW_STATE);
        getPreference().remove(NAV_MENU_STATE);
    }

    public static String getViewState() {
        return (String) getPreference().get(VIEW_STATE);
    }

    public static String getNavMenuState() {
        return (String) getPreference().get(NAV_MENU_STATE);
    }

    public static DCAUser getLoggedInUser() {
        return (DCAUser) VaadinSession.getCurrent().getSession().getAttribute(LOGGEDIN_USER_SESSION_KEY);
    }

    public static void initPreference() {
        Observable<DCAConfigWrapper> configWrapperObservable = getConfigWrapperEntity();

        configWrapperObservable.subscribe(dcaConfigWrapper -> {
            setPreference(dcaConfigWrapper.getRoot().getSettings().getUiState());
        }, throwable -> {
            throwable.printStackTrace();
            logger.error("Error : ", throwable);
        });
    }

    public static void setPreference(Map<String, Object> preferenceValue) {
        VaadinSession.getCurrent().getSession().removeAttribute(PREFERENCE_KEY);
        VaadinSession.getCurrent().getSession().setAttribute(PREFERENCE_KEY, preferenceValue);
    }

    public static Map<String, Object> getPreference() {
        Map<String, Object> preferenceObject = (Map<String, Object>) VaadinSession.getCurrent().getSession().getAttribute(PREFERENCE_KEY);

        if (preferenceObject != null) {
            return preferenceObject;
        }

        return new HashMap<String, Object>();
    }

    public static Map<String, String> getMap(String jsonString) {
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        return gson.fromJson(jsonString, type);
    }

    public static String getJson(Map<String, Object> mapObject) {
        return gson.toJson(mapObject);
    }

    public static void persistUIState() {
        clearUIState();

        DCAUser loggedInUser = getLoggedInUser();

        if (loggedInUser == null) {
            return;
        }

        Observable<DCAConfigWrapper> configWrapperObservable = getConfigWrapperEntity();

        configWrapperObservable.subscribe(dcaConfigWrapper -> {
            if (getPreference().isEmpty()) {
                return;
            }

            dcaConfigWrapper.getRoot().getSettings().setUiState(getPreference());

            try {
                String responseString =  adminPresenter.putEntity(dcaConfigWrapper, loggedInUser);
                if (logger.isDebugEnabled()) {
                    logger.debug(responseString);
                    logger.debug("persisted the UI state to the config server");
                }
            } catch (Exception e) {
                logger.error("Error : ", e);
            }

        }, throwable -> {
            throwable.printStackTrace();
            logger.error("Error : ", throwable);
        });
    }

    private static Observable<DCAConfigWrapper> getConfigWrapperEntity() {
        DCAUser loggedInUser = getLoggedInUser();

        Observable<DCAConfigEntity> configEntityObservable = adminPresenter.getUserConfiguration(loggedInUser);
        return configEntityObservable.flatMap(dcaConfigEntity -> {
            DCAConfigWrapper configEntityWrapper = new DCAConfigWrapper();
            configEntityWrapper.setUuid(dcaConfigEntity.getPADCAConfiguration().getUuid());
            configEntityWrapper.setRoot(dcaConfigEntity.getPADCAConfiguration().getRoot());
            return Observable.just(configEntityWrapper);
        });
    }
}
