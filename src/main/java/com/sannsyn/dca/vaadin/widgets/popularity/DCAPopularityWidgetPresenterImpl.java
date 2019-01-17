package com.sannsyn.dca.vaadin.widgets.popularity;

import com.google.gson.*;
import com.sannsyn.dca.metadata.*;
import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.service.DCAPopularityService;
import com.sannsyn.dca.service.DCAPopularityServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Presenter component of the widget
 * Created by jobaer on 3/9/16.
 */
public class DCAPopularityWidgetPresenterImpl implements DCAPopularityWidgetPresenter {
    private static final Logger logger = LoggerFactory.getLogger(DCAPopularityWidgetPresenterImpl.class);
    private DCAPopularityWidgetView view;
    private DCAUser loggedInUser;

    public DCAPopularityWidgetPresenterImpl(DCAPopularityService service, DCAPopularityWidgetView view,
                                            DCAUser loggedInUser) {
        this.view = view;
        this.loggedInUser = loggedInUser;
        initService();
    }

    private void initService() {
        Gson gson = new Gson();
        DCAPopularityService popularityService = new DCAPopularityServiceImpl(loggedInUser);
        Observable<String> popularItemsFinal = popularityService.getPopularItems();
        popularItemsFinal.subscribe(response -> {
            JsonElement jelement = new JsonParser().parse(response);
            JsonObject jobject = jelement.getAsJsonObject();

            //todo handle missing or error condition
            JsonArray popResult = jobject.getAsJsonObject("jsonResponseStr").getAsJsonArray("result");
            List<PopularItem> itemIds = new ArrayList<>();
            for (int i = 0; i < 20 && i < popResult.size(); i++) {
                JsonObject asJsonObject = popResult.get(i).getAsJsonObject();
                float w = asJsonObject.get("weight").getAsFloat();
                String id = asJsonObject.get("value").getAsString();
                PopularItem popularItem = new PopularItem(id, w);
                itemIds.add(popularItem);
            }

            String value = gson.toJson(itemIds);
            requestForMetadataUpdate(itemIds);
            requestForRecommenderNameUpdate(popularityService.getRecommenderName());
            view.updateItems(value);
        }, throwable -> {
            logger.error("Error Happened while fetching popularity data", throwable);
            requestForRecommenderNameUpdate(popularityService.getRecommenderName());
        });
    }

    private void requestForRecommenderNameUpdate(Observable<String> recommenderName) {
        recommenderName.subscribe(name -> view.updateRecommenderName(name));
    }

    private void requestForMetadataUpdate(List<PopularItem> items) {
        DCAMetadataServiceClient metadataServiceClient = new DCAMetadataServiceClientImpl();
        List<String> itemIds = items.stream().map(itemId -> itemId.id).collect(Collectors.toList());

        Observable<DCAMetadataResponse> metadataResponseObservable = metadataServiceClient.getMetadataItems(itemIds);

        metadataResponseObservable.subscribe(mdr -> {
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("MetaData Response : %s", mdr));
            }
            view.updateMetadataItem(mdr);
        }, e -> {
            logger.warn("Error retrieving metadata response.", e);
            view.clearMetadata();
        });
    }
}
