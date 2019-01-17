package com.sannsyn.dca.service;


import com.google.gson.Gson;
import com.sannsyn.dca.model.config.*;
import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.vaadin.servlet.DCAUtils;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.aggregates.form.create.DCAAggregateCreateResponse;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.*;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.info.DCAAggregateInfo;
import com.sannsyn.dca.vaadin.widgets.operations.dashboard.recommendation.model.DCAContext;
import com.sannsyn.dca.vaadin.widgets.operations.dashboard.recommendation.model.DCANumberOfRecommendation;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mashiur on 2/24/16.
 */
public class DCAConfigService extends DCACommonService {
    private static final Logger logger = LoggerFactory.getLogger(DCAConfigService.class);

    private static final String sampleControllerEndPoint = "http://localhost:8080/sample-controller/";
    private static final String NUMBER_OF_RECOMMENDATION_CHART_URI = "recapi/1.0/getmonitordata";
    public static final String SERVICE_CONFIG_URI = "<endpoint>/recapi/1.0/currentconfig/<serviceidentifier>";
    private String PIPELINE_EXTERNAL_ENDPOINT =
            String.format("%s/service.external.dca.pipeline", UPDATE_SRVS_CONFIG_TMPLT);
    private String PIPES_EXTERNAL_ENDPOINT = String.format("%s/service.external.dca.pipes", UPDATE_SRVS_CONFIG_TMPLT);
    private String PIPELINE_UPDATE_ENDPOINT = "<endpoint>/recapi/1.0/modify/<serviceidentifier>/Ensemble/<pipelineObjectName>";
    private String GLOBAL_FILTER_ENDPOINT = "<endpoint>/recapi/internal/overrideconfiguration/<serviceidentifier>/service.ensembles.globalFilters";



    private String get(String path, String userName, String password) {
        final Client client = ClientBuilder.newClient();
        client.property(ClientProperties.CONNECT_TIMEOUT, 10000);
        client.property(ClientProperties.READ_TIMEOUT, 10000);

        client.register(feature);

        WebTarget target = client.target(path);
        Invocation.Builder request = target.request("text/plain");
        return request.get(String.class);
    }

    private DCAConfigWrapper getDCAConfig() {
        String response = get(TOP_MENU_END_POINT, new HashMap<>());
        Gson gson = new Gson();
        return gson.fromJson(response, DCAConfigWrapper.class);
    }

    private DCAConfigWrapper getConfig(DCAUser loggedInUser) {
        String responseString = get(USER_CONFIGURATION_ROOT_END_POINT, getAuthHeaderMap(loggedInUser));

        Gson gson = new Gson();
        DCAConfigEntity configEntity = gson.fromJson(responseString, DCAConfigEntity.class);
        if ("ok".equals(configEntity.getStatus())) {
            return configEntity.getPADCAConfiguration();
        } else {
            logger.error("Config Service Fetching Error : ");
            return  getDCAConfig();
        }
    }

    private DCANumberOfRecommendation getRecommendationData(String webserviceUrl) {
        String response = get(webserviceUrl, userName, password);
        response = getJson(response);
        Gson gson = new Gson();
        return gson.fromJson(response, DCANumberOfRecommendation.class);
    }

    public DCAContext getEnsembleData(DCASelectedService targetService) {
        String webserviceUrl = String.format("%s/%s/%s/%s", targetService.getServiceEndpoint().getEndpointAddress(),
                NUMBER_OF_RECOMMENDATION_CHART_URI, targetService.getServiceIdentifier(), "Ensemble-OK");
        DCANumberOfRecommendation dcaNumberOfRecommendation = getRecommendationData(webserviceUrl);

        if (!dcaNumberOfRecommendation.getMonitorData().getContexts().isEmpty()) {
            return dcaNumberOfRecommendation.getMonitorData().getContexts().get(0);
        }

        return new DCAContext();
    }

    private String getJson(String input) {
        input = input.replace("\"{", "{");
        input = input.replace("}\"", "}");
        input = input.replace("\\\"", "\"");
        return input;
    }

