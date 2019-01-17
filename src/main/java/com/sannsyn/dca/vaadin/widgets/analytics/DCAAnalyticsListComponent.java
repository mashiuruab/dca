package com.sannsyn.dca.vaadin.widgets.analytics;

import com.google.gson.*;
import com.sannsyn.dca.service.analytics.DCAAnalyticsBackendService;
import com.sannsyn.dca.service.analytics.NumSales;
import com.sannsyn.dca.vaadin.component.custom.container.collapsible.DCACollapsibleItemContainer;
import com.sannsyn.dca.vaadin.component.custom.container.collapsible.DCACollapsibleItemContainerImpl;
import com.sannsyn.dca.vaadin.component.custom.container.collapsible.DCAColumnSpec;
import com.sannsyn.dca.vaadin.component.custom.field.DCAComboBox;
import com.sannsyn.dca.vaadin.helper.DCAFileDownloadWithSupplier;
import com.sannsyn.dca.vaadin.helper.OnDemandFileDownloader;
import com.sannsyn.dca.vaadin.ui.DCAUiHelper;
import com.sannsyn.dca.vaadin.widgets.operations.dashboard.salesbyrec.DCADynamicSalesByRecChart;
import com.vaadin.ui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.sannsyn.dca.vaadin.ui.DCAUiHelper.wrapWithCssLayout;
import static com.sannsyn.dca.vaadin.widgets.analytics.DCAAnalyticsDataParser.format;
import static com.vaadin.server.Sizeable.Unit.PERCENTAGE;

/**
 * The component that renders the list of analytics object
 * <p>
 * Created by jobaer on 5/9/17.
 */
