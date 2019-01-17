package com.sannsyn.dca.vaadin.widgets.operations.controller.component.pipelines.pipeline;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sannsyn.dca.presenter.DCADashboardPresenter;
import com.sannsyn.dca.service.DCAPipesConfigParser;
import com.sannsyn.dca.service.DCAPipesService;
import com.sannsyn.dca.vaadin.component.custom.DCAWrapper;
import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.common.DCABreadCrumb;
import com.sannsyn.dca.vaadin.widgets.operations.controller.DCAControllerOverviewComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAPopupErrorComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAPopupMessageComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.component.DCAWidgetContainerComponent;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.*;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.*;

/**
 * Created by mashiur on 5/13/16.
 */
public class DCAPipelineViewComponent extends DCAWidgetContainerComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAPipelineViewComponent.class);

    public static final String EXTERNAL_PIPELINE_OBJECT_KEY = "pipeline";
    public static final String CHANNEL_KEY = "channel";
    public static final String PAGE_TYPE_KEY = "pageType";
    public static final String SHOW_IN_DCA_KEY = "showInDCA";

    private DCAPipesService pipesService;
    private DCAServiceConfig serviceConfig;
    private String taskName;
    private DCATaskObject taskObject;
    private Map allPipeLineMap = new HashMap<>();
    private Map targetPipeLineInfoMap = new HashMap<>();
    private Map<String, DCAClassWrapper> faucets;

    private TextField title;
    private TextField description;
    private DCADropDownComponent outTaxonDropDownComponent;
    private TextField channel;
    private TextField pageType;
    private OptionGroup showInDCAComponent;
    private DCAFaucetContainerComponent faucetContainerComponent;
    private DCAFilterContainerComponent filterContainerComponent;
    private DCAPipesContainerComponent pipesContainerComponent;

    private CssLayout pipeLineViewComponent;
    private String permission;


    public DCAPipelineViewComponent(String taskName, DCAServiceConfig serviceConfig, DCADashboardPresenter dcaDashboardPresenter,
                                    DCALayoutContainer layoutContainer, String subtitleMessage, String permission) {
        setLayoutContainer(layoutContainer);
        setDashboardPresenter(dcaDashboardPresenter);

        this.pipesService = new DCAPipesService(getLoggedInUser());
        this.pipeLineViewComponent = this;
        this.permission = permission;

        this.serviceConfig = serviceConfig;
        this.taskName = taskName;

        if (StringUtils.isEmpty(taskName) || (serviceConfig.getEnsembles().getTasks().get(taskName) == null)) {
            this.taskObject = new DCATaskObject();
            this.taskObject.setEnsembles(this.serviceConfig.getEnsembles());
        } else {
            this.taskObject = serviceConfig.getEnsembles().getTasks().get(taskName);
            this.taskObject.setEnsembles(this.serviceConfig.getEnsembles());

            if (serviceConfig.getExternal().isPresent()
                    && serviceConfig.getExternal().get().getDca().isPresent()
                    && serviceConfig.getExternal().get().getDca().get().containsKey(EXTERNAL_PIPELINE_OBJECT_KEY)) {
                allPipeLineMap = serviceConfig.getExternal().get().getDca().get().get(EXTERNAL_PIPELINE_OBJECT_KEY);
            } else {
                allPipeLineMap = Collections.EMPTY_MAP;
            }

            if (!allPipeLineMap.isEmpty() && allPipeLineMap.get(taskName) != null) {
                this.targetPipeLineInfoMap = (Map) allPipeLineMap.get(taskName);
            }
        }

        this.faucets = serviceConfig.getEnsembles().getElements().getFaucets();

        this.setStyleName("pipe-view-container");

        try {
            init();

            if (!subtitleMessage.isEmpty()) {
                this.addComponent(new DCAPopupMessageComponent("Done:", subtitleMessage, pipeLineViewComponent));
            }
        } catch (Exception e) {
            logger.error("Error : ", e);
            this.addComponent(new DCAError(String.format("Error Creating Pipe View, Message : %s", e.getMessage())));
        }
    }

    private DCAWrapper getOutTaxon() {
        DCALabel outTaxonLabel = new DCALabel("Out taxon", "label-name");

        Set<String> taxonSet = new HashSet<>();
        taxonSet.addAll(serviceConfig.getPrePopulatedAggregateInfo().get("entityTaxon"));
        taxonSet.addAll(serviceConfig.getPrePopulatedAggregateInfo().get("clusterTaxon"));

        outTaxonDropDownComponent = new DCADropDownComponent(new ArrayList<>(taxonSet), getLayoutContainer());
        outTaxonDropDownComponent.setValue(taskObject.getOutTaxon());

        return new DCAWrapper(Arrays.asList(outTaxonLabel, outTaxonDropDownComponent), "outtaxon-container");
    }

    private Map<String, Object> getUpdatedAllPipeLineMap() {
        Map<String, Object> submittedPipeLineInfoMap = new HashMap<>();
        submittedPipeLineInfoMap.put(CHANNEL_KEY, StringUtils.stripToEmpty(channel.getValue()));
        submittedPipeLineInfoMap.put(PAGE_TYPE_KEY, StringUtils.stripToEmpty(pageType.getValue()));
        submittedPipeLineInfoMap.put(SHOW_IN_DCA_KEY, String.valueOf(showInDCAComponent.getValue()).equals("Yes"));

        String pipeLineKey = StringUtils.isEmpty(taskName) ? title.getValue() : taskName;
        Map<String, Object> updatedPipeLineMap = new HashMap<>();
        updatedPipeLineMap.putAll(allPipeLineMap);
        updatedPipeLineMap.put(pipeLineKey, submittedPipeLineInfoMap);

        return updatedPipeLineMap;
    }

    private void init() {
        DCABreadCrumb breadCrumb = getLayoutContainer().getBreadCrumb();
        String actionName = taskName;
        if(taskName.isEmpty()) {
          actionName = "Create new pipeline";
        }
        breadCrumb.addAction(actionName, s -> {});

        this.addComponent(breadCrumb.getView());
        title = new TextField("Name: ");
        title.setValue(taskName);
        title.setRequired(true);
        DCAWrapper titleContainer = new DCAWrapper(Collections.singletonList(title), "title");

        description = new TextField("Description: ");
        description.setValue(taskObject.getDescription());
        DCAWrapper descriptionContainer = new DCAWrapper(Collections.singletonList(description), "description");

        channel = new TextField("Channel: ");
        channel.setValue(String.valueOf(targetPipeLineInfoMap.getOrDefault(CHANNEL_KEY, "")));
        channel.setDescription("Name of the channel(s) in which this pipeline is used.");
        DCAWrapper channelItemContainer = new DCAWrapper(Collections.singletonList(channel), "title");

        pageType = new TextField("Page type: ");
        pageType.setValue(String.valueOf(targetPipeLineInfoMap.getOrDefault(PAGE_TYPE_KEY, "")));
        pageType.setDescription("Name describing the type of page the pipeline is presented at");
        DCAWrapper pageTypeContainer = new DCAWrapper(Collections.singletonList(pageType), "title");

        showInDCAComponent = new OptionGroup("Show In DCA: ");
        showInDCAComponent.addStyleName("horizontal");
        showInDCAComponent.addItems("No", "Yes");
        boolean showInDCA = Boolean.valueOf(String.valueOf(targetPipeLineInfoMap.get(SHOW_IN_DCA_KEY)));
        showInDCAComponent.select(showInDCA ? "Yes" : "No");
        showInDCAComponent.setDescription("When selected, the pipeline is presented at the Recommenders-page");

        DCAWrapper showInDCAComponentWrapper = new DCAWrapper(Collections.singletonList(showInDCAComponent), "show-in-dca-container");

        DCAWrapper outTaxonItemComponent = getOutTaxon();

        this.faucetContainerComponent = new DCAFaucetContainerComponent(this.faucets, this.taskObject, getLayoutContainer());

        Map<String, DCAPipeLineObject> pipeSources = this.pipesService.getPipeSources(
                this.serviceConfig.getEnsembles().getElements().getSources());

        this.filterContainerComponent = new DCAFilterContainerComponent(this.taskObject, pipeSources, getLayoutContainer());

        CssLayout bodyContainer = new CssLayout();
        bodyContainer.setStyleName("body-container");

        bodyContainer.addComponent(titleContainer);
        bodyContainer.addComponent(descriptionContainer);
        bodyContainer.addComponent(channelItemContainer);
        bodyContainer.addComponent(pageTypeContainer);
        bodyContainer.addComponent(showInDCAComponentWrapper);
        bodyContainer.addComponent(outTaxonItemComponent);
        bodyContainer.addComponent(this.faucetContainerComponent);

        CssLayout chainContainer = new CssLayout();
        chainContainer.setStyleName("chain-container");

        this.pipesContainerComponent = new DCAPipesContainerComponent(this.taskObject, this.serviceConfig.getEnsembles(),
                pipeSources, getLayoutContainer());

        chainContainer.addComponent(this.filterContainerComponent);
        chainContainer.addComponent(this.pipesContainerComponent);

        bodyContainer.addComponent(chainContainer);

        this.addComponent(bodyContainer);

        if (this.permission.contains("w")) {
            com.vaadin.ui.Button saveButton = new Button("Save");
            saveButton.setStyleName("btn-primary");
            saveButton.addClickListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    if (!title.isValid()) {
                        title.setRequiredError("PipeLine Name Required");
                        return;
                    }

                    try {

                        Gson gson = new Gson();
                        Map<String, Object> updatedAllPipeLineMap = getUpdatedAllPipeLineMap();

                        String pipeName = title.getValue();
                        DCATaskObject taskObject = new DCATaskObject();
                        taskObject.setDescription(description.getValue());
                        taskObject.setFinalFilters(filterContainerComponent.getFilters());
                        taskObject.setChain(pipesContainerComponent.getRegularPipeContainer()
                                .getItems(serviceConfig.getEnsembles()));
                        taskObject.setOut(faucetContainerComponent.getFaucetDropDownComponent().getValue());
                        taskObject.setOutTaxon(outTaxonDropDownComponent.getValue());


                        if (logger.isDebugEnabled()) {
                            logger.debug(pipeName);
                            logger.debug(gson.toJson(taskObject));
                        }

                        Observable<String> stringObservable =
                                getDashboardPresenter().createPipeLine(taskObject, pipeName, getLoggedInUser(), updatedAllPipeLineMap);
                        stringObservable.subscribe(s -> {
                            logger.info(String.format("Multiple Observable String result : %s", s));
                        }, throwable -> {
                            renderPipeLineError(throwable);
                        }, () -> {
                            updateWidgetContainer("pipeline-added-successfully-id");
                        });
                    } catch (Exception e) {
                        renderPipeLineError(e);
                    }
                }
            });

            this.addComponent(saveButton);

            Button discardButton = getDiscardButton();
            this.addComponent(discardButton);
        }
    }


    private Button getDiscardButton() {
        Button discardButton = new Button("Discard");
        discardButton.setStyleName("btn-primary");
        discardButton.addClickListener(event -> {
            loadPipeLineAfterDiscard();
        });

        return discardButton;
    }

    private void renderPipeLineView(DCAServiceConfigWrapper serviceConfigWrapper) {
        String subtitleMessage = String.format("Successfully fetched the last saved version  of pipeline '%s'", title.getValue());
        DCAServiceConfig serviceConfig = serviceConfigWrapper.getService();
        DCAPipelineViewComponent pipelineViewComponent = new DCAPipelineViewComponent(title.getValue(), serviceConfig,
                getDashboardPresenter(),getLayoutContainer(), subtitleMessage, this.permission);
        addComponentAsLast(pipelineViewComponent, getLayoutContainer().getWidgetContainer());
    }

    private void loadLatestVersion(DCAServiceConfigWrapper serviceConfigWrapper) {
        DCAServiceConfig serviceConfig = serviceConfigWrapper.getService();
        DCAPipelineViewComponent pipelineViewComponent = new DCAPipelineViewComponent(this.taskName, serviceConfig,
                getDashboardPresenter(),getLayoutContainer(), "", this.permission);
        addComponentAsLast(pipelineViewComponent, getLayoutContainer().getWidgetContainer());
    }

    private void renderPipeLineError(Throwable throwable) {
        throwable.printStackTrace();
        String subtitleMessage = String.format("%s", throwable.getMessage());
        logger.error(subtitleMessage, throwable);

        if (getLayoutContainer().getWidgetContainer().getComponent(0) instanceof DCAPipelineViewComponent) {
            DCAPipelineViewComponent viewComponent = (DCAPipelineViewComponent) getLayoutContainer().getWidgetContainer().getComponent(0);
            viewComponent.addComponent(new DCAPopupErrorComponent("Error:", subtitleMessage, viewComponent));
            getLayoutContainer().getWidgetContainer().removeAllComponents();
            addComponentAsLast(viewComponent, getLayoutContainer().getWidgetContainer());
        }
    }

    private void loadPipeLineAfterSaving() {
        getLayoutContainer().getWidgetContainer().removeAllComponents();
        Observable<DCAServiceConfigWrapper> dcaControllerConfigWrapper = getDashboardPresenter().getServiceConfig(getLoggedInUser());
        dcaControllerConfigWrapper.subscribe(this::renderPipeLineView, this::renderPipeLineError);
    }

    private void loadPipeLineAfterDiscard() {
        getLayoutContainer().getWidgetContainer().removeAllComponents();
        Observable<DCAServiceConfigWrapper> dcaControllerConfigWrapper = getDashboardPresenter().getServiceConfig(getLoggedInUser());
        dcaControllerConfigWrapper.subscribe(this::loadLatestVersion, this::renderPipeLineError);
    }

    @Override
    public void updateWidgetContainer(String clickedComponentId) {
        if ("controller-overview-id".equals(clickedComponentId)) {
            getLayoutContainer().getWidgetContainer().removeAllComponents();
            DCAControllerOverviewComponent dcaControllerOverviewComponent = new DCAControllerOverviewComponent(
                    getDashboardPresenter(), getLayoutContainer());
            getLayoutContainer().getWidgetContainer().addComponent(dcaControllerOverviewComponent);
        }

        if ("pipeline-added-successfully-id".equals(clickedComponentId)) {
            loadPipeLineAfterSaving();
        }
    }
}
