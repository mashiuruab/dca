package com.sannsyn.dca.vaadin.widgets.operations.controller.component.recommenders;

import com.google.gson.JsonElement;
import com.sannsyn.dca.metadata.DCAItem;
import com.sannsyn.dca.metadata.DCAMetadataResponse;
import com.sannsyn.dca.metadata.DCAMetadataServiceClient;
import com.sannsyn.dca.metadata.DCAMetadataServiceClientImpl;
import com.sannsyn.dca.service.DCAPipesService;
import com.sannsyn.dca.service.Status;
import com.sannsyn.dca.util.DCAConfigProperties;
import com.sannsyn.dca.vaadin.component.custom.DCASpinner;
import com.sannsyn.dca.vaadin.component.custom.DCAWrapper;
import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.component.custom.field.DCALabel;
import com.sannsyn.dca.vaadin.helper.OnEnterKeyHandler;
import com.sannsyn.dca.vaadin.icons.SannsynIcons;
import com.sannsyn.dca.vaadin.pipes.DCAPipeEditForm;
import com.sannsyn.dca.vaadin.ui.DCAUiHelper;
import com.sannsyn.dca.vaadin.view.DCALayoutContainer;
import com.sannsyn.dca.vaadin.widgets.controller.overview.model.aggregates.DCAPipe;
import com.sannsyn.dca.vaadin.widgets.controller.overview.model.aggregates.DCAPipeInstanceType;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.ui.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.dialogs.ConfirmDialog;
import rx.Observable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by mashiur on 2/23/17.
 */
public class DCAEditFilterPopUpContainer extends DCAPopUpContainer {
    private static final Logger logger = LoggerFactory.getLogger(DCAEditFilterPopUpContainer.class);

    private static final String TMP_DIR = DCAConfigProperties.getTemporaryDirectory().get();
    private static final String METADATA_SERVICE_RESPONSE_STATUS = "success";
    private static final String DEFAULT_NO_IMG = "";

    private static final String PIPE_CLASS_KEY = "class";
    private static final String PIPE_NAME_KEY = "name";
    private static final String PIPE_DESCRIPTION_KEY = "description";
    private static final String PIPE_FILTER_IDS_ARRAY_KEY = "filterIds";


    private DCALabel numberOfItemSelected;
    private CssLayout searchContentItemContainer;
    private CssLayout checkboxAndSearchResultItemContainer;

    private CheckBox allCheckBox = new CheckBox("All", false);
    private CheckBox noneCheckBox = new CheckBox("None", true);
    private CssLayout filterContentItemContainer;
    private TextField titleField;
    private TextField descriptionField;
    private TextField searchTextField = new TextField();

    private DCASpinner filterContentSpinner = new DCASpinner();
    private DCASpinner searchFilterSpinner = new DCASpinner();

    private Upload fileUploader;
    private File filterIdFile;

    private DCAMetadataServiceClient metadataServiceClient = new DCAMetadataServiceClientImpl();
    private DCAPipesService pipesService;
    private DCAPipe pipeItem;
    private String pipeComponentName;
    private boolean isCreateMode;
    private Map<String, Object> externalPipeData;


    public DCAEditFilterPopUpContainer(String pipeComponentName, DCALayoutContainer layoutContainer, boolean isCreateMode) {
        this.pipeComponentName = pipeComponentName;
        setLayoutContainer(layoutContainer);
        this.setStyleName("edit-filter-popup-container");
        setCurrentComponent(this);
        this.pipesService = new DCAPipesService(getLoggedInUser());

        this.isCreateMode = isCreateMode;

        try {
            init();
        } catch (Exception e) {
            logger.error("Error : ", e);
            addComponentAsLast(new DCAError(e.getMessage()), getCurrentComponent());
        }
    }

    public CheckBox getAllCheckBox() {
        return allCheckBox;
    }

    public CheckBox getNoneCheckBox() {
        return noneCheckBox;
    }