class DCAAnalyticsListComponent extends CustomComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCAAnalyticsListComponent.class);
    private final Runnable successMsgAction;
    private final Runnable errorMsgAction;
    private final UI currentUi;
    private final boolean isReadOnly;
    private CssLayout containerLayout = new CssLayout();

    private OnDemandFileDownloader.MutableStreamSource streamSource = new OnDemandFileDownloader.MutableStreamSource();
    private DCAAnalyticsBackendService backendService = new DCAAnalyticsBackendService();

    DCAAnalyticsListComponent(UI current, Runnable successMessageAction, Runnable errorMsgAction, boolean isReadOnly) {
        this.currentUi = current;
        this.successMsgAction = successMessageAction;
        this.errorMsgAction = errorMsgAction;
        this.isReadOnly = isReadOnly;

        streamSource.setFileName("chart-data.csv");

        containerLayout.setWidth(100, PERCENTAGE);
        containerLayout.addStyleName("recomndr-container-component");
        updateContainer();
        setCompositionRoot(containerLayout);
    }

    void refresh() {
        updateContainer();
    }

    private void updateContainer() {
        containerLayout.removeAllComponents();
        DCACollapsibleItemContainer container = new DCACollapsibleItemContainerImpl();

        List<DCAColumnSpec> columnSpecs = Arrays.asList(
            new DCAColumnSpec("Name", 50, "name"),
            new DCAColumnSpec("Time period", 25, "timePeriod"),
            new DCAColumnSpec("Channels", 25, "channels")
        );
        container.setColumnSpecs(columnSpecs);
        container.registerExpandHandler(this::handleExpansion);

        Observable<String> response = backendService.getAllAnalyticsForCurrentAccount();
        response.subscribe(val -> {
            List<JsonObject> items = getJsonObjects(val);
            currentUi.access(() -> container.addItems(items));
        }, e -> logger.error("Error while fetching analytics " + e.getMessage()));

        containerLayout.addComponent(container);
    }

    private List<JsonObject> getJsonObjects(String val) {
        List<JsonObject> items = new ArrayList<>();

        try {
            JsonParser parser = new JsonParser();
            JsonElement parse = parser.parse(val);
            JsonObject asJsonObject = parse.getAsJsonObject();
            JsonArray jsonArray = asJsonObject.getAsJsonArray("result");
            for (JsonElement jsonElement : jsonArray) {
                JsonObject item = jsonElement.getAsJsonObject();
                JsonObject format = format(item);
                logger.debug("format = " + format);
                items.add(format);
            }
        } catch (JsonSyntaxException e) {
            logger.warn("Error while parsing json", e);
        }
        return items;
    }

    private Component handleExpansion(JsonObject item) {
        CssLayout expansionLayout = new CssLayout();
        expansionLayout.setWidth(100, PERCENTAGE);
        createChartLayout(expansionLayout, item);
        return expansionLayout;
    }

    private void createChartLayout(CssLayout expansionLayout, JsonObject item) {
        DCADynamicSalesByRecChart salesByRecChart = new DCADynamicSalesByRecChart(currentUi, item);
        expansionLayout.addComponent(salesByRecChart);

        Component controls = createControls(item, salesByRecChart);
        expansionLayout.addComponent(controls);
    }

    private Component createControls(JsonObject item, DCADynamicSalesByRecChart salesByRecChart) {
        CssLayout layout = new CssLayout();
        layout.addStyleName("analytics-controls");
        layout.setWidth(100, PERCENTAGE);

        Label label = new Label("Export format: ");
        label.setWidth(100, PERCENTAGE);
        label.addStyleName("export-format-label");
        CssLayout labelWrapper = wrapWithCssLayout(label, "analytics-control-label", 20);

        DCAComboBox format = new DCAComboBox();
        format.setNullSelectionAllowed(false);
        format.addItem("CSV");
        format.select("CSV");
        format.setWidth(100, PERCENTAGE);
        CssLayout formatWrapper = wrapWithCssLayout(format, "analytics-control-format", 40);


        Button exportButton = new Button("Export");
        Component button = styleButton(exportButton);
        button.setWidth(100, PERCENTAGE);

        DCAFileDownloadWithSupplier extender = new DCAFileDownloadWithSupplier(streamSource, () -> {
            // Set stream source file name
            if (item.has("name")) {
                String name = item.get("name").getAsString();
                name = name + ".csv";
                streamSource.setFileName(name);
            }

            List<NumSales> numSales = salesByRecChart.getChartData();
            return getChartData(numSales);
        });
        extender.extend(exportButton);

        CssLayout exportWrapper = wrapWithCssLayout(button, "analytics-control-export", 40);

        attchEditButton(item, layout);

        CssLayout exportLayout = arrangeExportLayout(labelWrapper, formatWrapper, exportWrapper);
        layout.addComponent(exportLayout);

        return wrapWithCssLayout(layout, "analytics-controls-wrapper", 100);
    }

    private CssLayout arrangeExportLayout(CssLayout labelWrapper, CssLayout formatWrapper, CssLayout exportWrapper) {
        CssLayout exportLayout = new CssLayout();
        exportLayout.addStyleName("analytics-control-export-layout");
        exportLayout.addStyleName("pull-right");
        exportLayout.setWidth(50, PERCENTAGE);

        exportWrapper.addStyleName("pull-right");
        exportLayout.addComponent(exportWrapper);

        formatWrapper.addStyleName("pull-right");
        exportLayout.addComponent(formatWrapper);

        labelWrapper.addStyleName("pull-right");
        exportLayout.addComponent(labelWrapper);
        return exportLayout;
    }

    private void attchEditButton(JsonObject item, CssLayout layout) {
        if (isReadOnly) return;

        Button configButton = new Button("Edit configuration");
        configButton.addClickListener(event -> {
            logger.debug("Creating edit dialog with " + item);
            createEditDialog(layout, item);
        });
        Component button2 = styleButton(configButton);
        button2.setWidth(100, PERCENTAGE);
        CssLayout editWrapper = wrapWithCssLayout(button2, "analytics-control-edit", 20);
        layout.addComponent(editWrapper);
    }

    private void createEditDialog(CssLayout rootLayout, JsonObject item) {
        new DCAEditAnalyticsDialog(currentUi, rootLayout, item, this::updateContainer, successMsgAction, errorMsgAction);
    }

    private String getChartData(List<NumSales> numSales) {
        return backendService.formatData(numSales);
    }

    private Component styleButton(Button button) {
        button.setStyleName("btn-primary cancel-button analytics-control-button");

        CssLayout buttonWrapper = new CssLayout();
        buttonWrapper.addStyleName("btn-wrapper updated-padding");
        buttonWrapper.addComponent(button);

        return buttonWrapper;
    }
}
