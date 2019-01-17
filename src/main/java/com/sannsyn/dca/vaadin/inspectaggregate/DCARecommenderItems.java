package com.sannsyn.dca.vaadin.inspectaggregate;

import com.sannsyn.dca.metadata.DCAItem;
import com.sannsyn.dca.metadata.DCAMetadataResponse;
import com.sannsyn.dca.metadata.DCAMetadataServiceClient;
import com.sannsyn.dca.metadata.DCAMetadataServiceClientImpl;
import com.sannsyn.dca.model.user.DCAUser;
import com.sannsyn.dca.service.DCAConfigService;
import com.sannsyn.dca.service.DCARecommenderService;
import com.sannsyn.dca.vaadin.component.custom.container.ItemContainer;
import com.sannsyn.dca.vaadin.component.custom.container.ItemPainter;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAEnsembles;
import com.sannsyn.dca.vaadin.widgets.operations.controller.model.aggregates.DCAServiceConfigWrapper;
import com.vaadin.data.Property;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Arrays;
import java.util.List;

import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;

/**
 * This class shows result from a single recommender.
 */
class DCARecommenderItems extends CustomComponent {
    private ComboBox recs = new ComboBox();
    private ItemContainer<DCAItem> resultContainer = new ItemContainer<>(false);
    private DCARecommenderService recommenderService;
    private DCAMetadataServiceClient metadataServiceClient = new DCAMetadataServiceClientImpl();
    private UI ui;
    private DCAConfigService configService = new DCAConfigService();
    private DCARemoveRecommenderInspector removeHandler;
    private DCARecommenderItemsHandler handler;
    private static final Logger logger = LoggerFactory.getLogger(DCARecommenderItems.class);

    private DCAUser loggedInUser;

    DCARecommenderItems(DCAUser loggedInUser) {
        this.loggedInUser = loggedInUser;
        recommenderService = new DCARecommenderService(loggedInUser);

        recs.setInputPrompt("Pipeline...");
        recs.setStyleName(ValoTheme.COMBOBOX_TINY);
        recs.removeAllItems();
        recs.setNullSelectionAllowed(false);
        recs.addItem("Loading...");

        recs.addValueChangeListener((Property.ValueChangeListener) event -> {
            Object value = event.getProperty().getValue();
            if (value != null) {
                search();
            }
        });

        ItemPainter<DCAItem> detailsPainter = new ItemPainterDetailsView(72, 28) {
            @Override
            protected float getThumbnailWidth() {
                return 88;
            }
        };
        ItemPainter<DCAItem> listView = new ItemPainterListView();

        resultContainer.registerPainter("default", detailsPainter);
        resultContainer.registerPainter("listView", listView);

        resultContainer.setTitle("");
        initializeRecommenders();
    }

    void setHandler(DCARecommenderItemsHandler handler) {
        this.handler = handler;
    }

    private void initializeRecommenders() {
        Observable<DCAServiceConfigWrapper> aggregateInfo = configService.getServiceConfig(this.loggedInUser);
        aggregateInfo.subscribe(resp -> {
            Object currentSelection = recs.getValue();
            recs.removeAllItems();
            DCAEnsembles ensembles = resp.getService().getEnsembles();
            for (String s : ensembles.getTasks().keySet()) {
                recs.addItem(s);
            }
            recs.select(currentSelection);
        }, e -> logger.error("Error while initializing recommenders", e));

        Component rootComponent = getRootComponent();
        setCompositionRoot(rootComponent);
    }

    private Component getRootComponent() {
        Layout cssLayout = new CssLayout();
        cssLayout.setStyleName("rec-items");

        Layout wrapper = new CssLayout();
        wrapper.addStyleName("v-component-group");
        wrapper.addStyleName("rec-items-selector");
        wrapper.setWidth(100, PERCENTAGE);

        Button removeButton = new Button("");
        removeButton.setStyleName(ValoTheme.BUTTON_TINY);
        removeButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        removeButton.setIcon(FontAwesome.REMOVE);
        wrapper.addComponent(removeButton);
        removeButton.addClickListener(event -> removeHandler.remove(this));

        recs.setWidth(91, PERCENTAGE);
        wrapper.addComponent(recs);
        cssLayout.addComponent(wrapper);

        cssLayout.addComponent(resultContainer.getComponent());

        return cssLayout;
    }

    public void search() {
        resultContainer.clear();
        if (recs.getValue() == null || handler == null) return;
        String name = recs.getValue().toString();

        String idResponse = handler.getId();
        if (StringUtils.isEmpty(idResponse)) {
            return;
        }

        List<String> ids = Arrays.asList(StringUtils.split(idResponse, ';'));
        String count = handler.getCount();
        Observable<List<DCAItem>> recommenderResponseAsItem = recommenderService.getRecommenderResponse(name, ids, count);
        recommenderResponseAsItem.subscribe(
            this::handleResult,
            this::handleErrorMsg);
    }

    private void handleErrorMsg(Throwable e) {
        String message = "<p>Error while trying to fetch result.</p><p>Root cause: <br/>"
            + e.getMessage() + "</p>";
        logger.error(message, e);
        showErrorMessage(message);
    }

    private void handleResult(List<DCAItem> result) {
        if(result.isEmpty()) {
            String message = "Your search does not return any result.";
            showErrorMessage(message);
            return;
        }

        for (DCAItem item : result) {
            ui.access(() -> resultContainer.addItem(item));
            requestForMetadataUpdate(item, resultContainer);
        }
    }

    private void showErrorMessage(String message) {
        ui.access(() -> resultContainer.showErrorMessage(message));
    }

    private void requestForMetadataUpdate(DCAItem item, ItemContainer<DCAItem> bookContainer) {
        Observable<DCAMetadataResponse> metadataItem = metadataServiceClient.getMetadataItem(item.getId());

        metadataItem.subscribe(resp -> {
            DCAItem metaItem = createBookFromMetadataResponse(resp, item);
            ui.access(() -> bookContainer.updateItem(metaItem));
        }, e -> logger.error("Error while updating metadata of recommender item", e));
    }

    private DCAItem createBookFromMetadataResponse(DCAMetadataResponse resp, DCAItem item) {
        DCAItem dcaItem = DCAItem.fromMetadata(resp);
        dcaItem.setScore(item.getScore());
        return dcaItem;
    }

    void switchView(String viewName) {
        resultContainer.switchToView(viewName);
    }

    public void setUi(UI ui) {
        this.ui = ui;
    }

    void setRemoveHandler(DCARemoveRecommenderInspector removeHandler) {
        this.removeHandler = removeHandler;
    }

    String getSelectedValue() {
        return recs.getValue() != null ? recs.getValue().toString() : null;
    }

    void setSelection(String v) {
        recs.addItem(v);
        recs.select(v);
    }
}
