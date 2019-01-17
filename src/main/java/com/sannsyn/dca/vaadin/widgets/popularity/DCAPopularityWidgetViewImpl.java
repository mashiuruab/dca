package com.sannsyn.dca.vaadin.widgets.popularity;

import com.sannsyn.dca.metadata.DCAMetadataResponse;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.UI;

/**
 * View component of the PopularityWidgt
 * Created by jobaer on 3/9/16.
 */
public class DCAPopularityWidgetViewImpl implements DCAPopularityWidgetView {
    private final UI current;
    private DCAPopularityWidgetComponent widgetComponent;

    public DCAPopularityWidgetViewImpl(UI current) {
        this.current = current;
    }

    @Override
    public void setHandler(DCAPopularityWidgetPresenter presenter) {

    }

    @Override
    public void init() {
        widgetComponent = new DCAPopularityWidgetComponent();
        widgetComponent.setValue("{}");  // dummy value at the beginning
    }

    public Layout getComponent() {
        CssLayout layout = new CssLayout();
        layout.setSizeFull();
        layout.setStyleName("dca-popularity-widget-container");
        layout.addComponent(widgetComponent);
        return layout;
    }

    @Override
    public void updateItems(String value) {
        updateView(widgetComponent, current, value);
    }

    private void updateView(DCAPopularityWidgetComponent widgetComponent, UI ui, String value) {
        ui.access(() -> widgetComponent.setValue(value));
    }

    @Override
    public void updateMetadataItem(DCAMetadataResponse response) {
        updateMetadata(widgetComponent, current, response);
    }

    @Override
    public void updateRecommenderName(String name) {
        current.access(() -> widgetComponent.updateRecommenderName(name));
    }

    @Override
    public void clearMetadata() {
        current.access(() -> widgetComponent.clearMetadata());
    }

    private void updateMetadata(DCAPopularityWidgetComponent widgetComponent, UI ui, DCAMetadataResponse response) {
        ui.access(() -> widgetComponent.updateMetadata(response));
    }
}
