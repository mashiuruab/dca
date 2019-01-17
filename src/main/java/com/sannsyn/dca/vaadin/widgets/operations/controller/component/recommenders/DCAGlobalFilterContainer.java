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
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.controller.overview.model.aggregates.DCAPipe;
import com.sannsyn.dca.vaadin.widgets.controller.overview.model.aggregates.DCAPipeInstanceType;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAGlobalFilter;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAPipeLineObject;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAServiceConfig;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAServiceConfigWrapper;
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

import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by mashiur on 4/11/17.
 */
public class DCAGlobalFilterContainer extends DCAPopUpContainer {
    private static final Logger logger = LoggerFactory.getLogger(DCAGlobalFilterContainer.class);

    private static final String GLOBAL_FILTER_ID_ALL = "all";
    private static final String GLOBAL_FILTER_ID_NO_TAXON = "missingTaxon";

    private Map<String, List<String>> globalFilterMap = new HashMap<>();
    private List<Component> taxonItemButtonList = new ArrayList<>();

    private CssLayout activeFilterListContainer;
    private CssLayout activeFilterItemContainer;
    private DCASpinner activeFilterListSpinner = new DCASpinner();

    private CssLayout allFilterItemContainer;
    private DCASpinner allFilterListSpinner = new DCASpinner();
    private DCAWrapper filterListContainer;

    private DCATaxonItemButton all;
    private DCATaxonItemButton missingTaxon;

    private DCAPipesService pipesService;
    private DCAGlobalFilterContainer currentGlobalContainer;

    private Button saveButton = new Button("Save");
    private Button cancelButton = new Button("Cancel");


    public DCAGlobalFilterContainer(DCADashboardPresenter dashboardPresenter, DCALayoutContainer layoutContainer) {
        setLayoutContainer(layoutContainer);
        setDashboardPresenter(dashboardPresenter);
        setCurrentComponent(this);
        currentGlobalContainer = this;

        this.setStyleName("manage-filter-container global-filter-container");
        this.pipesService = new DCAPipesService(getLoggedInUser());

        try {
            init();
        } catch (Exception e) {
            logger.error("Error : ", e);
            throw e;
        }
    }

