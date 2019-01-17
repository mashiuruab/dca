package com.sannsyn.dca.vaadin.widgets.operations.controller.component.pipelines.pipeline;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sannsyn.dca.service.DCAPipesConfigParser;
import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAEnsembles;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAPipeLineObject;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCATaskObject;
import com.vaadin.server.Page;
import com.vaadin.ui.CssLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by mashiur on 5/26/16.
 */
public class DCAPipesContainerComponent extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAPipesContainerComponent.class);

    private Map<String, List<String>> pipeDropDownItems = new HashMap<String, List<String>>(){{
        put("Filters", new ArrayList<>());
        put("Transformers", new ArrayList<>());
        put("Joins", new ArrayList<>());
    }};

    private Map<String, List<String>> sourceDropdownItems = new HashMap<String, List<String>>(){{
        put("Producers", new ArrayList<>());
        put("Pipelines", new ArrayList<>());
    }};

    private Map<String, DCAPipeLineObject> pipeSources;
    private DCAEnsembles ensembles;
    private CssLayout pipesBodyContainer;
    private DCAPipeGenerator dcaPipeGenerator;

    private DCALabel pipeLineHeading = new DCALabel("Pipeline", "dca-widget-title-container");

    public DCAPipesContainerComponent(DCATaskObject taskObject, DCAEnsembles ensembles,
                                      Map<String, DCAPipeLineObject> pipeSources, DCALayoutContainer layoutContainer) {
        setLayoutContainer(layoutContainer);
        this.ensembles = ensembles;
        this.pipeSources = pipeSources;

        populatePipesDropdown();
        populateSourceDropDown();

        this.setStyleName("pipes-container");
        this.addComponent(pipeLineHeading);

        pipesBodyContainer = new CssLayout();
        pipesBodyContainer.setStyleName("pipes-body-container");

        this.dcaPipeGenerator = new DCAPipeGenerator(pipeDropDownItems, sourceDropdownItems, getLayoutContainer());
        this.dcaPipeGenerator.addPipes(taskObject.getChain(), pipesBodyContainer);
        Page.getCurrent().getJavaScript().execute("calculatePipeLineWidth()");

        this.addComponent(pipesBodyContainer);
    }

    private void populateSourceDropDown() {
        for(Map.Entry<String, DCAPipeLineObject> entry : this.pipeSources.entrySet()) {
            String itemName = entry.getKey();
            String itemType = entry.getValue().getType();
            if ("producer".equals(itemType)) {
                sourceDropdownItems.get("Producers").add(itemName);
            }
        }

        Map<String, DCATaskObject> sortedTasks = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.toLowerCase().compareTo(o2.toLowerCase());
            }
        });
        sortedTasks.putAll(this.ensembles.getTasks());

        for(Map.Entry<String, DCATaskObject> entry : sortedTasks.entrySet()) {
            sourceDropdownItems.get("Pipelines").add(String.format("(%s)", entry.getKey()));
        }
    }


    private void populatePipesDropdown() {
        for (Map.Entry<String, DCAPipeLineObject> entry : this.pipeSources.entrySet()) {
            String itemName = entry.getKey();
            String itemType = entry.getValue().getType();
            if ("filter".equals(itemType)) {
                pipeDropDownItems.get("Filters").add(itemName);
            } else if ("transform".equals(itemType)) {
                pipeDropDownItems.get("Transformers").add(itemName);
            } else if ("pipe".equals(itemType)) {
                pipeDropDownItems.get("Joins").add(itemName);
            }
        }
    }

    public DCARegularPipeComponent getRegularPipeContainer() {
        DCARegularPipeComponent regularPipeComponent = new DCARegularPipeComponent(pipeDropDownItems,
                sourceDropdownItems, getLayoutContainer());
        if (pipesBodyContainer.getComponentCount() > 0) {
            regularPipeComponent = (DCARegularPipeComponent) pipesBodyContainer.getComponent(0);
        }
        return regularPipeComponent;
    }
}
