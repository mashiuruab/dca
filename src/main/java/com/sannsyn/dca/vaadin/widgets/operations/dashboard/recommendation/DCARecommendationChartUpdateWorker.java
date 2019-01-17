package com.sannsyn.dca.vaadin.widgets.operations.dashboard.recommendation;

import com.vaadin.ui.UI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mashiur on 3/14/16.
 */
public class DCARecommendationChartUpdateWorker implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(DCARecommendationChartUpdateWorker.class.getName());

    private DCANumberOfRecommendationChart dcaNumberOfRecommendationChart;
    private UI ui;

    public DCARecommendationChartUpdateWorker(DCANumberOfRecommendationChart dcaNumberOfRecommendationChart, UI ui) {
        this.dcaNumberOfRecommendationChart = dcaNumberOfRecommendationChart;
        this.ui = ui;
    }

    @Override
    public void run() {
        this.ui.access(new Runnable() {
            @Override
            public void run() {
                logger.info("Updating NUMBER Of Recommendation chart");
                dcaNumberOfRecommendationChart.updateChartData();
            }
        });
    }
}
