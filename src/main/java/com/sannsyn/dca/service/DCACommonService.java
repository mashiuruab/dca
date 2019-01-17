package com.sannsyn.dca.service;

import com.google.gson.Gson;
import com.sannsyn.dca.model.config.*;
import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.util.DCAConfigProperties;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model.DCAAccount;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model.DCAAccountWithStatus;
import com.sannsyn.dca.vaadin.widgets.admin.dcasetup.model.DCAResponseEntity;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.summary.DCAControllerService;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.client.rx.rxjava.RxObservable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import javax.ws.rs.client.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mashiur on 6/27/16.
 */
public class DCACommonService {
    private static final Logger logger = LoggerFactory.getLogger(DCACommonService.class);

    public static final String ADMIN_SERVER_URL = DCAConfigProperties.getAdminServerUrl();
    static final String TOP_MENU_END_POINT = "http://localhost:8080/top-menu/";
    static final String DUMMY_END_POINT = "http://localhost:8080/dummy/";
    static final String ADMIN_ACCOUNT_URL = String.format("%s/admin/account", ADMIN_SERVER_URL);
    private static final String ADMIN_SERVICE_URL = String.format("%s/admin/service", ADMIN_SERVER_URL);
    static final String USER_CONFIGURATION_ROOT_END_POINT = String.format("%s/dca/userconfiguration/root", ADMIN_SERVER_URL);
    static final String CONFIGURATION_END_POINT = String.format("%s/dca/configuration", ADMIN_SERVER_URL);
    static final String USER_CONFIGURATION_END_POINT = String.format("%s/dca/userconfiguration", ADMIN_SERVER_URL);
    private static final String CONTROLLER_SERVICE_STATUS_URI = "recapi/1.0/servicestatus";
    static final String userName = DCAConfigProperties.getWsUserName();
    static final String password = DCAConfigProperties.getWsPassword();
    public static final String UPDATE_SRVS_CONFIG_TMPLT = "<endpoint>/recapi/internal/overrideconfiguration/<serviceidentifier>";
    public static final String DELETE_SRVS_CONFIG_TMPLT = "<endpoint>/recapi/internal/deletefromconfiguration/<serviceidentifier>";

    public HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(userName, password);


