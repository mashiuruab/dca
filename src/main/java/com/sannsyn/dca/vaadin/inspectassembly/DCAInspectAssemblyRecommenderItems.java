package com.sannsyn.dca.vaadin.inspectassembly;

import com.sannsyn.dca.metadata.DCAItem;
import com.sannsyn.dca.metadata.DCAMetadataServiceClient;
import com.sannsyn.dca.metadata.DCAMetadataServiceClientImpl;
import com.sannsyn.dca.model.inspectassembly.DCAInspectAssemblyResult;
import com.sannsyn.dca.vaadin.component.custom.container.ItemContainer;
import com.sannsyn.dca.vaadin.inspectaggregate.ItemPainterDetailsView;
import com.sannsyn.dca.vaadin.ui.DCAUiHelper;
import com.vaadin.ui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.sannsyn.dca.vaadin.ui.DCAUiHelper.createSpacer;
import static com.sannsyn.dca.vaadin.ui.DCAUiHelper.wrapWithCssLayout;
import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;

/**
 * UI for showing an individual item of the inspect assembly result list.
 * <p>
 * Created by jobaer on 1/16/17.
 */
class DCAInspectAssemblyRecommenderItems extends CustomComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAInspectAssemblyRecommenderItems.class);
    private DCAMetadataServiceClient metadataServiceClient = new DCAMetadataServiceClientImpl();
    private final DCAInspectAssemblyResult resultObject;
    private ItemContainer<DCAItem> resultContainer = new ItemContainer<>(false);

    DCAInspectAssemblyRecommenderItems(DCAInspectAssemblyResult resultObject) {
        this.resultObject = resultObject;
        resultContainer.registerPainter("default", new ItemPainterDetailsView(70, 30) {
            @Override
            protected float getThumbnailWidth() {
                return 88;
            }

            @Override
            protected float getWidthInPercentage() {
                return 100;
            }
        });

        Component componentRoot = createComponentRoot();
        setCompositionRoot(componentRoot);
    }

    private Component createComponentRoot() {
        CssLayout layout = new CssLayout();
        layout.addStyleName("inspect-assembly-rec-items-wrapper");
        layout.setWidth(100, PERCENTAGE);

        CssLayout titleLayout = new CssLayout();

        titleLayout.addStyleName("inspect-assembly-rec-name");
        titleLayout.setWidth(100, PERCENTAGE);
        Label nameLabel = new Label(resultObject.getName());
        CssLayout nameWrapper = wrapWithCssLayout(nameLabel, "inspect-assembly-rec-name-title");
        CssLayout titleDiv = wrapWithCssLayout(nameWrapper, "inspect-assembly-rec-name-title-wrapper");
        titleDiv.addComponent(nameWrapper);
        DCAUiHelper.addSeparator(titleDiv);
        titleLayout.addComponent(titleDiv);

        CssLayout rowWrapper = new CssLayout();
        rowWrapper.addStyleName("inspect-assembly-rec-name-row-wrapper");

        Label component = new Label("Path: ");
        component.setWidth(20, PERCENTAGE);
        component.setStyleName("gray-font");
        CssLayout pathRow = DCAUiHelper.wrapWithCssLayout(component, "inspect-assembly-rec-name-row");
        pathRow.setWidth(100, PERCENTAGE);
        pathRow.addComponent(createSpacer(5));
        Label c1 = new Label(resultObject.getPath());
        c1.setWidth(75, PERCENTAGE);
        pathRow.addComponent(c1);
        rowWrapper.addComponent(pathRow);

        Label component1 = new Label("Input value(s)");
        component1.setStyleName("gray-font");
        CssLayout inputRow = DCAUiHelper.wrapWithCssLayout(component1, "inspect-assembly-rec-name-row");
        inputRow.setWidth(100, PERCENTAGE);
        rowWrapper.addComponent(inputRow);

        Label sourceLabel = new Label("SourceId: ");
        sourceLabel.setWidth(20, PERCENTAGE);
        sourceLabel.setStyleName("gray-font");
        CssLayout sourceRow = DCAUiHelper.wrapWithCssLayout(sourceLabel, "inspect-assembly-rec-name-row");
        sourceRow.setWidth(100, PERCENTAGE);

        Label c = new Label("");
        c.setWidth(75, PERCENTAGE);
        if (resultObject.getSourceIds() != null) {
            resultObject.getSourceIds().
                stream().reduce((a, b) -> a + ", " + b).
                ifPresent(c::setValue);
        }
        sourceRow.addComponent(createSpacer(5));
        sourceRow.addComponent(c);
        rowWrapper.addComponent(sourceRow);

        titleLayout.addComponent(rowWrapper);
        layout.addComponent(titleLayout);

        CssLayout resultItems = new CssLayout();
        resultItems.setWidth(100, PERCENTAGE);
        resultItems.addStyleName("inspect-assembly-result-items");
        resultItems.addComponent(resultContainer.getComponent());

        resultObject.getResult().forEach(this::addItemToResultContainer);
        layout.addComponent(resultItems);

        return layout;
    }

    private void addItemToResultContainer(String id) {
        DCAItem b = new DCAItem();
        b.setId(id);
        resultContainer.addItem(b);
        requestForMetadataUpdate(b.getId(), resultContainer);
    }

    private void requestForMetadataUpdate(String itemId, ItemContainer<DCAItem> bookContainer) {
        metadataServiceClient.getMetadataItem(itemId).subscribe(
            resp -> {
                DCAItem item = DCAItem.fromMetadata(resp);
                UI.getCurrent().access(() -> bookContainer.updateItem(item));
            },
            e ->
                logger.error("Error while updating metadata of recommender item", e));
    }
}
