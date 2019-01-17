package com.sannsyn.dca.service;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.client.rx.rxjava.RxObservable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import static com.sannsyn.dca.service.Status.FAILURE;
import static com.sannsyn.dca.service.Status.SUCCESS;
import static com.sannsyn.dca.util.DCAConfigProperties.getWsPassword;
import static com.sannsyn.dca.util.DCAConfigProperties.getWsUserName;

/**
 * Abstract service for Querying via http GET
 * <p/>
 * Created by jobaer on 4/12/16.
 */
public abstract class DCAAbstractRestService<T> {
    private static final Logger logger = LoggerFactory.getLogger(DCAAbstractRestService.class);
    private static Client client;

    static {
        ClientConfig configuration = new ClientConfig();
        configuration.property(ClientProperties.CONNECT_TIMEOUT, 10000);
        configuration.property(ClientProperties.READ_TIMEOUT, 10000);
        client = ClientBuilder.newClient(configuration);
        HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(getWsUserName(), getWsPassword());
        client.register(feature);
    }

    private Client getClient() {
        return client;
    }

    protected Observable<T> doRxGet(String url, Function<Response, T> transformer) {
        Observable<Response> observable = RxObservable.from(getClient())
            .target(url)
            .request()
            .rx()
            .get();
        return observable.map(transformer::apply);
    }

    protected Observable<Pair<Status, String>> doRxPost(String jsonData, String url) {
        return RxObservable.from(getClient())
            .target(url)
            .request()
            .rx()
            .post(Entity.entity(jsonData, MediaType.APPLICATION_JSON))
            .map(this::getResponseTransformer)
            .onErrorReturn(e -> new ImmutablePair<>(FAILURE, e.getMessage()));
    }

    protected Observable<Pair<Status, String>> doRxPut(String jsonData, String url) {
        return RxObservable.from(getClient())
            .target(url)
            .request()
            .rx()
            .put(Entity.entity(jsonData, MediaType.APPLICATION_JSON))
            .map(this::getResponseTransformer)
            .onErrorReturn(e -> new ImmutablePair<>(FAILURE, e.getMessage()));
    }

    private Pair<Status, String> getResponseTransformer(Response response) {
        String responseString = response.readEntity(String.class);
        if (response.getStatus() != 200) {
            logger.error("Failed : HTTP error code : " + response.getStatus());
            return new ImmutablePair<>(FAILURE, response.getStatus() + ": " + responseString);
        }
        return new ImmutablePair<>(SUCCESS, responseString);
    }

    Pair<Status, String> doPutRequest(String jsonString, String url) {
        Response response;
        try {
            HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(getWsUserName(), getWsPassword());
            Client client = ClientBuilder.newClient();
            client.register(feature);

            WebTarget webTarget = client.target(url);
            response = webTarget.request().header(HttpHeaders.CONTENT_TYPE, "application/json")
                .accept("application/json").put(Entity.entity(jsonString, MediaType.APPLICATION_JSON));

            return getResponseTransformer(response);
        } catch (Exception e) {
            logger.error("Error occurred", e);
            return new ImmutablePair<>(FAILURE, e.getMessage());
        }
    }
}
