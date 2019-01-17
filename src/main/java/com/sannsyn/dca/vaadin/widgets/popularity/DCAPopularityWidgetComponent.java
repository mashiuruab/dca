package com.sannsyn.dca.vaadin.widgets.popularity;

import com.google.gson.Gson;
import com.sannsyn.dca.metadata.DCAMetadataResponse;
import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the custom js component for the popularity widget.
 * <p/>
 * Created by jobaer on 3/7/16.
 */
@JavaScript({"dcapopularity.js", "dcapopularity-connector.js"})
public class DCAPopularityWidgetComponent extends AbstractJavaScriptComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAPopularityWidgetComponent.class);
    public Gson gson = new Gson();

    void updateRecommenderName(String name) {
        callFunction("updateRecommenderName", name);
    }

    public void setValue(String value) {
        getState().setValue(value);
    }

    public String getValue() {
        return getState().value;
    }

    void updateMetadata(DCAMetadataResponse response) {
        logger.debug("Metadata response " + response.getAsJsonObject());
        callFunction("updateMetadata", response.getId(), response.getAsJsonObject());
    }

    void clearMetadata() {
        logger.debug("Clearing metadata response");
        callFunction("clearMetadata");
    }

    @Override
    protected DCAPopularityWidgetState getState() {
        return (DCAPopularityWidgetState) super.getState();
    }
}