    private Observable<DCAPipe> initAndGetPipeItem() {
        /*initializing and caching the pipe object*/

        if (this.pipeItem != null) {
            return Observable.just(this.pipeItem);
        }

        Observable<List<DCAPipe>> pipeListObservable = this.pipesService
                .getRecommenderComponents(Collections.singletonList(pipeComponentName));

        return pipeListObservable.flatMap(pipeList -> {
            if (pipeList.isEmpty() && isCreateMode) {
                return pipesService.getClass(DCAPopUpContainer.TARGET_FILTER_COMPONENT_CLS_NAME,
                        DCAPipeInstanceType.SPECIFICATION);
            } else if (pipeList.isEmpty()) {
                throw new RuntimeException(String.format("No Instance found for pipe %s", pipeComponentName));
            }

            this.pipeItem = pipeList.get(0);

            return Observable.just(this.pipeItem);
        });
    }

    private Observable<Map<String, Object>> initAndGetExternalPipeData() {
        if (externalPipeData != null) {
            return Observable.just(externalPipeData);
        }

        return pipesService.getExternalPipeData().flatMap(externalData -> {
            this.externalPipeData = externalData;
            return Observable.just(externalData);
        });
    }

    private void loadFilterContentList(DCAPipe dcaPipe) {
        List<String> filterIdList = new ArrayList<>();

        if (dcaPipe.getServerSideJson() != null && dcaPipe.getServerSideJson().has(PIPE_FILTER_IDS_ARRAY_KEY)) {
            for (JsonElement jsonElement : dcaPipe.getServerSideJson().getAsJsonArray(PIPE_FILTER_IDS_ARRAY_KEY)) {
                filterIdList.add(jsonElement.getAsString());
            }
        }

        Observable<DCAItem> itemObservable = metadataServiceClient
                .getAllMetadataItems(filterIdList)
                .map(this::getMetaContentItem);

        List<Component> filterItemComponentList = new ArrayList<>();

        itemObservable.subscribe(dcaItem -> {
            DCAFilterContentItemComponent filterContentItemComponent =
                    new DCAFilterContentItemComponent(dcaItem, filterContentItemContainer, getLayoutContainer());

            filterItemComponentList.add(filterContentItemComponent);
        }, throwable -> {
            removeComponent(filterContentSpinner, filterContentItemContainer);
            onError(throwable);
        }, () -> {
            filterContentItemContainer.removeAllComponents();
            addComponentAsLast(filterItemComponentList,  filterContentItemContainer);
        });
    }

    private void onError(Throwable throwable) {
        logger.error("Error : ", throwable);
        showErrorNotification(throwable.getMessage());
    }


