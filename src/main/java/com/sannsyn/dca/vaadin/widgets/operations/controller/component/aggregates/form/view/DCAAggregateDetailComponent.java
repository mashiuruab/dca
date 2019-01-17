package com.sannsyn.dca.vaadin.widgets.operations.controller.component.aggregates.form.view;

import com.sannsyn.dca.vaadin.component.custom.DCAWrapper;
import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.aggregates.DCAPopularityDecayComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAAggregateItem;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created by mashiur on 4/29/16.
 */
public class DCAAggregateDetailComponent extends DCAAggregateViewComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAAggregateDetailComponent.class);

    private String aggregateName;
    private DCAAggregateItem dcaAggregateItem;

    public DCAAggregateDetailComponent(String aggregateName, DCAAggregateItem dcaAggregateItem) {
        this.aggregateName = aggregateName;
        this.dcaAggregateItem = dcaAggregateItem;

        this.setStyleName("aggregate-detail-container");

        try {
            init();
        } catch (Exception e) {
            logger.error("Error : ", e);
            this.addComponent(new DCAError("Aggregate Detail Component Error Happened"));
        }
    }

    private void init() {
        DCALabel header = new DCALabel("<span>Aggregate Detail</span>", "header dca-widget-title");

        CssLayout itemWrapper = new CssLayout();
        itemWrapper.setStyleName("item-wrapper");
        itemWrapper.addComponent(createViewItem("item-row alternating-gray-color", "Name: ", this.aggregateName));
        itemWrapper.addComponent(createViewItem("item-row", "Description: ", this.dcaAggregateItem.getDescription()));
        itemWrapper.addComponent(createViewItem("item-row alternating-gray-color", "Type: ", this.dcaAggregateItem.getType()));
        itemWrapper.addComponent(createViewItem("item-row half-width", "Entity Taxon: ", this.dcaAggregateItem.getEntityTaxon()));
        itemWrapper.addComponent(createViewItem("item-row half-width", "Cluster Taxon: ", this.dcaAggregateItem.getClusterTaxon()));

        Component tagComponent = createViewItem("item-row half-width", "Tags: ", this.dcaAggregateItem.getTagsAsString());
        Component staticComponent = createViewItem("item-row half-width", "Static: ", String.valueOf(this.dcaAggregateItem.isStatic()));

        DCAWrapper grayComponentWrapper = new DCAWrapper(Arrays.asList(tagComponent, staticComponent), "item-row alternating-gray-color");
        itemWrapper.addComponent(grayComponentWrapper);
        itemWrapper.addComponent(createViewItem("item-row", "Size: ", this.dcaAggregateItem.getSize()));

        DCAPopularityDecayComponent popularityDecayComponent = new DCAPopularityDecayComponent(this.dcaAggregateItem.getPopularity(),
                "item-row alternating-gray-color", this.dcaAggregateItem.getIsOverriden(), true);
        itemWrapper.addComponent(popularityDecayComponent);

        this.addComponent(header);
        this.addComponent(itemWrapper);
    }
}