    private List<String> getAvailableFilterList() {
        Map<String, JsonObject> filteredSources = this.serviceConfig.getEnsembles().getElements().getSources().entrySet()
                .stream()
                .filter(entryObject -> showInManageFilter(entryObject.getKey()))
                .filter(entryObject -> !getGlobalFilterList().contains(entryObject.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String, DCAPipeLineObject> pipeSources = pipesService.getPipeSources(filteredSources);

        return pipeSources.entrySet().stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

    }

    private Observable<DCAServiceConfig> initAndGetServiceConfig() {
        Observable<DCAServiceConfigWrapper> configWrapperObservable = getDashboardPresenter().getServiceConfig(getLoggedInUser());

        return configWrapperObservable.flatMap(serviceConfigWrapper -> {
            this.serviceConfig = serviceConfigWrapper.getService();
            initGlobalFilterMap(serviceConfig);
            return Observable.just(this.serviceConfig);
        });
    }

    private void initFilterListOnUI() {
        allFilterItemContainer.removeAllComponents();
        allFilterItemContainer.addComponent(allFilterListSpinner);

        activeFilterItemContainer.removeAllComponents();
        activeFilterItemContainer.addComponent(activeFilterListSpinner);

        initAndGetServiceConfig().subscribe(serviceConfig-> {
            List<String> filterList = getAvailableFilterList();
            populateAllFilterUIWithServerData(filterList);
        }, this::onError, () -> {
            Observable<List<DCAPipe>> listObservable = pipesService.getRecommenderComponents(getGlobalFilterList());
            listObservable.subscribe(this::populateActiveFilterUIWithServerData, this::onError, ()-> {
                UI.getCurrent().access(() -> {
                    cancelButton.setEnabled(true);
                });
            });
        });
    }

    private void populateActiveFilterUIWithServerData(List<DCAPipe> globalFilterList) {
        activeFilterItemContainer.removeAllComponents();

        List<Component> activeFilterComponentList = new ArrayList<>();

        for (DCAPipe filter : globalFilterList) {
            setTypeOfFilter(filter, GLOBAL_TYPE_OF_FILTER);
            boolean isEditable = isEditable(filter);

            DCAManageFilterItemComponent filterItemComponent = new DCAManageFilterItemComponent(
                    filter, activeFilterItemContainer, getLayoutContainer(), true, isEditable);

            String globalFilterType = getGlobalFilterType(filter.getName());
            filterItemComponent.setId(globalFilterType);

            DragAndDropWrapper filterItemDragDropWrapper = getFilterItemDragWrapper(filterItemComponent);
            filterItemDragDropWrapper.addStyleName(globalFilterType);

            activeFilterComponentList.add(filterItemDragDropWrapper);
        }


        addComponentAsLast(activeFilterComponentList, activeFilterItemContainer);
    }

    private String getGlobalFilterType(String pipeName) {
        for (Map.Entry<String, List<String>> entry : globalFilterMap.entrySet()) {
            String key = entry.getKey();
            List<String> filterList = entry.getValue();

            if (filterList.contains(pipeName)) {
                return key;
            }
        }

        return "";
    }


    private DCAGlobalFilter getSubmittedActiveFilterList() {
        clearItemListInMap(globalFilterMap);

        Map<String, List<String>> submittedTaxa = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : globalFilterMap.entrySet()) {
            if (entry.getKey().equals(GLOBAL_FILTER_ID_ALL) || entry.getKey().equals(GLOBAL_FILTER_ID_NO_TAXON)) {
                continue;
            }

            submittedTaxa.put(entry.getKey(), new ArrayList<>());
        }

        List<String> submittedAllFilter = new ArrayList<>();
        List<String> submittedMissingTaxonFilter = new ArrayList<>();

        DCAGlobalFilter submittedGlobalFilter = new DCAGlobalFilter();
        submittedGlobalFilter.setTaxa(submittedTaxa);
        submittedGlobalFilter.setAll(submittedAllFilter);
        submittedGlobalFilter.setMissingTaxon(submittedMissingTaxonFilter);

        for (int counter = 0; counter < activeFilterItemContainer.getComponentCount(); counter++) {
            DragAndDropWrapper filterItemDragDropper = ((DragAndDropWrapper) activeFilterItemContainer.getComponent(counter));
            DCAManageFilterItemComponent itemComponent = (DCAManageFilterItemComponent) filterItemDragDropper.getData();

            if (itemComponent.getId().equals(GLOBAL_FILTER_ID_ALL)) {
                submittedAllFilter.add(itemComponent.getPipeItem().getName());
            } else if (itemComponent.getId().equals(GLOBAL_FILTER_ID_NO_TAXON)) {
                submittedMissingTaxonFilter.add(itemComponent.getPipeItem().getName());
            } else if (submittedTaxa.containsKey(itemComponent.getId())) {
                submittedTaxa.get(itemComponent.getId()).add(itemComponent.getPipeItem().getName());
            }

            globalFilterMap.get(itemComponent.getId()).add(itemComponent.getPipeItem().getName());
        }


        return submittedGlobalFilter;
    }

    private void clearItemListInMap(Map<String, List<String>> filterListMap) {
        for (Map.Entry<String, List<String>> entry : filterListMap.entrySet()) {
            entry.getValue().clear();
        }
    }

    private List<String> getGlobalFilterList() {
        List<String> globalFilterList = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : globalFilterMap.entrySet()) {
            globalFilterList.addAll(entry.getValue());
        }

        return globalFilterList;
    }

    private void populateAllFilterUIWithServerData(List<String> filterList) {
        Observable<List<DCAPipe>> observable = pipesService.getRecommenderComponents(filterList);

        observable.subscribe(pipeList -> {
            List<Component> allFilterItemComponentList = new ArrayList<>();

            for (DCAPipe pipeItem : pipeList) {
                boolean isEditable = isEditable(pipeItem);

                DCAManageFilterItemComponent filterItemComponent = new DCAManageFilterItemComponent(
                        pipeItem, allFilterItemContainer, getLayoutContainer(), false, isEditable);
                DragAndDropWrapper filterItemWrapperComponent = getFilterItemDragWrapper(filterItemComponent);
                allFilterItemComponentList.add(filterItemWrapperComponent);
            }

            allFilterItemContainer.removeAllComponents();
            addComponentAsLast(allFilterItemComponentList, allFilterItemContainer);

        }, this::onError);

    }

    private void onError(Throwable throwable) {
        logger.error("Error : ", throwable);
        showErrorNotification(throwable.getMessage());
    }

