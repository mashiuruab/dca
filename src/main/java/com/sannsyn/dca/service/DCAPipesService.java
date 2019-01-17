package com.sannsyn.dca.service;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.vaadin.pipes.DCAPipeEditForm;
import com.sannsyn.dca.vaadin.widgets.controller.overview.model.aggregates.DCAPipe;
import com.sannsyn.dca.vaadin.widgets.controller.overview.model.aggregates.DCAPipeInstanceType;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAPipeLineObject;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAServiceConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.observables.BlockingObservable;

import javax.ws.rs.core.Response;
import java.util.*;

import static com.sannsyn.dca.service.Status.FAILURE;
import static com.sannsyn.dca.service.Status.SUCCESS;
import static com.sannsyn.dca.service.Status.WARNING;
import static com.sannsyn.dca.vaadin.widgets.controller.overview.model.aggregates.DCAPipeInstanceType.IMPLEMENTATION;
import static java.util.Comparator.*;
import static java.util.stream.Collectors.toList;

/**
 * This service is used to search for and create/update of pipes (recommender components).
 * <p>
 * Created by jobaer on 5/5/16.
 */
public class DCAPipesService extends DCAAbstractRestService<String> {
    public static final String PIPES_KEY = "pipes";

    private static final Logger logger = LoggerFactory.getLogger(DCAPipesService.class);
    private final DCAPipesConfigParser configParser;
    private DCAConfigService configService = new DCAConfigService();
    private static final String URL_TEMPLATE = "<endpoint>/recapi/1.0/modify/<servicename>/ensembleelementsource/<name>";
    private DCAUser loggedInUser;

    public DCAPipesService(DCAUser loggedInUser) {
        this.loggedInUser = loggedInUser;
        this.configParser = new DCAPipesConfigParser();
    }

    private String buildUrl(String name) {
        DCAConfigService configService = new DCAConfigService();
        Observable<String> stringObservable = configService.getSelectedService(loggedInUser).flatMap(dcaSelectedService -> {
            String endpointAddress = dcaSelectedService.getServiceEndpoint().getEndpointAddress();
            String serviceIdentifier = dcaSelectedService.getServiceIdentifier();
            String url = URL_TEMPLATE.replace("<endpoint>", endpointAddress).replace("<servicename>", serviceIdentifier).replace("<name>", name);
            return Observable.just(url);
        });
        // todo - temporary blocking call. Need to fix.
        return BlockingObservable.from(stringObservable).first();
    }

    private Observable<List<DCAPipe>> searchInClasses(String query, String type) {
        Observable<List<DCAPipe>> allClasses = new DCAPipesConfigParser().getAllClassDefinitions();
        return allClasses
            .map(pipeList -> pipeList.stream()
                .filter(item -> item.matchesNameOrClass(query))
                .filter(item -> item.matchesType(type))
                .collect(toList()));
    }

    public Observable<DCAPipe> getClass(String className, DCAPipeInstanceType pipeInstanceType) {
        Observable<List<DCAPipe>> allClasses = new DCAPipesConfigParser().getAllClassDefinitions();

        return allClasses.map(pipeList -> pipeList.stream()
                   .filter(pipe -> className.equals(pipe.getClazz()))
                   .filter(pipe -> pipe.getComponentType().equals(pipeInstanceType))
                   .findFirst().get());
    }

    public Observable<List<DCAPipe>> search(String query, String type, DCAUser loggedInUser) {
        return searchInClasses(query, type)
            .flatMap(list -> configService.getServiceConfig(loggedInUser).map(dcaServiceConfigWrapper -> {
                Map<String, JsonObject> sources = dcaServiceConfigWrapper.getService().getEnsembles().getElements().getSources();
                List<DCAPipe> recommenderComponents = getRecommenderComponents(sources);
                recommenderComponents.addAll(list);
                List<DCAPipe> pipes = recommenderComponents.stream()
                    .filter(item -> item.matchesNameOrClass(query))
                    .filter(item -> item.matchesType(type))
                    .collect(toList());
                return sortResult(pipes);
            }));
    }

