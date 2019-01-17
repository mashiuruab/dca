package com.sannsyn.dca.vaadin.widgets.operations.controller.component.pipelines.pipeline;

import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;

import java.util.List;
import java.util.Map;

/**
 * Created by mashiur on 5/19/16.
 */
public class DCASubRegularPipeLineComponent extends DCAPipeSourceComponent {
    private DCALabel horizontalLine = new DCALabel("<div class='before-between'>&nbsp;</div><hr class='between'/><span class='arrow'></span>", "horizontal-line");

    private Map<String, List<String>> pipesDropDownItems;
    private Map<String, List<String>> sourceDropDownItems;

    public DCASubRegularPipeLineComponent(Map<String, List<String>> pipesDropDownItems,
                                          Map<String, List<String>> sourceDropDownItems, DCALayoutContainer layoutContainer) {
        this.setStyleName("sub-pipe-item");
        this.pipesDropDownItems = pipesDropDownItems;
        this.sourceDropDownItems = sourceDropDownItems;


        DCADropDownComponent sourceDropDownComponent = new DCADropDownComponent(sourceDropDownItems, layoutContainer);
        sourceDropDownComponent.addStyleName("source");

        setRemoveBranchComponent();
        setSourceDropDownComponent(sourceDropDownComponent);
        setSourceWrapperComponent();

        getItemContainer().setStyleName("join-container");
        getItemContainer().addComponent(horizontalLine);

        DCADropDownComponent dcaDropDownComponentRight = new DCADropDownComponent(pipesDropDownItems, sourceDropDownItems,
                getItemContainer(), layoutContainer);
        getItemContainer().addComponent(dcaDropDownComponentRight);

        this.addComponent(getItemContainer());
        this.addComponent(getSourceWrapperComponent());
    }

    public Map<String, List<String>> getPipesDropDownItems() {
        return pipesDropDownItems;
    }

    public Map<String, List<String>> getSourceDropDownItems() {
        return sourceDropDownItems;
    }
}