    private void initGlobalFilterMap(DCAServiceConfig config) {

        if (config.getEnsembles().getGlobalFilters() != null) {
            DCAGlobalFilter globalFilter = config.getEnsembles().getGlobalFilters();

            globalFilterMap = new HashMap<>();
            globalFilterMap.put(GLOBAL_FILTER_ID_ALL, globalFilter.getAll());
            globalFilterMap.put(GLOBAL_FILTER_ID_NO_TAXON, globalFilter.getMissingTaxon());

            for (Map.Entry<String, List<String>> taxaEntry : globalFilter.getTaxa().entrySet()) {
                globalFilterMap.put(taxaEntry.getKey(), new ArrayList<>(taxaEntry.getValue()));
            }

        }

        Set<String> taxonRuleItem = getTaxonRuleItem(config);

        for(String taxonRule : taxonRuleItem) {
            if (globalFilterMap.containsKey(taxonRule)) {
                continue;
            }

            globalFilterMap.put(taxonRule, new ArrayList<>());
        }
    }


    private Set<String> getTaxonRuleItem(DCAServiceConfig config) {
        Set<String> taxonRuleItem = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        taxonRuleItem.addAll(config.getPrePopulatedAggregateInfo().get("clusterTaxon"));
        taxonRuleItem.addAll(config.getPrePopulatedAggregateInfo().get("entityTaxon"));

        return taxonRuleItem;
    }

    private CssLayout initAndGetTaxonRuleComponent() {
        DCALabel label = new DCALabel("Taxon Rule ", "label");

        DCAWrapper taxonRuleComponent = new DCAWrapper(new ArrayList<>(), "taxon-rule-component");

        initAndGetServiceConfig().subscribe(config -> {
        }, this::onError, () -> {
            all = new DCATaxonItemButton("All", GLOBAL_FILTER_ID_ALL, currentGlobalContainer);
            taxonItemButtonList.add(all);


            for (Map.Entry<String, List<String>> entry : globalFilterMap.entrySet()) {
                String identifier = entry.getKey();

                if (identifier.equals(GLOBAL_FILTER_ID_ALL) || identifier.equals(GLOBAL_FILTER_ID_NO_TAXON)) {
                    continue;
                }

                String itemLabel = StringUtils.capitalize(identifier);
                DCATaxonItemButton buttonItem = new DCATaxonItemButton(itemLabel, identifier, currentGlobalContainer);
                taxonItemButtonList.add(buttonItem);
            }

            missingTaxon = new DCATaxonItemButton("No taxon", GLOBAL_FILTER_ID_NO_TAXON, currentGlobalContainer);
            taxonItemButtonList.add(missingTaxon);

            DCAWrapper items = new DCAWrapper(taxonItemButtonList,"item-wrapper");
            addComponentAsLast(Arrays.asList(label, items), taxonRuleComponent);

        });


        return taxonRuleComponent;
    }

    public CssLayout getFilterListContainer() {
        return filterListContainer;
    }

    public String getSelectedTaxonType() {
        boolean isOnlyAllSelected = all.isSelected()
                && !(taxonItemButtonList.stream()
                .filter(itemComponent -> !itemComponent.equals(all))
                .anyMatch(itemComponent -> ((DCATaxonItemButton) itemComponent).isSelected()));

        if (isOnlyAllSelected) {
            return GLOBAL_FILTER_ID_ALL;
        }

        boolean isOnlyNoTaxonSelected = missingTaxon.isSelected()
                && !(taxonItemButtonList.stream()
                .filter(taxonButtonComponent -> !taxonButtonComponent.equals(missingTaxon))
                .anyMatch(taxonButtonComponent -> ((DCATaxonItemButton) taxonButtonComponent).isSelected()));

        if (isOnlyNoTaxonSelected) {
            return GLOBAL_FILTER_ID_NO_TAXON;
        }

        if (all.isSelected() || missingTaxon.isSelected()) {
            /*Noting  Selected or Error Condition*/
            return "";
        }

        List<Component> selectedTaxonButtons = taxonItemButtonList.stream()
                .filter(taxonButtonComponent -> !(taxonButtonComponent.equals(all) || taxonButtonComponent.equals(missingTaxon)))
                .filter(targetTaxa -> ((DCATaxonItemButton) targetTaxa).isSelected()).collect(Collectors.toList());

        if (selectedTaxonButtons.size() == 1) {
            return selectedTaxonButtons.get(0).getId();
        } else {
            /*Noting  Selected or Error Condition*/
            return "";
        }
    }

    private CssLayout initFilterHeadersAndTaxonRule() {
        CssLayout filterHeadersAndTaxon = new CssLayout();
        filterHeadersAndTaxon.setStyleName("filter-headers-taxon-rule");

        CssLayout taxonRuleComponent = initAndGetTaxonRuleComponent();
        filterHeadersAndTaxon.addComponent(taxonRuleComponent);

        return filterHeadersAndTaxon;
    }

