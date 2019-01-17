package com.sannsyn.dca.vaadin.widgets.operations.controller.component.summary;

import com.google.gson.JsonObject;
import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Metadata summary container
 * <p>
 * Created by jobaer on 22/2/17.
 */
public class DCAMetadataSummaryContainer extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAMetadataSummaryContainer.class);

    public DCAMetadataSummaryContainer() {
        this.setStyleName("summary-container");
    }

    public void onNext(JsonObject response) {
        DCAMetadataSummaryComponent dcaSummaryComponent = new DCAMetadataSummaryComponent(response);
        dcaSummaryComponent.setWidth(100, Unit.PERCENTAGE);
        addComponentAsLast(dcaSummaryComponent, this);
    }

    @Override
    public void updateWidgetContainer(String clickedComponentId) {
    }

    public void onError(Throwable throwable) {
        throwable.printStackTrace();
        logger.error(throwable.getMessage());
        addComponentAsLast(new DCAError("Error Happened While Fetching Summary"), this);
    }
}