    String get(String path, Map<String, String> headerMap) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(path);
        Invocation.Builder request = target.request("text/plain");

        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            request.header(entry.getKey(), entry.getValue());
        }
        return request.get(String.class);
    }

    Map<String, String> getAuthHeaderMap(DCAUser loggedInUser) {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("PASession", loggedInUser.getSession());
        headerMap.put("PAToken", loggedInUser.getToken());

        return headerMap;
    }

    Client getClient() {
        ClientConfig configuration = new ClientConfig();
        configuration.property(ClientProperties.CONNECT_TIMEOUT, 10000);
        configuration.property(ClientProperties.READ_TIMEOUT, 10000);

        return ClientBuilder.newClient(configuration);
    }

    Response put(String putRequestUrl, String jsonString) {
        Response response;

        try {
            Client client = ClientBuilder.newClient();
            client.register(feature);

            WebTarget webTarget = client.target(putRequestUrl);
            response = webTarget.request().header(HttpHeaders.CONTENT_TYPE, "text/plain")
                    .accept("application/json").put(Entity.entity(jsonString, MediaType.APPLICATION_JSON));

            if (response.getStatus() != 200) {
                String errorMessage = String.format("Failed : HTTP error code : %s and error found : %s", response.getStatus(),
                        response.readEntity(String.class));
                logger.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }
        } catch (Exception e) {
            logger.error("Error : ", e);
            throw new RuntimeException(e);

        }

        return response;
    }

    Response post(String postRequestUrl, String jsonString, DCAUser loggedInUser) {
        Response response;

        try {
            Client client = ClientBuilder.newClient();
            client.register(feature);
            WebTarget webTarget = client.target(postRequestUrl);
            Invocation.Builder builder = webTarget.request();

            Map<String, String> headerMap = getAuthHeaderMap(loggedInUser);

            for(Map.Entry<String, String> entry : headerMap.entrySet()) {
                builder.header(entry.getKey(), entry.getValue());
            }
            response = builder.header(HttpHeaders.CONTENT_TYPE, "text/plain")
                    .accept("application/json").post(Entity.entity(jsonString, MediaType.APPLICATION_JSON));

            if (response.getStatus() != 200) {
                logger.error(response.readEntity(String.class));
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatus());
            }
        } catch (Exception e) {
            logger.error("Error : ", e);
            throw new RuntimeException(e);

        }

        return response;
    }

    Response putRequest(String postRequestUrl, String jsonString, DCAUser loggedInUser) {
        Response response;

        try {
            Client client = ClientBuilder.newClient();
            client.register(feature);
            WebTarget webTarget = client.target(postRequestUrl);
            Invocation.Builder builder = webTarget.request();

            Map<String, String> headerMap = getAuthHeaderMap(loggedInUser);

            for(Map.Entry<String, String> entry : headerMap.entrySet()) {
                builder.header(entry.getKey(), entry.getValue());
            }

            response = builder.header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .accept("application/json").put(Entity.entity(jsonString, MediaType.APPLICATION_JSON));

            if (response.getStatus() != 200) {
                logger.error(response.readEntity(String.class));
                /* todo Since this method returns Response object, we may not throw exception and just check the status
                 *  in the Response object at the callers side. */
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatus());
            }
        } catch (Exception e) {
            logger.error("Error : ", e);
            throw new RuntimeException(e);

        }

        return response;
    }

    /*Try Not to use this one*/
    Observable<Response> getResponse(String url) {
        Client client = getClient();
        client.register(feature);

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Doing Get Request %s", url));
        }

        return RxObservable.from(client)
                .target(url)
                .request()
                .rx()
                .get();

    }

    Observable<Response> getResponse(String url, Map<String, String> headerMap) {
        Client client = getClient();
        client.register(feature);

        return RxObservable.from(client)
                .target(url)
                .request()
                .header("PASession", headerMap.get("PASession"))
                .header("PAToken", headerMap.get("PAToken"))
                .rx()
                .get();

    }

    public Observable<Response> doHEADRequest(String url, DCAUser loggedInUser) {
        Map<String, String> headerMap = getAuthHeaderMap(loggedInUser);
        Client client = getClient();
        client.register(feature);

        return RxObservable.from(client)
                .target(url)
                .request()
                .header("PASession", headerMap.get("PASession"))
                .header("PAToken", headerMap.get("PAToken"))
                .rx()
                .head();

    }

    Observable<Response> deleteRequest(String url, Map<String, String> headerMap) {
        Client client = getClient();

        return RxObservable.from(client)
                .target(url)
                .request()
                .header("PASession", headerMap.get("PASession"))
                .header("PAToken", headerMap.get("PAToken"))
                .rx()
                .delete();
    }

    public Observable<DCAConfigEntity> getUserConfiguration(DCAUser loggedInUser) {
        Observable<Response> responseObservable = getResponse(USER_CONFIGURATION_END_POINT, getAuthHeaderMap(loggedInUser));

        return responseObservable.flatMap(response -> {
            String responseString = response.readEntity(String.class);
            Gson gson = new Gson();
            DCAConfigEntity dcaConfigEntity = gson.fromJson(responseString, DCAConfigEntity.class);

            return Observable.just(dcaConfigEntity);
        });

    }

    public DCAConfigEntity getLoggedInUserConfiguration(DCAUser loggedInUser) {
        String responseString = get(USER_CONFIGURATION_END_POINT, getAuthHeaderMap(loggedInUser));

        return new Gson().fromJson(responseString, DCAConfigEntity.class);
    }

    private DCAContainers getTargetContainer(List<DCAContainers> containerList, String containerName) {
        for (DCAContainers containers : containerList) {
            if (containers.getName().equals(containerName)) {
                return containers;
            }
        }

        return new DCAContainers();
    }

    public Observable<List<DCAWidget>> getWidget(DCAUser loggedInUser, String section, String menu, String subMenu) {
        Observable<DCAConfigEntity> entityObservable = getUserConfiguration(loggedInUser);

        return entityObservable.flatMap(dcaConfigEntity -> {
            List<DCASection> sectionList = dcaConfigEntity.getPADCAConfiguration().getRoot().getSections();

            for (DCASection dcaSection : sectionList) {
                if (dcaSection.getName().equals(section)) {
                    List<DCAContainers> containersList = dcaSection.getContainers();

                    if (StringUtils.isEmpty(subMenu)) {
                        DCAContainers targetMenuContainer = getTargetContainer(containersList,menu);
                        return Observable.just(targetMenuContainer.getWidgets());
                    } else {
                        DCAContainers targetMenuContainers = getTargetContainer(containersList, menu);
                        List<DCAContainers> subMenuContainerList = targetMenuContainers.getSubmenu();
                        DCAContainers targetSubMenuContainer = getTargetContainer(subMenuContainerList, subMenu);
                        return Observable.just(targetSubMenuContainer.getWidgets());
                    }
                }
            }

            throw new RuntimeException(String.format("No widget found for section %s, menu %s, submenu %s",
                    section, menu, subMenu));

        });
    }

    public Observable<DCASelectedService> getSelectedService(DCAUser loggedInUser) {
        Observable<DCAConfigEntity> configEntityObservable = getUserConfiguration(loggedInUser);

        return configEntityObservable.flatMap(configEntity -> {
            if ("ok".equals(configEntity.getStatus())) {
                Observable<DCASelectedService> selectedServiceObservable = getService(
                        configEntity.getPADCAConfiguration().getRoot().getSettings().getService().getServiceId());

                return selectedServiceObservable.flatMap(selectedService -> {
                    Observable<DCAAccount> accountObservable = getAccount(selectedService.getAccount().getUuid());

                    return accountObservable.flatMap(account -> {
                        selectedService.setAccount(account);
                        return Observable.just(selectedService);
                    });
                });

            } else {
                String errorMessage = String.format("Error  While fetching Config Service, Status Code is : %s", configEntity.getStatus());
                logger.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }
        });
    }

    public Observable<DCASelectedService> getService(String uuid) {
        String webserviceUrl = String.format("%s/%s", ADMIN_SERVICE_URL, uuid);
        if (logger.isDebugEnabled()) {
            logger.debug(webserviceUrl);
        }
        Observable<Response> responseObservable = getResponse(webserviceUrl);

        return responseObservable.flatMap(response -> {
            String responseString = response.readEntity(String.class);
            Gson gson = new Gson();
            DCAResponseEntity responseEntity = gson.fromJson(responseString, DCAResponseEntity.class);
            if ("ok".equals(responseEntity.getStatus())) {
                DCASelectedService selectedService = responseEntity.getService();
                Observable<DCAAccount> accountObservable = getAccount(selectedService.getAccount().getUuid());

                return accountObservable.flatMap(account -> {
                    selectedService.setAccount(account);
                    return Observable.just(selectedService);
                });
            } else {
                throw new RuntimeException(String.format("Error Fetching selected service, status found %s",
                        responseEntity.getStatus()));
            }
        });
    }

    public Observable<DCAAccount> getAccount(String uuid) {
        Observable<Response> responseObservable = getResponse(String.format("%s/%s", ADMIN_ACCOUNT_URL, uuid));

        return responseObservable.flatMap(response -> {
            String jsonString = response.readEntity(String.class);
            Gson gson = new Gson();
            DCAAccountWithStatus accountWithStatus = gson.fromJson(jsonString, DCAAccountWithStatus.class);
            if (!"ok".equals(accountWithStatus.getStatus())) {
                logger.error(String.format("Error Happened, Status code is : %s", accountWithStatus.getAccount()));
                throw new RuntimeException(String.format("Account with id %s not found", uuid));
            }
            return Observable.just(accountWithStatus.getAccount());
        });
    }

    public Observable<DCAControllerService> getServiceStatus(DCAUser loggedInUser) {
        Observable<DCASelectedService> selectedServiceObservable = getSelectedService(loggedInUser);

        return selectedServiceObservable.flatMap(dcaSelectedService -> {
            String webserviceUrl = String.format("%s/%s/%s", dcaSelectedService.getServiceEndpoint().getEndpointAddress(),
                    CONTROLLER_SERVICE_STATUS_URI, dcaSelectedService.getServiceIdentifier());
            Observable<Response> observableResponse = getResponse(webserviceUrl);

            return observableResponse.flatMap(response -> {
                String jsonResponse = response.readEntity(String.class);
                Gson gson = new Gson();
                DCAControllerService dcaControllerService = gson.fromJson(jsonResponse, DCAControllerService.class);
                return Observable.just(dcaControllerService);
            });
        });
    }
}
