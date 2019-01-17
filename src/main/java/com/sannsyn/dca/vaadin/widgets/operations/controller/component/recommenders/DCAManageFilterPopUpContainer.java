package com.sannsyn.dca.vaadin.widgets.operations.controller.component.recommenders;

import com.google.gson.JsonObject;
import com.sannsyn.dca.presenter.DCADashboardPresenter;
import com.sannsyn.dca.service.DCAPipesService;
import com.sannsyn.dca.vaadin.component.custom.DCAConfirmDialog;
import com.sannsyn.dca.vaadin.component.custom.DCASpinner;
import com.sannsyn.dca.vaadin.component.custom.DCAWrapper;
import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.component.custom.field.DCATooltip;
import com.sannsyn.dca.vaadin.component.custom.icon.DCAAddNewIcon;
import com.sannsyn.dca.vaadin.component.custom.logout.DCAModalComponent;
import com.sannsyn.dca.vaadin.servlet.DCAUtils;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.controller.overview.model.aggregates.DCAPipe;
import com.sannsyn.dca.vaadin.widgets.controller.overview.model.aggregates.DCAPipeInstanceType;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAPipeLineObject;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAServiceConfigWrapper;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCATaskObject;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.ui.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;
import rx.Observable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by mashiur on 1/10/17.
 */
public class DCAManageFilterPopUpContainer extends DCAPopUpContainer {
    private static final Logger logger = LoggerFactory.getLogger(DCAManageFilterPopUpContainer.class);

    private Map<String,  List<DCAPipe>> typeOfFilterMap = new HashMap<String, List<DCAPipe>>(){{
        put(LOCAL_TYPE_OF_FILTER, new ArrayList<>());
        put(GLOBAL_TYPE_OF_FILTER, new ArrayList<>());
    }};

    private DCASpinner activeFilterListSpinner = new DCASpinner();
    private DCASpinner allFilterListSpinner = new DCASpinner();

    private List<String> itemValues;
    private String pipeLineObjectName;
    private DCATaskObject pipeLineObject;

    private CssLayout activeFilterItemContainer;
    private CssLayout allFilterItemContainer;
    private DCAPipesService pipesService;

    private Button saveButton = new Button("Save");
    private Button cancelButton = new Button("Cancel");

    public DCAManageFilterPopUpContainer(List<String> itemValues, String pipeLineObjectName,
                                         DCADashboardPresenter dashboardPresenter, DCALayoutContainer layoutContainer) {
        setDashboardPresenter(dashboardPresenter);
        setLayoutContainer(layoutContainer);
        this.setStyleName("manage-filter-container");
        this.itemValues = itemValues;
        this.pipeLineObjectName = pipeLineObjectName;

        setCurrentComponent(this);
        this.pipesService = new DCAPipesService(getLoggedInUser());

        try {
            init();
        } catch (Exception e) {
            logger.error("Error : ", e);
            throw e;
        }
    }


    private CssLayout initActiveFilterListComponent() {
        CssLayout activeFilterListContainer = new CssLayout();
        activeFilterListContainer.setStyleName("active-filter-list-container");

        DCALabel headerLabel = new DCALabel("Active Filters", "header");
        activeFilterListContainer.addComponent(headerLabel);

        activeFilterItemContainer = new CssLayout();
        activeFilterItemContainer.setStyleName("item-container");
        activeFilterItemContainer.addComponent(activeFilterListSpinner);


        DragAndDropWrapper activeListDragAndDropWrapper = new DragAndDropWrapper(activeFilterItemContainer);
        activeListDragAndDropWrapper.setStyleName("active-item-container-wrapper");

        activeListDragAndDropWrapper.setDropHandler(new DropHandler() {
            @Override
            public void drop(DragAndDropEvent event) {
                DragAndDropWrapper.WrapperTransferable transferable =
                        (DragAndDropWrapper.WrapperTransferable) event.getTransferable();

                DCAManageFilterItemComponent sourceFilterItemComponent =
                        (DCAManageFilterItemComponent) ((DragAndDropWrapper) transferable.getSourceComponent()).getData();

                if (!sourceFilterItemComponent.isEditable()) {
                    return;
                }

                setTypeOfFilter(sourceFilterItemComponent.getPipeItem(), LOCAL_TYPE_OF_FILTER);
                DCAManageFilterItemComponent newActiveFilterComponent =
                        new DCAManageFilterItemComponent(sourceFilterItemComponent.getPipeItem(), activeFilterItemContainer,
                                getLayoutContainer(), true, sourceFilterItemComponent.isEditable());

                DragAndDropWrapper newActiveFilterDragDropper = getFilterItemDragWrapper(newActiveFilterComponent);

                String filterName = StringUtils.stripToEmpty(sourceFilterItemComponent.getPipeItem().getName());
                String dialogMessage = String.format("Are you sure you want to add filter \"%s\" to the active List", filterName);

                ConfirmDialog confirmDialog = ConfirmDialog.show(getUI(), dialogMessage, new ConfirmDialog.Listener() {
                    public void onClose(ConfirmDialog dialog) {

                        List<String> alreadyInActiveList = getFilterInContainer(activeFilterItemContainer);
                        if (alreadyInActiveList.contains(sourceFilterItemComponent.getPipeItem().getName())) {
                            return;
                        }

                        if (dialog.isConfirmed()) {
                            addComponentAsLast(newActiveFilterDragDropper, activeFilterItemContainer);
                            removeComponent(sourceFilterItemComponent.getParent(), allFilterItemContainer);
                        }
                    }
                });
                confirmDialog.getOkButton().setStyleName("btn-primary");
                confirmDialog.getCancelButton().setStyleName("btn-primary");
                confirmDialog.setCaption("");
            }

            @Override
            public AcceptCriterion getAcceptCriterion() {
                return AcceptAll.get();
            }
        });

        activeFilterListContainer.addComponent(activeListDragAndDropWrapper);

        return activeFilterListContainer;
    }

