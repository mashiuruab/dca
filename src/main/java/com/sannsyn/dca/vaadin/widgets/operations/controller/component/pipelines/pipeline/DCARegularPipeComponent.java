package com.sannsyn.dca.vaadin.widgets.operations.controller.component.pipelines.pipeline;

import com.sannsyn.dca.vaadin.view.DCALayoutContainer;

import java.util.List;
import java.util.Map;

/**
 * Created by mashiur on 5/20/16.
 */
public class DCARegularPipeComponent extends DCAPipeSourceComponent {

    public DCARegularPipeComponent(Map<String, List<String>> pipesDropDownItems,
                                   Map<String, List<String>> sourceDropDownItems, DCALayoutContainer layoutContainer) {
        this.setStyleName("item");


        DCADropDownComponent sourceDropDownComponent = new DCADropDownComponent(sourceDropDownItems, layoutContainer);
        sourceDropDownComponent.addStyleName("source");

        setRemoveBranchComponent();
        setSourceDropDownComponent(sourceDropDownComponent);
        setSourceWrapperComponent();

        getItemContainer().setStyleName("pipes-component-container");

        DCADropDownComponent pipesDropDownComponentFirst = new DCADropDownComponent(pipesDropDownItems, sourceDropDownItems,
                getItemContainer(), layoutContainer);
        getItemContainer().addComponent(pipesDropDownComponentFirst);

        this.addComponent(getItemContainer());
        this.addComponent(getSourceWrapperComponent());
    }
}
