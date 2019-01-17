package com.sannsyn.dca.vaadin.widgets.operations.controller.component.pipelines.pipeline;

import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.vaadin.event.LayoutEvents;
import com.vaadin.server.Page;
import com.vaadin.ui.CssLayout;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by mashiur on 3/7/17.
 */
public class DCAFilterPipeGenerator {
    private DCALayoutContainer layoutContainer;
    private Map<String, List<String>> filterPipeItems;

    public DCAFilterPipeGenerator(Map<String, List<String>> filterPipeItems, DCALayoutContainer layoutContainer) {
        this.filterPipeItems = filterPipeItems;
        this.layoutContainer = layoutContainer;
    }

    public void addFilter(List<String> filterItems, CssLayout itemContainer) {
        DCADropDownComponent filterDropDownComponent = new DCADropDownComponent(layoutContainer, itemContainer,
                filterPipeItems);

        itemContainer.addComponent(filterDropDownComponent);

        for (String filterItem : filterItems) {
            String removeIconId = UUID.randomUUID().toString();
            DCASelectionComponent dcaSelectionComponent = new DCASelectionComponent(String.valueOf(filterItem),
                    filterPipeItems, this.layoutContainer);
            dcaSelectionComponent.getRemoveIconComponent().setId(removeIconId);

            DCADropDownComponent rightFilterDropDownComponent = new DCADropDownComponent(this.layoutContainer,
                    itemContainer, filterPipeItems);

            itemContainer.addComponent(dcaSelectionComponent);
            itemContainer.addComponent(rightFilterDropDownComponent);

            dcaSelectionComponent.getSelectedComponent().addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
                @Override
                public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                    if (event.getChildComponent() != null && removeIconId.equals(event.getChildComponent().getId())) {
                        itemContainer.removeComponent(dcaSelectionComponent);
                        itemContainer.removeComponent(rightFilterDropDownComponent);
                    }
                }
            });
        }
    }
}
