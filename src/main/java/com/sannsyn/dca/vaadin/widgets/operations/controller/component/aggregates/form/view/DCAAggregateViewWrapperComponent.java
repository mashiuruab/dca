package com.sannsyn.dca.vaadin.widgets.operations.controller.component.aggregates.form.view;

import com.sannsyn.dca.presenter.DCADashboardPresenter;
import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.common.DCABreadCrumb;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAPopupMessageComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAAggregateItem;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.info.DCAAggregateInfo;
import com.vaadin.ui.CssLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

/**
 * Created by mashiur on 4/19/16.
 */
public class DCAAggregateViewWrapperComponent extends DCAAggregateViewComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAAggregateViewWrapperComponent.class);

    private DCAAggregateInfoComponent dcaAggregateInfoComponent = new DCAAggregateInfoComponent();
    private DCAAggregateRelationsComponent dcaAggregateRelationsComponent;
    private DCAAggregateDetailComponent dcaAggregateDetailComponent;

    private CssLayout aggregateViewComponent;
    private String subtitleMessage = "";


    public DCAAggregateViewWrapperComponent(String aggregateName, DCAAggregateItem dcaAggregateItem,
                                            DCADashboardPresenter dcaDashboardPresenter, String subtitleMessage,
                                            DCALayoutContainer layoutContainer) {
        setLayoutContainer(layoutContainer);
        setDashboardPresenter(dcaDashboardPresenter);

        this.aggregateViewComponent = this;

        Observable<DCAAggregateInfo> dcaAggregateInfoObservable = dcaDashboardPresenter.getServiceConfig(aggregateName, "LAST", 10);

        this.subtitleMessage = subtitleMessage;
        this.dcaAggregateDetailComponent = new DCAAggregateDetailComponent(aggregateName, dcaAggregateItem);

        this.setStyleName("view-aggregate-container");
        this.dcaAggregateRelationsComponent = new DCAAggregateRelationsComponent(aggregateName, dcaDashboardPresenter,
                dcaAggregateItem);
        try {
            init(aggregateName);
        } catch (Exception e) {
            logger.error("Error : ", e);
            this.addComponent(new DCAError("Error happened in loading the component"));
        }

        dcaAggregateInfoObservable.subscribe(dcaAggregateInfoComponent::onNext,  dcaAggregateInfoComponent::onError);
        dcaAggregateInfoObservable.subscribe(dcaAggregateRelationsComponent::onNext, dcaAggregateRelationsComponent::onError);
    }

    private void init(String aggregateName) {
        DCABreadCrumb breadCrumb = getLayoutContainer().getBreadCrumb();
        breadCrumb.addAction(aggregateName, s -> {});
        this.addComponent(breadCrumb.getView());

        this.addComponent(this.dcaAggregateInfoComponent);
        this.addComponent(this.dcaAggregateRelationsComponent);
        this.addComponent(this.dcaAggregateDetailComponent);

        if (!this.subtitleMessage.isEmpty()) {
            this.addComponent(new DCAPopupMessageComponent("Done:", subtitleMessage, aggregateViewComponent));
        }
    }
}