    public Observable<List<DCAPipe>> getRecommenderComponents(List<String> pipeComponentNameList) {
        Map<String, JsonObject> sourceMap = new HashMap<>();

        return configService.getServiceConfig(loggedInUser).flatMap(serviceConfigWrapper -> {
            DCAServiceConfig serviceConfig = serviceConfigWrapper.getService();
            for (String filterName : pipeComponentNameList) {
                if (serviceConfig.getEnsembles().getElements().getSources().containsKey(filterName)) {
                    sourceMap.put(filterName, serviceConfig.getEnsembles().getElements().getSources().get(filterName));
                }
            }

            List<DCAPipe> pipeItemList = getRecommenderComponents(sourceMap);

            return Observable.just(pipeItemList);
        });

    }

    public Map<String, DCAPipeLineObject> getPipeSources(Map<String, JsonObject> sources) {
        Map<String, DCAPipeLineObject> pipeSources = new TreeMap<>((o1, o2) -> o1.toLowerCase().compareTo(o2.toLowerCase()));
        for (Map.Entry<String, JsonObject> entry : sources.entrySet()) {

            String key = entry.getKey();
            if (key.equals("pipes")) {
                JsonObject pipeTypeObject = entry.getValue();
                for (Map.Entry<String, JsonElement> pipeTypeEntry : pipeTypeObject.entrySet()) {
                    String pipeKey = pipeTypeEntry.getKey();
                    if (pipeTypeEntry.getValue().isJsonObject()) {
                        DCAPipeLineObject dcaPipeLineObject = new DCAPipeLineObject();
                        dcaPipeLineObject.setClassName(String.valueOf(pipeTypeEntry.getValue().getAsJsonObject().get("class")));
                        dcaPipeLineObject.setType(configParser.getTypeFor(dcaPipeLineObject.getClassName()));
                        pipeSources.put(pipeKey, dcaPipeLineObject);
                    }
                }
            } else {
                JsonObject object = entry.getValue();
                DCAPipeLineObject dcaPipeLineObject = new DCAPipeLineObject();
                dcaPipeLineObject.setClassName(object.get("class").getAsString());
                dcaPipeLineObject.setType(configParser.getTypeFor(dcaPipeLineObject.getClassName()));
                pipeSources.put(key, dcaPipeLineObject);
            }

        }

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Pipesource Debug : %s", pipeSources));
        }