    private CssLayout initAllFilterListComponent() {
        CssLayout allFilterListContainer = new CssLayout();
        allFilterListContainer.setStyleName("all-filter-list-container");


        DCAAddNewIcon dcaAddNewIcon = new DCAAddNewIcon("add-new-filter", "add-new-filter-id");
        DCATooltip dcaTooltip = new DCATooltip("Add New Filter", "");
        dcaAddNewIcon.addComponent(dcaTooltip);

        DCALabel headerLabel = new DCALabel("All Filters", "header");

        DCAWrapper headerWrapper = new DCAWrapper(Arrays.asList(headerLabel, dcaAddNewIcon), "all-filter-header-wrapper");

        headerWrapper.addLayoutClickListener(event -> {
            if (event.getChildComponent() == null) {
                return;
            }

            if ("add-new-filter-id".equals(event.getChildComponent().getId())) {
                rx.Observable<DCAPipe> pipeObservable = pipesService.getClass(TARGET_FILTER_COMPONENT_CLS_NAME,
                        DCAPipeInstanceType.SPECIFICATION);

                pipeObservable.subscribe(pipeItem -> {
                    DCAEditFilterPopUpContainer editFilterComponent =
                            new DCAEditFilterPopUpContainer(pipeItem.getName(), getLayoutContainer(), true);
                    DCAModalComponent editFilterModalComponent = new DCAModalComponent(editFilterComponent) {
                        @Override
                        public void detach() {
                            super.detach();
                            populateFilterList();
                        }
                    };

                    addComponentAsLast(editFilterModalComponent, getLayoutContainer().getWidgetContainer());
                }, throwable -> {
                    logger.error("Error : ", throwable);
                    addComponentAsLast(new DCAError(throwable.getMessage()), getLayoutContainer().getWidgetContainer());
                });
            }
        });

        allFilterListContainer.addComponent(headerWrapper);

        allFilterItemContainer = new CssLayout();
        allFilterItemContainer.setStyleName("item-container");
        allFilterItemContainer.addComponent(allFilterListSpinner);

        DragAndDropWrapper allFilterListDragDropper = new DragAndDropWrapper(allFilterItemContainer);
        allFilterListDragDropper.setStyleName("all-filter-list-drag-drop-wrapper");
        allFilterListDragDropper.setWidth(100, Unit.PERCENTAGE);

        allFilterListDragDropper.setDropHandler(new DropHandler() {
            @Override
            public void drop(DragAndDropEvent event) {
                DragAndDropWrapper.WrapperTransferable transferable =
                        (DragAndDropWrapper.WrapperTransferable) event.getTransferable();

                DCAManageFilterItemComponent sourceFilterItemComponent =
                        (DCAManageFilterItemComponent) ((DragAndDropWrapper) transferable.getSourceComponent()).getData();

                if (!sourceFilterItemComponent.isEditable()) {
                    return;
                }

                sourceFilterItemComponent.getPipeItem().setTypeOfFilter("");

                DCAManageFilterItemComponent newAllFilterComponent =
                        new DCAManageFilterItemComponent(sourceFilterItemComponent.getPipeItem(), allFilterItemContainer,
                                getLayoutContainer(), false, sourceFilterItemComponent.isEditable());

                DragAndDropWrapper newAllFilterDragDropper = getFilterItemDragWrapper(newAllFilterComponent);


                String filterName = StringUtils.stripToEmpty(sourceFilterItemComponent.getPipeItem().getName());

                String dialogMessage = String.format("Are you sure you want to remove the filter \"%s\" from the Active Filters list?", filterName);

                ConfirmDialog confirmDialog = ConfirmDialog.show(getUI(), dialogMessage, new ConfirmDialog.Listener() {
                    public void onClose(ConfirmDialog dialog) {

                        List<String> alreadyInAllFilterList = getFilterInContainer(allFilterItemContainer);
                        if(alreadyInAllFilterList.contains(sourceFilterItemComponent.getPipeItem().getName())) {
                            return;
                        }

                        if (dialog.isConfirmed()) {
                            addComponentAsLast(newAllFilterDragDropper, allFilterItemContainer);
                            removeComponent(sourceFilterItemComponent.getParent(), activeFilterItemContainer);
                        }
                    }
                });
                confirmDialog.getOkButton().setStyleName("btn-primary");
                confirmDialog.getCancelButton().setStyleName("btn-primary");
                confirmDialog.setCaption("");
            }

            @Override
            public AcceptCriterion getAcceptCriterion() {
                return AcceptAll.get();
            }
        });

        allFilterListContainer.addComponent(allFilterListDragDropper);

        return allFilterListContainer;
    }