    private CssLayout initActiveFilterListComponent() {
        activeFilterListContainer = new CssLayout();
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


                setTypeOfFilter(sourceFilterItemComponent.getPipeItem(), GLOBAL_TYPE_OF_FILTER);

                DCAManageFilterItemComponent newActiveFilterComponent =
                        new DCAManageFilterItemComponent(sourceFilterItemComponent.getPipeItem(), activeFilterItemContainer,
                                getLayoutContainer(), true, sourceFilterItemComponent.isEditable());

                DragAndDropWrapper newActiveFilterDragDropper = getFilterItemDragWrapper(newActiveFilterComponent);


                String filterName = StringUtils.stripToEmpty(sourceFilterItemComponent.getPipeItem().getName());

                String dialogMessage = String.format("Are you sure you want to add filter %s to the active List", filterName);

                String selectedTaxonType = getSelectedTaxonType();

                if (StringUtils.isEmpty(selectedTaxonType)) {
                    dialogMessage = "You need to mark one taxon rule before you can define a filter as a global filter.";
                }

                ConfirmDialog confirmDialog = ConfirmDialog.show(getUI(), dialogMessage, new ConfirmDialog.Listener() {
                    public void onClose(ConfirmDialog dialog) {

                        List<String> alreadyInActiveList = getFilterInContainer(activeFilterItemContainer);
                        if (alreadyInActiveList.contains(sourceFilterItemComponent.getPipeItem().getName())) {
                            return;
                        }

                        if (dialog.isConfirmed() && StringUtils.isNotEmpty(selectedTaxonType)) {
                            newActiveFilterComponent.setId(selectedTaxonType);
                            newActiveFilterDragDropper.addStyleName(selectedTaxonType);

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
                            initFilterListOnUI();
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

                sourceFilterItemComponent.getPipeItem().setTypeOfFilter("");

                DCAManageFilterItemComponent newAllFilterComponent =
                        new DCAManageFilterItemComponent(sourceFilterItemComponent.getPipeItem(), allFilterItemContainer,
                                getLayoutContainer(), false, sourceFilterItemComponent.isEditable());

                DragAndDropWrapper newAllFilterDragDropper = getFilterItemDragWrapper(newAllFilterComponent);


                String filterName = StringUtils.stripToEmpty(sourceFilterItemComponent.getPipeItem().getName());

                String dialogMessage = String.format("Are you sure you want to add filter %s to the all filter List", filterName);

                ConfirmDialog confirmDialog = ConfirmDialog.show(getUI(), dialogMessage, new ConfirmDialog.Listener() {
                    public void onClose(ConfirmDialog dialog) {

                        List<String> alreadyInAllFilterList = getFilterInContainer(allFilterItemContainer);
                        if (alreadyInAllFilterList.contains(sourceFilterItemComponent.getPipeItem().getName())) {
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

    private void saveChanges() {
        DCAGlobalFilter submittedGlobalFilter = getSubmittedActiveFilterList();

        Observable<Response> responseObservable = getDashboardPresenter().updateGlobalFilter(submittedGlobalFilter);

        responseObservable.subscribe(response -> {
            String successMessage = response.readEntity(String.class);
            showSuccessNotification(successMessage);
            UI.getCurrent().access(() -> {
                saveButton.setEnabled(true);
            });
        }, throwable -> {
            logger.error("Error : ", throwable);
            showErrorNotification(throwable.getMessage());
        });
    }

    private void init() {
        List<Component> componentList = new ArrayList<>();

        String headLineText = "Global Filters";
        CssLayout headLineComponent = getHeadLineComponent(headLineText);
        componentList.add(headLineComponent);

        CssLayout filterHeadersAndTaxonRule = initFilterHeadersAndTaxonRule();
        componentList.add(filterHeadersAndTaxonRule);

        CssLayout activeFilterListComponent = initActiveFilterListComponent();
        CssLayout allFilterListComponent = initAllFilterListComponent();

        filterListContainer = new DCAWrapper(Arrays.asList(activeFilterListComponent, allFilterListComponent),
                "filter-list-container");

        componentList.add(filterListContainer);

        initFilterListOnUI();

        saveButton.setStyleName("btn-primary save-button");

        saveButton.addClickListener(event -> {
            String dialogMessage = String.format("You are about to create/edit a Global Filter.");

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
            cancelButton.setEnabled(false);
            initFilterListOnUI();
        });

        DCAWrapper buttonWrapper = new DCAWrapper(Arrays.asList(saveButton, cancelButton), "btn-wrapper");
        componentList.add(buttonWrapper);


        addComponentAsLast(componentList, getCurrentComponent());
    }
}
