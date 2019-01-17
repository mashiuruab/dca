package com.sannsyn.dca.service;

import com.google.gson.*;
import com.sannsyn.dca.metadata.DCAItem;
import com.sannsyn.dca.model.config.DCASelectedService;
import com.sannsyn.dca.model.config.DCAServiceEndpoint;
import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.vaadin.servlet.DCAUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.Func1;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Query for getting items via the recommenders.
 * <p/>
 * Created by jobaer on 4/12/16.
 */
public class DCARecommenderService extends DCAAbstractRestService<String> {
    private static final Logger logger = LoggerFactory.getLogger(DCARecommenderService.class.getName());

    private DCAConfigService configService = new DCAConfigService();
    private static final String recommend_endpoint = "<endpoint>/recapi/1.0/recommend";
    private static final String miprecommend_endpoint = "<endpoint>/recapi/1.0/miprecommend";
    private static final String presence_check_template = "<endpoint>/recapi/1.0/cluster/<servicename>/<name>/<id>";

    private Func1<Pair<Status, String>, List<DCAItem>> resultTransformer = resultPair -> {
        if (resultPair.getLeft().equals(Status.SUCCESS)) {
            return parseJsonResponse(resultPair.getRight());
        } else {
            logger.warn(resultPair.getLeft() + ": " + resultPair.getRight());
            return Collections.emptyList();
        }
    };

    private DCAUser loggedInUser;

