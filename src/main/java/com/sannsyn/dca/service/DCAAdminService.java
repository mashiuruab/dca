package com.sannsyn.dca.service;

import com.google.gson.*;
import com.sannsyn.dca.model.config.DCAConfigWrapper;
import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.vaadin.component.custom.logout.model.DCAChangePasswordWrapper;
import com.sannsyn.dca.vaadin.servlet.DCAUserPreference;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model.DCAAccountEntity;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model.DCAAccountSearchEntity;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model.DCAAccountsContainer;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model.DCAServiceEntity;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by mashiur on 6/27/16.
 */
public class DCAAdminService extends DCACommonService {
    private static final Logger logger = LoggerFactory.getLogger(DCAAdminService.class);

    private static final String ADMIN_ACCOUNT_SEARCH_URL = String.format("%s/admin/account/search", ADMIN_SERVER_URL);
    private static final String ADMIN_SERVICE_URL = String.format("%s/admin/service", ADMIN_SERVER_URL);
    private static final String ADMIN_PASSWORD_CHANGE_URL = String.format("%s/admin/password", ADMIN_SERVER_URL);
    private static final String ADMIN_ROLES_URL = String.format("%s/dca/role", ADMIN_SERVER_URL);
    private static final String ADMIN_ROLE_SINGLE_URL_TEMPLATE = String.format("%s/dca/role/<uniqueId>", ADMIN_SERVER_URL);
    private static final String ADMIN_ROLES_FILTER_URL_TEMPLATE = String.format("%s/dca/role?filter=<query>", ADMIN_SERVER_URL);
    private static final String PA_ROLES_KEY = "PARoles";

    private Gson gson = new Gson();


    public Observable<DCAAccountsContainer> getAccounts() {
        String webserviceUrl = ADMIN_ACCOUNT_URL;

        Observable<Response> responseObservable = getResponse(webserviceUrl);

        return responseObservable.flatMap(response -> {
            String jsonString = response.readEntity(String.class);
            Gson gson = new Gson();
            DCAAccountsContainer dcaAccountsContainer = gson.fromJson(jsonString, DCAAccountsContainer.class);
            return Observable.just(dcaAccountsContainer);
        });
    }

    public Observable<JsonObject> getAllRoles() {
        return getRolesResponse(ADMIN_ROLES_URL);
    }

    public Observable<JsonObject> getRolesByFilter(String query) {
        if (StringUtils.isBlank(query)) {
            return getAllRoles();
        }

        String endpointUrl = ADMIN_ROLES_FILTER_URL_TEMPLATE.replace("<query>", query);
        return getRolesResponse(endpointUrl);
    }

    private Observable<JsonObject> getRolesResponse(String endpointUrl) {
        return getResponse(endpointUrl).flatMap(response -> {
            if (response.getStatus() != 200) {
                logger.error("Fetch roles returned status " + response.getStatus());
                return Observable.error(new RuntimeException("Unable to fetch roles"));
            }

            String responseString = response.readEntity(String.class);
            List<JsonObject> jsonObjects = parseRoleJson(responseString);
            return Observable.from(jsonObjects);
        });
    }

    private List<JsonObject> parseRoleJson(String response) {
        List<JsonObject> result = new ArrayList<>();
        JsonParser jsonParser = new JsonParser();
        try {
            JsonElement jsonElement = jsonParser.parse(response);
            JsonObject asJsonObject = jsonElement.getAsJsonObject();
            if (asJsonObject.has(PA_ROLES_KEY)) {
                JsonArray rolesArray = asJsonObject.get(PA_ROLES_KEY).getAsJsonArray();
                for (JsonElement element : rolesArray) {
                    JsonObject roleJson = element.getAsJsonObject();
                    handleMissingName(roleJson);
                    result.add(roleJson);
                }
            }
        } catch (Exception e) {
            logger.error("Error while parsing role json", e);
        }
        return result;
    }

    private void handleMissingName(JsonObject roleJson) {
        if (!roleJson.has("name")) {
            String name = "No name found";

            if (roleJson.has("uuid")) {
                String uuid = roleJson.get("uuid").getAsString();
                name += " ( " + uuid + " )";
            }
            roleJson.addProperty("name", name);
        }
    }

    public Status updateRole(String jsonString, String uuid) {
        String webserviceUrl = ADMIN_ROLE_SINGLE_URL_TEMPLATE.replace("<uniqueId>", uuid);
        DCAUser loggedInUser = DCAUserPreference.getLoggedInUser();
        try {
            Response response = putEntity(webserviceUrl, jsonString, loggedInUser);
            if (response.getStatus() == 200) {
                return Status.SUCCESS;
            } else {
                return Status.FAILURE;
            }
        } catch (Exception e) {
            logger.error("Error occurred while updating role", e.getMessage());
            return Status.FAILURE;
        }
    }

    private String getWebServiceEndPoint(Object entity) {
        String webServiceUrl = "";
        if (entity instanceof DCAAccountEntity) {
            webServiceUrl = ADMIN_ACCOUNT_URL;
        } else if (entity instanceof DCAServiceEntity) {
            webServiceUrl = ADMIN_SERVICE_URL;
        } else if (entity instanceof DCAAccountSearchEntity) {
            webServiceUrl = ADMIN_ACCOUNT_SEARCH_URL;
        } else if (entity instanceof DCAConfigWrapper) {
            webServiceUrl = CONFIGURATION_END_POINT;
        } else if (entity instanceof DCAChangePasswordWrapper) {
            webServiceUrl = ADMIN_PASSWORD_CHANGE_URL;
        }
        return webServiceUrl;
    }

    public String postEntity(Object entity, DCAUser loggedInUser) {
        String webServiceUrl = getWebServiceEndPoint(entity);
        String jsonString = gson.toJson(entity);

        if (logger.isDebugEnabled()) {
            logger.debug(webServiceUrl);
            logger.debug(jsonString);
        }

        Response response = post(webServiceUrl, jsonString, loggedInUser);
        return response.readEntity(String.class);
    }

    public String putEntity(Object entity, DCAUser loggedInUser) {
        String webServiceUrl = getWebServiceEndPoint(entity);
        String jsonString = gson.toJson(entity);

        if (logger.isDebugEnabled()) {
            logger.debug(webServiceUrl);
            logger.debug(jsonString);
        }

        Response response = putRequest(webServiceUrl, jsonString, loggedInUser);
        return response.readEntity(String.class);
    }

    public Response putEntity(String webServiceUrl, String jsonString, DCAUser loggedInUser) {
        if (logger.isDebugEnabled()) {
            logger.debug(webServiceUrl);
            logger.debug(jsonString);
        }

        return putRequest(webServiceUrl, jsonString, loggedInUser);
    }

    public Observable<Response> getEntity(DCAUser loggedInUser, String webserviceUrl) {
        if (logger.isDebugEnabled()) {
            logger.debug(webserviceUrl);
        }
        return getResponse(webserviceUrl, getAuthHeaderMap(loggedInUser));
    }

    public Observable<Response> deleteService(String serviceUUID, DCAUser loggedInUser) {
        String webserviceUrl = String.format("%s/%s", ADMIN_SERVICE_URL, serviceUUID);

        if (logger.isDebugEnabled()) {
            logger.debug(webserviceUrl);
            logger.debug(String.format("Deleting Service UUID %s", serviceUUID));
        }

        Map<String, String> authHeaderMap = getAuthHeaderMap(loggedInUser);

        return deleteRequest(webserviceUrl, authHeaderMap);
    }
}
