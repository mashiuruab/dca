package com.sannsyn.dca.vaadin.widgets.operations.dashboard.salesbyrec;

import com.sannsyn.dca.service.analytics.NumSales;
import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.*;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.addon.charts.model.style.Style;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

/**
 * Chart ui
 * <p>
 * Created by jobaer on 11/2/16.
 */
class DCASalesByRecommenderChart extends CustomComponent {
    private static final Logger logger = LoggerFactory.getLogger(DCASalesByRecommenderChart.class);
    private Chart chart = new Chart(ChartType.COLUMN);
    private ListSeries sale = new ListSeries("Sales by Recommendations");
    private ListSeries day = new ListSeries("Sales");
    private YAxis yAxis = new YAxis();
    private LocalDate startDate = LocalDate.now();

    DCASalesByRecommenderChart() {
        yAxis.setType(AxisType.LOGARITHMIC); // by default logarithmic

        CssLayout layout = new CssLayout();
        layout.addStyleName("chart-wrapper");
        layout.setWidth(100, Unit.PERCENTAGE);
        Chart chart = emailRecommendations();
        chart.setHeight(300, Unit.PIXELS);
        layout.addComponent(chart);
        setCompositionRoot(layout);
    }

    void setRecsText(String name) {
        sale.setName(name);
    }

    void setSalesText(String name) {
        day.setName(name);
    }

    void setLogarithmic(Boolean isLogarithmic) {
        if (isLogarithmic) {
            yAxis.setType(AxisType.LOGARITHMIC);
        } else {
            yAxis.setType(AxisType.LINEAR);
        }
    }

    private Chart emailRecommendations() {
        return drawChart();
    }

    private Chart drawChart() {
        SolidColor lightGray = new SolidColor(204, 204, 204);

        chart.drawChart();

        chart.setJsonConfig("{" +
            "        chart: {" +
            "          animation: false" +
            "        }," +
            "        'tooltip': {" +
            "        'positioner': function(boxWidth, boxHeight, point) {" +
            "            var xPosition = point.plotX + 50;" +
            "            if(point.plotX + boxWidth > this.chart.plotWidth) {" +
            "                xPosition = point.plotX + 80 - boxWidth;" +
            "            }" +
            "            return {" +
            "                x: xPosition," +
            "                y: 0" +
            "            };" +
            "        }" +
            "        }" +
            "    }");

        Configuration conf = chart.getConfiguration();
        conf.setTitle("");

        ChartModel chartModel = conf.getChart();
        chartModel.setSpacingBottom(40);
        chartModel.setSpacingTop(40);

        XAxis xAxis = new XAxis();
        xAxis.setLineWidth(1);
        xAxis.setLineColor(lightGray);
        xAxis.setCategories(
            "1d", "2d", "3d", "4d", "5d", "6d",
            "7d", "8d", "9d", "10d", "11d", "12d",
            "13d", "14d", "15d", "16d", "17d", "18d",
            "19d", "20d", "21d", "22d", "23d", "24d",
            "25d", "26d", "27d", "28d", "29d", "30d"
        );
        Labels labels = xAxis.getLabels();
        labels.setStep(2);
        conf.addxAxis(xAxis);

        yAxis.setTitle("");
        yAxis.setGridLineWidth(0);
        yAxis.setLineWidth(1);
        yAxis.setLineColor(lightGray);
        Labels yAxisLabels = yAxis.getLabels();
        // Get rid of the metric style display of yAxis values
        yAxisLabels.setFormatter(
            "function() {" +
                "var value = this.value; " +
                "return value.toLocaleString();" +
                "}");
        conf.addyAxis(yAxis);

        Legend legend = new Legend();
        legend.setFloating(true);
        legend.setAlign(HorizontalAlign.LEFT);
        legend.setVerticalAlign(VerticalAlign.BOTTOM);
        legend.setX(0);
        legend.setY(40);
        legend.setMargin(20);
        legend.setSymbolHeight(12);
        legend.setSymbolWidth(12);
        legend.setSymbolRadius(6);

        conf.setLegend(legend);

        Tooltip tooltip = configureTooltip();
        conf.setTooltip(tooltip);

        PlotOptionsColumn plotOptions = new PlotOptionsColumn();
        plotOptions.setGroupPadding(0.03);
        plotOptions.setPointPadding(0.03);

        plotOptions.setStacking(Stacking.NORMAL);
        conf.setPlotOptions(plotOptions);

        PlotOptionsColumn options1 = new PlotOptionsColumn();
        options1.setColor(new SolidColor(213, 238, 251));
        day.setPlotOptions(options1);

        conf.addSeries(day);

        PlotOptionsColumn salesOptions = new PlotOptionsColumn();
        salesOptions.setColor(new SolidColor(26, 184, 152));
        sale.setPlotOptions(salesOptions);
        conf.addSeries(sale);

        chart.drawChart(conf);
        return chart;
    }

