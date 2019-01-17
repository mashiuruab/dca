package com.sannsyn.dca.vaadin.widgets.operations.controller.component.pipelines.pipeline;

import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAPipeLineObject;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCATaskObject;
import com.vaadin.external.org.slf4j.Logger;
import com.vaadin.external.org.slf4j.LoggerFactory;
import com.vaadin.server.Page;
import com.vaadin.ui.CssLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mashiur on 3/7/17.
 */
public class DCAFilterContainerComponent extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAFilterContainerComponent.class);

    private Map<String, DCAPipeLineObject> pipeSources;
    private Map<String, List<String>> filterListMap = new HashMap<String, List<String>>(){{
        put("filter", new ArrayList<>());
    }};

    private DCATaskObject taskObject;
    private DCAFilterBranchContainer filterBranchContainer;
    private DCAFilterPipeGenerator filterPipeGenerator;

    public DCAFilterContainerComponent(DCATaskObject taskObject, Map<String, DCAPipeLineObject> pipeSources, DCALayoutContainer layoutContainer) {
        setLayoutContainer(layoutContainer);
        this.pipeSources = pipeSources;
        this.taskObject = taskObject;
        this.setStyleName("filter-container-wrapper");


        populateFilterItems();

        this.filterPipeGenerator = new DCAFilterPipeGenerator(filterListMap, getLayoutContainer());

        try {
            init();
        } catch (Exception e) {
            logger.error("Error : ", e);
            addComponentAsLast(new DCAError(e.getMessage()), this);
        }
    }

    private void init() {
        CssLayout filterContainer = new CssLayout();
        filterContainer.setStyleName("filter-container");
        this.addComponent(filterContainer);

        DCALabel headerLabel = new DCALabel("Filters:", "label-name");
        filterContainer.addComponent(headerLabel);

        filterBranchContainer = new DCAFilterBranchContainer(filterListMap, getLayoutContainer());

        filterContainer.addComponent(filterBranchContainer);

        filterPipeGenerator.addFilter(this.taskObject.getFinalFilters(), filterBranchContainer.getChainContainer());
        Page.getCurrent().getJavaScript().execute("calculateFilterChainWidth()");
    }

    private void populateFilterItems() {
        for (Map.Entry<String, DCAPipeLineObject> entry : this.pipeSources.entrySet()) {
            String itemName = entry.getKey();
            String itemType = entry.getValue().getType();
            if ("filter".equals(itemType)) {
                filterListMap.get("filter").add(itemName);
            }
        }
    }

    public List<String> getFilters() {
        return filterBranchContainer.getFilters();
    }
}