        return pipeSources;
    }

    private List<DCAPipe> getRecommenderComponents(Map<String, JsonObject> sources) {
        List<DCAPipe> result = new ArrayList<>();
        for (Map.Entry<String, JsonObject> entry : sources.entrySet()) {
            String name = entry.getKey();
            if ("pipes".equals(name)) {
                for (Map.Entry<String, JsonElement> elementEntry : entry.getValue().entrySet()) {
                    DCAPipe pipe = createPipeFromJson(elementEntry.getKey(), elementEntry.getValue().getAsJsonObject());
                    result.add(pipe);
                }
            } else {
                DCAPipe pipe = createPipeFromJson(name, entry.getValue());
                result.add(pipe);
            }
        }
        return result;
    }

    private DCAPipe createPipeFromJson(String name, JsonObject value) {
        DCAPipe pipe = new DCAPipe();
        pipe.setName(name);
        setPipeProperties(value, pipe);
        return pipe;
    }

    private void setPipeProperties(JsonObject value, DCAPipe pipe) {
        pipe.setComponentType(IMPLEMENTATION);
        pipe.setServerSideJson(value);

        JsonElement aClass = value.get("class");
        if (aClass != null) {
            String className = value.get("class").getAsString();
            pipe.setClazz(className);
            String description = configParser.extractDescription(className);
            pipe.setDescription(description);
            String typeFor = configParser.getTypeFor(className);
            pipe.setType(typeFor);
        }

        if (value.has(DCAPipeEditForm.KEY_COMPONENT_DESCRIPTION)) {
            String componentDescription = value.get(DCAPipeEditForm.KEY_COMPONENT_DESCRIPTION).getAsString();
            pipe.setComponentDescription(componentDescription);
        }
    }

    private List<DCAPipe> sortResult(List<DCAPipe> pipes) {
        Comparator<DCAPipe> comparator =
            comparing(DCAPipe::getUnqualifiedClassName, nullsLast(naturalOrder()))
                .thenComparing(DCAPipe::getComponentType)
                .thenComparing(DCAPipe::getName, String.CASE_INSENSITIVE_ORDER.thenComparing(nullsLast(naturalOrder())));
        pipes.sort(comparator);
        return pipes;
    }

    public Pair<Status, String> createUpdatePipe(Map<String, Object> formValues, String className) {
        String name = formValues.get("name").toString();

        if (StringUtils.isEmpty(name)) {
            return new ImmutablePair<>(FAILURE, "Got empty name, unable to create/update.");
        }

        String url = buildUrl(name);
        logger.debug("Post url " + url);
        String jsonString = createJsonString(formValues);
        logger.debug("This json will be posted");
        logger.debug(jsonString);

        Pair<Boolean, String> validationResult = validate(className, jsonString);
        Pair<Status, String> httpStatus = doPutRequest(jsonString, url);
        return mergeStatuses(validationResult, httpStatus);
    }

    private Pair<Status, String> mergeStatuses(Pair<Boolean, String> validationResult, Pair<Status, String> httpStatus) {
        if(SUCCESS.equals(httpStatus.getLeft())){
            if(validationResult.getLeft()) {
                return new ImmutablePair<>(SUCCESS, httpStatus.getRight());
            } else {
                String message = "Operation was successful with some validation problem.\n";
                return new ImmutablePair<>(WARNING, message + validationResult.getRight());
            }
        } else {
            return httpStatus;
        }
    }

    private Pair<Boolean, String> validate(String className, String jsonString) {
        Pair<Boolean, String> stausPair = configParser.validateAgainstSchema(jsonString, className);
        logger.debug("Validation status: " + stausPair.getLeft());
        logger.debug("Validation message: " + stausPair.getRight());
        return stausPair;
    }

    private String createJsonString(Map<String, Object> formValues) {
        Gson gson = new Gson();
        formValues.remove("name");
        return gson.toJson(formValues);
    }

    public Observable<Map<String, Object>> getExternalPipeData() {
        return configService.getServiceConfig(loggedInUser).flatMap(serviceConfigWrapper -> {
            if (serviceConfigWrapper.getService().getExternal().isPresent()
                    && serviceConfigWrapper.getService().getExternal().get().getDca().isPresent()
                    && serviceConfigWrapper.getService().getExternal().get().getDca().get().containsKey(PIPES_KEY)) {
                return Observable.just(serviceConfigWrapper.getService().getExternal().get().getDca().get().get(PIPES_KEY));
            } else if (serviceConfigWrapper.getService().getExternal().isPresent()) {
                Map<String, Map<String, Object>> emptyPipeMap = new HashMap<>();
                emptyPipeMap.put(PIPES_KEY, new HashMap<>());
                return Observable.just(emptyPipeMap.get(PIPES_KEY));
            } else {
                return Observable.just(Collections.emptyMap());
            }
        });
    }

    public Observable<Pair<Status, String>> createUpdateExternalPipeData(Map<String, Object> pipeExternalData) {
        Observable<Response> responseObservable = configService.createUpdateExternalPipeData(pipeExternalData);

        return responseObservable.flatMap(response -> {
            String responseMessage = response.readEntity(String.class);
            if (response.getStatus() == 200) {
                return Observable.just(new ImmutablePair<>(Status.SUCCESS, responseMessage));
            } else {
                return Observable.just(new ImmutablePair<>(Status.FAILURE, responseMessage));
            }
        });
    }
}