    private Tooltip configureTooltip() {
        Tooltip tooltip = new Tooltip();
        tooltip.setUseHTML(true);
        attachTooltipFormatter(tooltip);
        tooltip.setBackgroundColor(new SolidColor("#E9E9E9"));
        tooltip.setBorderColor(new SolidColor("#808080"));
        tooltip.setBorderRadius(1);

        Style tooltipStyle = new Style();
        tooltipStyle.setFontFamily("Campton, sans-serif");
        tooltipStyle.setColor(new SolidColor(128, 128, 128));
        tooltip.setStyle(tooltipStyle);
        return tooltip;
    }

    private void attachTooltipFormatter(Tooltip tooltip) {
        String year = "" + startDate.getYear();
        String month = "" + (startDate.getMonthValue() - 1);
        String day = "" + startDate.getDayOfMonth();
        logger.debug("Year: %s, Monday: %s, Day: %s \n", year, month, day);
        tooltip.setFormatter(
            "function() { " +
                "var xVal = this.x ;" +
                "var dLen = xVal.length ;" +
                "var dayMinus = parseInt(xVal.substring(0, dLen - 1));" +
                "var minusSecond = (24*60*60*1000) * dayMinus; " +
                "var currentDate = new Date(" + year + ", " + month + ", " + day + ");" +
                "var date = new Date(currentDate - minusSecond);" +
                "var dd = date.getDate();" +
                "var mm = date.getMonth() + 1 ; " +
                "var yyyy = date.getFullYear();" +
                "var dateStr = '<span class=\"chart-date\"> - ' + dd + '.' + mm + '.' + yyyy + '</span>' ;" +
                "var chartYval = '' + this.series.chart.series[1].data[this.point.x].y; " +
                "if(chartYval == 'null') chartYval = '0'; " +
                "var result = '<span class=\"chart-day\"> ● </span>' + chartYval + '<span class=\"chart-sep\"> | </span>' + '<span class=\"chart-sales\"> ● </span>' + this.point.stackTotal + dateStr;" +
                "return result;" +
                "}");
    }

    private void updateTooltip() {
        Tooltip tooltip = configureTooltip();
        Configuration configuration = chart.getConfiguration();
        configuration.setTooltip(tooltip);
        chart.drawChart(configuration);
    }

    void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        logger.debug("startDate = " + this.startDate);
        updateTooltip();
    }

    void addToDataSeries(List<NumSales> salesList) {
        for (NumSales numSales : salesList) {
            Integer salesWithRecommendation = numSales.getSalesWithRecommendation();
            Integer totalSales = numSales.getSales();
            int diff = totalSales - salesWithRecommendation;
            logger.debug("Date: " + numSales.getDate() + ", sales = " + totalSales + ", withRec = " + salesWithRecommendation);
            sale.addData(salesWithRecommendation);
            day.addData(diff);
        }
    }
}
