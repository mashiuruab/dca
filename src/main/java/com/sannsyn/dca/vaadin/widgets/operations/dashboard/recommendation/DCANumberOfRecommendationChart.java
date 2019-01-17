package com.sannsyn.dca.vaadin.widgets.operations.dashboard.recommendation;

import com.sannsyn.dca.i18n.Messages;
import com.sannsyn.dca.model.config.DCASelectedService;
import com.sannsyn.dca.presenter.DCADashboardPresenter;
import com.sannsyn.dca.vaadin.component.custom.field.DCAError;
import com.sannsyn.dca.vaadin.servlet.DCASchedulerUtil;
import com.sannsyn.dca.vaadin.servlet.DCAUtils;
import com.sannsyn.dca.vaadin.widgets.operations.dashboard.recommendation.model.DCAContext;
import com.sannsyn.dca.vaadin.widgets.operations.dashboard.recommendation.model.DCAHistogram;
import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.*;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

/**
 * Created by mashiur on 3/10/16.
 */
public class DCANumberOfRecommendationChart extends DCAWidgetLiveUpdateComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCANumberOfRecommendationChart.class);

    private static final Double GRAPH_YAxis_MIN_VALUE = Math.pow(10, -2);

    private static final Map<String, SolidColor> denominationColorMap = new HashMap<String, SolidColor>(){{
        put("Second", new SolidColor("#FBB143"));
        put("Minute", new SolidColor("#2984BF"));
        put("Hour", new SolidColor("#C74443"));
        put("Day", new SolidColor("#33B28F"));
        put("Month", SolidColor.BLACK);
        put("Years", new SolidColor("#6B7ABB"));
        put("Void", SolidColor.DARKBLUE);
    }};

    private DCASelectedService targetService;
    private DCADashboardPresenter dcaDashboardPresenter;
    private DCARecommendationChartUpdateWorker dcaRecommendationChartUpdateWorker;
    private ScheduledFuture scheduledFuture;

    private Chart chart = new Chart(ChartType.AREA);

    private static final int MAX_CONSEC_ERROR = 10;
    private int consecutiveErrorCounter = 0;

    public DCANumberOfRecommendationChart(DCADashboardPresenter dcaDashboardPresenter) {
        this.dcaDashboardPresenter = dcaDashboardPresenter;

        this.addComponent(getTitle());
        chart.setHeight(300, Unit.PIXELS);
        this.addComponent(this.chart);
        this.setStyleName("dca-number-of-recommendation");

        initConfiguration();

        dcaRecommendationChartUpdateWorker = new DCARecommendationChartUpdateWorker(this, UI.getCurrent());

        rx.Observable<DCASelectedService> selectedServiceObservable = DCAUtils.getTargetService();
        selectedServiceObservable.subscribe(dcaSelectedService -> {
            targetService = dcaSelectedService;
            init();
        }, throwable -> {
            logger.error("Error : ", throwable);
        });

    }

    @Override
    public void attach() {
        super.attach();
        startLiveUpdate();
    }

    @Override
    public void startLiveUpdate() {
        logger.info("Starting the ChartUpdate Task");
        this.scheduledFuture = DCASchedulerUtil.getChartUpdateExecutorService().scheduleAtFixedRate
                (this.dcaRecommendationChartUpdateWorker, 10, 5, java.util.concurrent.TimeUnit.SECONDS);
    }

    @Override
    public void shutDownLiveUpdate() {
        logger.info("Closing the ChartUpdate Task");
        if (this.scheduledFuture != null) {
            this.scheduledFuture.cancel(true);
        }

    }

    @Override
    public void detach(){
        super.detach();
        shutDownLiveUpdate();
    }

    private void setRecommendationDate(DataSeriesItem dataSeriesItem, Long currentTimeStamp, int offsetCounter, String denomination) {
        Date date = getTargetDate(currentTimeStamp, offsetCounter, denomination);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.ENGLISH);
        String dateString = simpleDateFormat.format(date);

        dataSeriesItem.setName(dateString);
    }

    private Date getTargetDate(Long currentTimeStamp, int offsetCounter, String denomination) {
        Date date = new Date(currentTimeStamp);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        if (denomination.equals("Second")) {
            calendar.add(Calendar.SECOND, -offsetCounter);
        }
        else if (denomination.equals("Minute")) {
            calendar.add(Calendar.MINUTE, -offsetCounter);
        }
        else if (denomination.equals("Hour")) {
            calendar.add(Calendar.HOUR, -offsetCounter);
        }
        else if (denomination.equals("Day")) {
            calendar.add(Calendar.DAY_OF_MONTH, -offsetCounter);
        }
        else if (denomination.equals("Month")) {
            calendar.add(Calendar.MONTH, -offsetCounter);
        }
        else if (denomination.equals("Years")) {
            calendar.add(Calendar.YEAR, -offsetCounter);
        }

        return calendar.getTime();
    }

    private Double getNumberOfRecommendationPerSecond(Double numberOfRecommendation, String denomination) {
        double secondValue = 1;
        if (denomination.equals("Second")) {
            secondValue = 1;
        }
        else if (denomination.equals("Minute")) {
            secondValue = 60;
        }
        else if (denomination.equals("Hour")) {
            secondValue = 3600;
        }
        else if (denomination.equals("Day")) {
            secondValue = 3600 * 24;
        }
        else if (denomination.equals("Month")) {
            secondValue = 3600 * 24 * 30;
        }
        else if (denomination.equals("Years")) {
            secondValue = 3600 * 24 * 30 * 12;
        }
        return numberOfRecommendation / secondValue;
    }

    private boolean containsDataSeries(List<Series> seriesList, String denomination) {
        for (Series series : seriesList) {
            DataSeries dataSeries = (DataSeries) series;
            if (denomination.equals(dataSeries.getName())) {
                return true;
            }
        }

        return false;
    }

    public void updateChartData() {
        try {
            DCAContext dcaContext = this.dcaDashboardPresenter.getEnsembleData(targetService);
            updateChartData(dcaContext);
            consecutiveErrorCounter = 0;
        } catch (Exception e) {
            consecutiveErrorCounter++;
            logger.error("Error in Chart update : ", e);
            if(!this.getStyleName().contains("no-data-error")) {
                this.addStyleName("no-data-error");
            }
        } finally {
            if (consecutiveErrorCounter > MAX_CONSEC_ERROR) {
                logger.error(String.format("Closing the ChartUpdate Task due to Consecutive Error : %s", MAX_CONSEC_ERROR));
                shutDownLiveUpdate();
            }
        }
    }

    private void onError(Throwable throwable) {
        throwable.printStackTrace();
        logger.error("Error : ", throwable);
        addComponent(new DCAError("Error Happened Loading the component"));
    }

    private void updateChartData(DCAContext dcaContext) {
        List<Series> prevDataSeries = this.chart.getConfiguration().getSeries();

        List<DCAHistogram> dcaHistogramList = dcaContext.getHistograms();
        List<Series> dataSeriesList = new ArrayList<Series>();


        int xCounter = 0;
        int dataSeriesCounter = 0;
        int itemUpdateCounter = 0;

        for (DCAHistogram dcaHistogram : dcaHistogramList) {
            String denomination = dcaHistogram.getDenomination();
            List<Integer> values = dcaHistogram.getHistogram();

            if (values.isEmpty()) {
                continue;
            }

            DataSeries dataSeries;
            if (!containsDataSeries(prevDataSeries, denomination)) {
                dataSeries = new DataSeries(denomination);
            } else {
                dataSeries = (DataSeries) prevDataSeries.get(dataSeriesCounter++);
            }

            int offsetCounter = 1;
            boolean previousItemsNotFound = dataSeries.getData().isEmpty();

            for (Integer histogram :  values) {
                Double histogramValue = Double.valueOf(histogram);
                Double numberOfRecommendation = histogramValue;
                if (histogramValue == 0) {
                    histogramValue = GRAPH_YAxis_MIN_VALUE;
                    numberOfRecommendation = GRAPH_YAxis_MIN_VALUE;
                } else {
                    histogramValue = getNumberOfRecommendationPerSecond(histogramValue, denomination);

                    if (histogramValue < GRAPH_YAxis_MIN_VALUE) {
                        histogramValue = GRAPH_YAxis_MIN_VALUE * 10;
                    }
                }

                DataSeriesItem dataSeriesItem;
                if (previousItemsNotFound) {
                    dataSeriesItem = new DataSeriesItem(xCounter, histogramValue);
                    setRecommendationDate(dataSeriesItem, dcaContext.getTimestamp(), offsetCounter, denomination);
                    dataSeriesItem.setId(String.valueOf(numberOfRecommendation));
                    dataSeries.add(dataSeriesItem);
                    offsetCounter++;
                } else {
                    dataSeriesItem = dataSeries.get(offsetCounter - 1);
                    if (histogramValue.doubleValue() != dataSeriesItem.getY().doubleValue()) {
                        dataSeriesItem.setY(histogramValue);
                        setRecommendationDate(dataSeriesItem, dcaContext.getTimestamp(), offsetCounter, denomination);
                        dataSeriesItem.setId(String.valueOf(numberOfRecommendation));
                        dataSeries.update(dataSeriesItem);
                        itemUpdateCounter++;
                    }

                    offsetCounter++;
                }

                xCounter++;
            }

            dataSeriesList.add(dataSeries);
        }

        logger.info(String.format("Total Data Updated on live Update : %s ", itemUpdateCounter));


        this.chart.getConfiguration().setSeries(dataSeriesList);
    }

    private void initConfiguration() {
        this.chart.setStyleName("number-of-recommendation-chart-container");
        this.chart.setId("recommendation-chart-id");
        this.chart.setWidthUndefined();
        
        this.chart.setJsonConfig("{" +
                "        lang: {noData: '<span class=\"loading\">Loading.....</span><span class=\"no-data\">No data to display</span>'}," +
                "        chart: {" +
                "          animation: false" +
                "        }," +
                "        'tooltip': {" +
                "        'positioner': function(boxWidth, boxHeight, point) {" +
                "            var xPosition = point.plotX + 50;" +
                "            if(point.plotX + (boxWidth / 2) > this.chart.plotWidth) {" +
                "                xPosition = point.plotX;" +
                "            }" +
                "            return {" +
                "                x: xPosition," +
                "                y: 5" +
                "            };" +
                "        }" +
                "        }" +
                "    }");

        Configuration chartConfiguration = this.chart.getConfiguration();
        chartConfiguration.getNoData().setUseHTML(true);

        Title chartTitle = new Title("");
        chartConfiguration.setTitle(chartTitle);
        chartConfiguration.getLegend().setSymbolHeight(12);
        chartConfiguration.getLegend().setSymbolWidth(12);
        chartConfiguration.getLegend().setSymbolRadius(6);
    }

    private void init() {
        Configuration chartConfiguration = this.chart.getConfiguration();
        /*Data generation code*/
        updateChartData();

        List<String> categoryList = new ArrayList<String>();

        for(Series series : this.chart.getConfiguration().getSeries()) {
            DataSeries dataSeries = (DataSeries) series;
            PlotOptionsArea plotOptionsArea = new PlotOptionsArea();
            plotOptionsArea.setColor(denominationColorMap.get(dataSeries.getName()));
            plotOptionsArea.setLineWidth(0);
            plotOptionsArea.getStates().getHover().setEnabled(false);
            plotOptionsArea.getStates().getHover().setLineWidth(0);
            plotOptionsArea.getStates().getHover().setLineWidthPlus(0);

            Marker marker = new Marker();
            marker.setEnabled(false);
            marker.setSymbol(MarkerSymbolEnum.CIRCLE);
            marker.setRadius(2);
            plotOptionsArea.setMarker(marker);
            dataSeries.setPlotOptions(plotOptionsArea);

            for(DataSeriesItem dataSeriesItem : dataSeries.getData()) {
                categoryList.add(String.valueOf(dataSeriesItem.getX()));
            }
        }

        String[] categories = new String[categoryList.size()];
        int counter = 0;
        while (counter < categoryList.size()) {
            categories[counter] = categoryList.get(counter);
            counter++;
        }

        XAxis xAxis = new XAxis();
        xAxis.setCategories(categories);
        Labels labels = new Labels();
        labels.setFormatter("function() {" +
                "  var labelValue = parseInt(this.value) + 1;" +
                "  if(labelValue >= 1 && labelValue <= 60 && (labelValue % 10) == 0) {" +
                "    return labelValue + 's';" +
                "  } else if(labelValue >= 61 && labelValue <= 120 && ((labelValue - 60) % 10) == 0) {" +
                "    return labelValue - 60 + 'm';" +
                "  } else if(labelValue >= 121 && labelValue <= 144 && ((labelValue - 120) % 8) == 0) {" +
                "    return labelValue - 120 + 'h';" +
                "  } else if(labelValue >= 145 && labelValue <= 174 && ((labelValue - 144) % 10) == 0) {" +
                "    return labelValue - 144 + 'd';" +
                "  } else if(labelValue >= 175 && labelValue <= 186 && ((labelValue - 174) % 6) == 0) {" +
                "    return labelValue - 174 + 'M';" +
                "  } else if(labelValue >= 187 && labelValue <= 198 && ((labelValue - 186) % 10) == 0) {" +
                "    return labelValue - 186 + 'y';" +
                "  } else {" +
                "    return '';" +
                "  }" +
                "}");

        labels.setStep(1);
        labels.getStyle().setFontSize("12px");
        xAxis.setLabels(labels);
        chartConfiguration.addxAxis(xAxis);

        YAxis yAxis = new YAxis();
        yAxis.setType(AxisType.LOGARITHMIC);
        yAxis.setTitle("");
        yAxis.setMin(GRAPH_YAxis_MIN_VALUE);
        Labels yLabels = new Labels();
        yLabels.setFormatter("function() {" +
                "    var yLabelValue = parseFloat(this.value);" +
                "    var chart = this.chart;" +
                "    var minValue = chart.options.yAxis[0].min;" +
                "    if (yLabelValue == minValue) {" +
                "        return 0;" +
                "    } else if (yLabelValue < 1 && !this.isLast) {" +
                "        return '';" +
                "    } else {" +
                "        return this.value;" +
                "    }  " +
                "}");
        yAxis.setLabels(yLabels);
        chartConfiguration.addyAxis(yAxis);

        Tooltip tooltip = new Tooltip();
        tooltip.setFormatter("function() {" +
                "  var label = (this.point.id < 1) ? 0 : parseInt(this.point.id);" +
                "  return '<b>' + label + '</b> - ' + this.key;" +
                "}");
        tooltip.setBackgroundColor(new SolidColor("#E9E9E9"));
        tooltip.setBorderColor(new SolidColor("#808080"));
        tooltip.setBorderRadius(1);
        chartConfiguration.setTooltip(tooltip);
        chartConfiguration.getTooltip().setEnabled(false);


        Crosshair xCrossHair = new Crosshair();
        xCrossHair.setColor(SolidColor.GRAY);
        xCrossHair.setDashStyle(DashStyle.SOLID);
        xCrossHair.setZIndex(0);
        xCrossHair.setWidth(2);

        /*chartConfiguration.getxAxis().setCrosshair(xCrossHair);*/

        Legend legend = this.chart.getConfiguration().getLegend();
        legend.getItemStyle().setFontSize("12px");

        this.chart.drawChart(chartConfiguration);

    }

    private Component getTitle() {
        Label titleLabel = new Label(Messages.getInstance().getMessage("widget.number.of.recommendations.title"), ContentMode.HTML);
        titleLabel.setWidthUndefined();
        titleLabel.setStyleName("number-of-recommendation-chart-title");
        return titleLabel;
    }
}
