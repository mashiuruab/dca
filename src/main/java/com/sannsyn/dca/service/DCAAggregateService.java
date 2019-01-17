package com.sannsyn.dca.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sannsyn.dca.model.config.DCASelectedService;
import com.sannsyn.dca.model.config.DCAServiceEndpoint;
import com.sannsyn.dca.vaadin.servlet.DCAUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.Func1;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import java.util.function.Function;

import static javax.ws.rs.core.Response.Status.OK;

/**
 * Query for getting items via the aggregates.
 * <p/>
 * Created by jobaer on 4/12/16.
 */
public class DCAAggregateService extends DCAAbstractRestService<Response> {
    private static final Logger logger = LoggerFactory.getLogger(DCAAggregateService.class.getName());

    private static final String ENTITY_ENDPOINT_TEMPLATE =
        "<endpoint>/recapi/1.0/entity/<servicename>/<name>/<identity>/<sortBy>/<size>";

    private static final String CLUSTER_ENDPOINT_TEMPLATE =
        "<endpoint>/recapi/1.0/cluster/<servicename>/<name>/<identity>/<sortBy>/<size>";

    private Function<Response, Response> identity = response -> response;
    private Func1<Response, String> responseTransformer = response -> {
        int status = response.getStatus();
        if (status == Response.Status.OK.getStatusCode()) {
            return response.readEntity(String.class);
        } else {
            String errorMsg = response.readEntity(String.class);
            throw new RuntimeException(errorMsg);
        }
    };

    public Observable<String> getEntityAggregateResponse(AggregateQuery query) {
        Observable<Response> responseObservable = getEntityResponse(query);
        return responseObservable.map(responseTransformer);
    }

    public Observable<String> getAggregateResponse(AggregateQuery query) {
        Observable<Response> responseObservable = getEitherResponse(query);
        return responseObservable.map(responseTransformer);
    }

    private Observable<Response> getEitherResponse(AggregateQuery query) {
        return getEntityResponse(query).zipWith(getClusterResponse(query),
            (response, response2) -> response.getStatus() == OK.getStatusCode() ? response : response2);
    }

    private Observable<Response> getEntityResponse(AggregateQuery query) {
        return getResponse(query, ENTITY_ENDPOINT_TEMPLATE);
    }

    private Observable<Response> getClusterResponse(AggregateQuery query) {
        return getResponse(query, CLUSTER_ENDPOINT_TEMPLATE);
    }

    private Observable<Response> getResponse(AggregateQuery query, String urlTemplate) {
        return buildUrlFromQuery(urlTemplate, query).flatMap(url -> doRxGet(url, identity));
    }

    private String buildUrl(String template, AggregateQuery query, String endpoint, String name) {
        return template
            .replace("<endpoint>", endpoint)
            .replace("<servicename>", name)
            .replace("<name>", query.getName())
            .replace("<identity>", query.getId())
            .replace("<sortBy>", query.getSortBy().toString())
            .replace("<size>", query.getSize());
    }

    private Observable<String> buildUrlFromQuery(String urlTemplate, AggregateQuery query) {
        return DCAUtils.getTargetService().flatMap(dcaSelectedService -> {
            DCAServiceEndpoint endpoint = dcaSelectedService.getServiceEndpoint();
            String url = buildUrl(urlTemplate, query, endpoint.getEndpointAddress(), dcaSelectedService.getServiceIdentifier());
            logger.debug("Url:" + url);
            return Observable.just(url);
        });
    }

    private Observable<String> buildUrlFromQuery(String urlTemplate, AggregateQuery query, DCASelectedService dcaSelectedService) {
        DCAServiceEndpoint endpoint = dcaSelectedService.getServiceEndpoint();
        String url = buildUrl(urlTemplate, query, endpoint.getEndpointAddress(), dcaSelectedService.getServiceIdentifier());
        logger.debug("Url:" + url);
        return Observable.just(url);
    }

    private Observable<AggregateQuery> getPopularityQuery(String entityId, String popularityCountAggregate) {
        AggregateQuery query = new AggregateQuery(popularityCountAggregate, entityId, SortBy.ID,"1");
        return Observable.just(query);
    }

    public Observable<Double> getPopularity(@NotNull String entityId, DCASelectedService selectedService) {
        return getPopularityQuery(entityId, selectedService.getAccount().getPopularityCountAggregate())
            .flatMap(query -> buildUrlFromQuery(ENTITY_ENDPOINT_TEMPLATE, query, selectedService))
            .flatMap(url -> doRxGet(url, identity).map(responseTransformer).map(this::parseResponse));
    }

    private Double parseResponse(String resp) {
        try {
            JsonParser parser = new JsonParser();
            JsonObject asJsonObject = parser.parse(resp).getAsJsonObject();
            if (asJsonObject.has("popularity")) {
                return asJsonObject.get("popularity").getAsDouble();
            }
        } catch (Exception e) {
            logger.error("Exception while parsing response ", e);
        }
        return 0.0;
    }
}
