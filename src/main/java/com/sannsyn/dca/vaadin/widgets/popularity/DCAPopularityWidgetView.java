package com.sannsyn.dca.vaadin.widgets.popularity;

import com.sannsyn.dca.metadata.DCAMetadataResponse;
import com.vaadin.ui.Layout;

/**
 *
 * Created by jobaer on 3/8/16.
 */
public interface DCAPopularityWidgetView {
    void setHandler(DCAPopularityWidgetPresenter presenter);
    void init();
    Layout getComponent();
    void updateItems(String value);
    void updateMetadataItem(DCAMetadataResponse response);
    void updateRecommenderName(String name);
    void clearMetadata();
}
