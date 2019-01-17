package com.sannsyn.dca.metadata;

import com.google.gson.JsonObject;
import rx.Observable;

import java.util.List;

/**
 * A client for communicating with the new metadata service.
 * <p>
 * Created by jobaer on 5/31/16.
 */
public interface DCAMetadataServiceClient {
    public Observable<DCAMetadataResponse> getMetadataItems(List<String> itemIds);

    public Observable<DCAMetadataResponse> getAllMetadataItems(List<String> itemIds);

    public Observable<DCAMetadataResponse> getMetadataItem(String itemId);

    public Observable<DCAMetadataResponse> search(String query);

    public Observable<JsonObject> getScrapingMetrics();
}