    public List<DCASection> getSectionList(DCAUser loggedInUser) {
        DCAConfigWrapper dcaConfigWrapper = getConfig(loggedInUser);
        return dcaConfigWrapper.getRoot().getSections();
    }

    public List<DCAContainers> getLeftMenuContainers(String sectionName, DCAUser loggedInUser) {
        List<DCAContainers> containersList = new ArrayList<DCAContainers>();

        for(DCASection dcaSection : getSectionList(loggedInUser)) {
            if (sectionName.equals(dcaSection.getName())) {
                containersList = dcaSection.getContainers();
            }
        }

        return containersList;
    }

    public Observable<Response> getServiceConfigResponse(DCAUser loggedInUser) {
        Observable<DCASelectedService> selectedServiceObservable = getSelectedService(loggedInUser);

        return selectedServiceObservable.flatMap(dcaSelectedService -> {
            String webserviceUrl = SERVICE_CONFIG_URI
                    .replace("<endpoint>", dcaSelectedService.getServiceEndpoint().getEndpointAddress())
                    .replace("<serviceidentifier>", dcaSelectedService.getServiceIdentifier());
            logger.debug("webserviceUrl = " + webserviceUrl);
            return getResponse(webserviceUrl);
        });
    }

    public Observable<DCAServiceConfigWrapper> getServiceConfig(DCAUser loggedInUser) {
        Observable<Response> observableResponse = getServiceConfigResponse(loggedInUser);
        return observableResponse.flatMap(response -> {
            Gson gson = new Gson();
            String jsonResponse = response.readEntity(String.class);
            DCAServiceConfigStrWrapper serviceConfigStrWrapper = gson.fromJson(jsonResponse, DCAServiceConfigStrWrapper.class);
            DCAServiceConfigWrapper dcaServiceConfigWrapper = serviceConfigStrWrapper.getConfigurationStr();
            return Observable.just(dcaServiceConfigWrapper);
        });
    }

    public Observable<DCAAggregateCreateResponse> createAggregate(DCAAggregateItem dcaAggregateItem,
                                                                  String aggregateName, DCAUser loggedInUser) {
        Observable<DCASelectedService> selectedServiceObservable = getSelectedService(loggedInUser);

        return selectedServiceObservable.flatMap(dcaSelectedService -> {
            Gson gson = new Gson();

            String jsonString = gson.toJson(dcaAggregateItem);
            String putRequestUrl = String.format("%s/%s/%s/AddAggregate/%s", dcaSelectedService.getServiceEndpoint().getEndpointAddress(),
                    "recapi/1.0/modify", dcaSelectedService.getServiceIdentifier(), aggregateName);
            if (logger.isDebugEnabled()) {
                logger.debug(putRequestUrl);
                logger.debug(jsonString);
            }
            Response response = put(putRequestUrl, jsonString);

            String responseString = response.readEntity(String.class);
            logger.info(responseString);

            DCAAggregateCreateResponse dcaAggregateCreateResponse = gson.fromJson(responseString, DCAAggregateCreateResponse.class);
            dcaAggregateCreateResponse.setStatusCode(response.getStatus());
            return Observable.just(dcaAggregateCreateResponse);
        });
    }

    public String updatePipeLine(DCATaskObject taskObject, String pipeName, DCASelectedService selectedService) {
        Gson gson = new Gson();

        String jsonString = gson.toJson(taskObject);

        String putRequestUrl = PIPELINE_UPDATE_ENDPOINT
                .replace("<endpoint>", selectedService.getServiceEndpoint().getEndpointAddress())
                .replace("<serviceidentifier>", selectedService.getServiceIdentifier())
                .replace("<pipelineObjectName>", pipeName);

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Webservice Url to update Pipeline %s is %s", pipeName, putRequestUrl));
            logger.debug(String.format("Json %s", jsonString));
        }

        String responseString = put(putRequestUrl, jsonString).readEntity(String.class);

