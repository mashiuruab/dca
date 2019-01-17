package com.sannsyn.dca.vaadin.widgets.operations.dashboard.salesbyrec;

import com.google.gson.JsonObject;
import com.sannsyn.dca.service.analytics.DCAKeyFiguresService;
import com.sannsyn.dca.service.analytics.NumSales;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.UI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.sannsyn.dca.vaadin.ui.DCAUiHelper.runInUiThread;
import static com.sannsyn.dca.vaadin.widgets.analytics.DCAAnalyticsDataParser.*;

/**
 * A sales by recommendation chart ui
 * <p>
 * Created by jobaer on 4/28/17.
 */
public class DCADynamicSalesByRecChart extends CustomComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCADynamicSalesByRecChart.class);
    private final UI currentUi;

    private CssLayout root = new CssLayout();
    private DCAKeyFiguresService service = new DCAKeyFiguresService(true);
    private List<NumSales> chartData = new ArrayList<>();

    public DCADynamicSalesByRecChart(UI currentUi, JsonObject item) {
        this.currentUi = currentUi;
        root.setWidth(100, Unit.PERCENTAGE);
        root.addStyleName("dynamic-sales-by-rec-chart");
        root.addStyleName("sales-by-recommendation");

        drawChartAsync(item);
        setCompositionRoot(root);
    }

    private void drawChartAsync(JsonObject item) {
        root.removeAllComponents();
        DCASalesByRecommenderChart chart = new DCASalesByRecommenderChart();
        root.addComponent(chart);
        setBasicProperties(chart, item);
        requestChartDataUpdate(chart, item);
    }

    private void requestChartDataUpdate(DCASalesByRecommenderChart chart, JsonObject item) {
        Optional<LocalDate> fromDateOption = getDateProperty(item, FROM_DATE);
        Optional<LocalDate> toDateOption = getDateProperty(item, TO_DATE);
        fromDateOption.ifPresent(fromDate -> toDateOption.ifPresent(toDate -> {
            rx.Observable<List<NumSales>> recData = service.getSalesByRecData(fromDate, toDate);
            Observable<List<NumSales>> safeData = recData.onErrorResumeNext(Observable.just(new ArrayList<>()));
            safeData.subscribe(numSales -> {
                    chartData.clear();
                    chartData.addAll(numSales);

                    logger.debug("chart = " + chart);
                    logger.debug("currentUi = " + currentUi);
                    logger.debug("numSales = " + numSales);
                    currentUi.access(() -> {
                        chart.setStartDate(toDate);
                        chart.addToDataSeries(numSales);
                    });
                },
                e -> logger.error("Unable to fetch data from backend.", e));
        }));
    }

    private void setBasicProperties(DCASalesByRecommenderChart chart, JsonObject item) {
        String salesText = getPropertySafe(item, SALES_TEXT);
        chart.setSalesText(salesText);

        String recsText = getPropertySafe(item, RECS_TEXT);
        chart.setRecsText(recsText);

        Boolean isLogarithmic = getBooleanProperty(item, IS_LOGARITHMIC);
        chart.setLogarithmic(isLogarithmic);
    }

    /**
     * Set a new JsonObject. This will update the existing chart with the information from the new item.
     *
     * @param item The config object on which the chart will depend.
     */
    public void setItem(JsonObject item) {
        logger.debug("Requesting for chart update ... " + item);
        drawChartAsync(item);
    }

    /**
     * Returns the current data based on which the chart is drawn. Useful for exporting the data.
     *
     * @return A List of NumSales object
     */
    public List<NumSales> getChartData() {
        return Collections.unmodifiableList(chartData);
    }
}
