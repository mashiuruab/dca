package com.sannsyn.dca.vaadin.widgets.operations.controller.component.aggregates;

import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAAggregatePopularity;
import com.vaadin.data.Property;
import com.vaadin.ui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mashiur on 4/8/16.
 */
public class DCAPopularityDecayComponent extends CssLayout {
    private static final Logger logger = LoggerFactory.getLogger(DCAPopularityDecayComponent.class);

    private DCALabel labelName = new DCALabel("Popularity Decay", "item-label");
    private TextField shortTermHalfTime = new TextField("Short term: ");
    private TextField shortTermWeight = new TextField("Weight: ");
    private CheckBox overridePopularityDecay = new CheckBox("Override popularity decay default");
    private TextField longTermHalfTime = new TextField("Long term: ");
    private TextField longTermWeight = new TextField("Weight: ");
    private TextField maxCacheAge = new TextField("Max cache age: ");

    private DCAAggregatePopularity mDCAAggregatePopularity;
    private boolean isOverridden;
    private boolean isViewMode;

    public boolean isViewMode() {
        return isViewMode;
    }

    public TextField getShortTermHalfTime() {
        return shortTermHalfTime;
    }

    public TextField getShortTermWeight() {
        return shortTermWeight;
    }

    public TextField getLongTermHalfTime() {
        return longTermHalfTime;
    }

    public TextField getLongTermWeight() {
        return longTermWeight;
    }

    public TextField getMaxCacheAge() {
        return maxCacheAge;
    }

    public CheckBox getOverridePopularityDecay() {
        return overridePopularityDecay;
    }

    public DCAPopularityDecayComponent(DCAAggregatePopularity pDCAAggregatePopularity, String primaryStyleName,
                                       boolean isOverridden, boolean isViewMode) {
        this.mDCAAggregatePopularity = pDCAAggregatePopularity;
        this.isOverridden = isOverridden;
        this.isViewMode = isViewMode;

        this.setStyleName(primaryStyleName);

        this.overridePopularityDecay.setId("override-popularity-decay");

        this.overridePopularityDecay.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (!isViewMode()) {
                    updatePopularityDecayComponentState(!Boolean.valueOf(event.getProperty().getValue().toString()));
                }
            }
        });

        init();
    }

    public void updatePopularityDecayComponentState(boolean readOnlyMode) {
        this.shortTermHalfTime.setReadOnly(readOnlyMode);
        this.shortTermWeight.setReadOnly(readOnlyMode);
        this.longTermHalfTime.setReadOnly(readOnlyMode);
        this.longTermWeight.setReadOnly(readOnlyMode);
        this.maxCacheAge.setReadOnly(readOnlyMode);
    }

    private void init() {
        CssLayout valueContainer = new CssLayout();
        valueContainer.setStyleName("popularity-decay-value");

        this.shortTermHalfTime.setValue(mDCAAggregatePopularity.getShortTerm().getHalftime());
        this.shortTermHalfTime.setReadOnly(true);

        this.shortTermWeight.setValue(mDCAAggregatePopularity.getShortTerm().getWeight());
        this.shortTermWeight.setReadOnly(true);

        this.overridePopularityDecay.setValue(isOverridden);
        this.overridePopularityDecay.setReadOnly(isViewMode());

        this.longTermHalfTime.setValue(mDCAAggregatePopularity.getLongTerm().getHalftime());
        this.longTermHalfTime.setReadOnly(true);

        this.longTermWeight.setValue(mDCAAggregatePopularity.getLongTerm().getWeight());
        this.longTermWeight.setReadOnly(true);

        this.maxCacheAge.setValue(mDCAAggregatePopularity.getMaxCacheAge());
        this.maxCacheAge.setReadOnly(true);

        valueContainer.addComponent(this.shortTermHalfTime);
        valueContainer.addComponent(this.shortTermWeight);
        valueContainer.addComponent(this.overridePopularityDecay);
        valueContainer.addComponent(this.longTermHalfTime);
        valueContainer.addComponent(this.longTermWeight);
        valueContainer.addComponent(this.maxCacheAge);

        this.addComponent(this.labelName);
        this.addComponent(valueContainer);
    }
}
