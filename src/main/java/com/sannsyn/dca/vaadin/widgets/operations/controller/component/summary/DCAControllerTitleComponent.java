package com.sannsyn.dca.vaadin.widgets.operations.controller.component.summary;

import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.summary.DCAControllerService;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mashiur on 5/2/16.
 */
public class DCAControllerTitleComponent extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAControllerTitleComponent.class);

    public DCAControllerTitleComponent() {
        this.setStyleName("title-container");
    }

    @Override
    public void updateWidgetContainer(String clickedComponentId) {

    }

    public void onNext(DCAControllerService pDCAControllerService) {
        try {
            String htmlContent = String.format("<span>SERVICE: %s</span>", pDCAControllerService.getServiceName());
            Component component = getLabelComponent(htmlContent, "overview-title");
            addComponentAsLast(component, this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void onError(Throwable throwable) {
        throwable.printStackTrace();
        logger.error(throwable.getMessage());
        addComponentAsLast(new DCAError("Error Happened While Fetching Title"), this);
    }

    private Label getLabelComponent(String htmlContent, String primaryStyleName) {
        return new DCALabel(htmlContent, primaryStyleName);
    }
}