        if (logger.isDebugEnabled()) {
            logger.debug(responseString);
        }

        return responseString;
    }

    public Observable<String> createPipeLine(DCATaskObject taskObject, String pipeName, DCAUser loggedInUser,
                                             Map<String, Object> allPipelineExternalInfo) {
        Observable<DCASelectedService> selectedServiceObservable = getSelectedService(loggedInUser);

        return selectedServiceObservable.flatMap(dcaSelectedService -> {
            Gson gson = new Gson();

            String  responseString = updatePipeLine(taskObject, pipeName, dcaSelectedService);

            if (logger.isDebugEnabled()) {
                logger.debug(responseString);
            }

            String additionalPipeLineInfoString = gson.toJson(allPipelineExternalInfo);
            String externalServicePutRequestPath = PIPELINE_EXTERNAL_ENDPOINT
                    .replace("<endpoint>", dcaSelectedService.getServiceEndpoint().getEndpointAddress())
                    .replace("<serviceidentifier>", dcaSelectedService.getServiceIdentifier());

            Response externalInfoResponse = put(externalServicePutRequestPath, additionalPipeLineInfoString);
            String externalInfoResponseString = externalInfoResponse.readEntity(String.class);

            if (logger.isDebugEnabled()) {
                logger.debug(externalInfoResponseString);
            }

            return Observable.just(String.format("%s-%s",responseString, externalInfoResponseString));
        });
    }

    public Observable<Response> createUpdateExternalPipeData(Map<String, Object> externalPipeData) {
        Observable<DCASelectedService> targetServiceObservable = DCAUtils.getTargetService();

        return targetServiceObservable.flatMap(targetService -> {
            Gson gson = new Gson();
            String externalPipeDataJsonString = gson.toJson(externalPipeData);
            String externalPipeDataWebServiceEndpoint = PIPES_EXTERNAL_ENDPOINT
                    .replace("<endpoint>", targetService.getServiceEndpoint().getEndpointAddress())
                    .replace("<serviceidentifier>", targetService.getServiceIdentifier());

            Response externalInfoResponse = put(externalPipeDataWebServiceEndpoint, externalPipeDataJsonString);

            return Observable.just(externalInfoResponse);
        });
    }

    public Observable<Response> updateGlobalFilter(DCAGlobalFilter globalFilter) {
        Observable<DCASelectedService> targetServiceObservable = DCAUtils.getTargetService();

        return targetServiceObservable.flatMap(selectedService -> {
            Gson gson = new Gson();
            String jsonString = gson.toJson(globalFilter);
            String webserviceUrl = GLOBAL_FILTER_ENDPOINT
                    .replace("<endpoint>", selectedService.getServiceEndpoint().getEndpointAddress())
                    .replace("<serviceidentifier>", selectedService.getServiceIdentifier());

            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Url : %s ans jsonString to update %s", webserviceUrl, jsonString));
            }

            Response response = put(webserviceUrl, jsonString);

            return Observable.just(response);
        });
    }

    public Observable<DCAAggregateInfo> getServiceConfig(String aggregateName, String type, Integer number) {
        Observable<DCASelectedService> selectedServiceObservable = DCAUtils.getTargetService();

        return selectedServiceObservable.flatMap(dcaSelectedService -> {
            String webserviceUrl = String.format("%s/recapi/1.0/aggregateinfo/%s/%s/%s/%s/%s/%s",
                    dcaSelectedService.getServiceEndpoint().getEndpointAddress(), dcaSelectedService.getServiceIdentifier(),
                    aggregateName, type, number, type, number);

            Observable<Response> responseObservable = getResponse(webserviceUrl);

            return responseObservable.flatMap(response -> {
                String jsonString = response.readEntity(String.class);
                Gson gson = new Gson();
                DCAAggregateInfo dcaAggregateInfo = gson.fromJson(jsonString, DCAAggregateInfo.class);
                return Observable.just(dcaAggregateInfo);
            });
        });
    }
}
