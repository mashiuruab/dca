package com.sannsyn.dca.vaadin.inspectaggregate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sannsyn.dca.metadata.DCAItem;
import com.sannsyn.dca.metadata.DCAMetadataResponse;
import com.sannsyn.dca.metadata.DCAMetadataServiceClient;
import com.sannsyn.dca.metadata.DCAMetadataServiceClientImpl;
import com.sannsyn.dca.service.AggregateQuery;
import com.sannsyn.dca.service.DCAAggregateService;
import com.sannsyn.dca.service.SortBy;
import com.sannsyn.dca.vaadin.component.custom.container.ItemContainer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

/**
 * Helper class for the inspect aggregate component. It will communicate with the services,
 * do some json parsing and request the component to update according to the result.
 * <p>
 * Created by jobaer on 6/10/16.
 */
class DCAInspectAggregateHelper {
    private final DCAInspectAggregateUiHandler handler;
    private DCAMetadataServiceClient metadataServiceClient = new DCAMetadataServiceClientImpl();
    private DCAAggregateService aggregateService = new DCAAggregateService();
    private static final Logger logger = LoggerFactory.getLogger(DCAInspectAggregateHelper.class);

    DCAInspectAggregateHelper(DCAInspectAggregateUiHandler handler) {
        this.handler = handler;
    }

    void fetchResultAndUpdateUi(AggregateQuery query, ItemContainer<DCAItem> itemContainer) {
        Observable<String> aggregateResponse = aggregateService.getAggregateResponse(query);
        aggregateResponse.subscribe(
            resp -> updateResultUi(resp, query, itemContainer),
            e ->
            {
                String message = "<p>Error while trying to fetch result.</p><p>Root cause: <br/>"
                    + e.getMessage() + "</p>";
                handler.error(message);
                logger.error("Error while updating inspect aggregate ui. ", e);
            });
    }

    private void updateResultUi(String resp, AggregateQuery aggregateQuery, ItemContainer<DCAItem> itemContainer) {
        JsonParser parser = new JsonParser();
        JsonObject asJsonObject = parser.parse(resp).getAsJsonObject();
        JsonElement siblingsElement = asJsonObject.get("siblings");

        if (!siblingsElement.isJsonArray()) {
            String message = "Error parsing json value - 'siblings' is not an array";
            handler.error(message);
            return;
        }

        JsonArray siblings = siblingsElement.getAsJsonArray();
        if (siblings.size() == 0) {
            String message = "No result found for this aggregate.";
            handler.error(message);
            return;
        }

        for (JsonElement sibling : siblings) {
            JsonObject siblingObject = sibling.getAsJsonObject();
            DCAItem b = new DCAItem();
            addProperties(b, siblingObject);
            handler.addItemToContainer(b, itemContainer);
            requestMetadataAndAggregateData(b, itemContainer, aggregateQuery.getName());
        }
    }

    private void addProperties(DCAItem b, JsonObject siblingObject) {
        JsonElement externalId = siblingObject.get("externalId");
        String id = externalId.getAsString();
        b.setId(id);

        if (siblingObject.has("count")) {
            int count = siblingObject.get("count").getAsInt();
            b.setBoughtTogether(count);
        }
    }

    void requestMetadataAndAggregateData(DCAItem item, ItemContainer<DCAItem> bookContainer, String aggregateName) {
        Observable<DCAItem> bookMeta = getMetadataForBook(item.getId());
        Observable<DCAItem> bookOther = getAdditionalInfo(item, aggregateName);
        Observable<DCAItem> withErrorEmpty = bookOther.onExceptionResumeNext(Observable.just(new DCAItem()));
        Observable<DCAItem> bookObservable = bookMeta.zipWith(withErrorEmpty, this::mergeProperties);

        bookObservable.subscribe(bookFinal -> {
            handler.updateItemInContainer(bookFinal, bookContainer);
        }, e -> {
            logger.error("Unable to update metadata for inspect aggretate", e);
        });
    }

    private Observable<DCAItem> getAdditionalInfo(DCAItem item, String aggregateName) {
        AggregateQuery updatedQuery = new AggregateQuery(aggregateName, item.getId(), SortBy.POPULARITY, "1");
        Observable<String> aggregateResponse = aggregateService.getEntityAggregateResponse(updatedQuery);
        return aggregateResponse.map(resp -> createBookWithAdditionalProperties(item, resp));
    }

    private DCAItem createBookWithAdditionalProperties(DCAItem item, String resp) {
        DCAItem b = new DCAItem();
        b.setId(item.getId());
        b.setBoughtTogether(item.getBoughtTogether());

        JsonParser parser = new JsonParser();
        JsonObject asJsonObject = parser.parse(resp).getAsJsonObject();
        String description = asJsonObject.get("description").getAsString();

        int count = getCount(description);
        b.setCount(count);

        int size = getSize(description);
        b.setSize(size);

        Double popularity = getPopularity(description);
        b.setPopularity(popularity);

        return b;
    }

    private int getCount(String description) {
        String property = getProperty(description, "count=");
        try {
            return Integer.parseInt(property);
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }

    private int getSize(String description) {
        String property = getProperty(description, "size=");
        try {
            return Integer.parseInt(property);
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }

    private Double getPopularity(String description) {
        String property = getProperty(description, "popularity=");
        try {
            return Double.parseDouble(property);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    private String getProperty(String description, String property) {
        if (description.contains(property)) {
            int i = description.indexOf(property);
            String substring = description.substring(i + property.length(), description.length());
            int commaIndex = substring.indexOf(",");
            return substring.substring(0, commaIndex);
        } else {
            return "";
        }
    }

    private Observable<DCAItem> getMetadataForBook(String itemId) {
        Observable<DCAMetadataResponse> metadataItem = metadataServiceClient.getMetadataItem(itemId);
        return metadataItem.map(this::createBookFromMetadataResponse);
    }

    private DCAItem mergeProperties(DCAItem itemMeta, DCAItem item) {
        if (itemMeta != null && item != null) {
            if (StringUtils.isEmpty(itemMeta.getId())) {
                if (StringUtils.isNotEmpty(item.getId())) {
                    itemMeta.setId(item.getId());
                }
            }
            if (item.getCount() > 0) {
                itemMeta.setCount(item.getCount());
            }
            if (item.getPopularity() != null) {
                itemMeta.setPopularity(item.getPopularity());
            }
            if (item.getSize() > 0) {
                itemMeta.setSize(item.getSize());
            }
            if (item.getBoughtTogether() > 0) {
                itemMeta.setBoughtTogether(item.getBoughtTogether());
            }
        }

        return itemMeta;
    }

    private DCAItem createBookFromMetadataResponse(DCAMetadataResponse resp) {
        return DCAItem.fromMetadata(resp);
    }
}

interface DCAInspectAggregateUiHandler {
    void error(String message);

    void addItemToContainer(DCAItem item, ItemContainer<DCAItem> itemContainer);

    void updateItemInContainer(DCAItem item, ItemContainer<DCAItem> itemContainer);
}
