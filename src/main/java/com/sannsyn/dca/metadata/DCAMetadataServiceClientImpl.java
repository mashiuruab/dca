package com.sannsyn.dca.metadata;

import com.google.gson.*;
import com.sannsyn.dca.model.config.DCASelectedService;
import com.sannsyn.dca.util.DCAConfigProperties;
import com.sannsyn.dca.vaadin.servlet.DCAUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.rx.rxjava.RxObservable;
import org.glassfish.jersey.client.rx.rxjava.RxObservableInvoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.Func1;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * An implementation of the metadata service client.
 * <p>
 * Created by jobaer on 5/31/16.
 */
public class DCAMetadataServiceClientImpl implements DCAMetadataServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(DCAMetadataServiceClientImpl.class);

    private static final String API_ENDPOINT = DCAConfigProperties.getMetaDataServerUrl().orElse("");
    private static final String BULK = "/bulk";
    private static final String METRICS = "/scraping-metrics";
    private static final int RETRY_COUNT = 3;
    private Gson gson = new Gson();
    private static Client client;

    private Func1<Response, Observable<? extends String>> responseTransformer = r -> {
        int status = r.getStatus();
        if (status == Response.Status.OK.getStatusCode()) {
            String resp = r.readEntity(String.class);
            return Observable.just(resp);
        } else {
            return Observable.error(new RuntimeException("Error with status code " + status));
        }
    };

    private DCASelectedService targetService;

    static {
        ClientConfig configuration = new ClientConfig();
        configuration.property(ClientProperties.CONNECT_TIMEOUT, 5000);
        configuration.property(ClientProperties.READ_TIMEOUT, 5000);
        client = ClientBuilder.newClient(configuration);
    }


    @Override
    public Observable<DCAMetadataResponse> getMetadataItems(List<String> itemIds) {
        Observable<DCASelectedService> selectedServiceObservable = DCAUtils.getTargetService();

        return selectedServiceObservable.flatMap(selectedService -> {
            this.targetService = selectedService;
            Observable<DCAMetadataResponse> metadataResponseObservable = getMetadataItems(itemIds, RETRY_COUNT);

            return metadataResponseObservable.filter(metadataResponse -> !"in-progress".equals(metadataResponse.getStatus()));
        });
    }


    @Override
    public Observable<DCAMetadataResponse> getAllMetadataItems(List<String> itemIds) {
        Observable<DCASelectedService> selectedServiceObservable = DCAUtils.getTargetService();

        return selectedServiceObservable.flatMap(selectedService -> {
            this.targetService = selectedService;
            return getMetadataItems(itemIds, RETRY_COUNT);
        });
    }

    private Observable<DCAMetadataResponse> getMetadataItems(List<String> itemIds, int retryCount) {
        if (retryCount <= 0) {
            return Observable
                    .from(itemIds)
                    .map(itemId -> new DCAMetadataResponse(itemId, "in-progress"));
        }

        String lookupQuery = createLookupQuery(itemIds);

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Lookup Query = %s", lookupQuery));
        }
        Observable<String> response = getResponse(lookupQuery);

        return response.flatMap(r -> {
            Observable<DCAMetadataResponse> successItems = getSuccessItems(itemIds, r);
            Observable<DCAMetadataResponse> inprogressItems = getInprogressItems(itemIds, r, retryCount - 1);
            return successItems.mergeWith(inprogressItems);
        });
    }

    @Override
    public Observable<JsonObject> getScrapingMetrics() {
        return getScrapingMetricsResponse().flatMap(
            response -> DCAUtils.getTargetService().map(targetService -> {
                String serviceIdentifier = targetService.getServiceIdentifier();
                return parseScrapingMetrics(response, serviceIdentifier);
            }));
    }

    private JsonObject parseScrapingMetrics(String response, String serviceName) {
        JsonParser parser = new JsonParser();
        JsonElement parse = parser.parse(response);
        if (parse.getAsJsonObject().has(serviceName)) {
            JsonElement jsonElement = parse.getAsJsonObject().get(serviceName);
            if (jsonElement.isJsonObject()) {
                return jsonElement.getAsJsonObject();
            } else {
                logger.debug("Scraping metrics is not a json object, returning empty.");
            }
        } else {
            logger.debug("Scraping metrics does not return data for " + serviceName);
        }

        return new JsonObject();
    }

    private Observable<String> getScrapingMetricsResponse() {
        return createMetadataUrl(METRICS).flatMap(this::doGet).flatMap(responseTransformer);
    }

    private Observable<String> createMetadataUrl(String resource) {
        return getAPIUrl().flatMap(apiUrl -> {
            String metadataServerUrl = StringUtils.isEmpty(apiUrl) ? API_ENDPOINT : apiUrl;
            if (StringUtils.isEmpty(metadataServerUrl)) {
                logger.error("Error :: No Default Meta Data Server Url found, In the Configuration file " +
                    "there might be some error");
                return Observable.empty();
            }

            if (logger.isDebugEnabled()) {
                logger.debug(String.format("MetaDataService Url : %s", metadataServerUrl));
            }

            return Observable.just(metadataServerUrl + resource);
        });
    }


    private Observable<Response> doGet(String url) {
        return createRxInvoker(url).get();
    }

    private Observable<Response> doPost(String url, Entity<String> entity) {
        return createRxInvoker(url).post(entity);
    }

    private RxObservableInvoker createRxInvoker(String url) {
        return RxObservable.from(client)
            .target(url)
            .request()
            .header(HttpHeaders.CONTENT_TYPE, "application/json")
            .accept("application/json")
            .rx();
    }

    private Observable<String> getResponse(String lookupQuery) {
        Entity<String> entity = Entity.entity(lookupQuery, MediaType.APPLICATION_JSON);
        return createMetadataUrl(BULK).flatMap(
            url -> doPost(url, entity)).flatMap(responseTransformer);
    }

    @Override
    public Observable<DCAMetadataResponse> getMetadataItem(String itemId) {
        List<String> ids = Collections.singletonList(itemId);
        return getMetadataItems(ids);
    }

    @Override
    public Observable<DCAMetadataResponse> search(String query) {
        Observable<DCASelectedService> selectedServiceObservable = DCAUtils.getTargetService();

        return selectedServiceObservable.flatMap(selectedService -> {
            this.targetService = selectedService;

            String searchQuery = createSearchQuery(query);

            if (logger.isDebugEnabled()) {
                logger.debug(String.format("searchQuery = %s", searchQuery));
            }

            Observable<String> response = getResponse(searchQuery);
            return response.flatMap(this::parseSearchResponse);
        });
    }

    private Observable<DCAMetadataResponse> parseSearchResponse(String resp) {
        JsonParser parser = new JsonParser();
        JsonElement parse = parser.parse(resp);

        JsonArray outer = parse.getAsJsonArray();
        JsonArray result = outer.get(0).getAsJsonArray();

        if (result.size() == 0) {
            return Observable.empty();
        }

        List<DCAMetadataResponse> metadata = new ArrayList<>();
        for (int i = 0; i < result.size(); i++) {
            DCAMetadataResponse metadataResponse = new DCAMetadataResponse();
            metadataResponse.setStatus("success");
            parseSingle(metadataResponse, result.get(i).getAsJsonArray());
            metadata.add(metadataResponse);
        }

        return Observable.from(metadata);
    }

    private String createLookupQuery(List<String> itemIds) {
        List<Object> outer = new ArrayList<>();

        for (String id : itemIds) {
            Map<String, Object> props = buildProps();
            props.put("id", id);

            List<Object> inner = new ArrayList<>();
            inner.add("lookup");

            inner.add(props);
            outer.add(inner);
        }

        return gson.toJson(outer);
    }

    private String createSearchQuery(String term) {
        List<Object> outer = new ArrayList<>();

        Map<String, Object> props = buildProps();
        props.put("query", term);

        List<Object> inner = new ArrayList<>();
        inner.add("search");

        inner.add(props);
        outer.add(inner);

        return gson.toJson(outer);
    }

    private Map<String, Object> buildProps() {
        Map<String, Object> props = new HashMap<>();
        String serviceIdentifier = targetService.getServiceIdentifier();
        String accountName = serviceIdentifier;

        if (DCAMetadataConfig.getQueryItemMap(serviceIdentifier).containsKey("account")) {
            accountName = DCAMetadataConfig.getQueryItemMap(serviceIdentifier).get("account");
        }
        props.put("account", accountName);
        props.put("taxon", DCAMetadataConfig.getQueryItemMap(serviceIdentifier).get("taxon"));
        props.put("pointers", DCAMetadataConfig.getPointers(serviceIdentifier));
        return props;
    }

    private Observable<DCAMetadataResponse> getSuccessItems(List<String> itemIds, String response) {
        List<DCAMetadataResponse> dcaMetadataResponses = parseForSuccess(itemIds, response);
        return Observable.from(dcaMetadataResponses);
    }

    private List<DCAMetadataResponse> parseForSuccess(List<String> itemIds, String response) {
        ArrayList<DCAMetadataResponse> dcaMetadataResponses = new ArrayList<>();
        JsonParser parser = new JsonParser();
        JsonElement parse = parser.parse(response);
        JsonArray outerObject = parse.getAsJsonArray();

        int index = 0;
        for (JsonElement jsonElement : outerObject) {
            JsonArray inner = jsonElement.getAsJsonArray();
            String status = inner.get(0).getAsString();
            if ("success".equals(status)) {
                DCAMetadataResponse success = createMetadataResponse(itemIds, index, inner);
                dcaMetadataResponses.add(success);
            } else if ("failure".equals(status)) {
                String id = itemIds.get(index);
                DCAMetadataResponse failure = new DCAMetadataResponse(id, "failure");
                dcaMetadataResponses.add(failure);
            }

            index++;
        }

        return dcaMetadataResponses;
    }

    private DCAMetadataResponse createMetadataResponse(List<String> itemIds, int index, JsonArray inner) {
        DCAMetadataResponse metadataResponse = new DCAMetadataResponse();
        metadataResponse.setStatus("success");
        String id = itemIds.get(index);
        metadataResponse.setId(id);

        JsonArray jsonArray = inner.get(1).getAsJsonArray();
        parseSingle(metadataResponse, jsonArray);

        return metadataResponse;
    }

    private void parseSingle(DCAMetadataResponse metadataResponse, JsonArray jsonArray) {
        int idx = 0;

        String serviceIdentifier = targetService.getServiceIdentifier();
        List<String> fields = DCAMetadataConfig.getFields(serviceIdentifier);

        for (JsonElement jsonElement : jsonArray) {
            if (jsonElement.isJsonNull()) {
                idx++;
                continue;
            }

            String name = fields.get(idx);

            if (DCAMetadataConfig.getQueryItemMap(serviceIdentifier).containsKey(name)) {
                name = DCAMetadataConfig.getQueryItemMap(serviceIdentifier).get(name);
            }

            if ("author".equals(name) && jsonElement.isJsonArray()) {
                JsonArray asJsonArray = jsonElement.getAsJsonArray();
                if (asJsonArray.size() > 0) {
                    String firstAuthor = asJsonArray.get(0).getAsString();
                    metadataResponse.addProperty(name, firstAuthor);
                }
            } else {
                String value = jsonElement.getAsString();
                metadataResponse.addProperty(name, value);
            }
            idx++;
        }
    }

    private Observable<DCAMetadataResponse> getInprogressItems(List<String> itemIds, String response, int retryCount) {
        List<String> inprogressIds = new ArrayList<>();
        JsonParser parser = new JsonParser();
        JsonElement parse = parser.parse(response);
        JsonArray outerObject = parse.getAsJsonArray();

        int index = 0;
        for (JsonElement jsonElement : outerObject) {
            JsonArray inner = jsonElement.getAsJsonArray();
            String status = inner.get(0).getAsString();
            if ("in-progress".equals(status)) {
                String s = itemIds.get(index);
                inprogressIds.add(s);
            }
            index++;
        }

        return inprogressIds.isEmpty() ? Observable.empty() : delayedRequest(3, getMetadataItems(inprogressIds, retryCount));
    }

    private Observable<DCAMetadataResponse> delayedRequest(long delay, Observable<DCAMetadataResponse> source) {
        Observable<Long> interval = Observable.interval(delay, TimeUnit.SECONDS).take(1);
        return interval.flatMap(aLong -> source);
    }

    private Observable<String> getAPIUrl() {
        Observable<DCASelectedService> selectedServiceObservable = DCAUtils.getTargetService();
        return selectedServiceObservable.flatMap(dcaSelectedService -> {
            return Observable.just(dcaSelectedService.getMetaDataServerUrl());
        });
    }
}