    private CssLayout initAddedFilterContentList() {
        CssLayout filterContentListContainer = new CssLayout();
        filterContentListContainer.setStyleName("added-content-list-container");

        DCALabel headerLabel = new DCALabel("Filter Content", "header-label");


        DCACloseIconComponent closeIconComponent = new DCACloseIconComponent();

        DCALabel clearAllText = new DCALabel("Clear selected", "clear-all-txt");
        DCAWrapper clearComponent = new DCAWrapper(Arrays.asList(clearAllText, closeIconComponent), "clear-component");

        String clearAllId = UUID.randomUUID().toString();
        clearComponent.setId(clearAllId);

        DCAWrapper headLineComponent = new DCAWrapper(Arrays.asList(headerLabel, clearComponent), "header");

        filterContentListContainer.addComponent(headLineComponent);

        filterContentItemContainer = new CssLayout();
        filterContentItemContainer.setStyleName("item-container");

        if (!isCreateMode) {
            filterContentItemContainer.addComponent(filterContentSpinner);
        }

        initAndGetPipeItem().subscribe(this::loadFilterContentList, throwable -> {
            removeComponent(filterContentSpinner, filterContentItemContainer);
            onError(throwable);
        });

        DragAndDropWrapper dragAndDropWrapper = new DragAndDropWrapper(filterContentItemContainer);
        dragAndDropWrapper.setWidth(100, Unit.PERCENTAGE);
        dragAndDropWrapper.setDropHandler(new DropHandler() {
            @Override
            public void drop(DragAndDropEvent event) {
                DragAndDropWrapper.WrapperTransferable transferable =
                        (DragAndDropWrapper.WrapperTransferable) event.getTransferable();

                DCAFilterContentItemComponent sourceItemComponent =
                        (DCAFilterContentItemComponent)((DragAndDropWrapper) transferable.getSourceComponent()).getData();
                DCAFilterContentItemComponent copyItemComponent =
                        new DCAFilterContentItemComponent(sourceItemComponent.getContentItem(),
                                filterContentItemContainer, getLayoutContainer());

                String dialogMessage = String.format("Are you sure you want to add content %s to the filter content List",
                        sourceItemComponent.getContentItem().getTitle());

                ConfirmDialog confirmDialog = ConfirmDialog.show(getUI(), dialogMessage, new ConfirmDialog.Listener() {
                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            addComponentAsLast(copyItemComponent, filterContentItemContainer);
                            showSuccessNotification("Content Added to the List but not saved. " +
                                    "You have to click the save button to persist.");
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


        filterContentListContainer.addComponent(dragAndDropWrapper);

        headLineComponent.addLayoutClickListener(event -> {
            if (event.getChildComponent() == null) {
                return;
            }

            if (clearAllId.equals(event.getChildComponent().getId())) {
                for (int counter = 0; counter < filterContentItemContainer.getComponentCount(); counter++) {
                    DCAFilterContentItemComponent filterContentItemComponent =
                            (DCAFilterContentItemComponent) filterContentItemContainer.getComponent(counter);

                    if (filterContentItemComponent.getSelectItem().getValue()) {
                        removeComponent(filterContentItemComponent, filterContentItemContainer);
                    }

                }
            }
        });

        return filterContentListContainer;
    }

    public void updateCheckedItemCount() {
        if (numberOfItemSelected == null || searchContentItemContainer == null) {
            return;
        }

        int numberOfCheckedItems = 0;

        for (int componentCounter = 0; componentCounter < searchContentItemContainer.getComponentCount();
             componentCounter++) {

            if (!(searchContentItemContainer.getComponent(componentCounter) instanceof DragAndDropWrapper)) {
                continue;
            }

            DCAFilterContentItemComponent filterContentItemComponent = (DCAFilterContentItemComponent)
                    ((DragAndDropWrapper) searchContentItemContainer.getComponent(componentCounter)).getData();
            if (filterContentItemComponent.getSelectItem().getValue()) {
                numberOfCheckedItems++;
            }
        }

        setNumberOfCheckedItemComponent(numberOfCheckedItems);
    }

    private void setNumberOfCheckedItemComponent(int numberOfCheckedItem) {
        UI.getCurrent().access(() -> {
            String itemSelectedMessage = "no item selected";
            if (numberOfCheckedItem > 0) {
                itemSelectedMessage = String.format("%s items selected", numberOfCheckedItem);
            }

            numberOfItemSelected.setValue(itemSelectedMessage);
        });
    }

    private void doSearch() {
        if (StringUtils.isEmpty(searchTextField.getValue())) {
            return;
        }

        DCAItem defaultItemIfNotPresent = getDefaultItem(searchTextField.getValue());

        rx.Observable<DCAItem> itemObservable = metadataServiceClient
                .search(StringUtils.stripToEmpty(searchTextField.getValue()))
                .map(DCAItem::fromMetadata)
                .defaultIfEmpty(defaultItemIfNotPresent);

        populateSearchResultListUI(itemObservable);
    }

    private void populateSearchResultListUI(Observable<DCAItem> itemObservable) {
        addComponentAsLast(searchFilterSpinner, checkboxAndSearchResultItemContainer);

        List<Component> searchItemList = new ArrayList<>();

        itemObservable.subscribe(dcaItem -> {
            DCAFilterContentItemComponent filterContentItemComponent = new DCAFilterContentItemComponent(dcaItem,
                    searchContentItemContainer, getLayoutContainer(), this);

            DragAndDropWrapper dragAndDropFilterContentItemWrapper = new DragAndDropWrapper(filterContentItemComponent);
            dragAndDropFilterContentItemWrapper.setDragStartMode(DragAndDropWrapper.DragStartMode.COMPONENT);
            dragAndDropFilterContentItemWrapper.setData(filterContentItemComponent);
            dragAndDropFilterContentItemWrapper.setStyleName("drag-drop-filter-content-item-wrapper");
            dragAndDropFilterContentItemWrapper.setWidth(100, Unit.PERCENTAGE);

            searchItemList.add(dragAndDropFilterContentItemWrapper);

        }, throwable -> {
            removeComponent(searchFilterSpinner, checkboxAndSearchResultItemContainer);
            onError(throwable);
        }, () -> {
            removeComponent(searchFilterSpinner, checkboxAndSearchResultItemContainer);
            searchContentItemContainer.removeAllComponents();
            addComponentAsLast(searchItemList, searchContentItemContainer);
        });
    }

    private DCAItem getDefaultItem(String id) {
        DCAItem defaultItemIfNotPresent = new DCAItem();
        defaultItemIfNotPresent.setId(id);
        defaultItemIfNotPresent.setThumbnailUrl(DEFAULT_NO_IMG);

        return defaultItemIfNotPresent;
    }

    private DCAItem getMetaContentItem(DCAMetadataResponse metadataResponse) {
        if (!METADATA_SERVICE_RESPONSE_STATUS.equals(metadataResponse.getStatus())) {
            return getDefaultItem(metadataResponse.getId());
        } else {
            return DCAItem.fromMetadata(metadataResponse);
        }
    }

    private void populateSearchResult(List<String> itemIdList) {
        Observable<DCAItem>  itemObservable = metadataServiceClient
                .getAllMetadataItems(itemIdList)
                .map(this::getMetaContentItem);

        populateSearchResultListUI(itemObservable);
    }

    private CssLayout initSearchFilterContentList() {
        CssLayout filterContentListContainer = new CssLayout();
        filterContentListContainer.setStyleName("search-content-list-container");

        initFileUploader();

        DCAWrapper uploadItem = new DCAWrapper(Collections.singletonList(fileUploader), "upload-item");

        DCAWrapper uploadItemWrapper = new DCAWrapper(Collections.singletonList(uploadItem), "upload-item-wrapper");

        filterContentListContainer.addComponent(uploadItemWrapper);

        checkboxAndSearchResultItemContainer = new CssLayout();
        checkboxAndSearchResultItemContainer.setStyleName("item-container");

        searchTextField.setInputPrompt("Search...");
        searchTextField.setStyleName("search-text-field");
        OnEnterKeyHandler searchTextFieldInstallHandler = new OnEnterKeyHandler() {
            @Override
            public void onEnterKeyPressed() {
                doSearch();
            }
        };

        searchTextFieldInstallHandler.installOn(searchTextField);

        Button searchButton = new Button();
        searchButton.setStyleName("btn-primary search");
        searchButton.setIcon(SannsynIcons.SEARCH);
        searchButton.addClickListener(event -> {
            doSearch();
        });

        DCAWrapper searchComponent = new DCAWrapper(Arrays.asList(searchTextField, searchButton), "search-item-wrapper");

        checkboxAndSearchResultItemContainer.addComponent(searchComponent);

        DCALabel selectTextLabel = new DCALabel("Select:", "select-label");

        allCheckBox.setStyleName("all-check-box");
        String allCheckId = UUID.randomUUID().toString();
        allCheckBox.setId(allCheckId);

        noneCheckBox.setStyleName("none-check-box");
        String noneCheckId = UUID.randomUUID().toString();
        noneCheckBox.setId(noneCheckId);

        numberOfItemSelected = new DCALabel("no item selected", "item-count");

        DCAWrapper checkboxSelectorWrapper = new DCAWrapper(Arrays.asList(selectTextLabel, allCheckBox, noneCheckBox,
                numberOfItemSelected), "checkbox-action-wrapper");

        checkboxAndSearchResultItemContainer.addComponent(checkboxSelectorWrapper);

        searchContentItemContainer = new CssLayout(){
            @Override
            public void removeComponent(Component c) {
                super.removeComponent(c);
                updateCheckedItemCount();
            }
        };

        searchContentItemContainer.setStyleName("search-content-item-container");
        checkboxAndSearchResultItemContainer.addComponent(searchContentItemContainer);

        /*This is done only to implement the toggling feature as in the valueChangeListener it would fall in an infinite loop*/
        checkboxSelectorWrapper.addLayoutClickListener(event -> {
            if (event.getChildComponent() == null) {
                return;
            }

            if (allCheckId.equals(event.getChildComponent().getId())) {
                noneCheckBox.setValue(false);
                setCheckStatusInContentItems(searchContentItemContainer, !allCheckBox.getValue());
            } else if (noneCheckId.equals(event.getChildComponent().getId())) {
                allCheckBox.setValue(false);
                setCheckStatusInContentItems(searchContentItemContainer, false);
            }
        });

        filterContentListContainer.addComponent(checkboxAndSearchResultItemContainer);

        return filterContentListContainer;
    }

    private void setCheckStatusInContentItems(CssLayout searchContentItemContainer, Boolean value) {
        int numberOfCheckedItemCounter = 0;
        for (int counter = 0; counter < searchContentItemContainer.getComponentCount(); counter++) {
            if (searchContentItemContainer.getComponent(counter) instanceof DragAndDropWrapper) {
                DCAFilterContentItemComponent filterContentItemComponent = (DCAFilterContentItemComponent)
                        ((DragAndDropWrapper)searchContentItemContainer.getComponent(counter)).getData();
                filterContentItemComponent.getSelectItem().setValue(value);

                if (value) {
                    numberOfCheckedItemCounter++;
                }
            }
        }

        setNumberOfCheckedItemComponent(numberOfCheckedItemCounter);
    }


    private void init() {
        List<Component> componentList = new ArrayList<>();

        String headingText = isCreateMode ? "Add Filter" : "Edit Filter";
        DCAWrapper headerComponent = getHeadLineComponent(headingText);
        componentList.add(headerComponent);

        titleField = new TextField("Title: ");

        if (!isCreateMode) {
            initAndGetPipeItem().subscribe(dcaPipe -> {
                DCAUiHelper.runInUiThread(() -> {
                    titleField.setValue(dcaPipe.getName());
                    titleField.setReadOnly(true);
                });
            }, this::onError);
        }

        DCAWrapper titleContainer = new DCAWrapper(Collections.singletonList(titleField), "title-container");
        componentList.add(titleContainer);

        descriptionField = new TextField("Description: ");

        if (!isCreateMode) {
            initAndGetPipeItem().subscribe(dcaPipe -> {
                DCAUiHelper.runInUiThread(() -> {
                    descriptionField.setValue(dcaPipe.getComponentDescription());
                });
            }, this::onError);
        }

        DCAWrapper descriptionWrapper = new DCAWrapper(Collections.singletonList(descriptionField), "title-container");
        componentList.add(descriptionWrapper);

        CssLayout addedFilterContentList = initAddedFilterContentList();
        CssLayout searchFilterContentList = initSearchFilterContentList();

        DCAWrapper filterListContainer = new DCAWrapper(Arrays.asList(addedFilterContentList, searchFilterContentList), "filter-list-container");
        componentList.add(filterListContainer);

        com.vaadin.ui.Button saveButton = new Button("Save");
        saveButton.setStyleName("btn-primary save-button");

        saveButton.addClickListener(event -> {
            String dialogMessage = String.format("Do you want to save changes to the filter %s ?", titleField.getValue());
            ConfirmDialog confirmDialog = ConfirmDialog.show(getUI(), dialogMessage, new ConfirmDialog.Listener() {
                @Override
                public void onClose(ConfirmDialog dialog) {
                    if (dialog.isConfirmed()) {
                        saveChanges();
                    }
                }
            });

            confirmDialog.getOkButton().setStyleName("btn-primary");
            confirmDialog.getCancelButton().setStyleName("btn-primary");
            confirmDialog.setCaption("");
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyleName("btn-primary cancel-button");

        cancelButton.addClickListener(event -> {
            closePopUpWindow();
        });

        DCAWrapper buttonWrapper = new DCAWrapper(Arrays.asList(saveButton, cancelButton), "btn-wrapper");
        componentList.add(buttonWrapper);

        addComponentAsLast(componentList, this);
    }


    private void saveChanges() {
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put(PIPE_CLASS_KEY, DCAPopUpContainer.TARGET_FILTER_COMPONENT_CLS_NAME);
        valueMap.put(PIPE_NAME_KEY, titleField.getValue());
        valueMap.put(PIPE_DESCRIPTION_KEY, descriptionField.getValue());

        List<String> filterIdList = new ArrayList<>();

        for(int counter = 0; counter < filterContentItemContainer.getComponentCount(); counter++) {
            if (filterContentItemContainer.getComponent(counter) instanceof DCAFilterContentItemComponent) {
                DCAFilterContentItemComponent filterContentItemComponent = (DCAFilterContentItemComponent)
                        filterContentItemContainer.getComponent(counter);
                filterIdList.add(filterContentItemComponent.getContentItem().getId());
            }
        }

        valueMap.put(PIPE_FILTER_IDS_ARRAY_KEY, filterIdList);

        initAndGetPipeItem().subscribe(dcaPipe -> {
            Pair<Status, String> pipeStatus = pipesService.createUpdatePipe(valueMap, dcaPipe.getClazz());

            if (pipeStatus.getLeft().equals(Status.SUCCESS)) {
                initAndGetExternalPipeData().subscribe(pipeMapObject-> {
                    String pipeNameInput = StringUtils.stripToEmpty(titleField.getValue());

                    if (!pipeMapObject.containsKey(pipeNameInput)) {
                        pipeMapObject.put(pipeNameInput, new HashMap<>());
                    }

                    ((Map<String, Object>) pipeMapObject.get(pipeNameInput)).put(DCAPipeEditForm.SHOW_IN_MANAGE_FILTER_KEY, Boolean.TRUE);

                    if (logger.isDebugEnabled()) {
                        logger.debug(String.format("Setting the pipe %s to show in Manage filter widget", pipeNameInput));
                    }
                    
                    pipesService.createUpdateExternalPipeData(pipeMapObject).subscribe(externalStatus -> {
                        if (externalStatus.getLeft().equals(Status.SUCCESS)) {
                            showSuccessMessage(String.format("%s-%s", pipeStatus.getRight(), externalStatus.getRight()));
                        } else if (externalStatus.getLeft().equals(Status.FAILURE)){
                            showErrorMessage(externalStatus.getRight());
                        }
                    }, this::onError);

                }, this::onError);

            } else if (pipeStatus.getLeft().equals(Status.FAILURE)) {
                showErrorMessage(pipeStatus.getRight());
            }
        }, this::onError);
    }

    private void showSuccessMessage(String webServiceMessage) {
        String successMessage = StringUtils.isEmpty(webServiceMessage) ?
                String.format("Successfully saved %s", titleField.getValue()) : webServiceMessage;

        showSuccessNotification(successMessage);
    }

    private void showErrorMessage(String  webServiceErrorMessage) {
        showErrorNotification(webServiceErrorMessage);
    }



    private void initFileUploader() {
        fileUploader = new Upload(){
            @Override
            public void detach() {
                super.detach();
                if (filterIdFile != null && filterIdFile.exists()) {
                    try {
                        FileUtils.forceDelete(filterIdFile);
                        if (logger.isDebugEnabled()) {
                            logger.debug(String.format("DETACHING File Uploader Component : Deleting the file %s",
                                    filterIdFile.getAbsolutePath()));
                        }
                    } catch (IOException e) {
                        logger.error("Error : ", e);
                    }
                }
            }
        };

        fileUploader.setStyleName("file-upload-sannsyn-icon");

        fileUploader.setImmediate(true);
        fileUploader.setButtonCaption("Upload Items");

        fileUploader.setReceiver((filename, mimeType) -> {
            FileOutputStream fos = null;

            try {
                String generatedFileName = String.format("%s_%s", UUID.randomUUID().toString(), filename);
                filterIdFile = new File(TMP_DIR, generatedFileName);
                fos = new FileOutputStream(filterIdFile);
            } catch (Exception e) {
                logger.error("Error : ", e);
                fileUploader.interruptUpload();
                return new NullOutputStream();
            }

            return fos;
        });

        fileUploader.addSucceededListener(event -> {
            try {
                String fileContent = FileUtils.readFileToString(filterIdFile);
                fileContent = fileContent.replace("\n", "");
                List<String> itemIdList = Arrays.asList(fileContent.split(",")).stream()
                        .map(String::trim).collect(Collectors.toList());
                populateSearchResult(itemIdList);
            } catch (Exception  e) {
                logger.error("Exception : ", e);
            } finally {
                if (filterIdFile != null && filterIdFile.exists()) {
                    try {
                        FileUtils.forceDelete(filterIdFile);
                        if (logger.isDebugEnabled()) {
                            logger.debug(String.format("Deleted the file %s", filterIdFile.getAbsolutePath()));
                        }
                    } catch (IOException e) {
                        logger.error("Error : ", e);
                    }
                }

                initFileUploader();
            }
        });
    }
}