    private CssLayout getItemRow(List<String> items) {
        CssLayout itemComponent = new CssLayout();
        itemComponent.setStyleName("item-header");

        for (String item : items) {
            DCALabel columnItem = new DCALabel(item, "col");
            itemComponent.addComponent(columnItem);
        }

        return itemComponent;
    }

    private void onError(Throwable throwable) {
        logger.error("Error :", throwable);
        showErrorNotification(throwable.getMessage());
    }


    private List<String> getGlobalFilterList() {
        /*TODO:: This should be removed when the global config is ensured to be present at the service config*/
        return serviceConfig.getEnsembles().getGlobalFilters() == null ?
                Collections.emptyList() : serviceConfig.getEnsembles().getGlobalFilters().getAll();
    }

    private List<String> getAvailableFilterList() {
        Map<String, JsonObject> filteredSources = this.serviceConfig.getEnsembles().getElements().getSources().entrySet()
                .stream()
                .filter(entryObject -> !pipeLineObject.getFinalFilters().contains(entryObject.getKey()))
                .filter(entryObject -> !getGlobalFilterList().contains(entryObject.getKey()))
                .filter(entryObject -> showInManageFilter(entryObject.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String, DCAPipeLineObject> pipeSources = pipesService.getPipeSources(filteredSources);


        return pipeSources.entrySet().stream()
                .filter(entryObject -> "filter".equals(entryObject.getValue().getType())
                        && TARGET_FILTER_COMPONENT_CLS_NAME.equals(entryObject.getValue().getClassName()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

    }

    private void populateFilterList() {
        activeFilterItemContainer.removeAllComponents();
        activeFilterItemContainer.addComponent(activeFilterListSpinner);

        allFilterItemContainer.removeAllComponents();
        allFilterItemContainer.addComponent(allFilterListSpinner);

        Observable<String> configObservable = initLatestServiceConfigAndTaskObject();

        configObservable.filter(s -> s.equals("Success")).subscribe(s -> {
            List<String> localFilterList = getTargetFilterList(pipeLineObject.getFinalFilters());
            Observable<List<DCAPipe>> localFilterPipeObservable = pipesService.getRecommenderComponents(localFilterList);

            localFilterPipeObservable.subscribe(localPipeList -> {
                typeOfFilterMap.get(LOCAL_TYPE_OF_FILTER).clear();
                typeOfFilterMap.get(LOCAL_TYPE_OF_FILTER).addAll(localPipeList);
            }, this::onError, () -> {

                Observable<List<DCAPipe>> globalFilterPipeObservable =
                        pipesService.getRecommenderComponents(getGlobalFilterList());

                globalFilterPipeObservable.subscribe(globalPipeList -> {
                    typeOfFilterMap.get(GLOBAL_TYPE_OF_FILTER).clear();
                    typeOfFilterMap.get(GLOBAL_TYPE_OF_FILTER).addAll(globalPipeList);

                    populateActiveFilterUIWithServerData();
                    populateAllFilterUIWithServerData();
                }, this::onError, () -> {
                    UI.getCurrent().access(() -> {
                       cancelButton.setEnabled(true);
                    });
                });
            });


        }, this::onError);
    }

    private List<String> getFilterListToPOST(List<String> filterListInPipeline, List<String> submittedFilterList) {
        if (submittedFilterList.isEmpty()) {
            return filterListInPipeline.stream()
                    .filter(s -> !showInManageFilter(s))
                    .collect(Collectors.toList());
        } else if (filterListInPipeline.isEmpty()) {
            return submittedFilterList;
        }

        List<String> filterListToPOST = new ArrayList<>();

        for (String  filterInPipeLine : filterListInPipeline) {
            if (!showInManageFilter(filterInPipeLine)) {
                filterListToPOST.add(filterInPipeLine);
            }
        }

        filterListToPOST.addAll(submittedFilterList);

        return filterListToPOST;
    }

    private List<String> getSubmittedActiveFilterList() {
        List<String> submittedActiveFilterList = new ArrayList<>();

        for (int counter = 0; counter < activeFilterItemContainer.getComponentCount(); counter++) {
            if (activeFilterItemContainer.getComponent(counter) instanceof DragAndDropWrapper) {
                DragAndDropWrapper filterItemDragDropper =
                        (DragAndDropWrapper) activeFilterItemContainer.getComponent(counter);

                DCAManageFilterItemComponent activeFilterItemComponent =
                        (DCAManageFilterItemComponent) filterItemDragDropper.getData();

                if (activeFilterItemComponent.getPipeItem().getTypeOfFilter().startsWith(LOCAL_TYPE_OF_FILTER)) {
                    submittedActiveFilterList.add(activeFilterItemComponent.getPipeItem().getName());
                }
            }
        }
        return submittedActiveFilterList;
    }

    private void populateActiveFilterUIWithServerData() {
        activeFilterItemContainer.removeAllComponents();

        List<Component> activeFilterComponentList = new ArrayList<>();

        for (Map.Entry<String,List<DCAPipe>> entry : typeOfFilterMap.entrySet()) {
            String initialTypeOfFilter = entry.getKey();
            List<DCAPipe> filterList = entry.getValue();

            for (DCAPipe filter : filterList) {
                setTypeOfFilter(filter, initialTypeOfFilter);
                boolean isEditable = isEditable(filter) && !GLOBAL_TYPE_OF_FILTER.equals(initialTypeOfFilter);

                DCAManageFilterItemComponent filterItemComponent = new DCAManageFilterItemComponent(
                        filter, activeFilterItemContainer, getLayoutContainer(),
                        !GLOBAL_TYPE_OF_FILTER.equals(initialTypeOfFilter), isEditable);

                DragAndDropWrapper activeFilterItemDragDropper = getFilterItemDragWrapper(filterItemComponent);
                activeFilterItemDragDropper.setStyleName("filter-item-drag-dropper");
                activeFilterItemDragDropper.setWidth(100, Unit.PERCENTAGE);

                activeFilterComponentList.add(activeFilterItemDragDropper);
            }
        }


        addComponentAsLast(activeFilterComponentList, activeFilterItemContainer);
    }

    private void populateAllFilterUIWithServerData() {

        Observable<List<DCAPipe>> observable = pipesService.getRecommenderComponents(getAvailableFilterList());


        observable.subscribe(pipeList -> {
            List<Component> allFilterItemComponentList = new ArrayList<>();

            for (DCAPipe pipeItem : pipeList) {
                DCAManageFilterItemComponent filterItemComponent = new DCAManageFilterItemComponent(
                        pipeItem, allFilterItemContainer, getLayoutContainer(), false, true);

                DragAndDropWrapper filterItemWrapperComponent = getFilterItemDragWrapper(filterItemComponent);

                allFilterItemComponentList.add(filterItemWrapperComponent);
            }

            allFilterItemContainer.removeAllComponents();
            addComponentAsLast(allFilterItemComponentList, allFilterItemContainer);

        }, this::onError);


    }

    private rx.Observable<String> initLatestServiceConfigAndTaskObject() {
        Observable<DCAServiceConfigWrapper> wrapperObservable = getDashboardPresenter().getServiceConfig(getLoggedInUser());

        return wrapperObservable.flatMap(serviceConfigWrapper -> {
            this.serviceConfig = serviceConfigWrapper.getService();
            this.pipeLineObject = this.serviceConfig.getEnsembles().getTasks().get(this.pipeLineObjectName);
           return Observable.just("Success");
        });
    }

    private void init() {
        List<Component> componentList = new ArrayList<>();

        DCAWrapper manageFilterHeaderComponent = getHeadLineComponent("Manage Filters for Item");
        componentList.add(manageFilterHeaderComponent);

        CssLayout tabularHeaderComponent = getItemRow(DCARecommendersContainerComponent.HEADER_NAME_LIST);
        CssLayout rowComponent = getItemRow(itemValues);
        rowComponent.addStyleName("row-item");

        DCAWrapper recommenderDescriptionComponent = new DCAWrapper(Arrays.asList(tabularHeaderComponent, rowComponent), "reco-item-wrapper");
        componentList.add(recommenderDescriptionComponent);


        CssLayout activeFilterListComponent = initActiveFilterListComponent();
        CssLayout allFilterListComponent = initAllFilterListComponent();

        populateFilterList();

        DCAWrapper filterListContainer = new DCAWrapper(Arrays.asList(activeFilterListComponent, allFilterListComponent), "filter-list-container");
        componentList.add(filterListContainer);

        saveButton.setStyleName("btn-primary save-button");

        saveButton.addClickListener(event -> {
            List<String> submitterActiveFilterList = getSubmittedActiveFilterList();

            List<String> newlyAddedFilter = submitterActiveFilterList.stream()
                    .filter(item -> !pipeLineObject.getFinalFilters().contains(item)).collect(Collectors.toList());
            Optional<String> newlyAddedFilterStringOptional = newlyAddedFilter.stream()
                    .reduce((s, s2) -> s.concat(", ").concat(s2));
            String newlyAddedFilterString = newlyAddedFilterStringOptional.isPresent() ?
                    String.format("You are about to add the filter \"%s\" ", newlyAddedFilterStringOptional.get()) : "";;

            List<String> removedFilterList = pipeLineObject.getFinalFilters().stream()
                    .filter(this::showInManageFilter)
                    .filter(filterName -> !submitterActiveFilterList.contains(filterName)).collect(Collectors.toList());
            Optional<String> removedFilterListOptional = removedFilterList.stream()
                    .reduce((s, s2) -> s.concat(", ").concat(s2));
            String  removedFilterString = removedFilterListOptional.isPresent() ?
                    String.format("You are about to remove the filter \"%s\"", removedFilterListOptional.get()) : "";

            String addedAndRemovedFilterList = String.format("%s%s", newlyAddedFilterString, removedFilterString);

            String dialogMessage = "";

            if (StringUtils.isEmpty(addedAndRemovedFilterList)) {
                dialogMessage = String.format("No change done to the pipeline \"%s\". Do you want to continue?", pipeLineObjectName);

                ConfirmDialog confirmDialog = DCAConfirmDialog.show(getUI(), dialogMessage, new ConfirmDialog.Listener() {
                    public void onClose(ConfirmDialog dialog) {
                    }
                });

                confirmDialog.getCancelButton().addStyleName("hide");

                return;
            }

            dialogMessage = String.format("%s to the pipeline \"%s\". The change will affect only this recommender. Do you want to continue?",
                    addedAndRemovedFilterList, pipeLineObjectName);

            DCAConfirmDialog.show(getUI(), dialogMessage, new ConfirmDialog.Listener() {
                public void onClose(ConfirmDialog dialog) {
                    if (dialog.isConfirmed()) {
                        saveButton.setEnabled(false);
                        saveChanges();
                    }
                }
            });
        });

        cancelButton.setStyleName("btn-primary cancel-button");

        cancelButton.addClickListener(event -> {
            UI.getCurrent().access(() -> {
                cancelButton.setEnabled(false);
            });
            populateFilterList();
        });

        DCAWrapper buttonWrapper = new DCAWrapper(Arrays.asList(saveButton, cancelButton), "btn-wrapper");
        componentList.add(buttonWrapper);

        addComponentAsLast(componentList, this);
    }

    private void saveChanges() {
        List<String> submittedActiveFilterList = getSubmittedActiveFilterList();
        List<String> activeFilterListToPOST = getFilterListToPOST(pipeLineObject.getFinalFilters(), submittedActiveFilterList);
        pipeLineObject.setFinalFilters(activeFilterListToPOST);

        DCAUtils.getTargetService().subscribe(selectedService -> {
            String responseString = getDashboardPresenter().updatePipeLine(
                    pipeLineObject, pipeLineObjectName, selectedService);
            showSuccessNotification(responseString);
        }, throwable -> {
            logger.error("Error : ", throwable);
            showErrorNotification(throwable.getMessage());
        }, () -> {
            UI.getCurrent().access(() -> {
                saveButton.setEnabled(true);
            });
        });
    }


    private List<String> getTargetFilterList(List<String> filterList) {
        return filterList.stream().filter(this::showInManageFilter).collect(Collectors.toList());
    }
}
