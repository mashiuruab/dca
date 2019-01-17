package com.sannsyn.dca.vaadin.widgets.operations.controller.component.aggregates;

import com.sannsyn.dca.presenter.DCADashboardPresenter;
import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.aggregates.form.view.DCAAggregateViewWrapperComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAAggregateItem;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAAggregatePopularity;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.info.DCAAggregateInfo;
import com.vaadin.event.LayoutEvents;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by mashiur on 4/4/16.
 */
public class DCAAggregatesItemComponent extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAAggregatesItemComponent.class);

    private DCAAggregatesItemComponent itemComponent;
    private String aggregateName;
    private DCAAggregateItem dcaAggregateItem;
    private DCAAggregatePopularity mDCAAggregatePopularity;

    private Observable<DCAAggregateInfo> dcaAggregateInfoObservable;

    private List<Component> componentList = new ArrayList<>();

    public DCAAggregatesItemComponent(Collection<String> values) {
        this.setStyleName("aggregate-item-header");
        for(String value : values) {
            this.addComponent(createLabel(value, ""));
        }
    }

    public DCAAggregatesItemComponent(String aggregateName, DCAAggregateItem pDCAAggregateItem, DCALayoutContainer layoutContainer,
                                      DCAAggregatePopularity pDCAAggregateDefaultPopularity, DCADashboardPresenter dcaDashboardPresenter) {
        setLayoutContainer(layoutContainer);
        setDashboardPresenter(dcaDashboardPresenter);
        this.aggregateName = aggregateName;
        this.dcaAggregateItem = pDCAAggregateItem;
        this.dcaAggregateInfoObservable = dcaDashboardPresenter.getServiceConfig(this.aggregateName, "LAST", 10);

        this.mDCAAggregatePopularity = pDCAAggregateDefaultPopularity;
        if (this.dcaAggregateItem.getPopularity() != null) {
            this.mDCAAggregatePopularity = this.dcaAggregateItem.getPopularity();
        }

        this.itemComponent = this;

        this.setStyleName("item-row");
        this.setId(this.aggregateName);
        this.addLayoutClickListener((LayoutEvents.LayoutClickListener) event -> updateWidgetContainer(event.getComponent().getId()));

        this.dcaAggregateInfoObservable.subscribe(this::onNext, this::onError);
    }

    private void onNext(DCAAggregateInfo dcaAggregateInfo) {
        try {
            this.componentList.add(createLabel(aggregateName, ""));
            this.componentList.add(createLabel(this.dcaAggregateItem.getType(), ""));
            this.componentList.add(createLabel(StringUtils.abbreviate(this.dcaAggregateItem.getTagsAsString(), 30), ""));
            this.componentList.add(createLabel(String.format("%s, %s", this.dcaAggregateItem.getClusterTaxon(), this.dcaAggregateItem.getEntityTaxon()), ""));

            String size = (dcaAggregateInfo.getNumClusters().isEmpty() || Integer.valueOf(dcaAggregateInfo.getNumClusters()) <= 0) ? dcaAggregateInfo.getNumEntities() : dcaAggregateInfo.getNumClusters();
            this.componentList.add(createLabel(size, "aggregate-item-size"));

            this.componentList.add(createLabel(StringUtils.abbreviate(this.dcaAggregateItem.getDescription(), 60), "aggregate-item-description"));
            addComponentAsLast(this.componentList, this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void onError(Throwable throwable) {
        logger.error("Error : ", throwable);
        DCAError errorMessage = new DCAError(String.format("Error Happened While fetching the Aggregate Item %s", aggregateName));
        addComponentAsLast(errorMessage, this);
    }

    private Label createLabel(String value, String secondaryStyleName) {
        return new DCALabel(value, "aggregate-item", secondaryStyleName);
    }

    public void updateWidgetContainer(String clickedComponentId) {
        getLayoutContainer().getWidgetContainer().removeAllComponents();
        this.dcaAggregateItem.setIsOverriden(this.dcaAggregateItem.getPopularity() != null);
        if (this.dcaAggregateItem.getPopularity() == null) {
            this.dcaAggregateItem.setPopularity(mDCAAggregatePopularity);
        }
        DCAAggregateViewWrapperComponent dcaAggregateViewWrapperComponent = new DCAAggregateViewWrapperComponent(this.aggregateName,
                this.dcaAggregateItem, getDashboardPresenter(), "", getLayoutContainer());
        getLayoutContainer().getWidgetContainer().addComponent(dcaAggregateViewWrapperComponent);
    }
}
