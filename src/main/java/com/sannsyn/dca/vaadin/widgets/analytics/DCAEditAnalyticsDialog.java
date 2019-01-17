package com.sannsyn.dca.vaadin.widgets.analytics;

import com.google.gson.JsonObject;
import com.sannsyn.dca.service.Status;
import com.sannsyn.dca.service.analytics.DCAAnalyticsBackendService;
import com.sannsyn.dca.vaadin.component.custom.logout.DCAModalComponent;
import com.sannsyn.dca.vaadin.widgets.operations.dashboard.salesbyrec.DCADynamicSalesByRecChart;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.UI;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import static com.sannsyn.dca.vaadin.ui.DCAUiHelper.addComponentAsLast;
import static com.vaadin.server.Sizeable.Unit.PIXELS;

/**
 * The create/edit analytics dialog component
 * <p>
 * Created by jobaer on 5/9/17.
 */
class DCAEditAnalyticsDialog {
    private static final Logger logger = LoggerFactory.getLogger(DCAEditAnalyticsDialog.class);
    private final Runnable refreshAction;
    private final Runnable successMsgAction;
    private final Runnable errorMsgAction;
    private final UI currentUi;

    private DCAAnalyticsBackendService backendService = new DCAAnalyticsBackendService();

    DCAEditAnalyticsDialog(UI currentUi,
        CssLayout rootLayout, JsonObject item,
                           Runnable refreshAction,
                           Runnable successMsgAction,
                           Runnable errorMsgAction) {
        this.currentUi = currentUi;
        this.refreshAction = refreshAction;
        this.successMsgAction = successMsgAction;
        this.errorMsgAction = errorMsgAction;
        createEditDialog(rootLayout, item);
    }

    private void createEditDialog(CssLayout rootLayout, JsonObject item) {
        CssLayout newLayout = new CssLayout();
        newLayout.addStyleName("edit-conf-modal-wrapper");

        DCAEditAnalyticsFormComponent editAnalyticsFormComponent =
            new DCAEditAnalyticsFormComponent(item);
        newLayout.addComponent(editAnalyticsFormComponent);

        DCADynamicSalesByRecChart dynamicSalesByRecChart = new DCADynamicSalesByRecChart(currentUi, item);
        newLayout.addComponent(dynamicSalesByRecChart);

        DCAModalComponent modal = new DCAModalComponent(newLayout);
        modal.addStyleName("edit-conf-modal");

        Runnable removeAction = () -> removeComponent(modal, rootLayout);

        editAnalyticsFormComponent.setSaveAction(jsonItem -> {
            logger.debug("Requesting for save with the item " + jsonItem);
            Observable<Pair<Status, String>> status = backendService.saveUpdateAnalytics(jsonItem);
            status.subscribe(val -> {
                if (Status.SUCCESS.equals(val.getLeft())) {
                    successMsgAction.run();
                    removeAction.run();
                    refreshAction.run();
                } else {
                    errorMsgAction.run();
                }
            });
        });

        editAnalyticsFormComponent.setPreviewAction(dynamicSalesByRecChart::setItem);
        editAnalyticsFormComponent.setRemoveAction(removeAction);

        addComponentAsLast(modal, rootLayout);
    }

    private void removeComponent(Component component, CssLayout layout) {
        UI.getCurrent().access(() -> layout.removeComponent(component));
    }
}