    public DCARecommenderService(DCAUser loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

    public void setLoggedInUser(DCAUser loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

    public Observable<List<DCAItem>> getRecommenderResponse(String recommenderName, List<String> ids, String count) {
        return configService.getSelectedService(loggedInUser).flatMap(dcaSelectedService -> {
            DCAServiceEndpoint endpoint = dcaSelectedService.getServiceEndpoint();
            String endpointAddress = endpoint.getEndpointAddress();
            String url = recommend_endpoint.replace("<endpoint>", endpointAddress);
            logger.debug("Recommender url:" + url);

            String postData = createPostDataForRecommender(dcaSelectedService.getServiceIdentifier(), recommenderName, ids, count);
            logger.debug("postData = " + postData);
            Observable<Pair<Status, String>> pairObservable = doRxPost(postData, url);
            return pairObservable.map(resultTransformer);
        });
    }

    public Observable<List<DCAItem>> getShopHelperRecommendations(String name, List<String> ids) {
        return DCAUtils.getTargetService().flatMap(dcaSelectedService -> {
            DCAServiceEndpoint endpoint = dcaSelectedService.getServiceEndpoint();
            String endpointAddress = endpoint.getEndpointAddress();
            String url = miprecommend_endpoint.replace("<endpoint>", endpointAddress);
            logger.debug("Recommender url:" + url);

            String putData = createByItemQuery(dcaSelectedService.getServiceIdentifier(), name, ids, "10");
            logger.debug("postData = " + putData);
            Observable<Pair<Status, String>> pairObservable = doRxPut(putData, url);
            return pairObservable.map(resultTransformer);
        });
    }

    Observable<List<String>> getRecommendedCustomers(String recommenderName, int numberOfCustomers, List<String> ids) {
        String count = String.format("%d", numberOfCustomers);
        Observable<List<DCAItem>> recommenderResponse = getRecommenderResponse(recommenderName, ids, count);
        return recommenderResponse.map(itemList -> {
            List<String> result = new ArrayList<>(itemList.size());
            itemList.forEach(item -> result.add(item.getId()));
            return result;
        });
    }

    private Observable<String> fetchResult(String url) {
        return doRxGet(url, response -> {
            int status = response.getStatus();
            if (status == Response.Status.OK.getStatusCode()) {
                return response.readEntity(String.class);
            } else {
                String errorMsg = response.readEntity(String.class);
                throw new RuntimeException(errorMsg);
            }
        });
    }

    Observable<Boolean> isPresentInRecommender(DCAItem item, DCASelectedService dcaSelectedService) {
        String presenceCheckAggregate = dcaSelectedService.getAccount().getPresenceCheckAggregate();
        System.out.println("presenceCheckAggregate = " + presenceCheckAggregate);
        if (StringUtils.isNotBlank(presenceCheckAggregate)) {
            Observable<String> customerBook = getPresenceCheckResponse(presenceCheckAggregate, dcaSelectedService, item.getId());
            return customerBook.doOnNext(System.out::println).
                flatMap(s -> Observable.just(true)).onErrorResumeNext(Observable.just(false));
        } else {
            return Observable.just(false);
        }
    }

    private Observable<String> getPresenceCheckResponse(String name, DCASelectedService selectedService, String id) {
        DCAServiceEndpoint endpoint = selectedService.getServiceEndpoint();
        String url = presence_check_template
            .replace("<endpoint>", endpoint.getEndpointAddress())
            .replace("<servicename>", selectedService.getServiceIdentifier())
            .replace("<name>", name)
            .replace("<id>", id);
        logger.debug("Url:" + url);

        return fetchResult(url);
    }

    // todo Unit test the parsing, both standard and alternate version
    private List<DCAItem> parseJsonResponse(String response) {
        logger.debug("Json to parse for recommender response");
        logger.debug(response);
        List<DCAItem> customerIds = new ArrayList<>();

        try {
            JsonElement jelement = new JsonParser().parse(response);
            JsonObject rootObject = jelement.getAsJsonObject();
            if (rootObject.has("jsonResponseStr")) {
                logger.debug("Json object has jsonResponseStr property. Using standard parsing mechanism.");
                doStandardParsingItem(rootObject, customerIds);
            } else {
                logger.debug("Json object does not have jsonResponseStr property. Using alternative parsing mechanism.");
                doAlternateParsingItem(rootObject, customerIds);
            }
        } catch (Exception e) {
            logger.error("Error while parsing json", e);
        }

        return customerIds;
    }

    private void doAlternateParsingItem(JsonObject rootObject, List<DCAItem> customerIds) {
        if (rootObject.has("result") && rootObject.get("result").isJsonArray()) {
            JsonArray popResult = rootObject.getAsJsonArray("result");
            for (int i = 0; i < popResult.size(); i++) {
                JsonElement jsonElement = popResult.get(i);
                String id = jsonElement.getAsString();
                DCAItem dcaItem = new DCAItem();
                dcaItem.setId(id);
                customerIds.add(dcaItem);
            }
        } else {
            logger.debug("result property is missing or not an array, returning.");
        }
    }

    private void doStandardParsingItem(JsonObject rootObject, List<DCAItem> customerIds) {
        JsonElement jsonResponseStr = rootObject.get("jsonResponseStr");
        if (!jsonResponseStr.isJsonObject()) {
            logger.debug("jsonResponseStr is not a valid object, returning.");
            return;
        }

        JsonObject jsonReponseStrObject = rootObject.getAsJsonObject("jsonResponseStr");
        if (!jsonReponseStrObject.has("result")) {
            logger.debug("jsonResponseStr does not contain result, returning.");
            return;
        }

        JsonElement resultElement = jsonReponseStrObject.get("result");
        if (!resultElement.isJsonArray()) {
            logger.debug("jsonResponseStr.result is not an array, returning.");
            return;
        }

        JsonArray popResult = jsonReponseStrObject.getAsJsonArray("result");
        for (int i = 0; i < popResult.size(); i++) {
            JsonElement jsonElement = popResult.get(i);
            String id = jsonElement.getAsJsonObject().get("value").getAsString();
            DCAItem dcaItem = new DCAItem();
            dcaItem.setId(id);

            if (jsonElement.getAsJsonObject().has("weight")) {
                float score = jsonElement.getAsJsonObject().get("weight").getAsFloat();
                dcaItem.setScore(score);
            }

            customerIds.add(dcaItem);
        }
    }

    // sample:
    // {"service":"ark", "recommender":"ShopperhelperByItems","number":10, "externalIds":["9788202447953","9788205457409","9788202472061"]}
    private String createByItemQuery(String serviceIdentifier, String recommenderName, List<String> ids, String count) {
        JsonObject root = new JsonObject();

        root.addProperty("number", count);
        root.addProperty("recommender", recommenderName);
        root.addProperty("service", serviceIdentifier);

        JsonArray array = new JsonArray();
        ids.forEach(array::add);
        root.add("externalIds", array);

        Gson gson = new Gson();
        return gson.toJson(root);
    }

    private String createPostDataForRecommender(String serviceName, String recommenderName, List<String> ids, String count) {
        JsonObject root = new JsonObject();

        JsonObject n = new JsonObject();
        n.addProperty("n", "75");
        root.add("bm25", n);

        root.addProperty("debug", true);
        root.addProperty("number", count);
        root.addProperty("recommendername", recommenderName);
        root.addProperty("service", serviceName);

        JsonObject n2 = new JsonObject();
        n2.addProperty("n", 20);
        root.add("keywordextractor", n2);

        JsonArray array = new JsonArray();
        ids.forEach(array::add);
        root.add("externalIds", array);

        Gson gson = new Gson();
        return gson.toJson(root);
    }
}
