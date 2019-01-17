package com.sannsyn.dca.vaadin.widgets.operations.controller.component.aggregates.form.view;

import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.info.DCAAggregateInfoEntity;
import com.vaadin.ui.CssLayout;

import java.util.List;

/**
 * Created by mashiur on 4/28/16.
 */
public class DCAAggregateEntityComponent extends CssLayout {
    public DCAAggregateEntityComponent(List<String> headerNames) {
        this.setStyleName("item-header-row");
        for(String headerName : headerNames) {
            this.addComponent(new DCALabel(headerName, "item"));
        }
    }

    public DCAAggregateEntityComponent(DCAAggregateInfoEntity dcaAggregateInfoEntity) {
        this.setStyleName("item-row");
        this.addComponent(new DCALabel(String.valueOf(dcaAggregateInfoEntity.getExternalId()), "item"));
        this.addComponent(new DCALabel(String.valueOf(dcaAggregateInfoEntity.getCount()), "item"));
        this.addComponent(new DCALabel(String.valueOf(dcaAggregateInfoEntity.getPopularity()), "item"));
    }

}
