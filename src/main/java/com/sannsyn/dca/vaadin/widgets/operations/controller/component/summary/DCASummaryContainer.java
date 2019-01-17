package com.sannsyn.dca.vaadin.widgets.operations.controller.component.summary;

import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.summary.DCAControllerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mashiur on 5/2/16.
 */
public class DCASummaryContainer extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCASummaryContainer.class);

    public DCASummaryContainer() {
        this.setStyleName("summary-container");
    }

    @Override
    public void updateWidgetContainer(String clickedComponentId) {

    }

    public void onNext(DCAControllerService pDCAControllerService) {
        try {
            DCASummaryComponent dcaSummaryComponent = new DCASummaryComponent(pDCAControllerService.getServiceValues());
            addComponentAsLast(dcaSummaryComponent, this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void onError(Throwable throwable) {
        throwable.printStackTrace();
        logger.error(throwable.getMessage());
        addComponentAsLast(new DCAError("Error Happened While Fetching Summary"), this);
    }
}
